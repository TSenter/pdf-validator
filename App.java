package com.tylersenter;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

public class App {

  static void printField(PDSignatureField field) {
    if (field.getSignature() == null) {
      printField((PDField) field);
      return;
    }
    System.out.println(String.format("%s (%s): %s", field.getFullyQualifiedName(),
          field.getClass().getSimpleName(), (field).getSignature().getName()));
  }

  static void printField(PDField field) {
    System.out.println(String.format("%s (%s): %s", field.getFullyQualifiedName(),
        field.getClass().getSimpleName(), field.getValueAsString()));
  }

  public static void main(String[] args) throws Exception {
    String fileName = "ding_namdu_admission-to-candidacy-MS.pdf";
    // String fileName = "Admission to Candidacy Masters.pdf";

    PDDocument doc;

    try {
      doc = PDDocument.load(new File(fileName));
    } catch (Exception e) {
      throw e;
    }

    for (PDField field : doc.getDocumentCatalog().getAcroForm().getFields()) {
      // if (field instanceof PDTextField) continue;
      if (field instanceof PDSignatureField) {
        printField((PDSignatureField) field);
      } else {
        printField(field);
      }
    }
  }
}
