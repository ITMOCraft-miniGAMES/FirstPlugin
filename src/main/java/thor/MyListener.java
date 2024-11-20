package thor;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import static thor.FirstPlugin.BEGINBLOCKBREAK;
import static thor.FirstPlugin.limit;

public class MyListener implements Listener {
    public static boolean bomber = false;
    public static boolean bomberArrow = false;
    @EventHandler
    public void onTap(PlayerInteractEvent event) throws IOException {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (block!=null) {
            for (int j = 0; j < limit; j++) {
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
                else if (x==-13&&y==81&& z==6||x==1009&&y==127&&z==1036) {
                    event.getPlayer().teleport(new Location(FirstPlugin.world, 0, 0, 0));
                }
                else if (x==-11&&y==81&&z==10&&player.isOp()) {
                    FirstPlugin.builderMode(event.getPlayer());
                }
                else if (x==-2&&y==1&&z==0||x==0&&y==1&&z==14||x==1010&&y==127&&z==1036) {
                    event.getPlayer().teleport(FirstPlugin.respawnLoc);
                }
                else if (x==-2&&y==1&&z==-1||x==4&&y==1&&z==17) {
                    int gameMode = 0;
                    if (x==4&&y==1&&z==17) {
                        gameMode=1;
                    }
                    FirstPlugin.autoBegin(player, gameMode);
                }
                else if (x==-13&&y==81&&z==10||x==1008&&y==127&&z==1036) {
                    player.teleport(new Location(FirstPlugin.world, 4, 0, 17));
                }
                else if (x==-15&&y==83&&z==8) {
                    player.teleport(new Location(FirstPlugin.world, 1009, 126, 1037));
                }
            }
            else if (material==Material.OAK_BUTTON) {
                FirstPlugin.classChoice(loc, player);
            }
            else if (material==Material.RESPAWN_ANCHOR) {
                if (FirstPlugin.towerEnd(player, block)) {
                    event.setCancelled(true);
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
            int data = 0;
            if (itemStack.getItemMeta().hasCustomModelData()) {
                data = itemStack.getItemMeta().getCustomModelData();
            }
            Material material = itemStack.getType();
            boolean isRMB = action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
            if (data==1&&(material==Material.RAW_IRON||material==Material.MAGMA_CREAM)&& isRMB) {
                if (material==Material.RAW_IRON) {
                    FirstPlugin.autoBegin(player, 0);
                }
                else {
                    FirstPlugin.autoBegin(player, 1);
                }
            }
            else if (data==1&&(material==Material.BLUE_DYE||material==Material.YELLOW_DYE)&& isRMB) {
                if (material==Material.BLUE_DYE) {
                    FirstPlugin.autoObserve(player, 0);
                }
                else {
                    FirstPlugin.autoObserve(player, 1);
                }
            }
            else if (data==1&&material==Material.RAW_GOLD&& isRMB) {
                FirstPlugin.tAutoBegin(player);
            }
            else if (data == 1 && material == Material.DIAMOND&& isRMB) {
                int n = player.getMetadata("game").get(0).asInt();
                FirstPlugin.updatePlayers(player);
                FirstPlugin.updateObservers(player);
                n++;
                if (n%(limit/2)==0) {
                    n-=(limit/2);
                }
                FirstPlugin.tptoCatacombs(player, n);
            }
            else if (data==1&&material==Material.RAW_COPPER) {
                if (FirstPlugin.russians.contains(player.getName())) {
                    FirstPlugin.russians.remove(player.getName());
                    player.sendMessage("English language is set");
                }
                else {
                    FirstPlugin.russians.add(player.getName());
                    player.sendMessage("Установлен русский язык");
                }
                PrintStream out = new PrintStream("/home/container/plugins/FirstPlugin/russians.txt");
                for (int i = 0; i < FirstPlugin.russians.size(); i++) {
                    out.println(FirstPlugin.russians.get(i));
                }
            }
            else if (data==1&&material==Material.IRON_SWORD) {
                if (isRMB) {
                    if (itemStack.getDurability()==0) {
                        FirstPlugin.cooldown.add(itemStack);
                        short max = material.getMaxDurability();
                        itemStack.setDurability((short) (max - 1));
                        Location loc = player.getLocation();
                        Collection<LivingEntity> entities = player.getWorld().getNearbyLivingEntities(loc, 20);
                        for (LivingEntity target : entities) {
                            Location tLoc = target.getLocation();
                            Vector vector = new Vector(loc.getX()-tLoc.getX(), Math.sqrt((loc.getX()-tLoc.getX())*(loc.getX()-tLoc.getX())+(loc.getZ()-tLoc.getZ())*(loc.getZ()-tLoc.getZ()))/5, loc.getZ()-tLoc.getZ());
                            if (player.hasMetadata("tower")) {
                                vector.multiply(0.1);
                            }
                            else {
                                vector.multiply(0.5);
                            }
                            target.setVelocity(vector);
                        }
                    }
                    else {
                        player.sendMessage(FirstPlugin.translate("Ability is on cooldown", player));
                    }
                }
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
    public void onFishing(PlayerFishEvent event) {
        FirstPlugin.onFishing(event);
    }
    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile pr = event.getEntity();
        if (pr.hasMetadata("custom")) {
            FirstPlugin.onHit(event);
        }
        if (pr.hasMetadata("bomber")) {
            pr.getWorld().createExplosion(pr.getLocation(), 1);
        }
        boolean b = true;
        if (event.getHitEntity()!=null&&event.getHitEntity().getType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getHitEntity();
            if (player.hasMetadata("monk")) {
                long time = System.currentTimeMillis();
                if (time-player.getMetadata("monk").get(0).asLong()<=8000) {
                    if (!pr.hasMetadata("reflect")||!pr.getMetadata("reflect").get(0).value().equals(player)) {
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
                        ((Player) players[i]).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    } else {
                        ((Player) players[i]).setFireTicks(100);
                    }
                }
                ((Player)players[i]).setMetadata("explode", new FixedMetadataValue(FirstPlugin.plugin, 3));
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
        for (int j = 0; j < limit; j++) {
            if (FirstPlugin.map[j]!=null) {
                FirstPlugin.map[j].onBlockBreak(event, j);
            }
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onBlockBreak(event, j);
            }
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player.hasMetadata("inGame")&&block.getType()==Material.STONE) {
            int n = player.getMetadata("game").get(0).asInt();
            if (FirstPlugin.backCount[n]>BEGINBLOCKBREAK) {
                event.setCancelled(true);
            }
        }
        if (!player.hasMetadata("inGame")&&!player.isOp()&&(player.getWorld()==FirstPlugin.world||player.getWorld()==FirstPlugin.nether)) {
            event.setCancelled(true);
        }
        if (block.getType().equals(Material.BEDROCK)&&player.hasMetadata("creating")) {
            event.setCancelled(true);
        }
        if (block.getType()==Material.RESPAWN_ANCHOR) {
            if (FirstPlugin.towerEnd(player, block)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        FirstPlugin.onEntityDamageByEntity(event);
        EntityType entityType = entity.getType();
        EntityType damagerType = damager.getType();
        if (entityType == EntityType.ITEM_FRAME) {
            for (int j = 0; j < limit; j++) {
                for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                    FirstPlugin.customStr[i][j].onEntityDamage(event, j);
                }
            }
        }
        if (damager.getType()==EntityType.PLAYER) {
            Player player = (Player) damager;
            ItemStack itemStack = player.getItemInHand();
            if (player.hasMetadata("monk")||!FirstPlugin.canDamage(player)) {
                event.setCancelled(true);
            }
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                int data = 0;
                if (meta.hasCustomModelData()) {
                    data = meta.getCustomModelData();
                    if (itemStack.getType()==Material.BOW&&data>=1&&data<=3) {
                        event.setDamage(4);
                    }
                }
                if (data==4&&itemStack.getType() == Material.BOW) {
                    event.setCancelled(true);
                    LightningStrike lightning = (LightningStrike) damager.getWorld().spawnEntity(entity.getLocation(), EntityType.LIGHTNING);
                    lightning.setMetadata("owner", new FixedMetadataValue(FirstPlugin.plugin, damager));
                }
                else if (data==1&&itemStack.getType()==Material.IRON_SWORD) {
                    if (!player.hasMetadata("lightsaber")) {
                        player.setMetadata("lightsaber", new FixedMetadataValue(FirstPlugin.plugin, 2));
                    }
                    int v = player.getMetadata("lightsaber").get(0).asInt();
                    if (v==0) {
                        player.setMetadata("lightsaber", new FixedMetadataValue(FirstPlugin.plugin, 2));
                        Location loc = player.getLocation();
                        Location tLoc = entity.getLocation();
                        Vector vector = new Vector(tLoc.getX()-loc.getX(), Math.sqrt((loc.getX()-tLoc.getX())*(loc.getX()-tLoc.getX())+(loc.getZ()-tLoc.getZ())*(loc.getZ()-tLoc.getZ()))/2, tLoc.getZ()-loc.getZ());
                        if (player.hasMetadata("tower")) {
                            vector.multiply(1.5/vector.length());
                        }
                        else {
                            vector.multiply(7.0 / vector.length());
                        }
                        entity.setVelocity(vector);
                    }
                    else {
                        player.setMetadata("lightsaber", new FixedMetadataValue(FirstPlugin.plugin, v-1));
                    }
                }
            }
        }
        else if (damager.getType()==EntityType.LIGHTNING) {
            if (damager.hasMetadata("owner")&&damager.getMetadata("owner").get(0).value()==entity) {
                event.setCancelled(true);
            }
        }
        else if (damagerType ==EntityType.SKELETON&&damager.hasMetadata("bomber")) {
            bomber = true;
            Entity owner = (Entity) damager.getMetadata("bomber").get(0).value();
            if (owner!=null) {
                owner.setMetadata("explosionRes", new FixedMetadataValue(FirstPlugin.plugin, true));
                damager.getWorld().createExplosion(damager.getLocation(), 3);
                damager.remove();
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask(FirstPlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        owner.removeMetadata("explosionRes", FirstPlugin.plugin);
                    }
                }, 10);
            }
        }
        Entity cause = event.getDamageSource().getCausingEntity();
        if (cause!=null&&cause.getType()==EntityType.SKELETON&&cause.hasMetadata("bomber")) {
            if (!(cause.getMetadata("bomber").get(0).value() ==entity)) {
                bomberArrow = true;
                damager.getWorld().createExplosion(damager.getLocation(), 2);
            }
        }
    }
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType()==EntityType.PLAYER) {
            Player player = (Player) entity;
            if (!FirstPlugin.canUseItem(player, event.getItem().getItemStack())) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity entity = event.getWhoClicked();
        InventoryView view = event.getView();
        Location loc = view.getBottomInventory().getLocation();
        ItemStack itemStack = event.getCurrentItem();
        if (entity.hasMetadata("tower")) {
            for (int i = 0; i < FirstPlugin.tLimit; i++) {
                BoundingBox box = new BoundingBox(100 + 100 * i, 0, 100 + 100 * i, 115 + 100 * i, 6, 150 + 100 * i);
                if (FirstPlugin.isInBox(loc, box)) {
                    event.setCancelled(true);
                }
            }
        }
        if (entity.getType()==EntityType.PLAYER) {
            Player player = (Player) entity;
            if (!FirstPlugin.canUseItem(player, itemStack)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        Material material = itemStack.getType();
        ItemMeta meta = itemStack.getItemMeta();
        int data = 0;
        if (meta.hasCustomModelData()) {
            data = meta.getCustomModelData();
        }
        if (data==1&&(material==Material.MAGMA_CREAM||material==Material.RAW_IRON||material==Material.DIAMOND)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onHanger(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType()==EntityType.PLAYER) {
            Player player = (Player)entity;
            if (!FirstPlugin.canDamage(player)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.PLAYER)) {
            Player player = (Player)entity;
            if (player.hasMetadata("monk")||!FirstPlugin.canDamage(player)) {
                event.setCancelled(true);
            }
            if (player.hasMetadata("explosionRes")) {
                EntityDamageEvent.DamageCause cause = event.getCause();
                if (cause== EntityDamageEvent.DamageCause.BLOCK_EXPLOSION||cause== EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    event.setCancelled(true);
                }
            }
        }
        if (event.getCause()== EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            int damage = Integer.MAX_VALUE;
            if (entity.hasMetadata("explode")) {
                damage = entity.getMetadata("explode").get(0).asInt();
                entity.removeMetadata("explode", FirstPlugin.plugin);
            }
            if (bomberArrow) {
                damage = 5;
            }
            if (bomber) {
                damage = 8;
            }
            if (event.getFinalDamage()>damage) {
                event.setDamage(damage*event.getDamage()/event.getFinalDamage());
            }
            if (entity.hasMetadata("bomber")) {
                event.setCancelled(true);
            }
        }
        if (entity.hasMetadata("bomber")&&entity.getType()==EntityType.SKELETON) {
            Skeleton skeleton = (Skeleton) entity;
            if (skeleton.getHealth()<skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/2) {
                skeleton.getEquipment().clear();
            }
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
    public static int dropToken(Location loc) {
        Item item = (Item) FirstPlugin.world.spawnEntity(loc, EntityType.DROPPED_ITEM);
        int k = 2+(int)(5*Math.random());
        ItemStack itemStack = new ItemStack(Material.NETHER_STAR, k);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("Trade token");
        itemStack.setItemMeta(meta);
        item.setItemStack(itemStack);
        return k;
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();
        int countDrop = dropToken(player.getLocation());
        if (killer!=null&&killer.getType().equals(EntityType.PLAYER)) {
            killer.sendMessage(ChatColor.GREEN+"Из жертвы выпало "+countDrop+" токенов торговли");
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
        if (!player.hasMetadata("tower")||FirstPlugin.gameStep[player.getMetadata("tower").get(0).asInt()]==0) {
            FirstPlugin.updatePlayers(player);
            FirstPlugin.updateObservers(player);
            FirstPlugin.flushPlayer(player);
        }
        else {
            Location loc = player.getLocation();
            if (loc.getBlockY()<0) {
                loc.setY(0);
            }
            player.setMetadata("respawnLoc", new FixedMetadataValue(FirstPlugin.plugin, loc));
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
            event.getDrops().clear();
        }
    }
    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FirstPlugin.updatePlayers(player);
        FirstPlugin.updateObservers(player);
        FirstPlugin.flushPlayer(player);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            FirstPlugin.flushPlayer(player);
        }
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
        if (entity.hasMetadata("bomber")&&entity.getType()==EntityType.SKELETON) {
            bomber = true;
            Entity owner = (Entity) entity.getMetadata("bomber").get(0).value();
            if (owner!=null) {
                owner.setMetadata("explosionRes", new FixedMetadataValue(FirstPlugin.plugin, true));
                entity.getWorld().createExplosion(entity.getLocation(), 3);
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask(FirstPlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        owner.removeMetadata("explosionRes", FirstPlugin.plugin);
                    }
                }, 20);
            }
        }
    }
    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("tower")||FirstPlugin.gameStep[player.getMetadata("tower").get(0).asInt()]==0) {
            FirstPlugin.flushPlayer(player);
            event.setRespawnLocation(FirstPlugin.respawnLoc);
        }
        else {
            player.setGameMode(GameMode.SPECTATOR);
            event.setRespawnLocation((Location) player.getMetadata("respawnLoc").get(0).value());
            player.sendTitle(new Title("Respawning in 30 sec"));
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(FirstPlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    FirstPlugin.respawning(player);
                }
            }, 600);
        }
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
        if (entity.hasMetadata("bomber")&&entity.getMetadata("bomber").get(0).value().equals(target)) {
            event.setCancelled(true);
        }
    }
    @EventHandler (priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        event.setCancelled(false);
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
        if (player.hasMetadata("crossbowCD")) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX()!=to.getX()||from.getZ()!=to.getZ()) {
                player.setMetadata("crossbowCD", new FixedMetadataValue(FirstPlugin.plugin, 5000));
            }
        }
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("inGame")&&(event.getTo().getWorld()==FirstPlugin.end)) {
            event.setCancelled(true);
        }
        for (int j = 0; j < limit; j++) {
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onPortal(event, j);
            }
        }
    }
    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        for (int j = 0; j < limit; j++) {
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onExplosion(event, null, j);
            }
        }
    }
    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        for (int j = 0; j < limit; j++) {
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onExplosion(null, event, j);
            }
        }
        if (event.getBlock().getType()==Material.RESPAWN_ANCHOR) {
            event.setCancelled(true);
        }
        bomber = false;
        bomberArrow = false;
    }
    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        Entity entity = event.getEntity();
        if (entity!=null&&entity.hasMetadata("inGame")) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void itemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Inventory inv = player.getInventory();
        ItemStack itemStack = inv.getItem(event.getNewSlot());
        if (itemStack!=null&&itemStack.hasItemMeta()&&itemStack.getItemMeta().hasDisplayName()) {
            ItemMeta meta = itemStack.getItemMeta();
            String name = meta.getDisplayName();
            String realName = FirstPlugin.displayName(name, player);
            if (!name.equals(realName)) {
                meta.setDisplayName(realName);
                int x = inv.first(itemStack);
                itemStack.setItemMeta(meta);
                inv.setItem(x, itemStack);
            }
        }
    }
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        for (int j = 0; j < limit; j++) {
            for (int i = 0; i < FirstPlugin.kStr[j]; i++) {
                FirstPlugin.customStr[i][j].onHangingBreak(event, j);
            }
        }
    }
}
