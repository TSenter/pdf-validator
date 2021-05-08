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
import com.tylersenter.pdf.validations.tables.FieldTable;
import com.tylersenter.pdf.validations.tables.FieldTableEntry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class ValidatorBuilder {
  private Validator validator;
  private Map<String, FormField> fields;
  private Map<String, List<FieldValidation>> validations;

  public ValidatorBuilder(Validator validator, Map<String, FormField> fields,
      Map<String, List<FieldValidation>> validations) {
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

      FormField formField = new FormField(fieldName, fieldType, null);

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

  public void loadTables(JsonNode rootNode) {
    JsonNode tablesArray = rootNode.get("tables");

    if (tablesArray == null || tablesArray.isNull()) {
      return;
    }

    if (!tablesArray.isArray()) {
      throw new IllegalStateException(
          "The 'tables' property in the configuration file must be an array");
    }

    for (JsonNode tableNode : tablesArray) {
      loadTable(tableNode);
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

  private void loadTable(JsonNode tableNode) {
    JsonNode nameNode = tableNode.get("name");
    JsonNode rangeNode = tableNode.get("range");

    if (nameNode == null || nameNode.isNull() || !nameNode.isValueNode()) {
      throw new IllegalArgumentException("A table object must have the 'name' property defined");
    }

    if (rangeNode == null || rangeNode.isNull() || !rangeNode.isObject()) {
      throw new IllegalArgumentException("A table object must have the 'range' object defined");
    }

    FieldTable table = new FieldTable(nameNode.asText());

    if (rangeNode.has("start")) {
      JsonNode startNode = rangeNode.get("start");

      if (startNode.isNull() || !startNode.canConvertToInt()) {
        throw new IllegalArgumentException(
            "The 'start' property in a range object must be an integer");
      }

      table.setStart(startNode.asInt());
    }

    if (rangeNode.has("step")) {
      JsonNode stepNode = rangeNode.get("step");

      if (stepNode.isNull() || !stepNode.canConvertToInt()) {
        throw new IllegalArgumentException(
            "The 'step' property in a range object must be an integer");
      }

      table.setStep(stepNode.asInt());
    }

    if (!rangeNode.has("end")) {
      throw new IllegalArgumentException("A range object must have the 'end' property defined");
    }

    JsonNode endNode = rangeNode.get("end");

    if (endNode.isNull() || !endNode.canConvertToInt()) {
      throw new IllegalArgumentException("The 'end' property in a range object must be an integer");
    }

    table.setEnd(endNode.asInt());

    if ((table.getEnd() - table.getStart()) % table.getStep() != 0) {
      throw new IllegalArgumentException(
          String.format("The step %d is not valid for the start %d and end %d", table.getStep(),
              table.getStart(), table.getEnd()));
    }

    JsonNode structureNode = tableNode.get("structure");

    if (structureNode == null || structureNode.isNull()) {
      throw new IllegalArgumentException("A table object must have the 'structure' array defined");
    }

    if (!structureNode.isArray()) {
      throw new IllegalArgumentException(
          "The 'structure' property in a table object must be an array");
    }

    loadTableEntries(structureNode, table);
  }

  private void loadTableEntries(JsonNode structureNode, FieldTable table) {
    FieldTableEntry entry;
    Map<String, List<FieldValidation>> entryValidations;
    FormField formField;

    for (JsonNode node : structureNode) {
      entry = loadTableEntry(node, table);
      entryValidations = entry.buildValidations(table);

      for (Entry<String, List<FieldValidation>> validationsEntry : entryValidations.entrySet()) {
        String fieldName = validationsEntry.getKey();
        formField = new FormField(fieldName, entry.getFieldType());
        
        fields.put(fieldName, formField);

        List<FieldValidation> fieldValidations = validationsEntry.getValue();
        if (this.validations.containsKey(fieldName)) {
          fieldValidations.addAll(this.validations.get(fieldName));
        }
        this.validations.put(fieldName, fieldValidations);
      }
    }
  }

  private FieldTableEntry loadTableEntry(JsonNode entryNode, FieldTable table) {
    FieldTableEntry entry;

    JsonNode nameNode = entryNode.get("name");

    if (nameNode == null || !nameNode.isTextual()) {
      throw new IllegalStateException("A table entry must have the 'name' property defined");
    }

    entry = new FieldTableEntry(nameNode.asText(), entryNode);

    return entry;
  }
}
