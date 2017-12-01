package com.bonitasoft.custompage.longboard.casehistory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.log.event.BEvent.Level;import org.w3c.dom.css.CSSStyleSheet;

import com.bonitasoft.custompage.longboard.casehistory.cmdtimer.CmdGetTimer;

public class CaseOperations {

	private static Logger logger = Logger.getLogger(CaseOperations.class.getName());

	private final static BEvent eventCancelCaseWithSuccess = new BEvent(CaseOperations.class.getName(), 1, Level.SUCCESS, "Case is canceled", "The Case is canceled with success");
	private final static BEvent eventCancelCaseError = new BEvent(CaseOperations.class.getName(), 2, Level.ERROR, "Error canceling the Case", "An error arrived when the Case is canceled", "Case is still active", "Check the exception");

	
	private final static BEvent eventExecuteActivityWithSuccess = new BEvent(CaseOperations.class.getName(), 1, Level.SUCCESS, "Activity is executed", "The Activity is executed with success");
	private final static BEvent eventExecuteActivityError = new BEvent(CaseOperations.class.getName(), 2, Level.ERROR, "Error executing the Activity", "An error arrived when the Activity is executed", "Activity is still active", "Check the exception");

	private final static BEvent eventUpdateTimerWithSuccess = new BEvent(CaseOperations.class.getName(), 1, Level.SUCCESS, "Timer is updated", "The Timer is updated and the new value is used");
	private final static BEvent eventUpdateTimeError = new BEvent(CaseOperations.class.getName(), 2, Level.ERROR, "Error updating the Timer", "An error arrived when the Timer is updated", "Timer not change", "Check the exception");

	/**
	 * cancelCase
	 * 
	 * @param processInstanceId
	 * @param processAPI
	 * @return
	 */
	public static Map<String, Object> cancelCase(long processInstanceId, ProcessAPI processAPI) {
		Map<String, Object> caseOperation = new HashMap<String, Object>();
		logger.info("Cancel ProcessInstance[" + processInstanceId + "]");
		List<BEvent> listEvents = new ArrayList<BEvent>();
		try {
			processAPI.cancelProcessInstance(processInstanceId);
			// caseOperation.put("STATUS", "OK");
			logger.info("Cancel ProcessInstance[" + processInstanceId + "] Ok");
			listEvents.add( eventCancelCaseWithSuccess );
		} catch (Exception e) {
			logger.info("Cancel ProcessInstance[" + processInstanceId + "] Failed");
			listEvents.add( new BEvent(eventCancelCaseError, e, String.valueOf( processInstanceId) ));
			// caseOperation.put("STATUS", "FAILED");
		}
		caseOperation.put("listevents",  BEventFactory.getHtml( listEvents));
		return caseOperation;
	}

	/**
	 * execute the activity (no contrat at this moment)
	 * 
	 * @param activityId
	 * @param userId
	 * @param processAPI
	 * @return
	 */

	public static Map<String, Object> executeActivity(long activityId, long userId, ProcessAPI processAPI) {
		Map<String, Object> caseOperation = new HashMap<String, Object>();
		logger.info("Execute ActivityId[" + activityId + "]");
		List<BEvent> listEvents = new ArrayList<BEvent>();
		try {

			processAPI.assignUserTask(activityId, userId);
		} catch (Exception e) {
		}
		try {
			processAPI.executeFlowNode(activityId);
			// caseOperation.put("status", "OK");
			listEvents.add( eventExecuteActivityWithSuccess );
			
			logger.info("Execute ActivityId[" + activityId + "] Ok");
		} catch (Exception e) {
			logger.info("Execute ActivityId[" + activityId + "] Failed");
			listEvents.add( new BEvent(eventExecuteActivityError, e, String.valueOf( activityId) ));
			// caseOperation.put("status", "FAILED " + e.toString());
		}
		caseOperation.put("listevents",  BEventFactory.getHtml( listEvents));
		return caseOperation;
	}

	/**
	 * set the delay in the timer
	 * 
	 * @param processInstanceId
	 * @param timerId
	 * @param newDelayInSec
	 * @param processAPI
	 * @return
	 */
	public static Map<String, Object> setTimerDelay(Long processInstanceId, Long timerId, Long newDelayInSec, boolean forceDeployCommand, final InputStream inputStreamJarFile, final ProcessAPI processAPI, final IdentityAPI identityAPI, final CommandAPI commandAPI) {
		Map<String, Object> caseOperation = new HashMap<String, Object>();
		logger.info("setTimerDelay ProcessInstance[" + processInstanceId + "] timerId[" + timerId + "] delayInSec[" + newDelayInSec + "]");
		List<BEvent> listEvents = new ArrayList<BEvent>();
		try {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, newDelayInSec.intValue());

			try {
				processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerId, c.getTime());
				// caseOperation.put(CaseHistory.cstStatus, "OK");
				caseOperation.put(CaseHistory.cstActivityTimerDate, CaseHistory.sdf.format(c.getTime()));
				caseOperation.put(CaseHistory.cstActivityJobIsStillSchedule, true);
			} catch (TimerEventTriggerInstanceNotFoundException e) {
				caseOperation = CaseHistory.setTimerByCommand(processInstanceId, timerId, newDelayInSec, forceDeployCommand, inputStreamJarFile, commandAPI);
				if (caseOperation.get(CaseHistory.cstStatus)!=null && (! CmdGetTimer.cstResultStatus_OK.equals(caseOperation.get(CaseHistory.cstStatus))))
				{
					listEvents.add( new BEvent(eventUpdateTimeError, caseOperation.get(CaseHistory.cstStatus)+" : "+ String.valueOf( processInstanceId) ));					
				}
				caseOperation.remove(CaseHistory.cstStatus);
						
			}

			logger.info("Cancel ProcessInstance[" + processInstanceId + "] Ok");
			listEvents.add( eventUpdateTimerWithSuccess );
			
		} catch (Exception e) {
			logger.info("Cancel ProcessInstance[" + processInstanceId + "] Failed");
			listEvents.add( new BEvent(eventUpdateTimeError, e, String.valueOf( processInstanceId) ));
			
			// caseOperation.put("status", "FAILED");
		}
		caseOperation.put("listevents",  BEventFactory.getHtml( listEvents));
		return caseOperation;

	}

}
