package com.bonitasoft.custompage.longboard.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Test;

import com.bonitasoft.custompage.longboard.monitoring.MonitoringProcesses.MonitorProcessInput;

public class JunitMonitoringProcess {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<String, String>();
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
            APISession session = loginAPI.login(username, password);
            ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);

            MonitorProcessInput monitorProcessInput = new MonitorProcessInput();
            monitorProcessInput.defaultMaxItems = 5000;
            HashMap<String, Object> result = MonitoringProcesses.monitorProcesses(monitorProcessInput, processAPI);

        } catch (Exception e) {

        }
    }

}
