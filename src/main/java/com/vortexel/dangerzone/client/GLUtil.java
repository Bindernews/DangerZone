package com.vortexel.dangerzone.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class GLUtil {

    private static final FloatBuffer BUF_FLOAT_QUAD = BufferUtils.createFloatBuffer(2 * 4);


    public static void clipToRegion(int x, int y, int w, int h) {
        // Setup the stencil and write into the stencil buffer
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_GEQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        // Write to the stencil buffer
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        setQuadBuffer(x, y, w, h);
        GL11.glVertexPointer(2, 0, BUF_FLOAT_QUAD);
        // Anything in this area will have its stencil bit set, meaning only it will be drawn
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 4);
        // Any subsequent calls are checking the stencil, not writing to it
        GL11.glStencilMask(0x00);
    }

    public static void disableClipping() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void drawTexturedRect(int x, int y, int z, int textureX, int textureY, int width, int height,
                                        int textureWidth, int textureHeight) {
        float u = 1f / textureWidth;
        float v = 1f / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, z).tex((textureX) * u, (textureY + height) * v).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex((textureX + width) * u, (textureY + height) * v).endVertex();
        bufferbuilder.pos(x + width, y, z).tex((textureX + width) * u, (textureY) * v).endVertex();
        bufferbuilder.pos(x, y, z).tex((textureX) * u, (textureY) * v).endVertex();
        tessellator.draw();
    }

    public static void drawTexturedRect(int x, int y, int z, int width, int height, float minU, float minV,
                                       float maxU, float maxV) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, z).tex(minU, maxV).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(x + width, y, z).tex(maxU, minV).endVertex();
        bufferbuilder.pos(x, y, z).tex(minU, minV).endVertex();
        tessellator.draw();
    }


    private static void setQuadBuffer(float x, float y, float w, float h) {
        BUF_FLOAT_QUAD.clear();
        BUF_FLOAT_QUAD
                .put(x).put(y)
                .put(x + w).put(y)
                .put(x + w).put(y + h)
                .put(x).put(y + h);
    }

    private GLUtil() {}
}
