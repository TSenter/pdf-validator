package com.tylersenter.pdf.validations;

import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;

public class FormatValidation extends FieldValidation {

  public static enum FormatType {
    EMAIL, INTEGER, DECIMAL;
  }

  private FormatType type;

  public FormatValidation(FormatType type, String validMessage, String invalidMessage) {
    super(validMessage, invalidMessage);
    this.type = type;
  }

  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    switch (type) {
      case EMAIL:
        return validateEmail(field, fields, report, preferences);
      case INTEGER:
        return validateInteger(field, fields, report, preferences);
      case DECIMAL:
        return validateDecimal(field, fields, report, preferences);
    }
    throw new IllegalArgumentException("Invalid format type: " + type);
  }

  private boolean validateEmail(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    EmailValidator validator = EmailValidator.getInstance();

    boolean isValid = validator.isValid(field.asTextField().getValue());

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

  private boolean validateInteger(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    LongValidator validator = LongValidator.getInstance();

    boolean isValid = validator.isValid(field.asTextField().getValue());

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

  private boolean validateDecimal(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    DoubleValidator validator = DoubleValidator.getInstance();

    boolean isValid = validator.isValid(field.asTextField().getValue());

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

}
