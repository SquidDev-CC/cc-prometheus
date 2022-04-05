package cc.tweaked.prometheus.collectors;

import cc.tweaked.prometheus.MetricContext;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import io.prometheus.client.Gauge;

import static cc.tweaked.prometheus.Constants.NAMESPACE;

/**
 * Counts the number of computers registered in the {@link ServerComputerRegistry}.
 */
public class ComputerCollector {
    public static void register(MetricContext context) {
        var totalComputers = Gauge.build()
            .namespace(NAMESPACE)
            .name("computers_total")
            .help("Total number of loaded computers")
            .register(context.registry());

        var onComputers = Gauge.build()
            .namespace(NAMESPACE)
            .name("computers_on")
            .help("Total number of computers which are running")
            .register(context.registry());

        context.onTick(() -> {
            int total = 0, on = 0;
            for (var computer : ComputerCraft.serverComputerRegistry.getComputers()) {
                total++;
                if (computer.isOn()) on++;
            }

            totalComputers.set(total);
            onComputers.set(on);
        });
    }
}
