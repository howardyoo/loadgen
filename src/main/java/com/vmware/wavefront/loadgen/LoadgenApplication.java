package com.vmware.wavefront.loadgen;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.wavefront.WavefrontConfig;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.time.Duration;

@Component
@EnableAutoConfiguration
@SpringBootApplication
public class LoadgenApplication {

	static WavefrontConfig config = null;
	static MeterRegistry registry = null;

	protected final static String name = "loadgen-app";
	public static String getName() {
		return name;
	}

	@Value("${wf.proxy.enabled}")
	private boolean proxyEnabled;
	@Value("${wf.proxy.host}")
	private String proxyhost;
	@Value("${wf.proxy.port}")
	private String proxyport;
	@Value("${wf.prefix}")
	private String prefix;
	@Value("${wf.duration}")
	private String duration;

	protected final static Logger logger = Logger.getLogger(LoadgenApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AppConfig.class);
		app.run(LoadgenApplication.class, args);
	}

	@PostConstruct
	public void init()
	{
		if(proxyEnabled == true) {
			// setup metric configuration.
			config = new WavefrontConfig() {
				@Override
				public String uri() {
					String servicename = EnvUtil.getProxyServiceName();
					if (servicename != null && servicename.trim().length() > 0) {
						String[] hostport = EnvUtil.getProxyHostPort(servicename);
						proxyhost = hostport[0];
						proxyport = hostport[1];
					}
					logger.info("Using proxy as :" + proxyhost + ":" + proxyport);
					return "proxy://" + proxyhost + ":" + proxyport;
				}

				@Override
				public String source() {
					String address = EnvUtil.getInstanceAddress();
					if (address != null && address.trim().length() > 0) {
						return address.replaceAll(":", "_");
					} else {
						String hostname = null;
						try {
							hostname = InetAddress.getLocalHost().getHostName();
							return hostname;
						} catch (Exception e) {
							logger.error(e);
						}
					}
					logger.info("hostname unknown");
					return "unknown";
				}

				@Override
				public String get(String key) {
					// defaults everything else
					return null;
				}

				@Override
				public Duration step() {
					// 10 seconds reporting interval
					if (duration != null) {
						return Duration.ofSeconds(Integer.parseInt(duration));
					} else {
						return Duration.ofSeconds(10);
					}
				}

				@Override
				public String prefix() {
					return prefix;
				}

				@Override
				public String globalPrefix() {
					return prefix;
				}
			};

			// create a new registry
			registry = new WavefrontMeterRegistry(config, Clock.SYSTEM);

			// default JVM stats
			new ClassLoaderMetrics().bindTo(registry);
			new JvmMemoryMetrics().bindTo(registry);
			new JvmGcMetrics().bindTo(registry);
			new ProcessorMetrics().bindTo(registry);
			new JvmThreadMetrics().bindTo(registry);
			new FileDescriptorMetrics().bindTo(registry);
			new UptimeMetrics().bindTo(registry);

			// setup common tags - this time, app name, uri, and guid
			if (EnvUtil.getProxyServiceName() != null) {
				registry.config().commonTags("instance_guid", EnvUtil.getInstanceGuid(), "app_name", EnvUtil.getAppName());
			}
		}
	}

	public LoadgenApplication() {

	}

	public static MeterRegistry getMeterRegistry() {
		return registry;
	}

	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	public String getProxyAddress() {
		return proxyhost + ":" + proxyport;
	}
}
