package com.vmware.wavefront.loadgen;

public class Response {
  String name;
  String type;

  public Response(String name, String type, String message) {
    this.name = name;
    this.type = type;
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  String message;
}
