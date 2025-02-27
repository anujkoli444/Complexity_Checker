#!/bin/bash

# Compile the Java files
echo "Compiling Java files..."
javac -d . src/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful! Running application..."
    java CodeComplexityAnalyzer
else
    echo "Compilation failed. Please check the errors above."
fi