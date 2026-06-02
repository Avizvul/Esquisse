package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;

// Наследуем от ColorPencilItem, чтобы он автоматически забирал логику поглощения красителей!
public class ColorMarkerItem extends ColorPencilItem {

    public ColorMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.network.chat.Component getName(ItemStack stack) {
        java.util.List<Integer> colors = stack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
        if (colors.isEmpty()) {
            return net.minecraft.network.chat.Component.translatable("item.esquissemod.empty_color_marker");
        }
        return super.getName(stack);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getGuiTexture() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_color_marker_base.png");
    }
}