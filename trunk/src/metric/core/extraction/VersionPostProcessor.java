package metric.core.extraction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import metric.core.model.ClassMetricData;
import metric.core.model.HistoryMetricData;
import metric.core.model.VersionMetricData;
import metric.core.util.StatUtils;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;
import metric.core.vocabulary.Version;

public class VersionPostProcessor extends Observable
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private HistoryMetricData hmd;
	private int processed;;
	private BlockingQueue<VersionMetricData> versions;

	public VersionPostProcessor(HistoryMetricData hmd, BlockingQueue<VersionMetricData> versions)
	{
		this.hmd = hmd;
		this.versions = versions;
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
	}

	public void process()
	{
		// De-serialize from file.

		if (hmd.size() == 1)
		{
			VersionMetricData vmd = hmd.getVersion(1);
			firstPassProcessing(vmd);
			versions.offer(vmd);
			updateObservers(vmd);
			return;
		}
		for (int i = 2; i <= hmd.size(); i++)
		{
			VersionMetricData vmd = hmd.getVersion(i - 1);
			// Get next version
			VersionMetricData vmd2 = hmd.getVersion(i);

			firstPassProcessing(vmd);
			firstPassProcessing(vmd2);
			secondPassProcessing(vmd, vmd2);

			// Notify observers we are post-processing.
			// Because we have to look ahead a version for surivor post
			// processing we have to process every version twice. So we only
			// notify the first version being processed.
			versions.offer(vmd);
			versions.offer(vmd2);
			updateObservers(vmd);

			// There isn't anothe version to check this against, so just log it
			// with the post-processing done on it so far.
			if (i == hmd.size())
			{
				updateObservers(vmd2);
			}
		}
	}

	private void updateObservers(VersionMetricData vmd)
	{
		String msg = "Post-Processing - " + vmd;
		setChanged();
		notifyObservers(msg);
		logger.log(Level.ALL, msg);
	}

	private void firstPassProcessing(VersionMetricData vmd)
	{
		computeGUIClassCount(vmd);
		computeDependencies(vmd);
		computeGUIAndIOClasses(vmd);
		computeHiLowTime(vmd);
		computeLayers(vmd);
	}

	private void secondPassProcessing(VersionMetricData vmd, VersionMetricData vmd2)
	{
		scanAndMarkSurvivors(vmd, vmd2);
		updateDeletedClasses(vmd, vmd2);
	}

	public int getProcessed()
	{
		return processed;
	}

	// TODO: Fixme, urgent. Could be losing precision here.
	// Check whether this really does need to be of type double.
	// This should actually be changed to a complex metric and computed
	// on the fly.
	private static void computeGUIClassCount(VersionMetricData vmd)
	{
		double sum = 0.0;

		for (ClassMetricData c : vmd.metricData.values())
		{
			sum += c.getSimpleMetric(ClassMetric.GUI_DISTANCE);
		}
		vmd.setSimpleMetric(Version.GUI_CLASS_COUNT, (int) sum);
	}

	/**
     * Computes the lowest and highest modification time for the classes found
     * in the specified <code>VersionMetricData</code>.
     * 
     * @param vmd The VersionMetricData to compute the hi/low time for.
     */
	private void computeHiLowTime(VersionMetricData vmd)
	{
		Collection<ClassMetricData> versions = vmd.metricData.values();
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;

		for (ClassMetricData cmd : versions)
		{
			if (cmd.lastModified < min)
				min = cmd.lastModified;
			else if (cmd.lastModified > max)
				max = cmd.lastModified;
		}
		vmd.lowModifiedTime = min;
		vmd.hiModifiedTime = max;
	}

	/**
     * Should be called only after metric data has been extracted Fanin will be
     * updated directly on the ClassMetric object Internal Fanout value will
     * also be calculated and updated
     */
	private static void computeDependencies(VersionMetricData vmd)
	{
		for (ClassMetricData cm : vmd.metricData.values())
		{
			int storeCount = cm.getSimpleMetric(ClassMetric.ISTORE_COUNT)
					+ cm.getSimpleMetric(ClassMetric.STORE_FIELD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.REF_STORE_OP_COUNT);

			cm.setSimpleMetric(ClassMetric.STORE_COUNT, storeCount);

			int loadCount = cm.getSimpleMetric(ClassMetric.ILOAD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.LOAD_FIELD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT)
					+ cm.getSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT);

			cm.setSimpleMetric(ClassMetric.LOAD_COUNT, loadCount);
			for (String name : cm.dependencies)
			{
				if (name.equals(cm))
					continue; // ignore self-dependencies
				ClassMetricData fanInNode = vmd.metricData.get(name);
				if (fanInNode != null)
				{
					cm.internalDeps.add(name);
					fanInNode.users.add(cm.get(ClassMetric.NAME));
				}
			}
		}

		// Set fan-in counts and compute distance
		for (ClassMetricData cm : vmd.metricData.values())
		{
			cm.setSimpleMetric(ClassMetric.FAN_IN_COUNT, cm.users.size());
			cm.setSimpleMetric(ClassMetric.INTERNAL_FAN_OUT_COUNT, cm.internalDeps.size());

			computeDistance(cm); // compute distance

			// Compute metrics.
			cm.setSimpleMetric(ClassMetric.RAW_SIZE_COUNT, cm.getSimpleMetric(ClassMetric.LOAD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.STORE_COUNT) + cm.getSimpleMetric(ClassMetric.BRANCH_COUNT)
					+ cm.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) + cm.getSimpleMetric(ClassMetric.INTERFACE_COUNT));

			cm.incrementMetric(ClassMetric.RAW_SIZE_COUNT, cm.getSimpleMetric(ClassMetric.METHOD_COUNT)
					+ cm.getSimpleMetric(ClassMetric.FIELD_COUNT) + cm.getSimpleMetric(ClassMetric.METHOD_CALL_COUNT));
			cm.setSimpleMetric(
					ClassMetric.NORMALIZED_BRANCH_COUNT,
					(int) (((double) cm.getSimpleMetric(ClassMetric.BRANCH_COUNT) / cm
							.getSimpleMetric(ClassMetric.RAW_SIZE_COUNT)) * 100.0));
		}
	}

	/** Flag all classes that depend on a GUI class as GUI as well */
	private static void computeGUIAndIOClasses(VersionMetricData vmd)
	{
		Set<String> flagged = vmd.metricData.keySet();
		while (flagged.size() > 0)
			flagged = flagUsersAsGUI(vmd, flagged);
		// Now flag all IO classes like we did GUI
		flagged = vmd.metricData.keySet();
		while (flagged.size() > 0)
			flagged = flagUsersAsIO(vmd, flagged);
	}

	/** Computes the layers and instability metrics */
	private static void computeLayers(VersionMetricData vmd)
	{
		// Now update the fanin and instability value for each class
		for (ClassMetricData cmd : vmd.metricData.values())
		{
			cmd.setSimpleMetric(ClassMetric.INSTABILITY, (int) (((double) cmd
					.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) / (cmd.getSimpleMetric(ClassMetric.FAN_OUT_COUNT) + cmd
					.getSimpleMetric(ClassMetric.FAN_IN_COUNT))) * 1000));

			int fanIn = cmd.getSimpleMetric(ClassMetric.FAN_IN_COUNT);
			int internalFanOut = cmd.getSimpleMetric(ClassMetric.INTERNAL_FAN_OUT_COUNT);

			if ((fanIn > 0) && (internalFanOut > 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 1); // mid
			else if ((fanIn == 0) && (internalFanOut > 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 2); // top
			else if ((fanIn > 0) && (internalFanOut == 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 0); // foundation
			else if ((fanIn == 0) && (internalFanOut == 0))
				cmd.setSimpleMetric(ClassMetric.LAYER, 3); // free
			else
				cmd.setSimpleMetric(ClassMetric.LAYER, 4);
			; // there should be no classes here, hopefully

			// Compute distance for each ClassMetricData
			// computeDistance(cmd);
		}
	}

	private static void computeDistance(ClassMetricData cmd)
	{
		// If not computed then do it.
		if (cmd.getSimpleMetric(ClassMetric.COMPUTED_DISTANCE) < 0)
		{
			double l = StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.METHOD_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.FIELD_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.BRANCH_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.ZERO_OP_INSN_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.TYPE_INSN_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.ILOAD_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.ISTORE_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.LOAD_FIELD_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.STORE_FIELD_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.REF_LOAD_OP_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.REF_STORE_OP_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.TRY_CATCH_BLOCK_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.INTERFACE_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.SUPER_CLASS_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.FAN_OUT_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.EX_METHOD_CALL_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.IN_METHOD_CALL_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.LOCAL_VAR_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.PRIVATE_METHOD_COUNT))
					+ StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.CONSTANT_LOAD_COUNT));
			l += StatUtils.sqr(cmd.getSimpleMetric(ClassMetric.INCREMENT_OP_COUNT));
			l = Math.sqrt(l);
			cmd.setSimpleMetric(ClassMetric.COMPUTED_DISTANCE, StatUtils.scaleDoubleMetric(l, 10, 1000.0));
		}
	}

	/** Given an input set, it will iterate over it and flag all users as IO */
	private static Set<String> flagUsersAsIO(VersionMetricData vmd, Set<String> classNameSet)
	{
		Set<String> flagged = new HashSet<String>();
		for (String cn : classNameSet)
		{
			if (vmd.metricData.get(cn).getSimpleMetric(ClassMetric.IS_IO_CLASS) == 1)
			{
				for (String userClassName : vmd.metricData.get(cn).users)
				{
					if (vmd.metricData.get(userClassName).getSimpleMetric(ClassMetric.IS_IO_CLASS) == 0)
					{
						vmd.metricData.get(userClassName).setSimpleMetric(ClassMetric.IS_IO_CLASS, 1);
						flagged.add(userClassName);
					}
				}
			}
		}
		return flagged;
	}

	/** Given an input set, it will iterate over it and flag all users as GUI */
	private static Set<String> flagUsersAsGUI(VersionMetricData vmd, Set<String> classNameSet)
	{
		Set<String> flagged = new HashSet<String>();
		for (String cn : classNameSet)
		{
			double currGUIDistance = vmd.metricData.get(cn).getSimpleMetric(ClassMetric.GUI_DISTANCE);
			if (currGUIDistance != 0) // if it is a GUI class
			{
				// Check its immediate users and make them all as GUI classes as
				// well
				for (String userClassName : vmd.metricData.get(cn).users)
				{
					if (vmd.metricData.get(userClassName).getSimpleMetric(ClassMetric.GUI_DISTANCE) == 0)
					{
						// FIXME - URGENT: double cast to int as interim step in
						// shitfing to vocab.
						vmd.metricData.get(userClassName).setSimpleMetric(
								ClassMetric.GUI_DISTANCE,
								(int) currGUIDistance / 2);
						flagged.add(userClassName);
					}
				}
			}
		}
		return flagged;
	}

	/**
     * Updates the age for each Class in every Version. Increments age if the
     * class is an exact match from before
     */
	private static void scanAndMarkSurvivors(VersionMetricData v1, VersionMetricData v2)
	{
		// Look ahead at version two classes.
		for (ClassMetricData cm2 : v2.metricData.values())
		{
			// Assume unchanged
			cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, Evolution.UNCHANGED.getValue());
			ClassMetricData cm1 = v1.metricData.get(cm2.get(ClassMetric.NAME));
			if (cm1 == null)
			{
				cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.NEW.getValue());
				cm2.setSimpleMetric(ClassMetric.BORN_RSN, v2.getSimpleMetric(Version.RSN));
				cm2.setSimpleMetric(ClassMetric.AGE, 0);
			}
			// found in previous version
			else
			{
				cm2.setSimpleMetric(ClassMetric.BORN_RSN, cm1.getSimpleMetric(ClassMetric.BORN_RSN));

				// This class is a survivor.
				if (cm2.isExactMatch(cm1))
				{
					cm2.setSimpleMetric(ClassMetric.AGE, cm1.getSimpleMetric(ClassMetric.AGE) + 1);
					cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.UNCHANGED.getValue());
					cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS, Evolution.UNCHANGED.getValue());
					cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, Evolution.UNCHANGED.getValue());
				} else
				// found, but it is not an exact match
				{
					cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.MODIFIED.getValue());
					cm1.setSimpleMetric(ClassMetric.IS_MODIFIED, 1);
					cm2.setSimpleMetric(ClassMetric.AGE, 1);
					setEvolutionDistanceFrom(cm1, cm2);
					cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS, Evolution.MODIFIED.getValue());
				}
			}
		}

	}

	private static void updateDeletedClasses(VersionMetricData v1, VersionMetricData v2)
	{
		// Now look for deleted names and mark them down
		for (ClassMetricData cm1 : v1.metricData.values())
		{
			ClassMetricData cm2 = v2.metricData.get(cm1.get(ClassMetric.NAME));
			if (cm2 == null)
			{
				cm1.setSimpleMetric(ClassMetric.IS_DELETED, 1);
				cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS, Evolution.DELETED.getValue());
			}
		}
	}

	/**
     * Sets the evolution distance of cm2 to the distance from cm1 to cm2.
     */
	private static void setEvolutionDistanceFrom(ClassMetricData cm1, ClassMetricData cm2)
	{
		double ed = cm2.distanceFrom(cm1);
		double scaleMax = 100.0;
		double metricMax = 1000.0;
		if (ed > metricMax)
			ed = metricMax;
		double scaledValue = (scaleMax * ed) / metricMax;
		cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, (int) Math.round(scaledValue));
	}
}
