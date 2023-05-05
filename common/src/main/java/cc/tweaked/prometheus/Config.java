package cc.tweaked.prometheus;

public record Config(String host, int port, boolean vanilla, boolean jvm) {
    public static final String HOST_DEFAULT = "127.0.0.1";
    public static final String HOST_HELP = "The address the Prometheus exporter should be hosted on.";

    public static final int PORT_DEFAULT = 9226;
    public static final String PORT_HELP = "The port the Prometheus exporter should be hosted on.";
    public static final int PORT_MIN = 1;
    public static final int PORT_MAX = 65535;

    public static final boolean VANILLA_DEFAULT = false;
    public static final String VANILLA_HELP = "Whether to expose some metrics about the state of the vanilla server.";

    public static final boolean JVM_DEFAULT = false;
    public static final String JVM_HELP = "Whether to expose some metrics about the state of the Java runtime.";
}
