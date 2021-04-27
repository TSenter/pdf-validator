package com.tylersenter;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tylersenter.pdf.Validator;
import com.tylersenter.pdf.configurations.Preferences;
import com.tylersenter.pdf.reporting.Report;
import com.tylersenter.pdf.reporting.Report.ReportType;

public class App {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Error: no files were specified");
      System.exit(1);
    }

    Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);

    File configFile = new File("config.json");

    if (!configFile.exists()) {
      System.err.println("Error: no configuration file found, should be named config.json");
      System.exit(1);
    }
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootConfig = mapper.readTree(configFile);

    Validator validator;
    Preferences prefs;
    File pdf;
    Report report;
    for (String arg : args) {
      pdf = new File(arg);

      if (!pdf.exists()) {
        System.err.println("Error: file not found: " + arg);
        System.exit(1);
      }

      validator = new Validator(rootConfig, pdf);
      prefs = validator.getPreferences();

      report = validator.validateAll();

      if (prefs.isSilent()) {
        continue;
      }

      System.out.println(report.build(ReportType.JSON, true));
    }
  }
}
