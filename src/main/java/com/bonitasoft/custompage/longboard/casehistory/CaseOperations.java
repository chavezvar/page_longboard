package com.bonitasoft.custompage.longboard.casehistory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;

import com.bonitasoft.custompage.longboard.casehistory.cmdtimer.CmdGetTimer;

public class CaseOperations {

    private static Logger logger = Logger.getLogger(CaseOperations.class.getName());

    private final static BEvent eventCancelCaseWithSuccess = new BEvent(CaseOperations.class.getName(), 1, Level.SUCCESS, "Case is canceled", "The Case is canceled with success");
    private final static BEvent eventCancelCaseError = new BEvent(CaseOperations.class.getName(), 2, Level.ERROR, "Error canceling the Case", "An error arrived when the Case is canceled", "Case is still active", "Check the exception");

    private final static BEvent eventExecuteActivityWithSuccess     = new BEvent(CaseOperations.class.getName(), 3, Level.SUCCESS, "Activity executed", "The Activity is executed with success");
    private final static BEvent eventUpdateActorWithSuccess         = new BEvent(CaseOperations.class.getName(), 4, Level.SUCCESS, "Actor updated", "Actor is updated with success");
    private final static BEvent eventUpdateDueDateWithSuccess       = new BEvent(CaseOperations.class.getName(), 5, Level.SUCCESS, "Expected End Date updated", "The Expected end is updated with success");
    private final static BEvent eventRetryTaskWithSuccess           = new BEvent(CaseOperations.class.getName(), 6, Level.SUCCESS, "Retry tasks updated", "The Retry task is executed with success");
    private final static BEvent eventReleaseUserWithSuccess         = new BEvent(CaseOperations.class.getName(), 7, Level.SUCCESS, "Release task", "The task is released and is now visible by all users behind the actor");
    
    private final static BEvent eventExecuteActivityError           = new BEvent(CaseOperations.class.getName(), 8, Level.ERROR, "Error executing the Activity", "An error arrived when the Activity is executed", "Activity is still active", "Check the exception");
    private final static BEvent eventReplayActorFilter              = new BEvent(CaseOperations.class.getName(), 9, Level.ERROR, "Error Replay actor filter", "An error arrived when actor filter is updated", "Users does not changed", "Check the exception");
    private final static BEvent eventNotAHumanTask                  = new BEvent(CaseOperations.class.getName(), 10, Level.APPLICATIONERROR, "Not a human task", "This operation is possible only on a human task", "No application", "Check the activity");
    private final static BEvent eventUpdateDueDate                  = new BEvent(CaseOperations.class.getName(), 11, Level.ERROR, "Error updated the Due date", "Error when updating a due date in a task", "No change", "Check the exception");
    private final static BEvent eventRetryTask                      = new BEvent(CaseOperations.class.getName(), 12, Level.ERROR, "Error retry a task", "Error when executed again a task", "No execution", "Check the exception");
    private final static BEvent eventReleaseError                   = new BEvent(CaseOperations.class.getName(), 12, Level.ERROR, "Release error", "Error when release the task (maybe already released)", "Operation is not performed", "Check the exception");
    
    private final static BEvent eventUpdateTimerWithSuccess         = new BEvent(CaseOperations.class.getName(), 13, Level.SUCCESS, "Timer is updated", "The Timer is updated and the new value is used");
    private final static BEvent eventUpdateTimeError                = new BEvent(CaseOperations.class.getName(), 14, Level.ERROR, "Error updating the Timer", "An error arrived when the Timer is updated", "Timer not change", "Check the exception");

    
    

    
    /**
     * cancelCase
     * 
     * @param processInstanceId
     * @param processAPI
     * @return
     */
    public static Map<String, Object> cancelCase(long processInstanceId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("Cancel ProcessInstance[" + processInstanceId + "]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            processAPI.cancelProcessInstance(processInstanceId);
            // caseOperation.put("STATUS", "OK");
            logger.info("Cancel ProcessInstance[" + processInstanceId + "] Ok");
            listEvents.add(eventCancelCaseWithSuccess);
        } catch (Exception e) {
            logger.info("Cancel ProcessInstance[" + processInstanceId + "] Failed");
            listEvents.add(new BEvent(eventCancelCaseError, e, String.valueOf(processInstanceId)));
            // caseOperation.put("STATUS", "FAILED");
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
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
        List<BEvent> listEvents = new ArrayList<>();
        try {

            processAPI.assignUserTask(activityId, userId);
        } catch (Exception e) {
        }
        try {
            processAPI.executeFlowNode(activityId);
            // caseOperation.put("status", "OK");
            listEvents.add(eventExecuteActivityWithSuccess);

            logger.info("Execute ActivityId[" + activityId + "] Ok");
        } catch (Exception e) {
            logger.info("Execute ActivityId[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventExecuteActivityError, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;
    }

    /**
     * 
     * @param activityId
     * @param userId
     * @param processAPI
     * @return
     */
    public static Map<String, Object> replayActorFilter(long activityId, long userId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("replayActorFilter ActivityId[" + activityId + "]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            processAPI.updateActorsOfUserTask(activityId);
            listEvents.add(eventUpdateActorWithSuccess);

        } catch (Exception e) {        
            logger.info("Execute replayActorFilter[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventReplayActorFilter, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;
    }
    

    /**
     * 
     * @param activityId
     * @param userId
     * @param processAPI
     * @return
     */
    public static Map<String, Object> releaseUserTask(long activityId, long userId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("releaseUserTask ActivityId[" + activityId + "]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            processAPI.releaseUserTask(activityId);
            listEvents.add(eventReleaseUserWithSuccess);

        } catch (Exception e) {        
            logger.info("Execute replayActorFilter[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventReleaseError, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;
    }
    
    /**
     * 
     * @param activityId
     * @param delayInMinutes
     * @param userId
     * @param processAPI
     * @return
     */
    public static Map<String, Object> updateDueDate(long activityId, int delayInMinutes, long userId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("updateDueDate ActivityId[" + activityId + "] DelayInSecond["+delayInMinutes+"]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            ActivityInstance activity = processAPI.getActivityInstance(activityId);
            if (! (activity instanceof HumanTaskInstance)) {
                listEvents.add(new BEvent(eventNotAHumanTask, String.valueOf(activityId)));

            } else {
            HumanTaskInstance humanTask = (HumanTaskInstance) activity;
            Date dueDate = humanTask.getExpectedEndDate();
            if (dueDate==null)
                dueDate = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dueDate);
            c.add( Calendar.MINUTE, delayInMinutes);
            processAPI.updateDueDateOfTask(activityId, c.getTime());
            listEvents.add(eventUpdateDueDateWithSuccess);

            }

        } catch (Exception e) {        
            logger.info("Execute replayActorFilter[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventUpdateDueDate, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;
    }
    
    /**
     * 
     * @param activityId
     * @param userId
     * @param processAPI
     * @return
     */
    public static Map<String, Object> replayFailedTask(long activityId, long userId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("replayActorFilter ActivityId[" + activityId + "]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            processAPI.retryTask(activityId);
            listEvents.add(eventRetryTaskWithSuccess);

        } catch (Exception e) {        
            logger.info("Execute replayActorFilter[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventRetryTask, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;
    }
    
    public static Map<String, Object> skipFailedTask(long activityId, long userId, ProcessAPI processAPI) {
        Map<String, Object> caseOperation = new HashMap<>();
        logger.info("replayActorFilter ActivityId[" + activityId + "]");
        List<BEvent> listEvents = new ArrayList<>();
        try {
            processAPI.setActivityStateByName(activityId, ActivityStates.SKIPPED_STATE);
            listEvents.add(eventRetryTaskWithSuccess);

        } catch (Exception e) {        
            logger.info("Execute replayActorFilter[" + activityId + "] Failed");
            listEvents.add(new BEvent(eventRetryTask, e, String.valueOf(activityId)));
            // caseOperation.put("status", "FAILED " + e.toString());
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
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
                if (caseOperation.get(CaseHistory.cstStatus) != null && (!CmdGetTimer.cstResultStatus_OK.equals(caseOperation.get(CaseHistory.cstStatus)))) {
                    listEvents.add(new BEvent(eventUpdateTimeError, caseOperation.get(CaseHistory.cstStatus) + " : " + String.valueOf(processInstanceId)));
                }
                caseOperation.remove(CaseHistory.cstStatus);

            }

            logger.info("Cancel ProcessInstance[" + processInstanceId + "] Ok");
            listEvents.add(eventUpdateTimerWithSuccess);

        } catch (Exception e) {
            logger.info("Cancel ProcessInstance[" + processInstanceId + "] Failed");
            listEvents.add(new BEvent(eventUpdateTimeError, e, String.valueOf(processInstanceId)));

            // caseOperation.put("status", "FAILED");
        }
        caseOperation.put("listevents", BEventFactory.getHtml(listEvents));
        return caseOperation;

    }

}
