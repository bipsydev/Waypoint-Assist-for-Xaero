package dev.bipsy.xaero_waypoint_assist.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xaero.lib.client.gui.ScreenBase;

@Mixin(value = ScreenBase.class, remap = false)
public interface ScreenBaseAccessor {
    @Invoker("addWidget")
    <T extends GuiEventListener & NarratableEntry> T invokeAddWidget(T widget);
}
