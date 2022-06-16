/**
* Copyright 2017 Pivotal Software, Inc.
* <p>
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ai.foremast.micrometer.autoconfigure;

import ai.foremast.micrometer.autoconfigure.export.prometheus.PrometheusProperties;
import ai.foremast.micrometer.autoconfigure.export.prometheus.PrometheusPropertiesConfigAdapter;
import io.micrometer.core.instrument.Clock;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 *
 * @author Jon Schneider
 */
@Configuration
public class MetricsAutoConfiguration {
    @Bean
    public Clock micrometerClock() {
        return Clock.SYSTEM;
    }

    @Bean
    public static MeterRegistryPostProcessor meterRegistryPostProcessor() {
        return new MeterRegistryPostProcessor();
    }

    @Bean
    public MetricsProperties metricsProperties() {
        return new MetricsProperties();
    }


    @Autowired
    private PrometheusProperties prometheusProperties;

    @Bean
    public PrometheusProperties prometheusProperties() {
        return prometheusProperties = new PrometheusProperties();
    }

    @Bean
    @Order(0)
    public PropertiesMeterFilter propertiesMeterFilter(MetricsProperties properties) {
        return new PropertiesMeterFilter(properties);
    }


    @Bean
    public PrometheusConfig prometheusConfig() {
        return new PrometheusPropertiesConfigAdapter(prometheusProperties);
    }

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry(PrometheusConfig config, CollectorRegistry collectorRegistry,
                                                           Clock clock) {
        return new PrometheusMeterRegistry(config, collectorRegistry, clock);
    }

    @Bean
    public CollectorRegistry collectorRegistry() {
        return new CollectorRegistry(true);
    }

    @Bean
    @Order(0)
    public MeterFilter metricsHttpServerUriTagFilter(MetricsProperties properties) {
        String metricName = properties.getWeb().getServer().getRequestsMetricName();
        MeterFilter filter = new OnlyOnceLoggingDenyMeterFilter(() -> String
                .format("Reached the maximum number of URI tags for '%s'.", metricName));
        return MeterFilter.maximumAllowableTags(metricName, "uri",
                properties.getWeb().getServer().getMaxUriTags(), filter);
    }

    @Bean
    public CompositeMeterRegistry noOpMeterRegistry(Clock clock) {
        return new CompositeMeterRegistry(clock);
    }

    @Bean
    @Primary
    public CompositeMeterRegistry compositeMeterRegistry(Clock clock, List<MeterRegistry> registries) {
        return new CompositeMeterRegistry(clock, registries);
    }

    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Bean
    public FileDescriptorMetrics fileDescriptorMetrics() {
        return new FileDescriptorMetrics();
    }


    //JVM
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

}
