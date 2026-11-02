package dev.bipsy.xaero_waypoint_assist.gui;

import com.mojang.logging.LogUtils;
import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.IDropDownWidgetCallback;

import java.util.List;


/**
 * The GUI screen view for adding/editing a waypoint preset.
 * Allows the user to enter in the name, abbreviation/initial, color, and set for a preset to save/update.
 * Extends from `xaero.lib.client.gui.ScreenBase` for the best support for their GUI widgets.
 * Implements from Xaero's `IDropDownWidgetCallback` in order to save the color index from the drop-down menu upon selection.
 */
public class EditPresetScreen extends ScreenBase implements IDropDownWidgetCallback {

    /// A reference to the parent screen that created this one, so that we can enter this menu from any screen and then return back to it.
    private final Screen parentScreen;

    /// A reference to the existing preset that is passed into this screen if we are editing an existing one.
    private final WaypointPreset existingPreset;

    /// The index into the list of presets in the manager that we are currently editing (the preset `exitingPreset`), or -1 if adding a new preset.
    private final int editIndex;


    /// The GUI Widget for entering a name for this preset.
    private EditBox nameField;
    /// The GUI Widget for entering a 1 to 4 letter abbrevation/initial to use for the waypoint.
    private EditBox abbreviationField;
    /// The GUI Widget color drop-down box, the same sort of one that is shown on Xaero's GuiAddWaypoint screen.
    private DropDownWidget colorDD;
    /// The associated index that is selected from the user from the `colorDD` drop-down box widget.
    private int selectedColorIndex;

    /// Internal logger object.
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructor that creates the GUI screen.
     * @param parent the "previous" screen that created this one.
     * @param existingPreset The optional preset to edit, if we are editing an existing one.
     * @param editIndex The optional index that references the existing preset, or -1 if adding a new preset.
     */
    public EditPresetScreen(Screen parent, WaypointPreset existingPreset, int editIndex) {
        super(parent, parent, Component.literal(existingPreset == null ? "Add Waypoint Preset" : "Edit Waypoint Preset"));
        this.parentScreen = parent;
        this.existingPreset = existingPreset;
        this.editIndex = editIndex;
        this.selectedColorIndex = existingPreset != null ? existingPreset.colorIndex : 0;
    }


    /**
     * Initializes the GUI layout. Override from ScreenBase.
     */
    @Override
    protected void init() {
        super.init(); // initializes dropdowns, etc

        // some calculations
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
        // create each entry in the color drop-down
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


    /**
     * Event to react to the drop-down box selection. Updates the selected color index.
     * @param dropdown the drop-down widget we are reacting to (test if it is the color one, but should be the only one)
     * @param selectedIndex The selected entry index from the drop-down box.
     * @return Whether this event was handled or not.
     */
    @Override
    public boolean onSelected(DropDownWidget dropdown, int selectedIndex) {
        if (dropdown == this.colorDD && selectedIndex >= 0) {
            this.selectedColorIndex = selectedIndex;
            return true;
        }
        return false;
    }


    /**
     * This helper method is called when the save button is clicked.
     */
    private void savePreset() {
        // get the name & abbreviation that was entered
        String name = this.nameField.getValue().trim();
        String abbreviation = this.abbreviationField.getValue().trim();

        // do not allow saving an empty name
        if (name.isEmpty()) {
            return;
        }

        // automatically use name if abbreviation was left empty
        if (abbreviation.isEmpty()) {
            abbreviation = name.length() >= 4 ? name.substring(0, 4).toUpperCase() : name.toUpperCase();
        }
        // trim the abbreviation if needed (shouldn't since we validate the input before this using character limit)
        else if (abbreviation.length() > 4) {
            abbreviation = abbreviation.substring(0, 4);
        }

        // Create the new Waypoint instance from given input values
        WaypointPreset saved = new WaypointPreset(name, abbreviation, this.selectedColorIndex);

        // if we are editing an existing preset...
        if (this.editIndex >= 0) {
            List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
            if (this.editIndex < presets.size()) {
                // detect if there is another entry that equals this one we are editing (duplicate)
                boolean duplicate = false;
                for (int i = 0; i < presets.size(); i++) {
                    if (i != this.editIndex && saved.equals(presets.get(i))) {
                        duplicate = true;
                        break;
                    }
                }
                // only save if no duplicate
                // TODO: is this right?
                if (!duplicate) {
                    presets.set(this.editIndex, saved);
                    PresetManager.getInstance().setPresets(presets);
                }
            }
            // index given was too large
            else {
                LOGGER.warn("EditPresetScreen.editIndex too large: attempting to save an existing preset with index editIndex=" + this.editIndex + ", but PresetManager's size is presets.size()=" + presets.size() + "!");
            }
        }
        // ... or if we are adding a new preset...
        else {
            // `addPreset` contains logic for overwriting any existing one that equals it (duplicate)
            PresetManager.getInstance().addPreset(saved);
        }

        // if on client...
        if (this.minecraft != null) {
            // if the parent screen is our preset configuration screen, refresh the list to show updates.
            if (this.parentScreen instanceof PresetManagerConfigScreen configScreen) {
                configScreen.refreshList();
            }
            // move us back to the previous (parent) screen
            this.minecraft.setScreen(this.parentScreen);
        }
    }


    /**
     * Implementation of our GUI rendering. Override from Xaero's ScreenBase.
     * This renders the background, draws the title, then draws EditBox labels,
     * and finally calls super.render() to render all the Widgets we registered in init().
     * @param guiGraphics Passed in for us to access the GUI graphics system in this method.
     * @param mouseX The current mouse X position.
     * @param mouseY The current mouse Y position.
     * @param partialTick Passed in for us and we just pass it through to the superclass (something time related)
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int fieldWidth = 200;

        guiGraphics.drawString(this.font, "Waypoint Name:", centerX - fieldWidth / 2, 38, 0xA0A0A0);
        guiGraphics.drawString(this.font, "Abbreviation / Initial:", centerX - fieldWidth / 2, 83, 0xA0A0A0);
        guiGraphics.drawString(this.font, "Color:", centerX - fieldWidth / 2, 128, 0xA0A0A0);

        super.render(guiGraphics, mouseX, mouseY, partialTick); // renders widgets we added with `addWidget()`
    }
}
