package com.tylersenter.pdf.validations;

import java.util.List;
import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;

public class BlacklistValidation extends FieldValidation {

  private static final String CASE_SENSITIVE_FIELD = "caseSensitive";

  private List<String> disallowedValues;

  public BlacklistValidation(List<String> disallowedValues, String validMessage,
      String invalidMessage) {
    super(validMessage, invalidMessage);
    this.disallowedValues = disallowedValues;
  }

  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    String value = field.valueAsString();
    boolean caseSensitive = true;
    boolean isValid = true;

    if (hasProperty(CASE_SENSITIVE_FIELD)) {
      caseSensitive = (boolean) getProperty(CASE_SENSITIVE_FIELD);
    }

    for (String disallowedValue : disallowedValues) {
      if ((caseSensitive && disallowedValue.equals(value))
          || (!caseSensitive && disallowedValue.equalsIgnoreCase(value))) {
        isValid = false;
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
