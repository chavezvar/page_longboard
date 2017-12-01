package com.bonitasoft.custompage.longboard.qualification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.GatewayDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.custompage.longboard.qualification.QualificationPlatform.Sample;
import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;

public class QualificationPlatform {

	public final static String loggerName = "com.bonitasoft.custompage.longboard.qualification";

	/**
	 * @param listLabels
	 * @param listValues
	 * @return
	 */
	public static class Sample {

		public String code;
		public String label;
		public long baseInMs;
		public long timeInMs;

		public static Sample getSample(String code, String label, long baseInMs, long timeInMs) {
			Sample oneSample = new Sample();
			oneSample.code = code;
			oneSample.label = label;
			oneSample.baseInMs = baseInMs;
			oneSample.timeInMs = timeInMs;
			return oneSample;
		}
	}

	/**
	 * ----------------------------------------------------------------
	 * testQualificationWrite
	 * 
	 * @return
	 */
	public String testQualificationWrite(String filenameToWrite, ArrayList<QualificationPlatform.Sample> listSamples) throws FileNotFoundException {
		Logger logger = Logger.getLogger(loggerName);

		// write a BIG file
		String content = "1234567890";

		logger.info("#### 1 Write 100 Mb file [" + filenameToWrite + "]");

		long begWrite = System.currentTimeMillis();
		PrintWriter outFile = new PrintWriter(new FileOutputStream(filenameToWrite));
		// write 100 Mb
		for (int i = 0; i < 1024 * 1024 * 10; i++)
			outFile.write(content);
		outFile.close();
		long endWrite = System.currentTimeMillis();

		// 100% is 550 ms
		/*
		 * long baseWriteTimeInMs=550; long timeWriteTimeInMs = endWrite -
		 * begWrite; mapTest.put("BonitaHomeWriteBASE", baseWriteTimeInMs);
		 * mapTest.put("BonitaHomeWriteMS", timeWriteTimeInMs);
		 * mapTest.put("BonitaHomeWriteFACTOR", getFactor(timeWriteTimeInMs,
		 * baseWriteTimeInMs));
		 * logger.info("#### 1 Write 100 Mb file done in "+(timeWriteTimeInMs)+
		 * " ms - factor is "+getFactor(timeWriteTimeInMs, baseWriteTimeInMs) );
		 */
		populate(listSamples, "BonitaHomeWrite", "Write BonitaHome", 550, endWrite - begWrite, "BonitaHome Write", logger);
		return "";

	}

	/**
	 * ----------------------------------------------------------------
	 * testQualificationRead
	 * 
	 * @return
	 */
	public String testQualificationRead(String filenameToRead, ArrayList<QualificationPlatform.Sample> listSamples) throws IOException {
		Logger logger = Logger.getLogger(loggerName);
		// read the big file
		byte[] buffer = new byte[1000];
		logger.info("#### 2. Start Read");

		long endWrite = System.currentTimeMillis();
		FileInputStream inFile = new FileInputStream(filenameToRead);
		int nbRead = 0;
		while ((nbRead = inFile.read(buffer)) >= 0) {
		}
		;
		inFile.close();
		long endRead = System.currentTimeMillis();
		// 100% is 200 ms
		/*
		 * long baseReadTimeInMs=200; long timeReadTimeInMs = endRead -
		 * endWrite; mapTest.put("BonitaHomeReadBASE", baseReadTimeInMs);
		 * mapTest.put("BonitaHomeReadMS", timeReadTimeInMs);
		 * mapTest.put("BonitaHomeReadFACTOR",
		 * getFactor(timeReadTimeInMs,baseReadTimeInMs));
		 * logger.info("#### 2 Read 100 Mb file done in "
		 * +(timeReadTimeInMs)+" ms - factor is "+getFactor(timeReadTimeInMs,
		 * baseReadTimeInMs) );
		 */
		populate(listSamples, "BonitaHomeRead", "Read BonitaHome", 200, endRead - endWrite, "Read 100 Mb ", logger);
		return "";

	}

	/**
	 * ----------------------------------------------------------------
	 * testQualificationDelete
	 * 
	 * @return
	 */
	public String testQualificationDelete(String filenameToDelete, ArrayList<QualificationPlatform.Sample> listSamples) {
		// now delete it
		File fileToDelete = new File(filenameToDelete);
		fileToDelete.delete();
		return "";
	}

	/**
	 * ----------------------------------------------------------------
	 * testQualificationDatabase
	 * 
	 * @return
	 */
	public String testQualificationDatabase(ArrayList<QualificationPlatform.Sample> listSamples) {
		Logger logger = Logger.getLogger(loggerName);
		// Now access the database
		logger.info("#### 2. Start ReadStart Database test-2");

		Context ctx;
		Connection con = null;
		try {
			ctx = new InitialContext();

			con = LongboardToolbox.getConnection();

			logger.info("#### 2.a getConnection");

			DatabaseMetaData databaseMetaData = con.getMetaData();

			long begMetadata = System.currentTimeMillis();

			for (int i = 0; i < 100; i++) {
				// logger.info("Database test "+i+"/100 - time "+
				// (System.currentTimeMillis()-begDatabase)+" ms");

				ResultSet result = databaseMetaData.getTables(null, null, null, null);
			}
			long endMetadata = System.currentTimeMillis();
			populate(listSamples, "DataMeta", "Read Medata", 370, endMetadata - begMetadata, "Database test - Meta data", logger);

			// run a query
			logger.info("#### 2.b run a query");
			long begDatabase = System.currentTimeMillis();
			Statement stmt = con.createStatement();
			for (int i = 0; i < 1000; i++) {
				stmt.execute("SELECT * FROM PROCESS_DEFINITION");
			}

			long endDatabase = System.currentTimeMillis();
			populate(listSamples, "Database", "Sql Request", 190, endDatabase - begDatabase, "Database test- SqlRequest", logger);

			logger.info("#### 2.c End the test");
			con.close();
			con = null;
		} catch (NamingException e) {
			logger.info("#### Error on testQualificationDatabase " + e.toString());
			return ";During database " + e.toString();
		} catch (SQLException e) {
			logger.info("#### Error on testQualificationDatabase " + e.toString());
			return ";During database " + e.toString();
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {

				}
		}
		logger.info("#### 2.d connection close.");
		return "";
	}

	/**
	 * ----------------------------------------------------------------
	 * testQualificationDatabase
	 * 
	 * @return
	 */
	private static String cstProcessTestName = "LongBoardTestQualification";
	private static String cstProcessTestVersion = "1.0";
	private static String cstProcessVariableDaysOff = "daysoff";
	private static String cstProcessVariablePersonName = "personname";
	private static String cstProcessVariableVacationApproval = "vacationapproval";
	private static String cstProcessVariableVacationDate = "vacationdate";

	private static String cstProcessActivityAskVacationStartEvent = "AskVacation";
	private static String cstProcessActivitySubmitVacationTaskName = "submitVacationRequest";
	private static String cstProcessActivityValidateRequestTaskName = "ValidateRequest";
	private static String cstProcessActivityNotifyApprovalTaskName = "NotifyApproval";
	private static String cstProcessActivityGoodByApprovalEndEvent = "GoodByeApproval";

	public String testQualificationProcess(ArrayList<QualificationPlatform.Sample> listSamples, int numberOfCases, ProcessAPI processAPI) {
		Logger logger = Logger.getLogger(loggerName);

		try {
			logger.info("#### 3. Start QualificationProcess");

			// already exist ?
			Long processId = null;
			if (!purgeProcess(cstProcessTestName, cstProcessTestVersion, processAPI)) {
				return "Error during purge process [" + cstProcessTestName + "] Version[" + cstProcessTestVersion + "]";
			}

			logger.info("#### 3.a processpurged, start deployment");

			ProcessDefinitionBuilder design = new ProcessDefinitionBuilder().createNewInstance(cstProcessTestName, cstProcessTestVersion);

			// Define the actors of the process:

			// Add data at process level:
			design.addIntegerData(cstProcessVariableDaysOff, null);
			design.addShortTextData(cstProcessVariablePersonName, null);
			design.addBooleanData(cstProcessVariableVacationApproval, new ExpressionBuilder().createConstantBooleanExpression(true));
			design.addDateData(cstProcessVariableVacationDate, null);

			// Design the tasks, gateway, and transitions:

			design.addStartEvent(cstProcessActivityAskVacationStartEvent).addAutomaticTask(cstProcessActivitySubmitVacationTaskName).addAutomaticTask(cstProcessActivityValidateRequestTaskName).addAutomaticTask(cstProcessActivityNotifyApprovalTaskName)
					.addEndEvent(cstProcessActivityGoodByApprovalEndEvent);

			design.addTransition(cstProcessActivityAskVacationStartEvent, cstProcessActivitySubmitVacationTaskName);
			design.addTransition(cstProcessActivitySubmitVacationTaskName, cstProcessActivityValidateRequestTaskName);
			design.addTransition(cstProcessActivityValidateRequestTaskName, cstProcessActivityNotifyApprovalTaskName);
			design.addTransition(cstProcessActivityNotifyApprovalTaskName, cstProcessActivityGoodByApprovalEndEvent);

			long begDeploy = System.currentTimeMillis();

			// Deploy the newly designed process:
			ProcessDefinition processDefinition = processAPI.deploy(design.done());
			processId = processDefinition.getId();
			processAPI.enableProcess(processId);

			long endDeploy = System.currentTimeMillis();

			populate(listSamples, "ProcessDeploy", "Deploy process", 40, endDeploy - begDeploy, "Deploy a process ", logger);
			logger.info("#### 3.b Start process running for [" + numberOfCases + "] cases");

			// now, create 100 case to process all
			// processId =
			// processAPI.getProcessDefinitionId("TestQualification", "1.0");
			// -------------------------------------- create
			List<Operation> listOperations = new ArrayList<Operation>();
			Map<String, Serializable> listVariablesSerializable = new HashMap<String, Serializable>();

			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put(cstProcessVariableDaysOff, 12);
			variables.put(cstProcessVariablePersonName, "Walter.Bates");
			variables.put(cstProcessVariableVacationApproval, Boolean.TRUE);
			variables.put(cstProcessVariableVacationDate, new Date());

			for (String variableName : variables.keySet()) {

				if (variables.get(variableName) == null || (!(variables.get(variableName) instanceof Serializable)))
					continue;
				Object value = variables.get(variableName);
				Serializable valueSerializable = (Serializable) value;

				variableName = variableName.toLowerCase();
				Expression expr = new ExpressionBuilder().createExpression(variableName, variableName, value.getClass().getName(), ExpressionType.TYPE_INPUT);
				Operation op = new OperationBuilder().createSetDataOperation(variableName, expr);
				listOperations.add(op);
				listVariablesSerializable.put(variableName, valueSerializable);
			}
			long begCreate = System.currentTimeMillis();
			for (int i = 0; i < numberOfCases; i++) {
				processAPI.startProcess(processId, listOperations, listVariablesSerializable);
			}
			long endCreate = System.currentTimeMillis();

			logger.info("#### 3.c cases  [" + numberOfCases + "] created in [" + (endCreate - begCreate) + "]");

			// wait for the end of all management !
			SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);

			searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processId);

			boolean isFinish = false;
			while (!isFinish) {
				// still case present ?
				SearchResult<ProcessInstance> listProcessInstance = processAPI.searchProcessInstances(searchOptionsBuilder.done());

				if (listProcessInstance.getCount() > 20) {
					Thread.sleep(50);
				}
				isFinish = listProcessInstance.getCount() == 0;
			}
			long endRun = System.currentTimeMillis();
			logger.info("#### 3.d all cases  [" + numberOfCases + "] processed in [" + (begCreate - endRun) + "]");

			populate(listSamples, "ProcessCreate", "Create " + numberOfCases + " cases", (numberOfCases == 100 ? 3600 : 0), endCreate - begCreate, "Create cases", logger);
			populate(listSamples, "ProcessRun", "Process " + numberOfCases + " cases", (numberOfCases == 100 ? 3700 : 0), endRun - begCreate, "Process run", logger);
			/*
			 * processAPI.disableProcess(processId);
			 * processAPI.deleteProcessInstances( processId, 0,
			 * Integer.MAX_VALUE); processAPI.deleteArchivedProcessInstances(
			 * processId, 0, Integer.MAX_VALUE);
			 * processAPI.deleteProcessDefinition(processId);
			 */
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			logger.severe("Error running process running" + e.toString() + "] at " + sw.toString());
			return ";During TestProcess " + e.toString();
		}

		// delete the process at the end
		purgeProcess(cstProcessTestName, cstProcessTestVersion, processAPI);

		return "";
	}

	private boolean purgeProcess(String processName, String processVersion, ProcessAPI processAPI) {
		Logger logger = Logger.getLogger(loggerName);

		try {
			Long processId = processAPI.getProcessDefinitionId(processName, processVersion);
			try {
				processAPI.disableProcess(processId);
			} catch (ProcessActivationException ae) {
			}
			;
			processAPI.deleteProcessInstances(processId, 0, Integer.MAX_VALUE);
			processAPI.deleteArchivedProcessInstances(processId, 0, Integer.MAX_VALUE);
			processAPI.deleteProcessDefinition(processId);
			return true;
		} catch (ProcessDefinitionNotFoundException e) {
			// do nothing
			return true;
		} catch (Exception e1) {
			StringWriter sw = new StringWriter();
			e1.printStackTrace(new PrintWriter(sw));

			logger.info("Error during purge process[" + e1.toString() + "] at " + sw);
			return false;
		}
	}

	/**
	 * ---------------------------------------------------------------- run all
	 * tests
	 * 
	 * @return
	 */
	public static class RunQualification {

		public int numberOfCases = 100;
		public String pathDirectoryToWrite;
		public boolean runDirectoryWrite = true;
		public boolean runProcess = true;
		public boolean runDatabase = false;
	}

	public static HashMap<String, Object> allQualifications(String pathDirectoryToWrite, boolean runProcess) {
		return allQualifications(pathDirectoryToWrite, runProcess, null);
	}

	public static HashMap<String, Object> allQualifications(String pathDirectoryToWrite, boolean runProcess, ProcessAPI processAPI) {
		RunQualification runQualification = new RunQualification();
		runQualification.pathDirectoryToWrite = pathDirectoryToWrite;
		runQualification.runProcess = runProcess;
		return allQualifications(runQualification, processAPI);
	}

	public static HashMap<String, Object> allQualifications(RunQualification runQualification, ProcessAPI processAPI) {

		QualificationPlatform testQualificationPlatform = new QualificationPlatform();
		Logger logger = Logger.getLogger(loggerName);
		logger.info("Start QualifirtcationTest processAPI [" + processAPI + "] class[" + (processAPI != null ? processAPI.getClass().getName() : "null") + "]");
		HashMap<String, Object> mapTest = new HashMap<String, Object>();
		mapTest.put("errormessage", "");
		try {
			ArrayList<QualificationPlatform.Sample> listSamples = new ArrayList<QualificationPlatform.Sample>();
			String msg = "";
			String filenameToWrite = runQualification.pathDirectoryToWrite + "-testPerformance.txt";
			if (runQualification.runDirectoryWrite) {
				msg += testQualificationPlatform.testQualificationWrite(filenameToWrite, listSamples);
				msg += testQualificationPlatform.testQualificationRead(filenameToWrite, listSamples);
				msg += testQualificationPlatform.testQualificationDelete(filenameToWrite, listSamples);
			}
			if (runQualification.runDatabase)
				msg += testQualificationPlatform.testQualificationDatabase(listSamples);
			if (processAPI != null && runQualification.runProcess)
				msg += testQualificationPlatform.testQualificationProcess(listSamples, runQualification.numberOfCases, processAPI);

			logger.info("#### tests OK, prepare answer");
			mapTest.put("errormessage", msg);

			for (QualificationPlatform.Sample sample : listSamples) {
				mapTest.put(sample.code + "LABEL", sample.label);
				mapTest.put(sample.code + "BASE", sample.baseInMs);
				mapTest.put(sample.code + "MS", sample.timeInMs);
				mapTest.put(sample.code + "FACTOR", getFactor(sample.baseInMs, sample.timeInMs));
			}
			String valueChart = QualificationGraphDisplay.getSampleBarChart("Performance Measure", listSamples);
			// logger.info("Return CHART>>" + valueChart + "<<");

			mapTest.put("chartObject", valueChart);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			logger.info("#### Error during performance test[" + e.toString() + "] at " + sw);

			mapTest.put("errormessage", e.toString());
		}
		logger.info("#### Finish all tests");
		return mapTest;

	}

	/**
	 * @param mapTest
	 * @param label
	 * @param baseInMs
	 * @param timeInMs
	 */
	private void populate(ArrayList<QualificationPlatform.Sample> listSamples, String code, String label, long baseInMs, long timeInMs, String logLabel, Logger logger) {
		logger.info("#### " + logLabel + " in " + timeInMs + " (base " + baseInMs + ") ms - factor is " + getFactor(baseInMs, timeInMs));
		listSamples.add(QualificationPlatform.Sample.getSample(code, label, baseInMs, timeInMs));
	}

	/**
	 * -------------------------------------------------------------------------
	 * getFactor
	 */
	private static double getFactor(long base, long value) {
		if (base == 0)
			return 0;
		int factor = (int) ((double) 10 * ((double) base) / (((double) value)));
		return ((double) factor) / 10;
	}

}
