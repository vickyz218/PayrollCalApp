# Payroll Calculator

## Overview
This is a Java Maven project that calculates payroll for employees based on job time punches. 
The calculator handles:
- Regular hours (0-40)
- Overtime hours (40-48)
- Double time (48+)
- Benefit calculations (per hour, independent of overtime)

The project uses **Jackson** for JSON parsing.

## Project Structure
```text
PayrollCalApp/
├─ src/
│ └─ main/
│ ├─ java/
│ │ ├─ PayrollCalculator.java
│ │ └─ PayrollMain.java
│ └─ resources/
│ └─ PunchLogicTest.jsonc
├─ pom.xml
```

## How to Run
1. Ensure you have **Java 17** and **Maven** installed.
2. Build the project:
```bash
mvn clean compile
3.Run the project:
mvn exec:java -Dexec.mainClass="PayrollMain"

Alternatively, run PayrollMain directly from IntelliJ IDEA.
Note: Make sure PunchLogicTest.jsonc is placed in src/main/resources so the program can find it.
