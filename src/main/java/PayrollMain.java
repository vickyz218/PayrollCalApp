import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Main class to run payroll calculation.
 * Reads PunchLogicTest.jsonc from resources, parses it,
 * and computes payroll using PayrollCalculator.
 */
public class PayrollMain {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Load JSONC file from resources folder
        InputStream is = PayrollMain.class.getClassLoader().getResourceAsStream("PunchLogicTest.jsonc");
        if (is == null) {
            throw new RuntimeException("Cannot find PunchLogicTest.jsonc in resources");
        }
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // If the JSONC file contains comments, they need to be removed
        int jsonStart = content.indexOf("{");
        String jsonContent = content.substring(jsonStart);

        // Parse JSON content into a Map
        Map<String, Object> data = mapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});

        // Extract job meta and employee data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jobMeta = (List<Map<String, Object>>) data.get("jobMeta");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> employeeData = (List<Map<String, Object>>) data.get("employeeData");

        PayrollCalculator calculator = new PayrollCalculator(jobMeta, employeeData);
        // Calculate payroll for all employees
        Map<String, Map<String, String>> results = calculator.calculateAllEmployees();

        // Print results in pretty JSON format
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
    }
}
