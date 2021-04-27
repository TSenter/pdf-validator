package com.tylersenter.pdf.reporting;

import java.util.Map;

public class ReportEntry implements Map.Entry<String, String> {

  private String key;
  private String value;

  public ReportEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public String setValue(String value) {
    String oldValue = this.value;
    this.value = value;
    
    return oldValue;
  }

}