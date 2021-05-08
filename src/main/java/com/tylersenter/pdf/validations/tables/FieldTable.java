package com.tylersenter.pdf.validations.tables;

public class FieldTable {
  private String name;

  private int step;
  private int start;
  private int end;

  public FieldTable(String name) {
    this.name = name;
    this.step = 1;
    this.start = 1;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getStep() {
    return step;
  }

  public void setStep(int step) {
    this.step = step;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }
}
