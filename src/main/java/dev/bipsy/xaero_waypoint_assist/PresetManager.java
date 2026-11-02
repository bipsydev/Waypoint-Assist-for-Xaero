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
import java.util.List;


/**
 * Singleton class that holds a global preset list in memory, loaded from and saved to a JSON file.
 * Manages duplicates and bulk operations.
 */
public class PresetManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final PresetManager INSTANCE = new PresetManager();

    /// The global list of presets the user has saved, in memory.
    private final List<WaypointPreset> presets = new ArrayList<>();
    private final Path configPath;

    /**
     * Retrieve the global PresetManager instance.
     */
    public static PresetManager getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor that loads from the hardcoded .json file path within the config directory of the client user.
     */
    private PresetManager() {
        this.configPath = FMLPaths.CONFIGDIR.get().resolve("xaero_waypoint_assist_presets.json");
        load();
    }

    /**
     * Retrieve a copy of the preset list.
     */
    public synchronized List<WaypointPreset> getPresets() {
        return new ArrayList<>(this.presets);
    }

    /**
     * Search the preset manager for a preset that matches the one given, if it already exists.
     * @param preset The preset information to search for in the manager.
     * @return The instance of the preset that exists already in the manager that matches the given preset.
     */
    public synchronized WaypointPreset get(WaypointPreset preset) {
        if (preset == null) {
            return null;
        }
        for (WaypointPreset existing : this.presets) {
            if (preset.equals(existing)) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Get the index/position of this preset in the preset manager's list of presets.
     * @param preset The object reference to find the index of.
     * @return The index of `preset` within our list of presets, or -1 if not within the preset manager.
     */
    public synchronized int find(WaypointPreset preset) {
        if (preset == null) {
            return -1;
        }
        for (int i = 0; i < this.presets.size(); i++) {
            WaypointPreset existing = this.presets.get(i);
            if (preset.equals(existing)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Test if this preset is already contained in the manager. Uses `get()` and tests for null.
     * @param preset The preset to test if it is already contained in this manager.
     * @return true if it exists, false otherwise.
     */
    public synchronized boolean contains(WaypointPreset preset) {
        return get(preset) != null;
    }

    /**
     * Replaces the preset list in memory with a new list, and saves to disk.
     * @param newPresets the new list to replace with.
     */
    public synchronized void setPresets(List<WaypointPreset> newPresets) {
        this.presets.clear();
        if (newPresets != null) {
            for (WaypointPreset preset : newPresets) {
                if (preset != null) {
                    // if an existing waypoint with same name & color exists, remove it and overwrite with new secondary attributes (ie, abbreviation)
                    WaypointPreset existing = get(preset);
                    if (existing != null) {
                        this.presets.remove(existing);
                    }
                    this.presets.add(new WaypointPreset(preset.name, preset.abbreviation, preset.colorIndex));
                }
            }
        }
        // saves to disk (.json)
        save();
    }


    /**
     * Attempt to add a new preset, or if it already exists via .equals(), overwrite with new secondary attributes
     * (not considered during .equals()
     * @param preset The new preset to save.
     * @return true if a new preset was added or an existing one was replaced/updated; false otherwise.
     */
    public synchronized boolean addPreset(WaypointPreset preset) {
        if (preset != null) {
            // if an existing waypoint with same name & color exists, remove it and overwrite with new secondary attributes (ie, abbreviation)
            WaypointPreset existing = get(preset);
            int existingIdx = this.presets.size(); // default place new preset at end of list
            if (existing != null) {
                existingIdx = find(existing); // capture the index/position of the existing preset if we are replacing it
                this.presets.remove(existing);
            }
            this.presets.add(existingIdx, preset);
            save();
            return true;
        }
        return false;
    }

    /**
     * Remove a preset from given index position.
     */
    public synchronized void removePreset(int index) {
        if (index >= 0 && index < this.presets.size()) {
            this.presets.remove(index);
            save();
        }
    }


    /**
     * Load from disk (.json file) or create default config if none exists.
     */
    public synchronized void load() {
        // create default config if it doesn't exist
        if (!Files.exists(configPath)) {
            loadDefaults();
            save();
            return;
        }

        // read the .json file:
        try (Reader reader = Files.newBufferedReader(configPath)) {
            Type listType = new TypeToken<List<WaypointPreset>>() {}.getType();
            // load directly into List using GSON here
            List<WaypointPreset> loaded = GSON.fromJson(reader, listType);
            this.presets.clear();
            // add all loaded presets into the this.presets list in memory
            if (loaded != null && !loaded.isEmpty()) {
                for (WaypointPreset preset : loaded) {
                    if (preset != null && !contains(preset)) {
                        this.presets.add(preset);
                    }
                }
                // load defaults if empty
                if (this.presets.isEmpty()) {
                    LOGGER.warn("Loaded empty presets, reloading default presets.");
                    loadDefaults();
                }
            } else {
                LOGGER.warn("Loaded .json presets file, reloading default presets.");
                loadDefaults();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load waypoint presets from {}", configPath, e);
            loadDefaults();
        }
    }


    /**
     * Save to disk (.json file)
     */
    public synchronized void save() {
        try {
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            List<WaypointPreset> uniquePresets = new ArrayList<>();
            for (WaypointPreset preset : this.presets) {
                if (preset != null) {
                    boolean exists = false;
                    for (WaypointPreset u : uniquePresets) {
                        if (preset.equals(u)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        uniquePresets.add(preset);
                    }
                }
            }
            this.presets.clear();
            this.presets.addAll(uniquePresets);

            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this.presets, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save waypoint presets to {}", configPath, e);
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
