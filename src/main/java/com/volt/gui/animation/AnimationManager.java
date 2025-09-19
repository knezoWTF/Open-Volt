package com.volt.gui.animation;

import com.volt.module.Category;
import com.volt.module.Module;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    private final Map<Category, Float> categoryAnimations = new HashMap<>();
    private final Map<Module, Float> moduleAnimations = new HashMap<>();
    private final Map<Module, Float> dropdownAnimations = new HashMap<>();
    private float sidebarAnimation = 0;
    private float contentAnimation = 0;
    private float categorySwitch = 1f;

    private float guiAnimation = 0f;
    private float scaleAnimation = 0f;
    private boolean isClosing = false;
    private boolean animationComplete = false;
    private long openTime = 0;
    
    public AnimationManager() {
        for (Category category : Category.values()) {
            categoryAnimations.put(category, 0f);
        }
    }
    
    public void initializeModuleAnimations(Module module) {
        moduleAnimations.put(module, 0f);
        dropdownAnimations.put(module, 0f);
    }
    
    public void updateCategoryAnimation(Category category, boolean isSelected, boolean isHovered, float delta) {
        float targetAnimation = isSelected ? 1f : (isHovered ? 0.3f : 0f);
        float currentAnimation = categoryAnimations.get(category);
        float newAnimation = MathHelper.lerp(0.15f, currentAnimation, targetAnimation);
        categoryAnimations.put(category, newAnimation);
    }
    
    public void updateModuleAnimation(Module module, boolean isEnabled, boolean isHovered, float delta) {
        float targetAnimation = isEnabled ? 1f : (isHovered ? 0.2f : 0f);
        float currentAnimation = moduleAnimations.getOrDefault(module, 0f);
        float newAnimation = MathHelper.lerp(0.12f, currentAnimation, targetAnimation);
        moduleAnimations.put(module, newAnimation);
    }
    
    public void updateDropdownAnimation(Module module, boolean isExpanded, float delta) {
        float targetDropdown = isExpanded ? 1f : 0f;
        float currentDropdown = dropdownAnimations.getOrDefault(module, 0f);
        float newDropdown = MathHelper.lerp(0.15f, currentDropdown, targetDropdown);
        dropdownAnimations.put(module, newDropdown);
    }
    
    public void updateSidebarAnimation(float target, float delta) {
        sidebarAnimation = MathHelper.lerp(0.1f, sidebarAnimation, target);
    }
    
    public void updateContentAnimation(float target, float delta) {
        contentAnimation = MathHelper.lerp(0.1f, contentAnimation, target);
    }
    
    public float getCategoryAnimation(Category category) {
        return categoryAnimations.getOrDefault(category, 0f);
    }
    
    public float getModuleAnimation(Module module) {
        return moduleAnimations.getOrDefault(module, 0f);
    }
    
    public float getDropdownAnimation(Module module) {
        return dropdownAnimations.getOrDefault(module, 0f);
    }
    
    public float getSidebarAnimation() {
        return sidebarAnimation;
    }
    
    public float getContentAnimation() {
        return contentAnimation;
    }

    public float getCategorySwitch() {
        return categorySwitch;
    }

    public void triggerCategorySwitch() {
        this.categorySwitch = 0f;
        this.contentAnimation = 0f;
    }
    
    public void updateAnimations(float delta) {
    }
    
    public void setCategoryAnimation(Category category, float value) {
        categoryAnimations.put(category, value);
    }
    
    public void setModuleAnimation(Module module, float value) {
        moduleAnimations.put(module, value);
    }
    
    public void setDropdownAnimation(Module module, float value) {
        dropdownAnimations.put(module, value);
    }
    

    public void initializeGuiAnimation() {
        this.guiAnimation = 0f;
        this.scaleAnimation = 0f;
        this.isClosing = false;
        this.animationComplete = false;
        this.openTime = System.currentTimeMillis();
    }
    
    public void updateGuiAnimations() {
        updateGuiAnimations(1.0f / 60.0f);
    }
    
    public void updateGuiAnimations(float deltaTime) {
        long currentTime = System.currentTimeMillis();
        float timeSinceOpen = (currentTime - openTime) / 1000f;
        

        deltaTime = Math.min(deltaTime, 0.05f);
        
        if (isClosing) {

            float targetGui = 0f;
            float targetScale = 0.8f;
            
            float closingSpeed = 8.0f;
            
            guiAnimation = MathHelper.lerp(1.0f - (float)Math.exp(-closingSpeed * deltaTime), guiAnimation, targetGui);
            scaleAnimation = MathHelper.lerp(1.0f - (float)Math.exp(-closingSpeed * deltaTime), scaleAnimation, targetScale);
            

            if (guiAnimation <= 0.05f && !animationComplete) {
                animationComplete = true;
            }
        } else {

            float targetGui = 1f;
            
            float openingSpeed = 6.0f;
            
            guiAnimation = MathHelper.lerp(1.0f - (float)Math.exp(-openingSpeed * deltaTime), guiAnimation, targetGui);
            

            float scaleProgress = Math.min(timeSinceOpen * 2f, 1f);
            float easeOutBack = 1f + 2.7f * (float)Math.pow(scaleProgress - 1f, 3f) + 1.7f * (float)Math.pow(scaleProgress - 1f, 2f);
            scaleAnimation = MathHelper.lerp(1.0f - (float)Math.exp(-openingSpeed * deltaTime), scaleAnimation, easeOutBack);
            categorySwitch = MathHelper.lerp(0.12f, categorySwitch, 1f);
            contentAnimation = MathHelper.lerp(0.12f, contentAnimation, 1f);
        }
    }
    
    public boolean shouldCloseGui() {
        return guiAnimation <= 0.05f && isClosing && animationComplete;
    }
    
    public void startClosingAnimation() {
        if (!isClosing) {
            isClosing = true;
            animationComplete = false;
        }
    }
    
    public float getGuiAnimation() {
        return guiAnimation;
    }
    
    public float getScaleAnimation() {
        return scaleAnimation;
    }
    
    public boolean isClosing() {
        return isClosing;
    }
    
    public boolean isAnimationComplete() {
        return animationComplete;
    }
}