package extractor;

import io.TextFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * History - The set of versions that make up a systems evolution history
 * @author rvasa
 */
public class History
{
    private Map<Integer, Version> versions = new HashMap<Integer, Version>();
    public String name;
    public String shortName; // stores the first word of the name
    
    public History(String productName)
    {
        setName(productName);
        versions = new HashMap<Integer, Version>();
    }
    
    public History(File f) throws IOException
    {
        versions = new HashMap<Integer, Version>();
        loadVersionDataFromFile(f);
    }
    
    /** Load data from an external text file that describes the version data */
    private void loadVersionDataFromFile(File fn) throws IOException
    {
        TextFile f = new TextFile(fn);
        for (String line : f)
        {
            if (line.trim().length() == 0) continue; // skip empty lines
            if (line.trim().startsWith("#")) continue; // skip comments
            if (line.trim().startsWith("$")) // set product name and move to next line
            {
            	setName(line.replace('$', ' ').trim()); // remove the $ 
            	continue;
            }

            String[] cols = line.split(",");
            if (cols.length != 3) continue; // bad data -- skip line
            String jarFileName = new File(fn.getParent(), cols[2].trim()).toString();
            addVersion(new Version(Integer.parseInt(cols[0].trim()), cols[1], jarFileName));
        }
        f.close();
    }
    
    private void setName(String productName)
    {
        name = productName;
        // store the first word as the shortName
        int spacePos = name.indexOf(" ");
        if (spacePos > 0) shortName = name.substring(0, name.indexOf(" "));
        else shortName = name;        
    }
    
    public long extractMetrics()
    {
        long bytesProcessed = 0;
        for (Version v : versions.values())
        {
            bytesProcessed += v.extractMetrics();
        }
        scanAndMarkSurvivors();
        return bytesProcessed;
    }
    
    public Collection<Version> getVersionList()
    {
        return versions.values();
    }
    
    public void addVersion(int rsn, String id, String fileName) throws IOException
    {
        Version curr = versions.get(rsn); 
        if (curr == null)
        {
            addVersion(new Version(rsn, id, fileName));
        }
        else // RSN already exists, add new details only
        {
            curr.addInput(fileName);
        }        
    }
    
    public void addVersion(Version v)
    {
        versions.put(v.RSN, v); // new version
    }
    
    public Version getVersion(int rsn)
    {
        return versions.get(rsn);
    }
    
    /** Calculates the number of classes that did not change between versions */
    public void printSurvivors()
    {
    	if (versions.size() < 2) return;
        System.out.println("\n      Name\tRSNs\t\tIDs\t\tV1-CC\tV2-CC\tSurvivors");        
        for (int i=2; i <= versions.size(); i++)
        {
            Version v1 = versions.get(i-1);
            Version v2 = versions.get(i);
            int count = v2.getSurvivorClassNames().size();
            double survivors = (double)count/v2.getClassCount();
            System.out.printf("%10s\t%2d-%2d\t%8s - %8s\t%d\t%d\t%d\t%2.3f\t\n", 
                    shortName, v1.RSN, v2.RSN, v1.id, v2.id, 
                    v1.getClassCount(), v2.getClassCount(),
                    count, survivors);
        }
    }
    
    public void printCorrelations(String baseMetric, String[] otherMetrics)
    {
    	if (versions.size() < 2) return;
    	String header = "Name\t\tRSN\t       ID\t";
    	for (String om : otherMetrics) header += om+"\t";
    	System.out.println(header);
    	for (int i=2; i < versions.size(); i++)
    	{
    		Version v = versions.get(i);
    		try 
    		{
    			double[] correls = v.correlation(baseMetric, otherMetrics);
                String data = String.format("%10s\t%2d\t%9s  \t", 
                        shortName, v.RSN, v.id);    			
    			for (double c : correls) data += String.format("%2.4f  ", c)+"\t";
    			System.out.println(data);
			} catch (Exception e) {e.printStackTrace();}
    	}
    	//versions.get(versions.size()-1).printMetrics();    	
    }
    
    public void printConstrainedCorrelations(String baseMetric, String[] otherMetrics, String constraintField, int constraintMin, int constraintMax)
    {
    	if (versions.size() < 2) return;
    	String header = "Name\t\tRSN\t       ID\t";
    	for (String om : otherMetrics) header += om+"\t";
    	System.out.println(header);
    	for (int i=2; i < versions.size(); i++)
    	{
    		Version v = versions.get(i);
    		try 
    		{
    			double[] correls = v.constrainedCorrelation(baseMetric, otherMetrics, constraintField, constraintMin, constraintMax);
                String data = String.format("%10s\t%2d\t%9s  \t", 
                        shortName, v.RSN, v.id);    			
    			for (double c : correls) data += String.format("%2.4f  ", c)+"\t";
    			System.out.println(data);
			} catch (Exception e) {e.printStackTrace();}
    	}
    	//versions.get(versions.size()-1).printMetrics();    	
    }
    
    
    /** Will go through the entire history and computes this. Assumes scanAndMarkSurvivors has been called */
    private void updateDistanceMovedSinceBirth()
    {
    	for (int i=1; i <= versions.size(); i++)
    	{
    		Version v = versions.get(i);
    		for (ClassMetric cm : v.metricData.values())
    		{
    			if (cm.bornRSN == v.RSN) // new born
    			{
    				cm.distanceMovedSinceBirth = 0;
    				cm.modificationStatusSinceBirth = ClassMetric.ECAT_NEW_BORN;
    				continue; // move to the next class
    			}
    			
    			if (cm.bornRSN > v.RSN) //oh oh .. not good sign something went horribly wrong
    			{
    				System.out.println("FATAL ERROR! INVESTIGATE THIS NOW!!");
    				continue; // keep moving
    			}
    			
    			// born before current version, get the ancestor
    			ClassMetric ancestor = versions.get(cm.bornRSN).metricData.get(cm.className);
    			if (ancestor.isExactMatch(cm))
    			{
    				cm.modificationStatusSinceBirth = ClassMetric.ECAT_NEVER_MODIFIED;
    			}
    			else
    			{
	    			cm.setEvolutionDistanceSinceBirth(ancestor);
	    			cm.modificationStatusSinceBirth = ClassMetric.ECAT_MODIFIED_AFTER_BIRTH;
	    			cm.modifiedMetricCountSinceBirth = cm.computeModifiedMetrics(ancestor);
    			}
    		}
    		
    	}
    	
    }

    /** Updates the age for each Class in every Version.
     * Increments age if the class is an exact match from before
     */
    private void scanAndMarkSurvivors()
    {
        if (versions.size() < 2) return; // can do nothing with 1 version
        for (int i=2; i <= versions.size(); i++) // since we have 1 for base
        {
            Version v1 = versions.get(i-1);
            Version v2 = versions.get(i);
        	for (ClassMetric cm2 : v2.metricData.values())
        	{
        		cm2.evolutionDistance = 0; // assume NEW or unchanged
        		ClassMetric cm1 = v1.metricData.get(cm2.className);
        		if (cm1 == null) // cm2 class name not found in previous version
        		{
        			cm2.evolutionStatus = ClassMetric.ECAT_NEW;
        			cm2.modificationFrequency = 0; // new born
        			cm2.bornRSN = v2.RSN;
        			cm2.age = 0;
        		}
        		else // found in previous version
        		{
        			cm2.bornRSN = cm1.bornRSN;
        			if (cm2.isExactMatch(cm1))
        		    {
        				cm2.age = cm1.age + 1; // This class is a survivor
        				cm2.evolutionStatus = ClassMetric.ECAT_UNCHANGED;
        				cm1.nextVersionStatus = ClassMetric.ECAT_UNCHANGED;
        				cm2.modificationFrequency = cm1.modificationFrequency;
        				cm2.evolutionDistance = 0;
        				cm2.modifiedMetricCount = 0;
        		    }
        			else // found, but it is not an exact match
        			{
        				cm2.evolutionStatus = ClassMetric.ECAT_MODIFIED;
        				cm2.modificationFrequency = cm1.modificationFrequency + 1;
        				cm1.isModified = 1;
        				cm2.age = 1;
        				cm2.setEvolutionDistanceFrom(cm1);
        				cm2.modifiedMetricCount = cm2.computeModifiedMetrics(cm1);
        				//cm2.evolutionDistance = (int)Math.round(cm2.distanceFrom(cm1));
        				//System.out.println(cm2.evolutionDistance);
        				cm1.nextVersionStatus = ClassMetric.ECAT_MODIFIED;
        			}
        		}
        	}
        	
        	// Now look for deleted names and mark them down
        	for (ClassMetric cm1 : v1.metricData.values())
        	{
        		ClassMetric cm2 = v2.metricData.get(cm1.className);
        		if (cm2 == null) 
        		{
        			cm1.isDeleted = 1;
        			cm1.nextVersionStatus = ClassMetric.ECAT_DELETED;
        		}
        	}
        }
        updateDistanceMovedSinceBirth();  // TODO: move this at end of scanAndMarkSurvivors

    }
    
    
    /** Print out the clone clusters for the entire history */
    public void printCloneClusters()
    {
    	for (int i=1; i < versions.size(); i++) 	
    		versions.get(i).printCloneClusters();
    }

    /** Prints the class metrics for each version processed */
    public void printHistory()
    {
    	for (int i=1; i < versions.size(); i++) 	
    		versions.get(i).printMetrics();
    }
    
    public void printMetricHistory(String fieldName) //, String constraintField, int constraintMin, int constraintMax)
    {
		try 
		{
			Map<String, Integer[]> hm = createHistoryMap(fieldName); //, constraintField, constraintMin, constraintMax);
			printHistoryMap(hm);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
    }
    
    private void printHistoryMap(Map<String, Integer[]> hm)
    {
    	if (hm == null) return;
    	for (String className : hm.keySet())
    	{
    		String line = className;
    		Integer[] values = hm.get(className); 
    		for (Integer i : values)
    		{
    			if (i != null) line += ","+i;
    			else line += ",";
    		}    			
    		System.out.println(line.substring(0,line.length()));
    	}
    }
    
    /** This method will create a map of history for the metric stored in fieldName
     * It will compute the metric value for a class name for all versions.
     * If the class does not exist in a version then the value will be null for the metric
     * @return A name that contains class name and the metric values for each version
     */
    public Map<String, Integer[]> createHistoryMap(String fieldName) //, String constraintField, int constraintMin, int constraintMax) 
    throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
    {
    	//TODO: This is not smart enough, as it will not work well when we have
    	// classes that are removed for a version, then come back again.
    	Map<String, Integer[]> historyMap = new HashMap<String, Integer[]>();
    	for (int i=1; i <= versions.size(); i++)
    	{
    		Version v = versions.get(i);
    		Map<String, Integer> versionMetricMap = v.getMetricMap(fieldName); //, constraintField, constraintMin, constraintMax);
    		for (String className : versionMetricMap.keySet())
    		{
    			// first time, create the array object
    			if (!historyMap.containsKey(className))
    			{
    				historyMap.put(className, new Integer[versions.size()]);
    			}
    			
    			// Store the metric value against the version in array
    			historyMap.get(className)[i-1] = versionMetricMap.get(className);    			
    		}
    	}
    	return historyMap;
    }
    
    public void printDistances(String field, int max, boolean showHeader)
    {
        if (versions.size() < 2)
        {
            System.out.println("Insufficient versions to compute distances");
            return;
        }
        String header = "\n      Name\tRSNs\t\tIDs\t\tRawDist\t\tBetaDelta\tDeltaSize\thDiff\thInter";        
        if (showHeader) System.out.println(header);
        for (int i=2; i <= versions.size(); i++)
        {
            Version v1 = versions.get(i-1);
            Version v2 = versions.get(i);
            //int deltaSize = v2.getClassCount() - v1.getClassCount();
            double relDelta = ((double)v2.getClassCount() - v1.getClassCount())/v1.getClassCount();
            double rawDistance = v2.getRawCountDistanceFrom(v1);
            double deltaBeta = v2.getBeta(field) - v1.getBeta(field);
            double histDiff = histogramDiffDist(v1, v2, field, max);
            double histInter = histogramIntersectionDist(v1, v2, field, max);
            System.out.printf("%10s\t%2d-%2d\t%8s - %8s\t%8.2f\t%- 7.5f\t%- 6.4f\t\t%6.4f\t\t%6.4f\n", 
                    shortName, v1.RSN, v2.RSN, v1.id, v2.id, 
                    rawDistance, deltaBeta, relDelta, //deltaSize,
                    histDiff, histInter);
        }
    }

    
    public void printPredictions(boolean showHeader)
    {
        if (versions.size() < 2)
        {
            System.out.println("Insufficient versions to compute predictions");
            return;
        }
        String header = "\nName,RSN,ID,PrevClasses,Classes,%Growth,"; 
        header += "PubMethods,PubMethodsPred,%Err,";        
        header += "FanOut,FanOutPred,%Err,FanIn,FanInPred,%Err,";        
        header += "Methods,MethodsPred,%Err,Branches,BranchesPred,%Err,";        
        header += "Stores,StoresPred,%Err,Loads,LoadsPred,%Err,";        
        header += "Inherited,InheritedPred,%Err";        
        if (showHeader) System.out.println(header);
        for (int i=2; i <= versions.size(); i++)
        {
            Version v1 = versions.get(i-1);
            Version v2 = versions.get(i);
            System.out.printf("%s,%s,%s,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f,%d,%d,%4.3f\n", 
                    shortName, v2.RSN, v2.id, v1.getClassCount(), v2.getClassCount(),
                    v2.getRelativeSizeChange(v1),
                    v2.getISum("publicMethodCount"), v2.getPred("publicMethodCount", v1), v2.getPredErr("publicMethodCount", v1),              
                    v2.getISum("fanOutCount"), v2.getPred("fanOutCount", v1), v2.getPredErr("fanOutCount", v1),              
                    v2.getISum("fanInCount"), v2.getPred("fanInCount", v1), v2.getPredErr("fanInCount", v1),              
                    v2.getISum("methodCallCount"), v2.getPred("methodCount", v1), v2.getPredErr("methodCount", v1),              
                    v2.getISum("branchCount"), v2.getPred("branchCount", v1), v2.getPredErr("branchCount", v1),              
                    v2.getISum("storeCount"), v2.getPred("storeCount", v1), v2.getPredErr("storeCount", v1),              
                    v2.getISum("loadCount"), v2.getPred("loadCount", v1), v2.getPredErr("loadCount", v1),              
                    v2.getISum("superClassCount"), v2.getPred("superClassCount", v1), v2.getPredErr("superClassCount", v1));
        }
    }
    
    
    /** 
     * Computes difference using the relative histogram distribution.
     * Will build the histogram for bin range 0 - 100.
     * Field has to store an int value in ClassMetric
     */
    public double histogramDiffDist(Version v1, Version v2, String field, int maxValue)
    {
    	
    	double[] v1Hist = v1.createRelFreqTable(maxValue, field);
    	double[] v2Hist = v2.createRelFreqTable(maxValue, field);
    	// assert v1RelHist.length and v2RelHist.length are the same
    	double sum = 0.0;
    	for (int i=0; i < v1Hist.length; i++)
    	{
    		//bhattacharya distance measure B-distance
    		sum += Math.sqrt(v1Hist[i]) * Math.sqrt(v2Hist[i]);
    		
    		//bhattacharya cosine angle measure
    		//sum += Math.sqrt(v1Hist[i]*v2Hist[i]);
    		
    		// matusita measure
    		//(Math.sqrt(v2Hist[i]) - Math.sqrt(v1Hist[i]))^2
    	}
    	return sum;///(2*v1Hist.length);
    	//return (-1.0*Math.log(sum));
    }

    /** 
     * Computes intersection using absolute histogram distribution.
     * Will build the histogram for bin range 0 - 100.
     * Field has to store an int value in ClassMetric
     */
    public double histogramIntersectionDist(Version v1, Version v2, String field, int maxValue)
    {
    	int[] v1Hist = v1.createFreqTable(maxValue, field);
    	int[] v2Hist = v2.createFreqTable(maxValue, field);
    	// assert v1Hist.length and v2Hist.length are the same
    	double sum = 0.0;
    	for (int i=0; i < v1Hist.length; i++)
    	{
    		if (v1Hist[i] == v2Hist[i]) continue; // ignore similarities
    		double min = Math.min(v1Hist[i], v2Hist[i]);
    		double max = Math.max(v1Hist[i], v2Hist[i]);   		
    		if (max != 0.0) sum += (1 - min/max); // avoid div-by-zero
    	}
    	return sum;
    }
  
    public void printLayerFreq(boolean relative)
    {
    	printFreq(5, "layer", relative);
    }
    
    public void printFreq(int max, String field, boolean relative)
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s,\t%2d,  %7s,\t%5d,\t%s\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), 
                    v.getFreqDist(max, field, relative));
        }            	
    }
    
    public void printConstrainedFreq(int max, String field, boolean relative, String cField, int cMin, int cMax)
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            //Version v = versions.get(versions.size()); // print only final version
            System.out.printf("%10s,\t%2d,  %7s,\t%5d,\t%s\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), 
                    v.getConstrainedFreqDist(max, field, relative, cField, cMin, cMax));
        }            	
    }
    

    public void printCummulFreq(int max, String field)
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s,\t%2d,  %7s,\t%5d,\t%s\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), 
                    Version.toCSVString(v.createCumlFreqTable(max, field)));
        }            	
    }
    
    
    /** Prints the RSN, Version-ID, ClassCount and various exponents */
    public void printBeta(boolean showHeader)
    {
        if (showHeader)
            System.out.println("      Name\tRSN\tID\tClasses\tB-Out\tB-In\tloExp\tstExp\tbrExp\tmExp\tinhExp");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s\t%2d  %s\t%6d\t%2.3f\t%2.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), v.getBeta("fanOutCount"), v.getBeta("fanInCount"),
                    v.getBeta("loadCount"), v.getBeta("storeCount"), v.getBeta("branchCount"),
                    v.getBeta("methodCallCount"), v.getBeta("superClassCount"));
        }
    }

    /** Prints the RSN, Version-ID, ClassCount and various exponents */
    public void printStats(String metricName, boolean showHeader) throws Exception
    {
        if (showHeader)
            System.out.println("      Name\tRSN\tID\tClasses\tMean\tStDev\tMin\tMax\tSkew\tKurtosis");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s\t%2d  %s\t%6d\t%4.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), v.mean(metricName), v.stdev(metricName), v.min(metricName),
                    v.max(metricName), v.skew(metricName), v.kurtosis(metricName));
        }
    }
    
    
    public void printMetrics()
    {
        for (int i=1; i <= versions.size(); i++) versions.get(i).printMetrics();
    }
     
    public void printMetrics(int RSN)
    {
    	Version v = versions.get(RSN);
    	if (v != null) v.printMetrics();
    }

    /** Prints the RSN, Version-ID, ClassCount and various exponents */
    public void printAlpha(boolean showHeader)
    {
        if (showHeader)
            System.out.println("\n      Name\tRSN\tID\tClasses\tA-Out\tA-In\tA-load\tA-str\tA-brn\tA-mth\tA-inh");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s\t%2d  %s\t%6d\t%2.3f\t%2.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\t%4.3f\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), 
                    v.getAlpha("fanOutCount"), v.getAlpha("fanInCount"),
                    v.getAlpha("loadCount"), v.getAlpha("storeCount"), 
                    v.getAlpha("branchCount"),
                    v.getAlpha("methodCount"), v.getAlpha("superClassCount"));
        }
    }
    
    /** Prints the rsn, version-id, class & fanout counts in each version */
    public void printGiniCoeff(String[] fields) throws Exception
    {
    	if (fields.length == 0) return;
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            double[] giniValues = v.getGiniValues(fields);
            double distance = 0.0;
        	if (i > 1) distance = Version.calcDistance(giniValues, versions.get(i-1).getGiniValues(fields));
            System.out.printf("%10s\t%2d\t%10s\t%5d\t", shortName, v.RSN, v.id, v.getClassCount());
            for (int j=0; j < giniValues.length; j++)
            {
            	System.out.printf("%5.4f\t", giniValues[j]);	
            }
            System.out.printf("%5.4f\n", distance);            	
        }
    }
    
    /** Prints the rsn, version-id, class & fanout counts in each version */
    public void printRawCounts(boolean showHeader)
    {
        if (showHeader)
            System.out.println("\n      Name\tRSN\tID\tClasses\tMethods\tPubMtds\tFanOut\tFanIn\tLoads\tStores\tBrnchs\tmCall\tInherit\t   GUI\t  IO\tExc\tDeleted\tModified");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%10s\t%2d  %s\t%5d\t%6d\t%6d\t%6d\t%5d\t%5d\t%5d\t%5d\t%5d\t%5d\t%3.2f\t%5d\t%5d\t%5d\t%5d\n", 
                    shortName, v.RSN, v.id, v.getClassCount(), 
                    v.getISum("methodCount"), v.getISum("publicMethodCount"),
                    v.getISum("fanOutCount"), v.getISum("fanInCount"),
                    v.getISum("loadCount"), v.getISum("storeCount"), 
                    v.getISum("branchCount"), //v.getExtMethodCallCount(), 
                    v.getISum("methodCallCount"), v.getISum("superClassCount"),
                    v.getGUIClassCount(), v.getISum("isIOClass"),
                    v.getISum("isException"), v.getISum("isDeleted"), v.getISum("isModified"));
        }
    }
    
    /** Prints out the class metric along with the amount it has been modified, if modified */
    public void printModifiedDistance()
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            for (ClassMetric cm : v.metricData.values())
            {
            	if (cm.evolutionStatus == ClassMetric.ECAT_MODIFIED)
            	{
            		System.out.println(v.RSN+"\t"+cm.className + "\t" + cm.evolutionDistance +
            							"\t" + cm.fanInCount + "\t" + cm.fanOutCount);
            	}
            }            
        }	
    }
    
    public void printModifiedClassList()
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            for (ClassMetric cm : v.metricData.values())
            {
            	if (cm.nextVersionStatus == ClassMetric.ECAT_MODIFIED)
            	{
            		System.out.println(v.RSN+"\t"+cm.className + "\t" + cm.branchCount + "\t" + cm.layer +
            							"\t" + cm.fanInCount + "\t" + cm.fanOutCount);
            	}
            }            
        }	   	
    }
    
    public void printUnchangedClassList()
    {
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            for (ClassMetric cm : v.metricData.values())
            {
            	if (cm.evolutionStatus == ClassMetric.ECAT_UNCHANGED)
            	{
            		System.out.println(v.RSN+"\t"+cm.className + "\t" +
            						    "\t" + cm.loadCount + 
            							"\t" + cm.fanInCount + "\t" + cm.fanOutCount);
            	}
            }            
        }	    	
    }
    
    /** Prints the rsn, version-id, class & fanout counts in each version */
    public void printLayerCounts(boolean showHeader)
    {
        if (showHeader)
            System.out.println("\nRSN & ID & Classes & i & F & M & T & U & m & m-p & beta-M & beta-P");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%2d & %s & %5d  &  %4d  & %s & %6d  &  %6d & %1.3f & %1.3f\\\\ \n", 
                    v.RSN, v.id, v.getClassCount(), v.getISum("superClassCount"),
                    v.getFreqDist(4, "layer", true).replace(",", " &"),
                    v.getISum("methodCount"), v.getISum("publicMethodCount"),
                    v.getBeta("methodCount"), v.getBeta("publicMethodCount"));
        }
    }

    
    /** Prints the rsn, version-id, class & fanout counts in each version */
    public void printClassCounts(boolean showHeader)
    {
        if (showHeader)
            System.out.println("RSN\tID\tClasses\tFanout\tBeta\tLoads\tStores\tloExp\tstExp\tBrnchs\tbrExp\tMCall\tMCallExp");
        for (int i=1; i <= versions.size(); i++)
        {
            Version v = versions.get(i);
            System.out.printf("%2d  %s\t%6d\t%6d\t%2.3f\t%d\t%d\t%4.3f\t%4.3f\t%5d\t%4.3f\t%5d\t%4.3f\n", 
                    v.RSN, v.id, v.getClassCount(), 
                    v.getISum("fanOutCount"), v.getBeta("fanOutCount"),
                    v.getISum("loadCount"), v.getISum("storeCount"), 
                    v.getBeta("loadCount"), v.getBeta("storeCount"),
                    v.getISum("branchCount"), v.getBeta("branchCount"),
                    v.getISum("methodCallCount"), v.getBeta("methodCallCount"));
        }
    }

}
