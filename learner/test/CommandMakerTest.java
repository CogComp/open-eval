
import org.junit.Assert;

import static org.junit.Assert.*;

/**
 * Created by rnkelch on 11/8/2015.
 */
public class CommandMakerTest {

    @org.junit.Test
    public void testMakeCommand() throws Exception {
        CommandMaker test = new CommandMaker("before{json}after");
        String result = test.makeCommand(" test ");
        assertEquals("before test after", result);
    }
}