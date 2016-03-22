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

import com.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
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


public class Index implements PageController {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
	
		Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
		
		
		try {
			def String indexContent;
			pageResourceProvider.getResourceAsStream("Index.groovy").withStream { InputStream s-> indexContent = s.getText() };
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter()

			String action=request.getParameter("action");
			logger.info("#### LongBoardCustomPage:Groovy  action is["+action+"] !");
			if (action==null || action.length()==0 )
			{
				runTheBonitaIndexDoGet( request, response,pageResourceProvider,pageContext);
				return;
			}
			String paramJson= request.getParameter("paramjson");
			
			
			APISession session = pageContext.getApiSession()
			ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI(session);
			IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
			CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
			
			HashMap<String,Object> answer = null;
			
			if ("casehistory".equals(action))
			{
				int caseid = getIntegerParameter(request,"caseid",0);
				boolean showSubProcess = getBooleanParameter(request,"showSubProcess",false);
				logger.info("#### LongBoardCustomPage:Groovy casehistory on caseid["+request.getParameter("caseid")+"]");
		
							   
				out.write( getCaseHistoryInJson( caseid, showSubProcess, pageResourceProvider, processAPI,identityApi,commandAPI )  );
				out.flush();
				out.close();				
				return;				
			}
			else if( "cancelcase".equals(action))
			{
				int caseid = getIntegerParameter(request,"caseid",0);
				logger.info("#### LongBoardCustomPage:Groovy cancelCase caseid["+caseid+"]");
				answer  = com.bonitasoft.custompage.longboard.casehistory.CaseOperations.cancelCase(caseid, processAPI);
			}			
			else if ("monitoringapi".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy monitoringapi");
				answer  = com.bonitasoft.custompage.longboard.monitoring.MonitoringPlatformDetails.getDetails( false, platformMonitoringAPI );				
			}
			
			else if ("testperf".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy testPerformance");
				
				RunQualification runQualification = new RunQualification();
				runQualification.numberOfCases = getIntegerParameter( request, "runprocesstestnumber",100);
				runQualification.pathDirectoryToWrite = pageResourceProvider.getTempPageDirectory();
				runQualification.runDirectoryWrite = getBooleanParameter( request, "runbonitahometest", Boolean.TRUE);
				runQualification.runProcess= getBooleanParameter( request, "runprocesstest", Boolean.TRUE);
				runQualification.runDatabase= getBooleanParameter( request, "rundatabasetest", Boolean.TRUE);
		

		
				logger.info("#### LongBoardCustomPage:Groovy testPerformance numberOfCase "+runQualification.numberOfCases);
				
				out.write( testPerformance( runQualification , processAPI  )  );
				out.flush();
				out.close();
				return;
			}
			
			else if ("timetrackerservice".equals(action))
			{
				boolean startService = Boolean.valueOf( request.getParameter("start"));
				
				logger.info("#### LongBoardCustomPage:Groovy runService ["+startService+"]");
				answer = com.bonitasoft.custompage.longboard.connectortracker.TrackerAccess.runService(startService, session);				
			}
			
			else if ("timetrackerservicestate".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy TimeTraker getServiceState");
				answer = TrackerAccess.getServiceState( session);				
			}
			
			else if ("timetrackergetinfos".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy Getinfo Recording");
				
				boolean issimulation = Boolean.valueOf( request.getParameter("issimulation"));
				boolean showallinformations= Boolean.valueOf( request.getParameter("showallinformations"));
				Integer rangedisplayInH = Integer.valueOf(request.getParameter("rangedisplayinhour"));
				
				answer = com.bonitasoft.custompage.longboard.connectortracker.TrackerAccess.getInfos(issimulation,showallinformations,rangedisplayInH.intValue(), session, processAPI);
				
			}
			
			else if ("serverparams".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy Getinfo serverparam session=["+session+"]");
				answer =  com.bonitasoft.custompage.longboard.serverparams.ServerParams.getServerParamsInfo(session);				
			}
			else if ("monitoringprocess".equals(action))
			{
				logger.info("#### LongBoardCustomPage:Groovy monitoringProcesses");
				MonitorProcessInput monitorProcessInput =  MonitorProcessInput.getInstanceFromJsonSt( paramJson );
				answer  = MonitoringProcesses.monitorProcesses( monitorProcessInput, processAPI );				
			}
			
			if (answer!=null)
			{
				String jsonSt = JSONValue.toJSONString( answer );
				out.write( jsonSt );
				logger.info("#### LongBoardCustomPage:Groovy return json["+jsonSt+"]" );
				out.flush();
				out.close();
				return;
			}
			out.write( "Unknow command" );
			out.flush();
			out.close();
			return;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.severe("#### LongBoardCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
		}
	}

	
	/** -------------------------------------------------------------------------
	 *
	 * getCaseHistoryInJson
	 * 
	 */
	private String getCaseHistoryInJson( long processInstanceId, boolean showSubProcess, PageResourceProvider pageResourceProvider, ProcessAPI processAPI,IdentityAPI identityApi, CommandAPI commandAPI)
	{
		Logger logger= Logger.getLogger("org.bonitasoft.custompage.longboard.groovy");
		InputStream is = pageResourceProvider.getResourceAsStream("lib/CustomPageLongBoard-1.0.1.jar");
		
		
		String ping= com.bonitasoft.custompage.longboard.casehistory.CaseHistory.getPing();
		logger.info("#### LongBoardCustomPage:Groovy Ping"+ping);
		HashMap<String,Object> mapDetails = com.bonitasoft.custompage.longboard.casehistory.CaseHistory.getCaseDetails(processInstanceId, showSubProcess, is ,processAPI,identityApi,commandAPI);
	 
		String jsonDetailsSt = JSONValue.toJSONString( mapDetails );
		logger.info("#### LongBoardCustomPage:Groovy End return ["+mapDetails+"] ==>"+jsonDetailsSt);
		return jsonDetailsSt;
	}
	
	
	
	/** -------------------------------------------------------------------------
	 *
	 * testPerformance
	 *  
	 */
	private String testPerformance( RunQualification runQualification, ProcessAPI processAPI   )
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
	
	/** -------------------------------------------------------------------------
	 *
	 *getIntegerParameter
	 * 
	 */
	private int getIntegerParameter(HttpServletRequest request, String paramName, int defaultValue)
	{
		String valueParamSt = request.getParameter(paramName);
		if (valueParamSt==null  || valueParamSt.length()==0)
		{
			return defaultValue;
		}
		int valueParam=defaultValue;
		try
		{
			valueParam = Integer.valueOf( valueParamSt );
		}
		catch( Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			
			logger.severe("#### LongBoardCustomPage:Groovy LongBoard: getinteger : Exception "+e.toString()+" on  ["+valueParamSt+"] at "+exceptionDetails );
			valueParam= defaultValue;
		}
		return valueParam;
	}
	/** -------------------------------------------------------------------------
	 *
	 *getBooleanParameter
	 * 
	 */
	private boolean getBooleanParameter(HttpServletRequest request, String paramName, boolean defaultValue)
	{
		String valueParamSt = request.getParameter(paramName);
		if (valueParamSt==null  || valueParamSt.length()==0)
		{
			return defaultValue;
		}
		boolean valueParam=defaultValue;
		try
		{
			valueParam = Boolean.valueOf( valueParamSt );
		}
		catch( Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			
			logger.severe("#### LongBoardCustomPage:Groovy LongBoard: getBoolean : Exception "+e.toString()+" on  ["+valueParamSt+"] at "+exceptionDetails );
			valueParam= defaultValue;
		}
		return valueParam;
	}
	
	/** -------------------------------------------------------------------------
	 *
	 *runTheBonitaIndexDoGet
	 * 
	 */
	private void runTheBonitaIndexDoGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
				try {
						def String indexContent;
						pageResourceProvider.getResourceAsStream("index.html").withStream { InputStream s->
								indexContent = s.getText()
						}
						
						// def String pageResource="pageResource?&page="+ request.getParameter("page")+"&location=";
						// indexContent= indexContent.replace("@_USER_LOCALE_@", request.getParameter("locale"));
						// indexContent= indexContent.replace("@_PAGE_RESOURCE_@", pageResource);
						
						response.setCharacterEncoding("UTF-8");
						PrintWriter out = response.getWriter();
						out.print(indexContent);
						out.flush();
						out.close();
				} catch (Exception e) {
						e.printStackTrace();
				}
		}

}
