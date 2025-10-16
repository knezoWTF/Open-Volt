package com.volt.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ShaderManager {
    private static final Map<String, Integer> shaderPrograms = new HashMap<>();
    private static final Map<String, Integer> vertexShaders = new HashMap<>();
    private static final Map<String, Integer> fragmentShaders = new HashMap<>();

    public static int loadShaderProgram(String name, String vertexPath, String fragmentPath) {
        if (shaderPrograms.containsKey(name)) {
            return shaderPrograms.get(name);
        }

        int vertexShader = loadShader(vertexPath, GL20.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        if (vertexShader == 0 || fragmentShader == 0) {
            return 0;
        }

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
            System.err.println("Failed to link shader program: " + GL20.glGetProgramInfoLog(program));
            GL20.glDeleteProgram(program);
            return 0;
        }

        vertexShaders.put(name, vertexShader);
        fragmentShaders.put(name, fragmentShader);
        shaderPrograms.put(name, program);

        return program;
    }

    private static int loadShader(String path, int type) {
        try {
            Identifier identifier = Identifier.of("volt", path);
            Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier).orElse(null);

            if (resource == null) {
                System.err.println("Could not find shader: " + path);
                return 0;
            }

            String source;
            try (InputStream inputStream = resource.getInputStream()) {
                source = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            int shader = GL20.glCreateShader(type);
            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);

            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
                System.err.println("Failed to compile shader " + path + ": " + GL20.glGetShaderInfoLog(shader));
                GL20.glDeleteShader(shader);
                return 0;
            }

            return shader;
        } catch (IOException e) {
            System.err.println("Failed to load shader " + path + ": " + e.getMessage());
            return 0;
        }
    }

    public static void useShader(String name) {
        Integer program = shaderPrograms.get(name);
        if (program != null) {
            RenderSystem.setShader(() -> null);
            GL20.glUseProgram(program);
        }
    }

    public static void stopUsingShader() {
        GL20.glUseProgram(0);
    }

    public static int getUniformLocation(String shaderName, String uniformName) {
        Integer program = shaderPrograms.get(shaderName);
        if (program != null) {
            return GL20.glGetUniformLocation(program, uniformName);
        }
        return -1;
    }

    public static void setUniform2f(String shaderName, String uniformName, float x, float y) {
        int location = getUniformLocation(shaderName, uniformName);
        if (location != -1) {
            GL20.glUniform2f(location, x, y);
        }
    }

    public static void setUniform1f(String shaderName, String uniformName, float value) {
        int location = getUniformLocation(shaderName, uniformName);
        if (location != -1) {
            GL20.glUniform1f(location, value);
        }
    }

    public static void setUniform4f(String shaderName, String uniformName, float x, float y, float z, float w) {
        int location = getUniformLocation(shaderName, uniformName);
        if (location != -1) {
            GL20.glUniform4f(location, x, y, z, w);
        }
    }

    public static void cleanup() {
        for (int program : shaderPrograms.values()) {
            GL20.glDeleteProgram(program);
        }
        for (int shader : vertexShaders.values()) {
            GL20.glDeleteShader(shader);
        }
        for (int shader : fragmentShaders.values()) {
            GL20.glDeleteShader(shader);
        }
        shaderPrograms.clear();
        vertexShaders.clear();
        fragmentShaders.clear();
    }
}