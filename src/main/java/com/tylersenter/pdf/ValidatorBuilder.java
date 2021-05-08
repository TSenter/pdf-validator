package com.tylersenter.pdf;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.JsonNode;
import com.tylersenter.pdf.FormField.FieldType;
import com.tylersenter.pdf.validations.FieldValidation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class ValidatorBuilder {
  private Validator validator;
  private Map<String, FormField> fields;
  private Map<String, List<FieldValidation>> validations;

  public ValidatorBuilder(Validator validator, Map<String, FormField> fields, Map<String, List<FieldValidation>> validations) {
    this.validator = validator;
    this.fields = fields;
    this.validations = validations;
  }

  public void loadFields(JsonNode rootNode) {
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

      FormField formField = new FormField(fieldName, fieldType);

      fields.put(fieldName, formField);
      validations.put(fieldName, new LinkedList<FieldValidation>());

      loadValidations(fieldName, fieldNode);
    }
  }

  public void loadFieldsFromFile(File pdf) throws Exception {
    PDDocument doc = PDDocument.load(pdf);
    PDDocumentCatalog catalog = doc.getDocumentCatalog();
    PDAcroForm form = catalog.getAcroForm();

    for (PDField field : form.getFields()) {
      String fieldName = field.getFullyQualifiedName();

      if (!fields.containsKey(fieldName)) {
        if (validator.getPreferences().warnOnUnknownField()) {
          System.err.println("Unknown field: " + fieldName);
        }
        continue;
      }

      fields.get(fieldName).setField(field);
    }

    doc.close();
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
}
