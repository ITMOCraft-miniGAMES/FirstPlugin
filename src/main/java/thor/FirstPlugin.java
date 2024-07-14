package thor;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import json.JSONArray;
import json.JSONObject;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class FirstPlugin extends JavaPlugin {
    public static int gameTime = 1800;
    public static int limit = 20;
    public static int time = 30;
    public static Location arenaCoords;
    public static LocalStructure[][] customStr = new LocalStructure[300][limit];
    public static int[] kStr = new int[limit];
    public static int[] x = new int[limit];
    public static int[] y = new int[limit];
    public static int[] z = new int[limit];
    public static Player[][] players = new Player[limit][10];
    public static boolean[] isGame = new boolean[limit];
    public static int[] kPlayers = new int[limit];
    public static boolean[] canBreak = new boolean[limit];
    public static int[] backCount = new int[limit];
    public static Generation[] map = new Generation[limit];
    public static int[] kObs = new int[limit];
    public static World world;
    public static World nether;
    public static Scoreboard[] scoreboard = new Scoreboard[limit];
    public static Objective[] objectives = new Objective[limit];
    public static Player[][] observers = new Player[limit][100];
    public static Plugin plugin;
    public static Location spawn1;
    public static Location spawn2;
    public static Location respawnLoc;
    public static List<ItemStack> cooldown = new ArrayList<>();
    public static HashMap<String, Integer> cooldownTime = new HashMap<>();
    public static int kSpells = 1;
    public static ArrayList<Location>[] spells = new ArrayList[kSpells];
    public static ArrayList<Integer>[] times = new ArrayList[kSpells];
    public static ArrayList<Entity>[] owners = new ArrayList[kSpells];
    Logger logger;
    public static int q = 0;
    @Override
    public void onEnable() {
        plugin=this;
        world=Bukkit.getWorlds().get(0);
        nether=Bukkit.getWorlds().get(1);
        spawn1 = new Location(world, -60, -10, -20);
        spawn2 = new Location(world, 60, 180, 130);
        respawnLoc = new Location(world, 11, 125, 38);
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        logger = getLogger();
        arenaCoords = new Location(world, 512, 0, 512);
        spawnProtect();
        try {
            for (int i = 0; i < limit; i++) {
                updateSign(i);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < kSpells; i++) {
            spells[i]= new ArrayList<>();
            times[i]=new ArrayList<>();
            owners[i]=new ArrayList<>();
        }
        cooldownTime.put("trident1", 5);
        cooldownTime.put("trident2", 10);
        cooldownTime.put("trident3", 6);
        Location loc = new Location(world, 0, 0, 0);
        /*
        for (int q = 1; q <= 10; q++) {
            FirstPlugin.q=q;
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    int x = FirstPlugin.q * 10000;
                    int y = 0;
                    int z = FirstPlugin.q * 10000;
                    for (int i = 0; i <= 256; i++) {
                        for (int j = -63; j <= 0; j++) {
                            for (int k = 0; k <= 256; k++) {
                                loc.set(i + x, y + j, z + k);
                                Block block = FirstPlugin.world.getBlockAt(loc);
                                if (i == 0 || i == 256 || j == 0 || j == -63 || k == 0 || k == 256) {
                                    if (!(block.getType() == Material.BEDROCK)) {
                                        block.setType(Material.BEDROCK);
                                    }
                                } else {
                                    if (!(block.getType() == Material.WATER)) {
                                        block.setType(Material.WATER);
                                    }
                                }
                            }
                        }
                    }
                }
            }, 300L);
        }

         */
        int x1 =-10000;
        int y1 = 0;
        int z1 = -10000;
        for (int i = 0; i <= 256; i++) {
            for (int j = -63; j <= 0; j++) {
                for (int k = 0; k <= 256; k++) {
                    loc.set(i + x1, y1 + j, z1 + k);
                    Block block = FirstPlugin.world.getBlockAt(loc);
                    if (i == 0 || i == 256 || j == 0 || j == -63 || k == 0 || k == 256) {
                        if (!(block.getType() == Material.BEDROCK)) {
                            block.setType(Material.BEDROCK);
                        }
                    } else {
                        if (!(block.getType() == Material.WATER)) {
                            block.setType(Material.WATER);
                        }
                    }
                }
            }
        }
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < kSpells; i++) {
                    for (int j = 0; j < spells[i].size(); j++) {
                        int t = times[i].get(j);
                        if (t>0) {
                            t--;
                            times[i].set(j, t);
                            Location loc = spells[i].get(j);
                            World world = loc.getWorld();
                            Location particleLoc = new Location(world, 0, 0, 0);
                            double x = loc.getX();
                            double y = loc.getY();
                            double z = loc.getZ();
                            boolean f = true;
                            double r = 8;
                            for (double Y = y - r; Y <= y + r; Y += 0.5) {
                                double R = Math.sqrt(r * r - (y - Y) * (y - Y));
                                double l = 2 * Math.PI * R;
                                for (double n = 0; n < l; n += 0.6) {
                                    double b = 2 * Math.PI * n / l;
                                    double X = x + Math.cos(b) * R;
                                    double Z = z + Math.sin(b) * R;
                                    particleLoc.set(X, Y, Z);
                                    if (f) {
                                        f=false;
                                    }
                                    world.spawnParticle(Particle.SPELL_MOB, particleLoc, 1);
                                    //world.getBlockAt(particleLoc).setType(Material.GLASS);
                                }
                            }
                            int dx = (int) (Math.random()*r*2);
                            int dy = (int) (Math.random()*r*2);
                            int dz = (int) (Math.random()*r*2);
                            m:
                            for (int X = (int) (x-r); X <= x+r; X++) {
                                for (int Y = (int) (y-r); Y <= y+r; Y++) {
                                    for (int Z = (int) (z-r); Z <= z+r; Z++) {
                                        int cx = X+dx;
                                        int cy = Y+dy;
                                        int cz = Z+dz;
                                        if (cx>x+r) {
                                            cx=cx-2*(int)r;
                                        }
                                        if (cy>y+r) {
                                            cy=cy-2*(int)r;
                                        }
                                        if (cz>z+r) {
                                            cz=cz-2*(int)r;
                                        }
                                        if ((x-cx)*(x-cx)+(y-cy)*(y-cy)+(z-cz)*(z-cz)<=r*r) {
                                            Location blockLoc = new Location(loc.getWorld(), cx, cy, cz);
                                            Block block = world.getBlockAt(blockLoc);
                                            blockLoc.setY(cy-1);
                                            Block under = world.getBlockAt(blockLoc);
                                            blockLoc.setY(cy+1);
                                            Block upper = world.getBlockAt(blockLoc);
                                            if (block.getType().equals(Material.AIR)&&upper.getType().equals(Material.AIR)&&!under.getType().equals(Material.AIR)) {
                                                Skeleton skeleton = (Skeleton)world.spawnEntity(new Location(loc.getWorld(), cx, cy, cz), EntityType.SKELETON);
                                                skeleton.getEquipment().clear();
                                                skeleton.setMetadata("graveyard", new FixedMetadataValue(plugin, owners[i].get(j)));
                                                skeleton.setHealth(1);
                                                break m;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            times[i].remove(j);
                            spells[i].remove(j);
                            owners[i].remove(j);
                            j--;
                        }
                    }
                }
                for (int i = 0; i < cooldown.size(); i++) {
                    ItemStack itemStack = cooldown.get(i);
                    int data = itemStack.getItemMeta().getCustomModelData();
                    Material material = itemStack.getType();
                    short max = material.getMaxDurability();
                    String s = material.name().toLowerCase()+data;
                    if (cooldownTime.containsKey(s)) {
                        short d = (short) ((double)max/cooldownTime.get(s));
                        if (itemStack.getDurability()-d>0) {
                            itemStack.setDurability((short) (itemStack.getDurability() - d));
                        }
                        else {
                            itemStack.setDurability((short) 0);
                            cooldown.remove(i);
                            i--;
                        }
                    }
                    else {
                        plugin.getServer().sendMessage(Component.text(ChatColor.RED+"Ошибка 999 в использовании посоха, сообщите администрации!"));
                    }
                }
                for (int i = 0; i < backCount.length; i++) {
                    for (int j = 0; j < kPlayers[i]; j++) {
                        try {
                            if (players[i][j].hasMetadata("compass")) {
                                Player target = (Player) players[i][j].getMetadata("compass").get(0).value();
                                Inventory inv = players[i][j].getInventory();
                                if (inv.contains(Material.COMPASS)) {
                                    int index = inv.first(Material.COMPASS);
                                    ItemStack itemStack = inv.getItem(index);
                                    CompassMeta meta = (CompassMeta) itemStack.getItemMeta();
                                    meta.setLodestone(target.getLocation());
                                    itemStack.setItemMeta(meta);
                                    inv.setItem(index, itemStack);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (backCount[i]>0&&!isGame[i]) {
                        backCount[i]--;
                        objectives[i].setDisplayName("Начало через "+backCount[i]);
                        if (backCount[i] == time - 10) {
                            Location location = new Location(world, (i + 1) * 10000, 0, (i + 1) * 10000);
                            if (i>=limit/2) {
                                location = new Location(nether, -(i-limit/2 + 1) * 10000, 0, -(i-limit/2 + 1) * 10000);
                            }
                            try {
                                loadMap(location, i, false, 2*i/limit);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (backCount[i] == 0) {
                            isGame[i] = true;
                            backCount[i]=gameTime;
                            updateSign(i);
                            World currentWorld = world;
                            if (i>=limit/2) {
                                currentWorld=nether;
                            }
                            for (int j = 0; j < kPlayers[i]; j++) {
                                Player player = players[i][j];
                                players[i][j].setMetadata("inGame", new FixedMetadataValue(plugin, true));
                                Score score = objectives[i].getScore(player);
                                score.setScore(0);
                                Location tpLoc;
                                if (map[i].tpX[j]!=0) {
                                    tpLoc = new Location(currentWorld, map[i].tpX[j] + x[i], map[i].tpY[j]+y[i], map[i].tpZ[j] + z[i]);
                                }
                                else {
                                    tpLoc = new Location(currentWorld, map[i].tpX[0], map[i].tpY[0], map[i].tpZ[0]);
                                }
                                player.teleport(tpLoc);
                                giveItems(player);
                            }
                            killItems(i, currentWorld);
                        }
                    }
                    else if (backCount[i]>0) {
                        backCount[i]--;
                        String s = backCount[i]/60+":"+backCount[i]%60+" Убийства";
                        objectives[i].setDisplayName(s);
                        if (backCount[i]==900) {
                            canBreak[i]=true;
                            for (int j = 0; j < kPlayers[i]; j++) {
                                players[i][j].setMetadata("inGame", new FixedMetadataValue(plugin, false));
                                players[i][j].sendTitle(new Title("Теперь можно ломать камень!"));
                                ItemStack itemStack = new ItemStack(Material.COMPASS);
                                CompassMeta meta = (CompassMeta) itemStack.getItemMeta();
                                meta.setLodestoneTracked(true);
                                meta.setDisplayName(ChatColor.BLUE+"Player Tracker");
                                meta.setLodestone(players[i][j].getLocation());
                                itemStack.setItemMeta(meta);
                                players[i][j].getInventory().addItem(itemStack);
                                players[i][j].setMetadata("compass", new FixedMetadataValue(plugin, players[i][j]));
                                players[i][j].setMetadata("index", new FixedMetadataValue(plugin, i));
                            }
                        }
                        if (backCount[i]==0) {
                            World currentWorld = world;
                            if (i>=limit/2) {
                                currentWorld=nether;
                            }
                            Location loc = new Location(currentWorld, map[i].str[0].x+5+x[i], map[i].str[0].y+5+y[i], map[i].str[0].z+5+z[i]);
                            if (i<limit/2) {
                                for (int j = 0; j < kPlayers[i]; j++) {
                                    players[i][j].teleport(loc);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    public static void killItems(int n, World world) {
        BoundingBox box = new BoundingBox(x[n], y[n], z[n], x[n]+512, y[n]+128, z[n]+512);
        Object[] entities = world.getNearbyEntities(box).toArray();
        for (int i = 0; i < entities.length; i++) {
            Entity entity = (Entity) entities[i];
            if (entity.getType()==EntityType.DROPPED_ITEM) {
                entity.remove();
            }
        }
    }
    public static void autoBegin(Player player, int gameMode) throws IOException {
        int maxPlayers = 0;
        int maxN = -1;
        int l = limit*gameMode/2;
        int r = l+limit/2;
        for (int i = l; i < r; i++) {
            if (!isGame[i]&&kPlayers[i]>maxPlayers) {
                maxPlayers=kPlayers[i];
                maxN=i;
            }
        }
        if (maxN!=-1) {
            tptoCatacombs(player, maxN);
        }
        else {
            int minPlayers = 6;
            int minN = -1;
            for (int i = l; i < r; i++) {
                if (isGame[i]&&kPlayers[i]<minPlayers) {
                    minPlayers=kPlayers[i];
                    minN=i;
                }
            }
            if (minN!=-1) {
                tptoCatacombs(player, minN);
            }
            else {
                for (int i = l; i < r; i++) {
                    if (!isGame[i]) {
                        tptoCatacombs(player, i);
                        break;
                    }
                }
            }
        }
    }
    public static void giveItems(Player player) {
        player.clearActivePotionEffects();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 2400, 255));
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 36100, 1));
        player.setGameMode(GameMode.SURVIVAL);
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(1, new ItemStack(Material.GREEN_WOOL, 16));
    }
    public static void tptoCatacombs(Player player, int n) throws IOException {
        player.sendMessage("Используйте команду /lobby чтобы вернуться обратно в лобби");
        if (!isGame[n]) {
            if (kPlayers[n]<10) {
                players[n][kPlayers[n]]=player;
                kPlayers[n]++;
                if (kPlayers[n]==2) {
                    backCount[n]=time+1;
                    for (int i = 0; i < kPlayers[n]; i++) {
                        players[n][i].sendMessage("Игра начнётся через "+time+" секунд");
                        players[n][i].setGameMode(GameMode.ADVENTURE);
                    }
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    scoreboard[n]=manager.getNewScoreboard();
                    objectives[n]=scoreboard[n].registerNewObjective("game"+n, "dummy");
                    objectives[n].setDisplaySlot(DisplaySlot.SIDEBAR);
                    objectives[n].setDisplayName("Начало через "+time);
                    players[n][0].setScoreboard(scoreboard[n]);
                    players[n][1].setScoreboard(scoreboard[n]);
                    map[n]=new Generation(2*n/limit);
                    if (!map[n].error.equals("")) {
                        plugin.getServer().sendMessage(Component.text(map[n].error));
                    }
                }
                else if (kPlayers[n]>2) {
                    player.setScoreboard(scoreboard[n]);
                }
                else {
                    player.sendMessage("Ждите других игроков");
                }
            }
        }
        else {
            observers[n][kObs[n]]=player;
            kObs[n]++;
            player.sendMessage("Ожидайте окончания игры. На данный момент выживших осталось "+FirstPlugin.kPlayers[n]+" игрока");
            player.sendMessage("Используйте команду /tpto чтобы телепортироваться к случайному игроку.");
            player.setMetadata("observer", new FixedMetadataValue(plugin, true));
            player.setGameMode(GameMode.SPECTATOR);
        }
        updateSign(n);
        Location loc = new Location(world, 10000*(n+1), 70, 10000*(n+1));
        if (n>=limit/2) {
            loc = new Location(nether, -10000*(n-limit/2+1), 130, -10000*(n-limit/2+1));
        }
        player.teleport(loc);
    }
    public static void updateSign(int n) {
        Location loc = new Location(world, n, 1, -1);
        if (n>=limit/2) {
            loc = new Location(world, n-limit/2, 1, 20);
        }
        Block block = loc.getBlock();
        Sign sign = (Sign) block.getState();
        if (isGame[n]) {
            sign.setLine(0, ChatColor.BLUE + "Идёт игра");
        } else {
            if (backCount[n] == 0) {
                sign.setLine(0, ChatColor.BLUE + "Ждём игроков");
            } else {
                sign.setLine(0, ChatColor.BLUE + "Игра вот-вот начнётся");
            }
        }
        sign.setLine(1, "Игроков: " + kPlayers[n] + "/10");
        sign.setLine(2, "Наблюдателей: " + kObs[n]);
        sign.update();
    }
    public static void flushPlayer(Player player) {
        player.removeMetadata("inGame", plugin);
        player.removeMetadata("observer", plugin);

        if (player.hasMetadata("creating")) {
            player.removeMetadata("creating", plugin);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.clearActivePotionEffects();
        player.teleport(new Location(world, 11, 126, 38));
    }
    public static void builderMode(Player player) {
        int n = -1;
        boolean b = false;
        try {
            Scanner in = new Scanner(new File("/home/container/plugins/FirstPlugin/builder_mode.txt"));
            ArrayList<String> names = new ArrayList<>();
            String name = player.getName();
            while (in.hasNextLine()) {
                String s = in.nextLine();
                n++;
                names.add(s);
                if (s.equals(name)) {
                    b=true;
                    break;
                }
            }
            in.close();
            if (!b) {
                n++;
                names.add(name);
                PrintStream out = new PrintStream("/home/container/plugins/FirstPlugin/builder_mode.txt");
                while (!names.isEmpty()) {
                    out.println(names.remove(0));
                }
                out.close();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Location loc = new Location(world, 0, 0, 0);
        n=n*100;
        if (!b) {
            for (int i = 1000 + n; i <= 1070 + n; i++) {
                for (int j = 100; j <= 170; j++) {
                    for (int k = 1000 + n; k <= 1070 + n; k++) {
                        loc.set(i, j, k);
                        Block block = world.getBlockAt(loc);
                        block.removeMetadata("exit", plugin);
                        if (i > 1000 + n && i < 1070 + n && j > 100 && j < 170 && k > 1000 + n && k < 1070 + n) {
                            block.setType(Material.AIR);
                        } else {
                            block.setType(Material.BEDROCK);
                        }
                    }
                }
            }
        }
        player.setMetadata("creating", new FixedMetadataValue(plugin, new BuilderMode()));
        player.teleport(new Location(world, 1010+n, 110, 1010+n));
        player.setGameMode(GameMode.CREATIVE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 1));
        player.sendMessage("Стройте свою комнату или холл а затем введите команду /create ИмяВашейКомнаты.");
        player.sendMessage("Где вместо Имени вашей комнаты напишите любое слово как-то связанное с внешним её видом.");
        player.sendMessage("Если передумали в любой момент введите команду /lobby");
    }
    public static void gameEnd(int n) {
        Player winner = players[n][0];
        winner.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 200, 255));
        winner.sendMessage(ChatColor.GREEN+"Вы победили!");
        plugin.getServer().sendMessage(Component.text(ChatColor.BLUE+"Игрок "+winner.getName()+" победил в игре!"));
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                flushPlayer(winner);
                kObs[n]=0;
                kPlayers[n]=0;
                isGame[n]=false;
                backCount[n]=0;
                objectives[n].unregister();
                updateSign(n);
                System.out.println("Завершили игру");
                for (int i = 0; i < kObs[n]; i++) {
                    try {
                        tptoCatacombs(observers[n][i], n);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 200L);
    }
    public static void projectile(PlayerLaunchProjectileEvent event) {
        ItemStack itemStack = event.getItemStack();
        Projectile pr = event.getProjectile();
        Player player = event.getPlayer();
        ItemMeta meta = itemStack.getItemMeta();
        int data = meta.getCustomModelData();
        Material material = itemStack.getType();
        short max = material.getMaxDurability();
        World world = player.getWorld();
        //System.out.println(max+" "+(max-itemStack.getDurability()));
        if (itemStack.getDurability()==0) {
            if (material.equals(Material.TRIDENT)) {
                Location loc = new Location(world, pr.getX(), pr.getY(), pr.getZ());
                loc.setDirection(player.getLocation().getDirection());
                if (data == 1) {
                    SmallFireball fireball = (SmallFireball) world.spawnEntity(loc, EntityType.SMALL_FIREBALL);
                    fireball.setShooter(pr.getShooter());
                    fireball.setGravity(false);
                    fireball.setMetadata("explode", new FixedMetadataValue(plugin, 3));
                }
                else if (data==2) {
                    DragonFireball fireball = (DragonFireball) world.spawnEntity(loc, EntityType.DRAGON_FIREBALL);
                    fireball.setShooter(pr.getShooter());
                    fireball.setGravity(false);
                }
                else if (data==3) {
                    WitherSkull skull = (WitherSkull) world.spawnEntity(loc, EntityType.WITHER_SKULL);
                    skull.setShooter(pr.getShooter());
                    skull.setGravity(false);
                    skull.setMetadata("explode", new FixedMetadataValue(plugin, 3));
                }
                itemStack.setDurability((short) (max - 1));
                cooldown.add(itemStack);
                event.setCancelled(true);
            }
            else if (material.equals(Material.SNOWBALL)) {
                if (data==1) {
                    pr.setMetadata("custom", new FixedMetadataValue(plugin, material));
                    pr.setMetadata("customData", new FixedMetadataValue(plugin, data));
                    pr.setMetadata("owner", new FixedMetadataValue(plugin, player));
                    plugin.getServer().sendMessage(Component.text(player.getName() + " has cast " + meta.getDisplayName()));
                }
                else if (data==2) {
                    Inventory inv = player.getInventory();
                    inv.remove(itemStack);
                    itemStack.setAmount(itemStack.getAmount()-1);
                    inv.addItem(itemStack);
                    player.setMetadata("monk", new FixedMetadataValue(plugin, System.currentTimeMillis()));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 255));
                    world.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 30);
                    event.setCancelled(true);
                }
            }
        }
        else {
            player.sendMessage(ChatColor.RED+"Этот посох перезаряжается");
            event.setCancelled(true);
        }
    }
    public static void onHit(ProjectileHitEvent event) {
        Projectile pr = event.getEntity();
        Material material = (Material) pr.getMetadata("custom").get(0).value();
        Location loc = pr.getLocation();
        int data = pr.getMetadata("customData").get(0).asInt();
        if (material.equals(Material.SNOWBALL)) {
            if (data==1) {
                spells[0].add(loc);
                times[0].add(10);
                owners[0].add((Entity) pr.getMetadata("owner").get(0).value());
            }
        }
    }
    public static void updatePlayers(Player quited) {
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < kPlayers[i]; j++) {
                if (players[i][j]==quited) {
                    for (int q = j; q < kPlayers[i]-1; q++) {
                        players[i][j]=players[i][j+1];
                    }
                    System.out.println("нашли игрока номер "+j+" из "+kPlayers[i]);
                    kPlayers[i]--;
                    if (objectives[i]!=null) {
                        try {
                            Score score = objectives[i].getScore(quited);
                            scoreboard[i].resetScores(quited);
                            score.resetScore();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    updateSign(i);
                    if (kPlayers[i]==1) {
                        System.out.println("gameEnd");
                        gameEnd(i);
                    }
                }
            }
        }
    }
    public static void updateObservers(Player quited) {
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < kObs[i]; j++) {
                if (observers[i][j]==quited) {
                    for (int q = j; q < kObs[i]-1; q++) {
                        observers[i][j]=observers[i][j+1];
                    }
                    kObs[i]--;
                    updateSign(i);
                }
            }
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static void spawnProtect() {
        Location loc = new Location(world, 0, 0, 0);
        for (int i = -60; i <= 60; i++) {
            for (int j = -10; j < 180; j++) {
                for (int k = 40; k < 130; k++) {
                    loc.set(i, j, k);
                    Block block = world.getBlockAt(loc);
                    block.setMetadata("spawn", new FixedMetadataValue(plugin, true));
                }
            }
        }
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String commandLabel, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("catacombs")) {
            Player player = (Player)sender;
            if (args[0].equalsIgnoreCase("test")) {
                Location loc = player.getLocation();
                if (args.length==5) {
                    loc.set(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                }
                try {
                    map[10*Integer.parseInt(args[1])+9]=new Generation(Integer.parseInt(args[1]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    loadMap(loc, 10*Integer.parseInt(args[1])+9, true, Integer.parseInt(args[1]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (args[0].equalsIgnoreCase("custom")) {
                ItemStack itemStack = new ItemStack(Material.COMPASS);
                CompassMeta meta = (CompassMeta) itemStack.getItemMeta();
                meta.setLodestoneTracked(true);
                meta.setDisplayName(ChatColor.BLUE+"Player Tracker");
                meta.setLodestone(new Location(player.getWorld(), 1000, 1, 1000));
                itemStack.setItemMeta(meta);
                player.getInventory().addItem(itemStack);
            }
            else if (args[0].equalsIgnoreCase("time")) {
                Block block = world.getBlockAt(player.getLocation());
                System.out.println(block.getType()==Material.AIR);
                long t = System.currentTimeMillis();
                for (int i = 0; i < 100000; i++) {
                    if (!block.getType().equals(Material.AIR)) {
                        i=i+1-1;
                    }
                }
                System.out.println(System.currentTimeMillis()-t);
                t = System.currentTimeMillis();
                for (int i = 0; i < 100000; i++) {
                    if (!(block.getType()==Material.AIR)) {
                        i=i+1-1;
                    }
                }
                System.out.println(System.currentTimeMillis()-t);
            }
            else {
                String[] s = new String[3];
                s[0] = "Игроки  ";
                for (int i = 0; i < limit; i++) {
                    s[0] = s[0] + "Номер: " + i + " {";
                    for (int j = 0; j < kPlayers[i]; j++) {
                        s[0] += players[i][j].getName();
                        s[0] += " ";
                    }
                    s[0] += "} ";
                }
                s[1] = "Наблюдатели  ";
                for (int i = 0; i < limit; i++) {
                    s[1] = s[1] + "Номер: " + i + " {";
                    for (int j = 0; j < kObs[i]; j++) {
                        s[1] += observers[i][j].getName();
                        s[1] += " ";
                    }
                    s[1] += "} ";
                }
                s[2] = "isGame ";
                for (int i = 0; i < limit; i++) {
                    s[2] = s[2] + "[" + i + " , " + isGame[i] + "] ";
                }
                player.sendMessage(s);
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("structure")) {
            Player player = (Player)sender;
            if (args[0].equalsIgnoreCase("load")) {
                File file = new File("/home/container/world/generated/minecraft/structures/"+args[1]+".nbt");
                try {
                    Structure structure = plugin.getServer().getStructureManager().loadStructure(file);
                    structure.place(player.getLocation(), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("Загружено строение с именем "+args[1]);
            }
            else if (args[0].equalsIgnoreCase("save")) {
                Location loc = player.getLocation();
                loc.set(loc.getX()+1, loc.getY(), loc.getZ()+1);
                StructureManager manager = plugin.getServer().getStructureManager();
                Structure structure = manager.createStructure();
                Location dest = new Location(world, loc.getX()+Integer.parseInt(args[1]), loc.getY()+Integer.parseInt(args[2]), loc.getZ()+Integer.parseInt(args[3]));
                structure.fill(loc, dest, true);
                try {
                    manager.saveStructure(new File("/home/container/world/generated/minecraft/structures/"+args[4]+".nbt"), structure);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage("Структура с именем "+args[4]+" сохранена");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("create")) {
            Player player = (Player) sender;
            if (player.hasMetadata("creating")) {
                BuilderMode mode = new BuilderMode();
                player.setMetadata("creating", new FixedMetadataValue(plugin, mode));
                if (mode.step == -1 && args.length == 1) {
                    Inventory inv = player.getInventory();
                    inv.setItem(0, new ItemStack(Material.WOODEN_AXE));
                    player.sendMessage("Кликните этим топором по двум диаметрально противоположным углам вашей комнаты");
                    mode.room.put("name", args[0]);
                    mode.room.put("chance", 15);
                }
            }
            else {
                player.sendMessage(ChatColor.RED+"Данная команда доступна только игрокам в режиме строителя или операторам!");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("tpto")) {
            Player player = (Player)sender;
            if (player.hasMetadata("observer")) {
                tpObserver(player);
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("lobby")) {
            Player player = (Player)sender;
            flushPlayer(player);
            updatePlayers(player);
            updateObservers(player);
        }
        return false;
    }
    public static void byCreating(Block block, Player player) {
        BuilderMode mode = (BuilderMode)player.getMetadata("creating").get(0).value();
        if (mode.step==-1&&block!=null) {
            Location loc = block.getLocation();
            if (mode.kPoints < 2) {
                mode.points[mode.kPoints] = loc;
                mode.kPoints++;
                player.sendMessage(loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
            }
            if (mode.kPoints==2) {
                JSONArray size = new JSONArray();
                size.put(Math.abs(mode.points[1].getBlockX()-mode.points[0].getBlockX())+1);
                size.put(Math.abs(mode.points[1].getBlockY()-mode.points[0].getBlockY())+1);
                size.put(Math.abs(mode.points[1].getBlockZ()-mode.points[0].getBlockZ())+1);
                mode.room.put("size", size);
                mode.roomLoc = new Location(world, Math.min(mode.points[1].getBlockX(), mode.points[0].getBlockX()), Math.min(mode.points[1].getBlockY(), mode.points[0].getBlockY()), Math.min(mode.points[1].getBlockZ(), mode.points[0].getBlockZ()));
                int k = size.getInt(0)*size.getInt(1)*size.getInt(2);
                if (k<=25) {
                    mode.room.put("description", "tunnel");
                }
                else if (k>=8000) {
                    mode.room.put("description", "hall");
                }
                else {
                    mode.room.put("description", "room");
                }
                player.sendMessage("Если к вашему выходу не будет подсоединён тоннель то его надо закрыть каким-то блоком.");
                player.sendMessage("Поставьте на место выходов те блоки которыми они должны быть закрыты по вашему мнению.");
                player.sendMessage("Если вам мешают двери или люки то временно уберите их.");
                player.sendMessage("Как только сделаете это поставьте точку в чат.");
                mode.kPoints=0;
                mode.step = 1;
                player.getInventory().setItem(1, new ItemStack(Material.GLASS, 64));
            }
        }
        else if (mode.step==1&&block==null) {
            if (mode.kPoints>=2) {
                JSONArray exits = new JSONArray();
                Location loc = new Location(world, 0, 0, 0);
                int x1 = mode.roomLoc.getBlockX();
                int y1 = mode.roomLoc.getBlockY();
                int z1 = mode.roomLoc.getBlockZ();
                JSONArray size = mode.room.getJSONArray("size");
                int x2 = x1 + size.getInt(0);
                int y2 = y1 + size.getInt(1);
                int z2 = z1 + size.getInt(2);
                for (int x = x1; x < x2; x++) {
                    for (int y = y1; y < y2; y++) {
                        for (int z = z1; z < z2; z++) {
                            loc.set(x, y - 1, z);
                            Block under = world.getBlockAt(loc);
                            loc.set(x, y, z);
                            Block exit = world.getBlockAt(loc);
                            if (exit.hasMetadata("exit") && !under.hasMetadata("exit")) {
                                JSONObject exitJSON = new JSONObject();
                                JSONArray coords = new JSONArray();
                                coords.put(x - x1);
                                coords.put(y - y1);
                                coords.put(z - z1);
                                exitJSON.put("coordinates", coords);
                                exitJSON.put("block_name", exit.getMetadata("exit").get(0).asString());
                                exits.put(exitJSON);
                                loc.set(x, y + 1, z);
                                Block upper = world.getBlockAt(loc);
                                upper.removeMetadata("exit", plugin);
                                exit.removeMetadata("exit", plugin);
                            }
                        }
                    }
                }
                mode.room.put("exits", exits);
            }
            mode.kPoints=0;
            mode.step = 2;
            player.getInventory().setItem(0, new ItemStack(Material.WOODEN_AXE));
            player.sendMessage(ChatColor.GREEN+"Next mode.step:");
            player.sendMessage("Eсли у вас будут сундуки или бочки то сначала напишите в чат информацию о ней а затем кликните лкм этим топором по бочке/сундуку который вы описали");
            player.sendMessage("В качестве этого описания напишите в чат 2 сообщения: первое - это вероятность появления сундука (десятичная дробь через точку от 0 до 1)");
            player.sendMessage("Второе - это количество вещей в сундуке от 1 до 9 (1 - мало, 9 - много");
            player.sendMessage("Затем снова кликайте по сундуку или бочке и (если у вас ещё будут бочки или сундуки то повторяйте то же самое.");
            player.sendMessage("Закончив с сохранением сундуков восстановите двери и люки, затем напишите в чат точку");
        }
        else if (mode.step==2&&block!=null) {
            if (block.getType().equals(Material.CHEST)&&mode.probab!=0&&mode.saturation!=0) {
                if (!mode.room.has("chests")) {
                    JSONArray chests = new JSONArray();
                    mode.room.put("chests", chests);
                }
                JSONArray chests = mode.room.getJSONArray("chests");
                JSONObject chest = new JSONObject();
                JSONArray coords = new JSONArray();
                Location loc = block.getLocation();
                coords.put(loc.getBlockX()-mode.roomLoc.getBlockX());
                coords.put(loc.getBlockY()-mode.roomLoc.getBlockY());
                coords.put(loc.getBlockZ()-mode.roomLoc.getBlockZ());
                chest.put("coordinates", coords);
                chest.put("probability", mode.probab);
                chest.put("size", mode.saturation);
                chests.put(chest);
                mode.room.put("chests", chests);
            }
            else if (block.getType().equals(Material.BARREL)&&mode.probab!=0&&mode.saturation!=0) {
                if (!mode.room.has("barrels")) {
                    JSONArray barrels = new JSONArray();
                    mode.room.put("barrels", barrels);
                }
                JSONArray barrels = mode.room.getJSONArray("barrels");
                JSONObject chest = new JSONObject();
                JSONArray coords = new JSONArray();
                Location loc = block.getLocation();
                coords.put(loc.getBlockX()-mode.roomLoc.getBlockX());
                coords.put(loc.getBlockY()-mode.roomLoc.getBlockY());
                coords.put(loc.getBlockZ()-mode.roomLoc.getBlockZ());
                chest.put("coordinates", coords);
                chest.put("probability", mode.probab);
                chest.put("size", mode.saturation);
                barrels.put(chest);
                mode.room.put("barrels", barrels);
            }
            else {
                player.sendMessage(ChatColor.RED+"Некорректный формат данных, попробуйте снова это сундук/бочку");
            }
            mode.probab = 0;
            mode.saturation = 0;
        }
        else if (mode.step == 2) {
            Location loc = new Location(world, 0, 0, 0);
            int x1 = mode.roomLoc.getBlockX();
            int y1 = mode.roomLoc.getBlockY();
            int z1 = mode.roomLoc.getBlockZ();
            JSONArray size = mode.room.getJSONArray("size");
            int x2 = x1+size.getInt(0);
            int y2 = y1+size.getInt(1);
            int z2 = z1+size.getInt(2);
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    for (int z = z1; z < z2; z++) {
                        loc.set(x, y, z);
                        Block block1 = world.getBlockAt(loc);
                        if (block1.getType().equals(Material.CHEST)||block1.getType().equals(Material.BARREL)) {
                            block1.setType(Material.AIR);
                        }
                    }
                }
            }
            File folder = new File("/home/container/plugins/FirstPlugin/JSON");
            File[] files = folder.listFiles();
            for (File file : files) {
                if (mode.room.getString("name").equals(file.getName().substring(0, file.getName().length() - 5))) {
                    String name = "g" + (int) (Math.random() * 1000000);
                    mode.room.put("name", name);
                }
            }
            StructureManager manager = plugin.getServer().getStructureManager();
            Structure structure = manager.createStructure();
            structure.fill(mode.roomLoc, new Location(world, x2, y2, z2), true);
            try {
                manager.saveStructure(new File("/home/container/world/generated/minecraft/structures/"+mode.room.getString("name")+".nbt"), structure);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                String extra = "";
                if (!mode.room.has("exits")) {
                    extra="Lavawars";
                }
                FileWriter writer = new FileWriter("/home/container/plugins/FirstPlugin/"+extra+"Offer/"+mode.room.getString("name")+".json");
                mode.room.write(writer, 4, 0);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            player.sendMessage(ChatColor.GREEN+"Ваша комната успешно сохранена, ждите проверки администрацией и добавления её в игру");
            flushPlayer(player);
        }
    }
    public static void tpObserver(Player player) {
        boolean b = false;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < kObs[i]; j++) {
                if (player==observers[i][j]) {
                    player.teleport(players[i][0].getLocation());
                    b=true;
                }
            }
        }
        if (!b) {
            player.sendMessage("Вы не являетесь наблюдателем");
        }
    }
    public static void loadMap(Location loc, int n, boolean isTest, int gameMode) throws IOException {
        World world = loc.getWorld();
        HashMap<String, File> input = new HashMap<>();
        File folder = new File("/home/container/world/generated/minecraft/structures");
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            String s = files[i].getName().split("\\.")[0];
            input.put(s, files[i]);
        }
        LocalStructure[] structures = map[n].getStructures();
        LocalStructure[] water = map[n].water;
        System.out.println(map[n].kStr);
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        FirstPlugin.x[n] = x;
        FirstPlugin.y[n] = y;
        FirstPlugin.z[n] = z;
        if (gameMode==0) {
            if (!isTest) {
                for (int i = 0; i <= 256; i++) {
                    for (int j = 0; j <= 64; j++) {
                        for (int k = 0; k <= 256; k++) {
                            loc.set(x + i, y + j, z + k);
                            Block block = world.getBlockAt(loc);
                            if (i == 0 || i == 256 || j == 0 || j == 64 || k == 0 || k == 256) {
                                if (!(block.getType() ==Material.BEDROCK)) {
                                    block.setType(Material.BEDROCK);
                                }
                            } else {
                                if (!(block.getType() ==Material.STONE)) {
                                    block.setType(Material.STONE);
                                }
                                block.setMetadata("canBreak", new FixedMetadataValue(plugin, false));
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i <= 256; i++) {
                    for (int j = 0; j <= 64; j++) {
                        for (int k = 0; k <= 256; k++) {
                            loc.set(x + i, y + j, z + k);
                            Block block = world.getBlockAt(loc);
                            if (!(block.getType()==Material.AIR)) {
                                block.setType(Material.AIR);
                            }
                            block.setMetadata("canBreak", new FixedMetadataValue(plugin, false));
                        }
                    }
                }
            }
        }
        else if (gameMode==1) {
            int lavaLevel = map[n].lavaLevel;
            for (int i = 0; i <= 512; i++) {
                for (int j = 0; j < 128; j++) {
                    for (int k = 0; k <= 512; k++) {
                        loc.set(x+i, y+j, z+k);
                        Block block = world.getBlockAt(loc);
                        if (j==0||j==127) {
                            if (!(block.getType() ==Material.BEDROCK)) {
                                block.setType(Material.BEDROCK);
                            }
                        }
                        else if ((i==0||i==512||k==0||k==512)) {
                            if (!(block.getType() ==Material.BARRIER)) {
                                block.setType(Material.BARRIER);
                            }
                        }
                        else if (j<=lavaLevel) {
                            if (!(block.getType() ==Material.LAVA)) {
                                block.setType(Material.LAVA);
                            }
                        }
                        else {
                            if (!(block.getType() ==Material.AIR)) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i <= 256; i++) {
            for (int j = -63; j<=0; j++) {
                for (int k = 0; k <= 256; k++) {
                    loc.set(x + i, y + j, z + k);
                    Block block = FirstPlugin.world.getBlockAt(loc);
                    if (i == 0 || i == 256 || j == 0 || j == -63 || k == 0 || k == 256) {
                        if (!(block.getType() ==Material.BEDROCK)) {
                            block.setType(Material.BEDROCK);
                        }
                    } else {
                        if (!(block.getType() ==Material.WATER)) {
                            block.setType(Material.WATER);
                        }
                    }
                }
            }
        }
        StructureManager manager = plugin.getServer().getStructureManager();
        loc.set(x+arenaCoords.getBlockX(), y+arenaCoords.getBlockY(), z +arenaCoords.getBlockZ());
        Structure arena = manager.loadStructure(input.get("arena"));
        arena.place(loc, true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        //placeStructure(input.get("hall"), loc, false, false);
        for (int i = 0; i < map[n].kStr; i++) {
            if (structures[i]!=null) {
                loc.set(x+structures[i].x, y+structures[i].y, z+structures[i].z);
                Structure structure = manager.loadStructure(input.get(structures[i].name));
                structure.place(loc, true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
                if (!structures[i].isSimple) {
                    //System.out.println(structures[i].name);
                    customStr[kStr[n]][n]=structures[i];
                    structures[i].withGeneration(n);
                    kStr[n]++;
                }
            }
        }
        loc.setWorld(FirstPlugin.world);
        for (int i = 0; i < map[n].kWater; i++) {
            if (water[i]!=null) {
                loc.set(x+water[i].x, y+water[i].y, z+water[i].z);
                Structure structure = manager.loadStructure(input.get(water[i].name));
                structure.place(loc, true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
                if (!water[i].isSimple) {
                    //System.out.println(structures[i].name);
                    customStr[kStr[n]][n]=water[i];
                    water[i].withGeneration(n);
                    kStr[n]++;
                }
            }
        }
        LocalChest[] localChests = map[n].getChests();
        for (int i = 0; i < localChests.length; i++) {
            if (localChests[i]!=null) {
                loc.setWorld(world);
                if (localChests[i].y<0) {
                    loc.setWorld(FirstPlugin.world);
                }
                loc.set(x+ localChests[i].x, y+ localChests[i].y, z+ localChests[i].z);
                Block block = loc.getWorld().getBlockAt(loc);
                String chestName = "minecraft:"+ localChests[i].name;
                BlockData blockData = Bukkit.createBlockData(chestName);
                block.setBlockData(blockData);
                BlockState blockState = block.getState();
                if (localChests[i].name.equals("chest")) {
                    Chest chest = (Chest)blockState;
                    ItemStack[] itemStacks = new ItemStack[22];
                    for (int j = 0; j < localChests[i].k; j++) {
                        if (!localChests[i].items[j].isStr) {
                            //System.out.println(localChests[i].items[j].name);
                            Item current = localChests[i].items[j];
                            itemStacks[j] = new ItemStack(Material.matchMaterial(current.name));
                            itemStacks[j].setAmount(current.k);
                            if (current.cmd!=0) {
                                //System.out.println(chest.getX()+" "+chest.getY()+" "+chest.getZ());
                                ItemMeta meta = itemStacks[j].getItemMeta();
                                meta.setUnbreakable(true);
                                meta.setCustomModelData(current.cmd);
                                meta.setDisplayName(current.color+current.displayName);
                                itemStacks[j].setItemMeta(meta);
                            }
                        }
                        else {
                            itemStacks[j] = new ItemStack(Material.ENCHANTED_BOOK);
                            itemStacks[j].setAmount(1);
                            String name = localChests[i].items[j].name;
                            int value = Character.getNumericValue(name.charAt(name.length()-1));
                            name=name.substring(0, name.length()-1);
                            //System.out.println(Enchantment.getByName(name));
                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStacks[j].getItemMeta();
                            meta.addStoredEnchant(Enchantment.getByName(name), value, true);
                            itemStacks[j].setItemMeta(meta);
                        }
                    }
                    Inventory inv = chest.getBlockInventory();
                    inv.setContents(itemStacks);
                }
                if (localChests[i].name.equals("barrel")) {
                    Barrel chest = (Barrel)blockState;
                    ItemStack[] itemStacks = new ItemStack[22];
                    for (int j = 0; j < localChests[i].k; j++) {
                        itemStacks[j] = new ItemStack(Material.matchMaterial(localChests[i].items[j].name));
                        itemStacks[j].setAmount(localChests[i].items[j].k);

                    }
                    Inventory inv = chest.getInventory();
                    inv.setContents(itemStacks);
                }
            }
        }
        LocalBlock[] blocks = map[n].getBlocks();
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i]!=null) {
                if (blocks[i].type==1&&blocks[i].b) {
                    loc.setWorld(world);
                    if (blocks[i].Y<0) {
                        loc.setWorld(FirstPlugin.world);
                    }
                    loc.set(x+blocks[i].X, y+blocks[i].Y, z+blocks[i].Z);
                    Block block = loc.getWorld().getBlockAt(loc);
                    block.setBlockData(Bukkit.createBlockData(blocks[i].name));
                    if (blocks[i].sy==0) {
                        loc.setY(loc.getY()+1);
                        block = loc.getWorld().getBlockAt(loc);
                        block.setBlockData(Bukkit.createBlockData(blocks[i].name));
                    }
                }
                else if (blocks[i].type == 5) {
                    if (Math.random() * 1000 < blocks[i].sx * 100 + blocks[i].sy * 10 + blocks[i].sz) {
                        loc.setWorld(world);
                        if (blocks[i].Y<0) {
                            loc.setWorld(FirstPlugin.world);
                        }
                        loc.set(x+blocks[i].X, y+blocks[i].Y, z+blocks[i].Z);
                        Entity entity = loc.getWorld().spawnEntity(loc, EntityType.fromName(blocks[i].name));
                        entity.addScoreboardTag("only mob");
                        entity.setMetadata("dropToken", new FixedMetadataValue(plugin, true));
                    }
                }
                else if (blocks[i].type == 6) {
                    if (Math.random() * 1000 < blocks[i].sx * 100 + blocks[i].sy * 10 + blocks[i].sz) {
                        loc.setWorld(world);
                        if (blocks[i].Y<0) {
                            loc.setWorld(FirstPlugin.world);
                        }
                        loc.set(x+blocks[i].X, y+blocks[i].Y, z+blocks[i].Z);
                        Block block = loc.getWorld().getBlockAt(loc);
                        block.setBlockData(Bukkit.createBlockData(blocks[i].name));
                    }
                }
            }
        }

    }
    public static boolean isOnSpawn(Block block) {
        Location loc = block.getLocation();
        return loc.getBlockX()<spawn2.getBlockX()&&loc.getBlockX()>spawn1.getBlockX()&&loc.getBlockY()<spawn2.getBlockY()&&loc.getBlockY()>spawn1.getBlockY()&&loc.getBlockZ()<spawn2.getBlockZ()&&loc.getBlockZ()>spawn1.getBlockZ();
    }
    public static boolean isInBox(Location loc, BoundingBox box) {
        return box.contains(loc.getX(), loc.getY(), loc.getZ());
    }
}
