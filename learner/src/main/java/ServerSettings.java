/**
 * Holds settings a Server instance.
 */
public class ServerSettings {

    private int maxAmountBytesAccepted;
    private int maxNumInstancesAccepted;

    /**
     * Creates settings for a server.
     *
     * @param maxAmountBytesAccepted The maximum size, in bytes, that a batch can be
     * @param maxNumInstancesAccepted The maximum instances that a batch will have
     */
    public ServerSettings(int maxAmountBytesAccepted, int maxNumInstancesAccepted){
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
