package edu.illinois.cs.cogcomp.openeval.learner;

/**
 * Holds preferences a edu.illinois.cs.cogcomp.openeval.learner.Server instance. This tells the core what will be the biggest batch to send.
 * It does not enforce the preferences, and if used outside of the Open-Eval system it might be ignored.
 */
public class ServerPreferences {

    private int maxAmountBytesAccepted;
    private int maxNumInstancesAccepted;

    /**
     * Creates preferences for a server.
     *
     * @param maxAmountBytesAccepted The maximum size, in bytes, that a batch can be
     * @param maxNumInstancesAccepted The maximum instances that a batch will have
     */
    public ServerPreferences(int maxAmountBytesAccepted, int maxNumInstancesAccepted){
        this.maxAmountBytesAccepted = maxAmountBytesAccepted;
        this.maxNumInstancesAccepted = maxNumInstancesAccepted;
    }

    public int getMaxAmountBytesAccepted() {
        return maxAmountBytesAccepted;
    }

    public int getMaxNumInstancesAccepted() {
        return maxNumInstancesAccepted;
    }
}
