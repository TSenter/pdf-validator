package com.tylersenter.pdf.validations.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.JsonNode;
import com.tylersenter.pdf.FormField.FieldType;
import com.tylersenter.pdf.ValidationBuilder;
import com.tylersenter.pdf.validations.FieldValidation;

public class FieldTableEntry {
  private final static String VARIABLE_NAME_ESCAPED = "\\{\\{row\\}\\}";

  private String name;
  private JsonNode node;
  private FieldType fieldType;

  public FieldTableEntry(String name, JsonNode node) {
    this.name = name;
    this.node = node;
    loadFieldType();
  }

  public String getRawName() {
    return name;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  public Map<String, List<FieldValidation>> buildValidations(FieldTable table) {
    Map<String, List<FieldValidation>> result = new HashMap<>();
    int start = table.getStart(), end = table.getEnd(), step = table.getStep();

    String fieldName;
    List<FieldValidation> validations;

    for (int row = start; row <= end; row += step) {
      fieldName = buildName(row);
      validations = buildValidations(row);

      result.put(fieldName, validations);
    }

    return result;
  }

  private void loadFieldType() {
    String fieldTypeString = null;
    FieldType fieldType;

    try {
      fieldTypeString = node.get("type").asText();
      fieldType = FieldType.valueOf(fieldTypeString.toUpperCase());
    } catch (Exception e) {
      throw new IllegalStateException("Invalid field type " + fieldTypeString
          + ". Valid values are " + Arrays.toString(FieldType.values()));
    }

    this.fieldType = fieldType;
  }

  private String buildName(int row) {
    return resolveVariable(name, row);
  }

  private String resolveVariable(String string, int row) {
    if (string == null) {
      return null;
    }
    return string.replaceAll(VARIABLE_NAME_ESCAPED, String.valueOf(row));
  }

  private List<FieldValidation> buildValidations(int row) {
    List<FieldValidation> validations = new ArrayList<>();
    JsonNode validationsNode = node.get("validations");

    if (validationsNode == null || validationsNode.isNull()) {
      return validations;
    }

    if (!validationsNode.isObject()) {
      throw new IllegalArgumentException("The 'validations' property in a table entry must be an object");
    }

    Entry<String, JsonNode> validationEntry;
    Iterator<Entry<String, JsonNode>> iterator = validationsNode.fields();
    while (iterator.hasNext()) {
      validationEntry = iterator.next();

      FieldValidation validation = ValidationBuilder.build(validationEntry.getKey(), validationEntry.getValue());

      resolveValidationVariables(validation, row);
      validations.add(validation);
    }

    return validations;
  }

  @SuppressWarnings("unchecked")
  private void resolveValidationVariables(FieldValidation validation, int row) {
    Map<String, Object> properties = validation.getProperties();

    validation.setValidMessage(resolveVariable(validation.getValidMessage(), row));
    validation.setInvalidMessage(resolveVariable(validation.getInvalidMessage(), row));

    for (String key : properties.keySet()) {
      Object property = properties.get(key);

      if (property instanceof String) {
        property = resolveVariable((String) property, row);
      } else if (property instanceof List) {
        property = resolveListVariable((List<Object>) property, row);
      } else {
        continue;
      }

      properties.put(key, property);
    }

    validation.setProperties(properties);
  }

  private List<Object> resolveListVariable(List<Object> list, int row) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, resolveVariable(list.get(i).toString(), row));
    }
    return list;
  }
}
