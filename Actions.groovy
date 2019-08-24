import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Runtime;

import org.json.simple.JSONObject;
import org.codehaus.groovy.tools.shell.CommandAlias;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;


import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

import org.apache.commons.lang3.StringEscapeUtils
 
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.console.common.server.page.PageContext
import org.bonitasoft.console.common.server.page.PageController
import org.bonitasoft.console.common.server.page.PageResourceProvider
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;


import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;


import com.bonitasoft.custompage.longboard.connectortracker.TrackerAccess;
import com.bonitasoft.custompage.longboard.qualification.QualificationPlatform.RunQualification;
import com.bonitasoft.custompage.longboard.monitoring.MonitoringProcesses;
import com.bonitasoft.custompage.longboard.monitoring.MonitoringProcesses.MonitorProcessInput;

import com.bonitasoft.custompage.longboard.casehistory.CaseHistory;
import com.bonitasoft.custompage.longboard.casehistory.CaseHistory.CaseHistoryParameter;


import com.bonitasoft.custompage.longboard.casehistory.TimerOperations;
import com.bonitasoft.custompage.longboard.casehistory.TimerOperations.TimerParameter;

import com.bonitasoft.custompage.longboard.casehistory.SignalOperations;
import com.bonitasoft.custompage.longboard.casehistory.SignalOperations.SignalParameter;

import com.bonitasoft.custompage.longboard.casehistory.MessageOperations;
import com.bonitasoft.custompage.longboard.casehistory.MessageOperations.MessageParameter;

public class Actions {

	private static Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
	
	
	public static Index.ActionAnswer doAction(HttpServletRequest request, String paramJsonSt, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
				
		logger.info("#### LongBoardCustomPage:Actions start");
		Index.ActionAnswer actionAnswer = new Index.ActionAnswer();	
		try {
			String action=request.getParameter("action");
			logger.info("#### LongBoardCustomPage:Actions  action is["+action+"] !");
			if (action==null || action.length()==0 )
			{
				actionAnswer.isManaged=false;
				logger.info("#### LongBoardCustomPage:Actions END No Actions");
				return actionAnswer;
			}
			actionAnswer.isManaged=true;
			
			APISession session = pageContext.getApiSession()
			ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
			CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
			BusinessDataAPI businessDataAPI = TenantAPIAccessor.getBusinessDataAPI(session);
			

			
			if ("casehistory".equals(action))
			{
			   
			    // we can get the caseId as a direct parameters to have an external call
				Integer caseid = Index.getIntegerParameter(request,"caseid",0);
				Boolean showSubProcess = Index.getBooleanParameter(request,"showSubProcess", null);
				
				CaseHistoryParameter caseHistoryParameter = CaseHistoryParameter.getInstanceFromJson( paramJsonSt );
				if (caseid==null)
				{
					caseHistoryParameter.caseId=caseid;
				}
				if (showSubProcess !=null)
					caseHistoryParameter.showSubProcess=showSubProcess;
				logger.info("#### LongBoardCustomPage:Groovy casehistory on caseid["+request.getParameter("caseid")+"]");
		
				InputStream is = pageResourceProvider.getResourceAsStream("lib/CustomPageLongBoard-1.0.1.jar");
				
				//String ping= com.bonitasoft.custompage.longboard.casehistory.CaseHistory.getPing();
				//logger.info("#### LongBoardCustomPage:Groovy Ping"+ping);
				actionAnswer.setResponse( CaseHistory.getCaseDetails(caseHistoryParameter, true, is , session));
			 
			}
			else if ("searchbyindex".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy searchByIndex");
				CaseHistoryParameter caseHistoryParameter = CaseHistoryParameter.getInstanceFromJson( paramJsonSt );
				actionAnswer.setResponse( CaseHistory.getSearchByIndex(caseHistoryParameter, processAPI));
				
			}
			else if( "cancelcase".equals(action))
			{
				int caseid = Index.getIntegerParameter(request,"caseid",0);
				logger.info("#### LongBoardCustomPage:Groovy cancelCase caseid["+caseid+"]");
				actionAnswer.setResponse( com.bonitasoft.custompage.longboard.casehistory.CaseOperations.cancelCase(caseid, processAPI));
			}		
			else if( "executeactivity".equals(action))
			{
				int activityid = Index.getIntegerParameter(request,"activityid",0);
				logger.info("#### LongBoardCustomPage:Groovy Exzecute activityud["+activityid+"]");
				actionAnswer.setResponse( com.bonitasoft.custompage.longboard.casehistory.CaseOperations.executeActivity(activityid, session.getUserId(), processAPI));
			}		
			else if ("updatetimer".equals(action))
			{
				TimerParameter timerParameter = TimerParameter.getInstanceFromJson( paramJsonSt );
				actionAnswer.setResponse(TimerOperations.updateTimer(timerParameter, processAPI));			
			}
			else if ("sendsignal".equals(action))
			{
				SignalParameter signalParameter = SignalParameter.getInstanceFromJson( paramJsonSt );
				actionAnswer.setResponse(SignalOperations.sendSignal(signalParameter, processAPI));			
			}	
			else if ("sendmessage".equals(action))
			{
				MessageParameter messageParameter = MessageParameter.getInstanceFromJson( paramJsonSt );
				actionAnswer.setResponse(MessageOperations.sendMessage(messageParameter, processAPI));			
			}	
			else if ("monitoringapi".equals(action))
			{
				
				logger.info("#### LongBoardCustomPage:Groovy monitoringapi");
				actionAnswer.setResponse( com.bonitasoft.custompage.longboard.monitoring.MonitoringPlatformDetails.getDetails( false, null ));				
			}
			
			else if ("testperf".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy testPerformance");
				
				RunQualification runQualification = new RunQualification();
				runQualification.numberOfCases = Index.getIntegerParameter( request, "runprocesstestnumber",100);
				runQualification.pathDirectoryToWrite = pageResourceProvider.getPageDirectory();
				runQualification.runDirectoryWrite = Index.getBooleanParameter( request, "runbonitahometest", Boolean.TRUE);
				runQualification.runProcess= Index.getBooleanParameter( request, "runprocesstest", Boolean.TRUE);
				runQualification.runDatabase= Index.getBooleanParameter( request, "rundatabasetest", Boolean.TRUE);
		

		
				logger.info("#### LongBoardCustomPage:Groovy testPerformance numberOfCase "+runQualification.numberOfCases);
				actionAnswer.setResponse(com.bonitasoft.custompage.longboard.qualification.QualificationPlatform.allQualifications(runQualification, processAPI));
			}
			
			else if ("timetrackerservice".equals(action))
			{
				boolean startService = Boolean.valueOf( request.getParameter("start"));
				
				logger.info("#### LongBoardCustomPage:Groovy runService ["+startService+"]");
				actionAnswer.setResponse( TrackerAccess.runService(startService, session) );				
			}
			
			else if ("timetrackerservicestate".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy TimeTraker getServiceState");

			
				actionAnswer.setResponse( TrackerAccess.getServiceState( session) );				
			}
			
			else if ("timetrackergetinfos".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy Getinfo Recording");
				List<org.bonitasoft.engine.tracking.FlushEventListener> listFlushEvent=null;
				org.bonitasoft.engine.tracking.TimeTracker timeTracker = TrackerAccess.getTimeTracker(session);
				if (timeTracker!=null)
				{
					// this method is PRIVATE : thanks to goovy to let me call it :-)
					listFlushEvent = timeTracker.getActiveFlushEventListeners();
					logger.info("#### LongBoardCustomPage:Groovy flushEventListener: "+listFlushEvent.size());
				}

				boolean issimulation = Boolean.valueOf( request.getParameter("issimulation"));
				boolean showallinformations= Boolean.valueOf( request.getParameter("showallinformations"));
				Integer rangedisplayInH = Integer.valueOf(request.getParameter("rangedisplayinhour"));
				boolean rangedisplayDuration = Boolean.valueOf(request.getParameter("rangedisplayDuration"));
				boolean rangedisplayMaximum = Boolean.valueOf(request.getParameter("rangedisplayMaximum"));
				final TrackerAccess trackerAccess = TrackerAccess.getInstance();
				
				actionAnswer.setResponse( trackerAccess.getInfos(issimulation,showallinformations,rangedisplayInH.intValue(),rangedisplayDuration, rangedisplayMaximum, listFlushEvent, session, processAPI) );
				
			}
			
			else if ("serverparams".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy Getinfo serverparam session=["+session+"]");
				actionAnswer.setResponse( com.bonitasoft.custompage.longboard.serverparams.ServerParams.getServerParamsInfo(session) );				
			}
			else if ("monitoringprocess".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy monitoringProcesses");
				MonitorProcessInput monitorProcessInput =  MonitorProcessInput.getInstanceFromJsonSt( paramJsonSt );
				actionAnswer.setResponse( MonitoringProcesses.monitorProcesses( monitorProcessInput, processAPI ));				
			}
			
			logger.info("#### LongBoardCustomPage:Actions END responseMap ="+actionAnswer.responseMap.size());
			return actionAnswer;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.severe("#### LongBoardCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
			actionAnswer.isResponseMap=true;
			actionAnswer.responseMap.put("Error", "LongBoardCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
			return actionAnswer;
		}
	}

	
	
	
	/** -------------------------------------------------------------------------
	 *
	 * testPerformance
	 *  
	 */
	private static String testPerformance( RunQualification runQualification, ProcessAPI processAPI   )
	{
		Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
		
		HashMap<String,Object> mapTest = null;
		try
		{
			mapTest = com.bonitasoft.custompage.longboard.qualification.QualificationPlatform.allQualifications(runQualification, processAPI);
		} catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			
			logger.severe("#### LongBoardCustomPage:Groovy Error during performance test["+e.toString()+"] at "+exceptionDetails);
			mapTest = new HashMap<String,Object>();
			
			mapTest.put("errormessage", e.toString());
		}
		String jsonTestSt = JSONValue.toJSONString( mapTest );
		return jsonTestSt;
		
		
		
	}
	
	/** -------------------------------------------------------------------------
	 *
	 * getFactor
	 * 
	 */
	 private double getFactor(long value, long base)
	{
		int factor = (int) ( (double) 10 * ((double)base) / (((double)value))); 
		return ((double) factor) / 10;
	}
	
	
}
