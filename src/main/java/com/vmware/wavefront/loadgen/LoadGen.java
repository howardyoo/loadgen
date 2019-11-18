package com.vmware.wavefront.loadgen;

import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * main class for generating loads
 */
public abstract class LoadGen extends Thread {

  protected boolean run = false;
  long iteration = 0l;
  long starttime = 0l;
  long endtime = 0l;
  long currtime = 0l;
  int duration = 0;
  Span span;
  Tracer tracer;

  public LoadGen(int duration, Tracer tracer, Span span) {
    this.duration = duration;
    this.span = span;
    this.tracer = tracer;
  }

  protected abstract void runUnit() throws Exception;

  protected void clear() {

  }

  @Override
  public void run() {
    run = true;
    Span _span = tracer.buildSpan("LoadGen:run").asChildOf(span).start();
    long iteration = 0l;
    long starttime = System.currentTimeMillis();                  // log the start time
    long endtime = starttime + (duration * 1000l);               // time to terminate
    while(run == true) {
      try {
        // check time to see if it needs to stop
        if((currtime = System.currentTimeMillis()) < endtime) {
          runUnit();
          iteration++;
        } else {
          run = false;
        }
      }
      catch(Exception e) {
        // should report this exception
        e.printStackTrace();
      }
    }
    run = false;
    _span.finish();
    clear();
  }

  public boolean isRun() {
    return run;
  }

  public void endRun() {
    if(isRun()) {
      run = false;
    }
  }

  public long getIteration() {
    return iteration;
  }

  public long getStarttime() {
    return starttime;
  }

  public long getEndtime() {
    return endtime;
  }

  public long getCurrtime() {
    return currtime;
  }

  public int getDuration() {
    return duration;
  }
}
