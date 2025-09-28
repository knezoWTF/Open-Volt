package com.volt.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public final class TextureRenderer {
    private TextureRenderer() {}

    public static void drawCenteredQuad(MatrixStack matrices, Identifier texture, float width, float height, int color) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, -halfWidth, halfHeight, 0.0f).texture(0.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, halfHeight, 0.0f).texture(1.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, -halfHeight, 0.0f).texture(1.0f, 0.0f).color(r, g, b, a);
        builder.vertex(matrix, -halfWidth, -halfHeight, 0.0f).texture(0.0f, 0.0f).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    public static void drawCenteredQuad(DrawContext context, Identifier texture, float x, float y, float width, float height, float rotationDeg, int color) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0f);
        if (rotationDeg != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationDeg));
        }
        drawCenteredQuad(matrices, texture, width, height, color);
        matrices.pop();
    }
}
