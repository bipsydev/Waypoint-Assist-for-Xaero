package dev.bipsy.xaero_waypoint_assist;

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


    public WaypointPreset(String name, String abbreviation, int colorIndex)
    {
        this.name = name;
        this.abbreviation = abbreviation;
        this.colorIndex = colorIndex;
    }
    
}
