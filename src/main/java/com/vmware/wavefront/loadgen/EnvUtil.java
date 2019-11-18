package com.vmware.wavefront.loadgen;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * class to extract env variables and make it available.
 */
public class EnvUtil {

  public static final String vcapservices = "VCAP_SERVICES";
  public static final String vcapapplication = "VCAP_APPLICATION";
  public static final String wavefrontservice = "wavefront-proxy";
  protected final static Logger logger = Logger.getLogger(EnvUtil.class);

  public static String getInstanceAddress() {
    return System.getenv("CF_INSTANCE_ADDR");
  }

  public static String getInstanceGuid() {
    return System.getenv("CF_INSTANCE_GUID");
  }

  public static String getAppName() {
    String app = System.getenv(vcapapplication);
    if (app == null || app.length() == 0) {
      logger.error("VCAP_APPLICATION environment is unavailable. Please verify whether VCAP_APPLICATION is available in the system environment.");
    } else {
      JSONObject json = new JSONObject(app);
      String value = json.getString("application_name");
      return value;
    }
    return null;
  }

  public static String getAppId() {
    String app = System.getenv(vcapapplication);
    if (app == null || app.length() == 0) {
      logger.error("VCAP_APPLICATION environment is unavailable. Please verify whether VCAP_APPLICATION is available in the system environment.");
    } else {
      JSONObject json = new JSONObject(app);
      String value = json.getString("application_id");
      return value;
    }
    return null;
  }

  public static String getAppUris() {
    String app = System.getenv(vcapapplication);
    if (app == null || app.length() == 0) {
      logger.error("VCAP_APPLICATION environment is unavailable. Please verify whether VCAP_APPLICATION is available in the system environment.");
    } else {
      JSONObject json = new JSONObject(app);
      Object obj = json.get("application_uris");
      return obj.toString();
    }
    return null;
  }

  /**
   * when this is found, you can assume that you're running
   * the application on PCF. When deploying, make sure to set
   * user defined env var 'PCF_PROXY_SERVICENAME' which has the
   * service name of the wavefront proxy.
   */
  public static String getProxyServiceName() {
    return System.getenv("PCF_PROXY_SERVICENAME");
  }

  // extract proxy host and port using the provided servicename
  public static String[] getProxyHostPort(String servicename) {
    String[] hostAndPort = new String[2];
    StringBuffer buffer = new StringBuffer();
    String services = System.getenv(vcapservices);
    if (services == null || services.length() == 0) {
      logger.error("VCAP_SERVICES environment is unavailable. Please verify whether VCAP_SERVICES is available in the system environment.");
    } else {
      JSONObject json = new JSONObject(services);
      JSONArray jsonArray = json.getJSONArray(wavefrontservice);
      if (jsonArray == null || jsonArray.isNull(0)) {
        logger.error(servicename + " is not present in the VCAP_SERVICES env variable. Please verify and provide the wavefront proxy service name.");
      } else {
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject details = jsonArray.getJSONObject(i);
          JSONObject credentials = details.getJSONObject("credentials");
          String name = details.getString("name");
          if (name.equalsIgnoreCase(servicename)) {
            // host and port
            hostAndPort[0] = credentials.getString("hostname");
            hostAndPort[1] = Integer.toString(credentials.getInt("port"));
          }
        }
        logger.info("wavefront proxy obtained from service " + servicename + " is " + buffer.toString());
      }
    }
    return hostAndPort;
  }

}