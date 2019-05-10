package com.vortexel.dangerzone.client.gui.component;

import java.awt.Rectangle;

public interface IGuiComponent {

    default void onMouseClick(int mouseX, int mouseY, int mouseButton) {}
    default void onMouseRelease(int mouseX, int mouseY, int mouseButton) {}
    default void onMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {}
    default void onUpdate(float partialTicks) {}
    default void handleInput() {}
    void draw();
    Rectangle getArea();


}
