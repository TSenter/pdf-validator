package com.tylersenter.pdf.configurations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

public class Preferences {

  public static enum ReportLevel {
    NONE, EXIT_CODE, COMPACT, DETAILED, ALL;
  }

  private static final String INVALID_MESSAGE_FIELD = "invalidMessage";
  private static final String VALID_MESSAGE_FIELD = "validMessage";
  private static final String REPORT_LEVEL_FIELD = "reportLevel";
  private static final String SILENT_FIELD = "silent";
  private static final String WARN_ON_UNKNOWN_FIELD = "warnOnUnknownField";

  private static final Map<String, Object> DEFAULT_VALUES;

  static {
    DEFAULT_VALUES = new HashMap<>();

    DEFAULT_VALUES.put(INVALID_MESSAGE_FIELD, "The value '{{fieldValue}}' for '{{fieldName}}' is invalid.");
    DEFAULT_VALUES.put(VALID_MESSAGE_FIELD, "The field {{fieldName}} is valid.");
    DEFAULT_VALUES.put(REPORT_LEVEL_FIELD, ReportLevel.DETAILED);
    DEFAULT_VALUES.put(SILENT_FIELD, false);
    DEFAULT_VALUES.put(WARN_ON_UNKNOWN_FIELD, true);
  }

  private String validMessage;
  private String invalidMessage;
  private ReportLevel reportLevel;
  private boolean isSilent;
  private boolean warnOnUnknownField;

  public Preferences() {
    this.validMessage = (String) DEFAULT_VALUES.get(VALID_MESSAGE_FIELD);
    this.invalidMessage = (String) DEFAULT_VALUES.get(INVALID_MESSAGE_FIELD);
    this.reportLevel = (ReportLevel) DEFAULT_VALUES.get(REPORT_LEVEL_FIELD);
    this.isSilent = (boolean) DEFAULT_VALUES.get(SILENT_FIELD);
    this.warnOnUnknownField = (boolean) DEFAULT_VALUES.get(WARN_ON_UNKNOWN_FIELD);
  }

  public String getValidMessage() {
    return validMessage;
  }

  public void setValidMessage(String validMessage) {
    this.validMessage = validMessage;
  }

  public String getInvalidMessage() {
    return invalidMessage;
  }

  public void setInvalidMessage(String invalidMessage) {
    this.invalidMessage = invalidMessage;
  }

  public ReportLevel getReportLevel() {
    return reportLevel;
  }

  public void setReportLevel(ReportLevel reportingType) {
    this.reportLevel = reportingType;
  }

  public boolean isSilent() {
    return isSilent;
  }

  public void setIsSilent(boolean isSilent) {
    this.isSilent = isSilent;
  }

  public boolean warnOnUnknownField() {
    return warnOnUnknownField;
  }

  public void setWarnOnUnknownField(boolean warnOnUnknownField) {
    this.warnOnUnknownField = warnOnUnknownField;
  }

  /**
    Retrieve and construct a preferences object from the root configuration node.

    If the passed configuration object is null or does not contain the necessary <code>preferences</code> object,
    return an object populated with the default preferences.

    @return nonnull Preferences object
  */
  public static Preferences parseFromJson(JsonNode node) {
    Preferences preferences = new Preferences();

    if (node == null || !node.hasNonNull("preferences")) {
      return preferences;
    }
    JsonNode rootNode = node.get("preferences");

    preferences.setValidMessage(getValidMessage(rootNode));
    preferences.setInvalidMessage(getInvalidMessage(rootNode));
    preferences.setReportLevel(getReportTypeJson(rootNode));
    preferences.setIsSilent(getSilent(rootNode));
    preferences.setWarnOnUnknownField(getWarnOnUnknown(rootNode));

    return preferences;
  }

  private static String getValidMessage(JsonNode node) {
    JsonNode propertyNode = node.get(VALID_MESSAGE_FIELD);

    if (propertyNode == null || propertyNode.isNull()) {
      return (String) DEFAULT_VALUES.get(VALID_MESSAGE_FIELD);
    }

    return propertyNode.asText();
  }

  private static String getInvalidMessage(JsonNode node) {
    JsonNode propertyNode = node.get(INVALID_MESSAGE_FIELD);

    if (propertyNode == null || propertyNode.isNull()) {
      return (String) DEFAULT_VALUES.get(INVALID_MESSAGE_FIELD);
    }

    return propertyNode.asText();
  }

  private static ReportLevel getReportTypeJson(JsonNode node) {
    JsonNode propertyNode = node.get(REPORT_LEVEL_FIELD);

    if (propertyNode == null || propertyNode.isNull()) {
      return (ReportLevel) DEFAULT_VALUES.get(REPORT_LEVEL_FIELD);
    }

    String typeAsText = propertyNode.asText();
    try {
      return ReportLevel.valueOf(typeAsText.toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("Value '" + typeAsText + "' is not a valid reporting type. Valid values are " + Arrays.toString(ReportLevel.values()));
    }
  }

  private static boolean getSilent(JsonNode node) {
    JsonNode propertyNode = node.get(SILENT_FIELD);

    if (propertyNode == null || propertyNode.isNull()) {
      return (boolean) DEFAULT_VALUES.get(SILENT_FIELD);
    }

    if (propertyNode.isBoolean()) {
      return propertyNode.asBoolean();
    }

    throw new IllegalArgumentException("Value '" + propertyNode.asText() + "' is not a valid value. Valid values are [true,false]");
  }

  private static boolean getWarnOnUnknown(JsonNode node) {
    JsonNode propertyNode = node.get(WARN_ON_UNKNOWN_FIELD);

    if (propertyNode == null || propertyNode.isNull()) {
      return (boolean) DEFAULT_VALUES.get(WARN_ON_UNKNOWN_FIELD);
    }

    if (propertyNode.isBoolean()) {
      return propertyNode.asBoolean();
    }
    
    throw new IllegalArgumentException("Value '" + propertyNode.asText() + "' is not a valid value. Valid values are [true,false]");
  }
}
