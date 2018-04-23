package org.squiddev.cc_prometheus.reporters;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ServerComputer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

public class ComputerReporter implements Reporter {
    private final Gauge totalComputers = Gauge.build()
        .namespace(NAMESPACE)
        .name("computers_total")
        .help("Total number of loaded computers")
        .create();

    private final Gauge onComputers = Gauge.build()
        .namespace(NAMESPACE)
        .name("computers_on")
        .help("Total number of computers which are running")
        .create();

    @Override
    public void register(CollectorRegistry registry) {
        totalComputers.register(registry);
        onComputers.register(registry);
    }

    @Override
    public void update() {
        int total = 0, on = 0;
        for (ServerComputer computer : ComputerCraft.serverComputerRegistry.getComputers()) {
            total++;
            if (computer.isOn()) on++;
        }

        totalComputers.set(total);
        onComputers.set(on);
    }
}
