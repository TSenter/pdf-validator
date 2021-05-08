package com.tylersenter.pdf.validations;

import java.util.List;
import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;

public class WarnListValidation extends FieldValidation {

  private static final String CASE_SENSITIVE_FIELD = "caseSensitive";
  private static final String ALLOW_TRIM_FIELD = "allowTrim";

  private List<String> warnValues;

  public WarnListValidation(List<String> warnValues, String validMessage, String warnMessage) {
    super(validMessage, warnMessage);
    this.warnValues = warnValues;
  }

  @Override
  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    String value = field.valueAsString();
    boolean caseSensitive = true;
    boolean shouldWarn = false;

    if (hasProperty(CASE_SENSITIVE_FIELD)) {
      caseSensitive = (boolean) getProperty(CASE_SENSITIVE_FIELD);
    }

    if (hasProperty(ALLOW_TRIM_FIELD) && (boolean) getProperty(ALLOW_TRIM_FIELD)) {
      value = value.trim();
    }

    for (String warnValue : warnValues) {
      if ((caseSensitive && warnValue.equals(value))
          || (!caseSensitive && warnValue.equalsIgnoreCase(value))) {
        shouldWarn = true;
      }
    }

    if (shouldWarn) {
      generateWarning(field, preferences, report);
    } else {
      generateReport(field, preferences, report);
    }

    return true;
  }

}
