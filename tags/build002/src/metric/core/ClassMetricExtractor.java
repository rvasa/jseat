package metric.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.io.InputDataSet;
import metric.core.model.ClassMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.MethodMetric;
import metric.core.vocabulary.TypeModifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassMetricExtractor
{
	private ClassNode classNode;
	private ClassMetricData cmd;

	private InputDataSet ids;
	public static int innerclassesProcessed = 0;

	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	public static int METHODS_PROCESSED = 0;

	public ClassMetricExtractor(InputStream istream, InputDataSet ids)
			throws IOException
	{
		ClassReader cr = new ClassReader(istream);
		classNode = new ClassNode();
		cr.accept(classNode, ClassReader.SKIP_DEBUG);
		this.ids = ids;

		// Log organiser should check whether this logger already exists first
		// so should be ok to cal here.
		LogOrganiser.addLogger(logger);
	}

	public ClassMetricData extract()
	{
		cmd = new ClassMetricData();

		// Extract class related metrics.
		extractClassMetrics();
		// Extract and create method metrics
		extractMethodMetrics();
		// Extract dependencies from fields.
		extractFieldDependencies();
		// Extract dependencies from methods.
		extractMethodDependencies();
		// Extract, create and merge inner classes.
		extractInnerClasses();

		return cmd;
	}

	/**
     * Extracts and merges inner class data with this class.
     */
	private void extractInnerClasses()
	{
		Iterator it = classNode.innerClasses.iterator();
		while (it.hasNext())
		{
			InnerClassNode icn = (InnerClassNode) it.next();
			// Skip inner class processing if it has the same name as me (the
			// parent class). Unsure of why this happens.
			if (!icn.name.equals(classNode.name))
			{
				InputStream innerStream = ids.getInputStream(icn.name
						+ ".class");
				if (innerStream != null)
				{
					try
					{
						ClassMetricExtractor cme = new ClassMetricExtractor(
								innerStream, ids);
						ClassMetricData innerClass = cme.extract();

						// merge inner class metrics with this class.
						cmd.mergeInnerClass(innerClass);
						
						// Free up class as it is no longer needed.
						innerClass.methods = null;
						innerClass = null;
					} catch (IOException e)
					{
						logger.log(Level.WARNING, e.toString());
					} // handle for the moment.
					catch (ArrayIndexOutOfBoundsException e)
					{
						// logger.log(Level.WARNING, e.toString());
					}// handle for the moment.
				} else
				{
					// System.out.println("Unable to get stream for: " +
					// icn.name);
				}
			} else
			{
			} // skipping self
		}
	}

	/**
     * Extracts and updates MethodMetricData for this classes methods.
     */
	private void extractMethodMetrics()
	{
		// MethodNode methodNode = (MethodNode) methods.get(i);
		MethodMetricExtractor mme = new MethodMetricExtractor(classNode);
		// Add the extracted MethodMetricData to the current ClassMetricData
		HashMap<String, int[]> methodMap = mme.extract();

		for (int[] mm : methodMap.values())
			mergeMethodMetricsWithClass(mm);
		
		cmd.methods = new MethodMetricMap(methodMap);
	}

	/**
     * Merges the MethodMethodData metrics with the current ClassMetricData.
     * 
     * @param mmd The MethodMetricData.
     */
	private void mergeMethodMetricsWithClass(int[] mm)
	{
		cmd.incrementMetric(
				ClassMetric.BRANCH_COUNT,
				mm[MethodMetric.BRANCH_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.CONSTANT_LOAD_COUNT,
				mm[MethodMetric.CONSTANT_LOAD_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.INCREMENT_OP_COUNT,
				mm[MethodMetric.INCREMENT_OP_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.ISTORE_COUNT,
				mm[MethodMetric.ISTORE_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.ILOAD_COUNT,
				mm[MethodMetric.ILOAD_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.LOCAL_VAR_COUNT,
				mm[MethodMetric.LOCAL_VAR_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.METHOD_CALL_COUNT,
				mm[MethodMetric.METHOD_CALL_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.REF_LOAD_OP_COUNT,
				mm[MethodMetric.REF_LOAD_OP_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.REF_STORE_OP_COUNT,
				mm[MethodMetric.REF_STORE_OP_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.LOAD_FIELD_COUNT,
				mm[MethodMetric.LOAD_FIELD_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.STORE_FIELD_COUNT,
				mm[MethodMetric.STORE_FIELD_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.THROW_COUNT,
				mm[MethodMetric.THROW_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.TRY_CATCH_BLOCK_COUNT,
				mm[MethodMetric.TRY_CATCH_BLOCK_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.TYPE_INSN_COUNT,
				mm[MethodMetric.TYPE_INSN_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.ZERO_OP_INSN_COUNT,
				mm[MethodMetric.ZERO_OP_INSN_COUNT.ordinal()]);

		cmd.incrementMetric(
				ClassMetric.METHOD_CALL_COUNT,
				mm[MethodMetric.METHOD_CALL_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.IN_METHOD_CALL_COUNT,
				mm[MethodMetric.IN_METHOD_CALL_COUNT.ordinal()]);
		cmd.incrementMetric(
				ClassMetric.EX_METHOD_CALL_COUNT,
				mm[MethodMetric.EX_METHOD_CALL_COUNT.ordinal()]);

		if (mm[MethodMetric.SCOPE.ordinal()] == TypeModifier.PUBLIC.ordinal())
			cmd.incrementMetric(ClassMetric.PUBLIC_METHOD_COUNT);
		else if (mm[MethodMetric.SCOPE.ordinal()] == TypeModifier.PRIVATE
				.ordinal())
			cmd.incrementMetric(ClassMetric.PRIVATE_METHOD_COUNT);
		else if (mm[MethodMetric.SCOPE.ordinal()] == TypeModifier.PROTECTED
				.ordinal())
			cmd.incrementMetric(ClassMetric.PROTECTED_METHOD_COUNT);

		if (mm[MethodMetric.IS_ABSTRACT.ordinal()] == 1)
			cmd.incrementMetric(ClassMetric.ABSTRACT_METHOD_COUNT);

		if (mm[MethodMetric.IS_FINAL.ordinal()] == 1)
			cmd.incrementMetric(ClassMetric.FINAL_METHOD_COUNT);

		if (mm[MethodMetric.IS_STATIC.ordinal()] == 1)
			cmd.incrementMetric(ClassMetric.STATIC_METHOD_COUNT);

		if (mm[MethodMetric.IS_SYNCHRONIZED.ordinal()] == 1)
			cmd.incrementMetric(ClassMetric.SYNCHRONIZED_METHOD_COUNT);
	}

	/**
     * Extracts and updates fields dependencies.
     */
	private void extractFieldDependencies()
	{
		Iterator<?> iter = classNode.fields.iterator();
		while (iter.hasNext())
		{
			FieldNode fn = (FieldNode) iter.next();

			// Field count/deps.
			if ((fn.access & Opcodes.ACC_PUBLIC) != 0)
				cmd.incrementMetric(ClassMetric.PUBLIC_FIELD_COUNT);
			else if ((fn.access & Opcodes.ACC_PRIVATE) != 0)
				cmd.incrementMetric(ClassMetric.PRIVATE_FIELD_COUNT);
			else if ((fn.access & Opcodes.ACC_PROTECTED) != 0)
				cmd.incrementMetric(ClassMetric.PROTECTED_FIELD_COUNT);

			if ((fn.access & Opcodes.ACC_STATIC) != 0)
				cmd.incrementMetric(ClassMetric.STATIC_FIELD_COUNT);
			if ((fn.access & Opcodes.ACC_FINAL) != 0)
				cmd.incrementMetric(ClassMetric.FINAL_FIELD_COUNT);
			Type t = Type.getType(fn.desc);
			addDependency(cmd, t);
		}
	}

	/**
     * Extracts and updates dependencies from methods.
     */
	private void extractMethodDependencies()
	{
		Iterator<?> iter = classNode.methods.iterator();
		while (iter.hasNext())
		{
			MethodNode mn = (MethodNode) iter.next();
			for (int i = 0; i < mn.exceptions.size(); i++)
			{
				cmd.dependencies.add((String) mn.exceptions.get(i));
			}
			Type t = Type.getReturnType(mn.desc);
			addDependency(cmd, t);

			Type[] argTypes = Type.getArgumentTypes(mn.desc);
			for (int i = 0; i < argTypes.length; i++)
			{
				t = argTypes[i];
				addDependency(cmd, t);
			}
		}
		cmd.setSimpleMetric(ClassMetric.FAN_OUT_COUNT, cmd.dependencies.size());
	}

	/**
     * Extracts and updates class related metrics for the current
     * ClassMetricData.
     */
	private void extractClassMetrics()
	{
		cmd.setSimpleMetric(
				ClassMetric.INNER_CLASS_COUNT,
				classNode.innerClasses.size());
		cmd.setSimpleMetric(ClassMetric.INTERFACE_COUNT, classNode.interfaces
				.size());
		cmd.setSimpleMetric(ClassMetric.FIELD_COUNT, classNode.fields.size());
		cmd.setSimpleMetric(ClassMetric.METHOD_COUNT, classNode.methods.size());

		cmd.setProperty(ClassMetric.NAME, classNode.name);
		cmd.setProperty(ClassMetric.SUPER_CLASS_NAME, classNode.superName
				.trim());

		updateClassStats();
	}

	/**
     * Updates the super class name and various other statistics. Like whether
     * or not the class is an exception class, whether it is abstract, an
     * interface etc.
     */
	private void updateClassStats()
	{
		// Ignore java.lang.Object
		if (!cmd.get(ClassMetric.SUPER_CLASS_NAME).equals("java/lang/Object"))
			cmd.setSimpleMetric(ClassMetric.SUPER_CLASS_COUNT, 1);
		if (cmd.get(ClassMetric.SUPER_CLASS_NAME).contains("Exception"))
			cmd.setSimpleMetric(ClassMetric.IS_EXCEPTION, 1);
		if (cmd.get(ClassMetric.SUPER_CLASS_NAME).equals("java/lang/Throwable"))
			cmd.setSimpleMetric(ClassMetric.IS_EXCEPTION, 1);
		if ((classNode.access & Opcodes.ACC_ABSTRACT) != 0)
			cmd.setSimpleMetric(ClassMetric.IS_ABSTRACT, 1);
		if ((classNode.access & Opcodes.ACC_INTERFACE) != 0)
			cmd.setSimpleMetric(ClassMetric.IS_INTERFACE, 1);

		if ((classNode.access & Opcodes.ACC_PUBLIC) != 0)
			cmd.setSimpleMetric(ClassMetric.IS_PUBLIC, 1);
		else if ((classNode.access & Opcodes.ACC_PRIVATE) != 0)
			cmd.setSimpleMetric(ClassMetric.IS_PRIVATE, 1);
		else if ((classNode.access & Opcodes.ACC_PROTECTED) != 0)
			cmd.setSimpleMetric(ClassMetric.IS_PROTECTED, 1);
	}

	/**
     * Updates the specified ClassMetricData with the specified class type.
     * 
     * @param cmd The ClassMetricData.
     * @param t The class type.
     */
	private void addDependency(ClassMetricData cmd, Type t)
	{
		if (t == null)
			return; // do nothing
		if (t.getSort() == Type.OBJECT)
		{
			String depName = t.getInternalName();
			cmd.dependencies.add(depName);
			if (depName.startsWith("java/io"))
				cmd.setSimpleMetric(ClassMetric.IS_IO_CLASS, 1);
			if (depName.startsWith("java/nio"))
				cmd.setSimpleMetric(ClassMetric.IS_IO_CLASS, 1);
			if (depName.startsWith("java/awt/event"))
				cmd.setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
			if (depName.startsWith("java/applet"))
				cmd.setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
			if (depName.startsWith("javax/swing"))
				cmd.setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
			if (depName.startsWith("javax/swing/event"))
				cmd.setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
			if (depName.startsWith("org/eclipse/swt/events"))
				cmd.setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
		}
	}
}
