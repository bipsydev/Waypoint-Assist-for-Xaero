package dev.bipsy.xaero_waypoint_assist.mixin;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import dev.bipsy.xaero_waypoint_assist.gui.EditPresetScreen;
import net.minecraft.client.Minecraft;
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
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;

import java.util.Collections;
import java.util.List;

@Mixin(value = GuiAddWaypoint.class, remap = false)
public abstract class MixinGuiAddWaypoint
{
    @Shadow private EditBox nameTextField;
    @Shadow private EditBox initialTextField;
    @Shadow private DropDownWidget colorDD;
    @Shadow private WaypointEditForm getCurrent() {
        throw new AssertionError();
    }
    @Shadow private void updateConfirmButton() {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void injectPresetDropdown(CallbackInfo ci)
    {
        List<WaypointPreset> loadedPresets = PresetManager.getInstance().getPresets();
        final List<WaypointPreset> presets = loadedPresets != null ? loadedPresets : Collections.emptyList();

        Screen screen = (Screen) (Object) this;
        ScreenBase screenBase = (ScreenBase) (Object) this;

        Component[] options = new Component[presets.size() + 3];
        options[0] = Component.literal("No Preset");
        for (int i = 0; i < presets.size(); i++) {
            WaypointPreset preset = presets.get(i);
            WaypointColor color = null;
            try {
                color = WaypointColor.fromIndex(preset.colorIndex);
            } catch (Exception ignored) {}
            if (color != null) {
                int hex = color.getHex();
                options[i + 1] = Component.literal(preset.name).withStyle(style -> style.withColor(hex));
            } else {
                options[i + 1] = Component.literal(preset.name);
            }
        }
        options[presets.size() + 1] = Component.literal("Copy current to Preset");
        options[presets.size() + 2] = Component.literal("New Preset...");

        DropDownWidget presetDropdown = DropDownWidget.Builder.begin()
                .setOptions(options)
                .setX(screen.width / 2 - 100)
                .setY(10)
                .setW(200)
                .setSelected(0)
                .setContainer(screenBase)
                .setNarrationTitle(Component.literal("Preset"))
                .setCallback((dropdown, selectedIndex) -> {
                    if (selectedIndex > 0 && selectedIndex <= presets.size()) {
                        WaypointPreset selectedPreset = presets.get(selectedIndex - 1);
                        String presetName = selectedPreset.name != null ? selectedPreset.name : "";
                        String presetInitial = selectedPreset.abbreviation != null ? selectedPreset.abbreviation : "";
                        WaypointColor presetColor = WaypointColor.fromIndex(selectedPreset.colorIndex);

                        if (this.nameTextField != null) {
                            this.nameTextField.setValue(presetName);
                        }
                        if (this.initialTextField != null) {
                            this.initialTextField.setValue(presetInitial);
                        }
                        WaypointEditForm currentForm = this.getCurrent();
                        if (currentForm != null) {
                            currentForm.name = presetName;
                            currentForm.initial = presetInitial;
                            currentForm.autoInitial = false;
                            currentForm.color = presetColor;
                        }
                        if (this.colorDD != null) {
                            this.colorDD.selectId(selectedPreset.colorIndex, false);
                        }
                        try {
                            this.updateConfirmButton();
                        } catch (Exception ignored) {}
                    } else if (selectedIndex == presets.size() + 1) {
                        String name = this.nameTextField != null ? this.nameTextField.getValue().trim() : "";
                        if (name.isEmpty()) {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc != null) {
                                mc.setScreen(screen);
                            }
                            return true;
                        }

                        String initial = this.initialTextField != null ? this.initialTextField.getValue().trim() : "";
                        if (initial.isEmpty()) {
                            initial = name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
                        }
                        int colorIndex = 0;
                        WaypointEditForm currentForm = this.getCurrent();
                        if (currentForm != null && currentForm.color != null) {
                            colorIndex = currentForm.color.ordinal();
                        }

                        WaypointPreset newPreset = new WaypointPreset(name, initial, colorIndex);
                        PresetManager.getInstance().addPreset(newPreset);

                        Minecraft mc = Minecraft.getInstance();
                        if (mc != null) {
                            mc.setScreen(screen);
                        }
                    } else if (selectedIndex == presets.size() + 2) {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc != null) {
                            mc.setScreen(new EditPresetScreen(screen, null, -1));
                        }
                    }
                    return true;
                })
                .build();

        ((ScreenBaseAccessor) screenBase).invokeAddWidget(presetDropdown);
    }
}
