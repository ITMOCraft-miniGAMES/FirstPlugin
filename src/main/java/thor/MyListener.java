package thor;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import java.io.IOException;
import java.util.Collection;

public class MyListener implements Listener {
    @EventHandler
    public void onTap(PlayerInteractEvent event) throws IOException {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block!=null) {
            for (int j = 0; j < FirstPlugin.limit; j++) {
                if (FirstPlugin.map[j]!=null) {
                    FirstPlugin.map[j].onTap(event, j);
                }
                for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                    FirstPlugin.customStr[i][j].onTap(event, j);
                }
            }
            BlockData blockData = block.getBlockData();
            Material material = blockData.getMaterial();
            Location loc = block.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            if (material.equals(Material.BAMBOO_WALL_SIGN)) {
                if (y==1&&(z==-1||z==20)&&x>=0&&x<=9) {
                    if (z==-1) {
                        FirstPlugin.tptoCatacombs(player, x);
                    }
                    else {
                        FirstPlugin.tptoCatacombs(player, x+10);
                    }
                }
                else if (x==10&&y==127&& z==37) {
                    event.getPlayer().teleport(new Location(FirstPlugin.world, 0, 0, 0));
                }
                else if (x==11&&y==127&&z==37) {
                    FirstPlugin.builderMode(event.getPlayer());
                }
                else if (x==-2&&y==1&&z==0||x==0&&y==1&&z==14) {
                    event.getPlayer().teleport(FirstPlugin.respawnLoc);
                }
                else if (x==-2&&y==1&&z==-1||x==4&&y==1&&z==17) {
                    int gameMode = 0;
                    if (x==4&&y==1&&z==17) {
                        gameMode=1;
                    }
                    FirstPlugin.autoBegin(player, gameMode);
                }
                else if (x==9&&y==127&&z==37) {
                    player.teleport(new Location(FirstPlugin.world, 4, 0, 17));
                }
            }
            else if (material.equals(Material.MANGROVE_WALL_SIGN)) {
                if (x==-2&&y==129&&z==51) {
                    event.getPlayer().teleport(new Location(FirstPlugin.world, 0, 0, 0));
                }
            }
            if (event.getItem()!=null&&event.getItem().getType().equals(Material.WOODEN_AXE)&&player.hasMetadata("creating")) {
                FirstPlugin.byCreating(block, player);
            }
        }
        if (event.getItem()!=null) {
            ItemStack itemStack = event.getItem();
            Material material = itemStack.getType();
            if (material==Material.COMPASS&&player.hasMetadata("compass")) {
                int i = player.getMetadata("index").get(0).asInt();
                double min = 100000;
                Location playerLoc = player.getLocation();
                double x = playerLoc.getX();
                double y = playerLoc.getY();
                double z = playerLoc.getZ();
                Player target = player;
                for (int j = 0; j < FirstPlugin.kPlayers[i]; j++) {
                    if (!FirstPlugin.players[i][j].equals(player)) {
                        Location loc = FirstPlugin.players[i][j].getLocation();
                        double d = Math.sqrt((x-loc.getX())*(x-loc.getX())+(y-loc.getY())*(y-loc.getY())+(z-loc.getZ())*(z-loc.getZ()));
                        if (d<min) {
                            min=d;
                            target=FirstPlugin.players[i][j];
                        }
                    }
                }
                player.setMetadata("compass", new FixedMetadataValue(FirstPlugin.plugin, target));
                player.sendMessage(ChatColor.GREEN+"Компас указывает на игрока "+ChatColor.YELLOW+target.getName());
            }
        }
    }
    @EventHandler
    public void projectile(PlayerLaunchProjectileEvent event) {
        if (event.getItemStack().getItemMeta().hasCustomModelData()) {
            FirstPlugin.projectile(event);
        }
    }
    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile pr = event.getEntity();
        if (pr.hasMetadata("custom")) {
            FirstPlugin.onHit(event);
        }
        boolean b = true;
        if (event.getHitEntity()!=null&&event.getHitEntity().getType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getHitEntity();
            if (player.hasMetadata("monk")) {
                long time = System.currentTimeMillis();
                if (time-player.getMetadata("monk").get(0).asLong()<=8000) {
                    if (!pr.hasMetadata("reflect")||!pr.getMetadata("reflect").get(0).value().equals(player)) {
                        System.out.println(pr.getType());
                        if (pr.getType().equals(EntityType.FIREBALL)||pr.getType().equals(EntityType.SMALL_FIREBALL)||pr.getType().equals(EntityType.DRAGON_FIREBALL)||pr.getType().equals(EntityType.WITHER_SKULL)) {
                            Fireball fireball = (Fireball) pr;
                            fireball.setDirection(fireball.getDirection().multiply(-1));
                            pr.setVelocity(new Vector(0, 0, 0));
                        }
                        else {
                            Location loc = pr.getLocation();
                            loc.setDirection(loc.getDirection().multiply(-1));
                            pr.teleport(loc);
                            pr.setVelocity(pr.getVelocity().multiply(-1));
                        }
                        pr.setMetadata("reflect", new FixedMetadataValue(FirstPlugin.plugin, player));
                        pr.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, pr.getLocation(), 30);
                    }
                    b=false;
                    event.setCancelled(true);
                }
                else {
                    player.removeMetadata("monk", FirstPlugin.plugin);
                }
            }
        }
        if (b&&pr.hasMetadata("explode")&&(pr.getType()==EntityType.FIREBALL||pr.getType()==EntityType.SMALL_FIREBALL||pr.getType()==EntityType.WITHER_SKULL)) {
            Collection<Player> nearbyPlayers=pr.getWorld().getNearbyPlayers(pr.getLocation(), 2);
            Object[] players = nearbyPlayers.toArray();
            for (int i = 0; i < players.length; i++) {
                if (event.getHitEntity()!=null&&event.getHitEntity().equals(players[i])) {
                    if (pr.getType() == EntityType.WITHER_SKULL) {
                        ((Player) players[i]).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10, 1));
                    } else {
                        ((Player) players[i]).setFireTicks(4);
                    }
                }
                ((Player)players[i]).setMetadata("explode", new FixedMetadataValue(FirstPlugin.plugin, 6));
            }
            pr.remove();
            event.setCancelled(true);
        }
        if (pr.hasMetadata("explode")&&b) {
            pr.getWorld().createExplosion(pr.getLocation(), pr.getMetadata("explode").get(0).asInt(), false);
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        for (int j = 0; j < 10; j++) {
            if (FirstPlugin.map[j]!=null) {
                FirstPlugin.map[j].onBlockBreak(event, j);
            }
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onBlockBreak(event, j);
            }
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player.hasMetadata("inGame")) {
            if (block.getType().equals(Material.STONE) && block.hasMetadata("canBreak")&&player.getMetadata("inGame").get(0).equals(new FixedMetadataValue(FirstPlugin.plugin, true))) {
                event.setCancelled(true);
            }
        }
        if (block.hasMetadata("spawn")&&!player.isOp()) {
            event.setCancelled(true);
        }
        if (block.getType().equals(Material.BEDROCK)&&player.hasMetadata("creating")) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity.getType().equals(EntityType.ITEM_FRAME)) {
            for (int j = 0; j < 10; j++) {
                for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                    FirstPlugin.customStr[i][j].onEntityDamage(event, j);
                }
            }
        }
        if (entity.getType().equals(EntityType.PLAYER)) {
            if (damager.getType().equals(EntityType.PLAYER) && !entity.hasMetadata("inGame")) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.PLAYER)) {
            Player player = (Player)entity;
            if (player.hasMetadata("monk")) {
                event.setCancelled(true);
            }
        }
        if (event.getCause()== EntityDamageEvent.DamageCause.BLOCK_EXPLOSION&&entity.getType()==EntityType.PLAYER&&entity.hasMetadata("explode")) {
            int damage = entity.getMetadata("explode").get(0).asInt();
            if (event.getFinalDamage()>damage) {
                event.setDamage(damage*event.getDamage()/event.getFinalDamage());
            }
            entity.removeMetadata("explode", FirstPlugin.plugin);
        }
    }
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.hasMetadata("canBreak")) {
            block.removeMetadata("canBreak", FirstPlugin.plugin);
        }
        if (player.hasMetadata("inGame")&&block.getType().equals(Material.BARRIER)) {
            event.setCancelled(true);
        }
        if (player.hasMetadata("creating")&&((BuilderMode)(player.getMetadata("creating").get(0).value())).step==1) {
            block.setMetadata("exit", new FixedMetadataValue(FirstPlugin.plugin, block.getType().name().toLowerCase()));
            BuilderMode mode = (BuilderMode) player.getMetadata("creating").get(0).value();
            mode.kPoints++;
        }
        if (FirstPlugin.isOnSpawn(block)&&!player.isOp()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void playerSendMessage(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("creating")) {
            String s = event.getMessage();
            BuilderMode mode = (BuilderMode)player.getMetadata("creating").get(0).value();
            if (s.charAt(0)=='.') {
                System.out.println("byCreating");
                FirstPlugin.byCreating(null, player);
                event.setCancelled(true);
            }
            else {
                if (s.charAt(0)>='1'&&s.charAt(0)<='9') {
                    mode.saturation=Character.getNumericValue(s.charAt(0));
                    player.sendMessage(ChatColor.BLUE+"Вы ввели "+s);
                }
                else if (s.charAt(0)=='0'&&s.charAt(1)=='.') {
                    mode.probab = Double.parseDouble(s);
                    player.sendMessage(ChatColor.BLUE+"Вы ввели "+s);
                }
            }
        }
    }
    public static void dropToken(Location loc) {
        Item item = (Item) FirstPlugin.world.spawnEntity(loc, EntityType.DROPPED_ITEM);
        ItemStack itemStack = new ItemStack(Material.NETHER_STAR, 2+(int)(5*Math.random()));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("Trade token");
        itemStack.setItemMeta(meta);
        item.setItemStack(itemStack);
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();
        dropToken(player.getLocation());
        if (killer!=null&&killer.getType().equals(EntityType.PLAYER)) {
            Scoreboard scoreboard = killer.getScoreboard();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective!=null) {
                Score score = objective.getScore(killer);
                score.setScore(score.getScore() + 1);
            }
        }
        if (killer!=null&&killer.hasMetadata("graveyard")) {
            try {
                event.setDeathMessage(player.getName() + " was slain by Skeleton from " + ((Player) killer.getMetadata("owner").get(0).value()).getName() + "'s " + ChatColor.DARK_PURPLE + "Graveyard Spell");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        FirstPlugin.updatePlayers(player);
        FirstPlugin.updateObservers(player);
        FirstPlugin.flushPlayer(player);
    }
    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FirstPlugin.flushPlayer(player);
        FirstPlugin.updatePlayers(player);
        FirstPlugin.updateObservers(player);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setRespawnLocation(FirstPlugin.respawnLoc);
        //player.teleport(new Location(FirstPlugin.world, 10, 126, 38));
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("Добро пожаловать на СУС");
        if (player.isOp()) {
            player.setGameMode(GameMode.CREATIVE);
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("dropToken")) {
            dropToken(entity.getLocation());
        }
    }
    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        event.setRespawnLocation(new Location(FirstPlugin.world, 10, 126, 38));
    }
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.AREA_EFFECT_CLOUD)) {
            AreaEffectCloud cloud = (AreaEffectCloud) entity;
            if (cloud.getDuration()==600) {
                cloud.setDuration(300);
            }
        }
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (entity.hasMetadata("graveyard")&& entity.getMetadata("graveyard").get(0).value().equals(target)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("monk")) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX()!=to.getX()||from.getZ()!=to.getZ()) {
                long time = System.currentTimeMillis();
                if (time - player.getMetadata("monk").get(0).asLong() <= 8000) {
                    event.setCancelled(true);
                } else {
                    player.removeMetadata("monk", FirstPlugin.plugin);
                }
            }
        }
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        for (int j = 0; j < FirstPlugin.limit; j++) {
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onPortal(event, j);
            }
        }
    }
}
