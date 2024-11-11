package thor;

import json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;

public class Portal extends LocalStructure {
    public Location tpto;
    public Location tpfrom;
    public World world;
    public String descr;

    public Portal(JSONObject room) {
        super(room);
        isSimple=false;
        descr = room.getString("description");
    }
    @Override
    public void end(Generation map) {
        if (!descr.equals("hall")) {
            map.portals.add(this);
        }
        for (int i = 0; i < exit.length; i++) {
            if (exit[i].type==9) {
                world=map.world;
                if (descr.equals("hall")) {
                    world=FirstPlugin.world;
                }
                tpfrom = new Location(world, exit[i].X, exit[i].Y, exit[i].Z);
                break;
            }
        }

    }
    @Override
    public void onPortal(PlayerTeleportEvent event, int n) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to.getWorld()==FirstPlugin.end&&!player.hasMetadata("water")) {
            BoundingBox box = new BoundingBox(FirstPlugin.x[n] + x, FirstPlugin.y[n] + y, FirstPlugin.z[n] + z, FirstPlugin.x[n] + x + dx, FirstPlugin.y[n] + y + dy, FirstPlugin.z[n] + z + dz);
            if (FirstPlugin.isInBox(from, box) && world == from.getWorld()) {
                System.out.println(tpto);
                player.setMetadata("water", new FixedMetadataValue(FirstPlugin.plugin, true));
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask(FirstPlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.removeMetadata("water", FirstPlugin.plugin);
                    }
                }, 5L);
                player.teleport(new Location(tpto.getWorld(), tpto.getX()+FirstPlugin.x[n], tpto.getY()+FirstPlugin.y[n], tpto.getZ()+FirstPlugin.z[n]));
                event.setCancelled(true);
            }
        }
    }
}
