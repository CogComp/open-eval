import edu.illinois.cs.cogcomp.openeval.learner.Server
import edu.illinois.cs.cogcomp.openeval.learner.ServerPreferences

object Main {
  def main(args: Array[String]) = {
    val serverPreferences = new ServerPreferences(1000000, 25)
    val server = new Server(5757, serverPreferences, new SaulPosAnnotator())
    fi.iki.elonen.util.ServerRunner.executeInstance(server)
  }
}
