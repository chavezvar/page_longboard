package com.bonitasoft.custompage.longboard.connectortracker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;

/**
 * the TrackerRecordAdvance is the record decoded
 */

public class TrackerRecordAnalysed {

    final static Logger logger = Logger.getLogger(TrackerRecordAnalysed.class.getName());

    public long range;
    Record record;
    public String shortName;
    public String timeStampUser;
    public HashMap<String, Object> decodeDescription = new HashMap<String, Object>();

    public Long processDefinitionId;
    public String processDefinitionName;
    public String processDefinitionVersion;
    public String connectorDefinitionName;
    public Long connectorInstanceId;
    public String typeConnector;

    public Long processInstanceId;

    public String activityInstanceName;
    public Long activityId;

    public StringBuffer decodeRecord = new StringBuffer();

    public static TrackerRecordAnalysed getFromRecord(int range, final Record record, final Map<Long, ProcessDefinition> cacheProcessDefinition,
            final Map<Long, BaseElement> cacheActivityInstance,
            final Map<Long, BaseElement> cacheProcessInstance,
            final ProcessAPI processAPI) {

        final TrackerRecordAnalysed recordAdvance = new TrackerRecordAnalysed();
        recordAdvance.range = range;

        recordAdvance.record = record;

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:MM:ss SSS");
        recordAdvance.timeStampUser = simpleDateFormat.format(record.getTimestamp());

        recordAdvance.decodeRecord.append("~~~~~~~~~~~~~~~~~~~~~~~   name[" + record.getName() + "]  duration[" + record.getDuration() + "] Description [" + record.getDescription() + "]; ");
        // decode the description to get a better information
        // description is processDefinitionId: 8628163084116304956 -
        // connectorDefinitionName: MyFirstConnector - connectorInstanceId:
        // 80006
        // logger.info("Decode description [" + record.getDescription() + "]");
        // In case of a Groovy Connector, description is:

        if (record.getDescription() != null) {
            final StringTokenizer st = new StringTokenizer(record.getDescription(), "-");
            while (st.hasMoreTokens()) {
                final String oneValue = st.nextToken();
                final int posIndex = oneValue.indexOf(":");
                // recordAdvance.decodeRecord.append( "TOKEN[" + oneValue + "]
                // :?" + posIndex + ";");

                if (posIndex == -1) {
                    continue; // unexpected format
                }
                final String code = oneValue.substring(0, posIndex).trim();
                final String value = oneValue.substring(posIndex + 1).trim();
                recordAdvance.decodeRecord.append("CODE[" + code + "] Value[" + value + "]");

                recordAdvance.decodeDescription.put(code.toLowerCase(), value);

                if ("ConnectorResult".equals(code)) {
                    // this is a Groovy Result connector)
                    recordAdvance.shortName = "Connector Result";
                    recordAdvance.processDefinitionName = "Unknow (Groovy Output)";
                    recordAdvance.decodeRecord.append("FOUND: ConnectorResult;");
                } else if ("Expressions".equals(code)) {
                    recordAdvance.shortName = "Expression Result";
                    recordAdvance.processDefinitionName = "Unknow (Groovy Expression)";
                    recordAdvance.decodeRecord.append("FOUND: Expressions;");

                    // Value[SExpressionImpl [name=n/a:First page transitions
                    // condition, content="formCheckPerformance",
                    // returnType=java.lang.String, dependencies=[],
                    // expressionKind=ExpressionKind [interpreter=GROOVY,
                    // type=TYPE_READ_ONLY_SCRIPT]]]
                    recordAdvance.connectorDefinitionName = searchInString(value, "content=", Arrays.asList("\""));
                } else if ("Expression".equals(code)) {
                    // Value[SExpressionImpl [name=n/a:First page transitions
                    // condition, content="formCheckPerformance",
                    // returnType=java.lang.String, dependencies=[],
                    // expressionKind=ExpressionKind [interpreter=GROOVY,
                    // type=TYPE_READ_ONLY_SCRIPT]]]
                    recordAdvance.connectorDefinitionName = searchInString(value, "content=", Arrays.asList("\""));

                }

                else if ("dependencyValues".equals(code) || "evaluationContext".equals(code)) {
                    recordAdvance.connectorDefinitionName = "Groovy Script";

                    // dependencyValues:
                    // Value[{processDefinition=org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl@77bff14c,
                    // locale=en, processDefinitionId=4672351659305155369}]
                    // evaluationContext: Value[context [containerId=null,
                    // containerType=null,
                    // processDefinitionId=4672351659305155369,
                    // processDefinition=CheckPerformance]

                    // look for processDefinitionId
                    recordAdvance.decodeRecord.append("FOUND: " + code + ";");
                    final String processId = searchInString(value, "processDefinitionId=", Arrays.asList(",", "}", "]"));

                    final String containerType = searchInString(value, "containerType=", Arrays.asList(",", "}", "]"));
                    final String containerId = searchInString(value, "containerId=", Arrays.asList(",", "}", "]"));
                    if (containerType != null && containerId != null) {
                        if ("ACTIVITY_INSTANCE".equals(containerType)) {
                            recordAdvance.fillActivityInstance(containerId, cacheActivityInstance, processAPI);

                        }
                        if ("PROCESS_INSTANCE".equals(containerType)) {
                            recordAdvance.fillProcessInstance(containerId, cacheProcessInstance, processAPI);

                        }
                    }
                }
                if ("processDefinitionId".equals(code)) {
                    recordAdvance.decodeRecord.append("MATCH processDefinitionId;");
                    recordAdvance.processDefinitionId = toLong(value);
                    recordAdvance.decodeRecord.append("MATCH processDefinitionId[" + recordAdvance.processDefinitionId + "];");
                    // search the processDefinitnionId
                }
                if ("connectorDefinitionName".equals(code)) {
                    recordAdvance.connectorDefinitionName = value;
                    recordAdvance.decodeRecord.append("MATCH connectorDefinitionName[" + recordAdvance.connectorDefinitionName + "];");
                }
                if ("connectorInstanceId".equals(code)) {
                    recordAdvance.connectorInstanceId = toLong(value);
                    recordAdvance.decodeRecord.append("MATCH connectorInstanceId[" + recordAdvance.connectorInstanceId + "];");

                }
            }
        }
        if (record.getName() != null) {
            if (TimeTrackerRecords.EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT.equals(record.getName()))
                recordAdvance.shortName = "PoolSubmit";

            else if (TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE.equals(record.getName()))
                recordAdvance.shortName = "Callable";

            else if (TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS.equals(record.getName()))
                recordAdvance.shortName = "Operation";

            else if (TimeTrackerRecords.EXECUTE_CONNECTOR_INPUT_EXPRESSIONS.equals(record.getName()))
                recordAdvance.shortName = "Expression";

            else if (TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT.equals(record.getName()))
                recordAdvance.shortName = "Disconnect";

            else if (TimeTrackerRecords.EXECUTE_CONNECTOR_WORK.equals(record.getName()))
                recordAdvance.shortName = "Work";

            else if (TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT.equals(record.getName()))
                recordAdvance.shortName = "Express Including Context";

            else if (TimeTrackerRecords.EVALUATE_EXPRESSION.equals(record.getName()))
                recordAdvance.shortName = "Expression";

            else if (TimeTrackerRecords.EVALUATE_EXPRESSIONS.equals(record.getName()))
                recordAdvance.shortName = "Expressions";

            else
                recordAdvance.shortName = record.getName().toString();

        }
        if (recordAdvance.processInstanceId != null) {
            recordAdvance.fillProcessInstance(String.valueOf(recordAdvance.processInstanceId), cacheProcessInstance, processAPI);
        }
        // logger.info(recordAdvance.decodeRecord.toString());
        if (recordAdvance.processDefinitionId != null) {
            recordAdvance.decodeRecord.append("Search the Process[" + recordAdvance.processDefinitionId + "];");
            recordAdvance.fillProcessDefinition(String.valueOf(recordAdvance.processDefinitionId), cacheProcessDefinition, processAPI);
        }
        return recordAdvance;

    }

    public HashMap<String, Object> getHashMap() {
        final HashMap<String, Object> recordHash = new HashMap<String, Object>();
        recordHash.putAll(decodeDescription);
        recordHash.put("name", String.valueOf(record.getName()));
        recordHash.put("description", record.getDescription());
        recordHash.put("range", range);
        recordHash.put("duration", record.getDuration());
        recordHash.put("timestamp", record.getTimestamp());
        recordHash.put("timestampUser", timeStampUser);
        recordHash.put("pdefname", processDefinitionName);
        recordHash.put("pdefver", processDefinitionVersion);
        recordHash.put("connName", connectorDefinitionName);
        recordHash.put("connInstId", String.valueOf(connectorInstanceId));
        recordHash.put("shortname", shortName);
        recordHash.put("pid", processInstanceId);
        recordHash.put("actname", activityInstanceName);
        recordHash.put("aid", activityId);

        return recordHash;

    }

    /**
     * search something like "....processId=6664," with markerBegin:
     * "processId=", markerEnd1: "," will return "6664"
     *
     * @param value
     * @param markerBegin
     * @param markerEnd1
     * @param markerEnd2
     * @return
     */
    private static String searchInString(final String value, final String markerBegin, final List<String> listMarkerEnd) {
        int posBegin = value.indexOf(markerBegin);
        if (posBegin == -1) {
            return null;
        }

        posBegin += markerBegin.length();
        // search the FIRST marker end

        int posEnd = -1;
        for (String markerEnd : listMarkerEnd) {
            int posMarkerEnd = value.indexOf(markerEnd, posBegin);
            if (posMarkerEnd == -1)
                continue;
            if (posEnd == -1 || posMarkerEnd < posEnd)
                posEnd = posMarkerEnd;
        }
        if (posEnd == -1) {
            return null;
        }
        return value.substring(posBegin, posEnd);
    }

    /**
     * @param value
     * @param cacheProcessDefinition
     * @param processAPI
     * @return
     */
    private void fillProcessDefinition(final String value, final Map<Long, ProcessDefinition> cacheProcessDefinition, final ProcessAPI processAPI) {

        Long valueLong = toLong(value);
        if (valueLong == null)
            return;

        if (!cacheProcessDefinition.containsKey(valueLong)) {
            try {

                // logger.info("Search processId from [" + Long.valueOf(value) +
                // "]");
                cacheProcessDefinition.put(valueLong, processAPI.getProcessDefinition(valueLong));
            } catch (final Exception e) {
                logger.info("Can't found processDefinition from id[" + valueLong + "]");
            }

        }
        ProcessDefinition processDefinition = cacheProcessDefinition.get(valueLong);
        if (processDefinition != null) {
            processDefinitionId = processDefinition.getId();
            processDefinitionName = processDefinition.getName();
            processDefinitionVersion = processDefinition.getVersion();
        }

    }

    /**
     * @param value
     * @param cacheActivityInstance
     * @param processAPI
     * @return
     */
    private void fillActivityInstance(final String value, final Map<Long, BaseElement> cacheActivityInstance, final ProcessAPI processAPI) {
        Long valueLong = toLong(value);
        if (valueLong == null)
            return;
        if (!cacheActivityInstance.containsKey(valueLong)) {
            boolean found = false;
            try {

                // logger.info("Search processId from [" + Long.valueOf(value) +
                // "]");
                cacheActivityInstance.put(valueLong, processAPI.getFlowNodeInstance(valueLong));
                found = true;
            } catch (final FlowNodeInstanceNotFoundException e) {
            }
            if (!found) {
                try {
                    cacheActivityInstance.put(valueLong, processAPI.getArchivedFlowNodeInstance(valueLong));
                    found = true;
                } catch (final ArchivedFlowNodeInstanceNotFoundException e) {
                }
            }
            if (!found)
                try {
                    SearchOptionsBuilder searchOptions = new SearchOptionsBuilder(0, 100);
                    // we have the flowNode.SourceObjectId
                    searchOptions.filter(ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID, valueLong);
                    SearchResult<ArchivedActivityInstance> searchResult = processAPI.searchArchivedActivities(searchOptions.done());
                    if (searchResult.getCount() > 0)
                        cacheActivityInstance.put(valueLong, searchResult.getResult().get(0));
                    found = true;
                } catch (final SearchException e) {
                }
            if (!found) {
                logger.info("No flow node found with [" + valueLong + "]");
            }

        }
        // fullfill the element
        BaseElement baseElement = cacheActivityInstance.get(valueLong);
        if (baseElement != null && baseElement instanceof FlowNodeInstance) {
            FlowNodeInstance flowNode = (FlowNodeInstance) baseElement;
            activityId = flowNode.getId();
            activityInstanceName = flowNode.getName();
            processInstanceId = flowNode.getParentProcessInstanceId();
        }
        if (baseElement != null && baseElement instanceof ArchivedFlowNodeInstance) {
            ArchivedFlowNodeInstance flowNode = (ArchivedFlowNodeInstance) baseElement;
            activityId = flowNode.getId();
            activityInstanceName = flowNode.getName();
            processInstanceId = flowNode.getProcessInstanceId();
        }

    }

    /**
     * @param value
     * @param cacheActivityInstance
     * @param processAPI
     * @return
     */
    private void fillProcessInstance(final String value, final Map<Long, BaseElement> cacheProcessInstance, final ProcessAPI processAPI) {

        Long valueLong = toLong(value);
        if (valueLong == null)
            return;
        if (!cacheProcessInstance.containsKey(valueLong)) {
            try {
                cacheProcessInstance.put(valueLong, processAPI.getProcessInstance(valueLong));
            } catch (final Exception e) {
                try {
                    cacheProcessInstance.put(valueLong, processAPI.getFinalArchivedProcessInstance(valueLong));
                } catch (Exception e2) {
                    logger.info("Can't found ProcessInstanceId from id[" + valueLong + "]");
                }
            }
        }
        // fullfill the element
        BaseElement baseElement = cacheProcessInstance.get(valueLong);
        if (baseElement != null && baseElement instanceof ProcessInstance) {
            ProcessInstance processInstance = (ProcessInstance) baseElement;
            processInstanceId = processInstance.getId();
            processDefinitionId = processInstance.getProcessDefinitionId();
        }
        if (baseElement != null && baseElement instanceof ArchivedProcessInstance) {
            ArchivedProcessInstance archivedProcessInstance = (ArchivedProcessInstance) baseElement;
            processInstanceId = archivedProcessInstance.getSourceObjectId();
            processDefinitionId = archivedProcessInstance.getProcessDefinitionId();
        }
    }

    /**
     * transform to a long or return null
     * 
     * @param value
     * @return
     */
    private static Long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            logger.info("Can't Decode value[" + value + "] as Long");
            return null;
        }

    }
}
