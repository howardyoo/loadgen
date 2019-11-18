package com.vmware.wavefront.loadgen;
import static java.lang.StrictMath.tan;
import static java.lang.StrictMath.atan;
import io.opentracing.Span;
import io.opentracing.Tracer;

public class CPUGen extends LoadGen {

  public CPUGen(int duration, Tracer tracer, Span span) {
    super(duration, tracer, span);
  }

  /**
   * this is supposedly pretty expensive calculation
   * @throws Exception
   */
  protected void runUnit() throws Exception {
    double d = tan(atan(tan(atan(tan(atan(tan(atan(tan(atan(123456789.123456789))))))))));
  }
}
