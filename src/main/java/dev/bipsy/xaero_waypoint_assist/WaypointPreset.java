package dev.bipsy.xaero_waypoint_assist;

import java.util.Locale;
import java.util.Objects;

/**
 * Data/Model class that represents our preset information that is saved for each waypoint preset in the PresetManager.
 */
public class WaypointPreset
{
    /// The name of the waypoint.
    public String name;

    /// The abbreviation/2-letter symbol for the waypoint.
    /// (Shows up on the map & in-world)
    public String abbreviation;

    /// The color index (used within Xaero's default color system to identify a color)
    /// Use WaypointColor.fromIndex(int) to get the WaypointColor object associated with it.
    /// TODO: we're gonna change this to use our custom color scheme later
    public int colorIndex;

    // TODO add field for waypoint set (must be associated with a real waypoint set, make sure to validate)


    /**
     * Construct a WaypointPreset object, with data validation.
     * @param name The name of the waypoint preset, which is the name of the waypoints that use this preset template.
     * @param abbreviation The initials/abbreviation for this waypoint preset, which shows up on-map and in-game.
     * @param colorIndex The index of the color this preset uses
     */
    public WaypointPreset(String name, String abbreviation, int colorIndex)
    {
        this.name = name != null ? name.trim() : "";
        String trimmedAbbr = abbreviation != null ? abbreviation.trim() : "";
        this.abbreviation = trimmedAbbr.length() > 4 ? trimmedAbbr.substring(0, 4) : trimmedAbbr;
        this.colorIndex = colorIndex;
    }

    /**
     * Object equals implementation. Used to test for duplicates in the PresetManager.
     *
     * @implNote currently only tests against name and color.
     *
     * @param o   the reference object with which to compare.
     * @return true if both contain the same values, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaypointPreset that = (WaypointPreset) o;
        if (this.colorIndex != that.colorIndex) return false;

        String thisName = this.name != null ? this.name.trim() : "";
        String thatName = that.name != null ? that.name.trim() : "";
        if (!thisName.equalsIgnoreCase(thatName)) return false;

        // Don't check against abbreviation for now
        // TODO: this should be included, but later when we use .equals(), we should replace it with another method that checks only against primary attributes (name, color, & set)
        //String thisAbbr = this.abbreviation != null ? this.abbreviation.trim() : "";
        //String thatAbbr = that.abbreviation != null ? that.abbreviation.trim() : "";
        //return thisAbbr.equalsIgnoreCase(thatAbbr);
        return true;
    }
}
