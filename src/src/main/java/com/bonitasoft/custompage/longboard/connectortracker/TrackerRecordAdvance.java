package com.bonitasoft.custompage.longboard.connectortracker;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.tracking.Record;

/**
 * the TrackerRecordAdvance is the record decoded
 */

public class TrackerRecordAdvance {

    final static Logger logger = Logger.getLogger(TrackerRecordAdvance.class.getName());

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

    public StringBuffer decodeRecord = new StringBuffer();

    public static TrackerRecordAdvance getFromRecord(final Record record, final HashMap<String, ProcessDefinition> cacheProcessDefinition, final ProcessAPI processAPI) {
        final TrackerRecordAdvance recordAdvance = new TrackerRecordAdvance();
        recordAdvance.record = record;


        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:MM:ss SSS");
        recordAdvance.timeStampUser = simpleDateFormat.format(record.getTimestamp());

        recordAdvance.decodeRecord.append("~~~~~~~~~~~~~~~~~~~~~~~   name[" + record.getName() + "]  duration["
                + record.getDuration() + "] Description [" + record.getDescription() + "]; ");
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
                // recordAdvance.decodeRecord.append( "TOKEN[" + oneValue + "] :?" + posIndex + ";");

                if (posIndex == -1)
                 {
                    continue; // unexpected format
                }
                final String code = oneValue.substring(0, posIndex).trim();
                final String value = oneValue.substring(posIndex + 1).trim();
                recordAdvance.decodeRecord.append( "CODE[" + code + "] Value[" + value + "]");

                recordAdvance.decodeDescription.put(code.toLowerCase(), value);

                if ("ConnectorResult".equals(code)) {
                    // this is a Groovy Result connector)
                    recordAdvance.shortName = "Connector Result";
                    recordAdvance.processDefinitionName = "Unknow (Groovy Output)";
                    recordAdvance.decodeRecord.append( "FOUND: ConnectorResult;" );
                }
                else if ("Expressions".equals(code))
                {
                    recordAdvance.shortName = "Expression Result";
                    recordAdvance.processDefinitionName = "Unknow (Groovy Expression)";
                    recordAdvance.decodeRecord.append("FOUND: Expressions;");

                    // Value[SExpressionImpl [name=n/a:First page transitions condition, content="formCheckPerformance", returnType=java.lang.String, dependencies=[], expressionKind=ExpressionKind [interpreter=GROOVY, type=TYPE_READ_ONLY_SCRIPT]]]
                    recordAdvance.connectorDefinitionName = searchInString(value, "content=", "\"", null);
                }
                else if ("Expression".equals(code))
                {
                    // Value[SExpressionImpl [name=n/a:First page transitions condition, content="formCheckPerformance", returnType=java.lang.String, dependencies=[], expressionKind=ExpressionKind [interpreter=GROOVY, type=TYPE_READ_ONLY_SCRIPT]]]
                    recordAdvance.connectorDefinitionName = searchInString(value, "content=", "\"", null);

                }

                else if ("dependencyValues".equals(code) || "evaluationContext".equals(code))
                {
                    recordAdvance.connectorDefinitionName = "Groovy Script";

                    // dependencyValues: Value[{processDefinition=org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl@77bff14c, locale=en, processDefinitionId=4672351659305155369}]
                    // evaluationContext: Value[context [containerId=null, containerType=null, processDefinitionId=4672351659305155369, processDefinition=CheckPerformance]

                    // look for processDefinitionId
                    recordAdvance.decodeRecord.append("FOUND: " + code + ";");
                    final String processId = searchInString(value, "processDefinitionId=", ",", "}");
                    if (processId != null)
                    {
                        recordAdvance.decodeRecord.append( "Search the Process["+processId+"];");
                        final ProcessDefinition processDefinition = getProcessDefinition(processId, cacheProcessDefinition, processAPI);
                        if (processDefinition!=null)
                        {
                            recordAdvance.decodeRecord.append( "FOUND the Process;" );

                            recordAdvance.processDefinitionName = processDefinition == null ? "unknow (" + value + ")" : processDefinition.getName();
                            recordAdvance.processDefinitionVersion = processDefinition == null ? "unknow (" + value + ")" : processDefinition.getVersion();
                        }
                    }
                }
                if ("processDefinitionId".equals(code)) {
                    recordAdvance.decodeRecord.append( "MATCH processDefinitionId;" );
                    // search the processDefinitnionId
                    final ProcessDefinition processDefinition = getProcessDefinition( value, cacheProcessDefinition, processAPI );
                    if (processDefinition!=null)
                    {
                        recordAdvance.decodeRecord.append( "FOUND the Process;" );

                        recordAdvance.processDefinitionName = processDefinition == null ? "unknow (" + value + ")" : processDefinition.getName();
                        recordAdvance.processDefinitionVersion = processDefinition == null ? "unknow (" + value + ")" : processDefinition.getVersion();
                        recordAdvance.decodeRecord.append( "FOUND : processDefinitionName[" + recordAdvance.processDefinitionName + "(" + recordAdvance.processDefinitionVersion
                            + ")];" );
                    }
                }
                if ("connectorDefinitionName".equals(code)) {
                    recordAdvance.connectorDefinitionName = value;
                    recordAdvance.decodeRecord.append( "MATCH connectorDefinitionName[" + recordAdvance.connectorDefinitionName + "];" );
                }
                if ("connectorInstanceId".equals(code)) {
                    try {
                        recordAdvance.connectorInstanceId = Long.valueOf(value);
                        recordAdvance.decodeRecord.append( "MATCH connectorInstanceId[" + recordAdvance.connectorInstanceId + "];" );

                    } catch (final Exception e) {
                        logger.info("connectorInstanceId[" + value + "] is not a Long.");
                    }
                }
            }
        }
        if (record.getName() != null) {
            if ("EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT".equals(record.getName())) {
                recordAdvance.shortName = "PoolSubmit";
            }
            if ("EXECUTE_CONNECTOR_CALLABLE".equals(record.getName())) {
                recordAdvance.shortName = "Callable";
            }
            if ("EXECUTE_CONNECTOR_OUTPUT_OPERATIONS".equals(record.getName())) {
                recordAdvance.shortName = "Operation";
            }
            if ("EXECUTE_CONNECTOR_INPUT_EXPRESSIONS".equals(record.getName())) {
                recordAdvance.shortName = "Expression";
            }
            if ("EXECUTE_CONNECTOR_DISCONNECT".equals(record.getName())) {
                recordAdvance.shortName = "Disconnect";
            }
            if ("EXECUTE_CONNECTOR_WORK".equals(record.getName())) {
                recordAdvance.shortName = "Work";
            }
        }
        // logger.info(recordAdvance.decodeRecord.toString());

        return recordAdvance;

    }

    public HashMap<String, Object> getHashMap() {
        final HashMap<String, Object> recordHash = new HashMap<String, Object>();
        recordHash.putAll(decodeDescription);
        recordHash.put("name", record.getName());
        recordHash.put("description", record.getDescription());
        recordHash.put("duration", record.getDuration());
        recordHash.put("timestamp", record.getTimestamp());
        recordHash.put("timestampUser", timeStampUser);
        recordHash.put("processdefinitionname", processDefinitionName);
        recordHash.put("processdefinitionversion", processDefinitionVersion);
        recordHash.put("connectorDefinitionName", connectorDefinitionName);
        recordHash.put("connectorInstanceId", String.valueOf(connectorInstanceId));
        recordHash.put("shortname", shortName);

        return recordHash;

    }

    /**
     * search something like "....processId=6664," with markerBegin: "processId=", markerEnd1: "," will return "6664"
     *
     * @param value
     * @param markerBegin
     * @param markerEnd1
     * @param markerEnd2
     * @return
     */
    private static String searchInString(final String value, final String markerBegin, final String markerEnd1, final String markerEnd2)
    {
        int posBegin = value.indexOf(markerBegin);
        if (posBegin == -1) {
            return null;
        }

        posBegin += markerBegin.length();
        int posEnd = value.indexOf(markerEnd1, posBegin);
        if (posEnd == -1 && markerEnd2 != null) {
            posEnd = value.indexOf(markerEnd2, posBegin);
        }
        if (posEnd == -1) {
            return null;
        }
        return value.substring(posBegin, posEnd);
    }

    private static ProcessDefinition getProcessDefinition(final String value, final HashMap<String, ProcessDefinition> cacheProcessDefinition,
            final ProcessAPI processAPI)
    {
        ProcessDefinition processDefinition = null;
        // logger.info("Search processId from value ? [" + value + "] - inCache ? " + cacheProcessDefinition.containsKey(value) + " cache=" + cacheProcessDefinition);
        if (!cacheProcessDefinition.containsKey(value)) {
            try {

                // logger.info("Search processId from [" + Long.valueOf(value) + "]");
                processDefinition = processAPI.getProcessDefinition(Long.valueOf(value));

            } catch (final Exception e) {
                logger.info("Can't found processDefinition from id[" + value + "]");
            }
            cacheProcessDefinition.put(value, processDefinition);
        }
        return cacheProcessDefinition.get(value);

    }
}
