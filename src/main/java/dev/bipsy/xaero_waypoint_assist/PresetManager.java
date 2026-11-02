package dev.bipsy.xaero_waypoint_assist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final PresetManager INSTANCE = new PresetManager();

    private final List<WaypointPreset> presets = new ArrayList<>();
    private final Path configPath;

    public static PresetManager getInstance() {
        return INSTANCE;
    }

    private PresetManager() {
        this.configPath = FMLPaths.CONFIGDIR.get().resolve("xaero_waypoint_assist_presets.json");
        load();
    }

    public synchronized List<WaypointPreset> getPresets() {
        return new ArrayList<>(this.presets);
    }

    public synchronized void setPresets(List<WaypointPreset> newPresets) {
        this.presets.clear();
        if (newPresets != null) {
            for (WaypointPreset preset : newPresets) {
                if (preset != null) {
                    this.presets.add(new WaypointPreset(preset.name, preset.abbreviation, preset.colorIndex));
                }
            }
        }
        save();
    }

    public synchronized void addPreset(WaypointPreset preset) {
        if (preset != null) {
            this.presets.add(preset);
            save();
        }
    }

    public synchronized void removePreset(int index) {
        if (index >= 0 && index < this.presets.size()) {
            this.presets.remove(index);
            save();
        }
    }

    public synchronized void load() {
        if (!Files.exists(configPath)) {
            loadDefaults();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Type listType = new TypeToken<List<WaypointPreset>>() {}.getType();
            List<WaypointPreset> loaded = GSON.fromJson(reader, listType);
            this.presets.clear();
            if (loaded != null && !loaded.isEmpty()) {
                this.presets.addAll(loaded);
            } else {
                loadDefaults();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load waypoint presets from " + configPath, e);
            loadDefaults();
        }
    }

    public synchronized void save() {
        try {
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this.presets, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save waypoint presets to " + configPath, e);
        }
    }

    public synchronized void loadDefaults() {
        this.presets.clear();
        this.presets.add(new WaypointPreset("Base", "BS", 2));
        this.presets.add(new WaypointPreset("Village", "VL", 4));
        this.presets.add(new WaypointPreset("Nether Portal", "NP", 1));
        this.presets.add(new WaypointPreset("Mine", "MN", 6));
        this.presets.add(new WaypointPreset("Death", "D", 12));
    }
}
