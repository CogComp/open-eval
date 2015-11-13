import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rnkelch on 11/8/2015.
 */
public class LearnerRunnerTest {

    @Test
    public void testRunCommandSuccess() throws Exception {
        CommandMaker maker = mock(CommandMaker.class);
        when(maker.makeCommand("json")).thenReturn("cmd /c echo json");
        StreamReader reader = new StreamReader();

        LearnerRunner runner = new LearnerRunner(maker, reader);
        LearnerResult result = runner.runCommand("json");

        assertEquals("json", result.result);
        assertEquals(true, result.successful);
        assertEquals(null, result.errorMessage);
    }

    @Test
    public void testRunCommandError() throws Exception {
        CommandMaker maker = mock(CommandMaker.class);
        when(maker.makeCommand("json")).thenReturn("cmd /c test.bat");
        StreamReader reader = new StreamReader();

        LearnerRunner runner = new LearnerRunner(maker, reader);
        LearnerResult result = runner.runCommand("json");

        assertEquals(null, result.result);
        assertEquals(false, result.successful);
        assertEquals("error", result.errorMessage);
    }
}