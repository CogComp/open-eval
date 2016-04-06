import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;
import controllers.Core;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import models.LearnerSettings;
import org.junit.Test;

import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

import java.sql.SQLException;
import java.sql.DriverManager;
/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class CoreTest {
	
    @Test
    public void invalidUrl() {
        System.out.println("Test Running");
        String url = "fakeurl:9000";
        String conf_id = "1";
        String record_id = "1";
        LearnerSettings status = null;
        try {
            status = Core.testConnection(url);
        } catch (Exception e) {
            if (e.getMessage().contains("The driver has not received any packets from the server.")) //In the case where we cannot connect to the DB. 
                return;
        }
        assertEquals(null, status);
    }
}
