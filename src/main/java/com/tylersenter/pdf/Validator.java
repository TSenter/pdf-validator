package com.tylersenter.pdf;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.JsonNode;
import com.tylersenter.pdf.FormField.FieldType;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;
import com.tylersenter.pdf.validations.FieldValidation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class Validator {
  private Map<String, FormField> fields;
  private Map<String, List<FieldValidation>> validations;
  private Preferences preferences;

  public Validator(JsonNode rootNode, File pdf) throws Exception {
    this.fields = new HashMap<>();
    this.validations = new HashMap<>();
    this.preferences = Preferences.parseFromJson(rootNode);

    loadFields(rootNode);
    loadFieldsFromFile(pdf);
  }

  private void loadFields(JsonNode rootNode) {
    JsonNode fieldsArray = rootNode.get("fields");

    if (fieldsArray == null || fieldsArray.isNull()) {
      return;
    }

    if (!fieldsArray.isArray()) {
      throw new IllegalStateException(
          "The 'fields' property in the configuration file must be an array");
    }

    for (JsonNode fieldNode : fieldsArray) {
      JsonNode field = fieldNode.get("name");

      if (field == null || !field.isTextual()) {
        throw new IllegalStateException("A field object must have the 'name' property defined");
      }
      String fieldName = field.asText();
      String fieldTypeString = null;
      FieldType fieldType;

      if (fields.containsKey(fieldName)) {
        throw new IllegalStateException("The field '" + fieldName + "' is defined twice");
      }

      try {
        fieldTypeString = fieldNode.get("type").asText();
        fieldType = FieldType.valueOf(fieldTypeString.toUpperCase());
      } catch (Exception e) {
        throw new IllegalStateException("Invalid field type " + fieldTypeString
            + ". Valid values are " + Arrays.toString(FieldType.values()));
      }

      FormField formField = new FormField(fieldName, fieldType, null);

      fields.put(fieldName, formField);
      validations.put(fieldName, new LinkedList<FieldValidation>());

      loadValidations(fieldName, fieldNode);
    }
  }

  private void loadValidations(String fieldName, JsonNode fieldNode) {
    JsonNode validationsNode = fieldNode.get("validations");

    if (validationsNode == null || validationsNode.isNull()) {
      return;
    }

    if (!validationsNode.isObject()) {
      throw new IllegalStateException("The validations property must be an object");
    }

    Entry<String, JsonNode> validationEntry;
    Iterator<Entry<String, JsonNode>> iterator = validationsNode.fields();
    while (iterator.hasNext()) {
      validationEntry = iterator.next();

      FieldValidation validation =
          ValidationBuilder.build(validationEntry.getKey(), validationEntry.getValue());

      validations.get(fieldName).add(validation);
    }
  }

  private void loadFieldsFromFile(File pdf) throws Exception {
    PDDocument doc = PDDocument.load(pdf);
    PDDocumentCatalog catalog = doc.getDocumentCatalog();
    PDAcroForm form = catalog.getAcroForm();

    for (PDField field : form.getFields()) {
      String fieldName = field.getFullyQualifiedName();

      if (!fields.containsKey(fieldName)) {
        if (preferences.warnOnUnknownField()) {
          System.err.println("Unknown field: " + fieldName);
        }
        continue;
      }

      fields.get(fieldName).setField(field);
    }

    doc.close();
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
