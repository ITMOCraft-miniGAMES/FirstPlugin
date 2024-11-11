package thor;

import json.JSONObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class Teleport extends LocalStructure {
    public int tx;
    public int tz;
    public int ty;
    public int toX;
    public int toY;
    public int toZ;
    public int index;
    public Teleport(JSONObject room, int index) {
        super(room);
        isSimple = false;
        this.index=index;
    }
    @Override
    public void end(Generation map) {
        for (int i = 0; i < exit.length; i++) {
            if (exit[i].type==9) {
                if (exit[i].sx==9) {
                    tx = exit[i].X;
                    ty = exit[i].Y;
                    tz = exit[i].Z;
                }
            }
        }
        LocalBlock block=null;
        boolean loopexit = false;
        int k = 0;
            for (int i = index+1; i < map.kStr && !loopexit; i++) {
                if (map.str[i].getClass().equals(this.getClass())&&(k >= 2 || map.str[i] != this)) {
                    for (int j = 0; j < map.str[i].exit.length; j++) {
                        //System.out.println(Main.str[i].exit[j].type);
                        if (map.str[i].exit[j].type == 9&&map.str[i].exit[j].sx!=9) {
                            block = map.str[i].exit[j];
                        }
                    }
                    loopexit = true;
                    toX = block.X;
                    toY = block.Y;
                    toZ = block.Z;
                }
                if (i== map.kStr-1) {
                    i=0;
                    k++;
                }
            }
    }
    @Override
    public void onTap(PlayerInteractEvent event, int index) {
        Block block = event.getClickedBlock();
        Location blockLoc = block.getLocation();
        BlockData blockData = block.getBlockData();
        if (blockData.getMaterial().equals(Material.WARPED_BUTTON)&& FirstPlugin.x[index] +tx==blockLoc.getBlockX()&& FirstPlugin.y[index] +ty==blockLoc.getBlockY()&& FirstPlugin.z[index] +tz==blockLoc.getBlockZ()) {
            Player player = event.getPlayer();
            blockLoc.set(FirstPlugin.x[index]+toX, FirstPlugin.y[index]+toY, FirstPlugin.z[index]+toZ);
            player.teleport(blockLoc);
        }
    }
}
