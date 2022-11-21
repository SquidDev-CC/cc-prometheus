package cc.tweaked.prometheus;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    public static final ForgeConfigSpec.ConfigValue<String> host;
    public static final ForgeConfigSpec.ConfigValue<Integer> port;
    public static final ForgeConfigSpec.ConfigValue<Boolean> vanilla;
    public static final ForgeConfigSpec.ConfigValue<Boolean> jvm;

    public static final ForgeConfigSpec spec;

    static {
        var configBuilder = new ForgeConfigSpec.Builder();

        host = configBuilder
            .comment("The address the Prometheus exporter should be hosted on.")
            .define("host", "127.0.0.1");

        port = configBuilder
            .comment("The port the Prometheus exporter should be hosted on.")
            .defineInRange("port", 9226, 1, 65535);

        vanilla = configBuilder
            .comment("Whether to expose some metrics about the state of the vanilla server.")
            .worldRestart()
            .define("vanilla", false);

        jvm = configBuilder
            .comment("Whether to expose some metrics about the state of the Java runtime.")
            .worldRestart()
            .define("jvm", false);

        spec = configBuilder.build();
    }
}
