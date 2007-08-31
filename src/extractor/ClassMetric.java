package extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Data class - This will hold the metrics for a class
 * 
 * ClassMetricData
 * @author rvasa
 */
public class ClassMetric implements Comparable<ClassMetric>
{
	public final String[] metrics = 
	{"branchCount",
	 "constantLoadCount", "iLoadCount", "iStoreCount", "loadFieldCount", "storeFieldCount",
	 "refLoadOpCount", "refStoreOpCount", "superClassCount", "innerClassCount", "exceptionCount",
	 "staticMethodCount", "synchronizedMethodCount", "finalMethodCount", "staticFieldCount", "finalFieldCount",
	 "isAbstract", "isPublic", "isPrivate", "isInterface", "isProtected", "isException",
	 "fanOutCount", "interfaceCount", "methodCount", "tryCatchBlockCount", "typeInsnCount",
	 "fieldCount", "methodCallCount", "publicMethodCount", "privateMethodCount", "abstractMethodCount",
	 "protectedMethodCount", "privateFieldCount", "protectedFieldCount", "publicFieldCount",
	 "zeroOpInsnCount", "localVarCount", "internalMethodCallCount", "externalMethodCallCount",
	 "internalFanOutCount", "incrementOpCount", "throwCount"};//, "access"};

	public final String[] distanceComputationMetrics =
	{"branchCount",
			 "constantLoadCount", "iLoadCount", "iStoreCount", "loadFieldCount", "storeFieldCount",
			 "refLoadOpCount", "refStoreOpCount", "superClassCount", "innerClassCount", "exceptionCount",
			 "fanOutCount", "interfaceCount", "methodCount", "tryCatchBlockCount", "typeInsnCount",
			 "fieldCount", "methodCallCount", "publicMethodCount", "privateMethodCount", "abstractMethodCount",
			 "protectedMethodCount", "privateFieldCount", "protectedFieldCount", "publicFieldCount",
			 "zeroOpInsnCount", "localVarCount", "internalMethodCallCount", "externalMethodCallCount",
			 "internalFanOutCount", "incrementOpCount", "throwCount"};
//	{
//	 "fieldCount", "methodCount", "fanOutCount", "interfaceCount", "superClassCount", "paramCount",
	 //"fieldCount"
//     "loadCount"	
	 //"methodCount"		
	 //"branchCount",
//	 "methodCallCount","loadCount",
//	 "storeCount"
//	};

	
	public String className = null;  // Fully-qualified class name
    public String superClassName = "";
    
    public static final int ECAT_NEVER_MODIFIED = 0;
    public static final int ECAT_MODIFIED_AFTER_BIRTH = 1;
    public static final int ECAT_NEW_BORN = 2;
    
    public static final int ECAT_UNCHANGED = 0;  // evolution category
    public static final int ECAT_MODIFIED = 1;  // evolution category
    public static final int ECAT_DELETED = 2; // evolution category
    public static final int ECAT_NEW = 3;  // evolution category
    public int evolutionStatus = ECAT_UNCHANGED; // unknown initially, set after all processing complete
    public int nextVersionStatus = ECAT_UNCHANGED; // what will happen in the next version?
    public int evolutionDistance = 0; // distance from previous version CM, between 0 and 100
    
    //public HashMap<String, Integer> metrics = new HashMap<String, Integer>(20);
    
    public int superClassCount = 0; // will be set to 1, if it is not java/lang/Object
    public int fanOutCount = 0; // Non-primitive dependencies, including library deps
    public int internalFanOutCount = 0;
    public int fanInCount = 0;
    public int branchCount = 0; // Total number of branch instructions
    public int externalMethodCallCount = 0;
    public int internalMethodCallCount = 0;
    public int staticMethodCount = 0;
    public int methodCallCount = 0;
    public int methodCount = 0;  // including constructions & static initializers
    public int publicMethodCount = 0;    
    public int interfaceCount = 0; // Number of implemented interfaces
    public int fieldCount = 0; // all fields defined in this class
    public int innerClassCount = 0;
    public int isInterface = 0;
    public int isAbstract = 0;
    public int isPublic = 0;
    public int isPrivate = 0;
    public int isProtected = 0;
    public int isException = 0;
    public int throwCount = 0;
    public int paramCount = 0;
    public int access = 0;
    public int exceptionCount = 0;
    
    public int instanceOfCount = 0;
    public int checkCastCount = 0;
    public int newCount = 0;
    public int newArrayCount = 0;
    public int typeConstructionCount = 0;
    
    public int refLoadOpCount = 0; // Number of reference loads
    public int refStoreOpCount = 0; // Number of reference stores
    public int loadFieldCount = 0;  // Number of times a field was loaded
    public int storeFieldCount = 0; // Number of times a field was stored
    public int tryCatchBlockCount = 0; // Number of try-catch blocks
    public int localVarCount = 0;
    public int privateFieldCount = 0;
    public int protectedFieldCount = 0;
    public int publicFieldCount = 0;
    public int staticFieldCount = 0;
    public int finalFieldCount = 0;
    
    
    public int finalMethodCount = 0;
    public int privateMethodCount = 0;
    public int synchronizedMethodCount = 0;
    public int protectedMethodCount = 0;
    public int abstractMethodCount = 0;
    public int constantLoadCount = 0;
    public int incrementOpCount = 0;
    public int isIOClass = 0;
    public double guiDistance = 0;
    public int bornRSN = 1;  //lets assume it was born in first version
    public int modifiedMetricCount = 0;  // number of metrics that have changed from previous version
    
    Set<String> dependencies = new HashSet<String>(); // set of classes that this class depends on including library classes
    Set<String> users = new HashSet<String>(); // types that call in
    Set<String> internalDeps = new HashSet<String>(); // non-library class set
    Set<String> fields = new HashSet<String>(); // field names of this class
    Set<String> methods = new HashSet<String>(); // method names in short format

    public int loadCount = 0;
    public int storeCount = 0;
    public int iLoadCount = 0;
    public int iStoreCount = 0;
    public int typeInsnCount = 0;
    public int zeroOpInsnCount = 0;
    
    public int computedDistance = -1; // used to store distance once computed
    public int distanceMovedSinceBirth = -1; // assume unassigned
    public int modifiedMetricCountSinceBirth = -1; // assume unassigned
    public int instability = 0;
    public int layer = 0; // Currenly only 4 layers are extracted [top/Mid/bottom and free]
	public int age = 1; // Start off young
	public int isDeleted = 0; // assume not deleted in next version
	public int isModified = 0; // records if this class will be modified in the next version
	
	public int modificationStatusSinceBirth = ECAT_NEVER_MODIFIED; // has this class ever been modified since birth?
	public int modificationFrequency = 0; // assume it has never been modified
    
	public int rawSize = 0;
	public int normalizedBranchCount = 0;
	public int loadRatio = 0;
	
    public ClassMetric(InputStream istream) throws IOException
    {
    	internalMethodCallCount = 0;
        ClassReader cr = new ClassReader(istream);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG);
        
        //System.out.println("^"+cn.name);
        extractClassMetrics(cn);              
        extractMethodMetrics(cn);
        extractDependencies(cn);
        if (methods.size() != cn.methods.size()) System.out.println("***INVESTIGATE THIS***");
        
        rawSize =  branchCount + fanOutCount + interfaceCount;
        rawSize += methodCount + fieldCount + methodCallCount;
        computeDistance();
        normalizedBranchCount = (int) (((double)branchCount/rawSize)*100.0);
        //loadRatio = (int)(((double)loadCount/(loadCount+storeCount))*100);
    }
    
    public void setEvolutionDistanceFrom(ClassMetric cm1)
    {
    	double ed = this.distanceFrom(cm1);
    	evolutionDistance = scaleDoubleMetric(ed, 100, 1000);
    	/*
    	double scaleMax = 100.0;
    	double metricMax = 1000.0;
    	if (ed > metricMax) ed = metricMax;
    	double scaledValue = (scaleMax * ed)/metricMax;    	
    	this.evolutionDistance = (int)Math.round(scaledValue);
    	if ((ed > 0) && (this.evolutionDistance == 0)) // correct rounding error
    		this.evolutionDistance = 1;
    	//if (ed < 1.0) System.out.println(ed);
    	//(int)Math.round(this.distanceFrom(cm1));
    	 */
    }
    
    public void setEvolutionDistanceSinceBirth(ClassMetric cm1)
    {
    	double ed = this.distanceFrom(cm1);
    	//if (ed > 0) ed += 0.5; 
    	//distanceMovedSinceBirth = scaleDoubleMetric(ed, 10, 100);	    	
    	distanceMovedSinceBirth = (int)Math.round(ed);
    	if ((ed > 0) && (distanceMovedSinceBirth < 1)) distanceMovedSinceBirth = 1;
    	if (ed == 0) distanceMovedSinceBirth = 0;
    	/*    	
    	double scaleMax = 100.0;
    	double metricMax = 1000.0;
    	if (ed > metricMax) ed = metricMax;
    	double scaledValue = (scaleMax * ed)/metricMax;    	
    	this.distanceMovedSinceBirth = (int)Math.round(scaledValue);
    	if ((ed > 0) && (this.distanceMovedSinceBirth == 0)) // rounding error, correct it
    		this.distanceMovedSinceBirth = 1; // the lowest possible value
    	*/
    	//;
    	
    }
    
    private int scaleDoubleMetric(double metricValue, int scaleMax, double cutOffMax)
    {
    	double mv = metricValue;
    	if (metricValue > cutOffMax) mv = cutOffMax; 
    	Double d = new Double((scaleMax*mv)/cutOffMax);
    	if ((metricValue > 0) && (d < 1.0)) d = 1.0; // force a base line
    	//System.out.println(d);
    	return d.intValue();
    }
    
    
    /** Given a field name, return the metric value stored, -1 indicates error 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws IllegalArgumentException */
    public int getMetricValue(String fieldName) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
    {
        return this.getClass().getField(fieldName).getInt(this);
    }

    private void extractMethodMetrics(ClassNode cn)
    {
        List<?> methodList = cn.methods;
        for (int i = 0; i < methodList.size(); ++i)
        {
            MethodNode method = (MethodNode) methodList.get(i);
            paramCount += Type.getArgumentTypes(method.desc).length;
            exceptionCount += method.exceptions.size();
            this.methods.add(method.name+" "+method.desc);
            localVarCount += method.maxLocals;
            
            if ((method.access & Opcodes.ACC_PRIVATE) != 0) privateMethodCount++;
            if ((method.access & Opcodes.ACC_PROTECTED) != 0) protectedMethodCount++;
            if ((method.access & Opcodes.ACC_ABSTRACT) != 0) abstractMethodCount++;
            
            if ((method.access & Opcodes.ACC_STATIC) != 0) staticMethodCount++;
            if ((method.access & Opcodes.ACC_SYNCHRONIZED) != 0) synchronizedMethodCount++;
            if ((method.access & Opcodes.ACC_FINAL) != 0) finalMethodCount++;
            
            
            if (((method.access & Opcodes.ACC_PUBLIC) != 0) && 
               ((cn.access & Opcodes.ACC_PUBLIC) != 0)) publicMethodCount++;
            
            if (method.instructions.size() > 0)
            {
                TraceMethodVisitor mv = new TraceMethodVisitor() {                    
                    public void visitVarInsn(int opcode, int var)
                    {
                        if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.DLOAD) iLoadCount ++;
                        if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.DSTORE) iStoreCount++;
                        if (opcode == Opcodes.ALOAD)  refLoadOpCount++;
                        if (opcode == Opcodes.ASTORE) refStoreOpCount++;                        
                    }

                    public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
                    {
                        tryCatchBlockCount++;
                    }
                    
                    public void visitFieldInsn(int opcode, String owner, String name, String desc)
                    {
                        if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)
                            storeFieldCount++;
                        else if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC)
                            loadFieldCount++;
                    }
                    
                    public void visitJumpInsn(int opcode, Label label)
                    {
                        if (opcode != Opcodes.GOTO) branchCount++;
                    }
                    
                    public void visitTypeInsn(int opcode, String desc)
                    {
                    	if (opcode == Opcodes.INSTANCEOF) instanceOfCount++;
                    	if (opcode == Opcodes.CHECKCAST) checkCastCount++;
                    	if (opcode == Opcodes.NEW) {newCount++; typeConstructionCount++;}
                    	if (opcode == Opcodes.ANEWARRAY) {newArrayCount++; typeConstructionCount++;}

                        typeInsnCount++;
                    }
                    
                    public void visitInsn(int opcode)
                    {
                        zeroOpInsnCount++;
                        if (opcode == Opcodes.ATHROW)
                        {
                        	branchCount++; // TODO Remove this later
                        	throwCount++;
                        }
                    }
                    
                    public void visitMethodInsn(int opcode, String owner, String n, String d)
                    {
                    	methodCallCount++;
                        if (owner.equals(className)) internalMethodCallCount++;
                        else externalMethodCallCount++;
                        dependencies.add(owner);
                    }
                                        
                    public void visitLdcInsn(Object cst)
                    {
                        constantLoadCount++;
                    }
                    
                    public void visitIincInsn(int var, int increment)
                    {
                        incrementOpCount++;
                    }

                    
                    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
                    {
                    	branchCount += labels.length;                    	
                    }
                                        
                    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
                    {
                    	branchCount += labels.length;
                    }
                    
                };
                
                for (int j = 0; j < method.instructions.size(); ++j)
                {
                    Object insn = method.instructions.get(j);
                    ((AbstractInsnNode) insn).accept(mv);
                }
            }
        }
    }   
    
    private void addDependency(Type t)
    {
        if (t == null) return; // do nothing
        if (t.getSort() == Type.OBJECT)
        {
        	String depName = t.getInternalName();
        	dependencies.add(depName);
            // Verify if this class uses a GUI component and set isGUIClass to true
        	if (depName.startsWith("java/io")) isIOClass = 1;
        	if (depName.startsWith("java/nio")) isIOClass = 1;
        	if (depName.startsWith("java/awt/event")) guiDistance = 1;
        	if (depName.startsWith("java/applet")) guiDistance = 1;
        	if (depName.startsWith("javax/swing")) guiDistance = 1;
        	if (depName.startsWith("javax/swing/event")) guiDistance = 1; 
        	if (depName.startsWith("org/eclipse/swt/events")) guiDistance = 1;
        }
    }
    
    // Extract all dependencies from fields and method decl.
    private void extractDependencies(ClassNode cn)
    {
        Iterator<?> iter = cn.fields.iterator();
        while (iter.hasNext())
        {
            FieldNode fn = (FieldNode)iter.next();
            if ((fn.access & Opcodes.ACC_PRIVATE) != 0) privateFieldCount++;
            if ((fn.access & Opcodes.ACC_PROTECTED) != 0) protectedFieldCount++;
            if ((fn.access & Opcodes.ACC_PUBLIC) != 0) privateFieldCount++;
            if ((fn.access & Opcodes.ACC_STATIC) != 0) staticFieldCount++;
            if ((fn.access & Opcodes.ACC_FINAL) != 0) finalFieldCount++;
            
 
            fields.add(fn.name);
            Type t = Type.getType(fn.desc);
            addDependency(t);
        }
        if (fields.size() != cn.fields.size()) System.out.println(cn.name + "INTERESTING, INVESTIGATE!!");
        
        iter = cn.methods.iterator();
        while (iter.hasNext())
        {
            MethodNode mn = (MethodNode)iter.next();
            for (int i=0; i < mn.exceptions.size(); i++)
            {
                dependencies.add((String)mn.exceptions.get(i));
            }            
            Type t = Type.getReturnType(mn.desc);
            addDependency(t);
                        
            Type[] argTypes = Type.getArgumentTypes(mn.desc);
            for (int i=0; i < argTypes.length; i++)
            {
                t = argTypes[i];
                addDependency(t);
            }
        }
        fanOutCount = dependencies.size();
    }

    private void extractClassMetrics(ClassNode cn)
    {
        innerClassCount = cn.innerClasses.size();
        interfaceCount = cn.interfaces.size();
        fieldCount = cn.fields.size();
        methodCount = cn.methods.size();
        className = cn.name;
        superClassName = cn.superName.trim();
        this.access = cn.access;
        if (!superClassName.equals("java/lang/Object")) superClassCount = 1;
        if (superClassName.contains("Exception")) isException = 1;
        if (superClassName.equals("java/lang/Throwable")) isException = 1;
        if ((cn.access & Opcodes.ACC_ABSTRACT) != 0) isAbstract = 1;
        if ((cn.access & Opcodes.ACC_INTERFACE) != 0) isInterface = 1;
        if ((cn.access & Opcodes.ACC_PUBLIC) != 0) isPublic = 1;
        if ((cn.access & Opcodes.ACC_PRIVATE) != 0) isPrivate = 1;
        if ((cn.access & Opcodes.ACC_PROTECTED) != 0) isProtected = 1;

    }
    
    /** Square of i */
    private double sqr(int i)
    {
        return Math.pow(i, 2.0);
    }
    
    /** Computes the number of metrics that are different between this class and cm */
    public int computeModifiedMetrics(ClassMetric cm)
    {
    	int mmc = 0; // modified metric count, assume no change
		for (String m : metrics)
		{
			try 
			{
				if (cm.getMetricValue(m) != getMetricValue(m)) mmc++;
			} catch (Exception e) {e.printStackTrace();} // lets hope it does not get to this
		}
    	return mmc;    	
    }
    
    /** Computes the distance of the class from this. This is used to check how far a class has evolved or
     * to check if it is in the same neighbourhood.
     */
    public double distanceFrom(ClassMetric cm)
    {
    	double d = 0.0;
		for (String m : distanceComputationMetrics)
		{
			try 
			{
				d += sqr(cm.getMetricValue(m) - getMetricValue(m));
			} catch (Exception e) {e.printStackTrace();} // lets hope it does not get to this
		}
    	return Math.sqrt(d);
    }
    
    /** Compute the distance this class has moved from zero in Euclid space */
    public int computeDistance()
    {
    	double d = 0.0;
		for (String m : distanceComputationMetrics)
		{
			try 
			{
				d += sqr(this.getMetricValue(m));
			} catch (Exception e) {e.printStackTrace();} // lets hope it does not get to this
		}
		computedDistance = scaleDoubleMetric(d, 10, 1000.0);   	
        return computedDistance;
    }
    
    /** Checks if it is a clone, i.e. similar to another CM */
    public boolean isExactMatch(ClassMetric c)
    {
    	if (!c.className.equals(className)) return false;
    	if (!c.superClassName.equals(superClassName)) return false;
    	
    	if (!equals(c)) return false;
		// Make sure that dependencies have not changed		
		for (String dep : this.dependencies)
		{
			if (!c.dependencies.contains(dep)) return false;
		}
		for (String fn : this.fields)
		{
			if (!c.fields.contains(fn)) return false;
		}
		for (String md : this.methods)
		{
			if (!c.methods.contains(md)) return false;
		}
		
		return true;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof ClassMetric)) return false;
        
        ClassMetric other = (ClassMetric)o;
		for (String m : metrics)
		{
			try 
			{
				if (other.getMetricValue(m) != this.getMetricValue(m)) return false;
				//if (change > 0) modifiedVectors++;
				//d += change;
			} catch (Exception e) {e.printStackTrace();} // lets hope it does not get to this
		}		
		return true;
    }
    
    public String toString()
    {
        String classType = getClassType();
        
        return String.format("%3d, %3d, %3d, %3d, %3d, %3d, %3d, %4d, %3d, %3d, %3d, %4d, %4d, %3d, %3d, %s, %s", 
            fanInCount, fanOutCount, loadCount, storeCount, branchCount, methodCount, fieldCount, superClassCount, interfaceCount,
            localVarCount, typeInsnCount, zeroOpInsnCount, 
            internalMethodCallCount, externalMethodCallCount, age,
            classType, className); //, superClassName);
    }
    
    public String getClassType()
    {
        String classType = "C";
        if (isAbstract == 1) classType = "A";
        if (isInterface == 1) classType = "I";
        if (guiDistance == 1) classType+="-GUI";
        if (isIOClass == 1) classType+="-IO";
        if (isException == 1) classType+="-EX";
        return classType;
    }

    // Used for sorting by distance
    public int compareTo(ClassMetric cm)
    {
        return Integer.valueOf(cm.computeDistance()).compareTo(computeDistance());
        //return d.compareTo(distance());
    }
    
    @Override
    public int hashCode()
    {        
        return className.hashCode();
    }
}
