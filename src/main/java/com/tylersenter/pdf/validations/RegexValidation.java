package com.tylersenter.pdf.validations;

import java.util.Map;
import java.util.regex.Pattern;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;

public class RegexValidation extends FieldValidation {

  private Pattern pattern;

  public RegexValidation(Pattern pattern, String validMessage, String invalidMessage) {
    super(validMessage, invalidMessage);
    this.pattern = pattern;
  }

  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    String valueAsString = field.valueAsString();

    boolean isValid = pattern.matcher(valueAsString).matches();

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

}
