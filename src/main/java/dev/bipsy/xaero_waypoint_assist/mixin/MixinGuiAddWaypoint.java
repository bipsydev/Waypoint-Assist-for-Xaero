package dev.bipsy.xaero_waypoint_assist.mixin;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
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

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void injectPresetDropdown(CallbackInfo ci)
    {
        List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
        if (presets == null || presets.isEmpty()) {
            return;
        }

        Screen screen = (Screen) (Object) this;
        ScreenBase screenBase = (ScreenBase) (Object) this;

        Component[] options = new Component[presets.size()];
        for (int i = 0; i < presets.size(); i++) {
            WaypointPreset preset = presets.get(i);
            WaypointColor color = null;
            try {
                color = WaypointColor.fromIndex(preset.colorIndex);
            } catch (Exception ignored) {}
            if (color != null) {
                int hex = color.getHex();
                options[i] = Component.literal(preset.name).withStyle(style -> style.withColor(hex));
            } else {
                options[i] = Component.literal(preset.name);
            }
        }

        DropDownWidget presetDropdown = DropDownWidget.Builder.begin()
                .setOptions(options)
                .setX(screen.width / 2 - 100)
                .setY(10)
                .setW(200)
                .setHasEmptyOption(true)
                .setSelected(null)
                .setContainer(screenBase)
                .setNarrationTitle(Component.literal("Preset"))
                .setCallback((dropdown, selectedIndex) -> {
                    if (selectedIndex >= 0 && selectedIndex < presets.size()) {
                        WaypointPreset selectedPreset = presets.get(selectedIndex);
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
                        if (this.colorDD != null) {
                            this.colorDD.selectId(selectedPreset.colorIndex, false);
                        }
                    }
                    return true;
                })
                .build();

        ((ScreenBaseAccessor) screenBase).invokeAddWidget(presetDropdown);
    }
}
