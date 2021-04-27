package com.tylersenter.pdf.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.tylersenter.pdf.FormField;

public class VariableUtils {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([A-z]+)\\}\\}");

  private static VariableResolver DEFAULT_RESOLVER = new VariableResolver() {
    public String resolveVariable(FormField field, String variable) {
      switch (variable) {
        case "fieldName":
          return field.getField().getFullyQualifiedName();
        case "fieldValue":
          return field.valueAsString();
      }

      throw new IllegalArgumentException("The variable '" + variable + "' is undefined.");
    }
  };

  public static String replaceVariables(FormField field, String message) {
    return replaceVariables(field, message, DEFAULT_RESOLVER);
  }

  public static String replaceVariables(FormField field, String message,
      VariableResolver resolver) {
    Matcher matcher = VARIABLE_PATTERN.matcher(message);

    while (matcher.find()) {
      String varName = matcher.group(1);

      message = message.replace(matcher.group(), resolver.resolveVariable(field, varName));
    }

    return message;
  }

}
