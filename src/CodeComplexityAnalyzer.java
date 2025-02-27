import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeComplexityAnalyzer extends JFrame {
    private JTextArea codeInputArea;
    private JTextArea resultArea;
    private JComboBox<String> languageSelector;
    private JButton analyzeButton;
    private JButton optimizeButton;
    private JToggleButton darkModeToggle;
    private boolean isDarkMode = false;
    private ExecutorService executorService;
    
    // Colors for light and dark mode
    private final Color LIGHT_BG = new Color(255, 255, 255);
    private final Color LIGHT_TEXT = new Color(30, 30, 30);
    private final Color DARK_BG = new Color(35, 39, 46);
    private final Color DARK_TEXT = new Color(220, 220, 220);
    
    // Supported languages
    private final String[] SUPPORTED_LANGUAGES = {
        "Java", "Python", "C", "C++", "JavaScript", "Rust"
    };
    
    // Gemini API key - replace with your actual API key
    private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    public CodeComplexityAnalyzer() {
        // Initialize the executor service for handling API calls
        executorService = Executors.newFixedThreadPool(2);
        
        // Setup the UI
        setupUI();
        
        // Configure window
        setTitle("Code Complexity Analyzer");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void setupUI() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Top panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Language selector
        languageSelector = new JComboBox<>(SUPPORTED_LANGUAGES);
        controlPanel.add(new JLabel("Language:"));
        controlPanel.add(languageSelector);
        
        // Analyze button
        analyzeButton = new JButton("Analyze Complexity");
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeCode();
            }
        });
        controlPanel.add(analyzeButton);
        
        // Optimize button
        optimizeButton = new JButton("Suggest Optimizations");
        optimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                suggestOptimizations();
            }
        });
        controlPanel.add(optimizeButton);
        
        // Dark mode toggle
        darkModeToggle = new JToggleButton("Dark Mode");
        darkModeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDarkMode();
            }
        });
        controlPanel.add(darkModeToggle);
        
        // Add control panel to the top
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Center panel with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        // Code input area
        codeInputArea = new JTextArea();
        codeInputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScrollPane = new JScrollPane(codeInputArea);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Code Input"));
        
        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Analysis Results"));
        
        // Add panels to split pane
        splitPane.add(codeScrollPane);
        splitPane.add(resultScrollPane);
        
        // Add split pane to main panel
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Add main panel to frame
        setContentPane(mainPanel);
    }
    
    private void analyzeCode() {
        String code = codeInputArea.getText().trim();
        if (code.isEmpty()) {
            showError("Please enter code to analyze.");
            return;
        }
        
        String language = (String) languageSelector.getSelectedItem();
        
        // Disable button and show progress
        analyzeButton.setEnabled(false);
        resultArea.setText("Analyzing code complexity...");
        
        // Use a thread to avoid freezing the UI
        executorService.submit(() -> {
            try {
                String prompt = createComplexityAnalysisPrompt(code, language);
                String response = callGeminiAPI(prompt);
                
                // Process and format the response
                String formattedResult = processComplexityResponse(response);
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText(formattedResult);
                    analyzeButton.setEnabled(true);
                });
            } catch (Exception e) {
                handleAPIError(e);
            }
        });
    }
    
    private void suggestOptimizations() {
        String code = codeInputArea.getText().trim();
        if (code.isEmpty()) {
            showError("Please enter code to optimize.");
            return;
        }
        
        String language = (String) languageSelector.getSelectedItem();
        
        // Disable button and show progress
        optimizeButton.setEnabled(false);
        resultArea.setText("Generating optimization suggestions...");
        
        // Use a thread to avoid freezing the UI
        executorService.submit(() -> {
            try {
                String prompt = createOptimizationPrompt(code, language);
                String response = callGeminiAPI(prompt);
                
                // Process and format the response
                String formattedResult = processOptimizationResponse(response);
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    resultArea.setText(formattedResult);
                    optimizeButton.setEnabled(true);
                });
            } catch (Exception e) {
                handleAPIError(e);
            }
        });
    }
    
    private String createComplexityAnalysisPrompt(String code, String language) {
        return "Analyze the following " + language + " code and determine its time complexity and space complexity. " +
               "Provide a detailed analysis explaining why. " +
               "Focus on the algorithm's efficiency, not just language-specific details. " +
               "Identify any nested loops, recursive calls, or other complex structures. " +
               "Here's the code:\n\n" + code;
    }
    
    private String createOptimizationPrompt(String code, String language) {
        return "Review the following " + language + " code and suggest specific optimizations to improve its efficiency. " +
               "Focus on algorithmic improvements, not just style changes. " +
               "Explain why each suggestion would improve performance. " +
               "Include code examples where appropriate. " +
               "Here's the code:\n\n" + code;
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        URL url = new URL(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
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
    
    private String processComplexityResponse(String apiResponse) {
        // In a real implementation, you would parse the JSON response
        // This is a simplified version
        try {
            // Extract the text content from Gemini API response
            // This is a very simple parser and should be replaced with a proper JSON parser
            int textStartIndex = apiResponse.indexOf("\"text\":\"") + 8;
            int textEndIndex = apiResponse.indexOf("\"}", textStartIndex);
            
            if (textStartIndex > 8 && textEndIndex > textStartIndex) {
                String extractedText = apiResponse.substring(textStartIndex, textEndIndex)
                                                  .replace("\\n", "\n")
                                                  .replace("\\\"", "\"");
                
                return "COMPLEXITY ANALYSIS:\n\n" + extractedText;
            } else {
                return "Failed to parse API response. Please try again.";
            }
        } catch (Exception e) {
            return "Error processing API response: " + e.getMessage();
        }
    }
    
    private String processOptimizationResponse(String apiResponse) {
        // Similar to processComplexityResponse but for optimization results
        try {
            // Extract the text content from Gemini API response
            int textStartIndex = apiResponse.indexOf("\"text\":\"") + 8;
            int textEndIndex = apiResponse.indexOf("\"}", textStartIndex);
            
            if (textStartIndex > 8 && textEndIndex > textStartIndex) {
                String extractedText = apiResponse.substring(textStartIndex, textEndIndex)
                                                  .replace("\\n", "\n")
                                                  .replace("\\\"", "\"");
                
                return "OPTIMIZATION SUGGESTIONS:\n\n" + extractedText;
            } else {
                return "Failed to parse API response. Please try again.";
            }
        } catch (Exception e) {
            return "Error processing API response: " + e.getMessage();
        }
    }
    
    private void handleAPIError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            resultArea.setText("Error: " + e.getMessage());
            analyzeButton.setEnabled(true);
            optimizeButton.setEnabled(true);
        });
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }
    
    private void applyTheme() {
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color textColor = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color codeAreaBg = isDarkMode ? new Color(25, 29, 36) : new Color(250, 250, 250);
        
        // Update component colors
        codeInputArea.setBackground(codeAreaBg);
        codeInputArea.setForeground(textColor);
        codeInputArea.setCaretColor(textColor);
        
        resultArea.setBackground(codeAreaBg);
        resultArea.setForeground(textColor);
        
        // Update all panels
        updateComponentsWithTheme(this.getContentPane(), bgColor, textColor);
        
        // Force repaint
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    private void updateComponentsWithTheme(Component component, Color bgColor, Color fgColor) {
        if (component instanceof JPanel) {
            component.setBackground(bgColor);
            component.setForeground(fgColor);
        } else if (component instanceof JButton && !(component instanceof JToggleButton)) {
            component.setBackground(isDarkMode ? new Color(60, 63, 65) : null);
            component.setForeground(fgColor);
        } else if (component instanceof JLabel) {
            component.setForeground(fgColor);
        }
        
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                updateComponentsWithTheme(child, bgColor, fgColor);
            }
        }
    }
    
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new CodeComplexityAnalyzer().setVisible(true);
        });
    }
    
    // Clean up resources when done
    @Override
    public void dispose() {
        executorService.shutdown();
        super.dispose();
    }
}