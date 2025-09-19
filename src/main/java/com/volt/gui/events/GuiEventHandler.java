package com.volt.gui.events;

import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.*;
import com.volt.gui.components.ColorPicker;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiEventHandler {
    private static final int SIDEBAR_WIDTH = 180;
    private static final int CONTAINER_WIDTH = SIDEBAR_WIDTH + 600;
    private static final int HEADER_HEIGHT = 60;
    private static final int MODULE_HEIGHT = 35;
    private static final int PADDING = 18;
    private static final int SETTING_HEIGHT = 28;
    
    private final Map<Module, Boolean> moduleExpanded;
    private final Map<NumberSetting, Boolean> sliderDragging;
    private final Map<ModeSetting, Boolean> dropdownExpanded = new HashMap<>();
    private final Map<ColorSetting, Boolean> colorPickerExpanded = new HashMap<>();
    private final Map<ColorSetting, ColorPicker> colorPickers = new HashMap<>();
    private KeybindSetting listeningKeybind = null;
    private boolean wasListeningKeybind = false;
    private NumberSetting editingNumberSetting = null;
    private String numberInputText = "";
    private Category selectedCategory;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0; 
    private String searchQuery = "";
    private boolean searchFocused = false;
    
    public GuiEventHandler(Map<Module, Boolean> moduleExpanded, Map<NumberSetting, Boolean> sliderDragging, Category selectedCategory) {
        this.moduleExpanded = moduleExpanded;
        this.sliderDragging = sliderDragging;
        this.selectedCategory = selectedCategory;
    }
    
    public boolean handleMouseClick(double mouseX, double mouseY, int button, int screenWidth, int screenHeight, List<Module> modules) {
        if (listeningKeybind != null) {
            wasListeningKeybind = true;
            if (button == 0) {
                listeningKeybind.setKeyCode(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            } else if (button == 1) {
                listeningKeybind.setKeyCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            } else if (button == 2) {
                listeningKeybind.setKeyCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            } else {
                listeningKeybind.setKeyCode(button);
            }
            listeningKeybind.setListening(false);
            listeningKeybind = null;
            return true;
        }
        
        if (button < 0 || button > 8) return false;
        
        int containerX = (screenWidth - CONTAINER_WIDTH) / 2;
        int containerY = (screenHeight - 500) / 2;
        int containerHeight = 500;
        
        if (handleContentClick(mouseX, mouseY, containerX, containerY, containerHeight, modules, button)) {
            return true;
        }
        
        if (isAnyDropdownExpanded()) {
            closeAllDropdowns();
            return true;
        }
        
        if (button == 0) {
            if (handleSidebarClick(mouseX, mouseY, containerX, containerY)) {
                return true;
            }
            
            if (handleSearchBarClick(mouseX, mouseY, containerX, containerY)) {
                return true;
            }
            
            if (editingNumberSetting != null) {
                setEditingNumberSetting(null);
                return true;
            }
        }
        
        return false;
    }
    
    private boolean handleSidebarClick(double mouseX, double mouseY, int containerX, int containerY) {
        int sidebarX = containerX;
        int sidebarY = containerY + HEADER_HEIGHT;
        
        if (mouseX >= sidebarX && mouseX <= sidebarX + SIDEBAR_WIDTH && mouseY >= sidebarY) {
            int categoryY = sidebarY + PADDING;
            
            for (Category category : Category.values()) {
                if (mouseY >= categoryY && mouseY <= categoryY + 35) {
                    selectedCategory = category;
                    scrollOffset = 0;
                    return true;
                }
                categoryY += 45;
            }
        }
        
        return false;
    }
    
    private boolean handleContentClick(double mouseX, double mouseY, int containerX, int containerY, int containerHeight, List<Module> modules, int button) {
        int contentX = containerX + SIDEBAR_WIDTH;
        int contentY = containerY + HEADER_HEIGHT;
        int contentWidth = CONTAINER_WIDTH - SIDEBAR_WIDTH;
        
        if (mouseX >= contentX && mouseX <= contentX + contentWidth && mouseY >= contentY && mouseY <= contentY + containerHeight - HEADER_HEIGHT) {
            int moduleY = contentY + PADDING - scrollOffset;
            
            for (Module module : modules) {
                int moduleHeight = MODULE_HEIGHT + 5;
                if (moduleExpanded.get(module)) {
                    moduleHeight += getModuleSettingsHeight(module);
                }
                
                if (moduleY + moduleHeight < contentY) {
                    moduleY += moduleHeight;
                    continue;
                }
                
                if (moduleY > contentY + containerHeight - HEADER_HEIGHT) break;
                
                if (mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT) {
                    boolean hasSettings = module.getSettings() != null && !module.getSettings().isEmpty();
                    
                    if (button == 1) {
                        if (hasSettings) {
                            boolean now = !moduleExpanded.get(module);
                            moduleExpanded.put(module, now);
                            return true;
                        }
                    } else if (button == 0) {
                        if (hasSettings && mouseX >= contentX + contentWidth - PADDING - 30 && mouseX <= contentX + contentWidth - PADDING) {
                            boolean now = !moduleExpanded.get(module);
                            moduleExpanded.put(module, now);
                            return true;
                        }
                        
                        module.toggle();
                        return true;
                    } else if (button >= 2 && button <= 8) {
                        module.toggle();
                        return true;
                    }
                }
                
                if (button == 0 && moduleExpanded.get(module) && mouseY > moduleY + MODULE_HEIGHT + 5) {
                    if (handleSettingClick(mouseX, mouseY, module, contentX, moduleY + MODULE_HEIGHT + 5, contentWidth)) {
                        return true;
                    }
                }
                
                moduleY += moduleHeight;
            }
        }
        
        return false;
    }
    
    private boolean handleSettingClick(double mouseX, double mouseY, Module module, int contentX, int settingsY, int contentWidth) {
        if (module.getSettings() == null || module.getSettings().isEmpty()) {
            return false;
        }
        
        int maxControlWidth = Math.min(100, contentWidth - PADDING - 80);
        int settingY = settingsY + 5;

        for (Setting setting : module.getSettings()) {
            if (setting instanceof ModeSetting modeSetting && dropdownExpanded.getOrDefault(modeSetting, false)) {
                int controlX = contentX + contentWidth - PADDING - maxControlWidth - 12;
                int controlY = settingY + (SETTING_HEIGHT - 15) / 2;
                int controlWidth = maxControlWidth;
                int controlHeight = 15;

                int dropdownEndY = controlY + controlHeight + (modeSetting.getModes().size() * controlHeight);
                if (mouseX >= controlX && mouseX <= controlX + controlWidth && 
                    mouseY >= controlY && mouseY <= dropdownEndY) {
                    return handleSpecificSettingClick(setting, mouseX, mouseY, controlX, controlY, controlWidth);
                }
            }
            settingY += SETTING_HEIGHT;
        }

        settingY = settingsY + 5;
        
        for (Setting setting : module.getSettings()) {
            if (mouseY >= settingY && mouseY <= settingY + SETTING_HEIGHT) {
                int controlX = contentX + contentWidth - PADDING - maxControlWidth - 12;
                int controlY = settingY + (SETTING_HEIGHT - 15) / 2;
                int controlWidth = maxControlWidth;
                
                if (mouseX >= controlX && mouseX <= controlX + controlWidth) {
                    return handleSpecificSettingClick(setting, mouseX, mouseY, controlX, controlY, controlWidth);
                }
            }
            settingY += SETTING_HEIGHT;
        }
        
        return false;
    }
    
    private boolean handleSpecificSettingClick(Setting setting, double mouseX, double mouseY, int controlX, int controlY, int controlWidth) {
        switch (setting) {
            case BooleanSetting booleanSetting -> {
                booleanSetting.setValue(!booleanSetting.getValue());
                return true;
            }
            
            case NumberSetting numberSetting -> {
                controlWidth = (int) (((double) controlWidth) * .75);
                
                int valueTextX = controlX + controlWidth + 5;
                String valueText = String.valueOf(numberSetting.getValue());
                if (valueText.endsWith(".0")) {
                    valueText = valueText.substring(0, valueText.length() - 2);
                }
                int valueTextWidth = net.minecraft.client.MinecraftClient.getInstance().textRenderer.getWidth(valueText);
                
                if (mouseX >= valueTextX && mouseX <= valueTextX + valueTextWidth && 
                    mouseY >= controlY && mouseY <= controlY + SETTING_HEIGHT) {
                    setEditingNumberSetting(numberSetting);
                    return true;
                } else {
                    double relativeX = mouseX - controlX;
                    double percentage = Math.max(0, Math.min(1, relativeX / controlWidth));
                    double newValue = numberSetting.getMin() + percentage * (numberSetting.getMax() - numberSetting.getMin());
                    numberSetting.setValue(newValue);
                    sliderDragging.put(numberSetting, true);
                    return true;
                }
            }
            
            case ModeSetting modeSetting -> {
                int dropdownArrowX = controlX + controlWidth - 15;
                if (mouseX >= dropdownArrowX) {
                    closeAllDropdowns();
                    dropdownExpanded.put(modeSetting, !dropdownExpanded.getOrDefault(modeSetting, false));
                } else {
                    if (dropdownExpanded.getOrDefault(modeSetting, false)) {
                        int optionIndex = getDropdownOptionIndex(mouseY, controlY, modeSetting);
                        if (optionIndex >= 0 && optionIndex < modeSetting.getModes().size()) {
                            modeSetting.setMode(modeSetting.getModes().get(optionIndex));
                            dropdownExpanded.put(modeSetting, false);
                        }
                    } else {
                        closeAllDropdowns();
                        dropdownExpanded.put(modeSetting, true);
                    }
                }
                return true;
            }
            
            case KeybindSetting keybindSetting -> {
                if (listeningKeybind != null) {
                    listeningKeybind.setListening(false);
                }
                keybindSetting.setListening(!keybindSetting.isListening());
                listeningKeybind = keybindSetting.isListening() ? keybindSetting : null;
                return true;
            }
            
            case ColorSetting colorSetting -> {
                boolean isExpanded = colorPickerExpanded.getOrDefault(colorSetting, false);
                closeAllColorPickers();
                if (!isExpanded) {
                    colorPickerExpanded.put(colorSetting, true);
                    if (!colorPickers.containsKey(colorSetting)) {
                        colorPickers.put(colorSetting, new ColorPicker(colorSetting));
                    }
                }
                return true;
            }
            
            default -> {
                return false;
            }
        }
    }
    
    private boolean handleSearchBarClick(double mouseX, double mouseY, int containerX, int containerY) {
        int searchX = containerX + CONTAINER_WIDTH - 200 - PADDING;
        int searchY = containerY + (HEADER_HEIGHT - 25) / 2;
        
        if (mouseX >= searchX && mouseX <= searchX + 200 && mouseY >= searchY && mouseY <= searchY + 25) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }
        
        return false;
    }
    
    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY, int screenWidth, int screenHeight) {
        if (button == 0) {
            for (Map.Entry<NumberSetting, Boolean> entry : sliderDragging.entrySet()) {
                if (entry.getValue()) {
                    NumberSetting numberSetting = entry.getKey();
                    
                    int containerX = (screenWidth - CONTAINER_WIDTH) / 2;
                    int contentX = containerX + SIDEBAR_WIDTH;
                    int contentWidth = CONTAINER_WIDTH - SIDEBAR_WIDTH;
                    
                    int maxControlWidth = Math.min(100, contentWidth - PADDING - 80);
                    int controlX = contentX + contentWidth - PADDING - maxControlWidth - 12;
                    int controlWidth = maxControlWidth;
                    controlWidth = (int) (((double) controlWidth) * .75);
                    
                    double relativeX = mouseX - controlX;
                    double percentage = Math.max(0, Math.min(1, relativeX / controlWidth));
                    double newValue = numberSetting.getMin() + percentage * (numberSetting.getMax() - numberSetting.getMin());
                    numberSetting.setValue(newValue);
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (button == 0) {
            sliderDragging.replaceAll((k, v) -> false);
        }
        
        return false;
    }
    
    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (editingNumberSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                try {
                    double newValue = Double.parseDouble(numberInputText);
                    double min = editingNumberSetting.getMin();
                    double max = editingNumberSetting.getMax();
                    newValue = Math.max(min, Math.min(max, newValue));
                    editingNumberSetting.setValue(newValue);
                } catch (NumberFormatException e) {
                }
                setEditingNumberSetting(null);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                setEditingNumberSetting(null);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!numberInputText.isEmpty()) {
                    numberInputText = numberInputText.substring(0, numberInputText.length() - 1);
                }
                return true;
            }
            return true;
        }
        
        if (listeningKeybind != null) {
            wasListeningKeybind = true;
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningKeybind.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                listeningKeybind.setKeyCode(keyCode);
            }
            listeningKeybind.setListening(false);
            listeningKeybind = null;
            return true;
        }
        
        wasListeningKeybind = false;
        
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            if (!searchFocused) {
                searchFocused = true;
            }
            return true;
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && (!searchQuery.isEmpty() || searchFocused)) {
            searchFocused = false;
            searchQuery = "";
            return true;
        }
        
        if (searchFocused) {
            return handleSearchInput(keyCode, scanCode, modifiers);
        }
        
        return false;
    }
    
    public boolean handleCharTyped(char chr, int modifiers) {
        if (editingNumberSetting != null) {
            if (Character.isDigit(chr) || chr == '.' || chr == '-') {
                if (chr == '.' && numberInputText.contains(".")) {
                    return true;
                }
                if (chr == '-' && !numberInputText.isEmpty()) {
                    return true;
                }
                
                numberInputText += chr;
            }
            return true;
        }
        
        if ((listeningKeybind != null && listeningKeybind.isListening()) || wasListeningKeybind) {
            return true;
        }
        
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            if (!searchFocused) {
                searchFocused = true;
            }
            searchQuery += chr;
            return true;
        }
        
        return false;
    }
    
    private boolean handleSearchInput(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            searchFocused = false;
            searchQuery = "";
            return true;
        }
        
        return false;
    }
    
    public boolean handleScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, List<Module> modules) {
        scrollOffset -= (int)(verticalAmount * 20);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        return true;
    }
    
    public boolean handleMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, int screenHeight) {
        scrollOffset -= (int)(verticalAmount * 20);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        return true;
    }
    
    private int getModuleSettingsHeight(Module module) {
        if (module.getSettings() == null || module.getSettings().isEmpty()) {
            return 0;
        }
        
        int settingsHeight = module.getSettings().size() * SETTING_HEIGHT;
        
        for (Setting setting : module.getSettings()) {
            if (setting instanceof ModeSetting modeSetting && dropdownExpanded.getOrDefault(modeSetting, false)) {
                settingsHeight += modeSetting.getModes().size() * 15;
            }
        }
        
        return settingsHeight;
    }

    public void updateMaxScrollOffset(int totalContentHeight, int visibleHeight) {
        maxScrollOffset = Math.max(0, totalContentHeight - visibleHeight);
    }
    
    public Category getSelectedCategory() {
        return selectedCategory;
    }
    
    public void setSelectedCategory(Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }
    
    public int getScrollOffset() {
        return scrollOffset;
    }
    
    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public boolean isSearchFocused() {
        return searchFocused;
    }
    
    public void setSearchFocused(boolean searchFocused) {
        this.searchFocused = searchFocused;
    }
    
    public KeybindSetting getListeningKeybind() {
        return listeningKeybind;
    }
    
    public Map<ModeSetting, Boolean> getDropdownExpanded() {
        return dropdownExpanded;
    }
    
    private int getDropdownOptionIndex(double mouseY, int controlY, ModeSetting modeSetting) {
        int dropdownStartY = controlY + 15;
        int optionHeight = 15;
        int relativeY = (int)(mouseY - dropdownStartY);
        
        if (relativeY < 0) return -1;
        
        int optionIndex = relativeY / optionHeight;
        return optionIndex < modeSetting.getModes().size() ? optionIndex : -1;
    }
    
    private void closeAllDropdowns() {
        dropdownExpanded.replaceAll((k, v) -> false);
    }
    
    private void closeAllColorPickers() {
        colorPickerExpanded.clear();
    }
    
    public boolean isAnyDropdownExpanded() {
        return dropdownExpanded.values().stream().anyMatch(Boolean::booleanValue);
    }
    
    public boolean isAnyColorPickerExpanded() {
        return colorPickerExpanded.values().stream().anyMatch(Boolean::booleanValue);
    }
    
    public Map<ColorSetting, Boolean> getColorPickerExpanded() {
        return colorPickerExpanded;
    }
    
    public Map<ColorSetting, ColorPicker> getColorPickers() {
        return colorPickers;
    }
    
    public NumberSetting getEditingNumberSetting() {
        return editingNumberSetting;
    }
    
    public void setEditingNumberSetting(NumberSetting setting) {
        this.editingNumberSetting = setting;
        if (setting != null) {
            this.numberInputText = String.valueOf(setting.getValue());
            if (numberInputText.endsWith(".0")) {
                numberInputText = numberInputText.substring(0, numberInputText.length() - 2);
            }
        } else {
            this.numberInputText = "";
        }
    }
    
    public String getNumberInputText() {
        return numberInputText;
    }
    
    public void setNumberInputText(String text) {
        this.numberInputText = text;
    }
}