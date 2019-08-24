package com.bonitasoft.custompage.longboard.casehistory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bdm.BusinessObjectDAOFactory;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.json.simple.JSONValue;

import com.bonitasoft.custompage.longboard.casehistory.CaseGraphDisplay.ActivityTimeLine;
import com.bonitasoft.custompage.longboard.casehistory.cmdtimer.CmdGetTimer;
import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;

import groovy.json.JsonBuilder;

public class CaseHistory {

    final static Logger logger = Logger.getLogger(CaseHistory.class.getName());

    public static String loggerLabel = "LongBoard ##";
    private final static BEvent eventNoCaseFound = new BEvent(CaseHistory.class.getName(), 1, Level.APPLICATIONERROR, "No Case found", "The Case does not exist (or it's a SubProcess case)", "No information will be visible", "Give a correct case");

    public final static String cstStatus = "status";
    public final static String cstActivityName = "activityName";
    public final static String cstActivityDescription = "description";
    public final static String cstActivityDisplayDescription = "displaydescription";

    public final static String cstPerimeter = "perimeter";
    public final static String cstPerimeter_V_ACTIVE = "ACTIVE";
    public final static String cstPerimeter_V_ARCHIVED = "ARCHIVED";

    public final static String cstActivityId = "activityId";
    public final static String cstActivitySourceId = "activitySourceId";
    public final static String cstActivityIdDesc = "activityIdDesc";
    public final static String cstTriggerId = "triggerid";
    public final static String cstJobName = "jobName";
    public final static String cstActivityFlownodeDefId = "FlownodeDefId";
    public final static String cstActivityType = "type";
    public final static String cstActivityState = "state";
    public final static String cstActivityDate = "activityDate";
    public final static String cstActivityDateHuman = "humanActivityDateSt";
    public final static String cstActivitySourceObjectId = "SourceObjectId";
    public final static String cstActivityTimerDate = "timerDate";
    public final static String cstActivityParentContainer = "parentcontainer";
    public final static String cstActivityExpl = "expl";

    public final static String cstActivityDateBegin = "dateBegin";
    public final static String cstActivityDateBeginHuman = "dateBeginSt";

    public final static String cstActivityDateEnd = "dateEnd";
    public final static String cstActivityDateEndHuman = "dateEndSt";

    public final static String cstActivityIsTerminal = "isTerminal";
    public final static String cstActivityJobIsStillSchedule = "jobIsStillSchedule";
    public final static String cstActivityJobScheduleDate = "jobScheduleDate";

    // message
    public final static String cstActivityMessageName = "messageName";
    public final static String cstActivityMessageCorrelationList = "correlations";
    public final static String cstActivityMessageContentList = "contents";

    public final static String cstActivityMessageVarName = "msgVarName";
    public final static String cstActivityMessageVarValue = "msgVarValue";

    public final static String cstActivityCorrelationDefinition = "corrDefinition";

    // signal
    public final static String cstActivitySignalName = "signalName";

    public final static String cstCaseId = "caseId";
    public final static String cstRootCaseId = "rootCaseId";
    public final static String cstRealCaseId = "realCaseId";
    public final static String cstCaseProcessInfo = "processInfo";
    public final static String cstCaseStartDateSt = "startDateSt";

    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    public static String getPing() {
        return "PING:" + System.currentTimeMillis();
    }

    private static class TimeCollect {

        public Long activitySourceObjectId;
        public Long activityId;
        public String activityType;

        // a Task has the following markup
        // timeEntry :
        // timeAvailable : Available For User (all Input Connector ran) :
        // timeUserExecute : User submit the task :
        // timeFinish Complete (all end connector ran) :

        // USER TASK
        // timeEntry initializing.reachedStateDate or initializing.archivedDate
        // API: YES
        // timeAvailable ready.reachedStateDate API : No
        // timeUserExecute ready.archivedDate API : YES
        // timeFinish Completed.archivedDate API : YES
        // ==> No way to calculated the time of input connector or the time the
        // task is waiting

        // Service TASK
        // timeEntry initializing.archivedDate API: YES
        // timeAvailable API : No
        // timeUserExecute API : No
        // timeFinish Completed.archivedDate API : YES
        // ==> No way to calculated the time of input connector or Wait
        // Connector

        public Long timeEntry;
        public Long timeAvailable;
        public Long timeUserExecute;
        public Long timeFinish;

        @Override
        public String toString() {
            return activityId + ": timeEntry(" + timeEntry + ") available(" + timeAvailable + ") userExecute("
                    + timeUserExecute + ") complete(" + timeFinish + ")";
        }

    }

    public static class CaseHistoryParameter {

        public Long caseId;
        public boolean showSubProcess = false;
        public boolean showArchivedData;
        public String searchIndex1;
        public String searchIndex2;
        public String searchIndex3;
        public String searchIndex4;
        public String searchIndex5;

        public static CaseHistoryParameter getInstanceFromJson(String jsonSt) {
            CaseHistoryParameter caseHistoryParameter = new CaseHistoryParameter();
            if (jsonSt == null)
                return caseHistoryParameter;

            final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);

            caseHistoryParameter.caseId = LongboardToolbox.jsonToLong(jsonHash.get("caseId"), null);
            caseHistoryParameter.searchIndex1 = LongboardToolbox.jsonToString(jsonHash.get("search1"), "");
            caseHistoryParameter.searchIndex2 = LongboardToolbox.jsonToString(jsonHash.get("search2"), "");
            caseHistoryParameter.searchIndex3 = LongboardToolbox.jsonToString(jsonHash.get("search3"), "");
            caseHistoryParameter.searchIndex4 = LongboardToolbox.jsonToString(jsonHash.get("search4"), "");
            caseHistoryParameter.searchIndex5 = LongboardToolbox.jsonToString(jsonHash.get("search5"), "");
            caseHistoryParameter.showSubProcess = LongboardToolbox.jsonToBoolean(jsonHash.get("showSubProcess"), false);
            caseHistoryParameter.showArchivedData = LongboardToolbox.jsonToBoolean(jsonHash.get("showArchivedData"),
                    false);
            return caseHistoryParameter;
        }

    }

    /**
     * ----------------------------------------------------------------
     * getCaseDetails
     * 
     * @return
     */
    public static Map<String, Object> getCaseDetails(CaseHistoryParameter caseHistoryParameter,
            boolean forceDeployCommand, final InputStream inputStreamJarFile, APISession apiSession) {

        // Activities
        logger.info("############### start caseDetail v4.0 on [" + caseHistoryParameter.caseId + "] ShowSubProcess["
                + caseHistoryParameter.showSubProcess + "]");

        final Map<String, Object> caseDetails = new HashMap<String, Object>();
        caseDetails.put("errormessage", "");
        try {

            SearchOptionsBuilder searchOptionsBuilder;
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
            final BusinessDataAPI businessDataAPI = TenantAPIAccessor.getBusinessDataAPI(apiSession);
            final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);

            if (caseHistoryParameter.caseId == null) {
                caseDetails.put("errormessage", "Give a caseId");
                return caseDetails;
            }

            ProcessInstanceList loadAllprocessInstances = loadProcessInstances(caseHistoryParameter.caseId, caseHistoryParameter.showSubProcess, processAPI);

            if (loadAllprocessInstances.listIds.size() == 0) {
                caseDetails.put("errormessage", "No root case Id with this ID");
            }
            // ---------------------------- Activities
            final List<Map<String, Object>> listActivities = new ArrayList<Map<String, Object>>();
            // keep the list of FlownodeId returned: the event should return the
            // same ID and then it's necessary to merge them
            final Map<Long, Map<String, Object>> mapActivities = new HashMap<Long, Map<String, Object>>();

            final List<Map<String, Object>> listActivitiesActives = new ArrayList<Map<String, Object>>();

            // multi instance task : if the task is declare as a multi instance,
            // it considere as finish ONLY when we see the
            // MULTI_INSTANCE_ACTIVITY / completed
            final Set<Long> listMultiInstanceActivity = new HashSet<Long>();

            // Active tasks
            searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
            // searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID,
            // processInstanceId);

            if (caseHistoryParameter.showSubProcess) {
                searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID,
                        caseHistoryParameter.caseId);
            } else {
                searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                        caseHistoryParameter.caseId);
            }

            // SearchResult<ActivityInstance> searchActivity =
            // processAPI.searchActivities(searchOptionsBuilder.done());
            final SearchResult<FlowNodeInstance> searchFlowNode = processAPI.searchFlowNodeInstances(searchOptionsBuilder.done());
            if (searchFlowNode.getCount() == 0) {
                // caseDetails.put("errormessage", "No activities found");
            }

            for (final FlowNodeInstance activityInstance : searchFlowNode.getResult()) {
                final HashMap<String, Object> mapActivity = new HashMap<String, Object>();

                mapActivity.put(cstPerimeter, cstPerimeter_V_ACTIVE);

                mapActivity.put(cstActivityName, activityInstance.getName());
                mapActivity.put(cstActivityId, activityInstance.getId());
                mapActivity.put(cstActivityIdDesc, activityInstance.getId());
                mapActivity.put(cstActivityDescription, activityInstance.getDescription());
                mapActivity.put(cstActivityDisplayDescription, activityInstance.getDisplayDescription());

                Date date = activityInstance.getLastUpdateDate();
                if (activityInstance instanceof GatewayInstance) {
                    // an active gatewaty does not have any date...
                    date = null;
                }

                logger.info("##### FLOWNODE Activity[" + activityInstance.getName() + "] Class["
                        + activityInstance.getClass().getName() + "]");
                if (date != null) {
                    mapActivity.put(cstActivityDate, date.getTime());
                    mapActivity.put(cstActivityDateHuman, getDisplayDate(date));
                }
                // mapActivity.put("isterminal",
                // activityInstance.().toString());
                mapActivity.put(cstActivityType, activityInstance.getType().toString());
                mapActivity.put(cstActivityState, activityInstance.getState().toString());
                mapActivity.put(cstActivityFlownodeDefId, activityInstance.getFlownodeDefinitionId());
                mapActivity.put(cstActivityParentContainer, activityInstance.getParentContainerId());

                if ("MULTI_INSTANCE_ACTIVITY".equals(activityInstance.getType().toString())) {
                    listMultiInstanceActivity.add(activityInstance.getFlownodeDefinitionId());
                }
                mapActivity.put(cstActivityExpl,
                        "FlowNode :" + activityInstance.getFlownodeDefinitionId() + "] ParentContainer["
                                + activityInstance.getParentContainerId() + "] RootContainer["
                                + activityInstance.getRootContainerId() + "]");

                if (activityInstance.getExecutedBy() != 0) {
                    try {
                        final User user = identityAPI.getUser(activityInstance.getExecutedBy());
                        final String userExecuted = (user != null ? user.getUserName() : "unknow") + " ("
                                + activityInstance.getExecutedBy() + ")";
                        mapActivity.put("ExecutedBy", userExecuted);
                    } catch (final UserNotFoundException ue) {
                        mapActivity.put("ExecutedBy", "UserNotFound id=" + activityInstance.getExecutedBy());
                    } ;

                }
                logger.info("#### casehistory [" + mapActivity + "]");

                listActivities.add(mapActivity);
                mapActivities.put(activityInstance.getId(), mapActivity);
            }
            logger.info("#### casehistory on processInstanceId[" + caseHistoryParameter.caseId + "] : found ["
                    + listActivities.size() + "] activity");

            // ------------------- archived   
            // Attention, same activity will be returned multiple time
            Set<Long> setActivitiesRetrieved = new HashSet<Long>();
            for (Long processInstanceId : loadAllprocessInstances.listIds) {

                searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
                if (caseHistoryParameter.showSubProcess) {
                    searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                            processInstanceId);
                    // bug : not working
                    // searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID,
                    // caseHistoryParameter.caseId);
                } else {
                    searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                            processInstanceId);
                }

                final SearchResult<ArchivedFlowNodeInstance> searchActivityArchived;
                searchActivityArchived = processAPI.searchArchivedFlowNodeInstances(searchOptionsBuilder.done());
                for (final ArchivedFlowNodeInstance activityInstance : searchActivityArchived.getResult()) {
                    if (setActivitiesRetrieved.contains(activityInstance.getId()))
                        continue;
                    setActivitiesRetrieved.add(activityInstance.getId());

                    final HashMap<String, Object> mapActivity = new HashMap<String, Object>();
                    mapActivity.put(cstPerimeter, cstPerimeter_V_ARCHIVED);
                    mapActivity.put(cstActivityName, activityInstance.getName());
                    mapActivity.put(cstActivityId, activityInstance.getId());
                    mapActivity.put(cstActivitySourceId, activityInstance.getSourceObjectId());
                    mapActivity.put(cstActivityIdDesc, activityInstance.getSourceObjectId() + " ( " + activityInstance.getId() + ")");
                    mapActivity.put(cstActivityDescription, activityInstance.getDescription());
                    mapActivity.put(cstActivityDisplayDescription, activityInstance.getDisplayDescription());

                    final Date date = activityInstance.getArchiveDate();
                    mapActivity.put(cstActivityDate, date.getTime());
                    mapActivity.put(cstActivityDateHuman, getDisplayDate(date));
                    mapActivity.put(cstActivityIsTerminal, activityInstance.isTerminal() ? "Terminal" : "");
                    mapActivity.put(cstActivityType, activityInstance.getType().toString());
                    mapActivity.put(cstActivityState, activityInstance.getState().toString());
                    mapActivity.put(cstActivityFlownodeDefId, activityInstance.getFlownodeDefinitionId());
                    mapActivity.put("parentactivityid", activityInstance.getParentActivityInstanceId());
                    mapActivity.put(cstActivityParentContainer, activityInstance.getParentContainerId());
                    mapActivity.put(cstActivitySourceObjectId, activityInstance.getSourceObjectId());
                    mapActivity.put(cstActivityExpl,
                            "FlowNode :" + activityInstance.getFlownodeDefinitionId() + "] ParentActivityInstanceId["
                                    + activityInstance.getParentActivityInstanceId() + "] ParentContainer["
                                    + activityInstance.getParentContainerId() + "] RootContainer["
                                    + activityInstance.getRootContainerId() + "] Source["
                                    + activityInstance.getSourceObjectId() + "]");

                    if (activityInstance.getExecutedBy() != 0) {
                        try {
                            final User user = identityAPI.getUser(activityInstance.getExecutedBy());

                            final String userExecuted = (user != null ? user.getUserName() : "unknow") + " ("
                                    + activityInstance.getExecutedBy() + ")";
                            mapActivity.put("ExecutedBy", userExecuted);
                        } catch (final UserNotFoundException ue) {
                            mapActivity.put("ExecutedBy", "UserNotFound id=" + activityInstance.getExecutedBy());
                            caseDetails.put("errormessage", "UserNotFound id=" + activityInstance.getExecutedBy());

                        } ;
                    }
                    logger.info("#### casehistory Activity[" + mapActivity + "]");

                    listActivities.add(mapActivity);
                    mapActivities.put(activityInstance.getId(), mapActivity);

                }
            }
            // ------------------------------ events
            List<Map<String, Object>> listSignals = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> listMessages = new ArrayList<Map<String, Object>>();
            for (Long processInstanceId : loadAllprocessInstances.listIds) {
                final List<EventInstance> listEventInstance = processAPI.getEventInstances(processInstanceId, 0,
                        1000, EventCriterion.NAME_ASC);
                for (final EventInstance eventInstance : listEventInstance) {
                    Map<String, Object> mapActivity = null;
                    if (mapActivities.containsKey(eventInstance.getId()))
                        mapActivity = mapActivities.get(eventInstance.getId());
                    else {
                        mapActivity = new HashMap<String, Object>();
                        listActivities.add(mapActivity);
                        mapActivity.put(cstPerimeter, "ARCHIVED");
                        mapActivity.put(cstActivityName, eventInstance.getName());
                        mapActivity.put(cstActivityId, eventInstance.getId());
                        mapActivity.put(cstActivityIdDesc, eventInstance.getId());

                        mapActivity.put(cstActivityDescription, eventInstance.getDescription());
                        mapActivity.put(cstActivityDisplayDescription, eventInstance.getDisplayDescription());
                        mapActivity.put(cstActivityIsTerminal, "");

                    }

                    Date date = eventInstance.getLastUpdateDate();
                    if (date != null) {
                        mapActivity.put(cstActivityDate, date.getTime());
                        mapActivity.put(cstActivityDateHuman, getDisplayDate(date));
                    }
                    mapActivity.put(cstActivityType, eventInstance.getType().toString());
                    mapActivity.put(cstActivityState, eventInstance.getState().toString());
                    mapActivity.put(cstActivityFlownodeDefId, eventInstance.getFlownodeDefinitionId());
                    mapActivity.put(cstActivityParentContainer, eventInstance.getParentContainerId());

                    mapActivity.put(cstActivityExpl,
                            "EventInstance :" + eventInstance.getFlownodeDefinitionId() + "] ParentContainer["
                                    + eventInstance.getParentContainerId() + "] RootContainer["
                                    + eventInstance.getRootContainerId() + "]");

                    DesignProcessDefinition designProcessDefinition = processAPI
                            .getDesignProcessDefinition(eventInstance.getProcessDefinitionId());
                    FlowElementContainerDefinition flowElementContainerDefinition = designProcessDefinition
                            .getFlowElementContainer();
                    FlowNodeDefinition flowNodeDefinition = flowElementContainerDefinition
                            .getFlowNode(eventInstance.getFlownodeDefinitionId());
                    if (flowNodeDefinition instanceof CatchEventDefinition) {
                        CatchEventDefinition catchEventDefinition = (CatchEventDefinition) flowNodeDefinition;
                        if (catchEventDefinition.getSignalEventTriggerDefinitions() != null) {
                            SignalOperations.collectSignals(catchEventDefinition, eventInstance, listSignals);

                        } // end signal detection
                        if (catchEventDefinition.getMessageEventTriggerDefinitions() != null) {
                            MessageOperations.collectMessage(catchEventDefinition, eventInstance, listMessages);

                        } // end message detection
                    }
                    // ActivityDefinition activityDefinition= processAPI.getDef
                    // CatchEventDefinition.getSignalEventTriggerDefinitions().getSignalName()
                }
            }
            caseDetails.put("signals", listSignals);
            caseDetails.put("messages", listMessages);

            // -------------------------------------------- search the timer
            List<Map<String, Object>> listTimers = new ArrayList<Map<String, Object>>();
            for (Long processInstanceId : loadAllprocessInstances.listIds) {

                SearchResult<TimerEventTriggerInstance> searchTimer = processAPI.searchTimerEventTriggerInstances(
                        processInstanceId, new SearchOptionsBuilder(0, 100).done());
                if (searchTimer.getResult() != null)
                    for (TimerEventTriggerInstance triggerInstance : searchTimer.getResult()) {
                        Map<String, Object> eventTimer = new HashMap<String, Object>();

                        eventTimer.put(cstActivityJobIsStillSchedule, "Yes");
                        eventTimer.put(cstTriggerId, triggerInstance.getId());
                        eventTimer.put(cstActivityId, triggerInstance.getEventInstanceId());
                        eventTimer.put(cstActivityIdDesc, triggerInstance.getEventInstanceId());
                        eventTimer.put(cstActivityName, triggerInstance.getEventInstanceName());
                        eventTimer.put(cstActivityTimerDate, triggerInstance.getExecutionDate() == null ? ""
                                : sdf.format(triggerInstance.getExecutionDate()));

                        // update the activity : a timer is still active
                        if (mapActivities.containsKey(triggerInstance.getEventInstanceId())) {
                            Map<String, Object> mapActivity = mapActivities.get(triggerInstance.getEventInstanceId());
                            mapActivity.put(cstActivityJobIsStillSchedule, "Yes");
                            mapActivity.put(cstActivityJobScheduleDate, triggerInstance.getExecutionDate() == null ? ""
                                    : sdf.format(triggerInstance.getExecutionDate()));
                            mapActivity.put(cstTriggerId, triggerInstance.getExecutionDate() == null ? ""
                                    : sdf.format(triggerInstance.getExecutionDate()));

                        }
                        listTimers.add(eventTimer);
                    }
            }
            /*
             * List<Map<String, Object>> listTimerByCommand =
             * getTimerByCommand(caseHistoryParameter.caseId, listHistoryJson,
             * forceDeployCommand, inputStreamJarFile, commandAPI); // now use
             * this listTimer to complete the list : timer in error are // not
             * in the first list ! for (Map<String, Object> eventTimerCommand :
             * listTimerByCommand) { boolean found = false; for (Map<String,
             * Object> eventTimer : listTimers) { Long actId = (Long)
             * eventTimer.get(cstActivityId); Long actCmdId = (Long)
             * eventTimerCommand.get(cstActivityId); if (actId!=null &&
             * actId.equals(actCmdId)) { eventTimer.put(cstJobName,
             * eventTimerCommand.get(cstJobName)); found = true; } } if (!found)
             * listTimers.add(eventTimerCommand); }
             */
            caseDetails.put("timers", listTimers);

            // --- set the activities now that we updated it
            sortTheList(listActivities, cstActivityDate);

            caseDetails.put("activities", listActivities);

            // -------------------------- Calcul the Active list
            Map<Long, Map<String, Object>> mapActive = new HashMap<Long, Map<String, Object>>();
            for (Map<String, Object> activity : listActivities) {
                if (cstPerimeter_V_ARCHIVED.equals(activity.get(cstPerimeter)))
                    continue;
                Long idActivity = (Long) activity.get(cstActivityId);
                mapActive.put(idActivity, activity);
            }
            final long currentTime = System.currentTimeMillis();
            // ok, now we have in Map all the last state for each activity
            for (Map<String, Object> activity : mapActive.values()) {
                listActivitiesActives.add(activity);
                if (ActivityStates.INITIALIZING_STATE.equals(activity.get(cstActivityState))) {
                    if (currentTime - ((Long) activity.get(cstActivityDate)) > 1000 * 60)
                        activity.put("ACTIONEXECUTE", true);
                }
                if (ActivityStates.READY_STATE.equals(activity.get(cstActivityState)))
                    activity.put("ACTIONEXECUTE", true);
            }
            String s = ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE;
            caseDetails.put("actives", listActivitiesActives);
            logger.info("ACTIVE:" + listActivitiesActives.toString());

            // process instance
            caseDetails.put("processintances", loadAllprocessInstances.listDetails);

            // -------------------------------------------- Variables
            List<Map<String, Object>> listDataInstanceMap = new ArrayList<Map<String, Object>>();

            // process variables
            listDataInstanceMap.addAll(loadProcessVariables(caseHistoryParameter.caseId, caseHistoryParameter.showSubProcess, mapActivities, processAPI));
            listDataInstanceMap.addAll(loadBdmVariables(caseHistoryParameter.caseId, caseHistoryParameter.showSubProcess, apiSession, businessDataAPI, processAPI));

            sortTheList(listDataInstanceMap, "processinstance;name;datearchived");

            caseDetails.put("variables", listDataInstanceMap);

            // -------------------------------------------- Documents
            List<Map<String, Object>> listDocumentsMap = new ArrayList<Map<String, Object>>();

            List<ProcessInstanceDescription> listProcessInstances = getAllProcessInstance(caseHistoryParameter.caseId,
                    caseHistoryParameter.showSubProcess, processAPI);

            for (ProcessInstanceDescription processInstanceDescription : listProcessInstances) {
                List<Document> listDocuments = processAPI.getLastVersionOfDocuments(processInstanceDescription.id, 0, 1000,
                        DocumentCriterion.NAME_ASC);
                if (listDocuments != null) {
                    for (Document document : listDocuments) {
                        Map<String, Object> documentMap = new HashMap<String, Object>();
                        listDocumentsMap.add(documentMap);

                        ProcessDefinition processDefinition = processAPI.getProcessDefinition(processInstanceDescription.processDefinitionId);

                        documentMap.put("processname", processDefinition.getName());
                        documentMap.put("processversion", processDefinition.getVersion());
                        documentMap.put("processinstance", processInstanceDescription.id);

                        documentMap.put("name", document.getName());
                        documentMap.put("id", document.getId());
                        documentMap.put("hascontent", document.hasContent());
                        documentMap.put("contentstorageid", document.getContentStorageId());
                        documentMap.put("url", document.getUrl());
                        documentMap.put("contentfilename", document.getContentFileName());
                        documentMap.put("contentmimetype", document.getContentMimeType());
                        documentMap.put("docindex", Integer.valueOf(document.getIndex()));
                        documentMap.put("creationdate",
                                document.getCreationDate() == null ? "" : sdf.format(document.getCreationDate()));
                    }
                }
            }
            sortTheList(listDocumentsMap, "processinstance;name;docindex");
            caseDetails.put("documents", listDocumentsMap);

            // ---------------------------------- Synthesis
            final Map<Long, Map<String, Object>> mapSynthesis = new HashMap<Long, Map<String, Object>>();
            for (final Map<String, Object> mapActivity : listActivities) {

                final Long flowNodedefid = Long.valueOf((Long) mapActivity.get(cstActivityFlownodeDefId));
                if (mapSynthesis.get(flowNodedefid) != null) {
                    continue; // already analysis
                }
                final String type = (String) mapActivity.get(cstActivityType);
                if ("BOUNDARY_EVENT".equals(type)) {
                    continue; // don't keep this kind of activity
                }

                // analysis this one !
                final HashMap<String, Object> oneSynthesisLine = new HashMap<String, Object>();
                mapSynthesis.put(flowNodedefid, oneSynthesisLine);

                oneSynthesisLine.put(cstActivityName, mapActivity.get(cstActivityName));
                oneSynthesisLine.put(cstActivityType, mapActivity.get(cstActivityType));

                String expl = "";
                boolean isReady = false;
                boolean isInitializing = false;
                boolean isExecuting = false;
                boolean isCancelled = false;
                boolean isCompleted = false;
                boolean isReallyTerminated = false;
                boolean isFailed = false;

                // in case of a simple task, there are only one record. In case
                // of MultInstance, there are one per instance
                final HashMap<Long, TimeCollect> timeCollectPerSource = new HashMap<Long, TimeCollect>();
                // calculate the line : check in the list all related event
                // to make the relation, the SOURCE (
                // activityInstance.getSourceObjectId()) is necessary.
                // in case of a multi instance, we will have multiple
                // initializing / executing but with different source :
                // Instance 1 : initializing source="344"
                // instance 2 : initializing source ="345"
                // Instance 1 executing : source ="344"
                // Instance 2 executing : source ="345"
                // then we collect the time per source

                // ------------------- sub loop
                for (final Map<String, Object> mapRunActivity : listActivities) {
                    if (!mapRunActivity.get(cstActivityFlownodeDefId).equals(flowNodedefid)) {
                        continue;
                    }

                    expl += "Found state[" + mapRunActivity.get(cstActivityState) + "]";
                    Long key = (Long) mapRunActivity.get(cstActivitySourceObjectId);
                    if (key == null) {
                        key = (Long) mapRunActivity.get(cstActivityId);
                    }
                    TimeCollect timeCollect = timeCollectPerSource.get(key);

                    if (timeCollect == null) {
                        timeCollect = new TimeCollect();
                        timeCollect.activitySourceObjectId = (Long) mapRunActivity.get(cstActivitySourceObjectId);
                        timeCollect.activityId = (Long) mapRunActivity.get(cstActivityId);
                        timeCollect.activityType = (String) mapRunActivity.get(cstActivityType);
                        timeCollectPerSource.put(key, timeCollect);
                    }

                    // min and max
                    Long timeActivity = (Long) mapRunActivity.get(cstActivityDate);
                    if (timeActivity != null) {
                        if (timeCollect.timeEntry == null || (timeActivity < timeCollect.timeEntry))
                            timeCollect.timeEntry = timeActivity;
                        if (timeCollect.timeFinish == null || (timeActivity > timeCollect.timeFinish))
                            timeCollect.timeFinish = timeActivity;
                    }

                    if ("initializing".equals(mapRunActivity.get(cstActivityState))
                            || "executing".equals(mapRunActivity.get(cstActivityState))) {
                        // attention : multiple initializing or executing,
                        // specialy in a Call Activity. get the min !
                        // Long timeSynthesis = (Long)
                        // oneSynthesisLine.get(cstActivityDateBegin);
                        logger.info("##### Synthesis Init activity[" + oneSynthesisLine.get(cstActivityName) + " "
                                + timeCollect.toString());
                    }

                    if ("ready".equals(mapRunActivity.get(cstActivityState))) {
                        timeCollect.timeUserExecute = (Long) mapRunActivity.get(cstActivityDate);
                        isReady = true;
                    }
                    if ("failed".equals(mapRunActivity.get(cstActivityState))) {
                        isFailed = true;

                    }
                    if (("completed".equals(mapRunActivity.get(cstActivityState))
                            || "cancelled".equals(mapRunActivity.get(cstActivityState)))
                            && mapRunActivity.get(cstActivityDate) instanceof Long) {
                        isReallyTerminated = true;
                        // attention ! if the task is a MULTI The task is
                        // considere
                        if (listMultiInstanceActivity.contains(flowNodedefid)) {
                            if ("MULTI_INSTANCE_ACTIVITY".equals(mapRunActivity.get(cstActivityType))) {
                                isReallyTerminated = true;
                            } else {
                                isReallyTerminated = false;
                            }
                        }
                    }

                    if (ActivityStates.INITIALIZING_STATE.equals(mapRunActivity.get(cstActivityState))) {
                        isInitializing = true;
                    }
                    if (ActivityStates.EXECUTING_STATE.equals(mapRunActivity.get(cstActivityState))) {
                        isExecuting = true;
                    }
                    if (ActivityStates.READY_STATE.equals(mapRunActivity.get(cstActivityState))) {
                        isReady = true;
                    }
                    if (ActivityStates.COMPLETED_STATE.equals(mapRunActivity.get(cstActivityState))
                            && isReallyTerminated) {
                        isCompleted = true;
                    }
                    if (ActivityStates.CANCELLED_STATE.equals(mapRunActivity.get(cstActivityState))
                            && isReallyTerminated) {
                        isCancelled = true;
                    }

                } // end run sub activity lool
                  // build the activity synthesis
                long mintimeInitial = -1;
                long maxtimeComplete = -1;
                long sumTimeEnterConnector = -1; // a marker
                long sumTimeWaitUser = -1; // a marker
                long sumTimeFinishConnector = 0;
                for (final TimeCollect timeCollect : timeCollectPerSource.values()) {
                    if (timeCollect.timeEntry != null) {
                        if (mintimeInitial == -1 || timeCollect.timeEntry < mintimeInitial) {
                            mintimeInitial = timeCollect.timeEntry;
                        }
                    }
                    if (timeCollect.timeFinish != null) {
                        if (maxtimeComplete == -1 || timeCollect.timeFinish > maxtimeComplete) {
                            maxtimeComplete = timeCollect.timeFinish;
                            // automatic task : we have only a timeInitial and a
                            // timeComplete
                        }
                    }

                    // USER TASK
                    // timeEntry initializing.reachedStateDate or
                    // initializing.archivedDate API: YES
                    // timeAvailable ready.reachedStateDate API : No
                    // timeUserExecute ready.archivedDate API : YES
                    // timeFinish Completed.archivedDate API : YES
                    // ==> No way to calculated the time of input connector or
                    // the time the task is waiting

                    // Service TASK
                    // timeEntry initializing.archivedDate API: YES
                    // timeAvailable API : No
                    // timeUserExecute API : No
                    // timeFinish Completed.archivedDate API : YES

                    if (timeCollect.timeAvailable == null) {
                        timeCollect.timeAvailable = timeCollect.timeEntry;
                    }

                    if (timeCollect.timeUserExecute == null) {
                        timeCollect.timeUserExecute = timeCollect.timeAvailable;
                    }

                    // multi instance is not part of the sum calculation
                    if ("MULTI_INSTANCE_ACTIVITY".equals(timeCollect.activityType)) {
                        continue;
                    }
                    if (timeCollect.timeEntry != null && timeCollect.timeAvailable != null) {
                        if (sumTimeEnterConnector == -1) {
                            sumTimeEnterConnector = 0;
                        }
                        sumTimeEnterConnector += timeCollect.timeAvailable - timeCollect.timeEntry;
                    }

                    if (timeCollect.timeUserExecute != null && timeCollect.timeAvailable != null) {
                        if (sumTimeWaitUser == -1) {
                            sumTimeWaitUser = 0;
                        }
                        sumTimeWaitUser += timeCollect.timeUserExecute - timeCollect.timeAvailable;
                    }
                    if (timeCollect.timeFinish != null && timeCollect.timeUserExecute != null) {
                        if (sumTimeFinishConnector == -1) {
                            sumTimeFinishConnector = 0;
                        }
                        sumTimeFinishConnector += timeCollect.timeFinish - timeCollect.timeUserExecute;
                    }
                    // todo register connector time
                    /*
                     * if (activityRegisterInConnector.contains(timeCollect.
                     * activityId )) { TimeConnector =
                     * connector.get(timeCollect.activityId) }
                     */
                }
                // it's possible to not have any time (an active gateway has not
                // time)
                if (mintimeInitial != -1) {
                    oneSynthesisLine.put(cstActivityDateBegin, mintimeInitial);
                    oneSynthesisLine.put(cstActivityDateBeginHuman, getDisplayDate(mintimeInitial));
                }
                if (isReallyTerminated) {
                    oneSynthesisLine.put(cstActivityDateEnd, maxtimeComplete);
                    oneSynthesisLine.put(cstActivityDateEndHuman, getDisplayDate(maxtimeComplete));
                }

                if (isInitializing) {
                    oneSynthesisLine.put(cstActivityState, "initializing");
                }
                if (isExecuting) {
                    oneSynthesisLine.put(cstActivityState, "Executing");
                }
                if (isReady) {
                    oneSynthesisLine.put(cstActivityState, "ready");
                }
                if (isFailed) {
                    oneSynthesisLine.put(cstActivityState, "failed");
                }
                if (isCompleted) {
                    oneSynthesisLine.put(cstActivityState, "completed");
                }
                if (isCancelled) {
                    oneSynthesisLine.put(cstActivityState, "cancelled");
                }
                if (isFailed) {
                    oneSynthesisLine.put(cstActivityState, "failed");
                }

                // now build the synthesis
                expl += "timeEnterConnector[" + sumTimeEnterConnector + "] timeUser[" + sumTimeWaitUser
                        + "] timeFinishConnector[" + sumTimeFinishConnector + "]";
                oneSynthesisLine.put(cstActivityExpl, expl);

                oneSynthesisLine.put("enterconnector", sumTimeEnterConnector);
                oneSynthesisLine.put("user", sumTimeWaitUser);
                // case of gateway or automatic task
                oneSynthesisLine.put("finishconnector", sumTimeFinishConnector);

                logger.info("Calcul time:" + expl);

                // onAnalysis.put("end", (timeCompleted - timeCompleted));
            }
            // Then process instance information

            final List<Map<String, Object>> listSynthesis = new ArrayList<Map<String, Object>>();

            // built the timeline

            final Date currentDate = new Date();
            final List<ActivityTimeLine> listTimeline = new ArrayList<ActivityTimeLine>();
            for (final Map<String, Object> oneSynthesisLine : mapSynthesis.values()) {
                listSynthesis.add(oneSynthesisLine);
                if (oneSynthesisLine.get(cstActivityDateBegin) == null) {
                    continue;
                }
                listTimeline
                        .add(ActivityTimeLine.getActivityTimeLine((String) oneSynthesisLine.get(cstActivityName),
                                new Date((Long) oneSynthesisLine.get(cstActivityDateBegin)),
                                oneSynthesisLine.get(cstActivityDateEnd) == null ? currentDate
                                        : new Date((Long) oneSynthesisLine.get(cstActivityDateEnd))));
            }
            // now order all by the time
            Collections.sort(listTimeline, new Comparator<ActivityTimeLine>() {

                public int compare(final ActivityTimeLine s1, final ActivityTimeLine s2) {
                    final Long d1 = s1.getDateLong();
                    final Long d2 = s2.getDateLong();
                    return d1 > d2 ? 1 : -1;
                }
            });
            // and order the list
            sortTheList(listSynthesis, cstActivityDateBegin);
            caseDetails.put("synthesis", listSynthesis);

            final String timeLineChart = CaseGraphDisplay.getActivityTimeLine("Activity", listTimeline);
            // logger.info("Return CHART>>" + timeLineChart + "<<");

            caseDetails.put("chartTimeline", timeLineChart);

            // ----------------------------------- overview
            boolean oneProcessInstanceIsFound = false;
            try {
                final ProcessInstance processInstance = processAPI.getProcessInstance(caseHistoryParameter.caseId);
                oneProcessInstanceIsFound = true;
                caseDetails.put(cstCaseId, processInstance.getId());
                caseDetails.put("caseState", "ACTIF");
                caseDetails.put(cstCaseStartDateSt,
                        processInstance.getStartDate() == null ? "" : getDisplayDate(processInstance.getStartDate()));
                caseDetails.put("endDateSt",
                        processInstance.getEndDate() == null ? "" : getDisplayDate(processInstance.getEndDate()));
                caseDetails.put("stringIndex",
                        " " + getDisplayString(processInstance.getStringIndexLabel(1)) + ":["
                                + getDisplayString(processInstance.getStringIndex1()) + "] "
                                + getDisplayString(processInstance.getStringIndexLabel(2)) + ":["
                                + getDisplayString(processInstance.getStringIndex2())
                                + "] 3:[" + getDisplayString(processInstance.getStringIndex3()) + "] 4:["
                                + getDisplayString(processInstance.getStringIndex4()) + "] 5:["
                                + getDisplayString(processInstance.getStringIndex5()) + "]");
                final ProcessDefinition processDefinition = processAPI
                        .getProcessDefinition(processInstance.getProcessDefinitionId());
                caseDetails.put(cstCaseProcessInfo,
                        processDefinition.getName() + " (" + processDefinition.getVersion() + ")");

            } catch (final ProcessInstanceNotFoundException e1) {
                logger.info("processinstance [" + caseHistoryParameter.caseId + "] not found (not active) ");

            } catch (final Exception e) {
                final StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                logger.severe("During getProcessInstance : " + e.toString() + " at " + sw.toString());
                caseDetails.put("errormessage", "Error during get case history " + e.toString());
            }

            try {

                // search by the source
                if (!oneProcessInstanceIsFound) {
                    final ArchivedProcessInstance archivedProcessInstance = processAPI
                            .getFinalArchivedProcessInstance(caseHistoryParameter.caseId);
                    logger.info(
                            "Case  [" + caseHistoryParameter.caseId + "]  found by getFinalArchivedProcessInstance ? "
                                    + (archivedProcessInstance == null ? "No" : "Yes"));
                    if (archivedProcessInstance != null) {
                        oneProcessInstanceIsFound = true;
                        caseDetails.put("caseState", "ARCHIVED");
                        caseDetails.put("caseId", archivedProcessInstance.getSourceObjectId());
                        caseDetails.put("archiveCaseId", archivedProcessInstance.getId());

                    }
                    caseDetails.put("startDateSt", archivedProcessInstance.getStartDate() == null ? ""
                            : getDisplayDate(archivedProcessInstance.getStartDate()));
                    caseDetails.put("endDateSt", archivedProcessInstance.getEndDate() == null ? ""
                            : getDisplayDate(archivedProcessInstance.getEndDate()));

                    caseDetails.put("archivedDateSt", getDisplayDate(archivedProcessInstance.getArchiveDate()));
                    final ProcessDefinition processDefinition = processAPI
                            .getProcessDefinition(archivedProcessInstance.getProcessDefinitionId());
                    caseDetails.put("processdefinition",
                            processDefinition.getName() + " (" + processDefinition.getVersion() + ")");
                }

            } catch (final ArchivedProcessInstanceNotFoundException e1) {
                logger.info("Case found by getFinalArchivedProcessInstance ? exception so not found");
            } catch (final Exception e) {
                final StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                logger.severe("During getArchivedProcessInstance : " + e.toString() + " at " + sw.toString());
                caseDetails.put("errormessage", "Error during get case history " + e.toString());

            } ;
            if (!oneProcessInstanceIsFound) {
                caseDetails.put("errormessage", "The caseId [" + caseHistoryParameter.caseId + "] does not exist");
            }

        } catch (final SearchException e1) {
            final StringWriter sw = new StringWriter();
            e1.printStackTrace(new PrintWriter(sw));

            logger.severe("Error during get CaseHistory" + e1.toString() + " at " + sw.toString());
            caseDetails.put("errormessage", "Error during get case history " + e1.toString());

        } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.severe("Error during get CaseHistory" + e.toString() + " at " + sw.toString());
            caseDetails.put("errormessage", "Error during get case history " + e.toString());

        }
        return caseDetails;
    }

    /**
     * search by index
     * 
     * @param caseHistoryParameter
     * @param processAPI
     * @return
     */
    public static Map<String, Object> getSearchByIndex(CaseHistoryParameter caseHistoryParameter,
            final ProcessAPI processAPI) {
        final Map<String, Object> searchDetails = new HashMap<String, Object>();
        searchDetails.put("errormessage", "");
        try {
            final List<Map<String, Object>> listCasesJson = new ArrayList<Map<String, Object>>();

            SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
            // searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID,
            // processInstanceId);

            if (caseHistoryParameter.searchIndex1.trim().length() > 0) {
                searchOptionsBuilder.filter(com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor.STRING_INDEX_1, caseHistoryParameter.searchIndex1);
            }
            if (caseHistoryParameter.searchIndex2.trim().length() > 0) {
                searchOptionsBuilder.filter(com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor.STRING_INDEX_2, caseHistoryParameter.searchIndex2);
            }
            if (caseHistoryParameter.searchIndex3.trim().length() > 0) {
                searchOptionsBuilder.filter(com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor.STRING_INDEX_3, caseHistoryParameter.searchIndex3);
            }
            if (caseHistoryParameter.searchIndex4.trim().length() > 0) {
                searchOptionsBuilder.filter(com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor.STRING_INDEX_4, caseHistoryParameter.searchIndex4);
            }
            if (caseHistoryParameter.searchIndex5.trim().length() > 0) {
                searchOptionsBuilder.filter(com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor.STRING_INDEX_5, caseHistoryParameter.searchIndex5);
            }

            // SearchResult<ActivityInstance> searchActivity =
            // processAPI.searchActivities(searchOptionsBuilder.done());
            final SearchResult<ProcessInstance> searchProcessInstance = processAPI.searchProcessInstances(searchOptionsBuilder.done());
            searchDetails.put("nbcases", searchProcessInstance.getCount());
            if (searchProcessInstance.getCount() == 0) {
                // caseDetails.put("errormessage", "No activities found");
            }

            for (final ProcessInstance processInstance : searchProcessInstance.getResult()) {
                final HashMap<String, Object> mapCase = new HashMap<String, Object>();
                listCasesJson.add(mapCase);
                mapCase.put(cstCaseId, processInstance.getId());
                final ProcessDefinition processDefinition = processAPI.getProcessDefinition(processInstance.getProcessDefinitionId());
                mapCase.put(cstCaseProcessInfo, processDefinition.getName() + " (" + processDefinition.getVersion() + ")");
                mapCase.put(cstCaseStartDateSt, processInstance.getStartDate() == null ? "" : getDisplayDate(processInstance.getStartDate()));
                mapCase.put("index1", processInstance.getStringIndex1());
                mapCase.put("index2", processInstance.getStringIndex2());
                mapCase.put("index3", processInstance.getStringIndex3());
                mapCase.put("index4", processInstance.getStringIndex4());
                mapCase.put("index5", processInstance.getStringIndex5());
                mapCase.put(cstRealCaseId, processInstance.getId());
                if (processInstance.getId() != processInstance.getRootProcessInstanceId()) {
                    mapCase.put(cstRootCaseId, processInstance.getRootProcessInstanceId());
                    mapCase.put(cstRealCaseId, processInstance.getRootProcessInstanceId());

                }
            }
            searchDetails.put("cases", listCasesJson);
        } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.severe("Error during get CaseHistory" + e.toString() + " at " + sw.toString());
            searchDetails.put("errormessage", "Error during get case history " + e.toString());

        }

        return searchDetails;
    }

    private static String getDisplayDate(final Object dateObj) {
        if (dateObj == null) {
            return "";
        }
        if (dateObj instanceof Long) {
            return sdf.format(new Date((Long) dateObj)); // +"("+dateObj+")";
        }
        if (dateObj instanceof Date) {
            return sdf.format((Date) dateObj); // +"("+ (
                                               // (Date)dateObj).getTime()+")"
                                               // ;
        }
        return "-";
    }

    /**
     * return a string every time (if null, return "")
     * 
     * @param value
     * @return
     */
    private static String getDisplayString(final String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /*
     * *************************************************************************
     * *******
     */
    /*                                                                                  */
    /* Timer per command */
    /* not needed anymore */
    /*                                                                                  */
    /*                                                                                  */
    /*
     * *************************************************************************
     * *******
     */

    /**
     * deploy a command on the server
     * 
     * @param commandName
     * @param commandDescription
     * @param className
     * @param jarFileServer
     * @param jarName
     * @param commandAPI
     * @return
     * @throws IOException
     * @throws CreationException
     * @throws AlreadyExistsException
     * @throws DeletionException
     * @throws CommandNotFoundException
     */
    private final static String commandName = "LongBoardgetTimer";
    private final static String commandDescription = "Get timer";
    private final static String className = "com.bonitasoft.custompage.longboard.casehistory.cmdtimer.CmdGetTimer";

    private static CommandDescriptor deployTimerCommand(boolean forceDeployCommand,
            final InputStream inputStreamJarFile, final String jarName, final CommandAPI commandAPI)
            throws IOException, AlreadyExistsException, CreationException, CommandNotFoundException, DeletionException {

        final List<CommandDescriptor> listCommands = commandAPI.getAllCommands(0, 1000, CommandCriterion.NAME_ASC);
        for (final CommandDescriptor commandDescriptor : listCommands) {
            if (commandName.equals(commandDescriptor.getName())) {
                if (!forceDeployCommand)
                    return commandDescriptor;

                commandAPI.unregister(commandDescriptor.getId());
            }
        }

        String message = "";
        /*
         * File commandFile = new File(jarFileServer); FileInputStream fis = new
         * FileInputStream(commandFile); byte[] fileContent = new byte[(int)
         * commandFile.length()]; fis.read(fileContent); fis.close();
         */
        final ByteArrayOutputStream fileContent = new ByteArrayOutputStream();
        final byte[] buffer = new byte[10000];
        int nbRead = 0;
        while ((nbRead = inputStreamJarFile.read(buffer)) > 0) {
            fileContent.write(buffer, 0, nbRead);
        }

        try {
            commandAPI.removeDependency(jarName);
        } catch (final Exception e) {
        } ;

        message += "Adding jarName [" + jarName + "] size[" + fileContent.size() + "]...";
        commandAPI.addDependency(jarName, fileContent.toByteArray());
        message += "Done.";

        message += "Registering...";
        final CommandDescriptor commandDescriptor = commandAPI.register(commandName, commandDescription, className);
        logger.fine(loggerLabel + "deployTimerCommand:" + message);
        return commandDescriptor;
    }

    /**
     * get the timer by the command and then update the listHistoryJson
     * 
     * @param listHistoryJson
     */
    protected static List<Map<String, Object>> getTimerByCommand(long processInstanceId,
            List<Map<String, Object>> listHistoryJson, boolean forceDeployCommand, final InputStream inputStreamJarFile,
            CommandAPI commandAPI) {
        try {

            final CommandDescriptor command = deployTimerCommand(forceDeployCommand, inputStreamJarFile,
                    "custompagelongboard", commandAPI);
            final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
            parameters.put(CmdGetTimer.cstParamProcessInstanceId, Long.valueOf(processInstanceId));
            parameters.put(CmdGetTimer.cstParamCommand, CmdGetTimer.cstCommandGetTimer);

            final Serializable resultCommand = commandAPI.execute(command.getId(), parameters);
            final HashMap<String, Object> resultCommandHashmap = (HashMap<String, Object>) resultCommand;
            if (resultCommandHashmap == null) {
                logger.info("#### Timer : Can't access the command");
                return null;
            } else {
                // caseDetails.put("TimerStatus",
                // resultCommandHashmap.get(CmdGetTimer.cstResultStatus));
                // caseDetails.put("TimerExpl",
                // resultCommandHashmap.get(CmdGetTimer.cstResultExpl));
                // complete the list by searching in all activities base on the
                // flowNodeId
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> listTimerByCommand = (List<Map<String, Object>>) resultCommandHashmap
                        .get(CmdGetTimer.cstResultListEvents);

                for (final Map<String, Object> eventTimer : listTimerByCommand) {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> hashJobParameters = (HashMap<String, Object>) eventTimer
                            .get(CmdGetTimer.cstResultEventJobParam);
                    final Long targerSflownodeDefinition = hashJobParameters == null ? null
                            : (Long) hashJobParameters.get("targetSFlowNodeDefinitionId");

                    // search if we find this activity in the list
                    if (targerSflownodeDefinition != null) {
                        for (final Map<String, Object> mapActivity : listHistoryJson) {
                            if (targerSflownodeDefinition.equals(mapActivity.get(cstActivityFlownodeDefId))) {
                                mapActivity.put(cstActivityJobIsStillSchedule, "YES");
                                eventTimer.put(cstActivityId, mapActivity.get(cstActivityId));
                                eventTimer.put(cstActivityName, mapActivity.get(cstActivityName));

                            }
                        }
                    }
                    // logger.info("#### Timer [" + eventTimer + "]");
                }
                return listTimerByCommand;

            }

        } catch (final Exception e) {

            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe("During getTimer : " + e.toString() + " at " + sw.toString());
            return null;
        }
    }

    /**
     * @param processInstanceId
     * @param timerId
     * @param newDelayInSec
     * @param forceDeployCommand
     * @param inputStreamJarFile
     * @param commandAPI
     * @return
     */
    protected static Map<String, Object> setTimerByCommand(long processInstanceId, Long timerId, Long newDelayInSec,
            boolean forceDeployCommand, final InputStream inputStreamJarFile, CommandAPI commandAPI) {
        try {

            final CommandDescriptor command = deployTimerCommand(forceDeployCommand, inputStreamJarFile,
                    "custompagelongboard", commandAPI);
            final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
            parameters.put(CmdGetTimer.cstParamProcessInstanceId, Long.valueOf(processInstanceId));
            parameters.put(CmdGetTimer.cstParamTimerId, timerId);
            parameters.put(CmdGetTimer.cstParamTimerDelayInSec, newDelayInSec);

            parameters.put(CmdGetTimer.cstParamCommand, CmdGetTimer.cstCommandSetTimer);

            final Serializable resultCommand = commandAPI.execute(command.getId(), parameters);
            @SuppressWarnings("unchecked")
            final HashMap<String, Object> resultCommandHashmap = (HashMap<String, Object>) resultCommand;
            if (resultCommandHashmap == null) {
                logger.info("#### Timer : Can't access the command");
                return null;
            } else {

                return (Map<String, Object>) resultCommandHashmap;
            }

        } catch (final Exception e) {

            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe("During getTimer : " + e.toString() + " at " + sw.toString());
            return null;
        }
    }

    /*
     * *********************************************************************
     */
    /*                                                                                  */
    /* Load sub function */
    /*    														*/
    /*                                                                                  */
    /*                                                                                  */
    /*
     * *********************************************************************
     */
    /**
     * load the processVariables
     * 
     * @param processInstanceId
     * @param processAPI
     * @return
     */
    public static List<Map<String, Object>> loadProcessVariables(Long rootProcessInstanceId, boolean showSubProcess,
            Map<Long, Map<String, Object>> mapActivities, ProcessAPI processAPI) {
        List<Map<String, Object>> listDataInstanceMap = new ArrayList<Map<String, Object>>();

        List<ProcessInstanceDescription> listProcessInstances = getAllProcessInstance(rootProcessInstanceId,
                showSubProcess, processAPI);
        for (ProcessInstanceDescription processInstanceDescription : listProcessInstances) {
            // maybe an archived ID
            try {
                ProcessDefinition processDefinition = processAPI.getProcessDefinition(processInstanceDescription.processDefinitionId);
                List<DataInstance> listDataInstances = null;
                List<Map<String, Object>> listArchivedDataInstances = null;
                List<Long> listSourceId = new ArrayList<Long>();
                listSourceId.add(processInstanceDescription.id);
                // collect each list
                if (processInstanceDescription.isActive) {
                    listDataInstances = processAPI.getProcessDataInstances(processInstanceDescription.id, 0, 1000);

                    //listArchivedDataInstances = processAPI.getArchivedProcessDataInstances(processInstanceDescription.id, 0, 1000);
                    listArchivedDataInstances = loadArchivedProcessVariables(listSourceId, processAPI);

                } else {
                    listDataInstances = new ArrayList<DataInstance>();
                    // listArchivedDataInstances = processAPI.getArchivedProcessDataInstances(processInstanceDescription.id, 0, 1000);
                    listArchivedDataInstances = loadArchivedProcessVariables(listSourceId, processAPI);
                }

                completeListDataInstanceMap(listDataInstanceMap, ScopeVariable.PROCESS, null, listDataInstances, processDefinition, processInstanceDescription.id, "");
                completeListDataInstanceMap(listDataInstanceMap, ScopeVariable.PROCESS, StatusVariable.ARCHIVED, listArchivedDataInstances, processDefinition, processInstanceDescription.id, "");

            } catch (Exception e) {

            }

        }

        // collect local variable in activity - attention, the same sourceinstanceid can come up multiple time
        Set<Long> setSourceInstanceId = new HashSet<Long>();
        for (Map<String, Object> mapActivity : mapActivities.values()) {
            Long activityInstanceId = (Long) mapActivity.get(cstActivityId);
            Long activitySourceInstanceId = (Long) mapActivity.get(cstActivitySourceId);
            if (setSourceInstanceId.contains(activitySourceInstanceId))
                continue;
            setSourceInstanceId.add(activitySourceInstanceId);
            try {
                ActivityInstance act = processAPI.getActivityInstance(activityInstanceId);

                ProcessDefinition processDefinition = processAPI.getProcessDefinition(act.getProcessDefinitionId());
                List<DataInstance> listDataInstances = processAPI.getActivityDataInstances(activityInstanceId, 0, 1000);
                // the getActivityDataInstances return PROCESS_INSTANCE variable !!!
                List<DataInstance> listFilterDataInstances = new ArrayList<DataInstance>();
                for (DataInstance dataInstance : listDataInstances) {
                    if (!dataInstance.getContainerType().equals("PROCESS_INSTANCE"))
                        listFilterDataInstances.add(dataInstance);
                }
                completeListDataInstanceMap(listDataInstanceMap, ScopeVariable.LOCAL, null, listFilterDataInstances, processDefinition, act.getParentContainerId(), act.getName() + "(" + activityInstanceId + ")");
            } catch (Exception e) {
                // logger.info("Exception "+e.getMessage());
                // this should be a Archive activity
            }
            try {

                List<Long> listSourceId = new ArrayList<Long>();
                listSourceId.add(activitySourceInstanceId);
                ArchivedActivityInstance archAct = processAPI.getArchivedActivityInstance(activitySourceInstanceId);

                ProcessDefinition processDefinition = processAPI.getProcessDefinition(archAct.getProcessDefinitionId());
                // we don't collect all value by this request

                List<Map<String, Object>> listArchivedDataInstances = loadArchivedProcessVariables(listSourceId, processAPI);
                // List<ArchivedDataInstance> listArchivedDataInstances =  processAPI.getArchivedActivityDataInstances( activitySourceInstanceId, 0, 1000);

                completeListDataInstanceMap(listDataInstanceMap, ScopeVariable.LOCAL, StatusVariable.ARCHIVED, listArchivedDataInstances, processDefinition, archAct.getParentContainerId(), archAct.getName() + "(" + activitySourceInstanceId + ")");
            } catch (Exception e) {
                logger.info("Exception " + e.getMessage());
            }
        }
        sortTheList(listDataInstanceMap, "processinstance;name;datearchived");

        return listDataInstanceMap;
    }

    public enum StatusVariable {
        ACTIF, ARCHIVED
    };

    public enum ScopeVariable {
        BDM, PROCESS, LOCAL
    };

    /**
     * DataInstance to the Map
     * 
     * @param listDataInstanceMap
     * @param listDataInstances
     * @param processDefinition
     * @param processId
     */
    private static void completeListDataInstanceMap(List<Map<String, Object>> listDataInstanceMap, ScopeVariable scopeVariable, StatusVariable statusVariable, List<?> listDataInstances, ProcessDefinition processDefinition, Long processId, String contextInfo) {
        for (Object dataInstance : listDataInstances) {
            Map<String, Object> mapDataInstance = new HashMap<String, Object>();
            mapDataInstance.put("processname", processDefinition.getName());
            mapDataInstance.put("processversion", processDefinition.getVersion());
            mapDataInstance.put("processinstance", processId);

            mapDataInstance.put("scope", scopeVariable.toString());
            mapDataInstance.put("contextinfo", contextInfo);
            listDataInstanceMap.add(mapDataInstance);

            if (dataInstance instanceof DataInstance) {
                mapDataInstance.put("name", ((DataInstance) dataInstance).getName());
                mapDataInstance.put("description", ((DataInstance) dataInstance).getDescription());
                mapDataInstance.put("type", ((DataInstance) dataInstance).getClassName());
                mapDataInstance.put("datearchived", null);
                mapDataInstance.put("status", StatusVariable.ACTIF.toString());
                mapDataInstance.put("scope", scopeVariable.toString());

                mapDataInstance.put("value", getValueToDisplay(((DataInstance) dataInstance).getValue()));
            }
            if (dataInstance instanceof ArchivedDataInstance) {
                mapDataInstance.put("name", ((ArchivedDataInstance) dataInstance).getName());
                mapDataInstance.put("description", ((ArchivedDataInstance) dataInstance).getDescription());
                mapDataInstance.put("type", ((ArchivedDataInstance) dataInstance).getClassName());
                mapDataInstance.put("datearchived", null);

                mapDataInstance.put("status", StatusVariable.ARCHIVED.toString());
                mapDataInstance.put("scope", scopeVariable.toString());

                mapDataInstance.put("value", getValueToDisplay(((DataInstance) dataInstance).getValue()));
            }
            if (dataInstance instanceof Map) {

                mapDataInstance.putAll((Map) dataInstance);

                mapDataInstance.put("processinstance", processId);
                mapDataInstance.put("processname", processDefinition == null ? "" : processDefinition.getName());
                mapDataInstance.put("processversion", processDefinition == null ? "" : processDefinition.getVersion());

                //mapDataInstance.put("name", variable.get("name") );
                //mapDataInstance.put("description", variable.get("description"));
                //mapDataInstance.put("type", variable.get("type"));
                //mapDataInstance.put("datearchived", variable.get("datearchived"));
                // mapDataInstance.put("value", variable.get("value"));
                mapDataInstance.put("status", statusVariable.toString());
                mapDataInstance.put("scope", scopeVariable.toString());

                // String jsonSt = new JsonBuilder(variable.getValue()).toPrettyString();
            }
            /*
             * Object dataValueJson = (jsonSt==null || jsonSt.length()==0) ?
             * null : new JsonSlurper().parseText(jsonSt);
             * mapDataInstance.put("value", dataValueJson);
             */
        }
    }

    /**
     * JsonBuilder can manage very complex data except.... the basic integer one...
     * 
     * @param value
     * @return
     */
    private static Object getValueToDisplay(Object value) {
        if (value == null)
            return null;
        if (value instanceof Long || value instanceof Double || value instanceof Float || value instanceof Integer)
            return value;
        Object valueJson = new JsonBuilder(value).toPrettyString();
        // last controle...
        if (valueJson == "" && value != null)
            return value;
        return valueJson;
    }

    /**
     * Container for the processInstanceList
     */
    public static class ProcessInstanceList {

        List<Map<String, Object>> listDetails = new ArrayList<Map<String, Object>>();;
        List<Long> listIds = new ArrayList<Long>();;
    }

    /**
     * load all processinstance declaration
     * 
     * @param rootProcessInstanceId
     * @param showSubProcess
     * @param processAPI
     * @return
     */
    public static ProcessInstanceList loadProcessInstances(Long rootProcessInstanceId, boolean showSubProcess,
            ProcessAPI processAPI) {
        ProcessInstanceList processInstanceList = new ProcessInstanceList();

        List<ProcessInstanceDescription> listProcessInstances = getAllProcessInstance(rootProcessInstanceId,
                showSubProcess, processAPI);
        for (ProcessInstanceDescription processInstanceDescription : listProcessInstances) {
            try {
                ProcessDefinition processDefinition = processAPI.getProcessDefinition(processInstanceDescription.processDefinitionId);

                Map<String, Object> processInstanceMap = new HashMap<String, Object>();
                processInstanceList.listDetails.add(processInstanceMap);
                processInstanceList.listIds.add(processInstanceDescription.id);

                processInstanceMap.put("id", processInstanceDescription.id);

                processInstanceMap.put("processname", processDefinition.getName());
                processInstanceMap.put("processversion", processDefinition.getVersion());
                if (processInstanceDescription.callerId != null && processInstanceDescription.callerId > 0) {
                    boolean foundIt = false;
                    try {
                        ActivityInstance act = processAPI.getActivityInstance(processInstanceDescription.callerId);
                        foundIt = true;
                        processInstanceMap.put("parentact", act.getName());
                        long ppid = act.getParentProcessInstanceId();

                        processInstanceMap.put("parentid", ppid);;
                        ProcessDefinition parentProcessDefinition = processAPI.getProcessDefinition(act.getProcessDefinitionId());

                        processInstanceMap.put("parentprocessname", parentProcessDefinition.getName());
                        processInstanceMap.put("parentprocessversion", parentProcessDefinition.getVersion());
                    } catch (Exception e) {
                    }
                    if (!foundIt) { // maybe archived
                        try {
                            ArchivedActivityInstance act = processAPI.getArchivedActivityInstance(processInstanceDescription.callerId);
                            foundIt = true;
                            processInstanceMap.put("parentact", act.getName());
                            long ppid = act.getProcessInstanceId();

                            processInstanceMap.put("parentid", ppid);;
                            ProcessDefinition parentProcessDefinition = processAPI.getProcessDefinition(act.getProcessDefinitionId());

                            processInstanceMap.put("parentprocessname", parentProcessDefinition.getName());
                            processInstanceMap.put("parentprocessversion", parentProcessDefinition.getVersion());
                        } catch (Exception e) {
                        }
                    }

                }
                processInstanceMap.put("status", processInstanceDescription.isActive ? "ACTIF" : "ARCHIVED");
            } catch (Exception e) {
                final StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.severe("During loadProcessinstance : " + e.toString() + " at " + sw.toString());

            }
        }

        return processInstanceList;
    }

    public static class ProcessInstanceDescription {

        public long id;
        public Long callerId;
        public Date startDate;
        public Date endDate;
        public long processDefinitionId;
        public boolean isActive;

    }

    private static List<ProcessInstanceDescription> getAllProcessInstance(long rootProcessInstance,
            boolean showSubProcess, ProcessAPI processAPI) {
        List<ProcessInstanceDescription> listProcessInstances = new ArrayList<ProcessInstanceDescription>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sqlRequest = "";
        try {
            sqlRequest = "select ID, PROCESSDEFINITIONID, CALLERID,  STARTDATE, ENDDATE  from PROCESS_INSTANCE where ROOTPROCESSINSTANCEID = ?";

            // search all process instance like with the root
            con = getConnection();
            pstmt = con.prepareStatement(sqlRequest);
            pstmt.setLong(1, rootProcessInstance);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProcessInstanceDescription processInstanceDescription = new ProcessInstanceDescription();
                processInstanceDescription.id = rs.getLong(1);
                processInstanceDescription.processDefinitionId = rs.getLong(2);
                processInstanceDescription.callerId = rs.getLong(3);
                processInstanceDescription.isActive = true;
                if (showSubProcess || processInstanceDescription.id == rootProcessInstance)
                    listProcessInstances.add(processInstanceDescription);
            }
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
            sqlRequest = "select SOURCEOBJECTID, PROCESSDEFINITIONID, CALLERID, STARTDATE, ENDDATE  from ARCH_PROCESS_INSTANCE where ROOTPROCESSINSTANCEID = ?";
            pstmt = con.prepareStatement(sqlRequest);
            pstmt.setLong(1, rootProcessInstance);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProcessInstanceDescription processInstanceDescription = new ProcessInstanceDescription();
                processInstanceDescription.id = rs.getLong(1);
                processInstanceDescription.processDefinitionId = rs.getLong(2);
                processInstanceDescription.callerId = rs.getLong(3);
                processInstanceDescription.isActive = false;
                //maybe in double?
                boolean alreadyExist = false;
                for (ProcessInstanceDescription current : listProcessInstances) {
                    if (current.id == processInstanceDescription.id)
                        alreadyExist = true;
                }
                if (!alreadyExist)
                    if (showSubProcess || processInstanceDescription.id == rootProcessInstance)
                        listProcessInstances.add(processInstanceDescription);

            }
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
        } catch (Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe("During getAllProcessInstance : " + e.toString() + " at " + sw.toString());

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (final SQLException localSQLException) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                    pstmt = null;
                } catch (final SQLException localSQLException) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                    con = null;
                } catch (final SQLException localSQLException1) {
                }
            }
        }

        return listProcessInstances;
    }

    private static String sqlDataSourceName = "java:/comp/env/bonitaSequenceManagerDS";

    /**
     * The ProcessAPI.getArchivedProcessDataInstances return only the LAST archived version, (not the
     * list) so to get all archived data, only way is the direct SQL request
     * 
     * @param sourceId : maybe the processInstance ID or, for a local variable, the activityId
     * @param processAPI
     * @return
     */
    public static List<Map<String, Object>> loadArchivedProcessVariables(List<Long> sourceId,
            ProcessAPI processAPI) {
        // the PROCESSAPI load as archive the current value.
        // Load the archived : do that in the table
        int maxCount = 200;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> listArchivedDataInstanceMap = new ArrayList<Map<String, Object>>();
        try {
            // logger.info("Connect to [" + sqlDataSourceName + "]
            // loaddomainename[" + domainName + "]");

            List<String> listColumnName = new ArrayList<String>();
            listColumnName.add("INTVALUE");
            listColumnName.add("LONGVALUE");
            listColumnName.add("SHORTTEXTVALUE");
            listColumnName.add("BOOLEANVALUE");
            listColumnName.add("DOUBLEVALUE");
            listColumnName.add("FLOATVALUE");
            listColumnName.add("BLOBVALUE");
            listColumnName.add("CLOBVALUE");

            String sqlRequest = " select NAME , CLASSNAME, CONTAINERID, SOURCEOBJECTID, ID,";
            for (String columnName : listColumnName)
                sqlRequest += columnName + ", ";

            sqlRequest += " ARCHIVEDATE from ARCH_DATA_INSTANCE where CONTAINERID in (";
            // generate a ? per item
            for (int i = 0; i < sourceId.size(); i++) {
                if (i > 0)
                    sqlRequest += ",";
                sqlRequest += " ? ";
            }
            sqlRequest += ") ORDER BY ARCHIVEDATE";

            con = getConnection();
            pstmt = con.prepareStatement(sqlRequest);

            for (int i = 0; i < sourceId.size(); i++) {
                pstmt.setObject(i + 1, sourceId.get(i));
            }

            rs = pstmt.executeQuery();
            while (rs.next() && listArchivedDataInstanceMap.size() < maxCount) {
                Map<String, Object> mapArchivedDataInstance = new HashMap<String, Object>();
                listArchivedDataInstanceMap.add(mapArchivedDataInstance);
                mapArchivedDataInstance.put("name", rs.getString("NAME"));
                mapArchivedDataInstance.put("datearchived", rs.getLong("ARCHIVEDATE"));
                mapArchivedDataInstance.put("containerId", rs.getLong("CONTAINERID"));
                mapArchivedDataInstance.put("sourceId", rs.getLong("SOURCEOBJECTID"));
                String typeVariable = rs.getString("CLASSNAME");
                mapArchivedDataInstance.put("type", typeVariable);

                Object value = null;
                String valueSt = null;
                for (String columnName : listColumnName) {
                    if (value == null) {
                        value = rs.getObject(columnName);
                        if (value != null) {
                            if ("java.util.Date".equals(typeVariable) && value instanceof Long) {
                                valueSt = sdf.format(new Date((Long) value));
                            } else if (value instanceof Clob) {
                                long length = ((Clob) value).length();
                                valueSt = ((Clob) value).getSubString(1, (int) length);
                            } else if ("java.lang.String".equals(typeVariable)) {
                                valueSt = value.toString();
                                // format is clob14:'value'
                                int pos = valueSt.indexOf(":");
                                if (pos != -1) {
                                    valueSt = valueSt.substring(pos + 3);
                                    valueSt = valueSt.substring(0, valueSt.length() - 1);
                                    valueSt = "\"" + valueSt + "\"";
                                }

                            } else
                                valueSt = value.toString();
                        }
                    }
                }

                mapArchivedDataInstance.put("value", valueSt);
            }

        } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();
            logger.severe("loadArchivedProcessVariables : " + e.toString() + " : " + exceptionDetails);

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (final SQLException localSQLException) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                    pstmt = null;
                } catch (final SQLException localSQLException) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                    con = null;
                } catch (final SQLException localSQLException1) {
                }
            }
        }
        return listArchivedDataInstanceMap;
        /*
         * List<Map<String, Object>> listArchivedDataInstanceMap = new
         * ArrayList<Map<String, Object>>(); List<ArchivedDataInstance>
         * listArchivedDataInstance =
         * processAPI.getArchivedProcessDataInstances(processInstanceId, 0,
         * 1000); for (ArchivedDataInstance archivedDataInstance :
         * listArchivedDataInstance) { Map<String, Object>
         * mapArchivedDataInstance = new HashMap<String, Object>();
         * listArchivedDataInstanceMap.add(mapArchivedDataInstance);
         * mapArchivedDataInstance.put("name", archivedDataInstance.getName());
         * mapArchivedDataInstance.put("archiveDate",
         * sdf.format(archivedDataInstance.getArchiveDate()));
         * mapArchivedDataInstance.put("containerId",
         * archivedDataInstance.getContainerId());
         * mapArchivedDataInstance.put("sourceId",
         * archivedDataInstance.getSourceObjectId());
         * mapArchivedDataInstance.put("type",
         * archivedDataInstance.getClassName());
         * String jsonSt = new
         * JsonBuilder(archivedDataInstance.getValue()).toPrettyString();
         * mapArchivedDataInstance.put("value", jsonSt); /* Object dataValueJson
         * = (jsonSt==null || jsonSt.length()==0) ? null : new
         * JsonSlurper().parseText(jsonSt); mapArchivedDataInstance.put("value",
         * dataValueJson);
         */

    }

    /**
     * load BDM
     * 
     * @param processInstanceId
     * @param businessDataAPI
     * @return
     */
    public static List<Map<String, Object>> loadBdmVariables(Long rootProcessInstanceId, boolean showSubProcess, APISession apiSession,
            BusinessDataAPI businessDataAPI, ProcessAPI processAPI) {
        List<Map<String, Object>> listDataInstanceMap = new ArrayList<Map<String, Object>>();
        List<ProcessInstanceDescription> listProcessInstances = getAllProcessInstance(rootProcessInstanceId, showSubProcess, processAPI);
        for (ProcessInstanceDescription processInstanceDescription : listProcessInstances) {
            // BDM
            List<BusinessDataReference> listBdmReference = businessDataAPI.getProcessBusinessDataReferences(processInstanceDescription.id, 0, 1000);
            for (BusinessDataReference businessDataReference : listBdmReference) {

                List<Long> listStorageIds = new ArrayList<Long>();
                Object collectListBdm = null;
                if (businessDataReference instanceof SimpleBusinessDataReference) {
                    collectListBdm = null;
                    // if null, add it even to have a result (bdm name + null)
                    listStorageIds.add(((SimpleBusinessDataReference) businessDataReference).getStorageId());
                } else if (businessDataReference instanceof MultipleBusinessDataReference) {
                    // this is a multiple data
                    collectListBdm = new ArrayList<Object>();
                    if (((MultipleBusinessDataReference) businessDataReference).getStorageIds() == null)
                        listStorageIds.add(null); // add a null value to have a
                                                  // result (bdm name + null) and
                                                  // geet the resultBdm as null
                    else {
                        listStorageIds.addAll(((MultipleBusinessDataReference) businessDataReference).getStorageIds());
                    }
                }

                // now we get a listStorageIds
                try {
                    String classDAOName = businessDataReference.getType() + "DAO";
                    @SuppressWarnings("rawtypes")
                    Class classDao = Class.forName(classDAOName);
                    if (classDao == null) {
                        // a problem here...
                        continue;
                    }

                    BusinessObjectDAOFactory daoFactory = new BusinessObjectDAOFactory();

                    BusinessObjectDAO dao = daoFactory.createDAO(apiSession, classDao);
                    for (Long storageId : listStorageIds) {
                        if (storageId == null) {
                            continue;
                        }
                        Entity dataBdmEntity = null; // dao.findByPersistenceId(storageId);
                        String jsonSt = dataBdmEntity == null ? "" : new JsonBuilder(dataBdmEntity).toPrettyString();
                        // Object dataValueJson = new
                        // JsonSlurper().parseText(jsonSt);

                        if (collectListBdm != null) {
                            @SuppressWarnings("unchecked")
                            List<Object> collectList = (List<Object>) collectListBdm;
                            collectList.add(jsonSt);
                        } else {
                            collectListBdm = jsonSt;
                            break; // be sure we load only one BDM
                        }
                    }

                    Map<String, Object> mapDataInstance = new HashMap<String, Object>();
                    listDataInstanceMap.add(mapDataInstance);

                    ProcessDefinition processDefinition = processAPI.getProcessDefinition(processInstanceDescription.processDefinitionId);

                    mapDataInstance.put("processinstance", processInstanceDescription.id);
                    mapDataInstance.put("processname", processDefinition == null ? "" : processDefinition.getName());
                    mapDataInstance.put("processversion", processDefinition == null ? "" : processDefinition.getVersion());

                    mapDataInstance.put("status", StatusVariable.ACTIF.toString());
                    mapDataInstance.put("scope", ScopeVariable.BDM.toString());

                    mapDataInstance.put("name", businessDataReference.getName());
                    mapDataInstance.put("type", "");
                    mapDataInstance.put("value", collectListBdm);
                } catch (Exception e) {

                }
            } // end loop on all BDM
        } // end loop all processinstance
        return listDataInstanceMap;
    } // end collect BDM

    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* getConnection */
    /*                                                                      */
    /* -------------------------------------------------------------------- */
    private static List<String> listDataSources = Arrays.asList("java:/comp/env/bonitaSequenceManagerDS",
            "java:jboss/datasources/bonitaSequenceManagerDS");

    /**
     * getConnection
     * 
     * @return
     * @throws NamingException
     * @throws SQLException
     */

    public static Connection getConnection() throws SQLException {
        // logger.info(loggerLabel+".getDataSourceConnection() start");

        String msg = "";
        List<String> listDatasourceToCheck = new ArrayList<String>();
        for (String dataSourceString : listDataSources)
            listDatasourceToCheck.add(dataSourceString);

        for (String dataSourceString : listDatasourceToCheck) {
            logger.info(loggerLabel + ".getDataSourceConnection() check[" + dataSourceString + "]");
            try {
                final Context ctx = new InitialContext();
                final DataSource dataSource = (DataSource) ctx.lookup(dataSourceString);
                logger.info(loggerLabel + ".getDataSourceConnection() [" + dataSourceString + "] isOk");
                return dataSource.getConnection();

            } catch (NamingException e) {
                logger.info(
                        loggerLabel + ".getDataSourceConnection() error[" + dataSourceString + "] : " + e.toString());
                msg += "DataSource[" + dataSourceString + "] : error " + e.toString() + ";";
            }
        }
        logger.severe(loggerLabel + ".getDataSourceConnection: Can't found a datasource : " + msg);
        return null;
    }

    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* Private */
    /*                                                                      */
    /* -------------------------------------------------------------------- */
    /**
     * @param listToSort
     * @param attributName : list of attribut separate par ; Example: name;docindex,processinstance .
     *        If name hjave the same value, compare docindex and so on.
     */
    private static void sortTheList(List<Map<String, Object>> listToSort, final String attributName) {

        Collections.sort(listToSort, new Comparator<Map<String, Object>>() {

            public int compare(final Map<String, Object> s1, final Map<String, Object> s2) {

                StringTokenizer st = new StringTokenizer(attributName, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    Object d1 = s1.get(token);
                    Object d2 = s2.get(token);
                    if (d1 != null && d2 != null) {
                        int comparaison = 0;
                        if (d1 instanceof String)
                            comparaison = ((String) d1).compareTo(((String) d2));
                        if (d1 instanceof Integer)
                            comparaison = ((Integer) d1).compareTo(((Integer) d2));
                        if (d1 instanceof Long)
                            comparaison = ((Long) d1).compareTo(((Long) d2));
                        if (comparaison != 0)
                            return comparaison;
                    }
                    // one is null, or both are null : continue
                }
                return 0;
            }
        });;
    }

}
