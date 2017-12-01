package com.bonitasoft.custompage.longboard.casehistory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.log.event.BEvent.Level;
import org.json.simple.JSONValue;

import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;

public class MessageOperations {
	private static Logger logger = Logger.getLogger(CaseOperations.class.getName());

	private final static BEvent eventMessageSentWithSuccess = new BEvent(MessageOperations.class.getName(), 1, Level.SUCCESS, "Message sent", "The signal is sent with success");
	private final static BEvent eventMessageSentError = new BEvent(MessageOperations.class.getName(), 2, Level.ERROR, "Error sending the message", "An error arrived when the Message was send", "Cases are not unblock", "Check the exception");

	/* Sql to get information:     
	 * get all catching event 
	 * SELECT * FROM WAITING_EVENT;                                                                  
	 *
	 * All messages 
	 * select * from MESSAGE_INSTANCE ;
	 */
		
	/* -------------------------------------------------------------------- */
	/*                                                                      */
	/* Collect Message														*/
	/*                                                                      */
	/* -------------------------------------------------------------------- */

	
	/**
	 * collect a message
	 * 
	 * @param catchEventDefinition
	 * @param listMessages
	 *            complete the list
	 */
	public static void collectMessage(CatchEventDefinition catchEventDefinition, EventInstance eventInstance, List<Map<String, Object>> listMessages) {
		for (CatchMessageEventTriggerDefinition messageEvent : catchEventDefinition.getMessageEventTriggerDefinitions()) {
			Map<String, Object> eventmessage = new HashMap<String, Object>();
			listMessages.add(eventmessage);
			eventmessage.put(CaseHistory.cstActivityName, eventInstance.getName());
			eventmessage.put(CaseHistory.cstActivityMessageName, messageEvent.getMessageName());
			eventmessage.put(CaseHistory.cstActivityId, eventInstance.getId());

			List<String> listCorrelationValue = getCorrelationValue(eventInstance);
			List<Map<String, Object>> listCorrelations = new ArrayList<Map<String, Object>>();
			eventmessage.put(CaseHistory.cstActivityMessageCorrelationList, listCorrelations);
			
			List<CorrelationDefinition> listCorrections = messageEvent.getCorrelations();
			for (int i =0; i< listCorrections.size();i++)
			{
				CorrelationDefinition correlationDefinition = listCorrections.get( i );
			
				Map<String, Object> correlation = new HashMap<String, Object>();
				correlation.put(CaseHistory.cstActivityMessageVarName, correlationDefinition.getKey().getName());
				correlation.put(CaseHistory.cstActivityMessageVarValue, i < listCorrelationValue.size() ? listCorrelationValue.get( i ): null);
				
				correlation.put(CaseHistory.cstActivityCorrelationDefinition, correlationDefinition.getValue().getContent().toString());
				listCorrelations.add(correlation);

			}
			List<Map<String, Object>> listContent = new ArrayList<Map<String, Object>>();
			eventmessage.put(CaseHistory.cstActivityMessageContentList, listContent);
			for (Operation operationDefinition : messageEvent.getOperations()) {
				Map<String, Object> operation = new HashMap<String, Object>();
				operation.put(CaseHistory.cstActivityMessageVarName, operationDefinition.getRightOperand().getName());
				listContent.add(operation);

			}

		}

	}

	/**
	 * getCorrelationValue
	 * @param eventInstance
	 * @return
	 */

	private static List<String> getCorrelationValue(EventInstance eventInstance) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> listCorrelationValue = new ArrayList<String>();
		try {

			// Get the VALUE for the correlation
			// SELECT CORRELATION1, CORRELATION2, CORRELATION3, CORRELATION4,
			// CORRELATION5 FROM WAITING_EVENT where FLOWNODEINSTANCEID=60004
			con = CaseHistory.getConnection();
			List<String> listColumnName = new ArrayList<String>();

			for (int i = 1; i <= 5; i++) {
				listColumnName.add("CORRELATION" + i);
			}
			String sqlRequest = " select ";
			for (int i = 0; i < listColumnName.size(); i++) {
				sqlRequest += listColumnName.get(i) + ", ";
			}
			sqlRequest += " FLOWNODEINSTANCEID  FROM WAITING_EVENT where FLOWNODEINSTANCEID=?";

			pstmt = con.prepareStatement(sqlRequest);
			pstmt.setObject(1, eventInstance.getId());

			rs = pstmt.executeQuery();
			// expect only one record
			if (!rs.next())
				return listCorrelationValue;
			for (int i = 0; i < listColumnName.size(); i++) {
				String result = rs.getString(i + 1);
				// form is msgKeyId-$-3002
				if (result != null)
				{
					int pos=result.indexOf("-$-");
					if (pos !=-1)
					result = result.substring(pos+"-$-".length());
				}
				listCorrelationValue.add( result );
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
		return listCorrelationValue;

	}
	/* -------------------------------------------------------------------- */
	/*                                                                      */
	/* sendMessage call														*/
	/*                                                                      */
	/* -------------------------------------------------------------------- */

	/**
	 * operation on the message
	 *
	 * 
	 */
	public static class MessageParameter {
		
		public String messageName;
		public Long activityId;
		List<Map<String,Object>> listCorrelations;
		List<Map<String,Object>> listContent;
		
		public static MessageParameter getInstanceFromJson(String jsonSt) {
			MessageParameter messageParameter = new MessageParameter();
			if (jsonSt == null)
				return messageParameter;

			final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);
			messageParameter.messageName = LongboardToolbox.jsonToString(jsonHash.get("messageName"), null);
			messageParameter.activityId = LongboardToolbox.jsonToLong(jsonHash.get(CaseHistory.cstActivityId), null);
			messageParameter.listCorrelations = LongboardToolbox.jsonToListMap(jsonHash.get("correlations"));		
			messageParameter.listContent = LongboardToolbox.jsonToListMap(jsonHash.get("contents"));
			
			return messageParameter;
		}

	}

	/**
	 * send the message
	 * @param messageParameter
	 * @param processAPI
	 * @return
	 */
	public static Map<String, Object> sendMessage(MessageParameter messageParameter, ProcessAPI processAPI) {
		Map<String, Object> answer = new HashMap<String, Object>();
		List<BEvent> listEvents = new ArrayList<BEvent>();
		try {
			FlowNodeInstance flowNodeInstance= processAPI.getFlowNodeInstance(messageParameter.activityId);
			ProcessDefinition processDefinition= processAPI.getProcessDefinition(flowNodeInstance.getProcessDefinitionId());
			
			//Expression targetProcess= new ExpressionBuilder().createNewInstance("targetProcess").setContent(String.valueOf(processDefinition.getName())).setExpressionType( ExpressionType.TYPE_CONSTANT).setReturnType(String.class.getName()).done();
			// Expression targetFlowNode= new ExpressionBuilder().createNewInstance("targetFlowNode").setContent(String.valueOf(flowNodeInstance.getName())).setExpressionType( ExpressionType.TYPE_CONSTANT).setReturnType(String.class.getName()).done();
			
			Expression targetProcess= new ExpressionBuilder().createConstantStringExpression( processDefinition.getName());
			Expression targetFlowNode = new ExpressionBuilder().createConstantStringExpression( flowNodeInstance.getName());
			Map<Expression,Expression> messageContent = createMapExpression(messageParameter.listContent);
			
			
			if (messageParameter.listCorrelations==null)
			{
				processAPI.sendMessage(messageParameter.messageName, targetProcess, targetFlowNode, messageContent);
			}
			else
			{
				Map<Expression,Expression> correlations = createMapExpression(messageParameter.listCorrelations);
				processAPI.sendMessage(messageParameter.messageName,  targetProcess,  targetFlowNode, messageContent,  correlations);
			}
			// answer.put("statusexecution", "Done");
			listEvents.add(eventMessageSentWithSuccess);
			} catch (SendEventException se )
		{
			// answer.put("statusexecution", "Message Can't be send "+se.toString());
			listEvents.add(new BEvent( eventMessageSentError, se, messageParameter.messageName));


		} catch (Exception e) {
			// answer.put("statusexecution", "Message error "+e.toString());
			listEvents.add(new BEvent( eventMessageSentError, e, messageParameter.messageName));

		}
		answer.put("listevents",  BEventFactory.getHtml( listEvents));
		return answer;
	}
	
	private static Map<Expression,Expression> createMapExpression(List<Map<String,Object>> listValues) throws InvalidExpressionException, IllegalArgumentException
	{
		Map<Expression,Expression> mapExpression = new HashMap<Expression,Expression>();
		for ( Map<String,Object> oneItem : listValues)
		{
			String name= (String) oneItem.get(CaseHistory.cstActivityMessageVarName);
			String value= (String) oneItem.get(CaseHistory.cstActivityMessageVarValue);
			
			//Expression exprName= new ExpressionBuilder().createNewInstance("name"+name).setContent( name).setExpressionType( ExpressionType.TYPE_CONSTANT).setReturnType( String.class.getName()).done();
			//Expression exprValue= new ExpressionBuilder().createNewInstance("value").setContent( value ).setExpressionType( ExpressionType.TYPE_CONSTANT).setReturnType(String.class.getName()).done();
			
			Expression exprName= new ExpressionBuilder().createConstantStringExpression( name);
			Expression exprValue= null;
			if (value==null || value.length()==0)
				exprValue= new ExpressionBuilder().createNewInstance("value-"+name).setContent( "" ).setExpressionType( ExpressionType.TYPE_CONSTANT).setReturnType(String.class.getName()).done();
			else
				exprValue=new ExpressionBuilder().createConstantStringExpression( value );
			mapExpression.put( exprName, exprValue);
		}
		
		return mapExpression;
		
	}
}
