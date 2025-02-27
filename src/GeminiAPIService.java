// File: GeminiAPIService.java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiAPIService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-2:generateContent";
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
                    response.append(responseLine);
                }
            }
            return extractTextFromResponse(response.toString());
        } else {
            // Handle error response
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine);
                }
            }
            throw new Exception("API call failed with status code: " + responseCode + 
                               "\nError details: " + errorResponse.toString());
        }
    }
    
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    // Improved method to extract text from Gemini API response
    public String extractTextFromResponse(String apiResponse) {
        try {
            // Use a more robust regex pattern to extract text content
            // This handles the nested JSON structure and properly deals with escaped quotes
            Pattern pattern = Pattern.compile("\"text\":\\s*\"((?:\\\\.|[^\\\\\"])*?)\"");
            Matcher matcher = pattern.matcher(apiResponse);
            
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String matchedText = matcher.group(1);
                // Unescape JSON string escapes
                String unescaped = matchedText
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
                    
                result.append(unescaped);
            }
            
            if (result.length() > 0) {
                return result.toString();
            } else {
                // Fallback to more complex JSON parsing if regex doesn't find matches
                // This is a simplified parsing approach for specific Gemini API response format
                int candidatesStart = apiResponse.indexOf("\"candidates\"");
                if (candidatesStart >= 0) {
                    int contentStart = apiResponse.indexOf("\"content\"", candidatesStart);
                    if (contentStart >= 0) {
                        int partsStart = apiResponse.indexOf("\"parts\"", contentStart);
                        if (partsStart >= 0) {
                            int textStart = apiResponse.indexOf("\"text\"", partsStart);
                            if (textStart >= 0) {
                                int valueStart = apiResponse.indexOf("\"", textStart + 7) + 1;
                                int valueEnd = findMatchingQuoteEnd(apiResponse, valueStart);
                                
                                if (valueEnd > valueStart) {
                                    String extractedText = apiResponse.substring(valueStart, valueEnd);
                                    return extractedText
                                        .replace("\\n", "\n")
                                        .replace("\\r", "\r")
                                        .replace("\\t", "\t")
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\");
                                }
                            }
                        }
                    }
                }
                
                return "Failed to extract text from API response. Response format might have changed.";
            }
        } catch (Exception e) {
            return "Error processing API response: " + e.getMessage() + 
                   "\nResponse received: " + apiResponse.substring(0, Math.min(100, apiResponse.length())) + "...";
        }
    }
    
    // Helper method to find the end of a JSON string value considering escaped quotes
    private int findMatchingQuoteEnd(String json, int startPos) {
        for (int i = startPos; i < json.length(); i++) {
            // If we find a backslash, skip the next character as it's escaped
            if (json.charAt(i) == '\\') {
                i++;
                continue;
            }
            // If we find a quote that's not escaped, it's the end of the string
            if (json.charAt(i) == '"') {
                return i;
            }
        }
        return -1; // No matching quote found
    }
    
    // Parse specific sections from the Gemini response for code analysis
    public CodeAnalysis parseComplexityAnalysis(String analysisText, String language, String code) {
        CodeAnalysis analysis = new CodeAnalysis(language, code);
        
        // Extract time complexity
        Pattern timePattern = Pattern.compile("(?i)Time\\s+Complexity\\s*:?\\s*([^\\n]+)");
        Matcher timeMatcher = timePattern.matcher(analysisText);
        if (timeMatcher.find()) {
            analysis.setTimeComplexity(timeMatcher.group(1).trim());
        } else {
            analysis.setTimeComplexity("Not specified");
        }
        
        // Extract space complexity
        Pattern spacePattern = Pattern.compile("(?i)Space\\s+Complexity\\s*:?\\s*([^\\n]+)");
        Matcher spaceMatcher = spacePattern.matcher(analysisText);
        if (spaceMatcher.find()) {
            analysis.setSpaceComplexity(spaceMatcher.group(1).trim());
        } else {
            analysis.setSpaceComplexity("Not specified");
        }
        
        // Extract explanation (everything else)
        String timeSection = "Time Complexity";
        String spaceSection = "Space Complexity";
        String explanationSection = "Explanation";
        
        int explanationStart = -1;
        
        // Try to find an Explanation section
        int explicitExplanationStart = analysisText.indexOf(explanationSection);
        if (explicitExplanationStart >= 0) {
            // Find the colon or newline after "Explanation"
            int colonPos = analysisText.indexOf(":", explicitExplanationStart);
            int newlinePos = analysisText.indexOf("\n", explicitExplanationStart);
            
            if (colonPos >= 0 && (newlinePos < 0 || colonPos < newlinePos)) {
                explanationStart = colonPos + 1;
            } else if (newlinePos >= 0) {
                explanationStart = newlinePos + 1;
            }
        }
        
        // If no explicit Explanation section, try to infer it after Time and Space sections
        if (explanationStart < 0) {
            int timePos = analysisText.indexOf(timeSection);
            int spacePos = analysisText.indexOf(spaceSection);
            
            if (timePos >= 0 && spacePos >= 0) {
                // Find which section comes later
                int laterSection = Math.max(timePos, spacePos);
                
                // Find the end of that section (next newline after the complexity value)
                int sectionValueEnd = analysisText.indexOf("\n", laterSection + 20); // Approximate position
                
                if (sectionValueEnd >= 0) {
                    explanationStart = sectionValueEnd + 1;
                }
            }
        }
        
        // If we found a start position for explanation
        if (explanationStart >= 0) {
            analysis.setExplanation(analysisText.substring(explanationStart).trim());
        } else {
            // Fallback: Just use the whole text as explanation
            analysis.setExplanation(analysisText);
        }
        
        return analysis;
    }
    
    // Parse optimization suggestions
    public String parseOptimizationSuggestions(String optimizationText) {
        // For optimization text, we usually just want to return the entire response
        // since it's already structured by the AI model
        return optimizationText.trim();
    }
}