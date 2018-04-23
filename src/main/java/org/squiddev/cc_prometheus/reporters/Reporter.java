package org.squiddev.cc_prometheus.reporters;

import io.prometheus.client.CollectorRegistry;

public interface Reporter {
    String NAMESPACE = "computercraft";

    void register(CollectorRegistry registry);

    default void update() {
    }

    default void fetch() {
    }
}
