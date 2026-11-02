package dev.bipsy.xaero_waypoint_assist.gui;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import xaero.hud.minimap.waypoint.WaypointColor;

import java.util.ArrayList;
import java.util.List;

public class EditPresetScreen extends Screen {
    private final PresetConfigScreen parent;
    private final WaypointPreset existingPreset;
    private final int editIndex;

    private EditBox nameField;
    private EditBox abbreviationField;
    private int selectedColorIndex;

    public EditPresetScreen(PresetConfigScreen parent, WaypointPreset existingPreset, int editIndex) {
        super(Component.literal(existingPreset == null ? "Add Waypoint Preset" : "Edit Waypoint Preset"));
        this.parent = parent;
        this.existingPreset = existingPreset;
        this.editIndex = editIndex;
        this.selectedColorIndex = existingPreset != null ? existingPreset.colorIndex : 0;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = 200;
        int fieldHeight = 20;

        // Name Field
        this.nameField = new EditBox(this.font, centerX - fieldWidth / 2, 50, fieldWidth, fieldHeight, Component.literal("Preset Name"));
        this.nameField.setValue(this.existingPreset != null ? this.existingPreset.name : "");
        this.nameField.setMaxLength(32);
        this.addWidget(this.nameField);

        // Abbreviation Field
        this.abbreviationField = new EditBox(this.font, centerX - fieldWidth / 2, 95, fieldWidth, fieldHeight, Component.literal("Abbreviation"));
        this.abbreviationField.setValue(this.existingPreset != null ? this.existingPreset.abbreviation : "");
        this.abbreviationField.setMaxLength(8);
        this.addWidget(this.abbreviationField);

        // Color CycleButton (indices 0 to 15)
        List<Integer> colorIndices = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            colorIndices.add(i);
        }

        CycleButton<Integer> colorButton = CycleButton.<Integer>builder(index -> {
            try {
                WaypointColor color = WaypointColor.fromIndex(index);
                if (color != null) {
                    return Component.literal(index + " - " + color.name());
                }
            } catch (Exception ignored) {}
            return Component.literal(String.valueOf(index));
        })
        .withValues(colorIndices)
        .withInitialValue(this.selectedColorIndex)
        .create(centerX - fieldWidth / 2, 140, fieldWidth, fieldHeight, Component.literal("Color"), (btn, val) -> {
            this.selectedColorIndex = val;
        });

        this.addRenderableWidget(colorButton);

        // Save & Cancel Buttons
        int buttonWidth = 95;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            savePreset();
        }).bounds(centerX - fieldWidth / 2, 180, buttonWidth, fieldHeight).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }).bounds(centerX + fieldWidth / 2 - buttonWidth, 180, buttonWidth, fieldHeight).build());
    }

    private void savePreset() {
        String name = this.nameField.getValue().trim();
        String abbreviation = this.abbreviationField.getValue().trim();

        if (name.isEmpty()) {
            name = "New Preset";
        }
        if (abbreviation.isEmpty()) {
            abbreviation = name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
        }

        WaypointPreset saved = new WaypointPreset(name, abbreviation, this.selectedColorIndex);
        if (this.editIndex >= 0) {
            List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
            if (this.editIndex < presets.size()) {
                presets.set(this.editIndex, saved);
                PresetManager.getInstance().setPresets(presets);
            }
        } else {
            PresetManager.getInstance().addPreset(saved);
        }

        if (this.minecraft != null) {
            this.parent.refreshList();
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int fieldWidth = 200;

        guiGraphics.drawString(this.font, "Waypoint Name:", centerX - fieldWidth / 2, 38, 0xA0A0A0);
        this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, "Abbreviation / Initial:", centerX - fieldWidth / 2, 83, 0xA0A0A0);
        this.abbreviationField.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, "Color:", centerX - fieldWidth / 2, 128, 0xA0A0A0);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
