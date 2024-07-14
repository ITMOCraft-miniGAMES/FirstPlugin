package thor;

import json.JSONObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class Levitation extends LocalStructure {
    int tx;
    int ty;
    int tz;
    public Levitation(JSONObject room) throws IOException {
        super(room);
        isSimple = false;
        command=new String[1];
        boolean loopexit = false;
        for (int i = 0; i < exit.length&&!loopexit; i++) {
            if (exit[i].type==9) {
                command[0]="execute positioned "+exit[i].X+" "+exit[i].Y+" "+exit[i].Z+" if block ~ ~ ~ polished_blackstone_pressure_plate [\"redstone_signal\"=1] run effect @p [r=1] levitation 3 1";
            }
        }
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
        BlockData blockData = block.getBlockData();
        if (blockData.getMaterial().equals(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE)&&(!event.getAction().isLeftClick())&&(!event.getAction().isRightClick())&& FirstPlugin.x[index] +tx==blockLoc.getBlockX()&& FirstPlugin.y[index] +ty==blockLoc.getBlockY()&& FirstPlugin.z[index] +tz==blockLoc.getBlockZ()) {
            //System.out.println(4);
            Player player = event.getPlayer();
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 120, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 1));
        }
    }
}
