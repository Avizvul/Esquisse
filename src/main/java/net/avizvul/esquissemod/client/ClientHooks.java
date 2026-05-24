package net.avizvul.esquissemod.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.avizvul.esquissemod.client.screen.SketchedPageScreen;

public class ClientHooks {
    public static void openSketchedPageScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new SketchedPageScreen(stack));
    }
}