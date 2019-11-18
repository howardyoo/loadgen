package com.vmware.wavefront.loadgen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import io.opentracing.Span;
import io.opentracing.Tracer;

@RestController
public class Controller extends HashMap {

  @Autowired
  private TraceUtil traceutil;

  public Controller() {
    super();
  }

  @PostConstruct
  public void init() {

  }

  @RequestMapping("/")
  public Response root() {
    return new Response("/", "dir", "['/cpu','/mem']");
  }

  @RequestMapping("/help")
  public Response help() {
    return new Response("help", "text", "Help messages are here..");
  }

  @RequestMapping("/cpu")
  public Response cpu() {
    return new Response("/cpu", "dir", "['/info','/run']");
  }

  @RequestMapping("/mem")
  public Response mem() {
    return new Response("/mem", "dir", "['/info','/run']");
  }

  /**
   * checks whether certain runners are currently running or not
   * @param key
   * @return
   */
  protected boolean isRunning(String key) {
    ArrayList gens = (ArrayList)get(key);
    if(gens != null && !gens.isEmpty()) {
      Iterator itr = gens.iterator();
      while(itr.hasNext()) {
        LoadGen gen = (LoadGen)itr.next();
        if(gen.isRun() == true) return true;
      }
    }
    return false;
  }

  private Tracer getTracer() {
    return traceutil.tracer;
  }

  @RequestMapping("/mem/info")
  public Response meminfo() {
    // get mem info

    Span span = getTracer().buildSpan("/mem/info").start();

    ArrayList<MemGen> gens = (ArrayList<MemGen>)get("/mem");
    String msg = "";
    if(isRunning("/mem")) {
      int duration = gens.get(0).getDuration();
      int size = gens.size();
      msg = String.format("{duration='%d', threads='%d', status='running'}", duration, size);
    } else {
      msg = "{status='not running'}";
    }

    span.finish();
    return new Response("/mem/info", "text", msg);
  }

  @RequestMapping("/cpu/info")
  public Response cpuinfo() {
    // get cpu info
    Span span = getTracer().buildSpan("/cpu/info").start();

    ArrayList<CPUGen> gens = (ArrayList<CPUGen>)get("/cpu");
    String msg = "";
    if(isRunning("/cpu")) {
      int duration = gens.get(0).getDuration();
      int size = gens.size();
      msg = String.format("{duration='%d', threads='%d'}", duration, size);
    } else {
      msg = "{status='not running'}";
    }

    span.finish();
    return new Response("/cpu/info", "text", msg);
  }

  @RequestMapping("/cpu/run")
  public Response cpurun(@RequestParam(value="threads", defaultValue="0") int threads,
                         @RequestParam(value="duration", defaultValue="0") int duration) {

    Span span = getTracer().buildSpan("/cpu/run").start();
    String msg = "";
    if(threads > 0) {
      ArrayList<CPUGen> gens = (ArrayList<CPUGen>)get("/cpu");
      if(gens == null) {
        gens = new ArrayList<>();
        put("/cpu", gens);
      }
      if(!isRunning("/cpu")) {
        gens.clear();
        for (int i = 0; i < threads; i++) {
          CPUGen gen = new CPUGen(duration, getTracer(), span);
          gens.add(gen);
          gen.start();
        }
        msg = String.format("{threads='%d', duration='%d', status='started'}", threads, duration);
      } else {
        duration = gens.get(0).getDuration();
        threads = gens.size();
        msg = String.format("{threads='%d', duration='%d', status='already running'}", threads, duration);
      }
    } else {
      msg = "need to specify two parameters, threads and duration.";
    }
    span.finish();
    return new Response("/cpu/run", "text", msg);
  }

  @RequestMapping("/cpu/stop")
  public Response cpustop() {
    Span span = getTracer().buildSpan("/cpu/stop").start();
    String msg = "";
    ArrayList<CPUGen> gens = (ArrayList<CPUGen>)get("/cpu");
    if(gens != null) {
      for(CPUGen gen : gens) {
        gen.endRun();
      }
      msg = String.format("{threads='%d', status='all stopped'}", gens.size());
    }
    else msg = "there is no running cpu load at this moment to stop.";
    span.finish();
    return new Response("/cpu/stop", "text", msg);
  }

  @RequestMapping("/mem/run")
  public Response memrun(@RequestParam(value="threads", defaultValue="0") int threads,
                         @RequestParam(value="duration", defaultValue="0") int duration) {
    Span span = getTracer().buildSpan("/mem/run").start();
    String msg = "";
    if(threads > 0) {

      ArrayList<MemGen> gens = (ArrayList<MemGen>)get("/mem");
      if(gens == null) {
        gens = new ArrayList<>();
        put("/mem", gens);
      }

      if(!isRunning("/mem")) {
        gens.clear();
        for (int i = 0; i < threads; i++) {
          MemGen gen = new MemGen(duration, getTracer(), span);
          gens.add(gen);
          gen.start();
        }
        msg = String.format("{threads='%d', duration='%d', status='started'}", threads, duration);
      } else {
        duration = gens.get(0).getDuration();
        threads = gens.size();
        msg = String.format("{threads='%d', duration='%d', status='already running'}", threads, duration);
      }
    } else {
      msg = "need to specify two parameters, threads and duration.";
    }
    span.finish();
    return new Response("/mem/run", "text", msg);
  }

  @RequestMapping("/mem/stop")
  public Response memstop() {
    Span span = getTracer().buildSpan("/mem/stop").start();
    String msg = "";
    ArrayList<MemGen> gens = (ArrayList<MemGen>)get("/mem");
    if(gens != null) {
      for(MemGen gen : gens) {
        gen.endRun();
      }
      msg = String.format("{threads='%d', status='all stopped'}", gens.size());
    }
    else msg = "there is no running mem load at this moment to stop.";
    span.finish();
    return new Response("/mem/stop", "text", msg);
  }
}
