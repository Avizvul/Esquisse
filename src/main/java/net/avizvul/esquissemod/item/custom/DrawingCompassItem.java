package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.item.custom.base.DrawingToolItem;
import net.minecraft.resources.ResourceLocation;

public class DrawingCompassItem extends DrawingToolItem {

    public DrawingCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_drawing_compass.png");
    }

    @Override
    public int getPeekHeight() {
        return 12; // Циркуль длинный, пусть торчит повыше в пенале/меню
    }
}