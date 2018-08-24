package com.bonitasoft.custompage.longboard.connectortracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.bonitasoft.engine.tracking.csv.CSVFlushEventListener;
import org.bonitasoft.engine.tracking.memory.DayRecord;
import org.bonitasoft.engine.tracking.memory.MemoryFlushEventListener;

// public class TimeTracker implements TenantLifecycleService {
public class TrackerAccess {
	final static Logger logger = Logger.getLogger(TrackerAccess.class.getName());

	private static List<TimeTrackerRecords> listListener = Arrays.asList(TimeTrackerRecords.EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT, TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE, TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS, TimeTrackerRecords.EXECUTE_CONNECTOR_INPUT_EXPRESSIONS,
			TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT, TimeTrackerRecords.EXECUTE_CONNECTOR_WORK, TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT, TimeTrackerRecords.EVALUATE_EXPRESSION, TimeTrackerRecords.EVALUATE_EXPRESSIONS);

	// private MemoryFlushEventListener memoryFlushEventListener;

	/**
	 * ----------------------------------------------------------------
	 * startRecording
	 *
	 * @param start
	 *            is true, then the recording is started.
	 * @return
	 */
	public static HashMap<String, Object> runService(final boolean start, final APISession apiSession) {
		logger.info("com.bonitasoft.custompage.longboard:================================ Run service");

		final HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("errormessage", "");
		String message = "";

		try {

			final TimeTracker timeTracker = getTimeTracker(apiSession);
			if (start) {
				// message += "start requested;";

				for (TimeTrackerRecords timeTrackerRecord : listListener) {
					timeTracker.activateRecord(timeTrackerRecord);
				}

				// message += "];";

				logger.info("com.bonitasoft.custompage.longboard:================================ listEvent[" + message + "]");

				if (!timeTracker.isTracking()) {
					logger.info("com.bonitasoft.custompage.longboard:================================ Not already started, do it now");
					message += "";
					timeTracker.start();
					message += "Tracking start...;";
					// Protect against the timeTracker.stopTracking() : force a
					// stop before
					timeTracker.stopTracking();

					timeTracker.startTracking();
				}
				// result.put("flushIntervalSeconds",
				// timeTracker.getFlushIntervalInSeconds());
				// message += "Flush every" +
				// timeTracker.getFlushIntervalInSeconds() + " s;";

			} else {

				message += "Tracking stop...;";
				timeTracker.stopTracking();
				message += "";
				timeTracker.stop();
				/*
				 * final CollectorFlushEventListener collectorFlushEvent =
				 * getCollectorFlushEventListener(timeTracker); if
				 * (collectorFlushEvent != null) { message +=
				 * "Tracer found, stop it"; //
				 * timeTracker.unRegisterListener(collectorFlushEvent);
				 * collectorFlushEvent.clear(); }
				 */
				// message += "EventListener[" +
				// timeTracker.getListEventListener() + "]";

			}

			message += "State: [" + (timeTracker.isTracking() ? "Tracking ON" : "Tracking OFF") + "];";
			result.putAll(getServiceState(apiSession));
			result.put("msg", message);
			logger.info("com.bonitasoft.custompage.longboard: State[" + timeTracker.isTracking() + "] msg=[" + message + "]");

		} catch (final Exception e) {

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();

			logger.info("com.bonitasoft.custompage.longboard:================= TrackerAccess.Start : Error exception[" + e.toString() + "] at " + exceptionDetails);
			message += "Error";
			result.put("errormessage", "Error " + e.toString());
		}

		return result;
	}

	/**
	 * get the service state
	 *
	 * @param apiSession
	 * @return
	 */
	public static HashMap<String, Object> getServiceState(final APISession apiSession) {
		logger.info("com.bonitasoft.custompage.longboard: =========================== TrackerAccess.getInfo");

		final HashMap<String, Object> result = new HashMap<String, Object>();
		try {

			/*
			 * if (!(timeTrackerObj instanceof TimeTrackerV2)) {
			 * result.put("errormessage",
			 * "TimeTracker V2 is not installed service give the class[" +
			 * timeTrackerObj.getClass().getName() + "]");
			 * logger.severe("TimeTracker V2 is not installed");
			 * 
			 * return result; }
			 */
			final TimeTracker timeTracker = getTimeTracker(apiSession);
			logger.info("com.bonitasoft.custompage.longboard:=========================== TimeTrackerV3 Service OK");

			// started IF the service is started AND the listener is present
			final boolean isStarted = timeTracker == null ? false : true;
			boolean isTracking = timeTracker == null ? false : timeTracker.isTracking();

			// ConnectorTrackRecordEvents connectorTrackRecordEvents =
			// getConnectorTrackRecordEvent( timeTracker);
			/*
			 * final CollectorFlushEventListener collectorFlushEventListener =
			 * getCollectorFlushEventListener(timeTracker); if (isStarted) {
			 * isRegistered = collectorFlushEventListener != null; }
			 */
			result.put("isstarted", isStarted);
			result.put("isregistered", isTracking);
			result.put("startedmsg", (isTracking ? "Events recorded" : "Events not recorded"));

			// result.put("startedmsg", (isStarted ? "service started, " :
			// "service not started, ") + (isTracking ? "Events recorded" :
			// "Events not recorded"));
			if (timeTracker != null) {

			}
			logger.info("com.bonitasoft.custompage.longboard: ===== isStarted tenant[" + apiSession.getTenantId() + "] isStarted: " + isStarted + "] isTracking : " + isTracking + "]");
			// + "] collector[" + collectorFlushEventListener + "] listMess[" +
			// timeTracker.getListEventListener() + "]");
		} catch (final Exception e) {
			result.put("errormessage", "Error " + e.toString());
			final StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			logger.severe("Error=" + e.toString() + " at " + sw.toString());

		}
		return result;
	}


	private String sourceInformation;

	/**
	 * the method is used then some cache can be use by the object
	 *
	 * @param isSimulation
	 * @param showAllInformations
	 * @param rangedisplayInH
	 * @param apiSession
	 * @return
	 */
	public Map<String, Object> getInfos(final boolean isSimulation, final boolean showAllInformations, final int rangedisplayInH, boolean rangeDisplayDuration, boolean rangeDisplayMaximum, List<FlushEventListener> listFlushEvent, final APISession apiSession, final ProcessAPI processAPI) {
		logger.info("com.bonitasoft.custompage.longboard:=========================== TrackerAccess.getInfo isSimulation[" + isSimulation + "] showallinformation[" + showAllInformations + "] rangedisplauyinH[" + rangedisplayInH + "]");

		String info = "";
		final Map<String, Object> result = new HashMap<String, Object>();
		result.put("errormessage", "");
		result.put("startedmsg", "...");
		sourceInformation = "";
		try {
			cacheProcessDefinition = new HashMap<Long, ProcessDefinition>();
			cacheActivity = new HashMap<Long, BaseElement>();
			cacheProcess = new HashMap<Long, BaseElement>();

			TimeTracker timeTracker = getTimeTracker(apiSession);

			List<Record> listRecords = null;
			;
			if (isSimulation) {
				listRecords = getSimulationRecords();
			} else {
				//
				// let's ask the listener
				// we don't want the user to install / configure a new service
				// or whatever, so let's adapt to what we have.
				if (listFlushEvent != null)
					for (FlushEventListener eventListener : listFlushEvent) {
						if (eventListener instanceof MemoryFlushEventListener) {
							sourceInformation += "Source:Memory;";
							DayRecord dayRecord = ((MemoryFlushEventListener) eventListener).getDayRecord();
							listRecords = dayRecord.getRecordsCopy();
						}
						if (eventListener instanceof CSVFlushEventListener) {
							sourceInformation += "Source:Csv ";
							// gosh - let's read the file then, but the class
							// does not have a getter on the file
							listRecords = readRecordFromCsvListener(((CSVFlushEventListener) eventListener));
						}
					}
				// listRecords = timeTracker.getRecordsCopy();
				// List<Record> listRecords2 = timeTracker.getRecordsCopy();;
				// DayRecord dayRecord= memoryFlushEventListener.getDayRecord();
				// listRecords = dayRecord.getRecordsCopy();
			}
			final List<HashMap<String, Object>> allInformations = new ArrayList<HashMap<String, Object>>();

			// put all informations only is requested

			// build an synthesis array, time 10 mn per 10 mn
			final HashMap<String, StatsTracker> lastHours = new HashMap<String, StatsTracker>();
			final ArrayList<TrackerRecordAnalysed> top10Record = new ArrayList<TrackerRecordAnalysed>();

			final HashMap<String, StatsTracker> repartitionPerConnector = new HashMap<String, StatsTracker>();
			final HashMap<String, StatsTracker> repartitionPerWork = new HashMap<String, StatsTracker>();

			// cReferenceStartFilter is the first date : we keep all info AFTER
			// this date.
			final Calendar cReferenceStartFilter = Calendar.getInstance();
			cReferenceStartFilter.add(Calendar.HOUR_OF_DAY, -rangedisplayInH);
			cReferenceStartFilter.set(Calendar.MINUTE, 0);
			cReferenceStartFilter.set(Calendar.SECOND, 0);
			cReferenceStartFilter.set(Calendar.MILLISECOND, 0);
			// logger.info("com.bonitasoft.custompage.longboard:DateReferenceFilter
			// =" + cReferenceStartFilter.toString());
			if (listRecords != null) {
				for (int i=0;i<listRecords.size();i++)
				{
					final Record record =listRecords.get(i);
					if (record.getTimestamp() < cReferenceStartFilter.getTimeInMillis()) {
						continue; // too old
					}

					final TrackerRecordAnalysed recordAdvance = TrackerRecordAnalysed.getFromRecord(i+1, record, cacheProcessDefinition, cacheActivity, cacheProcess, processAPI);
					/*
					 * logger.info("--------------RECORD ADVANCE:"); for (final
					 * String key : recordAdvance.getHashMap().keySet()) {
					 * logger.info("             Key[" + key + "]=[" +
					 * recordAdvance.getHashMap().get(key) + "]"); }
					 */
					// keep all information
					allInformations.add(recordAdvance.getHashMap());

					// --------------------------- top 10
					top10Record.add(recordAdvance);

					// ---------------------------------- Range per range
					final Calendar cRecord = Calendar.getInstance();
					cRecord.setTimeInMillis(record.getTimestamp());
					final String key = getRangeKeyFromCalendar(cRecord);
					final String dateForGraph = getRangeDateForGraphFromCalendar(cRecord);

					StatsTracker statsTracker = lastHours.get(key);
					if (statsTracker == null) {
						statsTracker = new StatsTracker(key, dateForGraph, null);
					}
					statsTracker.nbExecution++;
					statsTracker.sumDurationMs += record.getDuration();
					if (record.getDuration() > statsTracker.maxDurationMs) {
						statsTracker.maxDurationMs = record.getDuration();
						statsTracker.details = recordAdvance.connectorDefinitionName + " task:" + recordAdvance.connectorInstanceId + " in " + recordAdvance.processDefinitionName + "(" + recordAdvance.processDefinitionVersion + ")";
					}

					lastHours.put(key, statsTracker);

					// logger.info("Trace record "+record.getTimestamp()+"
					// key="+key+" lastHours.get(key) exist ?
					// "+lastHours.get(key)+"] duration="+record.getDuration()+"
					// statsTracker.sumDuration="+statsTracker.sumDuration);
					// logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					// recordAdvance.connectorDefinitionName=[" +
					// recordAdvance.connectorDefinitionName + "] duration[" +
					// recordAdvance.record.getDuration() + "]");

					// ------------------------------ per type of connector
					if (recordAdvance.connectorDefinitionName != null) {
						StatsTracker statsTrackerConnector = repartitionPerConnector.get(recordAdvance.connectorDefinitionName);
						if (statsTrackerConnector == null) {
							statsTrackerConnector = new StatsTracker(recordAdvance.connectorDefinitionName, recordAdvance.connectorDefinitionName, null);
						}
						statsTrackerConnector.nbExecution++;
						statsTrackerConnector.sumDurationMs += recordAdvance.record.getDuration();
						repartitionPerConnector.put(recordAdvance.connectorDefinitionName, statsTrackerConnector);

					}
					// ------------------------------ per type of work
					if (recordAdvance.shortName != null) {
						StatsTracker statsTrackerConnector = repartitionPerWork.get(recordAdvance.shortName);
						if (statsTrackerConnector == null) {
							statsTrackerConnector = new StatsTracker(recordAdvance.shortName, recordAdvance.shortName, null);
						}
						statsTrackerConnector.nbExecution++;
						statsTrackerConnector.sumDurationMs += recordAdvance.record.getDuration();
						repartitionPerWork.put(recordAdvance.shortName, statsTrackerConnector);
					}

					// logger.info("KEEP Record ["+cRecord.toString()+"]
					// KeyRange="+
					// key+" StatsTraker["+statsTracker+"]");

				}
			}
			// logger.info("StatsTracker debug="+lastHours);

			// create a synthesis per RANGE
			// sow, we start now for the reference start, and we get all
			// different information
			final Calendar cRangePerRange = (Calendar) cReferenceStartFilter.clone();
			final ArrayList<HashMap<String, Object>> last24HoursSynthesis = new ArrayList<HashMap<String, Object>>();
			final ArrayList<StatsTracker> last24HoursStatsTracker = new ArrayList<StatsTracker>();
			// get the current range
			long timeEndRange=System.currentTimeMillis() + 10*60*1000;
			for (int i = 0; i < 24 * 6; i++) {
				// don't generate nothing after the current time please
				if (cRangePerRange.getTimeInMillis() > timeEndRange) {
					break;
				}
				final String key = getRangeKeyFromCalendar(cRangePerRange);
				final String dateForGraph = getRangeDateForGraphFromCalendar(cRangePerRange);

				StatsTracker statsTracker = lastHours.get(key);
				if (statsTracker == null) {
					statsTracker = new StatsTracker(key, dateForGraph, null);
				}
				final HashMap<String, Object> oneRangeRecord = new HashMap<String, Object>();
				oneRangeRecord.put("date", key);
				oneRangeRecord.put("nbExecution", statsTracker.nbExecution);
				oneRangeRecord.put("sumDuration", statsTracker.sumDurationMs);
				oneRangeRecord.put("avgDuration", statsTracker.getAvgDuration());
				oneRangeRecord.put("maxDuration", statsTracker.maxDurationMs);
				oneRangeRecord.put("details", statsTracker.details);
				// logger.info("Last24h = "+key+" exist ?"+lastHours.get(key)+"
				// : ["+oneRangeRecord.toString()+"]");
				last24HoursSynthesis.add(oneRangeRecord);
				last24HoursStatsTracker.add(statsTracker);
				cRangePerRange.add(Calendar.MINUTE, 10);

			}

			// ------------------------------------------- top 10
			Collections.sort(top10Record, new Comparator<TrackerRecordAnalysed>() {

				public int compare(final TrackerRecordAnalysed s1, final TrackerRecordAnalysed s2) {
					// do an DESC sort : compare s2 to s1 then
					return Long.compare(s2.record.getDuration(), s1.record.getDuration());
				}
			});
			// logger.info("--------------top10informations:");
			final ArrayList<HashMap<String, Object>> top10Synthesis = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < 10; i++) {
				if (i >= top10Record.size()) {
					break;
				}

				// logger.info(" " + i + " duration[" +
				// top10Record.get(i).record.getDuration() + "] record[" +
				// top10Record.get(i).getHashMap() + "]");

				top10Synthesis.add(top10Record.get(i).getHashMap());

			}

			info += "Nb records " + allInformations.size();
			result.put("info", info);

			result.put("allinformations", allInformations);

			result.put("rangeinformations", last24HoursSynthesis);
			result.put("top10informations", top10Synthesis);

			// logger.info("com.bonitasoft.custompage.longboard:top10informations="
			// + top10Synthesis);
			// logger.info("com.bonitasoft.custompage.longboard:repartitionPerConnector="
			// + repartitionPerConnector);
			// logger.info("com.bonitasoft.custompage.longboard:repartitionPerWork="
			// + repartitionPerWork);

			result.put("chartRange", TrackRangeGraph.getGraphRange("Range", rangeDisplayDuration, rangeDisplayMaximum, last24HoursStatsTracker));
			result.put("chartRepartitionConnector", TrackRangeGraph.getGraphRepartition("Connector", repartitionPerConnector));
			result.put("chartRepartitionWork", TrackRangeGraph.getGraphRepartition("Work", repartitionPerWork));
			// logger.info("com.bonitasoft.custompage.longboard:allInformation="+allInformations);
			// logger.info("com.bonitasoft.custompage.longboard:last24HoursSynthesis="+last24HoursSynthesis);
			// logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH
			// chartRange=" + result.get("chartRange"));
			// logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH
			// chartRepartitionConnector=" +
			// result.get("chartRepartitionConnector"));
			// logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH
			// chartRepartitionWork=" + result.get("chartRepartitionWork"));

			result.putAll(getServiceState(apiSession));
			result.put("information", sourceInformation);

			return result;
		} catch (final Exception e) {
			result.put("errormessage", "Error " + e.toString());
			final StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.severe("com.bonitasoft.custompage.longboard:Error=" + e.toString() + " at " + sw.toString());

		}

		return result;
	}

	public static TrackerAccess getInstance() {
		return new TrackerAccess();
	}

	/**
	 * return the rangeKey from the calendar. Range is get for 10 mn : then for
	 * example, range of "26/12/2014 12:43" is "2014-12-26 12:40"
	 *
	 * @param c
	 * @return
	 */
	private static String getRangeKeyFromCalendar(final Calendar c) {
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " + getOnTwoDigit(c.get(Calendar.HOUR_OF_DAY)) + ":" + getOnTwoDigit(c.get(Calendar.MINUTE) / 10 * 10);
	}

	private static String getRangeDateForGraphFromCalendar(final Calendar c) {
		return getOnTwoDigit(c.get(Calendar.HOUR_OF_DAY)) + ":" + getOnTwoDigit(c.get(Calendar.MINUTE) / 10 * 10);

	}

	private static String getOnTwoDigit(final int value) {
		if (value < 10) {
			return "0" + value;
		}
		return String.valueOf(value);
	}

	/**
	 * a statsTracker is use to calculate all different stats
	 */
	public static class StatsTracker {

		public String title;
		public String titleGraph;
		private final String label;
		public long nbExecution = 0;
		public long sumDurationMs = 0;
		public long maxDurationMs = 0;
		public String details;

		public StatsTracker(final String title, final String titleGraph, final String label) {
			this.title = title;
			this.titleGraph = titleGraph;
			this.label = label;
		};

		public long getAvgDuration() {
			return nbExecution == 0 ? 0 : (long) (sumDurationMs / nbExecution);
		}

		public String getLabel() {
			if (label != null) {
				return label;
			}
			String humanCPUTime = "";
			long cpuTime = sumDurationMs;
			final long nbDays = cpuTime / (1000 * 60 * 60 * 24);
			if (nbDays > 0) {
				humanCPUTime += nbDays + " days,";
				cpuTime = cpuTime - nbDays * (1000 * 60 * 60 * 24);
			}
			final long nbHours = cpuTime / (1000 * 60 * 60);
			if (nbHours > 0) {
				humanCPUTime += nbHours + " hours,";
				cpuTime = cpuTime - nbHours * 1000 * 60 * 60;
			}
			final long nbMn = cpuTime / (1000 * 60);
			if (nbMn > 0) {
				humanCPUTime += nbMn + " mn,";
				cpuTime = cpuTime - nbMn * 1000 * 60;
			}
			final long nbs = cpuTime / 1000;
			if (nbs > 0) {
				humanCPUTime += nbs + " s,";
				cpuTime = cpuTime - nbs * 1000;
			}
			humanCPUTime += cpuTime + " ms";

			return "Average:" + getAvgDuration() + ", Nb Execution:" + nbExecution + ", Total CPU time:" + humanCPUTime + " (" + sumDurationMs + " ms)";
		}

		@Override
		public String toString() {
			return "nbExecution [" + nbExecution + "] sumDuration[" + sumDurationMs + "]";
		}
	}

	private Map<Long, ProcessDefinition> cacheProcessDefinition;
	private Map<Long, BaseElement> cacheActivity;
	private Map<Long, BaseElement> cacheProcess;

	/**
	 * get the HashMap (use by JSON) from a record
	 *
	 * @param record
	 * @return
	 */

	/**
	 * @param timeTracker
	 * @return
	 */
	/*
	 * private static ConnectorTrackRecordEvents getConnectorTrackRecordEvent(
	 * TimeTrackerV2 timeTracker ) { for (FlushEventListener eventListener :
	 * timeTracker.getListEventListener()) { if
	 * (eventListener.getClass().getName().equals(
	 * ConnectorTrackRecordEvents.class.getName())) { // Ok, we know one
	 * trackerEvent are present. // ATTENTION ATTENTION ATTENTION // Each custom
	 * page has its onw class loader. So, we must be sure to register a STATIC
	 * object, else one object load from a custom page // WILL NOT be accessible
	 * by the another custom page. This is the goal of the factory return
	 * ConnectorTrackFactory.getInstance().getConnectorRecordEvent(); } } return
	 * null; }
	 */
	/*
	 * private static CollectorFlushEventListener
	 * getCollectorFlushEventListener(final TimeTrackerV2 timeTracker) { for
	 * (final FlushEventListener eventListener :
	 * timeTracker.getListEventListener()) { if
	 * (eventListener.getClass().getName().equals(CollectorFlushEventListener.
	 * class.getName())) { // Ok, we know one trackerEvent are present.
	 * 
	 * // ATTENTION ATTENTION ATTENTION // Each custom page has its onw class
	 * loader. So, we must be // sure to register a STATIC object, else one
	 * object load from a // custom page // WILL NOT be accessible by the
	 * another custom page. This is // the goal of the factory
	 * 
	 * return (CollectorFlushEventListener) eventListener; } } return null;
	 * 
	 * }
	 */
	/**
	 * generate a list of record to simulate
	 *
	 * @return
	 */
	private static List<Record> getSimulationRecords() {
		final List<Record> listRecords = new ArrayList<Record>();
		final Calendar cal = Calendar.getInstance();
		final long currentTimeMs = System.currentTimeMillis();
		final List<String> descriptions = new ArrayList<String>();
		descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
		descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
		descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
		descriptions.add("Connector ID: scripting-groovy - input parameters: {script=null, engineExecutionContext=org.bonitasoft.engine.connector.EngineExecutionContext@7c93e545, connectorApiAccessor=com.bonitasoft.engine.expression.ConnectorAPIAccessorExt@51b154e8}	");
		descriptions.add("connectorDefinitionName: sleep 14 - connectorInstanceId: 80013");
		descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
		descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
		descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
		descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
		descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
		descriptions.add("Connector ID: scripting-groovy - input parameters: {script={MSG=Calcul is done, SUM=0, FACT=1}, engineExecutionContext=org.bonitasoft.engine.connector.EngineExecutionContext@73364dcc, connectorApiAccessor=com.bonitasoft.engine.expression.ConnectorAPIAccessorExt@722dc72b}");

		final List<TimeTrackerRecords> typeswork = new ArrayList<TimeTrackerRecords>();
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE);
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT);
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT);
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_INPUT_EXPRESSIONS);
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS);
		typeswork.add(TimeTrackerRecords.EXECUTE_CONNECTOR_WORK);

		for (int i = 0; i < 24 * 60; i++) {
			cal.set(Calendar.HOUR_OF_DAY, i / 60);
			cal.set(Calendar.MINUTE, i % 60);
			if (cal.getTimeInMillis() > currentTimeMs) {
				break; // nothing AFTER the current time
			}
			long duration = (int) (Math.random() * 3000);
			if (i == 100) {
				duration = 4000;
			}
			if (i == 140) {
				duration = 4500;
			}

			final int indexTypeWork = (int) (Math.random() * typeswork.size());
			// Record(long timestamp, TimeTrackerRecords name, String
			// description, long duration)
			final Record record = new Record(cal.getTimeInMillis(), typeswork.get(indexTypeWork), descriptions.get(i % descriptions.size()), duration);

			listRecords.add(record);
		}
		logger.info("com.bonitasoft.custompage.longboard:Generate [" + listRecords.size() + "] records");
		return listRecords;
	}

	/**
	 * 
	 * @param eventListener
	 * @return
	 */
	private List<Record> readRecordFromCsvListener(CSVFlushEventListener eventListener) {
		List<Record> listRecords = null;
		String status = eventListener.getStatus();
		logger.info("StatusCsv=" + eventListener.getStatus());
		// the status contains the output folder
		int pos = status.indexOf("outputFolder:");
		if (pos == -1) {
			sourceInformation += "Status does not declare the outputFolder[" + status + "]: can't retrieve files;";
			return listRecords;
		}
		String outputFolder = status.substring(pos + "outputFolder:".length());
		// remove the last \n
		outputFolder = outputFolder.replace('\n', ' ').trim();
		File file = getDayFile(System.currentTimeMillis(), outputFolder, CSVFlushEventListener.FILE_PREFIX, CSVFlushEventListener.FILE_SUFFIX);
		// the file may not exist : this is not an error then
		listRecords = new ArrayList<Record>();
		if (!file.exists()) {
			sourceInformation += "No file[" + file.getAbsolutePath() + "]";
			return listRecords;
		}
		sourceInformation += "file[" + file.getAbsolutePath() + " ("+( file.length()/(1024))+" ko) ]";

		// CSV structure is
		// timestamp;year;month;dayOfMonth;hourOfDay;minute;second;millisecond;duration;name;description
		// 1511396390608;2017;11;22;16;19;50;608;0;EVALUATE_EXPRESSION;Expression:
		// SExpressionImpl [name=Table of expression containing the following
		// expressions: []., content=Table of expression containing the
		// following expressions: []., returnType=java.util.List,
		// dependencies=[], expressionKind=ExpressionKind [interpreter=NONE,
		// type=TYPE_LIST]] - dependencyValues:
		// {processDefinitionId=6625372469926929885,
		// containerType=ACTIVITY_INSTANCE, containerId=80093} - strategy:
		// org.bonitasoft.engine.expression.impl.ListExpressionExecutorStrategy@2beb3e17

		// read the file and create a list of Record
		FileReader fileReader = null;
		BufferedReader br=null;
		try {
			fileReader = new FileReader(file);
			br = new BufferedReader(new FileReader(file));
			// first, the header
			String line = br.readLine();
			String[] lineHeader = null;
			if (line != null)
				lineHeader = line.split(";");
			Record record=null;
			
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] content = line.split(";");
				// attention in the CSV, the Groovy script is display, so after
				// a record EXECUTE_CONNECTOR_CALLABLE, we may have multiple
				// line !
				
				// so, if the line does not match, this is the current line
				if (content.length != lineHeader.length)
				{
					// we have to complete the current Description record
					if (record!=null)
					{
						Record newRecord= new Record( record.getTimestamp(), record.getName(), record.getDescription()+line, record.getDuration());
						listRecords.set( listRecords.size()-1, newRecord);
					}
					continue;
				}
				// ok, this is a new line. If the completeLine is not null, create a record before

				Map<String, String> lineMap = new HashMap<String, String>();
				for (int i = 0; i < lineHeader.length; i++) {
					lineMap.put(lineHeader[i], content[i]);
				}
				try {
					TimeTrackerRecords timeTrackerName = TimeTrackerRecords.valueOf(lineMap.get("name"));
					long timeStamp = Long.valueOf(lineMap.get("timestamp"));
					long duration = Long.valueOf(lineMap.get("duration"));

					record = new Record(timeStamp, timeTrackerName, lineMap.get("description"), duration);
					listRecords.add(record);
				} catch (Exception e) {
					// we may have an exception if finaly ths line does not have
					// the correct stucture, because this is a GROVY SCRIPT
					// information
				}
				
			}
			
			
		} catch (Exception e) {
			logger.severe("Error during reading CsvFile " + e.toString());
		}
		try {
			if (br!=null)
				br.close();
		
			if (fileReader != null)
				fileReader.close();
		} catch (Exception e) {
		}
		;

		return listRecords;

	}

	/**
	 * decode the line now
	 * @param completeLine
	 * @param lineHeader
	 * @param listRecords
	 */
private void decodeLine(String completeLine, String[] lineHeader,List<Record> listRecords)
{
	
}
	/**
	 * unfortunately, this method is private in CSVFlushEventListener
	 * 
	 * @param time
	 * @param folder
	 * @param filePrefix
	 * @param fileSuffix
	 * @return
	 */
	private File getDayFile(long time, String folder, String filePrefix, String fileSuffix) {
		final StringBuilder sb = new StringBuilder();
		final GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(time);

		sb.append(getIntOnTwoNumbers(c.get(Calendar.YEAR)));
		sb.append("_");
		sb.append(getIntOnTwoNumbers(c.get(Calendar.MONTH) + 1));
		sb.append("_");
		sb.append(getIntOnTwoNumbers(c.get(Calendar.DAY_OF_MONTH)));

		final String timestamp = sb.toString();
		final String fileName = filePrefix + timestamp + fileSuffix;

		return new File(folder, fileName);
	}

	private String getIntOnTwoNumbers(final int i) {
		if (i < 10) {
			return "0" + i;
		}
		return Integer.toString(i);
	}

	public static TimeTracker getTimeTracker(APISession apiSession) throws Exception {
		TenantServiceAccessor tenantServiceAccessor = ServiceAccessorFactory.getInstance().createTenantServiceAccessor(apiSession.getTenantId());

		final TimeTracker timeTracker = tenantServiceAccessor.getTimeTracker();
		return timeTracker;
	}
}
