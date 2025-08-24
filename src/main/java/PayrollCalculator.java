import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * PayrollCalculator is responsible for calculating wages and benefits
 * for employees based on their time punches.
 *
 * Rules:
 * - Regular hours: first 40 hours at normal rate
 * - Overtime: next 8 hours (40–48) at 1.5x rate
 * - Doubletime: any hours beyond 48 at 2x rate
 */

class PayrollCalculator {
    // Map of job name -> job details (rate, benefitsRate)
    private final Map<String, Map<String, Object>> jobRates;
    // List of employee time punch data
    private final List<Map<String, Object>> employeeData;
    // Formatter for parsing date-time strings
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //Constructor: build a jobRates map from job metadata.
    public PayrollCalculator(List<Map<String, Object>> jobMeta, List<Map<String, Object>> employeeData) {
        this.jobRates = new HashMap<>();
        for (Map<String, Object> job : jobMeta) {
            jobRates.put((String) job.get("job"), job);
        }
        this.employeeData = employeeData;
    }

    private LocalDateTime parseDateTime(String dtString) {
        return LocalDateTime.parse(dtString, formatter);
    }

    //Calculate hours worked between start and end timestamps
    private double calculateHoursWorked(String start, String end) {
        LocalDateTime startDt = parseDateTime(start);
        LocalDateTime endDt = parseDateTime(end);
        Duration duration = Duration.between(startDt, endDt);
        return duration.toSeconds() / 3600.0;  // convert seconds to hours
    }

    //Calculate totals (regular/overtime/doubletime hours, wage, benefits) for one employee.
    private Map<String, String> calculateEmployeeTotals(String employeeName, List<Map<String, Object>> timePunches) {
        // Sort punches by start time
        timePunches.sort(Comparator.comparing(p -> parseDateTime((String) p.get("start"))));

        double totalHours = 0;
        double wageTotal = 0;
        double benefitTotal = 0;

        // Process each time punch
        for (Map<String, Object> punch : timePunches) {
            String job = (String) punch.get("job");
            double hours = calculateHoursWorked((String) punch.get("start"), (String) punch.get("end"));
            double jobRate = ((Number) jobRates.get(job).get("rate")).doubleValue();
            double benefitRate = ((Number) jobRates.get(job).get("benefitsRate")).doubleValue();

            double punchWage = 0;
            double hoursRemaining = hours;
            double currentTotal = totalHours;

            // Allocate hours into regular, overtime, doubletime
            while (hoursRemaining > 0) {
                if (currentTotal < 40) { // Regular
                    double regularHoursAvailable = Math.min(hoursRemaining, 40 - currentTotal);
                    punchWage += regularHoursAvailable * jobRate;
                    hoursRemaining -= regularHoursAvailable;
                    currentTotal += regularHoursAvailable;
                } else if (currentTotal < 48) { // Overtime
                    double overtimeHoursAvailable = Math.min(hoursRemaining, 48 - currentTotal);
                    punchWage += overtimeHoursAvailable * jobRate * 1.5;
                    hoursRemaining -= overtimeHoursAvailable;
                    currentTotal += overtimeHoursAvailable;
                } else { // Doubletime
                    punchWage += hoursRemaining * jobRate * 2.0;
                    currentTotal += hoursRemaining;
                    hoursRemaining = 0;
                }
            }
            // Accumulate totals
            double punchBenefit = hours * benefitRate;
            wageTotal += punchWage;
            benefitTotal += punchBenefit;
            totalHours += hours;
        }

        // Breakdown of hours
        double regularHours = Math.min(totalHours, 40.0);
        double overtimeHours = Math.max(0, Math.min(totalHours - 40, 8.0));
        double doubletimeHours = Math.max(0, totalHours - 48);

        // Format result map
        Map<String, String> result = new LinkedHashMap<>();
        result.put("employee", employeeName);
        result.put("regular", String.format("%.4f", regularHours));
        result.put("overtime", String.format("%.4f", overtimeHours));
        result.put("doubletime", String.format("%.4f", doubletimeHours));
        result.put("wageTotal", String.format("%.4f", wageTotal));
        result.put("benefitTotal", String.format("%.4f", benefitTotal));
        return result;
    }
    //Calculate payroll for all employees.
    public Map<String, Map<String, String>> calculateAllEmployees() {
        Map<String, Map<String, String>> results = new LinkedHashMap<>();
        for (Map<String, Object> employeeRecord : employeeData) {
            String employeeName = (String) employeeRecord.get("employee");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> timePunches = (List<Map<String, Object>>) employeeRecord.get("timePunch");
            results.put(employeeName, calculateEmployeeTotals(employeeName, timePunches));
        }
        return results;
    }
}


