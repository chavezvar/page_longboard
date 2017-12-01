package com.bonitasoft.custompage.longboard.casehistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;
import org.json.simple.JSONValue;

import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;

public class SignalOperations {
	private static Logger logger = Logger.getLogger(CaseOperations.class.getName());

	private final static BEvent eventSignalSentWithSuccess = new BEvent(SignalOperations.class.getName(), 1, Level.SUCCESS, "Signal sent", "The signal is sent with success");
	private final static BEvent eventSignalSentError = new BEvent(SignalOperations.class.getName(), 2, Level.ERROR, "Error sending the Signal", "An error arrived when the Signal was send", "Cases are not unblock", "Check the exception");
	
	/* -------------------------------------------------------------------- */
	/*                                                                      */
	/* Collect Message														*/
	/*                                                                      */
	/* -------------------------------------------------------------------- */

	public static void collectSignals(CatchEventDefinition catchEventDefinition, EventInstance eventInstance, List<Map<String, Object>> listSignals) 
		{
			for (CatchSignalEventTriggerDefinition signalEvent : catchEventDefinition.getSignalEventTriggerDefinitions()) {
				Map<String, Object> eventSignal = new HashMap<String, Object>();
				eventSignal.put(CaseHistory.cstActivityName, eventInstance.getName());
				eventSignal.put(CaseHistory.cstActivitySignalName, signalEvent.getSignalName());
				eventSignal.put(CaseHistory.cstActivityId, eventInstance.getFlownodeDefinitionId());

				// mapActivity.put(CaseHistory.cstActivitySignalName, signalEvent.getSignalName());
				listSignals.add(eventSignal);
			}
		}
	/* -------------------------------------------------------------------- */
	/*                                                                      */
	/* sendSignal call														*/
	/*                                                                      */
	/* -------------------------------------------------------------------- */

	public static class SignalParameter {
		public String signalName;
		public Long activityId;

		public static SignalParameter getInstanceFromJson(String jsonSt) {
			SignalParameter signalParameter = new SignalParameter();
			if (jsonSt == null)
				return signalParameter;

			final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);

			signalParameter.signalName = LongboardToolbox.jsonToString(jsonHash.get("signalName"), null);
			signalParameter.activityId = LongboardToolbox.jsonToLong(jsonHash.get(CaseHistory.cstActivityId), null);
			return signalParameter;
		}

	}

	public static Map<String, Object> sendSignal(SignalParameter signalParameter, ProcessAPI processAPI) {
		Map<String, Object> answer = new HashMap<String, Object>();
		List<BEvent> listEvents = new ArrayList<BEvent>();
		try {
			processAPI.sendSignal(signalParameter.signalName);
			// answer.put("statusexecution", "Done");
			listEvents.add(eventSignalSentWithSuccess);
			
		} catch (SendEventException e) {
			// answer.put("statusexecution", "Signal does not exist");
			listEvents.add(new BEvent( eventSignalSentError, e, signalParameter.signalName));

		}
		answer.put("listevents",  BEventFactory.getHtml( listEvents));
		return answer;
	}
}
