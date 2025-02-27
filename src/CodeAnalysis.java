public class CodeAnalysis {
    private String language;
    private String code;
    private String timeComplexity;
    private String spaceComplexity;
    private String explanation;
    private String optimizationSuggestions;
    
    public CodeAnalysis(String language, String code) {
        this.language = language;
        this.code = code;
    }
    
    // Getters and setters
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getTimeComplexity() {
        return timeComplexity;
    }
    
    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }
    
    public String getSpaceComplexity() {
        return spaceComplexity;
    }
    
    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getOptimizationSuggestions() {
        return optimizationSuggestions;
    }
    
    public void setOptimizationSuggestions(String optimizationSuggestions) {
        this.optimizationSuggestions = optimizationSuggestions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Language: ").append(language).append("\n");
        sb.append("Time Complexity: ").append(timeComplexity).append("\n");
        sb.append("Space Complexity: ").append(spaceComplexity).append("\n\n");
        sb.append("Explanation:\n").append(explanation).append("\n\n");
        
        if (optimizationSuggestions != null && !optimizationSuggestions.isEmpty()) {
            sb.append("Optimization Suggestions:\n").append(optimizationSuggestions);
        }
        
        return sb.toString();
    }
}