package dev.bipsy.xaero_waypoint_assist;

import java.util.Locale;
import java.util.Objects;

public class WaypointPreset
{
    // The name of the waypoint.
    public String name;

    // The abbreviation/2-letter symbol for the waypoint.
    // (Shows up on the map & in-world)
    // TODO: we're gonna expand this to 4 letters with font rendering options.
    public String abbreviation;

    // The color index (used within Xaero's default color system to identify a color)
    // TODO: we're gonna change this to use our custom color scheme later
    public int colorIndex;


    public WaypointPreset()
    {
    }

    public WaypointPreset(String name, String abbreviation, int colorIndex)
    {
        this.name = name != null ? name.trim() : "";
        String trimmedAbbr = abbreviation != null ? abbreviation.trim() : "";
        this.abbreviation = trimmedAbbr.length() > 4 ? trimmedAbbr.substring(0, 4) : trimmedAbbr;
        this.colorIndex = colorIndex;
    }

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
        //String thisAbbr = this.abbreviation != null ? this.abbreviation.trim() : "";
        //String thatAbbr = that.abbreviation != null ? that.abbreviation.trim() : "";
        //return thisAbbr.equalsIgnoreCase(thatAbbr);
        return true;
    }

    @Override
    public int hashCode()
    {
        String cleanName = this.name != null ? this.name.trim().toLowerCase(Locale.ROOT) : "";
        String cleanAbbr = this.abbreviation != null ? this.abbreviation.trim().toLowerCase(Locale.ROOT) : "";
        return Objects.hash(cleanName, cleanAbbr, this.colorIndex);
    }
}
