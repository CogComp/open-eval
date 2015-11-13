import fi.iki.elonen.ServerRunner;

import java.io.IOException;

/**
 * Created by rnkelch on 11/7/2015.
 */
public class Main {

    public static void main(String args[]){
        String command = args[0];
        System.out.println(command);
        LearnerRunner learnerRunner = new LearnerRunner(new CommandMaker(command), new StreamReader());
        try {
            ServerRunner.executeInstance(new Server(5757, learnerRunner, new StreamReader()));
        } catch (IOException e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
    }
}
