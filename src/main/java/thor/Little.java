package thor;

import json.JSONObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Little extends LocalStructure {
    public int number;
    public int tx;
    public int ty;
    public int tz;
    public Little(JSONObject room) {
        super(room);
        isSimple = false;
    }
    @Override
    public void end(Generation map) {
        for (int i = 0; i < exit.length; i++) {
            if (exit[i].type==9) {
                tx=exit[i].X;
                ty=exit[i].Y;
                tz=exit[i].Z;
            }
        }
    }
    @Override
    public void onTap(PlayerInteractEvent event, int index) {
        Block block = event.getClickedBlock();
        Location blockLoc = block.getLocation();
        if (block.getType()==Material.ACACIA_BUTTON&& FirstPlugin.x[index] +tx==blockLoc.getBlockX()&& FirstPlugin.y[index] +ty==blockLoc.getBlockY()&& FirstPlugin.z[index] +tz==blockLoc.getBlockZ()) {
            Player player = event.getPlayer();
            player.setMetadata("arena", new FixedMetadataValue(FirstPlugin.plugin, player.getLocation()));
            blockLoc.set(FirstPlugin.x[index]+FirstPlugin.arenaCoords.getBlockX()+Math.random()*25+1, FirstPlugin.y[index]+FirstPlugin.arenaCoords.getBlockY()+1, FirstPlugin.z[index]+FirstPlugin.arenaCoords.getBlockZ()+Math.random()*25+1);
            player.teleport(blockLoc);
        }
    }
}
