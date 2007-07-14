package metric.core.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.Map.Entry;

import metric.core.MetricModelBuilder;
import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.MethodMetricMap;
import metric.core.model.MetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.StringUtils;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.MethodMetric;
import metric.core.vocabulary.Version;

public class MetricDataToCSV extends Observable
{
	private char delim = ',';
	private HashMap<Integer, VersionMetricData> versions;

	private HashMap<ClassMetric, Integer> metricExclusionList;
	private HashMap<ClassMetric, String> propertyExclusionList;

	private int completion, work;

//	private BlockingQueue<Runnable> workToProcess;
//	private BlockingQueue<VersionMetricData> processedVersions;
//	private ThreadPoolExecutor threadPool;

	public MetricDataToCSV()
	{
		// exclusion list
		// We don't store these values becuase they are re-computed
		// after deserialization.
		metricExclusionList = new HashMap<ClassMetric, Integer>();
		metricExclusionList.put(ClassMetric.BORN_RSN, 1);
		metricExclusionList.put(ClassMetric.IS_PUBLIC, 1);
		metricExclusionList.put(ClassMetric.AGE, 1);
		// -1 means any value.
		metricExclusionList.put(ClassMetric.INSTABILITY, -1);
		metricExclusionList.put(ClassMetric.LAYER, -1);
		metricExclusionList.put(ClassMetric.COMPUTED_DISTANCE, -1);
		metricExclusionList.put(ClassMetric.FAN_IN_COUNT, -1);
		metricExclusionList.put(ClassMetric.INTERNAL_FAN_OUT_COUNT, -1);
		metricExclusionList.put(ClassMetric.RAW_SIZE_COUNT, -1);
		metricExclusionList.put(ClassMetric.NORMALIZED_BRANCH_COUNT, -1);
		metricExclusionList.put(ClassMetric.LOAD_COUNT, -1);
		metricExclusionList.put(ClassMetric.STORE_COUNT, -1);

		propertyExclusionList = new HashMap<ClassMetric, String>();
		propertyExclusionList.put(
				ClassMetric.SUPER_CLASS_NAME,
				"java/lang/Object");

//		workToProcess = new LinkedBlockingQueue<Runnable>();
//		threadPool = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
//				workToProcess);
//
//		processedVersions = new LinkedBlockingQueue<VersionMetricData>();
	}

	public void toCSV(MetricData md, BufferedWriter bw) throws IOException
	{
		if (md instanceof HistoryMetricData)
		{
			HistoryMetricData hmd = (HistoryMetricData) md;
			writeHistoryToCSV(hmd, bw);
		}
	}

	// public MetricData fromCSV(BufferedReader br) throws IOException
	// {
	// versions = new HashMap<Integer, VersionMetricData>();
	//
	// work = getNumLines(br);
	//
	// queueWork(br);
	//
	// // Start processing.
	// int workQueueSize = workToProcess.size();
	// for (int i = 0; i < workQueueSize; i++)
	// {
	// try
	// {
	// threadPool.execute(workToProcess.take());
	// } catch (InterruptedException e)
	// {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * Initiates an orderly shutdown in which previously submitted tasks are
	// * executed, but no new tasks will be accepted. Invocation has no
	// * additional effect if already shut down.
	// */
	// threadPool.shutdown();
	// while (!threadPool.isTerminated())
	// {
	// try
	// {
	// Thread.sleep(200);
	//				
	// System.out.println(workToProcess.size());
	// } catch (InterruptedException e)
	// {
	// // We have been interrupted. Probably to stop processing.
	// // // In most cases, this is expected if the user intentionally
	// // // interrupts.
	// // // Perform an immediate shutdown.
	// // logger.log(Level.WARNING,
	// // "Was interrupted during version processing.");
	// threadPool.shutdownNow();
	// }
	// }
	// System.out.println("Done.");
	// for (VersionMetricData vmd : processedVersions)
	// {
	// versions.put(vmd.getSimpleMetric(Version.RSN), vmd);
	// }
	// System.out.println("Updating metrics");
	// MetricModelBuilder.updateMetrics(versions);
	// System.out.println("Done.");
	// // return null;
	// return new HistoryMetricData("test", versions);
	// }

//	private void queueWork(BufferedReader br) throws IOException
//	{
//		while (br.ready())
//		{
//			final String s = br.readLine();
//
//			Runnable toProcess = new Runnable()
//			{
//				public void run()
//				{
//					if (s.trim().length() == 0)
//						return; // skip blank lines.
//
//					String[] tokens = s.split(",\\{|\\},|\\]\\{|\\}\\{");
//
//					// parse version information.
//					VersionMetricData currentVersion = readVersion(tokens[0]);
//					// parse class property information.
//					ClassMetricData currentClass = readClassProperties(
//							tokens[1],
//							currentVersion);
//					currentVersion.setProperty(Version.NAME, currentClass
//							.get(ClassMetric.PRODUCT_NAME));
//					// parse class metric information
//					readClassMetrics(tokens[2], currentClass);
//					// parse class dependencies, innerDeps and users.
//					readClassDepsEtc(tokens[3], currentClass);
//
//					// parse methods (should start at tokens 4 to tokens.length
//					String[] methods = new String[tokens.length - 4];
//					for (int i = 4; i < tokens.length; i++)
//						methods[i - 4] = tokens[i];
//					readMethods(methods, currentClass);
//
//					processedVersions.offer(currentVersion);
//					System.out.println(currentVersion.get(Version.RSN));
//				}
//			};
//			// Add runnable work to queue.
//			workToProcess.offer(toProcess);
//		}
//		// Normally we would leave this up to the user to close
//		// but closing it now will help free memory.
//		br.close();
//	}

	public MetricData fromCSV(BufferedReader br) throws IOException
	{
		versions = new HashMap<Integer, VersionMetricData>();
		ClassMetricData currentClass = null;

		work = getNumLines(br);

		while (br.ready())
		{
			String s = br.readLine();
			if (s.trim().length() == 0)
				continue; // skip blank lines.

			String[] tokens = s.split(",\\{|\\},|\\]\\{|\\}\\{");

			// parse version information.
			VersionMetricData currentVersion = readVersion(tokens[0]);
			// parse class property information.
			currentClass = readClassProperties(tokens[1], currentVersion);
			currentVersion.setProperty(Version.NAME, currentClass
					.get(ClassMetric.PRODUCT_NAME));
			// parse class metric information
			readClassMetrics(tokens[2], currentClass);
			// parse class dependencies, innerDeps and users.
			readClassDepsEtc(tokens[3], currentClass);

			// parse methods (should start at tokens 4 to tokens.length
			String[] methods = new String[tokens.length - 4];
			for (int i = 4; i < tokens.length; i++)
				methods[i - 4] = tokens[i];
			readMethods(methods, currentClass);

		}
		MetricModelBuilder.updateMetrics(versions);
		return new HistoryMetricData(
				currentClass.get(ClassMetric.PRODUCT_NAME), versions);
	}

	private int getNumLines(BufferedReader br) throws IOException
	{
		if (br.ready())
		{
			String tmp = br.readLine();
			return Integer.parseInt(tmp.substring(tmp.indexOf("$") + 1, tmp
					.length()));
		}
		return -1; // error
	}

	private VersionMetricData readVersion(String rsnAndVid)
	{
		String[] toks = rsnAndVid.split(",");

		int rsn = Integer.parseInt(toks[0]);
		String id = toks[1];
		VersionMetricData vmd = null;
		if (!versions.containsKey(rsn))
		{
			vmd = new VersionMetricData(rsn, id);
			versions.put(rsn, vmd);
			completion = (int) (((double) versions.size() / (double) work) * 100);
			setChanged();
			notifyObservers();
		} else
			vmd = versions.get(rsn);
		return vmd;
	}

	private ClassMetricData readClassProperties(String properties,
			VersionMetricData vmd)
	{
		// The correct name will be set in the loop below.
		ClassMetricData cmd = new ClassMetricData("");
		// Create properties
		String[] toks = properties.split(",");
		for (int i = 0; i < toks.length; i++)
		{
			String key = toks[i].substring(0, toks[i].indexOf("="));
			String value = toks[i].substring(toks[i].indexOf("=") + 1, toks[i]
					.length());
			cmd.setProperty(ClassMetric.parse(key), value);
		}
		vmd.metricData.put(cmd.get(ClassMetric.NAME), cmd);
		return cmd;
	}

	private void readClassMetrics(String metrics, ClassMetricData cmd)
	{
		// Start at one to skip blank line.
		// Finish as -1 to skip blank line.
		String[] toks = metrics.split("\\{|,|\\}");
		for (int i = 1; i < toks.length - 1; i++)
		{
			String key = toks[i].substring(0, toks[i].indexOf("="));
			int value = Integer.parseInt(toks[i].substring(
					toks[i].indexOf("=") + 1,
					toks[i].length()));
			cmd.setSimpleMetric(ClassMetric.parse(key), value);
		}
	}

	/**
     * Extracts the class dependencies, internal dependencies and users.
     * 
     * @param deps
     * @param rsn
     */
	private void readClassDepsEtc(String deps, ClassMetricData cmd)
	{
		String[] toks = deps.split("\\],\\[");

		// Dependencies
		if (toks.length >= 1)
		{
			String tmpDependencies = toks[0];
			String[] dependencies = tmpDependencies.split("\\[|,");
			for (int i = 0; i < dependencies.length; i++)
			{
				cmd.dependencies.add(dependencies[i].trim());
			}
		}

		// Inner dependencies.
		if (toks.length >= 2)
		{
			String tmpInternalDeps = toks[1];
			String[] innerDeps = tmpInternalDeps.split("\\[|,");
			for (int i = 0; i < innerDeps.length; i++)
			{
				cmd.internalDeps.add(innerDeps[i].trim());
			}
		}

		// Inner dependencies.
		if (toks.length == 3)
		{
			String tmpUsers = toks[2];
			String[] users = tmpUsers.split("\\[|,");
			for (int i = 0; i < users.length; i++)
			{
				cmd.internalDeps.add(users[i].trim());
			}
		}
	}

	private void readMethods(String[] tokens, ClassMetricData cmd)
	{
		HashMap<String, int[]> methodMap = new HashMap<String, int[]>();
		for (int i = 0; i < tokens.length; i++)
		{
			String[] method = tokens[i].split(",|\\{|\\}");
			int[] methodMetrics = new int[MethodMetric.values().length];
			String methodName = method[0];
			if (methodName.length() == 0)
				continue; // skip blank line

			for (int j = 1; j < method.length; j++)
			{
				methodMetrics[j - 1] = Integer.parseInt(method[j].trim());
			}
			methodMap.put(methodName.trim(), methodMetrics);
		}
		cmd.methods = new MethodMetricMap(methodMap);
	}

	private void writeHistoryToCSV(HistoryMetricData hmd, BufferedWriter bw)
			throws IOException
	{
		Collection c = hmd.versions.values();
		Iterator it = c.iterator();
		bw.write("$" + c.size() + "\n");
		bw.flush();
		int work = c.size();
		int at = 1;

		while (it.hasNext())
		{
			bw.write(writeVersionToCSV((VersionMetricData) it.next()));
			bw.newLine();
			bw.flush();
			completion = (int) (((double) at / (double) work) * 100);
			setChanged();
			notifyObservers();
			at++;
		}
	}

	private String writeVersionToCSV(VersionMetricData vmd)
	{
		StringBuffer vBuffer = new StringBuffer();
		vBuffer.append(vmd.get(Version.RSN));
		vBuffer.append(delim);
		vBuffer.append(vmd.get(Version.ID));
		vBuffer.append(delim);

		String versionHeader = vBuffer.toString();
		Collection classes = vmd.metricData.values();
		Iterator it = classes.iterator();
		vBuffer.delete(0, vBuffer.length());
		while (it.hasNext())
		{
			vBuffer.append(versionHeader);
			vBuffer.append(writeClassToCSV((ClassMetricData) it.next()));
			vBuffer.append("\n");
		}
		return vBuffer.toString();
	}

	private <K, T> String writeClassToCSV(ClassMetricData cmd)
	{
		StringBuffer cBuffer = new StringBuffer();
		// cBuffer.append(cmd.get(ClassMetric.CLASS_NAME));
		// cBuffer.append(delim);
		Set metricSet = cmd.simpleMetricSet();
		Set propertySet = cmd.propertySet();

		cBuffer.append(StringUtils.toCSVString(
				propertySet,
				delim,
				propertyExclusionList));
		cBuffer.append(delim);
		cBuffer.append(StringUtils.toCSVString(
				metricSet,
				delim,
				false,
				metricExclusionList));
		cBuffer.append(delim);
		cBuffer.append(StringUtils.toCSV(cmd.dependencies));
		cBuffer.append(delim);
		cBuffer.append(StringUtils.toCSV(cmd.internalDeps));
		cBuffer.append(delim);
		cBuffer.append(StringUtils.toCSV(cmd.users));
		// Iterator it = cmd.methods.iterator();
		// while (it.hasNext())
		// {
		// cBuffer.append(writeMethodToCSV((MethodMetricData) it.next()));
		// }
		cBuffer.append(writeMethodsToCSV(cmd.methods));
		return cBuffer.toString();
	}

	private String writeMethodsToCSV(MethodMetricMap mmm)
	{
		StringBuffer mBuffer = new StringBuffer();
		HashMap<String, int[]> methods = mmm.methods();
		Set<Entry<String, int[]>> entrySet = methods.entrySet();
		Iterator<Entry<String, int[]>> it = entrySet.iterator();
		while (it.hasNext())
		{
			Entry<String, int[]> entry = it.next();
			mBuffer.append("{");
			mBuffer.append(entry.getKey());
			mBuffer.append(delim);
			mBuffer.append(StringUtils.toCSVString(entry.getValue(), false));
			mBuffer.append("}");
			mBuffer.append(delim);
		}
		// Strip very last comma. (extra one).
		if (mBuffer.length() >= 1)
			mBuffer.delete(mBuffer.length() - 1, mBuffer.length());
		return mBuffer.toString();
	}

	// private String writeMethodToCSV(MethodMetricData mmd)
	// {
	// StringBuffer cBuffer = new StringBuffer();
	// // cBuffer.append(mmd.get(Method.NAME));
	// // cBuffer.append(delim);
	// Set propertySet = mmd.propertySet();
	// Set metricSet = mmd.simpleMetricSet();
	//
	// cBuffer.append(StringUtils.toCSVString(propertySet, delim));
	// cBuffer.append(delim);
	// cBuffer.append(StringUtils.toCSVString(metricSet, delim, false));
	// return cBuffer.toString();
	// }

	/**
     * @return the completion
     */
	public final int getCompletion()
	{
		return completion;
	}

}
