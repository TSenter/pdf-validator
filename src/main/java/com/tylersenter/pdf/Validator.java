package com.tylersenter.pdf;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.JsonNode;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;
import com.tylersenter.pdf.validations.FieldValidation;

public class Validator {
  private Map<String, FormField> fields;
  private Map<String, List<FieldValidation>> validations;
  private Preferences preferences;

  public Validator(JsonNode rootNode, File pdf) throws Exception {
    this.fields = new HashMap<>();
    this.validations = new HashMap<>();
    this.preferences = Preferences.parseFromJson(rootNode);

    build(rootNode, pdf);
  }

  private void build(JsonNode rootNode, File pdf) throws Exception {
    ValidatorBuilder builder = new ValidatorBuilder(this, fields, validations);

    builder.loadFields(rootNode);
    builder.loadTables(rootNode);
    builder.loadFieldsFromFile(pdf);
  }

  public Preferences getPreferences() {
    return preferences;
  }

  public Report validateAll() {
    Report report = new Report();

    boolean isValid;
    for (Entry<String, FormField> entry : fields.entrySet()) {
      String fieldName = entry.getKey();
      List<FieldValidation> validations = this.validations.get(fieldName);

      for (FieldValidation validation : validations) {
        isValid = validation.validate(entry.getValue(), fields, report, preferences);

        if (!isValid) {
          break;
        }
      }
      report.next();
    }

    return report;
  }
}
