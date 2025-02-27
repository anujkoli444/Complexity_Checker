import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiAPIService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private String apiKey;
    
    public GeminiAPIService(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String analyzeCodeComplexity(String code, String language) throws Exception {
        String prompt = createComplexityAnalysisPrompt(code, language);
        return callAPI(prompt);
    }
    
    public String suggestOptimizations(String code, String language) throws Exception {
        String prompt = createOptimizationPrompt(code, language);
        return callAPI(prompt);
    }
    
    private String createComplexityAnalysisPrompt(String code, String language) {
        return "Analyze the following " + language + " code and determine its time complexity and space complexity. " +
               "Provide a detailed analysis explaining why. " +
               "Focus on the algorithm's efficiency, not just language-specific details. " +
               "Identify any nested loops, recursive calls, or other complex structures. " +
               "Format your response clearly with sections for Time Complexity, Space Complexity, and Explanation. " +
               "Here's the code:\n\n" + code;
    }
    
    private String createOptimizationPrompt(String code, String language) {
        return "Review the following " + language + " code and suggest specific optimizations to improve its efficiency. " +
               "Focus on algorithmic improvements, not just style changes. " +
               "Explain why each suggestion would improve performance. " +
               "Include code examples where appropriate. " +
               "Here's the code:\n\n" + code;
    }
    
    private String callAPI(String prompt) throws Exception {
        URL url = new URL(API_URL + "?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // Prepare JSON payload
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + 
                              escapeJsonString(prompt) + "\"}]}]}";
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return response.toString();
        } else {
            throw new Exception("API call failed with status code: " + responseCode);
        }
    }
    
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    // Helper method to extract text from Gemini API response
    public String extractTextFromResponse(String apiResponse) {
        try {
            // Simple extraction - in production you should use a proper JSON parser
            int textStartIndex = apiResponse.indexOf("\"text\":\"") + 8;
            int textEndIndex = apiResponse.indexOf("\"}", textStartIndex);
            
            if (textStartIndex > 8 && textEndIndex > textStartIndex) {
                return apiResponse.substring(textStartIndex, textEndIndex)
                                 .replace("\\n", "\n")
                                 .replace("\\\"", "\"");
            } else {
                return "Failed to parse API response.";
            }
        } catch (Exception e) {
            return "Error processing API response: " + e.getMessage();
        }
    }
}