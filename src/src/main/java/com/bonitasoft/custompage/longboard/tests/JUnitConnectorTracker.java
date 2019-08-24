package com.bonitasoft.custompage.longboard.tests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.Test;

import com.bonitasoft.custompage.longboard.connectortracker.TrackerAccess;

public class JUnitConnectorTracker {

    @Test
    public void test() {

        final APISession apiSession = login("http://localhost:8080", "bonita", "Walter.Bates", "bpm");
        if (apiSession == null) {
            fail("Can't login");
            return;
        }
        ProcessAPI processAPI;
        try {
            processAPI = TenantAPIAccessor.getProcessAPI(apiSession);

            TrackerAccess trackerAccess = new TrackerAccess();
            final Map<String, Object> param = trackerAccess.getInfos(false, true, 0, true, true, new ArrayList<FlushEventListener>(), apiSession, processAPI);

            System.out.println("PARAMETERS=" + param);
        } catch (final BonitaHomeNotSetException e) {
            e.printStackTrace();
        } catch (final ServerAPIException e) {
            e.printStackTrace();
        } catch (final UnknownAPITypeException e) {
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
    public APISession login(final String applicationUrl, final String applicationName, final String userName, final String passwd) {
        try {
            // Define the REST parameters
            final Map<String, String> map = new HashMap<String, String>();
            map.put("server.url", applicationUrl == null ? "http://localhost:8080" : applicationUrl);
            map.put("application.name", applicationName == null ? "bonita" : applicationName);
            APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, map);

            // Set the username and password
            // final String username = "helen.kelly";
            final String username = "walter.bates";
            final String password = "bpm";

            // get the LoginAPI using the TenantAPIAccessor
            final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();

            // log in to the tenant to create a session
            final APISession session = loginAPI.login(username, password);
            return session;
        } catch (final BonitaHomeNotSetException e) {
            e.printStackTrace();
            return null;
        } catch (final ServerAPIException e) {
            e.printStackTrace();
            return null;
        } catch (final UnknownAPITypeException e) {
            e.printStackTrace();
            return null;
        } catch (final LoginException e) {
            e.printStackTrace();
            return null;
        }
    }
}
