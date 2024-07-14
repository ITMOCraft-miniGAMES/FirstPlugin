package thor;

import json.JSONObject;
import org.bukkit.Location;

public class BuilderMode {
    public int step = -1;
    public Location[] points = new Location[100];
    public int kPoints = 0;
    public int saturation = 0;
    public double probab = 0;
    public JSONObject room = new JSONObject();
    public Location roomLoc;
    public BuilderMode() {

    }
}
