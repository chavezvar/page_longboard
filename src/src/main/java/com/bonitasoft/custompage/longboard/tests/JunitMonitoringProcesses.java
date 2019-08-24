package com.bonitasoft.custompage.longboard.tests;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
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

import com.bonitasoft.custompage.longboard.monitoring.MonitoringProcesses;
import com.bonitasoft.custompage.longboard.monitoring.MonitoringProcesses.MonitorProcessInput;

public class JunitMonitoringProcesses {

    @Test
    public void test() {
        APISession apiSession = login("http://localhost:8080", "bonita", "Walter.Bates", "bpm");
        if (apiSession == null) {
            fail("Can't login");
            return;
        }
        MonitorProcessInput monitoringPlatformInput = new MonitorProcessInput();
        ProcessAPI processAPI;
        try {
            processAPI = TenantAPIAccessor.getProcessAPI(apiSession);

            HashMap<String, Object> result = MonitoringProcesses.monitorProcesses(monitoringPlatformInput, processAPI);
            System.out.println("result=" + result);
        } catch (BonitaHomeNotSetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServerAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownAPITypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param applicationUrl
     * @param applicationName
     * @param userName
     * @param passwd
     * @return
     */
    public APISession login(String applicationUrl, String applicationName, String userName, String passwd) {
        try {
            // Define the REST parameters
            Map<String, String> map = new HashMap<String, String>();
            map.put("server.url", applicationUrl == null ? "http://localhost:8080" : applicationUrl);
            map.put("application.name", applicationName == null ? "bonita" : applicationName);
            APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, map);

            // Set the username and password
            // final String username = "helen.kelly";
            final String username = "walter.bates";
            final String password = "bpm";

            // get the LoginAPI using the TenantAPIAccessor
            LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();

            // log in to the tenant to create a session
            APISession session = loginAPI.login(username, password);
            return session;
        } catch (BonitaHomeNotSetException e) {
            e.printStackTrace();
            return null;
        } catch (ServerAPIException e) {
            e.printStackTrace();
            return null;
        } catch (UnknownAPITypeException e) {
            e.printStackTrace();
            return null;
        } catch (LoginException e) {
            e.printStackTrace();
            return null;
        }
    }
}
