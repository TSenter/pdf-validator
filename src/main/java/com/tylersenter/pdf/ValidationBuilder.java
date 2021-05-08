package com.tylersenter.pdf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tylersenter.pdf.validations.AllowListValidation;
import com.tylersenter.pdf.validations.DisallowListValidation;
import com.tylersenter.pdf.validations.FieldValidation;
import com.tylersenter.pdf.validations.FormatValidation;
import com.tylersenter.pdf.validations.FormatValidation.FormatType;
import com.tylersenter.pdf.validations.RegexValidation;
import com.tylersenter.pdf.validations.RequiredValidation;
import com.tylersenter.pdf.validations.WarnListValidation;
import com.tylersenter.pdf.validations.RequiredValidation.Level;

public class ValidationBuilder {
  public static FieldValidation build(String key, JsonNode value) {
    JsonNode valueNode = value;
    String validMessage = null;
    String invalidMessage = null;
    Map<String, Object> properties = new HashMap<>();

    if (value.isObject()) {
      valueNode = value.get("value");

      if (valueNode == null || valueNode.isNull()) {
        throw new IllegalArgumentException(
            "The 'value' property on a validation must be set if the object configuration is used");
      }

      JsonNode validMessageNode = value.get("validMessage");
      JsonNode invalidMessageNode = value.get("invalidMessage");

      if (validMessageNode != null && !validMessageNode.isNull()) {
        validMessage = validMessageNode.asText();
      }

      if (invalidMessageNode != null && !invalidMessageNode.isNull()) {
        invalidMessage = invalidMessageNode.asText();
      }

      Iterator<Entry<String, JsonNode>> iterator = value.fields();
      Entry<String, JsonNode> entry;
      while (iterator.hasNext()) {
        entry = iterator.next();

        String entryKey = entry.getKey();
        JsonNode node = entry.getValue();
        String nodeText = node.asText();
        if (nodeText.equals("value") || nodeText.equals("validMessage")
            || nodeText.equals("invalidMessage")) {
          continue;
        }

        switch (node.getNodeType()) {
          case BOOLEAN:
            properties.put(entryKey, node.asBoolean());
            continue;
          case NUMBER:
            properties.put(entryKey, node.asDouble());
            continue;
          case STRING:
            properties.put(entryKey, node.asText());
            continue;
          case ARRAY:
            properties.put(entryKey, convertArrayToList((ArrayNode) node));
            continue;
          default:
            break;
        }

        throw new IllegalArgumentException(
            "Validation property of type " + node.getNodeType() + " is not supported");
      }
    }

    FieldValidation validation;

    if (key.equals("required")) {
      validation = buildRequired(valueNode, validMessage, invalidMessage);
    } else if (key.equals("format")) {
      validation = buildFormat(valueNode, validMessage, invalidMessage);
    } else if (key.equals("allowList")) {
      validation = buildAllowList(valueNode, validMessage, invalidMessage);
    } else if (key.equals("disallowList")) {
      validation = buildDisallowList(valueNode, validMessage, invalidMessage);
    } else if (key.equals("warnList")) {
      validation = buildWarnList(valueNode, validMessage, invalidMessage);
    } else if (key.equals("regex")) {
      validation = buildRegex(valueNode, validMessage, invalidMessage);
    } else if (key.equals("custom")) {
      validation = buildCustom(valueNode, validMessage, invalidMessage);
    } else {
      throw new IllegalArgumentException("Invalid validation type '" + key + "'");
    }

    validation.setProperties(properties);

    return validation;
  }

  private static List<String> convertArrayToList(ArrayNode node) {
    List<String> list = new ArrayList<>(node.size());

    for (JsonNode element : node) {
      list.add(element.asText());
    }

    return list;
  }

  private static FieldValidation buildRequired(JsonNode node, String validMessage,
      String invalidMessage) {
    Level level;

    if (node.isBoolean()) {
      level = Level.fromValue(node.asBoolean());
    } else {
      try {
        level = Level.valueOf(node.asText().toUpperCase());
      } catch (Exception e) {
        throw new IllegalArgumentException("Required level '" + node.asText()
            + "' is not supported. Valid values are " + Arrays.toString(Level.values()));
      }
    }

    return new RequiredValidation(level, validMessage, invalidMessage);
  }

  private static FieldValidation buildFormat(JsonNode node, String validMessage,
      String invalidMessage) {
    FormatType type;
    String typeText = node.asText();

    try {
      type = FormatType.valueOf(typeText.toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("Format type '" + typeText
          + "' is not supported. Valid values are " + Arrays.toString(FormatType.values()));
    }

    return new FormatValidation(type, validMessage, invalidMessage);
  }

  private static FieldValidation buildAllowList(JsonNode node, String validMessage,
      String invalidMessage) {
    List<String> allowedValues;

    if (node.isTextual()) {
      allowedValues = Arrays.asList(node.asText().split(","));
    } else if (node.isArray()) {
      allowedValues = convertArrayToList((ArrayNode) node);
    } else {
      throw new IllegalArgumentException("Invalid type for allowList value " + node.getNodeType());
    }

    return new AllowListValidation(allowedValues, validMessage, invalidMessage);
  }

  private static FieldValidation buildDisallowList(JsonNode node, String validMessage,
      String invalidMessage) {
    List<String> disallowedValues;

    if (node.isTextual()) {
      disallowedValues = Arrays.asList(node.asText().split(","));
    } else if (node.isArray()) {
      disallowedValues = convertArrayToList((ArrayNode) node);
    } else {
      throw new IllegalArgumentException(
          "Invalid type for disallowList value " + node.getNodeType());
    }

    return new DisallowListValidation(disallowedValues, validMessage, invalidMessage);
  }

  private static FieldValidation buildWarnList(JsonNode node, String validMessage,
      String warnMessage) {
    List<String> warnValues;

    if (node.isTextual()) {
      warnValues = Arrays.asList(node.asText().split(","));
    } else if (node.isArray()) {
      warnValues = convertArrayToList((ArrayNode) node);
    } else {
      throw new IllegalArgumentException("Invalid type for warnList value " + node.getNodeType());
    }

    return new WarnListValidation(warnValues, validMessage, warnMessage);
  }

  private static FieldValidation buildRegex(JsonNode node, String validMessage,
      String invalidMessage) {
    String nodeText = node.asText();
    Pattern pattern;

    try {
      pattern = Pattern.compile(nodeText);
    } catch (Exception e) {
      throw new IllegalArgumentException("Regular expression '" + nodeText + "' is not valid");
    }

    return new RegexValidation(pattern, validMessage, invalidMessage);
  }

  private static FieldValidation buildCustom(JsonNode node, String validMessage,
      String invalidMessage) {
    String nodeText = node.asText();

    Class<?> cls;

    try {
      cls = Class.forName(nodeText);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Could not find class with the name '" + nodeText + "'");
    }

    if (!FieldValidation.class.isAssignableFrom(cls)) {
      throw new IllegalArgumentException("Class '" + cls.getSimpleName() + "' does not extend "
          + FieldValidation.class.getSimpleName());
    }

    Constructor<?> validationConstructor;

    try {
      validationConstructor = cls.getConstructor(String.class);
    } catch (NoSuchMethodException e) {
      // This should never happen
      throw new IllegalStateException(
          "Failed to get constructor for custom validation " + nodeText);
    }

    try {
      return (FieldValidation) validationConstructor.newInstance(validMessage, invalidMessage);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to create new instance of custom validation " + nodeText);
    }
  }
}
