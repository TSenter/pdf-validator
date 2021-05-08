package com.tylersenter.pdf.validations;

import java.util.Map;
import com.tylersenter.pdf.FormField;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;

public class RequiredValidation extends FieldValidation {

  private static final String DEPENDENT_KEYS_FIELD = "dependentKeys";

  public static enum Level {
    YES, NO, WARNING;

    public static Level fromValue(boolean value) {
      if (value == true) {
        return YES;
      }
      return NO;
    }
  }

  private Level level;

  public RequiredValidation(Level level, String validMessage, String invalidMessage) {
    super(validMessage, invalidMessage);
    this.level = level;
  }

  public boolean validate(FormField field, Map<String, FormField> fields, Report report,
      Preferences preferences) {
    boolean isEnabled = true;
    if (hasProperty(DEPENDENT_KEYS_FIELD)) {
      isEnabled = false;
      String dependentKeys = (String) getProperty(DEPENDENT_KEYS_FIELD);
      boolean needsAll = dependentKeys.charAt(0) == '+';
      String[] dependencies = dependentKeys.substring(needsAll ? 1 : 0).split(",");

      for (String dependency : dependencies) {
        if (!fields.containsKey(dependency)) {
          continue;
        }
        FormField dependentField = fields.get(dependency);

        if (needsAll) {
          isEnabled &= dependentField.hasValue();
        } else {
          isEnabled |= dependentField.hasValue();
        }
      }
    }

    if (!isEnabled) {
      return false;
    }

    if (level == Level.NO && !field.hasValue()) {
      generateReport(field, preferences, report);
      return false;
    }

    if (level == Level.WARNING && !field.hasValue()) {
      generateWarning(field, preferences, report);
      return true;
    }

    if (level == Level.YES && !field.hasValue()) {
      generateError(field, preferences, report);
      return false;
    }

    generateReport(field, preferences, report);
    return true;
  }

}
