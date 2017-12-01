package com.bonitasoft.custompage.longboard.casehistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.log.event.BEvent.Level;
import org.json.simple.JSONValue;

import com.bonitasoft.custompage.longboard.casehistory.CaseHistory.CaseHistoryParameter;
import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;

public class TimerOperations {
	private static Logger logger = Logger.getLogger(CaseOperations.class.getName());
	private final static BEvent eventUpdateTimerWithSuccess = new BEvent(TimerOperations.class.getName(), 1, Level.SUCCESS, "Timer is updated", "The Timer is updated and the new value is used");
	private final static BEvent eventUpdateTimeError = new BEvent(TimerOperations.class.getName(), 2, Level.ERROR, "Error updating the Timer", "An error arrived when the Timer is updated", "Timer not change", "Check the exception");
	private final static BEvent eventTimerNotFound =  new BEvent(TimerOperations.class.getName(), 3, Level.ERROR, "Timer not found", "The timer is not found (it was maybe excuted ? )", "Timer not change", "Check the exception");
	public static class TimerParameter {
		public String jobName;
		public Long activityId;
		public Long triggerId;
		public String updatetype;
		public String updatevalue;
		public String explanation;

		public static TimerParameter getInstanceFromJson(String jsonSt) {
			TimerParameter timerParameter = new TimerParameter();
			if (jsonSt == null)
				return timerParameter;

			final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);

			timerParameter.jobName = LongboardToolbox.jsonToString(jsonHash.get("jobName"), null);
			timerParameter.activityId = LongboardToolbox.jsonToLong(jsonHash.get(CaseHistory.cstActivityId), null);
			timerParameter.triggerId = LongboardToolbox.jsonToLong(jsonHash.get(CaseHistory.cstTriggerId), null);

			timerParameter.updatetype = LongboardToolbox.jsonToString(jsonHash.get("updatetype"), null);
			timerParameter.updatevalue = LongboardToolbox.jsonToString(jsonHash.get("updatevalue"), null);
			return timerParameter;
		}

		public Date getDateExecution() {
			explanation = "";
			if ("DELAY".equals(updatetype)) {
				Long delayInSecond = LongboardToolbox.jsonToLong(updatevalue, -1L);
				if (delayInSecond == -1) {
					explanation = "Delay must be a number (long)";
					return null;
				}
				return new Date(System.currentTimeMillis() + delayInSecond * 1000);
			} else if ("DATE".equals(updatetype)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					return sdf.parse(updatevalue);
				} catch (Exception e) {
					explanation = "Date must follow the format  yyyy-mm-dd HH24:MM:ss";
				}
				return null;
			}
			explanation = "Only DELAY or DATE policy are accepted";
			return null;
		}
	}

	public static Map<String, Object> updateTimer(TimerParameter timerParameter, ProcessAPI processAPI) {
		Map<String, Object> answer = new HashMap<String, Object>();
		List<BEvent> listEvents = new ArrayList<BEvent>();
		Date dateTrigger = timerParameter.getDateExecution();
		if (dateTrigger == null) {
			answer.put("statusexecution", timerParameter.explanation);
		} else {
			try {
				processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerParameter.triggerId, dateTrigger);
				// answer.put("statusexecution", "Done. Date is now [" + CaseHistory.sdf.format(dateTrigger) + "]");
				answer.put("datetrigger", CaseHistory.sdf.format(dateTrigger));
				listEvents.add( new BEvent(eventUpdateTimerWithSuccess,"Date is now [" + CaseHistory.sdf.format(dateTrigger) + "]" ));
				
			} catch (TimerEventTriggerInstanceNotFoundException e) {
				answer.put("statusexecution", "Timer does not exist");
				listEvents.add( eventTimerNotFound );
				
			} catch (UpdateException e) {
				answer.put("statusexecution", "Error during update :" + e.toString());
				listEvents.add( new BEvent( eventUpdateTimeError, e, "" ));
			}
		}
		answer.put("listevents",  BEventFactory.getHtml( listEvents));
		return answer;
	}
}
