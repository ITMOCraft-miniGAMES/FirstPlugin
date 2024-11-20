package thor;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import json.JSONArray;
import json.JSONObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class FirstPlugin extends JavaPlugin {
    public static int gameTime = 1800;
    public static int limit = 6;
    public static int tLimit = 10;
    public static int time = 30;
    public static Location arenaCoords;
    public static LocalStructure[][] customStr = new LocalStructure[500][limit];
    public static int[] kStr = new int[limit];
    public static int[] x = new int[limit];
    public static int[] y = new int[limit];
    public static int[] z = new int[limit];
    public static int[] tx = new int[tLimit];
    public static int[] ty = new int[tLimit];
    public static int[] tz = new int[tLimit];
    public static ArrayList<Player>[] players = new ArrayList[limit];
    public static ArrayList<Player>[] tPlayers = new ArrayList[tLimit];
    public static boolean[] isGame = new boolean[limit];
    public static int[] gameStep = new int[tLimit];
    public static boolean[] canBreak = new boolean[limit];
    public static int[] backCount = new int[limit];
    public static int[] tBackCount = new int[tLimit];
    public static Generation[] map = new Generation[limit];
    public static World world;
    public static World nether;
    public static World end;
    public static Scoreboard[] scoreboard = new Scoreboard[limit];
    public static Scoreboard[] tScoreboard = new Scoreboard[tLimit];
    public static Objective[] objectives = new Objective[limit];
    public static Objective[] tObjectives = new Objective[tLimit];
    public static ArrayList<Player>[] observers = new ArrayList[limit];
    public static ArrayList<Player>[] tObservers = new ArrayList[tLimit];
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
    public static HashMap<String, String> translation = new HashMap<>();
    Logger logger;
    public static ArrayList<String> russians = new ArrayList<>();
    public static HashMap<String, String> itemInfo = new HashMap<>();
    public static HashMap<String, CharacterClass> classesInfo = new HashMap<>();
    public static int BEGINBLOCKBREAK = 1500;
    public static Scoreboard fake;

    public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (damager.getType()==EntityType.PLAYER) {
            Player player = (Player) damager;
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                int data = 0;
                if (meta.hasCustomModelData()) {
                    data = meta.getCustomModelData();
                }
                if (data==4&&itemStack.getType() == Material.FISHING_ROD) {
                    event.setCancelled(true);
                    LightningStrike lightning = (LightningStrike) world.spawnEntity(entity.getLocation(), EntityType.LIGHTNING);
                    lightning.setMetadata("owner", new FixedMetadataValue(plugin, damager));
                }
                else if (data==1&&itemStack.getType()==Material.IRON_SWORD) {
                    if (!player.hasMetadata("lightsaber")) {
                        player.setMetadata("lightsaber", new FixedMetadataValue(plugin, 2));
                    }
                    int v = player.getMetadata("lightsaber").get(0).asInt();
                    if (v==0) {
                        player.setMetadata("lightsaber", new FixedMetadataValue(plugin, 2));
                        Location loc = player.getLocation();
                        Location tLoc = entity.getLocation();
                        Vector vector = new Vector(tLoc.getX()-loc.getX(), Math.sqrt((loc.getX()-tLoc.getX())*(loc.getX()-tLoc.getX())+(loc.getZ()-tLoc.getZ())*(loc.getZ()-tLoc.getZ()))/2, tLoc.getZ()-loc.getZ());
                        vector.multiply(7.0/vector.length());
                        entity.setVelocity(vector);
                    }
                    else {
                        player.setMetadata("lightsaber", new FixedMetadataValue(plugin, v-1));
                    }
                }
            }
        }
        else if (damager.getType()==EntityType.LIGHTNING) {
            if (damager.hasMetadata("owner")&&damager.getMetadata("owner").get(0).value()==entity) {
                event.setCancelled(true);
            }
        }
    }
    public void preloadMap(int n) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                System.out.println(n);
                int x = n * 10000;
                int y = 0;
                int z = n * 10000;
                Location loc = new Location(world, 0, 0, 0);
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
                try {
                    PrintStream out = new PrintStream("/home/container/plugins/FirstPlugin/n.txt");
                    out.println(n);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (n<10) {
                    preloadMap(n + 1);
                }
            }
        }, 200);
    }
    @Override
    public void onEnable() {
        for (int i = 0; i < limit; i++) {
            players[i] = new ArrayList<>(10);
            observers[i]=new ArrayList<>();
        }
        for (int i = 0; i < tLimit; i++) {
            tPlayers[i]=new ArrayList<>();
            tObservers[i]=new ArrayList<>();
        }
        plugin=this;
        fake = Bukkit.getScoreboardManager().getNewScoreboard();
        world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        nether=Bukkit.createWorld(new WorldCreator("lavawars"));
        nether.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        end=Bukkit.getWorld("world_the_end");
        spawn1 = new Location(world, -60, -10, -20);
        spawn2 = new Location(world, 60, 180, 130);
        respawnLoc = new Location(world, -13, 81, 8);
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        logger = getLogger();
        arenaCoords = new Location(world, 512, 0, 512);
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
        cooldownTime.put("fishing_rod1", 5);
        cooldownTime.put("fishing_rod2", 10);
        cooldownTime.put("fishing_rod3", 6);
        cooldownTime.put("iron_sword1", 5);
        try {
            Scanner in = new Scanner(new File("/home/container/plugins/FirstPlugin/russians.txt"));
            while (in.hasNextLine()) {
                russians.add(in.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Scanner in = new Scanner(new File("/home/container/plugins/FirstPlugin/translation.txt"));
            while (in.hasNextLine()) {
                translation.put(in.nextLine(), in.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Scanner in = new Scanner(new File("/home/container/plugins/FirstPlugin/itemInfo.txt"));
            while (in.hasNextLine()) {
                String s1 = in.nextLine();
                String s2 = in.nextLine();
                String s3 = in.nextLine();
                itemInfo.put(s1, s2);
                translation.put(s2, s3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Scanner categorysStream = new Scanner(new File("/home/container/plugins/FirstPlugin/SkyRPG/item_categorys.json"));
            Scanner classesStream = new Scanner(new File("/home/container/plugins/FirstPlugin/SkyRPG/classes.json"));
            StringBuilder categorysString = new StringBuilder();
            while (categorysStream.hasNextLine()) {
                categorysString.append(categorysStream.nextLine());
            }
            StringBuilder classesString = new StringBuilder();
            while (classesStream.hasNextLine()) {
                classesString.append(classesStream.nextLine());
            }
            JSONObject categorys = new JSONObject(categorysString.toString());
            JSONArray category = categorys.getJSONArray("categorys");
            HashMap<String, JSONArray> categoryInfo = new HashMap<>();
            HashMap<String, JSONArray> categoryCustom = new HashMap<>();
            for (int i = 0; i < category.length(); i++) {
                JSONObject currentCategory = category.getJSONObject(i);
                if (currentCategory.has("items")) {
                    categoryInfo.put(currentCategory.getString("name"), currentCategory.getJSONArray("items"));
                }
                if (currentCategory.has("custom_items")) {
                    categoryCustom.put(currentCategory.getString("name"), currentCategory.getJSONArray("custom_items"));
                }
            }
            JSONObject classJSON = new JSONObject(classesString.toString());
            JSONArray classes = classJSON.getJSONArray("classes");
            for (int i = 0; i < classes.length(); i++) {
                JSONObject currentClass = classes.getJSONObject(i);
                CharacterClass characterClass = new CharacterClass(currentClass.getString("name"));
                JSONArray exceptions = currentClass.getJSONArray("exceptions");
                for (int j = 0; j < exceptions.length(); j++) {
                    String exceptionName = exceptions.getString(j);
                    characterClass.removedCategorys.add(exceptionName);
                    if (categoryInfo.containsKey(exceptionName)) {
                        JSONArray exceptionInfo = categoryInfo.get(exceptionName);
                        for (int k = 0; k < exceptionInfo.length(); k++) {
                            characterClass.exceptions.add(exceptionInfo.getString(k) + "0");
                        }
                    }
                    if (categoryCustom.containsKey(exceptionName)) {
                        JSONArray exceptionInfo = categoryCustom.get(exceptionName);
                        for (int k = 0; k < exceptionInfo.length(); k++) {
                            JSONObject customItem = exceptionInfo.getJSONObject(k);
                            characterClass.exceptions.add(customItem.getString("item")+customItem.getInt("custom_model"));
                        }
                    }
                }
                classesInfo.put(characterClass.name, characterClass);
                characterClass.setDescription();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Location loc = new Location(world, 39, 2, 32);
            Block block = world.getBlockAt(loc);
            while (block.getType()==Material.BAMBOO_WALL_SIGN) {
                Sign sign = (Sign) world.getBlockAt(loc).getState();
                String[] lines = sign.getLines();
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].length()>1&&lines[i].charAt(0)>='a'&&lines[i].charAt(0)<='z') {
                        if (classesInfo.containsKey(lines[i])) {
                            CharacterClass characterClass = classesInfo.get(lines[i]);
                            ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
                            Location bookLoc = new Location(world, loc.getBlockX()-1, 0, loc.getBlockZ());
                            Block block1 = world.getBlockAt(bookLoc);
                            block1.setType(Material.LECTERN);
                            Lectern lectern = (Lectern)block1.getState();
                            BookMeta meta = (BookMeta) book.getItemMeta();
                            meta.setAuthor("SkyRPG");
                            meta.setTitle("Info about class");
                            meta.addPage(characterClass.description);
                            meta.addPage(characterClass.russianDescription);
                            book.setItemMeta(meta);
                            org.bukkit.block.data.type.Lectern lecternData = (org.bukkit.block.data.type.Lectern) lectern.getBlockData();
                            lecternData.setFacing(BlockFace.SOUTH);
                            lectern.getInventory().addItem(book);
                            lectern.setPage(0);
                        }
                        break;
                    }
                }

                loc.setZ(loc.getBlockZ()+2);
                block = world.getBlockAt(loc);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PrintStream in = new PrintStream("/home/container/plugins/ChunkLoader/data.yml");
            in.println("chunks-list:");
            for (int i = -3; i <=3; i++) {
                int r = (i*10000)/16;
                int l = (i*10000+256)/16;
                for (int j = r; j <= l; j++) {
                    for (int k = r; k <= l; k++) {
                        in.println("- " + j + ";" + k + ";world");
                    }
                }
            }
            for (int i = -3; i <= -1; i++) {
                int r = (i*10000)/16;
                int l = (i*10000+512)/16;
                for (int j = r; j <= l; j++) {
                    for (int k = r; k <= l; k++) {
                        in.println("- " + j + ";" + k + ";lavawars");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        /*
        int n = 1;
        try {
            Scanner in = new Scanner(new File("/home/container/plugins/FirstPlugin/n.txt"));
            n=in.nextInt();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        preloadMap(n+1);
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
        int x1 =0;
        int y1 = 200;
        int z1 = 1000;
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
        for (int i = 0; i <= 256; i++) {
            for (int j = 0; j <= 64; j++) {
                for (int k = 0; k <= 256; k++) {
                    loc.set(x1 + i, y1 + j, z1 + k);
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
         */
        /*
        StructureManager manager = plugin.getServer().getStructureManager();
        Structure structure = manager.createStructure();
        structure.fill(new Location(world, 30, 0, 30), new Location(world, 42, 6, 60), true);
        for (int i = 0; i < tLimit; i++) {
            Location loc = new Location(world, 100+100*i, 0, 100+100*i);
            structure.place(loc, true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
         */
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                List<Entity> entities = world.getEntities();
                for (Entity entity : entities) {
                    if (entity.getType() == EntityType.DROPPED_ITEM) {
                        entity.remove();
                    }
                }
            }
        }, 20);
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    iterPlayers(getServer().getOnlinePlayers());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
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
                                                skeleton.setShouldBurnInDay(false);
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
                    //System.out.println(s);
                    if (cooldownTime.containsKey(s)) {
                        short d = (short) ((double)max/cooldownTime.get(s));
                        if (itemStack.getDurability()-d>0) {
                            itemStack.setDurability((short) (itemStack.getDurability() - d));
                            //System.out.println(itemStack.getDurability());
                        }
                        else {
                            itemStack.setDurability((short) 0);
                            cooldown.remove(i);
                            i--;
                        }
                    }
                    else {
                        //plugin.getServer().sendMessage(Component.text(ChatColor.RED+"Error 999 by using the wand, please tell it to administration!"));
                        cooldown.remove(i);
                        i--;
                    }
                }
                for (int i = 0; i < backCount.length; i++) {
                    if (map[i]!=null) {
                        map[i].arenaButton(i);
                    }
                    if (backCount[i]>0&&!isGame[i]) {
                        for (int j = 0; j < players[i].size(); j++) {
                            players[i].get(j).getInventory().clear();
                        }
                        backCount[i]--;
                        objectives[i].setDisplayName("The game starts in "+backCount[i]);
                        if (backCount[i] == time - 10&&!map[i].loaded) {
                            Location location = new Location(world, (i + 1) * 10000, 0, (i + 1) * 10000);
                            if (i>=limit/2) {
                                location = new Location(nether, -(i-limit/2 + 1) * 10000, 0, -(i-limit/2 + 1) * 10000);
                            }
                            try {
                                loadMap(location, i, false, 2*i/limit);
                                map[i].loaded=true;
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
                            for (int j = 0; j < players[i].size(); j++) {
                                Player player = players[i].get(j);
                                players[i].get(j).setMetadata("inGame", new FixedMetadataValue(plugin, true));
                                Score score = objectives[i].getScore(player);
                                score.setScore(0);
                                Location tpLoc;
                                if (map[i].tpX[j]!=0) {
                                    tpLoc = new Location(currentWorld, map[i].tpX[j] + x[i], map[i].tpY[j]+y[i], map[i].tpZ[j] + z[i]);
                                }
                                else {
                                    tpLoc = new Location(currentWorld, map[i].tpX[0], map[i].tpY[0], map[i].tpZ[0]);
                                }
                                player.setFoodLevel(20);
                                player.setFireTicks(0);
                                player.teleport(tpLoc);
                                player.sendMessage(ChatColor.YELLOW+" "+ChatColor.BOLD+translate("To find out detailed information about an item in your hand, use the command! /info", player).toUpperCase());
                                giveItems(player);
                            }
                            killItems(i, currentWorld);
                        }
                    }
                    else if (backCount[i]>0) {
                        backCount[i]--;
                        String s = backCount[i]/60+":"+backCount[i]%60+" Kills";
                        objectives[i].setDisplayName(s);
                        if (backCount[i]==BEGINBLOCKBREAK) {
                            canBreak[i]=true;
                            for (int j = 0; j < players[i].size(); j++) {
                                players[i].get(j).setMetadata("inGame", new FixedMetadataValue(plugin, false));
                                if (i<limit/2) {
                                    players[i].get(j).sendTitle(new Title(translate("Now you can break stone", players[i].get(j))));
                                }
                                else {
                                    players[i].get(j).sendTitle(new Title("Всем выдано яйцо, указывающее на игроков"));
                                }
                                Inventory inv = players[i].get(j).getInventory();
                                ItemStack itemStack = new ItemStack(Material.EGG);
                                ItemMeta meta =  itemStack.getItemMeta();
                                meta.setCustomModelData(100);
                                meta.setDisplayName(ChatColor.GREEN+translate("Throw to find out the direction to nearest player", players[i].get(j)));
                                itemStack.setItemMeta(meta);
                                itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
                                HashMap<Integer, ItemStack> canAdd = inv.addItem(itemStack);
                                if (!canAdd.isEmpty()) {
                                    org.bukkit.entity.Item item = (org.bukkit.entity.Item) players[i].get(j).getWorld().spawnEntity(players[i].get(j).getLocation(), EntityType.DROPPED_ITEM);
                                    item.setItemStack(itemStack);
                                }
                            }
                        }
                        if (backCount[i]==0) {
                            Location loc = new Location(map[i].world, FirstPlugin.x[i]+FirstPlugin.arenaCoords.getBlockX()+Math.random()*25+1, FirstPlugin.y[i]+FirstPlugin.arenaCoords.getBlockY()+1, FirstPlugin.z[i]+FirstPlugin.arenaCoords.getBlockZ()+Math.random()*25+1);
                            for (int j = 0; j < players[i].size(); j++) {
                                players[i].get(j).teleport(loc);
                            }
                        }
                        if (backCount[i]>=0&&backCount[i]<=10) {
                            for (int j = 0; j < players[i].size(); j++) {
                                players[i].get(j).sendTitle(new Title(backCount[i]+""));
                            }
                        }
                    }
                }
                for (int i = 0; i < tLimit; i++) {
                    if (gameStep[i]==0&&tBackCount[i]>0) {
                        tBackCount[i]--;
                        tObjectives[i].setDisplayName("The game starts in "+tBackCount[i]);
                        if (tBackCount[i]==time-10) {
                            tLoadMap(new Location(world, 5000+i*10000, 0, 5000+i*10000), i, false);

                        }
                        else if (tBackCount[i]==0) {
                            gameStep[i]=1;
                            Team team1 = tScoreboard[i].getTeam(i+"1");
                            Team team2 = tScoreboard[i].getTeam(i+"2");
                            tBackCount[i] = 30*(team1.getSize()+team2.getSize());
                        }
                    }
                    else if (gameStep[i]==1) {
                        if (tBackCount[i]%30==0) {
                            int m = tBackCount[i]/30;
                            if (m<tPlayers[i].size()) {
                                Player chosen = tPlayers[i].get(m+1);
                                chosen.setMetadata("chosen", new FixedMetadataValue(plugin, true));
                                Team team = tScoreboard[i].registerNewTeam(i+""+m);
                                Team baseTeam = tScoreboard[i].getPlayerTeam(chosen);
                                team.setColor(baseTeam.getColor());
                                if (chosen.hasMetadata("class")) {
                                    sendMessage(ChatColor.GREEN+""+ChatColor.BOLD+chosen.getName()+" is now "+chosen.getMetadata("class").get(0).asString()+"!", tPlayers[i]);
                                    baseTeam.setSuffix(chosen.getMetadata("class").get(0).asString());
                                }
                                else {
                                    sendMessage(ChatColor.RED + chosen.getName() + " did not have time to select a character class!", tPlayers[i]);
                                }
                            }
                            if (m-1>=0) {
                                Player player = tPlayers[i].get(m);
                                sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName() + " is choosing the class!", tPlayers[i]);
                                player.sendTitle(new Title("Choose a class now!"));
                                tObjectives[i].setDisplayName(player.getName() + " is choosing " + (tBackCount[i] % 30));
                            }
                        }
                        if (tBackCount[i]==0) {
                            for (int j = 0; j < tPlayers[i].size(); j++) {
                                Player player = tPlayers[i].get(j);
                                player.sendMessage(ChatColor.GREEN + translate("Your team has 5 minutes to build a defensive structure to protect your spawn anchor and attack the enemy one.", player));
                                try {
                                    Score score = tObjectives[i].getScore(player);
                                    score.setScore(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ItemStack[] itemStacks = null;
                                if (!player.hasMetadata("class")) {
                                    int k = 0;
                                    Location loc = new Location(world, 0, 0, 0);
                                    for (int x = 32; x < 80; x += 2) {
                                        loc.set(i, 1, 39);
                                        Block block = world.getBlockAt(loc);
                                        if (block.getType() == Material.CHEST) {
                                            k++;
                                        }
                                    }
                                    k = (int) (Math.random() * k);
                                    Chest chest = (Chest) world.getBlockAt(new Location(world, 39, 1, 32 + 2 * k)).getState();
                                    Sign sign = (Sign) world.getBlockAt(new Location(world, 39, 2, 32 + 2 * k)).getState();
                                    String str = "";
                                    String[] lines = sign.getLines();
                                    for (int l = 0; l < lines.length; l++) {
                                        if (lines[l].length() > 1) {
                                            str = lines[l];
                                            break;
                                        }
                                    }
                                    itemStacks = chest.getBlockInventory().getContents();
                                    player.setMetadata("class", new FixedMetadataValue(plugin, str));
                                    player.sendMessage(ChatColor.RED + translate("You have not chosen a character class! Class chosen randomly!", player));
                                } else {
                                    itemStacks = (ItemStack[]) player.getMetadata("classItems").get(0).value();
                                }
                                Inventory inv = player.getInventory();
                                for (int q = 0; q < itemStacks.length; q++) {
                                    if (!(itemStacks[q] == null) && !(itemStacks[q].getType() == Material.AIR)) {
                                        if (q <= 3) {
                                            inv.setItem(36 + q, itemStacks[q]);
                                        } else {
                                            inv.addItem(itemStacks[q]);
                                        }
                                    }
                                }
                                Chest chest = (Chest) world.getBlockAt(new Location(world, 34, 1, 31)).getState();
                                inv = chest.getBlockInventory();
                                itemStacks = inv.getContents();
                                Inventory playerInv = player.getInventory();
                                for (int q = 0; q < itemStacks.length; q++) {
                                    if (itemStacks[q] != null) {
                                        playerInv.addItem(itemStacks[q]);
                                    }
                                }
                                player.sendTitle(new Title("Your class: " + translate(player.getMetadata("class").get(0).asString(), player)));
                                player.setMetadata("inGame", new FixedMetadataValue(plugin, true));
                                respawning(player);
                            }
                            gameStep[i] = 2;
                            tBackCount[i] = gameTime;
                            tKillItems(i, 20);
                        }
                        tBackCount[i]--;
                    }
                    else if (tBackCount[i]>0) {
                        tBackCount[i]--;
                        String s = tBackCount[i]/60+":"+tBackCount[i]%60+" Kills";
                        if (tBackCount[i]>1500) {
                            s = (tBackCount[i]/60-25)+":"+tBackCount[i]%60+" Preparing";
                        }
                        tObjectives[i].setDisplayName(s);
                        if (tBackCount[i]==1500) {
                            updateMap(i);
                            for (int j = 0; j < tPlayers[i].size(); j++) {
                                tPlayers[i].get(j).sendTitle(new Title(translate("Time to fight!", tPlayers[i].get(j))));
                            }
                        }
                        else if (tBackCount[i]==300) {
                            for (int j = 0; j < tPlayers[i].size(); j++) {
                                Player player = tPlayers[i].get(j);
                                ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);
                                itemStack.addEnchantment(Enchantment.DIG_SPEED, 3);
                                giveItems(player, itemStack);
                                player.sendTitle(new Title(translate("You have got a pickaxe!", player)));
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);
    }
    public static void sendMessage(String message, ArrayList<Player> players) {
        for (Player player: players) {
            player.sendMessage(message);
        }
    }
    public static boolean canDamage(Player player) {
        World world1 = player.getLocation().getWorld();
        return !(!player.hasMetadata("inGame")&&(world1==world||world1==nether));
    }
    public static boolean canUseItem(Player player, ItemStack itemStack) {
        if (player.hasMetadata("class")&&itemStack.getType()!=Material.AIR) {
            String className = player.getMetadata("class").get(0).asString();
            if (classesInfo.containsKey(className)) {
                CharacterClass characterClass = classesInfo.get(className);
                String itemName = itemStack.getType().toString().toLowerCase();
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.hasCustomModelData()) {
                    itemName+=meta.getCustomModelData();

                }
                else {
                    itemName+="0";
                }
                if (characterClass.containsItem(itemName)) {
                    player.sendMessage(ChatColor.RED+translate(characterClass.name+" can't use this item!", player));
                    player.closeInventory();
                    return false;
                }
            }
        }
        return true;
    }

    public static void killItems(int n, World world) {
        BoundingBox box = new BoundingBox(x[n], y[n], z[n], x[n]+1024, y[n]+128, z[n]+1024);
        Collection<Entity> entities = world.getNearbyEntities(box);
        for (Entity entity : entities) {
            if (entity.getType()==EntityType.DROPPED_ITEM) {
                entity.remove();
            }
        }
    }
    public static void tKillItems(int n, long delay) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                BoundingBox box = new BoundingBox(tx[n], ty[n], tz[n], tx[n]+180, ty[n]+100, tz[n]+140);
                Collection<Entity> entities = world.getNearbyEntities(box);
                System.out.println("tKillItems "+entities.size());
                System.out.println(box);
                for (Entity entity : entities) {
                    System.out.print(entity.getType() + " ");
                    entity.remove();
                }
                System.out.println("endl");
            }
        }, delay);
    }
    public static void tLoadMap(Location loc, int n, boolean isTest) {
        tx[n]=loc.getBlockX();
        ty[n]=loc.getBlockY();
        tz[n] = loc.getBlockZ();
        World world = loc.getWorld();
        tKillItems(n, 0);
        for (int i = 0; i <= 180; i++) {
            for (int j = 0; j <= 100; j++) {
                for (int k = 0; k <= 140; k++) {
                    loc.set(tx[n]+i, ty[n]+j, tz[n]+k);
                    Block block = world.getBlockAt(loc);
                    if (i==0||i==80||i==100||i==180||k==0||k==140||j==100) {
                        if (block.getType()!=Material.BARRIER) {
                            block.setType(Material.BARRIER);
                        }
                    }
                    else if (j==0&&k>=50&&k<=90&&(i>=50&&i<=70||i>=110&&i<=130)) {
                        if (block.getType()!=Material.BEDROCK) {
                            block.setType(Material.BEDROCK);
                        }
                    }
                    else if (j==1&&k==70&&(i==60||i==120)) {
                        block.setType(Material.RESPAWN_ANCHOR);
                        if (i==60) {
                            block.setMetadata("anchor", new FixedMetadataValue(plugin, n+"1"));
                        }
                        else {
                            block.setMetadata("anchor", new FixedMetadataValue(plugin, n+"2"));
                        }
                        RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
                        anchor.setCharges(anchor.getMaximumCharges());
                        block.setBlockData(anchor);
                    }
                    else {
                        if (block.getType()!=Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
    public static void updateMap(int n) {
        Location loc = new Location(world, 0, 0, 0);
        for (int j = 0; j < 100; j++) {
            for (int k = 1; k < 140; k++) {
                loc.set(tx[n]+80, ty[n]+j, tz[n]+k);
                Block block = world.getBlockAt(loc);
                block.setType(Material.AIR);
                loc.set(tx[n]+100, ty[n]+j, tz[n]+k);
                block = world.getBlockAt(loc);
                block.setType(Material.AIR);
            }
        }
        for (int i = 71; i < 110; i++) {
            for (int k = 50; k <=90; k++) {
                if (k==51||k==89||k>=69&&k<=71) {
                    loc.set(tx[n]+i, 0, tz[n]+k);
                    Block block = world.getBlockAt(loc);
                    block.setType(Material.BEDROCK);
                }
            }
        }
    }
    public static String translate(String s, Player player) {
        if (translation.containsKey(s)&&(player==null||russians.contains(player.getName()))) {
            return translation.get(s);
        }
        else {
            return s;
        }
    }
    public static void tAutoBegin(Player player) {
        int maxPlayers = 0;
        int maxN = -1;
        for (int i = 0; i < 10; i++) {
            if (gameStep[i]==0&&tPlayers[i].size()>maxPlayers) {
                maxPlayers=tPlayers[i].size();
                maxN=i;
            }
        }
        if (maxN!=-1) {
            tptoTower(player, maxN);
        }
        else {
            int minPlayers = 6;
            int minN = -1;
            for (int i = 0; i < 10; i++) {
                if (gameStep[i]!=0&&tPlayers[i].size()<minPlayers) {
                    minPlayers=tPlayers[i].size();
                    minN=i;
                }
            }
            if (minN!=-1) {
                tptoTower(player, minN);
            }
            else {
                for (int i = 0; i < 10; i++) {
                    if (gameStep[i]==0) {
                        tptoTower(player, i);
                        break;
                    }
                }
            }
        }
    }

    public static void tptoTower(Player player, int n) {
        player.sendMessage(translate("Use the command /lobby to come back to the lobby", player));
        if (gameStep[n]==0) {
            if (tPlayers[n].size()<10) {
                tPlayers[n].add(player);
                player.getInventory().clear();
                player.setGameMode(GameMode.ADVENTURE);
                player.setMetadata("tower", new FixedMetadataValue(plugin, n));
                if (tPlayers[n].size()==2) {
                    tBackCount[n]=time+1;
                    for (int i = 0; i < tPlayers[n].size(); i++) {
                        tPlayers[n].get(i).sendMessage("The game starts in "+time+" seconds");
                        tPlayers[n].get(i).setGameMode(GameMode.ADVENTURE);
                    }
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    tScoreboard[n]=manager.getNewScoreboard();
                    tObjectives[n]=tScoreboard[n].registerNewObjective("game"+n, "dummy");
                    tObjectives[n].setDisplaySlot(DisplaySlot.SIDEBAR);
                    tObjectives[n].setDisplayName("Start in "+time);
                    tPlayers[n].get(0).setScoreboard(tScoreboard[n]);
                    tPlayers[n].get(1).setScoreboard(tScoreboard[n]);
                    Team team1 = tScoreboard[n].registerNewTeam(n+"1");
                    Team team2 = tScoreboard[n].registerNewTeam(n+"2");
                    team1.addPlayer(tPlayers[n].get(0));
                    team1.setAllowFriendlyFire(false);
                    team1.color(NamedTextColor.RED);
                    team2.addPlayer(tPlayers[n].get(1));
                    team2.setAllowFriendlyFire(false);
                    team2.color(NamedTextColor.GREEN);
                }
                else if (tPlayers[n].size()>2) {
                    player.setScoreboard(tScoreboard[n]);
                    Team team1 = tScoreboard[n].getTeam(n+"1");
                    Team team2 = tScoreboard[n].getTeam(n+"2");
                    if (team2.getSize()<team1.getSize()) {
                        team2.addPlayer(player);
                        player.sendMessage(ChatColor.GREEN+translate("You are on the green team", player));
                    }
                    else {
                        team1.addPlayer(player);
                        player.sendMessage(ChatColor.RED+translate("You are on the red team", player));
                    }
                    player.sendMessage(ChatColor.YELLOW+translate("Select character class", player));
                }
                else {
                    player.sendMessage(translate("Wait for other players", player));
                    player.sendMessage(ChatColor.YELLOW+translate("Select character class", player));
                }
            }
        }
        else {
            tObservers[n].add(player);
            player.setMetadata("tObserver", new FixedMetadataValue(plugin, n));
            player.sendMessage(translate("Wait for the game to end. There are currently survivors ", player)+FirstPlugin.tPlayers[n].size());
            player.sendMessage(translate("Use the command /tpto to teleport to some player", player));
            player.setMetadata("observer", new FixedMetadataValue(plugin, true));
            player.setGameMode(GameMode.SPECTATOR);
        }
        Location loc = new Location(world, 105+100*n, 2, 105+100*n);
        player.teleport(loc);
    }
    public static void respawning(Player player) {
        if (!player.hasMetadata("tower")) {
            flushPlayer(player);
        }
        else {
            int n = player.getMetadata("tower").get(0).asInt();
            Team team1 = tScoreboard[n].getTeam(n + "1");
            Team team = tScoreboard[n].getPlayerTeam(player);
            player.setGameMode(GameMode.SURVIVAL);
            if (team.equals(team1)) {
                player.teleport(new Location(world, tx[n] + 60, getHeight(tx[n] + 60, tz[n] + 70), tz[n] + 70));
            } else {
                player.teleport(new Location(world, tx[n] + 120, getHeight(tx[n] + 120, tz[n] + 70), tz[n] + 70));
            }
        }
    }
    public static int getHeight(int x, int z) {
        Location loc = new Location(world, x, 0, z);
        for (int i = 2; i < 98; i++) {
            loc.setY(i);
            Block block = world.getBlockAt(loc);
            loc.setY(i+1);
            Block upper = world.getBlockAt(loc);
            if (block.getType()==Material.AIR&&upper.getType()==Material.AIR) {
                return i;
            }
        }
        return 98;
    }
    public static void autoBegin(Player player, int gameMode) {
        int maxPlayers = -1;
        int maxN = -1;
        int l = limit*gameMode/2;
        int r = l+limit/2;
        for (int i = l; i < r; i++) {
            if (!isGame[i]&&players[i].size()>maxPlayers) {
                maxPlayers=players[i].size();
                maxN=i;
            }
        }
        if (maxN>-1) {
            tptoCatacombs(player, maxN);
        }
        else {
            player.sendMessage(ChatColor.RED+""+ChatColor.BOLD+"Пока что все игры заняты, попробуйте снова позже");
        }
    }
    public static void autoObserve(Player player, int gameMode) {
        int maxPlayers = -1;
        int maxN = -1;
        int l = limit*gameMode/2;
        int r = l+limit/2;
        for (int i = l; i < r; i++) {
            if (isGame[i]&&players[i].size()>maxPlayers) {
                maxPlayers=players[i].size();
                maxN=i;
            }
        }
        if (maxN>-1) {
            tptoCatacombs(player, maxN);
        }
        else {
            player.sendMessage(ChatColor.RED+""+ChatColor.BOLD+"Сейчас никто не играет. Не за кем заблюдать!");
        }
    }
    public static void giveItems(Player player) {
        player.clearActivePotionEffects();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 2400, 255));
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 36100, 1));
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(1, new ItemStack(Material.GREEN_WOOL, 16));
    }
    public static void tptoCatacombs(Player player, int n) {
        player.sendMessage(translate("Use the command /lobby to come back to the lobby", player));
        player.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"Используйте команду /expand время_в_секундах чтобы предложить игрокам увеличить время ожидания!");
        if (!isGame[n]) {
            if (players[n].size()<20) {
                players[n].add(player);
                if (players[n].size()>2&&backCount[n]<time+1) {
                    backCount[n]=time+1;
                }
                Inventory inv = player.getInventory();
                inv.clear();
                player.setMetadata("game", new FixedMetadataValue(plugin, n));
                if (players[n].size()==2) {
                    backCount[n]=time+1;
                    for (int i = 0; i < players[n].size(); i++) {
                        players[n].get(i).sendMessage("The game starts in "+time+" seconds");
                        players[n].get(i).setGameMode(GameMode.ADVENTURE);
                    }
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    scoreboard[n]=manager.getNewScoreboard();
                    objectives[n]=scoreboard[n].registerNewObjective("game"+n, "dummy");
                    objectives[n].setDisplaySlot(DisplaySlot.SIDEBAR);
                    objectives[n].setDisplayName("Start in "+time);
                    players[n].get(0).setScoreboard(scoreboard[n]);
                    players[n].get(1).setScoreboard(scoreboard[n]);
                    boolean b = false;
                    while (!b) {
                        try {
                            map[n] = new Generation(2 * n / limit);
                            b=true;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!map[n].error.equals("")) {
                        plugin.getServer().sendMessage(Component.text(map[n].error));
                    }
                }
                else if (players[n].size()>2) {
                    player.setScoreboard(scoreboard[n]);
                }
                else {
                    player.sendMessage(translate("Wait for other players", player));
                }
            }
        }
        else {
            observers[n].add(player);
            player.setScoreboard(scoreboard[n]);
            player.setMetadata("observer", new FixedMetadataValue(plugin, n));
            player.sendMessage(translate("Use the command /tpto to teleport to some player", player));
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
            sign.setLine(0, ChatColor.BLUE + "The game is on");
        } else {
            if (backCount[n] == 0) {
                sign.setLine(0, ChatColor.BLUE + "Waiting for players");
            } else {
                sign.setLine(0, ChatColor.BLUE + "Game is about to begin");
            }
        }
        sign.setLine(1, "Players: " + players[n].size() + "/10");
        sign.setLine(2, "Observers: " + observers[n].size());
        sign.update();
    }
    public static void flushPlayer(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();
        ItemStack itemStack = new ItemStack(Material.RAW_IRON);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+translate("Click to play Catacombs/Labirint/Tunnels", player));
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.addItem(itemStack);
        itemStack = new ItemStack(Material.MAGMA_CREAM);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+translate("Click to play Lavawars", player));
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.addItem(itemStack);

        itemStack = new ItemStack(Material.BLUE_DYE);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"Наблюдать за игрой в катакомбы");
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.addItem(itemStack);
        if (player.isOp()) {
            itemStack = new ItemStack(Material.RED_DYE);
            meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Играть в тнтран");
            meta.setCustomModelData(1);
            itemStack.setItemMeta(meta);
            itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            inv.addItem(itemStack);

            itemStack = new ItemStack(Material.AMETHYST_SHARD);
            meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Играть в UHC");
            meta.setCustomModelData(1);
            itemStack.setItemMeta(meta);
            itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            inv.addItem(itemStack);
        }

        itemStack = new ItemStack(Material.YELLOW_DYE);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"Наблюдать за лававарсом");
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.addItem(itemStack);

        itemStack = new ItemStack(Material.RAW_COPPER);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+translate("Click to change language", player));
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.setItem(8, itemStack);
        /*
        itemStack = new ItemStack(Material.RAW_GOLD);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+translate("Click to play sky RPG (alpha testing)", player));
        meta.setCustomModelData(1);
        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        inv.addItem(itemStack);
         */
        player.removeMetadata("inGame", plugin);
        player.removeMetadata("observer", plugin);
        player.removeMetadata("tower", plugin);
        player.removeMetadata("class", plugin);
        player.removeMetadata("expand", plugin);
        if (player.hasMetadata("creating")) {
            player.removeMetadata("creating", plugin);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.clearActivePotionEffects();
        player.teleport(respawnLoc);
    }
    public static void classChoice(Location loc, Player player) {
        if (player.hasMetadata("chosen")) {
            player.sendMessage(ChatColor.RED+translate("You have already chosen the character class!", player));
            return;
        }
        boolean f = false;
        int z = loc.getBlockZ();
        int x = loc.getBlockX();
        if (loc.getBlockY()==3) {
            for (int i = 0; i < tLimit; i++) {
                if (x==109+100*i&&z>=100+100*i&&z<=150+100*i) {
                    f=true;
                    break;
                }
            }
        }
        if (f) {
            defClassItems(loc, player);
        }
    }
    public static void defClassItems(Location loc, Player player) {
        loc.setY(loc.getY()-1);
        Sign sign = (Sign) world.getBlockAt(loc).getState();
        String[] lines = sign.getLines();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length()>1&&lines[i].charAt(0)>='a'&&lines[i].charAt(0)<='z') {
                player.setMetadata("class", new FixedMetadataValue(plugin, lines[i]));
                loc.setY(loc.getY()-1);
                Chest chest = (Chest) world.getBlockAt(loc).getState();
                Inventory inv = chest.getBlockInventory();
                player.setMetadata("classItems", new FixedMetadataValue(plugin, inv.getContents()));
                player.sendMessage("selected class: "+lines[i]);
                break;
            }
        }
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
        Player winner = players[n].get(0);
        winner.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 200, 255));
        winner.sendTitle(new Title(ChatColor.GREEN+translate("You won!", winner)));
        plugin.getServer().sendMessage(Component.text(ChatColor.BLUE+"Игрок "+winner.getName()+" победил в игре!"));
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                flushPlayer(winner);
                winner.removeMetadata("game", plugin);
                for (Player player: observers[n]) {
                    updateObservers(player);
                }
                observers[n].clear();
                players[n].clear();
                isGame[n]=false;
                backCount[n]=0;
                objectives[n].unregister();
                updateSign(n);
                for (int i = 0; i < observers[n].size(); i++) {
                    try {
                        tptoCatacombs(observers[n].get(i), n);
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
            if (material.equals(Material.SNOWBALL)) {
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
                else if (data==3) {
                    long time = System.currentTimeMillis();
                    if (!player.hasMetadata("crossbowCD")) {
                        player.setMetadata("crossbowCD", new FixedMetadataValue(plugin, 5000));
                        player.setMetadata("crossbowTime", new FixedMetadataValue(plugin, 0L));
                    }
                    long cd = player.getMetadata("crossbowCD").get(0).asLong();
                    if (time-player.getMetadata("crossbowTime").get(0).asLong()>=cd) {
                        if (cd == 5000) {
                            player.sendMessage(ChatColor.YELLOW+"Стойте на месте чтобы ускорить стрельбу!");
                        }
                        player.setMetadata("crossbowCD", new FixedMetadataValue(plugin, cd/2));
                        player.setMetadata("crossbowTime", new FixedMetadataValue(plugin, time));
                        player.launchProjectile(Arrow.class);
                    }
                    event.setCancelled(true);
                }
                else if (data==4) {
                    pr.setGravity(false);
                    pr.setMetadata("tpto", new FixedMetadataValue(plugin, player));
                    pr.setMetadata("custom", new FixedMetadataValue(plugin, material));
                    pr.setMetadata("customData", new FixedMetadataValue(plugin, data));
                }
                else if (data==5) {
                    pr.setMetadata("custom", new FixedMetadataValue(plugin, material));
                    pr.setMetadata("customData", new FixedMetadataValue(plugin, data));
                    pr.setMetadata("owner", new FixedMetadataValue(plugin, player));
                }
            }
            else if (material==Material.EGG) {
                if (data==100) {
                    int n = player.getMetadata("game").get(0).asInt();
                    double minD = 10000000;
                    Location locP = player.getLocation();
                    Player targetP = player;
                    for (int i = 0; i< players[n].size(); i++) {
                        Location locT = players[n].get(i).getLocation();
                        double d = (locT.getX()-locP.getX())*(locT.getX()-locP.getX()) + (locT.getY()-locP.getY())*(locT.getY()-locP.getY()) + (locT.getZ()-locP.getZ())*(locT.getZ()-locP.getZ());
                        if (d<minD&&player!=players[n].get(i)) {
                            minD=d;
                            targetP=players[n].get(i);
                        }
                    }
                    player.sendMessage(ChatColor.GREEN+translate(" direction to ", player)+targetP.getName());
                    Location target = targetP.getLocation();
                    locP.setY(locP.getY()+1);
                    minD=Math.sqrt(minD);
                    double x0 = locP.getX()+5*(target.getX()-locP.getX())/minD;
                    double y0 = locP.getY()+5*(target.getY()-locP.getY())/minD;
                    double z0 = locP.getZ()+5*(target.getZ()-locP.getZ())/minD;
                    double dx = (x0-locP.getX())/20;
                    double dy = (y0-locP.getY())/20;
                    double dz = (z0-locP.getZ())/20;
                    for (int i = 0; i < 20; i++) {
                        Location location = new Location(player.getWorld(), locP.getX()+i*dx, locP.getY()+i*dy, locP.getZ()+i*dz);
                        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 1);
                    }
                    event.setCancelled(true);
                }
            }
    }
    public static void onHit(ProjectileHitEvent event) {
        Projectile pr = event.getEntity();
        Material material = (Material) pr.getMetadata("custom").get(0).value();
        Location loc = pr.getLocation();
        World world = loc.getWorld();
        int data = pr.getMetadata("customData").get(0).asInt();
        if (material ==Material.SNOWBALL) {
            if (data==1) {
                spells[0].add(loc);
                times[0].add(10);
                owners[0].add((Entity) pr.getMetadata("owner").get(0).value());
            }
            else if (data==4) {
                if (event.getHitEntity()!=null) {
                    Entity entity = event.getHitEntity();
                    entity.teleport(((Entity) (pr.getMetadata("tpto").get(0).value())).getLocation());
                }
            }
            else if (data==5) {
                Skeleton skeleton = (Skeleton) world.spawnEntity(pr.getLocation(), EntityType.SKELETON);
                skeleton.setMetadata("bomber", new FixedMetadataValue(plugin, pr.getMetadata("owner").get(0).value()));
                skeleton.setShouldBurnInDay(false);
            }
        }
        if (pr.getType()==EntityType.TRIDENT) {
            Entity entity = event.getHitEntity();
            if (pr.hasMetadata("thor")) {
                Player owner = (Player) pr.getMetadata("thor").get(0).value();
                ItemStack itemStack = (ItemStack) pr.getMetadata("itemStack").get(0).value();
                event.setCancelled(true);
                if (entity!=null&&entity!=owner) {
                    world.spawnEntity(entity.getLocation(), EntityType.LIGHTNING);
                }
                else if (entity==null) {
                    world.spawnEntity(pr.getLocation(), EntityType.LIGHTNING);
                    if (!pr.hasMetadata("reflectThor")) {
                        System.out.println("reflecting");
                        Location loc1 = pr.getLocation();
                        Vector direction = loc1.getDirection();
                        direction.multiply(-1);
                        loc1.setDirection(direction);
                        Vector asset = direction.multiply(0.5/direction.length());
                        loc1.set(loc1.getX()+asset.getX(), loc1.getY()+asset.getY(), loc1.getZ()+asset.getZ());
                        pr.remove();
                        Trident trident = (Trident) world.spawnEntity(loc1, EntityType.TRIDENT);
                        trident.setGravity(false);
                        trident.setVelocity(pr.getVelocity().multiply(-1));
                        trident.setMetadata("reflectThor", new FixedMetadataValue(plugin, true));
                        trident.setMetadata("thor", new FixedMetadataValue(plugin, pr.getMetadata("thor").get(0).value()));
                        trident.setMetadata("custom", new FixedMetadataValue(plugin, pr.getMetadata("custom").get(0).value()));
                        trident.setMetadata("customData", new FixedMetadataValue(plugin, pr.getMetadata("customData").get(0).asInt()));
                        trident.setMetadata("itemStack", new FixedMetadataValue(plugin, pr.getMetadata("itemStack").get(0).value()));
                    }
                    else {
                        owner.setMetadata("reflectThor", new FixedMetadataValue(plugin, itemStack));
                        pr.remove();
                        giveItems(owner, itemStack);
                    }
                }
                else {
                    giveItems(owner, itemStack);
                    pr.remove();
                }
            }
        }
    }
    public static void giveItems(Player player, ItemStack... itemStacks) {
        Inventory inv = player.getInventory();
        HashMap<Integer, ItemStack> items  = inv.addItem(itemStacks);
        if (!items.isEmpty()) {
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                org.bukkit.entity.Item item = (org.bukkit.entity.Item) player.getWorld().spawnEntity(player.getLocation(), EntityType.DROPPED_ITEM);
                item.setItemStack(entry.getValue());
            }
        }
    }
    public static void updatePlayers(Player quited) {
        if (quited.hasMetadata("game")) {
            int n = quited.getMetadata("game").get(0).asInt();
            if (players[n].contains(quited)) {
                players[n].remove(quited);
                quited.removeMetadata("game", plugin);
                try {
                    scoreboard[n].resetScores(quited);
                    quited.setScoreboard(fake);
                }
                catch (Exception ignored) {

                }
                if (players[n].size()==1) {
                    gameEnd(n);
                }
            }
            else {
                System.out.println("!!!! PLAYER NOT FOUND");
            }
        }
        if (quited.hasMetadata("tower")) {
            int n = quited.getMetadata("tower").get(0).asInt();
            if (tPlayers[n].contains(quited)) {
                tPlayers[n].remove(quited);
                quited.removeMetadata("tower", plugin);
                try {
                    Team team = tScoreboard[n].getPlayerTeam(quited);
                    team.removePlayer(quited);
                    tScoreboard[n].resetScores(quited);
                }
                catch (Exception ignored) {

                }
            }
        }
    }
    public static void updateObservers(Player quited) {
        if (quited.hasMetadata("observer")) {
            int n = quited.getMetadata("observer").get(0).asInt();
            observers[n].remove(quited);
            try {
                scoreboard[n].resetScores(quited);
                quited.setScoreboard(fake);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            quited.removeMetadata("observer", plugin);
        }
        if (quited.hasMetadata("tObserver")) {
            int n = quited.getMetadata("tObserver").get(0).asInt();
            tObservers[n].remove(quited);
            quited.removeMetadata("tObserver", plugin);
        }
    }
    public static boolean towerEnd(Player player, Block block) {
        boolean b = false;
        if (!(player.getGameMode() ==GameMode.SPECTATOR) &&player.hasMetadata("tower")&&block.hasMetadata("anchor")) {
            String name = block.getMetadata("anchor").get(0).asString();
            int n = player.getMetadata("tower").get(0).asInt();
            Team team = tScoreboard[n].getPlayerTeam(player);
            if (team.getName().equals(name)) {
                b=true;
            }
            else {
                win(team, n);
            }
        }
        return b;
    }
    public static void win(Team team, int n) {
        for (int i = 0; i < tPlayers[n].size(); i++) {
            Player player = tPlayers[n].get(i);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 200, 255));
            if (team.equals(tScoreboard[n].getPlayerTeam(player))) {
                player.sendTitle(new Title(ChatColor.GREEN + translate("You won!", player)));
            }
            else {
                player.sendTitle(new Title(ChatColor.RED+translate("You lost!", player)));
            }
        }
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < tPlayers[n].size(); i++) {
                    flushPlayer(tPlayers[n].get(i));
                }
                tPlayers[n].clear();
                gameStep[n]=0;
                tBackCount[n]=0;
                tObjectives[n].unregister();
                System.out.println("Завершили игру");
                for (int i = 0; i < tObservers[n].size(); i++) {
                    flushPlayer(tObservers[n].get(i));
                }
                tObservers[n].clear();
            }
        }, 200L);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
                Inventory inv = player.getInventory();
                ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setCustomModelData(1);
                meta.setDisplayName(ChatColor.YELLOW+"Fireball Wand");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.FISHING_ROD);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(2);
                meta.setDisplayName(ChatColor.RED+"Dragon Wand");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.FISHING_ROD);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(3);
                meta.setDisplayName(ChatColor.YELLOW+"Wither Wand");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.FISHING_ROD);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(4);
                meta.setDisplayName(ChatColor.RED+"Mjolnir");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.SNOWBALL, 16);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(1);
                meta.setDisplayName(ChatColor.BLUE+"Graveyard Spell");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.SNOWBALL, 16);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(2);
                meta.setDisplayName(ChatColor.GOLD+"Monk Ability");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.SNOWBALL);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(3);
                meta.setDisplayName(ChatColor.YELLOW+"Little Prince Crossbow");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.SNOWBALL, 16);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(4);
                meta.setDisplayName(ChatColor.BLUE+"Teleport");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.SNOWBALL, 16);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(5);
                meta.setDisplayName(ChatColor.YELLOW+"Bomber Skeleton");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

                itemStack = new ItemStack(Material.IRON_SWORD);
                meta = itemStack.getItemMeta();
                meta.setCustomModelData(1);
                meta.setDisplayName(ChatColor.RED+"Lightsaber");
                itemStack.setItemMeta(meta);
                inv.addItem(itemStack);

            }
            else if (args[0].equalsIgnoreCase("time")) {

            }
            else {
                String[] s = new String[3];
                s[0] = "Игроки  ";
                for (int i = 0; i < limit; i++) {
                    s[0] = s[0] + "Номер: " + i + " {";
                    for (int j = 0; j < players[i].size(); j++) {
                        s[0] += players[i].get(j).getName();
                        s[0] += " ";
                    }
                    s[0] += "} ";
                }
                s[1] = "Наблюдатели  ";
                for (int i = 0; i < limit; i++) {
                    s[1] = s[1] + "Номер: " + i + " {";
                    for (int j = 0; j < observers[i].size(); j++) {
                        s[1] += observers[i].get(j).getName();
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
                StructureRotation rotation = StructureRotation.NONE;
                if (args.length==3&&args[2].equals("rotate")) {
                    rotation=StructureRotation.CLOCKWISE_90;
                }
                try {
                    Structure structure = plugin.getServer().getStructureManager().loadStructure(file);
                    structure.place(player.getLocation(), true, rotation, Mirror.NONE, 0, 1, new Random());
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
            if (player.hasMetadata("tObserver")) {
                tpObserver(player);
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("expand")) {
            Player player = (Player)sender;
            if (player.hasMetadata("game")&&args.length==1) {
                int n = player.getMetadata("game").get(0).asInt();
                int value = Integer.parseInt(args[0]);
                if (value<=backCount[n]) {
                    player.sendMessage(ChatColor.RED+"Время ожидания можно только увеличить!");
                    return true;
                }
                if (value>300) {
                    player.sendMessage(ChatColor.GOLD+"Вы ввели слишком большое число. Конвертировали в 300");
                    value=300;
                }
                if (!isGame[n]&&backCount[n]>0) {
                    int newTime = 301;
                    boolean update = true;
                    for (Player gamer: players[n]) {
                        if (gamer.hasMetadata("expand")&&gamer!=player) {
                            int t = gamer.getMetadata("expand").get(0).asInt();
                            if (t<newTime) {
                                newTime=t;
                            }
                        }
                        else if (gamer!=player){
                            update=false;
                        }
                    }
                    if (update) {
                        backCount[n]=Math.min(newTime, value);
                        for (Player gamer: players[n]) {
                            gamer.removeMetadata("expand", plugin);
                            gamer.sendMessage(ChatColor.GREEN+"Время ожидание обновлено!");
                        }
                    }
                    else {
                        player.setMetadata("expand", new FixedMetadataValue(plugin, value));
                        if (newTime==301) {
                            sendMessage(ChatColor.YELLOW+"Игрок "+player.getName()+" предлагает увеличить время ожидания до "+value, players[n]);
                        }
                        else {
                            sendMessage(ChatColor.YELLOW+"Игрок "+player.getName()+" согласен увеличить время ожидания на "+value, players[n]);
                        }
                    }
                }
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("lobby")) {
            Player player = (Player)sender;
            updatePlayers(player);
            updateObservers(player);
            flushPlayer(player);
        }
        else if (cmd.getName().equalsIgnoreCase("info")) {
            Player player = (Player)sender;
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.getType() != Material.AIR && itemStack.getItemMeta().hasCustomModelData()) {
                int data = itemStack.getItemMeta().getCustomModelData();
                String key = itemStack.getType().toString().toLowerCase()+data;
                player.sendMessage(ChatColor.AQUA+translate(itemInfo.get(key), player));
            }
            else {
                player.sendMessage(translate("This item doesn't have info", player));
            }
        }
        return false;
    }
    public static void iterPlayers(Collection<? extends Player> playerCollection) {
        for (Player player: playerCollection) {
            Inventory inv = player.getInventory();
            ItemStack[] itemStacks = inv.getContents();
            for (int i = 0; i < itemStacks.length; i++) {
                if (itemStacks[i]!=null) {
                    tryToAddCoolDown(itemStacks[i]);
                }
            }
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.getType()==Material.FISHING_ROD&&itemStack.getItemMeta().hasCustomModelData()&&itemStack.getItemMeta().getCustomModelData()==4) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 1));
            }
        }
    }
    public static void tryToAddCoolDown(ItemStack itemStack) {
        if (itemStack.getDurability()!=0&&itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasCustomModelData()&&!cooldown.contains(itemStack)) {
                String name = itemStack.getType().name().toLowerCase()+meta.getCustomModelData();
                if (cooldownTime.containsKey(name)) {
                    cooldown.add(itemStack);
                }
            }
        }
    }
    public static void onFishing(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState()== PlayerFishEvent.State.FISHING) {
            player = event.getPlayer();
            ItemStack itemStack = player.getItemInHand();
            ItemMeta meta = itemStack.getItemMeta();
            int data = 0;
            if (meta.hasCustomModelData()) {
                data = meta.getCustomModelData();
            }
            Material material = itemStack.getType();
            short max = material.getMaxDurability();
            World world = player.getWorld();
            FishHook pr = event.getHook();
            event.setCancelled(true);
            if (itemStack.getDurability() == 0) {
                if (material.equals(Material.FISHING_ROD)) {
                    Location loc = new Location(world, pr.getX(), pr.getY(), pr.getZ());
                    loc.setDirection(player.getLocation().getDirection());
                    if (data == 1) {
                        SmallFireball fireball = (SmallFireball) world.spawnEntity(loc, EntityType.SMALL_FIREBALL);
                        fireball.setShooter(pr.getShooter());
                        fireball.setGravity(false);
                        fireball.setMetadata("explode", new FixedMetadataValue(plugin, 3));
                        cooldown.add(itemStack);
                        itemStack.setDurability((short) (max - 1));
                    } else if (data == 2) {
                        DragonFireball fireball = (DragonFireball) world.spawnEntity(loc, EntityType.DRAGON_FIREBALL);
                        fireball.setShooter(pr.getShooter());
                        fireball.setGravity(false);
                        cooldown.add(itemStack);
                        itemStack.setDurability((short) (max - 1));
                    } else if (data == 3) {
                        WitherSkull skull = (WitherSkull) world.spawnEntity(loc, EntityType.WITHER_SKULL);
                        skull.setShooter(pr.getShooter());
                        skull.setGravity(false);
                        skull.setMetadata("explode", new FixedMetadataValue(plugin, 3));
                        cooldown.add(itemStack);
                        itemStack.setDurability((short) (max - 1));
                    } else if (data == 4) {
                        Trident trident = player.launchProjectile(Trident.class);
                        trident.setMetadata("thor", new FixedMetadataValue(plugin, player));
                        trident.setGravity(false);
                        trident.setMetadata("custom", new FixedMetadataValue(plugin, material));
                        trident.setMetadata("customData", new FixedMetadataValue(plugin, data));
                        trident.setMetadata("itemStack", new FixedMetadataValue(plugin, itemStack));
                        player.getInventory().remove(itemStack);
                        player.removeMetadata("reflectThor", plugin);
                        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                        Player finalPlayer = player;
                        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if (!finalPlayer.hasMetadata("reflectThor")) {
                                    giveItems(finalPlayer, itemStack);
                                }
                            }
                        }, 100);
                    }
                }
            }
            else {
                player.sendMessage(ChatColor.RED+translate("This wand is rechargeable", player));
            }
        }
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
            if (!player.isOp()) {
                flushPlayer(player);
            }
        }
    }
    public static void tpObserver(Player player) {
        if (player.hasMetadata("observer")) {
            int n = player.getMetadata("observer").get(0).asInt();
            if (!player.hasMetadata("tptoObs")) {
                player.setMetadata("tptoObs", new FixedMetadataValue(plugin, -1));
            }
            int k = player.getMetadata("tptoObs").get(0).asInt()+1;
            if (k>=players[n].size()) {
                k=0;
            }
            player.teleport(players[n].get(k));
        }
        if (player.hasMetadata("tObserver")) {
            int n = player.getMetadata("tObserver").get(0).asInt();
            if (!player.hasMetadata("tptoObs")) {
                player.setMetadata("tptoObs", new FixedMetadataValue(plugin, -1));
            }
            int k = player.getMetadata("tptoObs").get(0).asInt()+1;
            if (k>=tPlayers[n].size()) {
                k=0;
            }
            player.teleport(tPlayers[n].get(k));
        }
    }
    public static String displayName(String displayName, Player player) {
        String name = displayName.substring(2);
        boolean isRussian = name.charAt(0)>='А'&&name.charAt(0)<='Я'||name.charAt(0)>='а'&&name.charAt(0)<='я';
        if (isRussian!=russians.contains(player.getName())) {
            if (isRussian&&translation.containsValue(name)) {
                for (Map.Entry<String, String> entry : translation.entrySet()) {
                    if (entry.getValue().equals(name)) {
                        name=entry.getKey();
                        break;
                    }
                }
            }
            else if (!isRussian&&translation.containsKey(name)) {
                name=translation.get(name);
            }
        }
        return displayName.substring(0, 2)+name;
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
                /*
                if (structures[i].y<0) {
                    for (int x1 = structures[i].x+x; x1<=structures[i].x+x+structures[i].dx; x1++) {
                        for (int y1 = structures[i].y+y; y1<=structures[i].y+y+structures[i].dy; y1++) {
                            for (int z1 = structures[i].z+z; z1<=structures[i].z+z+structures[i].dz; z1++) {
                                loc.set(x1, y1, z1);
                                Block block = FirstPlugin.world.getBlockAt(loc);
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
                */
                loc.set(x+structures[i].x, y+structures[i].y, z+structures[i].z);
                int v = 0;
                for (int j = 10; j > 0; j--) {
                    if (input.containsKey(structures[i].name+j)) {
                        v=j;
                        break;
                    }
                }
                String name = structures[i].name;
                if (v>0) {
                    int rand = (int)(Math.random()*(v+1));
                    if (rand>0) {
                        name=name+rand;
                    }
                }
                if (!input.containsKey(name)) {
                    name=structures[i].name;
                }
                Structure structure = manager.loadStructure(input.get(name));
                structure.place(loc, true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
                if (structures[i].y<0) {
                    for (int x1 = structures[i].x+x; x1<=structures[i].x+x+structures[i].dx; x1++) {
                        for (int y1 = structures[i].y+y; y1<=structures[i].y+y+structures[i].dy; y1++) {
                            for (int z1 = structures[i].z+z; z1<=structures[i].z+z+structures[i].dz; z1++) {
                                loc.set(x1, y1, z1);
                                Block block = FirstPlugin.world.getBlockAt(loc);
                                try {
                                    if (block.getBlockData().getClass().isAssignableFrom(Waterlogged.class)) {
                                        Waterlogged waterlogged = (Waterlogged) block.getBlockData();
                                        waterlogged.setWaterlogged(false);
                                        block.setBlockData(waterlogged);
                                    }
                                }
                                catch (Exception e) {

                                }
                            }
                        }
                    }
                }
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
                if (localChests[i].type==2) {
                    Container container = (Container)blockState;
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
                    Inventory inv = container.getInventory();
                    inv.setContents(itemStacks);
                }
                if (localChests[i].type==3) {
                    Container container = (Container)blockState;
                    ItemStack[] itemStacks = new ItemStack[22];
                    for (int j = 0; j < localChests[i].k; j++) {
                        itemStacks[j] = new ItemStack(Material.matchMaterial(localChests[i].items[j].name));
                        itemStacks[j].setAmount(localChests[i].items[j].k);

                    }
                    Inventory inv = container.getInventory();
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
                        entity.setCustomName("only mob");
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
