package com.vortexel.dangerzone.client.gui.component;

import com.google.common.collect.Lists;
import com.vortexel.dangerzone.client.GLUtil;
import com.vortexel.dangerzone.client.gui.BaseGuiContainer;
import com.vortexel.dangerzone.client.gui.Sprite;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.DangerMath;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.util.List;
import java.util.function.Consumer;

public class BScrollBar implements IGuiComponent {

    protected Rectangle area;
    protected float minValue;
    protected float maxValue;
    protected float stepSize;
    /** The real value of the scroll bar. */
    protected float realValue;
    /** realValue but adjusted to be a multiple of step size */
    protected float stepValue;
    protected boolean isDragging;
    protected Sprite scrollBarSprite;
    protected  boolean renderInBetweenSteps;

    public final List<Consumer<Float>> scrollListeners;
    public final BaseGuiContainer container;

    public BScrollBar(BaseGuiContainer container, int x, int y, int w, int h, Sprite scrollBarSprite) {
        this.container = container;
        area = new Rectangle(x, y, w, h);
        realValue = 0;
        stepValue = 0;
        isDragging = false;
        this.scrollBarSprite = scrollBarSprite;
        scrollListeners = Lists.newArrayList();
        setRange(0, 1, 0);
        renderInBetweenSteps = false;
    }

    public void setRange(float min, float max) {
        setRange(min, max, 0);
    }

    public void setRange(float min, float max, float step) {
        this.minValue = min;
        this.maxValue = max;
        this.stepSize = step;
    }

    /**
     * Set the scrollbar to the closest allowed value to {@code value}.
     */
    public void setTo(float value) {
        this.realValue = MathHelper.clamp(value, minValue, maxValue);
        float temp;
        if (stepSize > 0) {
            temp = (Math.round((realValue - minValue) / stepSize) * stepSize) + minValue;
        } else {
            temp = realValue;
        }
        if (stepValue != temp) {
            stepValue = temp;
            for (Consumer<Float> con : scrollListeners) {
                con.accept(temp);
            }
        }
    }

    public float getValue() {
        return stepValue;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (area.contains(mouseX, mouseY) && mouseButton == Consts.MOUSE_LEFT) {
            isDragging = true;
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        if (isDragging && mouseButton == Consts.MOUSE_LEFT) {
            isDragging = false;
        }
    }

    @Override
    public void onMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (isDragging) {
            // If we're dragging, then get the mouse position and let's do this!
            setTo(getValueFromMouseY(mouseY));
        }
    }

    @Override
    public void handleInput() {
        if (!isDragging) {
            // Don't do scrolling when the user is already dragging the scroll bar
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                int d = dWheel > 0 ? -1 : 1;
                if (stepSize > 0) {
                    setTo(realValue + (stepSize * d));
                } else {
                    setTo(realValue + 0.5f);
                }
            }
        }
    }

    public float getValueFromMouseY(int mouseY) {
        mouseY -= container.getGuiTop();
        return (float)DangerMath.changeRangeClamp(mouseY, calcMinY(), calcMaxY(), minValue, maxValue);
    }

    public int getDrawYFromValue(float aValue) {
        int centerY = (int)DangerMath.changeRangeClamp(aValue, minValue, maxValue, calcMinY(), calcMaxY());
        return centerY - scrollBarSprite.height / 2;
    }

    @Override
    public void draw() {
        int drawY = 0;
        if (renderInBetweenSteps) {
            drawY = getDrawYFromValue(realValue);
        } else {
            drawY = getDrawYFromValue(stepValue);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(scrollBarSprite.texture);
        container.drawTexturedModalRect(area.x, drawY, scrollBarSprite.x, scrollBarSprite.y, area.width,
                scrollBarSprite.height);
//        GLUtil.drawTexturedRect(area.x, drawY, 0, scrollBarSprite.x, scrollBarSprite.y, scrollBarSprite.width,
//                scrollBarSprite.height, scrollBarSprite.texW, scrollBarSprite.texH);
    }

    public boolean isRenderInBetweenSteps() {
        return renderInBetweenSteps;
    }

    public void setRenderInBetweenSteps(boolean v) {
        renderInBetweenSteps = v;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    protected int calcMinY() {
        return area.y + (scrollBarSprite.height / 2);
    }

    protected int calcMaxY() {
        return area.y + area.height - (scrollBarSprite.height / 2);
    }
}
