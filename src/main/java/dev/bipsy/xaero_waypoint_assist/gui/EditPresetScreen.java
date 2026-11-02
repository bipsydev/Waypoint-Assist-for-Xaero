package dev.bipsy.xaero_waypoint_assist.gui;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.IDropDownWidgetCallback;

import java.util.List;

public class EditPresetScreen extends ScreenBase implements IDropDownWidgetCallback {
    private final Screen parentScreen;
    private final WaypointPreset existingPreset;
    private final int editIndex;

    private EditBox nameField;
    private EditBox abbreviationField;
    private DropDownWidget colorDD;
    private int selectedColorIndex;

    public EditPresetScreen(Screen parent, WaypointPreset existingPreset, int editIndex) {
        super(parent, parent, Component.literal(existingPreset == null ? "Add Waypoint Preset" : "Edit Waypoint Preset"));
        this.parentScreen = parent;
        this.existingPreset = existingPreset;
        this.editIndex = editIndex;
        this.selectedColorIndex = existingPreset != null ? existingPreset.colorIndex : 0;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int fieldWidth = 200;
        int fieldHeight = 20;

        // Name Field
        this.nameField = new EditBox(this.font, centerX - fieldWidth / 2, 50, fieldWidth, fieldHeight, Component.literal("Preset Name"));
        this.nameField.setValue(this.existingPreset != null ? this.existingPreset.name : "");
        this.nameField.setMaxLength(32);
        this.addRenderableWidget(this.nameField);

        // Abbreviation Field
        this.abbreviationField = new EditBox(this.font, centerX - fieldWidth / 2, 95, fieldWidth, fieldHeight, Component.literal("Abbreviation"));
        this.abbreviationField.setValue(this.existingPreset != null ? this.existingPreset.abbreviation : "");
        this.abbreviationField.setMaxLength(4);
        this.addRenderableWidget(this.abbreviationField);

        // Color DropDownWidget
        WaypointColor[] colors = WaypointColor.values();
        Component[] colorOptions = new Component[colors.length];
        for (int i = 0; i < colors.length; i++) {
            WaypointColor color = colors[i];
            colorOptions[i] = color.getName().copy().withStyle(style -> style.withColor(color.getHex()));
        }

        this.colorDD = DropDownWidget.Builder.begin()
                .setOptions(colorOptions)
                .setX(centerX - fieldWidth / 2)
                .setY(140)
                .setW(fieldWidth)
                .setSelected(this.selectedColorIndex)
                .setCallback(this)
                .setContainer(this)
                .setNarrationTitle(Component.literal("Color"))
                .build();

        this.addWidget(this.colorDD);

        // Save & Cancel Buttons
        int buttonWidth = 95;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            savePreset();
        }).bounds(centerX - fieldWidth / 2, 180, buttonWidth, fieldHeight).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parentScreen);
            }
        }).bounds(centerX + fieldWidth / 2 - buttonWidth, 180, buttonWidth, fieldHeight).build());
    }

    @Override
    public boolean onSelected(DropDownWidget dropdown, int selectedIndex) {
        if (dropdown == this.colorDD && selectedIndex >= 0) {
            this.selectedColorIndex = selectedIndex;
            return true;
        }
        return false;
    }

    private void savePreset() {
        String name = this.nameField.getValue().trim();
        String abbreviation = this.abbreviationField.getValue().trim();

        if (name.isEmpty()) {
            name = "New Preset";
        }
        if (abbreviation.isEmpty()) {
            abbreviation = name.length() >= 4 ? name.substring(0, 4).toUpperCase() : name.toUpperCase();
        } else if (abbreviation.length() > 4) {
            abbreviation = abbreviation.substring(0, 4);
        }

        WaypointPreset saved = new WaypointPreset(name, abbreviation, this.selectedColorIndex);
        if (this.editIndex >= 0) {
            List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
            if (this.editIndex < presets.size()) {
                boolean duplicate = false;
                for (int i = 0; i < presets.size(); i++) {
                    if (i != this.editIndex && saved.equals(presets.get(i))) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    presets.set(this.editIndex, saved);
                    PresetManager.getInstance().setPresets(presets);
                }
            }
        } else {
            PresetManager.getInstance().addPreset(saved);
        }

        if (this.minecraft != null) {
            if (this.parentScreen instanceof PresetConfigScreen configScreen) {
                configScreen.refreshList();
            }
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int fieldWidth = 200;

        guiGraphics.drawString(this.font, "Waypoint Name:", centerX - fieldWidth / 2, 38, 0xA0A0A0);
        guiGraphics.drawString(this.font, "Abbreviation / Initial:", centerX - fieldWidth / 2, 83, 0xA0A0A0);
        guiGraphics.drawString(this.font, "Color:", centerX - fieldWidth / 2, 128, 0xA0A0A0);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
