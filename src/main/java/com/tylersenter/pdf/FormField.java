package com.tylersenter.pdf;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

public class FormField {
  public static enum FieldType {
    BUTTON("Btn"), TEXT("Tx"), SIGNATURE("Sig");

    private String apacheType;

    private FieldType(String apacheType) {
      this.apacheType = apacheType;
    }

    public String getApacheType() {
      return apacheType;
    }
  }

  private String name;
  private FieldType fieldType;
  private PDField field;
  private String valueAsString;

  public FormField(String name, FieldType fieldType) {
    this.name = name;
    this.fieldType = fieldType;
    this.field = null;
  }

  private void findValueAsString() {
    if (isTextField()) {
      valueAsString = asTextField().getValue();
    } else if (isCheckBox()) {
      valueAsString = String.valueOf(asCheckBox().isChecked());
    } else if (isRadioButton()) {
      valueAsString = asRadioButton().getValue();
    } else if (isSignature()) {
      PDSignature signature = asSignatureField().getSignature();

      if (signature == null) {
        valueAsString =  "";
      } else {
        valueAsString = signature.getName();
      }
    } else {
      throw new IllegalStateException("Unknown field type: " + fieldType);
    }

  }

  public String getName() {
    return name;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public PDField getField() {
    return field;
  }

  public void setField(PDField field) {
    this.field = field;
    findValueAsString();
  }

  public boolean isTextField() {
    return field instanceof PDTextField;
  }

  public PDTextField asTextField() {
    return (PDTextField) field;
  }

  public boolean isCheckBox() {
    return field instanceof PDCheckBox;
  }

  public PDCheckBox asCheckBox() {
    return (PDCheckBox) field;
  }

  public boolean isRadioButton() {
    return field instanceof PDRadioButton;
  }

  public PDRadioButton asRadioButton() {
    return (PDRadioButton) field;
  }

  public boolean isSignature() {
    return field instanceof PDSignatureField;
  }

  public PDSignatureField asSignatureField() {
    return (PDSignatureField) field;
  }

  public boolean hasValue() {
    String valueAsString = valueAsString();
    return valueAsString != null && !valueAsString.isEmpty();
  }

  public String valueAsString() {
    return valueAsString;
  }
}
