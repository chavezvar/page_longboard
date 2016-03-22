package com.bonitasoft.custompage.longboard.connectortracker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.TimeTrackerV2;
import org.bonitasoft.engine.tracking.collector.CollectorFlushEventListener;

// public class TimeTracker implements TenantLifecycleService {
public class TrackerAccess {

    /**
     * ----------------------------------------------------------------
     * startRecording
     *
     * @param start
     *        is true, then the recording is started.
     * @return
     */
    public static HashMap<String, Object> runService(final boolean start, final APISession apiSession) {
        final Logger logger = Logger.getLogger("org.bonitasoft");
        logger.info("com.bonitasoft.custompage.longboard:================================ Run service");

        final HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("errormessage", "");
        String message = "";
        TenantServiceAccessor tenantServiceAccessor;
        try {
            tenantServiceAccessor = ServiceAccessorFactory.getInstance().createTenantServiceAccessor(apiSession.getTenantId());

            final TimeTrackerV2 timeTracker = (TimeTrackerV2) tenantServiceAccessor.getTimeTracker();
            if (start) {
                message += "start requested;";
                // logger.info("com.bonitasoft.custompage.longboard:================================ Register listener ? ");
                if (getCollectorFlushEventListener(timeTracker) == null) {
                    logger.info("com.bonitasoft.custompage.longboard:================================ Register listener");
                    // ATTENTION ATTENTION ATTENTION
                    // Each custom page has its onw class loader. So, we must be
                    // sure to register a STATIC object, else one object load
                    // from a custom page
                    // WILL NOT be accessible by the another custom page. This
                    // is the goal of the factory
                    message += "register a CollectorFlushEventListener;";
                    timeTracker.registerListener(CollectorFlushEventListener.getInstance());
                }

                // logger.info("com.bonitasoft.custompage.longboard:================================ getEventListener");
                message += "EventListener[";
                for (final FlushEventListener eventListener : timeTracker.getListEventListener()) {
                    final String eventListenerSt = eventListener.getClass().getSimpleName();
                    message += eventListenerSt + ",";
                }
                message += "];";

                logger.info("com.bonitasoft.custompage.longboard:================================ listEvent[" + message + "]");

                if (!timeTracker.isStarted()) {
                    logger.info("com.bonitasoft.custompage.longboard:================================ Not already started, do it now");
                    message += "Ask Service Start;";
                    timeTracker.start();
                }
                result.put("flushIntervalSeconds", timeTracker.getFlushIntervalInSeconds());
                message += "Flush every" + timeTracker.getFlushIntervalInSeconds() + " s;";

            } else {

                message += "stop;";
                // timeTracker.stop();
                final CollectorFlushEventListener collectorFlushEvent = getCollectorFlushEventListener(timeTracker);
                if (collectorFlushEvent != null) {
                    message += "Tracer found, stop it";
                    timeTracker.unRegisterListener(collectorFlushEvent);
                    collectorFlushEvent.clear();
                }
                message += "EventListener[" + timeTracker.getListEventListener() + "]";

            }

            message += "ServiceState: [" + (timeTracker.isStarted() ? "Service_Started" : "Service_Stopped") + "];";
            result.putAll(getServiceState(apiSession));
            result.put("msg", message);
            logger.info("com.bonitasoft.custompage.longboard: State[" + timeTracker.isStarted() + "] msg=[" + message + "]");

        } catch (final Exception e) {
            logger.info("com.bonitasoft.custompage.longboard:================= TrackerAccess.Start : Error exception[" + e.toString() + "]");
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
        final Logger logger = Logger.getLogger("org.bonitasoft");
        logger.info("com.bonitasoft.custompage.longboard: =========================== TrackerAccess.getInfo");

        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            final TenantServiceAccessor tenantServiceAccessor = ServiceAccessorFactory.getInstance().createTenantServiceAccessor(apiSession.getTenantId());
            final Object timeTrackerObj = tenantServiceAccessor.getTimeTracker();
            if (timeTrackerObj == null)
            {
                result.put("errormessage", "No service TimeTracker: check your configuration(file cfg-bonita-time-tracker.xml)");
                logger.severe("com.bonitasoft.custompage.longboard:No service TimeTracker: check your configuration(file cfg-bonita-time-tracker.xml)");

                return result;

            }
            if (!(timeTrackerObj instanceof TimeTrackerV2))
            {
                result.put("errormessage", "TimeTracker V2 is not installed service give the class[" + timeTrackerObj.getClass().getName() + "]");
                logger.severe("TimeTracker V2 is not installed");

                return result;
            }
            final TimeTrackerV2 timeTracker = (TimeTrackerV2) timeTrackerObj;
            logger.info("com.bonitasoft.custompage.longboard:=========================== TimeTrackerV2 Service OK");

            // started IF the service is started AND the listener is present
            final boolean isStarted = timeTracker.isStarted();
            boolean isRegistered = false;

            // ConnectorTrackRecordEvents connectorTrackRecordEvents =
            // getConnectorTrackRecordEvent( timeTracker);
            final CollectorFlushEventListener collectorFlushEventListener = getCollectorFlushEventListener(timeTracker);
            if (isStarted) {
                isRegistered = collectorFlushEventListener != null;
            }
            result.put("isstarted", timeTracker.isStarted());
            result.put("isregistered", isRegistered);
            result.put("startedmsg", (timeTracker.isStarted() ? "service started, " : "service not started, ")
                    + (isRegistered ? "Events recorded" : "Events not recorded"));

            logger.info("com.bonitasoft.custompage.longboard: ===== isStarted tenant[" + apiSession.getTenantId() + "] isStarted: " + timeTracker.isStarted()
                    + "] isRegistered : " + isRegistered + "] collector["
                    + collectorFlushEventListener + "] listMess[" + timeTracker.getListEventListener() + "]");

        } catch (final Exception e) {
            result.put("errormessage", "Error " + e.toString());
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.severe("Error=" + e.toString() + " at " + sw.toString());

        }
        return result;
    }

    /**
     * getInformation on recording
     *
     * @return
     */
    public static HashMap<String, Object> getInfos(final boolean isSimulation, final boolean showAllInformations, final int rangedisplayInH,
            final APISession apiSession, final ProcessAPI processAPI) {
        final TrackerAccess trackerAccess = getInstance();
        return trackerAccess.getInternalInfos(isSimulation, showAllInformations, rangedisplayInH, apiSession, processAPI);
    }

    /**
     * the method is used then some cache can be use by the object
     *
     * @param isSimulation
     * @param showAllInformations
     * @param rangedisplayInH
     * @param apiSession
     * @return
     */
    private HashMap<String, Object> getInternalInfos(final boolean isSimulation, final boolean showAllInformations, final int rangedisplayInH,
            final APISession apiSession, final ProcessAPI processAPI) {
        final Logger logger = Logger.getLogger("org.bonitasoft");
        logger.info("com.bonitasoft.custompage.longboard:=========================== TrackerAccess.getInfo isSimulation[" + isSimulation
                + "] showallinformation[" + showAllInformations + "] rangedisplauyinH[" + rangedisplayInH + "]");

        String info = "";
        final HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("errormessage", "");
        result.put("startedmsg", "...");
        try {
            cacheProcessDefinition = new HashMap<String, ProcessDefinition>();
            final TenantServiceAccessor tenantServiceAccessor = ServiceAccessorFactory.getInstance().createTenantServiceAccessor(apiSession.getTenantId());

            List<Record> listRecords;
            if (isSimulation) {
                listRecords = getSimulationRecords();
            } else {
                final Object timeTrackerObj = tenantServiceAccessor.getTimeTracker();

                if (timeTrackerObj instanceof TimeTrackerV2) {
                    final TimeTrackerV2 timeTracker = (TimeTrackerV2) timeTrackerObj;
                    logger.info("com.bonitasoft.custompage.longboard:=========================== TimeTrackerV2 Service OK");
                    result.putAll(getServiceState(apiSession));

                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd HH:MM:ss");
                    final Date lastExecution = timeTracker.getLastFlushExecutionTime();
                    info += "Flush every " + timeTracker.getFlushIntervalInSeconds() + " s, last flush "
                            + (lastExecution == null ? "not yet" : sdf.format(lastExecution)) + ",";
                    info += "Flush State [" + timeTracker.getFlushMsgState() + "]";

                    // ConnectorTrackRecordEvents connectorTrackRecordEvents =
                    // getConnectorTrackRecordEvent( timeTracker);
                    final CollectorFlushEventListener collectorFlushEventListener = getCollectorFlushEventListener(timeTracker);
                    logger.info("com.bonitasoft.custompage.longboard:collectorFlushEventListener[" + collectorFlushEventListener + "]");

                    listRecords = collectorFlushEventListener == null ? new ArrayList<Record>() : collectorFlushEventListener.getRecords();
                } else {
                    listRecords = new ArrayList<Record>();
                }
            }
            final List<HashMap<String, Object>> allInformations = new ArrayList<HashMap<String, Object>>();

            // put all informations only is requested

            // build an synthesis array, time 10 mn per 10 mn
            final HashMap<String, StatsTracker> lastHours = new HashMap<String, StatsTracker>();
            final ArrayList<TrackerRecordAdvance> top10Record = new ArrayList<TrackerRecordAdvance>();

            final HashMap<String, StatsTracker> repartitionPerConnector = new HashMap<String, StatsTracker>();
            final HashMap<String, StatsTracker> repartitionPerWork = new HashMap<String, StatsTracker>();

            // cReferenceStartFilter is the first date : we keep all info AFTER
            // this date.
            final Calendar cReferenceStartFilter = Calendar.getInstance();
            cReferenceStartFilter.add(Calendar.HOUR_OF_DAY, -rangedisplayInH);
            cReferenceStartFilter.set(Calendar.MINUTE, 0);
            cReferenceStartFilter.set(Calendar.SECOND, 0);
            cReferenceStartFilter.set(Calendar.MILLISECOND, 0);
            // logger.info("com.bonitasoft.custompage.longboard:DateReferenceFilter =" + cReferenceStartFilter.toString());

            for (final Record record : listRecords) {
                if (record.getTimestamp() < cReferenceStartFilter.getTimeInMillis())
                {
                    continue; // too old
                }

                final TrackerRecordAdvance recordAdvance = TrackerRecordAdvance.getFromRecord(record, cacheProcessDefinition, processAPI);
                /*
                 * logger.info("--------------RECORD ADVANCE:");
                 * for (final String key : recordAdvance.getHashMap().keySet()) {
                 * logger.info("             Key[" + key + "]=[" + recordAdvance.getHashMap().get(key) + "]");
                 * }
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
                    statsTracker.details = recordAdvance.connectorDefinitionName + " task:" + recordAdvance.connectorInstanceId
                            + " in " + recordAdvance.processDefinitionName + "("
                            + recordAdvance.processDefinitionVersion + ")";
                }

                lastHours.put(key, statsTracker);

                // logger.info("Trace record "+record.getTimestamp()+" key="+key+" lastHours.get(key) exist ? "+lastHours.get(key)+"] duration="+record.getDuration()+" statsTracker.sumDuration="+statsTracker.sumDuration);
                // logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ recordAdvance.connectorDefinitionName=[" + recordAdvance.connectorDefinitionName    + "] duration[" + recordAdvance.record.getDuration() + "]");

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

                // logger.info("KEEP Record ["+cRecord.toString()+"] KeyRange="+
                // key+" StatsTraker["+statsTracker+"]");

            }
            // logger.info("StatsTracker debug="+lastHours);

            // create a synthesis per RANGE
            // sow, we start now for the reference start, and we get all
            // different information
            final Calendar cRangePerRange = (Calendar) cReferenceStartFilter.clone();
            final ArrayList<HashMap<String, Object>> last24HoursSynthesis = new ArrayList<HashMap<String, Object>>();
            final ArrayList<StatsTracker> last24HoursStatsTracker = new ArrayList<StatsTracker>();
            for (int i = 0; i < 2 * 24 * 6; i++) {
                // don't generate nothing after the current time please
                if (cRangePerRange.getTimeInMillis() > System.currentTimeMillis()) {
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
                // logger.info("Last24h = "+key+" exist ?"+lastHours.get(key)+" : ["+oneRangeRecord.toString()+"]");
                last24HoursSynthesis.add(oneRangeRecord);
                last24HoursStatsTracker.add(statsTracker);
                cRangePerRange.add(Calendar.MINUTE, 10);

            }

            //------------------------------------------- top 10
            Collections.sort(top10Record, new Comparator<TrackerRecordAdvance>() {

                public int compare(final TrackerRecordAdvance s1, final TrackerRecordAdvance s2) {
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

                // logger.info("  " + i + " duration[" + top10Record.get(i).record.getDuration() + "] record[" + top10Record.get(i).getHashMap() + "]");

                top10Synthesis.add(top10Record.get(i).getHashMap());

            }

            info += "Nb records " + allInformations.size();
            result.put("info", info);

            if (showAllInformations) {
                //logger.info("com.bonitasoft.custompage.longboard:return all informations to the client");
                result.put("allinformations", allInformations);
            }

            result.put("rangeinformations", last24HoursSynthesis);
            result.put("top10informations", top10Synthesis);

            // logger.info("com.bonitasoft.custompage.longboard:top10informations=" + top10Synthesis);
            // logger.info("com.bonitasoft.custompage.longboard:repartitionPerConnector=" + repartitionPerConnector);
            //logger.info("com.bonitasoft.custompage.longboard:repartitionPerWork=" + repartitionPerWork);

            result.put("chartRange", TrackRangeGraph.getGraphRange("Range", last24HoursStatsTracker));
            result.put("chartRepartitionConnector", TrackRangeGraph.getGraphRepartition("Connector", repartitionPerConnector));
            result.put("chartRepartitionWork", TrackRangeGraph.getGraphRepartition("Work", repartitionPerWork));
            // logger.info("com.bonitasoft.custompage.longboard:allInformation="+allInformations);
            // logger.info("com.bonitasoft.custompage.longboard:last24HoursSynthesis="+last24HoursSynthesis);
            // logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH chartRange=" + result.get("chartRange"));
            // logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH chartRepartitionConnector=" + result.get("chartRepartitionConnector"));
            // logger.info("com.bonitasoft.custompage.longboard:>>> GRAPH chartRepartitionWork=" + result.get("chartRepartitionWork"));

            return result;
        } catch (final Exception e) {
            result.put("errormessage", "Error " + e.toString());
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe("com.bonitasoft.custompage.longboard:Error=" + e.toString() + " at " + sw.toString());

        }
        return result;
    }

    private static TrackerAccess getInstance() {
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
        return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " + getOnTwoDigit(c.get(Calendar.HOUR_OF_DAY))
                + ":"
                + getOnTwoDigit(c.get(Calendar.MINUTE) / 10 * 10);
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
            if (nbDays > 0)
            {
                humanCPUTime += nbDays + " days,";
                cpuTime = cpuTime - nbDays * (1000 * 60 * 60 * 24);
            }
            final long nbHours = cpuTime / (1000 * 60 * 60);
            if (nbHours > 0)
            {
                humanCPUTime += nbHours + " hours,";
                cpuTime = cpuTime - nbHours * 1000 * 60 * 60;
            }
            final long nbMn = cpuTime / (1000 * 60);
            if (nbMn > 0)
            {
                humanCPUTime += nbMn + " mn,";
                cpuTime = cpuTime - nbMn * 1000 * 60;
            }
            final long nbs = cpuTime / 1000;
            if (nbs > 0)
            {
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

    private HashMap<String, ProcessDefinition> cacheProcessDefinition;

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
     * trackerEvent are present.
     * // ATTENTION ATTENTION ATTENTION // Each custom page has its onw class
     * loader. So, we must be sure to register a STATIC object, else one object
     * load from a custom page // WILL NOT be accessible by the another custom
     * page. This is the goal of the factory
     * return ConnectorTrackFactory.getInstance().getConnectorRecordEvent(); } }
     * return null;
     * }
     */

    private static CollectorFlushEventListener getCollectorFlushEventListener(final TimeTrackerV2 timeTracker) {
        for (final FlushEventListener eventListener : timeTracker.getListEventListener()) {
            if (eventListener.getClass().getName().equals(CollectorFlushEventListener.class.getName())) {
                // Ok, we know one trackerEvent are present.

                // ATTENTION ATTENTION ATTENTION
                // Each custom page has its onw class loader. So, we must be
                // sure to register a STATIC object, else one object load from a
                // custom page
                // WILL NOT be accessible by the another custom page. This is
                // the goal of the factory

                return (CollectorFlushEventListener) eventListener;
            }
        }
        return null;

    }

    /**
     * generate a list of record to simulate
     *
     * @return
     */
    private static List<Record> getSimulationRecords() {
        final Logger logger = Logger.getLogger("org.bonitasoft");
        final List<Record> listRecords = new ArrayList<Record>();
        final Calendar cal = Calendar.getInstance();
        final long currentTimeMs = System.currentTimeMillis();
        final List<String> descriptions = new ArrayList<String>();
        descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
        descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
        descriptions.add("connectorDefinitionName: finishTheJob - connectorInstanceId: 80011");
        descriptions
                .add("Connector ID: scripting-groovy - input parameters: {script=null, engineExecutionContext=org.bonitasoft.engine.connector.EngineExecutionContext@7c93e545, connectorApiAccessor=com.bonitasoft.engine.expression.ConnectorAPIAccessorExt@51b154e8}	");
        descriptions.add("connectorDefinitionName: sleep 14 - connectorInstanceId: 80013");
        descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
        descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
        descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
        descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
        descriptions.add("connectorDefinitionName: MyFirstConnector - connectorInstanceId: 80009");
        descriptions
                .add("Connector ID: scripting-groovy - input parameters: {script={MSG=Calcul is done, SUM=0, FACT=1}, engineExecutionContext=org.bonitasoft.engine.connector.EngineExecutionContext@73364dcc, connectorApiAccessor=com.bonitasoft.engine.expression.ConnectorAPIAccessorExt@722dc72b}");

        final List<String> typeswork = new ArrayList<String>();
        typeswork.add("EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT");
        typeswork.add("EXECUTE_CONNECTOR_CALLABLE");
        typeswork.add("EXECUTE_CONNECTOR_OUTPUT_OPERATIONS");
        typeswork.add("EXECUTE_CONNECTOR_INPUT_EXPRESSIONS");
        typeswork.add("EXECUTE_CONNECTOR_INPUT_EXPRESSIONS");
        typeswork.add("EXECUTE_CONNECTOR_DISCONNECT");
        typeswork.add("EXECUTE_CONNECTOR_WORK");
        typeswork.add("EXECUTE_CONNECTOR_WORK");
        typeswork.add("EXECUTE_CONNECTOR_WORK");
        typeswork.add("EXECUTE_CONNECTOR_WORK");

        for (int i = 0; i < 24 * 60; i++) {
            cal.set(Calendar.HOUR_OF_DAY, i / 60);
            cal.set(Calendar.MINUTE, i % 60);
            if (cal.getTimeInMillis() > currentTimeMs)
            {
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
            final Record record = new Record(cal.getTimeInMillis(), typeswork.get(indexTypeWork), descriptions.get(i % descriptions.size()), duration);

            listRecords.add(record);
        }
        logger.info("com.bonitasoft.custompage.longboard:Generate [" + listRecords.size() + "] records");
        return listRecords;
    }
}
