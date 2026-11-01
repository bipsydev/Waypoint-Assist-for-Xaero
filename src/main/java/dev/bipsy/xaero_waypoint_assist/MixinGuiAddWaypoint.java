package dev.bipsy.xaero_waypoint_assist;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// import xaero.hud.minimap.waypoint.WaypointColor;

import java.util.ArrayList;


@Mixin(targets = "xaero.common.gui.GuiAddWaypoint", remap = false)
public abstract class MixinGuiAddWaypoint
{

    @Shadow private EditBox nameTextField;
    @Shadow private EditBox initialTextField;
    //@Shadow private
    private int colorIndex;


    @Inject(method = "init", at = @At("TAIL"))
    private void injectPresetDropdown(CallbackInfo ci)
    {
        ArrayList<WaypointPreset> mockPresets = new ArrayList<>();
        mockPresets.add(new WaypointPreset("Base", "BS", 2));
        mockPresets.add(new WaypointPreset("Village", "VL", 4));
        mockPresets.add(new WaypointPreset("Nether Portal", "NP", 1));

        // 1. Cast 'this' to standard vanilla Screen to grab positional dimensions (like width)
        Screen screen = (Screen) (Object) this;

        CycleButton<WaypointPreset> presetDropdown = CycleButton.builder((WaypointPreset preset) -> Component.literal(preset.name))
                .withValues(mockPresets)
                .create(screen.width / 2 - 100, 10, 200, 20, Component.literal("Preset"), (button, selectedPreset) -> {

                    if (this.nameTextField != null) {
                        this.nameTextField.setValue(selectedPreset.name);
                    }
                    if (this.initialTextField != null) {
                        this.initialTextField.setValue(selectedPreset.abbreviation);
                    }
                    this.colorIndex = selectedPreset.colorIndex;
                });

        // 2. Direct approach to bypass 'protected' visibility:
        // We pass the widget directly into the screen's public children list,
        // fulfilling rendering and event processing loops natively.
        screen.children().add(presetDropdown);
        screen.renderables.add(presetDropdown);
    }
}
