package cc.tweaked.prometheus;

import cc.tweaked.prometheus.collectors.ComputerCollector;
import cc.tweaked.prometheus.collectors.ComputerFieldCollector;
import cc.tweaked.prometheus.collectors.ThreadGroupCollector;
import cc.tweaked.prometheus.collectors.VanillaCollector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ServerMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMetrics.class);

    private static HTTPServer server;
    private static List<Runnable> toTick;

    private ServerMetrics() {
    }

    public static void onServerStart(MinecraftServer server) {
        var collectorRegistry = new CollectorRegistry(true);
        var ticking = ServerMetrics.toTick = new ArrayList<>();
        var registry = new MetricContext(server, collectorRegistry, ticking::add);

        if (Config.computercraft.get()) {
            ComputerCollector.register(registry);
            ComputerFieldCollector.register(registry);
            ThreadGroupCollector.register(registry);
        }
        if (Config.vanilla.get()) VanillaCollector.export(registry);
        if (Config.jvm.get()) DefaultExports.register(collectorRegistry);

        if (!collectorRegistry.metricFamilySamples().hasMoreElements()) {
            LOG.warn("Warning: no collectors are enabled! Check the configuration.");
        }

        try {
            ServerMetrics.server = new HTTPServer.Builder()
                .withHostname(Config.host.get())
                .withPort(Config.port.get())
                .withDaemonThreads(true)
                .withRegistry(collectorRegistry)
                .build();

            LOG.info("Serving Prometheus metrics on http://{}:{}", Config.host.get(), Config.port.get());
        } catch (IOException e) {
            LOG.error("Failed to start HTTP server", e);
            ServerMetrics.server = null;
        }
    }

    public static void onServerStop() {
        if (server != null) server.close();
        server = null;
        toTick = null;

        LOG.info("Server stopped, no longer hosting metrics");
    }

    public static void onServerTick() {
        for (var action : toTick) action.run();
    }
}
