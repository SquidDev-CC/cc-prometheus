package org.squiddev.cc_prometheus;

import org.squiddev.cc_prometheus.reporters.ComputerReporter;
import org.squiddev.cc_prometheus.reporters.ManagementReporter;
import org.squiddev.cc_prometheus.reporters.ThreadGroupReporter;
import org.squiddev.cc_prometheus.reporters.TrackingReporter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ccprometheus")
public class CCPrometheus {
    public static final String MOD_ID = "cc-prometheus";
    public static Logger log = LogManager.getLogger();

    private Server server;
    private PrometheusController controller;

    private int port = 8189;
    private int maxThreads = 16;

    public void onServerStarted(FMLServerStartedEvent event) {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.error("Cannot stop prometheus server", e);
            }
        }

        controller = new PrometheusController();
        controller.addReporter(new ComputerReporter());
        controller.addReporter(new ManagementReporter());

        if (classExists("dan200.computercraft.core.tracking.Tracker")) {
            controller.addReporter(new TrackingReporter());
        }

        if (classExists("dan200.computercraft.shared.util.ThreadUtils")) {
            controller.addReporter(new ThreadGroupReporter());
        }

        // Setup pool with a maximum number of threads and a friendlier name.
        ThreadGroup group = new ThreadGroup(MOD_ID + " ThreadPool");

        server = new Server();

        // Setup connector to listen on the specific port.
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        // Register some handlers for logging and requests
        server.setHandler(controller);
        server.setRequestLog((request, response) -> {
            if (response.getStatus() != 200) {
                log.error(String.format("Request to '%s' returned status %s: %s",
                        request.getRequestURL(), response.getStatus(), response.getReason()));
            }
        });

        try {
            server.start();
        } catch (Exception e) {
            log.error("Cannot start prometheus server", e);

            server = null;
            controller = null;
        }
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.error("Cannot stop prometheus server", e);
            }

            server = null;
            controller = null;
        }
    }

    public void onTick(TickEvent.ServerTickEvent event) {
        if (controller != null) {
            controller.update();
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, CCPrometheus.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public CCPrometheus() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }
}
