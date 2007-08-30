package metric.core;

import java.util.HashMap;
import java.util.List;

import metric.core.vocabulary.MethodMetric;
import metric.core.vocabulary.TypeModifier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Extracts and returns a list of<code>MethodMetricData</code> objects for
 * the specified ClassNode.
 * 
 * @author Joshua Hayes,Swinburne University (ICT),2007
 */
public class MethodMetricExtractor
{
	private ClassNode classNode;

	public MethodMetricExtractor(ClassNode classNode)
	{
		this.classNode = classNode;
	}

	/**
     * @return Extracts and returns a HashMap of Methods for the current
     *         <code>ClassNode</code>.
     */
	public HashMap<String, int[]> extract()
	{
		HashMap<String, int[]> methodsMap = new HashMap<String, int[]>();
		List<?> methods = classNode.methods;
		for (int i = 0; i < methods.size(); ++i)
		{
			MethodNode methodNode = (MethodNode) methods.get(i);
			int[] methodMetrics = new int[MethodMetric.values().length];

			methodMetrics[MethodMetric.LOCAL_VAR_COUNT.ordinal()] = methodNode.maxLocals;

			// Update method scope.
			extractAndSetScope(methodNode, methodMetrics);

			// Update type modifiers.
			extractAndUpdateTypeModifiers(methodNode, methodMetrics);

			// Extract and update instructions.
			if (methodNode.instructions.size() > 0)
				extractInstructions(methodNode, methodMetrics);

			// Add to our processed method map.
			String methodKey = methodNode.name + " " + methodNode.desc;
			methodsMap.put(methodKey, methodMetrics);
		}
		return methodsMap;
	}

	/**
     * Extracts the scope of the specified MethodNode and updates the specified
     * MethodMetricData.
     * 
     * @param methodNode The MethodNode.
     * @param mmd The MethodMetricData.
     */
	private void extractAndSetScope(MethodNode methodNode, int[] mm)
	{
		if ((methodNode.access & Opcodes.ACC_PRIVATE) != 0)
			mm[MethodMetric.SCOPE.ordinal()] = TypeModifier.PRIVATE.ordinal();
		else if ((methodNode.access & Opcodes.ACC_PROTECTED) != 0)
			mm[MethodMetric.SCOPE.ordinal()] = TypeModifier.PROTECTED.ordinal();
		else if (((methodNode.access & Opcodes.ACC_PUBLIC) != 0) && ((classNode.access & Opcodes.ACC_PUBLIC) != 0))
			mm[MethodMetric.SCOPE.ordinal()] = TypeModifier.PUBLIC.ordinal();
	}

	/**
     * Extracts any type modifiers specified for MethodNode and updates the
     * specified MethodMetricData with these modifers.
     * 
     * @param methodNode The MethodNode.
     * @param mmd The MethodMetricData.
     */
	private void extractAndUpdateTypeModifiers(MethodNode methodNode, int[] mm)
	{
		if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0)
			mm[MethodMetric.IS_ABSTRACT.ordinal()] = 1;
		if ((methodNode.access & Opcodes.ACC_FINAL) != 0)
			mm[MethodMetric.IS_FINAL.ordinal()] = 1;
		if ((methodNode.access & Opcodes.ACC_STATIC) != 0)
			mm[MethodMetric.IS_STATIC.ordinal()] = 1;
		if ((methodNode.access & Opcodes.ACC_SYNCHRONIZED) != 0)
			mm[MethodMetric.IS_SYNCHRONIZED.ordinal()] = 1;
	}

	/**
     * Extracts all instructional related information from the specified
     * MethodNode and updates the respective MethodMetricData.
     * 
     * @param methodNode The MethodNode.
     * @param mmd The MethodMetricData.
     */
	private void extractInstructions(MethodNode methodNode, int[] mm)
	{
		for (int i = 0; i < methodNode.instructions.size(); ++i)
		{
			// Get instruction node.
			Object insn = methodNode.instructions.get(i);
			// Have it accept our visitors and update the provided
			// MethodMetricData.
			((AbstractInsnNode) insn).accept(getVariableInstructionVisitor(mm));
			// Try/Catch blocks.
			((AbstractInsnNode) insn).accept(getTryCatchBlockVisitor(mm));
			// Fields.
			((AbstractInsnNode) insn).accept(getFieldVisitor(mm));
			// Jump/branching.
			((AbstractInsnNode) insn).accept(getJumpInstructionVisitor(mm));
			// Switching.
			((AbstractInsnNode) insn).accept(getSwitchVisitor(mm));
			// Op/Loads.
			((AbstractInsnNode) insn).accept(getOpAndLoadVisitor(mm));
			// Type.
			((AbstractInsnNode) insn).accept(getTypeVisitor(mm));
			// Methods.
			((AbstractInsnNode) insn).accept(getMethodVisitor(mm));
		}
	}

	/**
     * A visitor for updating variable instruction counts.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getVariableInstructionVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitVarInsn(int opcode, int var)
			{
				if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.DLOAD)
				{
					mm[MethodMetric.ILOAD_COUNT.ordinal()]++;
				}
				if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.DSTORE)
				{
					mm[MethodMetric.ISTORE_COUNT.ordinal()]++;
				}
				if (opcode == Opcodes.ALOAD)
				{
					mm[MethodMetric.REF_LOAD_OP_COUNT.ordinal()]++;
				}
				if (opcode == Opcodes.ASTORE)
				{
					mm[MethodMetric.REF_STORE_OP_COUNT.ordinal()]++;
				}
			}
		};
		return mv;
	}

	/**
     * A visitor for updating try-catch block occurrences.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getTryCatchBlockVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
			{
				mm[MethodMetric.TRY_CATCH_BLOCK_COUNT.ordinal()]++;
			}
		};
		return mv;
	}

	/**
     * A visitor for updating field instructions.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getFieldVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitFieldInsn(int opcode, String owner, String name, String desc)
			{
				if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)
				{
					mm[MethodMetric.STORE_FIELD_COUNT.ordinal()]++;
				} else if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC)
				{
					mm[MethodMetric.LOAD_FIELD_COUNT.ordinal()]++;
				}
			}
		};
		return mv;
	}

	/**
     * A visitor for updating type counts.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getTypeVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitTypeInsn(int opcode, String desc)
			{
            	if (opcode == Opcodes.INSTANCEOF) mm[MethodMetric.INSTANCE_OF_COUNT.ordinal()]++;
            	if (opcode == Opcodes.CHECKCAST) mm[MethodMetric.CHECK_CAST_COUNT.ordinal()]++;
            	if (opcode == Opcodes.NEW)
            	{
            		mm[MethodMetric.NEW_COUNT.ordinal()]++;
            		mm[MethodMetric.TYPE_CONSTRUCTION_COUNT.ordinal()]++;
            	}
            	if (opcode == Opcodes.ANEWARRAY)
            	{
            		mm[MethodMetric.NEW_ARRAY_COUNT.ordinal()]++;
            		mm[MethodMetric.TYPE_CONSTRUCTION_COUNT.ordinal()]++;
            	}
            	
				mm[MethodMetric.TYPE_INSN_COUNT.ordinal()]++;
			}

			public void visitInsn(int opcode)
			{
				mm[MethodMetric.ZERO_OP_INSN_COUNT.ordinal()]++;
				if (opcode == Opcodes.ATHROW)
				{
					// TODO: Remove this later.
					mm[MethodMetric.BRANCH_COUNT.ordinal()]++;
					mm[MethodMetric.THROW_COUNT.ordinal()]++;
				}
			}
		};
		return mv;
	}

	/**
     * A visitor for updating jump/branching counts.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getJumpInstructionVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitJumpInsn(int opcode, Label label)
			{
				if (opcode != Opcodes.GOTO)
				{
					mm[MethodMetric.BRANCH_COUNT.ordinal()]++;
				}
			}
		};
		return mv;
	}

	/**
     * A visitor for updating switch statement branching.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getSwitchVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
			{
				mm[MethodMetric.BRANCH_COUNT.ordinal()] += labels.length;
			}

			public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
			{
				mm[MethodMetric.BRANCH_COUNT.ordinal()] += labels.length;
			}
		};
		return mv;
	}

	/**
     * A visitor for updating op and load counts.
     * 
     * @param mmd The MethodMetricData that should be updated.
     * @return The TraceMethodVisitor.
     */
	private TraceMethodVisitor getOpAndLoadVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitLdcInsn(Object cst)
			{
				mm[MethodMetric.CONSTANT_LOAD_COUNT.ordinal()]++;
			}

			public void visitIincInsn(int var, int increment)
			{
				mm[MethodMetric.INCREMENT_OP_COUNT.ordinal()]++;
			}
		};
		return mv;
	}

	private TraceMethodVisitor getMethodVisitor(final int[] mm)
	{
		TraceMethodVisitor mv = new TraceMethodVisitor()
		{
			public void visitMethodInsn(int opcode, String owner, String n, String d)
			{
				mm[MethodMetric.METHOD_CALL_COUNT.ordinal()]++;
				if (owner.equals(classNode.name))
					mm[MethodMetric.IN_METHOD_CALL_COUNT.ordinal()]++;
				else
					mm[MethodMetric.EX_METHOD_CALL_COUNT.ordinal()]++;
			}
		};
		return mv;
	}

	// public void visitMethodInsn(int opcode, String owner,
	// String n, String d)
	// {
	// cmd.incrementMetric(ClassMetric.METHOD_CALL_COUNT);
	// mmd.incrementMetric(MethodMetric.METHOD_CALL_COUNT);
	// if (owner.equals(cmd.get(ClassMetric.NAME)))
	// cmd
	// .incrementMetric(ClassMetric.IN_METHOD_CALL_COUNT);
	// else
	// cmd
	// .incrementMetric(ClassMetric.EX_METHOD_CALL_COUNT);
	// cmd.dependencies.add(owner);
	// }

}
