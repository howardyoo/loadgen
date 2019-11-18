package com.vmware.wavefront.loadgen;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.CompositeReporter;
import com.wavefront.opentracing.reporting.ConsoleReporter;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.entities.tracing.sampling.ConstantSampler;
import com.wavefront.sdk.proxy.WavefrontProxyClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@Component("traceutil")
public class TraceUtil {

  /* its own tracer */
  public Tracer tracer;

  public TraceUtil() {

  }

  @Value("${wf.proxy.enabled}")
  public boolean proxyEnabled;

  @Value("${wf.proxy.host}")
  public String proxyhost;

  @Value("${wf.proxy.port}")
  private String proxyport;

  @Value("${wf.trace.enabled}")
  public boolean traceEnabled;

  @Value("${wf.proxy.histogram.port}")
  private String histogramport;

  @Value("${wf.proxy.trace.port}")
  private String traceport;

  @Value("${wf.direct.enabled}")
  public boolean directEnabled;

  @Value("${wf.direct.server}")
  public String server;

  @Value("${wf.direct.token}")
  public String token;

  @Value("${wf.application}")
  public String application;

  @Value("${wf.service}")
  public String service;

  protected final static Logger logger = Logger.getLogger(TraceUtil.class);

  @PostConstruct
  public void init() {
    if(traceEnabled == true && application != null && service != null) {

      ApplicationTags appTags = new ApplicationTags.Builder(application, service).build();
      String hostname = "unknown";
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }

      WavefrontSender wavefrontSender = null;
      if(proxyEnabled == true) {
        wavefrontSender = new WavefrontProxyClient.Builder(proxyhost).
            metricsPort(Integer.parseInt(proxyport)).
            distributionPort(Integer.parseInt(histogramport)).tracingPort(Integer.parseInt(traceport)).build();
      } else if(directEnabled == true){
        wavefrontSender = new WavefrontDirectIngestionClient.Builder(server, token).build();
      }
      Reporter wfspanreporter = new WavefrontSpanReporter.Builder().withSource(hostname).build(wavefrontSender);
      Reporter consoleReporter = new ConsoleReporter(hostname);
      Reporter composite = new CompositeReporter(wfspanreporter, consoleReporter);
      logger.info("created new tracer with " + application + " : " + service);
      tracer = new WavefrontTracer.Builder(composite, appTags).withSampler(new ConstantSampler(true)).build();
    }
    else {
      logger.info("created new Global Tracer...");
      tracer = GlobalTracer.get();
    }
  }

  public Tracer getTracer() {
    return tracer;
  }
}
