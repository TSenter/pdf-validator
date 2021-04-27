package com.tylersenter.pdf.validations;

import java.util.List;
import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;

public class WhitelistValidation extends FieldValidation {

  private static final String CASE_SENSITIVE_FIELD = "caseSensitive";

  private List<String> allowedValues;

  public WhitelistValidation(List<String> allowedValues, String validMessage,
      String invalidMessage) {
    super(validMessage, invalidMessage);
    this.allowedValues = allowedValues;
  }

  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    String value = field.valueAsString();
    boolean caseSensitive = true;
    boolean isValid = false;

    if (hasProperty(CASE_SENSITIVE_FIELD)) {
      caseSensitive = (boolean) getProperty(CASE_SENSITIVE_FIELD);
    }

    for (String allowedValue : allowedValues) {
      if ((caseSensitive && allowedValue.equals(value))
          || (!caseSensitive && allowedValue.equalsIgnoreCase(value))) {
        isValid = true;
      }
    }

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

}
