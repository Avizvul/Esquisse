package net.avizvul.esquissemod.client.tooltip;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

// Этот record просто хранит данные рисунка для передачи в клиентский тултип
public record SketchedPageTooltipData(SketchData sketchData) implements TooltipComponent {
}