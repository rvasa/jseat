package extractor;

import io.InputDataSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Represents a version of data for a particular product
 * Version
 * @author rvasa
 */
public class Version implements Comparable<Version>
{
	public static boolean showProcessing = true;
    public final int RSN; // initialised by the constructor
    public final String id;
    private InputDataSet input;
    public HashMap<String, ClassMetric> metricData;
    
    public Version(int rsn, String versionID)
    {
        RSN = rsn;
        id = versionID.trim();
        input = new InputDataSet();
        metricData = new HashMap<String, ClassMetric>();
    }
    
    /** Construct with a JAR file or a directory of JAR files */
    public Version(int rsn, String versionID, String fileName) throws IOException
    {
        this(rsn, versionID);
        addInput(fileName.trim());
    }
    
    
    /** Add input data from either a JAR file or a directory */
    public void addInput(String fName) throws IOException
    {
        if ((new File(fName)).isDirectory())
            input.addInputDir(fName, false); // no recursive support
        else 
            input.addInputFile(fName);
    }
    
    public void addInputDir(String dirName) throws IOException
    {
        input.addInputDir(dirName, false); // no recursive support yet
    }
    
    public void addJARFile(String fileName) throws IOException
    {
        input.addInputFile(fileName);
    }
    
    public Map<String, Integer> getMetricMap(String fieldName) 
    throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
    {
    	Map<String, Integer> m = new HashMap<String, Integer>();
        for (ClassMetric c : metricData.values())
        {
            m.put(c.className, c.getMetricValue(fieldName));        		
        }
    	return m;    	
    }

    public int getClassCount() {return metricData.size();}

    public double getGUIClassCount() 
    {
    	double sum = 0.0;
    	for (ClassMetric c : metricData.values()) {sum += c.guiDistance;}
    	return sum;
    }
    
    /** Sum the given integer field in the ClassMetric */
    public int getISum(String field)     
    {
        int sum = 0;
        try
        {
            for (ClassMetric c : metricData.values())
            	sum += c.getMetricValue(field);
        }
        catch (Exception e) {e.printStackTrace();} // ignore exception
        return sum;
    }
        
    /** Extract metrics from each class in the input data set */
    public long extractMetrics()
    {
    	if (showProcessing) System.out.print("."); // to show processing
        if (input.size() == 0) return 0; // nothing to extract from
        metricData.clear();
        
            for (InputStream is : input)
            {
            	try
            	{
                    ClassMetric cmd = new ClassMetric(is);
                    metricData.put(cmd.className, cmd);
                    is.close();            		
            	}
                catch (IOException iox)
                {
                    System.err.println(iox.getMessage());
                    iox.printStackTrace();
                }
            }
        long bytesProcessed = input.sizeInBytes();
        
        //input = null;  
        //System.out.println("Computing dependencies for Version# "+id);
        computeDependencies(); // parse all nodes and update this now
        computeLayers();
        computeGUIAndIOClasses();
        return bytesProcessed;
    }

    public double skew(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}
    	
    	double mean = Descriptive.mean(data);
    	double var = Descriptive.variance(data.size(), Descriptive.sum(data), Descriptive.sumOfSquares(data));
    	double sdev = Descriptive.standardDeviation(var);
    	return Descriptive.skew(data, mean, sdev);
    	//return 0.0;
    }
    
    public double min(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}    	
    	return Descriptive.min(data);
    }

    public double max(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}    	
    	return Descriptive.max(data);
    }

    public double mean(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}    	
    	return Descriptive.mean(data);
    }
    
    
    public double stdev(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}    	
    	double var = Descriptive.variance(data.size(), Descriptive.sum(data), Descriptive.sumOfSquares(data));
    	return Descriptive.standardDeviation(var);
    }
    
    

    public double kurtosis(String metricName) throws Exception
    {
    	DoubleArrayList data = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		data.add(cm.getMetricValue(metricName));
    	}
    	
    	double mean = Descriptive.mean(data);
    	double var = Descriptive.variance(data.size(), Descriptive.sum(data), Descriptive.sumOfSquares(data));
    	double sdev = Descriptive.standardDeviation(var);
    	return Descriptive.kurtosis(data, mean, sdev);
    }
    
    
    /** Correlate survivors with instability metric and others as needed */
    public double[] correlation(String baseMetric, String[] otherMetrics) throws Exception
    {
    	double[] correls = new double[otherMetrics.length];
    	for (int i=0; i < otherMetrics.length; i++)
    	{
        	DoubleArrayList m1 = new DoubleArrayList();
        	DoubleArrayList m2 = new DoubleArrayList();
	    	for (ClassMetric cm : metricData.values())
	    	{
	    		if (cm.isInterface == 1) continue; // ignore interfaces
	    		m1.add(cm.getMetricValue(baseMetric));
	    		m2.add(cm.getMetricValue(otherMetrics[i]));
	    	}
	    	correls[i] = calcCorrelation(m1, m2);   	
    	}
    	return correls;
    }
    
    /** Correlate survivors with instability metric and others as needed */
    public double[] constrainedCorrelation(String baseMetric, String[] otherMetrics, String constraintField, int constraintMin, int constraintMax) throws Exception
    {
    	double[] correls = new double[otherMetrics.length];
    	for (int i=0; i < otherMetrics.length; i++)
    	{
        	DoubleArrayList m1 = new DoubleArrayList();
        	DoubleArrayList m2 = new DoubleArrayList();
	    	for (ClassMetric cm : metricData.values())
	    	{
	    		int c = cm.getMetricValue(constraintField);
	    		if ((c >= constraintMin) && (c <= constraintMin))
	    		{
		    		m1.add(cm.getMetricValue(baseMetric));
		    		m2.add(cm.getMetricValue(otherMetrics[i]));	    			
	    		}
	    	}
	    	correls[i] = calcCorrelation(m1, m2);   	
    	}
    	
    	return correls;
    }
    
    
    private double calcCorrelation(DoubleArrayList m1, DoubleArrayList m2)
    {
    	double sdm1 = Descriptive.standardDeviation(Descriptive.variance(m1.size(), 
    			Descriptive.sum(m1), Descriptive.sumOfSquares(m1)));
    	double sdm2 = Descriptive.standardDeviation(Descriptive.variance(m2.size(), 
    			Descriptive.sum(m2), Descriptive.sumOfSquares(m2)));
    	return Descriptive.correlation(m1, sdm1, m2, sdm2);
    }
    
    /** Should be called only after metric data has been extracted
     * Fanin will be updated directly on the ClassMetric object
     * Internal Fanout value will also be calculated and updated
     */
    private void computeDependencies()
    {
        for (ClassMetric cm : metricData.values())
        {
        	cm.storeCount = cm.iStoreCount + cm.storeFieldCount + cm.refStoreOpCount;
        	cm.loadCount =  cm.iLoadCount + cm.loadFieldCount + cm.refLoadOpCount + cm.constantLoadCount;
        	cm.loadRatio = (int)(((double)cm.loadCount/(cm.loadCount+cm.storeCount))*10);
            for (String name : cm.dependencies)
            {
            	if (name.equals(cm)) continue; // ignore self-dependencies
                ClassMetric fanInNode = metricData.get(name);
                if (fanInNode != null)
                {
                	cm.internalDeps.add(name);
                	fanInNode.users.add(cm.className);
                }                
            }
        }
        
        // Now set the fanin counts       
        for (ClassMetric cm : metricData.values())
        {
            cm.fanInCount = cm.users.size();
            cm.internalFanOutCount = cm.internalDeps.size();
            cm.computeDistance(); // compute distance
        }        
    }
    
    /** Flag all classes that depend on a GUI class as GUI as well */
    private void computeGUIAndIOClasses()
    {
    	Set<String> flagged = metricData.keySet();
    	while (flagged.size() > 0)
    	  flagged = flagUsersAsGUI(flagged);
    	// Now flag all IO classes like we did GUI
    	flagged = metricData.keySet();
    	while (flagged.size() > 0)
    	  flagged = flagUsersAsIO(flagged);   	
    	
    }
    
    /** Given an input set, it will iterate over it and flag all users as GUI */
    private Set<String> flagUsersAsIO(Set<String> classNameSet)
    {
    	Set<String> flagged = new HashSet<String>();
    	for (String cn : classNameSet)
    	{
    		if (metricData.get(cn).isIOClass == 1)
    		{
    			for (String userClassName : metricData.get(cn).users)
    			{
    				//System.out.println(cn+" is used by "+userClassName);
    				if (metricData.get(userClassName).isIOClass == 0)
    				{
    					metricData.get(userClassName).isIOClass = 1;
    					flagged.add(userClassName);
    				}
    			}    			
    		}
    	}
    	//System.out.println("Returning flagged users: "+flagged.size());
    	return flagged;    	
    }
    
    
    /** Given an input set, it will iterate over it and flag all users as GUI */
    private Set<String> flagUsersAsGUI(Set<String> classNameSet)
    {
    	Set<String> flagged = new HashSet<String>();
    	for (String cn : classNameSet)
    	{
    		double currGUIDistance = metricData.get(cn).guiDistance; 
    		if (currGUIDistance != 0) // if it is a GUI class
    		{
    			// Check its immediate users and make them all as GUI classes as well
    			for (String userClassName : metricData.get(cn).users)
    			{
    				if (metricData.get(userClassName).guiDistance == 0)
    				{
    					metricData.get(userClassName).guiDistance = 1; //
    					//currGUIDistance/2; 
    					flagged.add(userClassName);
    				}
    			}    			
    		}
    	} 
    	return flagged;    	
    }
    
    /** Computes the layers and instability metrics */
    private void computeLayers()
    {
        // Now update the fanin and instability value for each class        
        for (ClassMetric cm : metricData.values())
        {
            cm.instability = (int)(((double)cm.fanOutCount / (cm.fanOutCount + cm.fanInCount))*1000);
            if ((cm.fanInCount > 0) && (cm.internalFanOutCount > 0)) cm.layer = 1; // mid
            else if ((cm.fanInCount == 0) && (cm.internalFanOutCount > 0)) cm.layer = 2; // top
            else if ((cm.fanInCount > 0) && (cm.internalFanOutCount == 0)) cm.layer = 0; // foundation
            else if ((cm.fanInCount == 0) && (cm.internalFanOutCount == 0)) cm.layer = 3; // free
            else cm.layer = 4; // there should be no classes here, hopefully
        }
    }
    
    /** Convert the array into a comma seperated value string */
    public static String toCSVString(double[] data)
    {
    	String s = "";
        for (double f : data) s += String.format("%1.3f", f)+", ";   
        return s.trim().substring(0, s.lastIndexOf(','));         
    }
    
    public static String toCSVString(int[] data)
    {
    	String s = "";
        for (int i : data) s += i+", ";   
        return s.trim().substring(0, s.lastIndexOf(','));             	
    }
    
    /** Get the frequency distribution as a string for the given field */
    public String getFreqDist(int maxValue, String field, boolean relative)
    {
        int[] freq = createFreqTable(maxValue, field);
        if (relative) // create a list of relative freq. values
        {
        	return Version.toCSVString(computeRelativeFreqTable(freq));
        }
        else 
        {
        	return Version.toCSVString(freq);
        }
    }

    
    /** Get the frequency distribution as a string for the given field */
    public String getConstrainedFreqDist(int maxValue, String field, boolean relative, String cField, int cMin, int cMax)
    {
        int[] freq = createConstrainedFreqTable(maxValue, field, cField, cMin, cMax);
        int sum = 0; for (int f : freq) { sum += f; }
        if (relative) // create a list of relative freq. values
        {
        	return sum+", \t"+Version.toCSVString(computeRelativeFreqTable(freq));
        }
        else 
        {
        	return sum+", \t"+Version.toCSVString(freq);
        }
    }
    
    /** Create relative frequency table over fields that store int values in ClassMetric */
    public double[] createRelFreqTable(int maxValue, String field)
    {
    	return computeRelativeFreqTable(createFreqTable(maxValue, field));
    }
    
    public double[] createCumlFreqTable(int maxValue, String field)
    {
    	return computeCummulFreqTable(createFreqTable(maxValue, field));
    }
    
    /** Converts a given freq. table into a cummul. dist. table */
    private double[] computeCummulFreqTable(int[] freq)
    {
    	double[] cf = computeRelativeFreqTable(freq);
    	for (int i=0; i < cf.length-1; i++)
    	{
    		cf[i+1] = cf[i+1] + cf[i];
    	}
    	return cf;
    }
    
    /** Converts a given frequency table into a relative freq table */
    private double[] computeRelativeFreqTable(int[] freq)
    {
    	double total = 0.0;
        for (int i : freq) total += i;
        double[] relFreq = new double[freq.length];
        for (int j = 0; j < freq.length; j++) relFreq[j] = freq[j]/total;
        return relFreq;    	
    }
    
    /** Create a Frequency table over fields that store integer value in ClassMetric */
    public int[] createFreqTable(int maxValue, String field)
    {
        int[] freq = new int[maxValue];
        for (ClassMetric cm : metricData.values())
        {
            try
            {
        		int m = cm.getMetricValue(field); //ClassMetric.class.getField(field).getInt(cm);
        		if (m >= freq.length) freq[freq.length - 1]++;
        		else freq[m]++;
            }
            catch (Exception e) // TODO: should not happen -- check with assert?
            {
                e.printStackTrace();
            }
        }
        return freq;        
    }
    
    /** Create a Frequency table over fields that store integer value in ClassMetric */
    public int[] createConstrainedFreqTable(int maxValue, String field, String constraintField, int constraintMin, int constraintMax)
    {
        int[] freq = new int[maxValue];
        for (ClassMetric cm : metricData.values())
        {
            try
            {
            	int c = cm.getMetricValue(constraintField);
            	if ((c >= constraintMin) && (c <= constraintMax))
            	{
            		int m = cm.getMetricValue(field); //ClassMetric.class.getField(field).getInt(cm);
            		if (m >= freq.length) freq[freq.length - 1]++;
            		else freq[m]++;
            	}
            }
            catch (Exception e) // TODO: should not happen -- check with assert?
            {
                e.printStackTrace();
            }
        }
        return freq;        
    }
    

    /** Prints the class metrics to screen -- unsorted */
    public void printMetrics()
    {
        //System.out.printf("Version-ID: %s\tRSN:%2d Classes: %5d\n", id, RSN, metricData.size());
        for (ClassMetric cm : metricData.values()) 
        	System.out.println(RSN+", "+id+", "+metricData.size()+", "+cm);        
    }
    
    /** Print the clusters in the data, i.e. those that are clones */
    public void printCloneClusters()
    {
    	System.out.println("\n\nVersion: "+RSN+"\t"+id);
    	List<ClassMetric> data = new LinkedList<ClassMetric>(metricData.values());    	
        Collections.sort(data);
        int clusters = 0;
        LinkedList<ClassMetric> list = new LinkedList<ClassMetric>();
        for (ClassMetric cm : data)
        {
            if (list.size() == 0) {list.addFirst(cm); continue;}
            if (cm.equals(list.getFirst())) list.addFirst(cm);
            else
            {
                if (list.size() == 1) {list.clear(); continue;}
                
                // iterate over the stack and dump it to screen
                if (list.get(0).isInterface == 0) clusters++; 
                for (ClassMetric c : list) System.out.println(c);
                System.out.println("--- "+list.size()+" items ---");
                list.clear();
            }
        }
        System.out.print("#clusters: "+clusters);
    }

    public int compareTo(Version o)
    {
        return o.RSN - RSN;
    }
    
    /** Two versions are the same if they have the same RSN */
    @Override
    public boolean equals(Object o)
    {
        return ((Version)o).RSN == RSN;
    }
    
    /** Square of i */
    private double sqr(double i)
    {
        return i*i;
    }
    
    public int getPred(String field, Version prev)
    {
    	return (int)Math.pow(this.getClassCount(), prev.getBeta(field));    	
    }
    
    public double getPredErr(String field, Version prev)
    {
    	return relativeChange(getISum(field), getPred(field, prev));    	
    }
        
    public double getRelativeSizeChange(Version prev) {return relativeChange(prev.getClassCount(),getClassCount());}
    
    /** Compute the relative change between the numbers m1 and m2 */
    private double relativeChange(int m1, int m2)
    {
    	return ((double)(m2-m1))/m1;
    }
    
    // Power and Linear scaling exponents
    public double getBeta(String field) {return Math.log(getISum(field))/Math.log(getClassCount());}
    public double getAlpha(String field) {return (double)getISum(field)/getClassCount();}
        
    public double getRawCountDistanceFrom(Version v)
    {
        double d = 0.0;
        d += sqr(v.getISum("methodCount") - this.getISum("methodCount"));
        d += sqr(v.getISum("fanOutCount") - this.getISum("fanOutCount"));
        d += sqr(v.getISum("methodCallCount") - this.getISum("methodCallCount"));
        d += sqr(v.getISum("loadCount") - this.getISum("loadCount"));
        d += sqr(v.getISum("storeCount") - this.getISum("storeCount"));        
        d += sqr(v.getISum("branchCount") - this.getISum("branchCount"));
        d = Math.sqrt(d); // v.getClassCount();
        return d;
    }
    
    /** Cacluate the euclidian distance between two n-dimensial vectors */
    public static double calcDistance(double[] start, double[] end)
    {
    	if (start.length != end.length) return -1; // error in the input data set
    	double distance = 0.0;
    	for (int i=0; i < start.length; i++) distance += Math.pow((start[i] - end[i]), 2.0);
    	return Math.sqrt(distance);    		
    }
    
    public double[] getGiniValues(String[] fields) throws Exception
    {
        double[] giniValues = new double[fields.length];
        for (int j=0; j < fields.length; j++) giniValues[j] = calcGiniCoefficient(fields[j]);
        return giniValues;
    }
    
//    public double calcGiniCoefficient(int maxValue, String field) throws Exception
    public double calcGiniCoefficient(String field) throws Exception
    {
    	DoubleArrayList d = new DoubleArrayList();
    	for (ClassMetric cm : metricData.values())
    	{
    		if ((field.equals("loadCount"))&&(cm.isInterface == 1)) continue;
    		if ((field.equals("storeCount"))&&(cm.isInterface == 1)) continue;
    		if ((field.equals("branchCount"))&&(cm.isInterface == 1)) continue;
    		if ((field.equals("fieldCount"))&&(cm.isInterface == 1)) continue;
    		if ((field.equals("methodCallCount"))&&(cm.isInterface == 1)) continue;
    		if ((field.equals("typeConstructionCount"))&&(cm.isInterface == 1)) continue;    		    		
    		d.add(cm.getMetricValue(field));
    	}
    	//int[] data = createFreqTable(maxValue, field);
    	//for (int i : data) d.add(i);

    	double relVars = 0;
    	double descMean = Descriptive.mean(d);
    	
    	for (int i = 0; i < d.size(); i++) 
    	{
    	  for (int j = 0; j < d.size(); j++) 
    	  {
    		  if (i == j) continue; 
    		  relVars += (Math.abs(d.get(i) - d.get(j))); // / descMean;
    	  }
    	}
    	relVars = relVars / (2 * d.size() * d.size());
    	double gini = relVars / descMean;
    	
    	//System.out.println("Gini: " + gini);
    	return gini;
    }
    
    /** If it has an age > 1 then it is a survivor */
    public Set<String> getSurvivorClassNames()
    {
    	Set<String> survivors = new HashSet<String>(); 
    	for (ClassMetric cm : metricData.values())
    	{
    		if (cm.age > 0) survivors.add(cm.className);
    	}
    	//System.out.println("Survivors in Version ID: "+id+" -- "+RSN+":: "+survivors.size());
    	return survivors;
    }
    
    
    
}
