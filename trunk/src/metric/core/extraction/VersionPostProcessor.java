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
import metric.core.util.MetricUtil;
import metric.core.util.StatUtils;
import metric.core.util.logging.LogOrganiser;
import metric.core.vocabulary.ClassMetric;
import metric.core.vocabulary.Evolution;
import metric.core.vocabulary.MetricType;
import metric.core.vocabulary.Version;

public class VersionPostProcessor extends Observable
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private HistoryMetricData hmd;
	private int processed;;
	private BlockingQueue<VersionMetricData> versions;
	private VersionPersister persister;

	public VersionPostProcessor(HistoryMetricData hmd, BlockingQueue<VersionMetricData> versions,
			VersionPersister persister)
	{
		this.hmd = hmd;
		this.versions = versions;
		LogOrganiser.addLogger(logger);
		logger.setLevel(Level.ALL);
		this.persister = persister;
	}

	public void process()
	{

		// Perform first pass processing.
		firstPassProcessing();
		
		// Cannot perform second pass processing with 1 version.
		if (hmd.size() == 1)
			return;

		// Ensure all versions have been properly persisted with first-pass
		// processing done.
		synchroniseThreadWithPersister(hmd.size());
		persister.reset();

		for (int i = 2; i <= hmd.size(); i++)
		{
			VersionMetricData vmd = hmd.getVersion(i - 1);
			VersionMetricData vmd2 = hmd.getVersion(i);
			
			// Perform second pass processing.
			secondPassProcessing(vmd, vmd2);

			// Notify observers we are post-processing.
			// Because we have to look ahead a version for surivor post
			// processing we have to process every version twice. So we only
			// notify the first version being processed.
			versions.offer(vmd);
			versions.offer(vmd2);
			updateObservers(vmd);

			// Must wait for VersionPersister to persist these to disk first so
			// the correct data is loaded up on the next loop.
			synchroniseThreadWithPersister(i);

			// There isn't another version to check this against, so just log it
			// with the post-processing done on it so far.
			if (i == hmd.size())
			{
				updateObservers(vmd2);
			}
		}
	}

	/**
     * Synchronises the current thread with the VersionPersister by forcing it
     * to wait until the VersionPersister has persister the specified number of
     * Versions since the last reset.
     * 
     * @param persisterIndex The number of versions that should be persister
     *            before allowing this thread to continue.
     */
	private void synchroniseThreadWithPersister(int persisterIndex)
	{
		while (persister.getProcessingDone() != persisterIndex)
		{
			try
			{
				System.out.println("Finished first pass processing and waiting on persister.");
				System.out.println("Done: " +persister.getProcessingDone() + " Index: " + persisterIndex);
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	private void firstPassProcessing()
	{
		for (int i = 1; i <= hmd.size(); i++)
		{
			VersionMetricData vmd = hmd.getVersion(i);
			computeInstructionCount(vmd);
			computeDependencies(vmd);
			computeFanIn(vmd);
			computeLayers(vmd);
			computeGUIAndIOClasses(vmd);
			computeGUIClassCount(vmd);
			computeHiLowTime(vmd);

			versions.offer(vmd);
			updateObservers(vmd);
		}
	}

	private void secondPassProcessing(VersionMetricData vmd, VersionMetricData vmd2)
	{
		scanAndMarkSurvivors(vmd, vmd2);
		// updateDistanceMovedSinceBirth(vmd);
		// updateDistanceMovedSinceBirth(vmd2);
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
	private void computeGUIClassCount(VersionMetricData vmd)
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

	private void computeInstructionCount(VersionMetricData vmd)
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

			cm
					.setSimpleMetric(ClassMetric.LOAD_RATIO, (int) (((double) cm
							.getSimpleMetric(ClassMetric.LOAD_COUNT) / (cm.getSimpleMetric(ClassMetric.LOAD_COUNT) + cm
							.getSimpleMetric(ClassMetric.STORE_COUNT))) * 10));
		}
	}

	/**
     * Should be called only after metric data has been extracted Fanin will be
     * updated directly on the ClassMetric object Internal Fanout value will
     * also be calculated and updated
     */
	private void computeDependencies(VersionMetricData vmd)
	{
		for (ClassMetricData cm : vmd.metricData.values())
		{
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
	}

	private void computeFanIn(VersionMetricData vmd)
	{
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
	private void computeGUIAndIOClasses(VersionMetricData vmd)
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
	private void computeLayers(VersionMetricData vmd)
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
		}
	}

	private void computeDistance(ClassMetricData cmd)
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
	private Set<String> flagUsersAsIO(VersionMetricData vmd, Set<String> classNameSet)
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
	private Set<String> flagUsersAsGUI(VersionMetricData vmd, Set<String> classNameSet)
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
						vmd.metricData.get(userClassName).setSimpleMetric(ClassMetric.GUI_DISTANCE, 1);
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
	private void scanAndMarkSurvivors(VersionMetricData v1, VersionMetricData v2)
	{
		// Look ahead at version two classes.
		for (ClassMetricData cm2 : v2.metricData.values())
		{
			// Assume unchanged
			cm2.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, Evolution.NEW.getValue());
			ClassMetricData cm1 = v1.metricData.get(cm2.get(ClassMetric.NAME));
			if (cm1 == null)
			{
				cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.NEW.getValue());
				cm2.setSimpleMetric(ClassMetric.MODIFICATION_FREQUENCY, 0);
				cm2.setSimpleMetric(ClassMetric.BORN_RSN, v2.getSimpleMetric(Version.RSN));
				cm2.setSimpleMetric(ClassMetric.AGE, 1);

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
					cm2.setSimpleMetric(ClassMetric.MODIFICATION_FREQUENCY, cm1
							.getSimpleMetric(ClassMetric.MODIFICATION_FREQUENCY));
					cm2.setSimpleMetric(ClassMetric.MODIFIED_METRIC_COUNT, 0);
				} else
				// found, but it is not an exact match
				{
					cm2.setSimpleMetric(ClassMetric.EVOLUTION_STATUS, Evolution.MODIFIED.getValue());
					cm2.setSimpleMetric(ClassMetric.MODIFICATION_FREQUENCY, cm1
							.getSimpleMetric(ClassMetric.MODIFICATION_FREQUENCY) + 1);
					cm1.setSimpleMetric(ClassMetric.IS_MODIFIED, 1);
					cm2.setSimpleMetric(ClassMetric.AGE, 1);
					setEvolutionDistanceFrom(cm1, cm2);
					cm2.setSimpleMetric(ClassMetric.MODIFIED_METRIC_COUNT, computeModifiedMetrics(cm2, cm1));
					cm1.setSimpleMetric(ClassMetric.NEXT_VERSION_STATUS, Evolution.MODIFIED.getValue());
				}
			}
		}
	}

	private void updateDeletedClasses(VersionMetricData v1, VersionMetricData v2)
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
     * Will go through the entire history and computes this. Assumes
     * scanAndMarkSurvivors has been called
     */
	private void updateDistanceMovedSinceBirth(VersionMetricData vmd)
	{
		for (ClassMetricData cm : vmd.metricData.values())
		{
			if (cm.getSimpleMetric(ClassMetric.BORN_RSN) == vmd.getSimpleMetric(Version.RSN)) // new
			// born
			{
				cm.setSimpleMetric(ClassMetric.DISTANCE_MOVED_SINCE_BIRTH, 0);
				cm.setSimpleMetric(ClassMetric.MODIFICATION_STATUS_SINCE_BIRTH, Evolution.NEW_BORN.getValue());
				continue; // move to the next class
			}

			if (cm.getSimpleMetric(ClassMetric.BORN_RSN) >= vmd.getSimpleMetric(Version.RSN))
			{
				logger.log(Level.WARNING, "Born RSN greater than Version RSN....Something went wrong.");
				continue; // keep moving
			}

			// born before current version, get the ancestor
			// ClassMetricData ancestor =
			// versions.get(ClassMetric.BORN_RSN).metricData.get(cm.className);
			ClassMetricData ancestor = null;
			if (ancestor.isExactMatch(cm))
			{
				cm.setSimpleMetric(ClassMetric.MODIFICATION_STATUS_SINCE_BIRTH, Evolution.NEVER_MODIFIED.getValue());
			} else
			{
				setEvolutionDistanceSinceBirth(cm, ancestor);

				cm.setSimpleMetric(ClassMetric.MODIFICATION_STATUS_SINCE_BIRTH, Evolution.MODIFIED_AFTER_BIRTH
						.getValue());
				cm.setSimpleMetric(ClassMetric.MODIFIED_METRIC_COUNT_SINCE_BIRTH, computeModifiedMetrics(cm, ancestor));
			}
		}
	}

	public void setEvolutionDistanceSinceBirth(ClassMetricData cm1, ClassMetricData cm2)
	{
		double ed = MetricUtil.distanceFrom(cm1.getMetrics(), cm2.getMetrics(), MetricType.DISTANCE);
		// if (ed > 0) ed += 0.5;
		// distanceMovedSinceBirth = scaleDoubleMetric(ed, 10, 100);
		cm1.setSimpleMetric(ClassMetric.DISTANCE_MOVED_SINCE_BIRTH, (int) Math.round(ed));
		if ((ed > 0) && (cm1.getSimpleMetric(ClassMetric.DISTANCE_MOVED_SINCE_BIRTH) < 1))
		{
			cm1.setSimpleMetric(ClassMetric.DISTANCE_MOVED_SINCE_BIRTH, 1);
		}
		if (ed == 0)
		{
			cm1.setSimpleMetric(ClassMetric.DISTANCE_MOVED_SINCE_BIRTH, 0);
		}
	}

	/**
     * Computes the number of metrics that are different between this class and
     * cm
     */
	private int computeModifiedMetrics(ClassMetricData cm, ClassMetricData cm2)
	{
		int mmc = 0; // modified metric count, assume no change

		for (int i = 0; i < ClassMetric.getNumberOfComparativeMetrics(); i++)
		{
			if (cm.getMetrics()[i] != cm2.getMetrics()[i])
			{
				mmc++;
			}
		}
		return mmc;
	}

	/**
     * Computes the distance of the class from this. This is used to check how
     * far a class has evolved or to check if it is in the same neighbourhood.
     */
	private void setEvolutionDistanceFrom(ClassMetricData cm1, ClassMetricData cm2)
	{
		double ed = MetricUtil.distanceFrom(cm1.getMetrics(), cm2.getMetrics(), MetricType.DISTANCE);
		cm1.setSimpleMetric(ClassMetric.EVOLUTION_DISTANCE, StatUtils.scaleDoubleMetric(ed, 100, 1000));
	}
}
