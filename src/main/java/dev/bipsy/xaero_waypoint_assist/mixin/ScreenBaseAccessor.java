package dev.bipsy.xaero_waypoint_assist.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xaero.lib.client.gui.ScreenBase;


/**
 * Allows us to access the transient parent methods of the Mixin target `GuiAddWaypoint` from the mixin `MixinGuiAddWaypoint`.
 * Typically, we can @Shadow parent fields and methods, but this doesn't work when it is inherited transiently from
 * superclasses of the mixin target class.
 * Therefore, we can use this accessor interface to cast MixinGuiAddWaypoint, and invoke methods from here.
 */
@Mixin(value = ScreenBase.class, remap = false)
public interface ScreenBaseAccessor {

    /// Allow invokation of ScreenBase.addWidget
    @Invoker("addWidget")
    <T extends GuiEventListener & NarratableEntry> T invokeAddWidget(T widget);

}
