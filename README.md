# Code Complexity Analyzer

A Java application that analyzes code complexity and suggests optimizations for various programming languages using Gemini AI.

## Features

- **Multi-language Support**: Analyze code in Java, Python, C, C++, JavaScript, and Rust
- **Time & Space Complexity Analysis**: Get detailed complexity analysis powered by Gemini AI
- **Code Optimization Suggestions**: Receive AI-generated optimization recommendations
- **Dark Mode**: Modern UI with light and dark themes
- **User-friendly Interface**: Simple split-pane design with syntax highlighting

## Requirements

- Java 8 or higher
- Google Gemini API key (gemini-flash-2 model)

## Setup

1. Clone this repository
2. Add your Gemini API key in the `CodeComplexityAnalyzer.java` file
3. Compile and run using the included script:

```bash
./run.sh
```

## How It Works

The application leverages Google's Gemini AI (flash-2 model) to analyze code and determine its complexity without relying on predefined rules. It sends the code to the Gemini API and processes the response to extract meaningful insights.

The analysis provides:
- Time complexity (Big O notation)
- Space complexity
- Detailed explanation of the analysis
- Targeted optimization suggestions

## OOP Structure

This project follows object-oriented principles:
- **CodeComplexityAnalyzer**: Main application class with UI components
- **CodeAnalysis**: Model class that stores analysis results
- **GeminiAPIService**: Service class for API interactions
- **UIThemeManager**: Class for handling UI themes

## Multithreading

The application uses a thread pool (ExecutorService) to make asynchronous API calls, ensuring the UI remains responsive during analysis.

## Future Improvements

- Add support for more programming languages
- Implement file import/export functionality
- Add code history with comparison features
- Integrate with IDEs as a plugin

## License

MIT License