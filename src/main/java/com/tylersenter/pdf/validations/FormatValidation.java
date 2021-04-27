package com.tylersenter.pdf.validations;

import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;

public class FormatValidation extends FieldValidation {

  private static final String MINIMUM_PROP = "minimum";
  private static final String MAXIMUM_PROP = "maximum";
  private static final String EQUALS_PROP = "equals";

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

    Long value;
    Long equals = null;
    long minimum = Long.MIN_VALUE;
    long maximum = Long.MAX_VALUE;

    if (!isValid) {
      generateError(field, preferences, report);
      return false;
    }

    value = Long.parseLong(field.valueAsString());

    if (hasProperty(MINIMUM_PROP)) {
      Object minObject = getProperty(MINIMUM_PROP);

      if (!(minObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + MINIMUM_PROP + " must be a number");
      }
      minimum = (long) minObject;
    }

    if (hasProperty(MAXIMUM_PROP)) {
      Object maxObject = getProperty(MAXIMUM_PROP);

      if (!(maxObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + MAXIMUM_PROP + " must be a number");
      }
      maximum = (long) maxObject;
    }

    if (hasProperty(EQUALS_PROP)) {
      Object eqObject = getProperty(EQUALS_PROP);

      if (!(eqObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + EQUALS_PROP + " must be a number");
      }
      equals = (long) eqObject;
    }

    isValid &= minimum <= value;
    isValid &= value <= maximum;
    if (equals != null) {
      isValid &= value == equals;
    }

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

    Double value;
    Double equals = null;
    double minimum = Double.MIN_VALUE;
    double maximum = Double.MAX_VALUE;

    if (!isValid) {
      generateError(field, preferences, report);
      return false;
    }

    value = Double.parseDouble(field.valueAsString());

    if (hasProperty(MINIMUM_PROP)) {
      Object minObject = getProperty(MINIMUM_PROP);

      if (!(minObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + MINIMUM_PROP + " must be a number");
      }
      minimum = (double) minObject;
    }

    if (hasProperty(MAXIMUM_PROP)) {
      Object maxObject = getProperty(MAXIMUM_PROP);

      if (!(maxObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + MAXIMUM_PROP + " must be a number");
      }
      maximum = (double) maxObject;
    }

    if (hasProperty(EQUALS_PROP)) {
      Object eqObject = getProperty(EQUALS_PROP);

      if (!(eqObject instanceof Long)) {
        throw new IllegalArgumentException("The value of " + EQUALS_PROP + " must be a number");
      }
      equals = (double) eqObject;
    }

    isValid &= minimum <= value;
    isValid &= value <= maximum;
    if (equals != null) {
      isValid &= value == equals;
    }

    if (isValid) {
      generateReport(field, preferences, report);
    } else {
      generateError(field, preferences, report);
    }

    return isValid;
  }

}
