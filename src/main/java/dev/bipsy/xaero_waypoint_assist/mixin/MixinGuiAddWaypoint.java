package dev.bipsy.xaero_waypoint_assist.mixin;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xaero.common.gui.GuiAddWaypoint;
import xaero.common.gui.WaypointEditForm;
import xaero.hud.minimap.waypoint.WaypointColor;

import java.util.List;

@Mixin(value = GuiAddWaypoint.class, remap = false)
public abstract class MixinGuiAddWaypoint
{
    @Shadow private EditBox nameTextField;
    @Shadow private EditBox initialTextField;
    @Shadow private WaypointEditForm getCurrent() {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void injectPresetDropdown(CallbackInfo ci)
    {
        List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
        if (presets == null || presets.isEmpty()) {
            return;
        }

        Screen screen = (Screen) (Object) this;

        CycleButton<WaypointPreset> presetDropdown = CycleButton.builder((WaypointPreset preset) -> {
            WaypointColor color = null;
            try {
                color = WaypointColor.fromIndex(preset.colorIndex);
            } catch (Exception ignored) {}
            if (color != null) {
                int hex = color.getHex();
                return Component.literal(preset.name).withStyle(style -> style.withColor(hex));
            }
            return Component.literal(preset.name);
        })
                .withValues(presets)
                .create(screen.width / 2 - 100, 10, 200, 20, Component.literal("Preset"), (button, selectedPreset) -> {
                    if (this.nameTextField != null) {
                        this.nameTextField.setValue(selectedPreset.name);
                    }
                    if (this.initialTextField != null) {
                        this.initialTextField.setValue(selectedPreset.abbreviation);
                    }
                    WaypointEditForm currentForm = this.getCurrent();
                    if (currentForm != null) {
                        currentForm.color = WaypointColor.fromIndex(selectedPreset.colorIndex);
                    }
                });

        ScreenAccessor accessor = (ScreenAccessor) screen;
        accessor.getRenderables().add(presetDropdown);
        accessor.getChildren().add(presetDropdown);
        accessor.getNarratables().add(presetDropdown);
    }
}
