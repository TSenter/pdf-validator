package com.tylersenter.pdf.reporting;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Report {
  public static enum ReportType {
    JSON;
  }

  private List<ReportEntry> allReports;
  private List<ReportEntry> allWarnings;
  private List<ReportEntry> allErrors;

  private List<ReportEntry> reports;
  private List<ReportEntry> warnings;
  private List<ReportEntry> errors;

  public Report() {
    this.allReports = new LinkedList<>();
    this.allWarnings = new LinkedList<>();
    this.allErrors = new LinkedList<>();

    this.reports = new LinkedList<>();
    this.warnings = new LinkedList<>();
    this.errors = new LinkedList<>();
  }

  public Iterator<ReportEntry> getReports() {
    return allReports.iterator();
  }

  public boolean hasReports() {
    return !allReports.isEmpty();
  }

  public void addReport(String fieldName, String report) {
    reports.add(new ReportEntry(fieldName, report));
  }

  public Iterator<ReportEntry> getWarnings() {
    return allWarnings.iterator();
  }

  public boolean hasWarnings() {
    return !allWarnings.isEmpty();
  }

  public void addWarning(String fieldName, String warning) {
    warnings.add(new ReportEntry(fieldName, warning));
  }

  public Iterator<ReportEntry> getErrors() {
    return allErrors.iterator();
  }

  public boolean hasErrors() {
    return !allErrors.isEmpty();
  }

  public void addError(String fieldName, String error) {
    errors.add(new ReportEntry(fieldName, error));
  }

  public void next() {
    if (errors.isEmpty() && warnings.isEmpty()) {
      allReports.addAll(reports);
    }

    if (!warnings.isEmpty()) {
      allWarnings.addAll(warnings);
    }
    if (!errors.isEmpty()) {
      allErrors.addAll(errors);
    }

    errors.clear();
    warnings.clear();
    reports.clear();
  }

  public String build(ReportType reportType, boolean prettyPrint) {
    switch (reportType) {
      case JSON:
        return buildJson(prettyPrint);
      default:
        throw new IllegalArgumentException("Invalid report type option '" + reportType
            + "'. Valid values are " + Arrays.toString(ReportType.values()));
    }
  }

  private String buildJson(boolean prettyPrint) {
    JsonMapper mapper = new JsonMapper();
    ObjectNode rootNode = mapper.createObjectNode();

    if (hasReports()) {
      ArrayNode reportsNode = rootNode.putArray("reports");

      for (ReportEntry report : allReports) {
        reportsNode.add(report.getValue());
      }
    }

    if (hasWarnings()) {
      ArrayNode warningsNode = rootNode.putArray("warnings");

      for (ReportEntry warning : allWarnings) {
        warningsNode.add(warning.getValue());
      }
    }

    if (hasErrors()) {
      ArrayNode errorsNode = rootNode.putArray("errors");

      for (ReportEntry error : allErrors) {
        errorsNode.add(error.getValue());
      }
    }

    if (rootNode.isEmpty()) {
      return "";
    }

    if (prettyPrint) {
      return rootNode.toPrettyString();
    }
    return rootNode.toString();
  }
}
