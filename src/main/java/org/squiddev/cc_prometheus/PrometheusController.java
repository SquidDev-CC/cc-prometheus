package org.squiddev.cc_prometheus;

import com.google.common.base.Preconditions;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.squiddev.cc_prometheus.reporters.Reporter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrometheusController extends AbstractHandler {
    private final CollectorRegistry registry = new CollectorRegistry();
    private final List<Reporter> reporters = new ArrayList<>();

    public void addReporter(@Nonnull Reporter reporter) {
        Preconditions.checkNotNull(reporter, "reporter cannot be null");
        reporters.add(reporter);
        reporter.register(registry);
    }

    public void update() {
        for (Reporter reporter : reporters) reporter.update();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!target.equals("/metrics")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        for (Reporter reporter : reporters) {
            try {
                reporter.fetch();
            } catch (RuntimeException e) {
                CCPrometheus.log.error("Failed to update " + reporter.getClass().getName(), e);
            }
        }

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(TextFormat.CONTENT_TYPE_004);

            TextFormat.write004(response.getWriter(), registry.metricFamilySamples());

            baseRequest.setHandled(true);
        } catch (IOException e) {
            CCPrometheus.log.error("Failed to read PrometheusController statistics", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
