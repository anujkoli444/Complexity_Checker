import javax.swing.*;
import java.awt.*;

public class UIThemeManager {
    // Light theme colors
    public static final Color LIGHT_BG = new Color(255, 255, 255);
    public static final Color LIGHT_TEXT = new Color(30, 30, 30);
    public static final Color LIGHT_CODE_BG = new Color(250, 250, 250);
    public static final Color LIGHT_BUTTON_BG = new Color(230, 230, 230);
    
    // Dark theme colors
    public static final Color DARK_BG = new Color(35, 39, 46);
    public static final Color DARK_TEXT = new Color(220, 220, 220);
    public static final Color DARK_CODE_BG = new Color(25, 29, 36);
    public static final Color DARK_BUTTON_BG = new Color(60, 63, 65);
    
    private boolean isDarkMode;
    
    public UIThemeManager(boolean startInDarkMode) {
        this.isDarkMode = startInDarkMode;
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public void toggleDarkMode() {
        isDarkMode = !isDarkMode;
    }
    
    public void applyThemeToComponent(Component component) {
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color textColor = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color codeAreaBg = isDarkMode ? DARK_CODE_BG : LIGHT_CODE_BG;
        Color buttonBg = isDarkMode ? DARK_BUTTON_BG : LIGHT_BUTTON_BG;
        
        if (component instanceof JTextArea) {
            component.setBackground(codeAreaBg);
            component.setForeground(textColor);
            if (component instanceof JTextArea) {
                ((JTextArea) component).setCaretColor(textColor);
            }
        } else if (component instanceof JPanel || component instanceof JSplitPane) {
            component.setBackground(bgColor);
            component.setForeground(textColor);
        } else if (component instanceof JButton && !(component instanceof JToggleButton)) {
            component.setBackground(buttonBg);
            component.setForeground(textColor);
        } else if (component instanceof JLabel || component instanceof JComboBox) {
            component.setForeground(textColor);
            if (component instanceof JComboBox) {
                component.setBackground(isDarkMode ? DARK_BUTTON_BG : null);
            }
        }
        
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeToComponent(child);
            }
        }
    }
    
    // Apply theme to an entire window/frame
    public void applyTheme(Window window) {
        for (Component comp : window.getComponents()) {
            applyThemeToComponent(comp);
        }
        window.repaint();
    }
}