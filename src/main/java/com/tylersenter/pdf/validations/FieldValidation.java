package com.tylersenter.pdf.validations;

import java.util.HashMap;
import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.misc.VariableUtils;
import com.tylersenter.pdf.reporting.Report;

public abstract class FieldValidation {

  private String validMessage;
  private String invalidMessage;
  private Map<String, Object> properties;

  public FieldValidation() {
    this(null, null);
  }

  public FieldValidation(String validMessage, String invalidMessage) {
    this.validMessage = validMessage;
    this.invalidMessage = invalidMessage;
    this.properties = new HashMap<>();
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

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  protected final boolean hasProperty(String key) {
    return properties.containsKey(key);
  }

  protected final Object getProperty(String key) {
    return properties.get(key);
  }

  public abstract boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences);

  protected void generateReport(FormField field, Preferences preferences, Report report) {
    if (validMessage == null) {
      validMessage = preferences.getValidMessage();
    }

    report.addReport(field.getName(), replaceVariables(field, validMessage));
  }

  protected void generateWarning(FormField field, Preferences preferences, Report report) {
    if (invalidMessage == null) {
      invalidMessage = preferences.getInvalidMessage();
    }

    report.addWarning(field.getName(), replaceVariables(field, invalidMessage));
  }

  protected void generateError(FormField field, Preferences preferences, Report report) {
    if (invalidMessage == null) {
      invalidMessage = preferences.getInvalidMessage();
    }

    report.addError(field.getName(), replaceVariables(field, invalidMessage));
  }

  protected String replaceVariables(FormField field, String message) {
    return VariableUtils.replaceVariables(field, message);
  }
}
