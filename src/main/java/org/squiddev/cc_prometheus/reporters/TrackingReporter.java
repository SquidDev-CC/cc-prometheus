package org.squiddev.cc_prometheus.reporters;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.tracking.Tracker;
import dan200.computercraft.core.tracking.Tracking;
import dan200.computercraft.core.tracking.TrackingField;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;

import java.util.HashMap;
import java.util.Map;

public class TrackingReporter implements Reporter, Tracker {
    private final Map<TrackingField, Summary> summaries = new HashMap<>();
    private final Summary taskSummary;
    private final Summary serverSummary;

    public TrackingReporter() {
        for (TrackingField field : TrackingField.fields().values()) {
            summaries.put(field, Summary.build()
                .namespace(NAMESPACE)
                .name(field.id())
                .help(field.displayName())
                .labelNames("computer_id")
                .create());
        }

        taskSummary = summaries.get(TrackingField.TASKS);
        serverSummary = summaries.get(TrackingField.SERVER_TIME);

        // Remove additional timing information: this can be observed from the graph.
        summaries.remove(TrackingField.AVERAGE_TIME);
        summaries.remove(TrackingField.MAX_TIME);
        summaries.remove(TrackingField.TOTAL_TIME);
        summaries.remove(TrackingField.SERVER_COUNT);
    }

    @Override
    public void register(CollectorRegistry registry) {
        for (Summary gauge : summaries.values()) gauge.register(registry);

        Tracking.add(this);
    }

    @Override
    public void addTaskTiming(Computer computer, long time) {
        taskSummary.labels("computer_" + computer.getID()).observe(time / 1e9);
    }

    @Override
    public void addServerTiming(Computer computer, long time) {
        serverSummary.labels("computer_" + computer.getID()).observe(time / 1e9);
    }

    @Override
    public void addValue(Computer computer, TrackingField field, long count) {
        summaries.get(field).labels("computer_" + computer.getID()).observe(count);
    }
}
