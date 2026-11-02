package dev.bipsy.xaero_waypoint_assist.gui;

import dev.bipsy.xaero_waypoint_assist.PresetManager;
import dev.bipsy.xaero_waypoint_assist.WaypointPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import xaero.hud.minimap.waypoint.WaypointColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetConfigScreen extends Screen {
    private final Screen parent;
    private PresetList presetList;

    public PresetConfigScreen(Screen parent) {
        super(Component.literal("Waypoint Presets Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.presetList = new PresetList(this.minecraft, this.width, this.height, 32, this.height - 42, 26);
        this.addWidget(this.presetList);

        // Add bottom action buttons
        int buttonWidth = 100;
        int buttonHeight = 20;
        int y = this.height - 30;

        this.addRenderableWidget(Button.builder(Component.literal("Add Preset"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new EditPresetScreen(this, null, -1));
            }
        }).bounds(this.width / 2 - 155, y, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), b -> {
            PresetManager.getInstance().loadDefaults();
            PresetManager.getInstance().save();
            refreshList();
        }).bounds(this.width / 2 - 50, y, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }).bounds(this.width / 2 + 55, y, buttonWidth, buttonHeight).build());

        refreshList();
    }

    public void refreshList() {
        if (this.presetList != null) {
            this.presetList.refreshEntries();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.presetList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public class PresetList extends ContainerObjectSelectionList<PresetList.PresetEntry> {
        public PresetList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
        }

        public void refreshEntries() {
            this.clearEntries();
            List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
            for (int i = 0; i < presets.size(); i++) {
                this.addEntry(new PresetEntry(presets.get(i), i, presets.size()));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(340, PresetConfigScreen.this.width - 20);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 6;
        }

        public class PresetEntry extends ContainerObjectSelectionList.Entry<PresetEntry> {
            private final WaypointPreset preset;
            private final int index;
            private final List<Button> children = new ArrayList<>();
            private final Button editButton;
            private final Button deleteButton;
            private final Button upButton;
            private final Button downButton;

            public PresetEntry(WaypointPreset preset, int index, int totalCount) {
                this.preset = preset;
                this.index = index;

                this.editButton = Button.builder(Component.literal("Edit"), b -> {
                    if (PresetConfigScreen.this.minecraft != null) {
                        PresetConfigScreen.this.minecraft.setScreen(new EditPresetScreen(PresetConfigScreen.this, preset, index));
                    }
                }).bounds(0, 0, 42, 20).build();

                this.deleteButton = Button.builder(Component.literal("X"), b -> {
                    PresetManager.getInstance().removePreset(index);
                    PresetConfigScreen.this.refreshList();
                }).bounds(0, 0, 20, 20).build();

                this.upButton = Button.builder(Component.literal("▲"), b -> {
                    if (index > 0) {
                        List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
                        Collections.swap(presets, index, index - 1);
                        PresetManager.getInstance().setPresets(presets);
                        PresetConfigScreen.this.refreshList();
                    }
                }).bounds(0, 0, 20, 20).build();
                this.upButton.active = index > 0;

                this.downButton = Button.builder(Component.literal("▼"), b -> {
                    if (index < totalCount - 1) {
                        List<WaypointPreset> presets = PresetManager.getInstance().getPresets();
                        Collections.swap(presets, index, index + 1);
                        PresetManager.getInstance().setPresets(presets);
                        PresetConfigScreen.this.refreshList();
                    }
                }).bounds(0, 0, 20, 20).build();
                this.downButton.active = index < totalCount - 1;

                this.children.add(this.editButton);
                this.children.add(this.deleteButton);
                this.children.add(this.upButton);
                this.children.add(this.downButton);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int entryIndex, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                WaypointColor color = null;
                try {
                    color = WaypointColor.fromIndex(this.preset.colorIndex);
                } catch (Exception ignored) {}

                net.minecraft.network.chat.MutableComponent displayText = Component.literal(this.preset.name + " [" + this.preset.abbreviation + "] (");
                if (color != null) {
                    int hex = color.getHex();
                    displayText.append(color.getName().copy().withStyle(style -> style.withColor(hex)));
                } else {
                    displayText.append(Component.literal(String.valueOf(this.preset.colorIndex)));
                }
                displayText.append(")");

                guiGraphics.drawString(PresetConfigScreen.this.font, displayText, left + 4, top + 6, 0xFFFFFF);

                int right = left + entryWidth;
                this.deleteButton.setX(right - 22);
                this.deleteButton.setY(top + 1);
                this.deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);

                this.downButton.setX(right - 44);
                this.downButton.setY(top + 1);
                this.downButton.render(guiGraphics, mouseX, mouseY, partialTick);

                this.upButton.setX(right - 66);
                this.upButton.setY(top + 1);
                this.upButton.render(guiGraphics, mouseX, mouseY, partialTick);

                this.editButton.setX(right - 110);
                this.editButton.setY(top + 1);
                this.editButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }
        }
    }
}
