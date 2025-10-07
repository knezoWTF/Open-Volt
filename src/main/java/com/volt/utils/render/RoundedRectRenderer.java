package com.volt.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.*;

public class RoundedRectRenderer {
    private static final String SHADER_NAME = "rounded_rect";
    private static int vao = -1;
    private static int vbo = -1;
    private static boolean initialized = false;

    private static final float[] QUAD_VERTICES = {
        -1.0f, -1.0f,
         1.0f, -1.0f,
         1.0f,  1.0f,
        -1.0f,  1.0f
    };

    private static final int[] QUAD_INDICES = {
        0, 1, 2,
        2, 3, 0
    };

    public static void init() {
        if (initialized) return;

        ShaderManager.loadShaderProgram(SHADER_NAME, "shaders/roundedrect.vsh", "shaders/roundedrect.fsh");

        vao = GL30.glGenVertexArrays();
        vbo = GL20.glGenBuffers();
        int ebo = GL20.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, QUAD_VERTICES, GL20.GL_STATIC_DRAW);

        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, QUAD_INDICES, GL20.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL30.glBindVertexArray(0);

        initialized = true;
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(matrices, x, y, width, height, radius, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, float r, float g, float b, float a) {
        if (!initialized) {
            init();
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        ShaderManager.useShader(SHADER_NAME);

        ShaderManager.setUniform2f(SHADER_NAME, "u_resolution", screenWidth, screenHeight);
        ShaderManager.setUniform2f(SHADER_NAME, "u_position", x, y);
        ShaderManager.setUniform2f(SHADER_NAME, "u_size", width, height);
        ShaderManager.setUniform1f(SHADER_NAME, "u_radius", radius);
        ShaderManager.setUniform4f(SHADER_NAME, "u_color", r, g, b, a);

        GL30.glBindVertexArray(vao);
        GL20.glDrawElements(GL20.GL_TRIANGLES, 6, GL20.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        ShaderManager.stopUsingShader();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void cleanup() {
        if (vao != -1) {
            GL30.glDeleteVertexArrays(vao);
            vao = -1;
        }
        if (vbo != -1) {
            GL20.glDeleteBuffers(vbo);
            vbo = -1;
        }
        initialized = false;
    }
}