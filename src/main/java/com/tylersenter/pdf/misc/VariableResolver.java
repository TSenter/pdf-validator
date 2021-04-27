package com.tylersenter.pdf.misc;

import com.tylersenter.pdf.FormField;

/**
 * A VariableResolver is a class that returns some information about a field, given a variable name.
 * For example, a radio button field may resolve the variable <code>selectedValue</code> to the text
 * of the selected option.
 */
public interface VariableResolver {
  /**
   * Given a FormField object, return information corresponding to the label variable
   * 
   * @param field
   * @param variable
   * @return A string containing the value of the variable, or <code>null</code> if the variable
   *         could not be resolved
   */
  public String resolveVariable(FormField field, String variable);
}
