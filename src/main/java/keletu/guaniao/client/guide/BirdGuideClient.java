/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package keletu.guaniao.client.guide;

import keletu.guaniao.client.guide.BirdGuideScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class BirdGuideClient {
    private BirdGuideClient() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen((Screen)new BirdGuideScreen());
    }
}

