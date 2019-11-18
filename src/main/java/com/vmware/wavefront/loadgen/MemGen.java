package com.vmware.wavefront.loadgen;
import java.util.Vector;
import java.lang.Math;
import io.opentracing.Span;
import io.opentracing.Tracer;

public class MemGen extends LoadGen {

  protected Vector<String> mem;

  public MemGen(int duration, Tracer tracer, Span span) {
    super(duration, tracer, span);
    mem = new Vector<>();
  }

  /**
   * continuously increase the heap memory allocation by
   * allocating random size strings and pushing it into the hashmap.
   * @throws Exception
   */
  protected void runUnit() throws Exception {

    int randSize = (int)(Math.random() * 100) + 16;
    StringBuffer buff = new StringBuffer();
    for(int i=0; i < randSize; i++) {
      buff.append("0");
    }
    mem.add(buff.toString());
  }

  protected void clear() {
    mem.clear();
  }
}
