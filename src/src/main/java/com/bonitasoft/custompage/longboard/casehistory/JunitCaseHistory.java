package com.bonitasoft.custompage.longboard.casehistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Test;

import com.bonitasoft.custompage.longboard.casehistory.CaseHistory.CaseHistoryParameter;

public class JunitCaseHistory {

	@Test
	public void test() {
		final Map<String, String> map = new HashMap<String, String>();
		map.put("server.url", "http://localhost:8080");
		map.put("application.name", "bonita");
		APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, map);

		// Set the username and password
		// final String username = "helen.kelly";
		final String username = "walter.bates";
		final String password = "bpm";

		// get the LoginAPI using the TenantAPIAccessor
		LoginAPI loginAPI;
		try {
			loginAPI = TenantAPIAccessor.getLoginAPI();
			// log in to the tenant to create a session
			final APISession session = loginAPI.login(username, password);
			//final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			//final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
			//final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);


			final File commandFile = new File("target/CustomPageLongBoard-1.0.1.jar");
			FileInputStream fis;

			fis = new FileInputStream(commandFile);

			CaseHistoryParameter caseHistoryParameter = new CaseHistoryParameter();
			caseHistoryParameter.caseId = 1L;
			final Map<String, Object> caseDetails = CaseHistory.getCaseDetails(caseHistoryParameter, true, fis, session);
			System.out.println(caseDetails);
			System.out.println("-------------------- Synthesis");
			final List<HashMap<String, Object>> synthesis = (List<HashMap<String, Object>>) caseDetails.get("synthesis");
			if (synthesis != null) {
				for (final HashMap<String, Object> oneLine : synthesis) {
					System.out.println("State[" + oneLine.get("State") + "] name[" + oneLine.get("ActivityName") + "] exe[" + oneLine.get("DateBeginST") + " >> " + oneLine.get("DateEndST") + "] timeEnterConnector[" + oneLine.get("timeEnterConnector") + "] timeUser[" + oneLine.get("timeUser")
							+ "] timeFinishConnector[" + oneLine.get("timeFinishConnector") + "]");
				}
			}
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final BonitaHomeNotSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ServerAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final UnknownAPITypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
