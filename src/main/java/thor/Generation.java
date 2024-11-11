package thor;


import json.JSONArray;
import json.JSONObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Generation {
    public static int size = 20;
    boolean loaded = false;
    World world;
    String[][] advancedStructures;
    HashMap<String, JSONObject> rooms = new HashMap<>();
    int[][] chances;
    int[] kAdv;
    int[] summs;
    int kB;
    int kStr;
    LocalChest[] localChests;
    int kChest;
    LocalStructure[] str;
    LocalBlock[] blocks;
    Item[] items;
    Item[] food;
    int kItem;
    int kFood;
    int itemSumm;
    static String inputPath;
    int foodSumm;
    int key;
    int number;
    LocalBlock[] exits;
    int kExits;
    int[] tpX;
    int[] tpY;
    int[] tpZ;
    String error = "";
    int gameMode;
    int lavaLevel = 4;
    ArrayList<Portal> portals = new ArrayList<>();
    LocalStructure[] water = new LocalStructure[500];
    int kWater = 0;
    Location arenaButton = new Location(FirstPlugin.world, FirstPlugin.arenaCoords.getBlockX()+1, FirstPlugin.arenaCoords.getBlockY()+2, FirstPlugin.arenaCoords.getBlockZ()+1);
    public Generation(int gameMode) throws IOException {
        exits = new LocalBlock[100000];
        kExits = 0;
        advancedStructures = new String[5][50];
        chances = new int[5][50];
        kAdv = new int[5];
        summs = new int[5];
        kB = 0;
        kStr = 0;
        localChests = new LocalChest[1000];
        kChest = 0;
        str = new LocalStructure[10000];
        blocks = new LocalBlock[256 * 256 * 256];
        items = new Item[300];
        food = new Item[300];
        kItem = 0;
        kFood = 0;
        itemSumm = 0;
        inputPath = "/home/container/plugins/FirstPlugin/";
        foodSumm=0;
        number=0;
        this.gameMode=gameMode;
        tpY = new int[size];
        tpX = new int[size];
        tpZ = new int[size];
        File folder = new File(inputPath+"JSON");
        if (gameMode==1) {
            folder=new File(inputPath+"Lavawars");
        }
        updateStructures(folder);
        Scanner keyFout = new Scanner(new File(inputPath+"key.txt"));
        key = Integer.parseInt(keyFout.nextLine());
        PrintStream keyFin = new PrintStream(inputPath+"key.txt");
        keyFin.println(key + 1);
        Scanner fout = new Scanner(new File(inputPath + "ballance.txt"));
        while (fout.hasNextLine()) {
            String s = fout.nextLine();
            boolean isStr = s.charAt(s.length() - 1) >= '0' && s.charAt(s.length() - 1) <= '9';
            String[] ss = s.split(" ");
            int a = Integer.parseInt(ss[0]);
            int data = -1;
            items[kItem] = new Item(isStr, Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), itemSumm, a + itemSumm, ss[3], data, null, 0, 0, null, null);
            if (ss.length>4) {
                Item current = items[kItem];
                current.cmd = Integer.parseInt(ss[4]);
                current.cooldown = Integer.parseInt(ss[5]);
                current.color = ChatColor.valueOf(ss[6].toUpperCase());
                String display = ss[7];
                for (int i = 8; i < ss.length; i++) {
                    display=display+" "+ss[i];
                }
                current.displayName=display;
                FirstPlugin.cooldownTime.put(current.name+current.cmd, current.cooldown);
            }
            itemSumm += a;
            kItem++;
        }
        Scanner fout1 = new Scanner(new File(inputPath + "barrel.txt"));
        while (fout1.hasNextLine()) {
            String s = fout1.nextLine();
            boolean isStr = s.charAt(s.length() - 1) >= '0' && s.charAt(s.length() - 1) <= '9';
            String[] ss = s.split(" ");
            int a = Integer.parseInt(ss[0]);
            int data = -1;
            food[kFood] = new Item(isStr, Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), foodSumm, a + foodSumm, ss[3], data, null, 0, 0, null, null);
            if (ss.length>4) {
                Item current = food[kFood];
                current.cmd = Integer.parseInt(ss[4]);
                current.cooldown = Integer.parseInt(ss[5]);
                current.color = ChatColor.valueOf(ss[6]);
                String display = ss[7];
                for (int i = 8; i < ss.length; i++) {
                    display=display+" "+ss[i];
                }
                current.displayName=display;
                FirstPlugin.cooldownTime.put(current.name+current.cmd, current.cooldown);
            }
            foodSumm += a;
            kFood++;
        }
        if (gameMode==0) {
            world=FirstPlugin.world;
            str[kStr] = LocalStructure.spawn(this, (int) (Math.random() * 200), (int) (Math.random() * 45), (int) (Math.random() * 200), "hall");
            kB = str[kStr].initBlocks(this, blocks, kB);
            kStr++;
            int g = str[0].exit.length;
            boolean time = true;
            for (int i = 0; i < g; i++) {
                if (str[0].exit[i].type == 1) {
                    time = true;
                    //System.out.println("Количество блоков перекрёстка: " + str[0].exit.length);
                    LocalBlock current = str[0].exit[i];
                    //.out.println(current.X+" "+current.Y+" "+current.Z);
                    current.b = false;
                    int jEnd = (int) (Math.random() * 20);
                    //System.out.println("будет структур " + jEnd);
                    boolean skip = false;
                    for (int j = 0; j < jEnd && !skip; j++) {
                        //System.out.println("Вошли во второй цикл");
                        int kEnd = -1;
                        LocalStructure ex = null;
                        boolean b = false;
                        LocalStructure currStr;
                        while (kEnd <= 0 && !skip) {
                            //System.out.println("Кординаты current: " + current.x + "," + current.y + "," + current.z);
                            if (b) {
                                //System.out.println("Вошли в подбор другого направления");
                                if (time) {
                                    currStr = str[0];
                                } else {
                                    currStr = str[kStr - 1];
                                }
                                boolean loopexit = false;
                                for (int k = 0; k < currStr.exit.length && !loopexit; k++) {
                                    if (currStr.exit[k].type == 1 && !currStr.exit[k].useToFindDist && currStr.exit[k] != current) {
                                        current = currStr.exit[k];
                                        current.useToFindDist = true;
                                        loopexit = true;
                                    }
                                }
                                if (!loopexit) {
                                    skip = true;
                                }
                            }
                            if (!skip) {
                                b = true;
                                kEnd = (int) (Math.random() * 50) + 1;
                                int type = 1000 + Math.abs(current.sx) * 100 + Math.abs(current.sy) * 10 + Math.abs(current.sz);
                                ex = LocalStructure.spawn(this, 0, 0, 0, "" + Math.abs(current.sx) + Math.abs(current.sy) + Math.abs(current.sz));
                                //System.out.println("Кординаты current: "+current.X+","+current.Y+","+current.Z);
                                //System.out.println("Кординаты port: "+ex.port.x+","+ex.port.y+","+ex.port.z+"Тип структуры: "+type+" Количество блоков структуры:"+ex.exit.length);
                                int distance = current.canPlaceOn(blocks, kB);
                                //System.out.println("Результат" + distance);
                                if (distance < 60 && kEnd > distance - 10) {
                                    kEnd = distance - 10;
                                }
                            }  //System.out.println("ПРОПУЩЕНО СТРУКТУР: " + (jEnd - j));

                        }
                        if (!skip) {
                            current.b = false;
                            if (kEnd > 0) {
                                str[kStr] = LocalStructure.spawn(this, current.X + current.sx - ex.port.x, current.Y + current.sy - ex.port.y, current.Z + current.sz - ex.port.z, ex);
                                kB = str[kStr].initBlocks(this, blocks, kB);
                                str[kStr].port.b = false;
                                //System.out.println(str[kStr].port.X+" "+str[kStr].port.Y+" "+str[kStr].port.Z);
                                kStr++;
                            }

                            for (int k = 1; k < kEnd; k++) {
                                //System.out.println(str[kStr - 1].port.X + current.sx - ex.port.x);
                                str[kStr] = LocalStructure.spawn(this, str[kStr - 1].port.X + current.sx - ex.port.x, str[kStr - 1].port.Y + current.sy - ex.port.y, str[kStr - 1].port.Z + current.sz - ex.port.z, ex);
                                kB = str[kStr].initBlocks(this, blocks, kB);
                                str[kStr].port.b = false;
                                //System.out.println(current.sx+" "+current.sy+" "+current.sz+" спавн тоннелей");
                                kStr++;
                            }
                            boolean flag = true;
                            int newcurrentIndex = 0;
                            int errorCount = 0;
                            while (flag && errorCount < 1000) {
                                ex = LocalStructure.spawn(this, 0, 0, 0, "room");
                                int w = (int) (Math.random() * (ex.kPorts - 1));
                                for (int l = 0; l < ex.exit.length; l++) {
                                    if (ex.exit[l].type == 1) {
                                        //System.out.println("Тип структуры " + ex.type);
                                        if (current.sx + ex.exit[l].sx == 0 && current.sy + ex.exit[l].sy == 0 && current.sz + ex.exit[l].sz == 0) {
                                            //System.out.println(ex.type + " Вошли в этот блок");
                                            flag = false;
                                            ex.exit[l].b = false;
                                            //System.out.println("Параметры входа "+ex.exit[l].x+" "+ex.exit[l].y+" "+ex.exit[l].z);
                                            //System.out.println("kEnd = "+kEnd+" корды порта "+str[kStr-1].port.X+" "+str[kStr-1].port.Y+" "+str[kStr-1].port.Z);
                                            //System.out.println("Параметры выхода "+current.x+" "+current.y+" "+current.z);
                                            str[kStr] = LocalStructure.spawn(this, str[kStr - 1].port.X + current.sx - ex.exit[l].x, str[kStr - 1].port.Y + current.sy - ex.exit[l].y, str[kStr - 1].port.Z + current.sz - ex.exit[l].z, ex);
                                            //System.out.println("Кординаты комнаты: " + str[kStr].x + " " + str[kStr].y + " " + str[kStr].z);
                                            if (str[kStr].canLoad(this)) {
                                                boolean loopexit2 = false;
                                                for (int m = 0; m < str[kStr].exit.length && !loopexit2; m++) {
                                                    if (str[kStr].exit[m].type == 1 && str[kStr].exit[m].sx == ex.exit[l].sx && str[kStr].exit[m].sy == ex.exit[l].sy && str[kStr].exit[m].sz == ex.exit[l].sz) {
                                                        str[kStr].exit[m].b = false;
                                                        loopexit2 = true;
                                                    }
                                                }
                                                kB = str[kStr].initBlocks(this, blocks, kB);
                                                kStr++;
                                            } else {
                                                flag = true;
                                                errorCount++;
                                            }
                                        } else {
                                            if (w == 0) {
                                                newcurrentIndex = l;
                                            }
                                            w--;
                                        }
                                    }
                                }
                            }
                            if (errorCount >= 999) {
                            /*
                            for (int qq = 0; qq < kStr; qq++) {
                                System.out.println(str[qq].name+" "+str[qq].x+" "+str[qq].y+" "+str[qq].z);
                            }
                            */
                                error = ChatColor.RED+"КАРТА ЗАГРУЖЕНА С ОШИБКОЙ 999! ПОЖАЛУЙСТА, СООБЩИТЕ ОБ ЭТОМ АДМИНИСТРАЦИИ СЕРВЕРА";
                                break;
                            }
                            time = false;
                            current = str[kStr - 1].exit[newcurrentIndex];
                        }
                    }
                }
            }
            System.out.println("Количество блоков " + kB);
            int h = kExits;
            for (int i = 0; i < h; i++) {
                if (exits[i].b) {
                    for (int j = i + 1; j < h; j++) {
                        if (exits[i].b && exits[j].b) {
                            exits[i].canLink(this, exits[j]);
                        }
                    }
                }
            }
        }
        else if (gameMode==1) {
            world = FirstPlugin.nether;
            int k = (int) (40+Math.random()*50);
            int c = 0;
            while (kStr<k&&c<100000) {
                c++;
                int x = (int) (Math.random()*510+1);
                int y = (int) (Math.random()*115+lavaLevel*2);
                int z = (int) (Math.random()*510+1);
                str[kStr]=LocalStructure.spawn(this, x, y, z, "hall");
                if (str[kStr].canLoad(this)) {
                    kB = str[kStr].initBlocks(this, blocks, kB);
                    kStr++;
                }
            }
            k=k+(int) (100+Math.random()*50);
            while (kStr<k&&c<100000) {
                c++;
                int x = (int) (Math.random()*510+1);
                int y = lavaLevel;
                int z = (int) (Math.random()*510+1);
                str[kStr]=LocalStructure.spawn(this, x, y, z, "room");
                if (str[kStr].canLoad(this)) {
                    kB = str[kStr].initBlocks(this, blocks, kB);
                    kStr++;
                }
            }
            System.out.println("C = "+c);
            if (c>=99999) {
                error="Слишком много неудачных попыток генерации островов";
            }
        }
        for (int i = 0; i < kStr; i++) {
            str[i].end(this);
        }
        int k = portals.size();
        System.out.println("k = "+k);
        int c = 0;
        folder=new File(inputPath+"Water");
        updateStructures(folder);
        while (kWater<k&&c<100000) {
            c++;
            int x = (int) (Math.random()*254+1);
            int y = (int) (Math.random()*126-62);
            int z = (int) (Math.random()*254+1);
            water[kWater]=LocalStructure.spawn(this, x, y, z, "hall");
            if (water[kWater].canLoadWater(this)) {
                kB = water[kWater].initBlocks(this, blocks, kB);
                water[kWater].end(this);
                portals.get(kWater).tpto=((Portal)(water[kWater])).tpfrom;
                ((Portal)(water[kWater])).tpto=portals.get(kWater).tpfrom;
                System.out.println(portals.get(kWater).tpfrom);
                System.out.println(((Portal)(water[kWater])).tpfrom);
                kWater++;
            }
        }
        k=k+(int) (5+Math.random()*5);
        while (kWater<k&&c<100000) {
            c++;
            int x = (int) (Math.random()*254+1);
            int y = (int) (Math.random()*126-62);
            int z = (int) (Math.random()*254+1);
            water[kWater]=LocalStructure.spawn(this, x, y, z, "room");
            if (water[kWater].canLoadWater(this)) {
                kB = water[kWater].initBlocks(this, blocks, kB);
                kWater++;
            }
        }
        System.out.println("C = "+c);
        if (c>=99999) {
            error="Слишком много неудачных попыток генерации островов";
        }
        System.out.println("Количество всех блоков: " + kB);
        for (int i = 0; i < kB; i++) {
            if (blocks[i].type == 2 || blocks[i].type == 3) {
                //System.out.println("Вошли в создание сундука " + i);
                if (blocks[i].sy == 0)
                    blocks[i].sy = 10;
                if ((int) (10 * Math.random()) < blocks[i].sy) {
                    localChests[kChest] = new LocalChest(blocks[i], items, kItem, food, kFood, itemSumm, foodSumm);
                    kChest++;
                }
            }
        }
        for (int i = 1; i <= size; i++) {
            boolean b = false;
            k = 0;
            int j = (int) (Math.random() * kB);
            while (!b) {
                boolean loopexit = false;
                for (j = j; j < kB && !loopexit; j++) {
                    if (blocks[j].type == 4 && blocks[j].b) {
                        b = true;
                        loopexit = true;
                        blocks[j].b = false;
                        tpX[i - 1] = blocks[j].X;
                        tpY[i - 1] = blocks[j].Y;
                        tpZ[i - 1] = blocks[j].Z;
                    }
                }
                j = 0;
                k++;
                if (k >= 3) {
                    b = true;
                }
            }
        }
    }
    public LocalStructure spawn(JSONObject room) throws IOException {
        String name = room.getString("name");
        return switch (name) {
            case "teleport", "netherTeleport" -> new Teleport(room, kStr);
            case "shop" -> new Shop(room);
            case "levitation" -> new Levitation(room);
            case "little" -> new Little(room);
            case "portal", "waterPortal", "netherPortal" -> new Portal(room);
            default -> new LocalStructure(room);
        };
    }
    public LocalStructure[] getStructures() {
        return str;
    }
    public LocalChest[] getChests() {
        return localChests;
    }
    public LocalBlock[] getBlocks() {
        return blocks;
    }
    public void onTap(PlayerInteractEvent event, int n) {
        Block block = event.getClickedBlock();
        if (block.getType()== Material.OAK_BUTTON&&FirstPlugin.x[n]+arenaButton.getBlockX()==block.getX()&&FirstPlugin.y[n]+arenaButton.getBlockY()==block.getY()&&FirstPlugin.z[n]+arenaButton.getBlockZ()==block.getZ()) {
            Player player = event.getPlayer();
            if (player.hasMetadata("arena")&&FirstPlugin.backCount[n]>0) {
                Location loc = (Location) player.getMetadata("arena").get(0).value();
                player.teleport(loc);
                player.removeMetadata("arena", FirstPlugin.plugin);
            }
        }
    }
    public void onBlockBreak(BlockBreakEvent event, int n) {
        Block block = event.getBlock();
        if (block.getType()== Material.OAK_BUTTON&&FirstPlugin.x[n]+arenaButton.getBlockX()==block.getX()&&FirstPlugin.y[n]+arenaButton.getBlockY()==block.getY()&&FirstPlugin.z[n]+arenaButton.getBlockZ()==block.getZ()) {
            event.setCancelled(true);
        }
    }
    public void arenaButton(int n) {
        Block block = world.getBlockAt(new Location(world, FirstPlugin.x[n]+arenaButton.getBlockX(), FirstPlugin.y[n]+arenaButton.getBlockY(), FirstPlugin.z[n]+arenaButton.getBlockZ()));
        if (!(block.getType()==Material.OAK_BUTTON)) {
            block.setType(Material.OAK_BUTTON);
        }
    }
    public void updateStructures(File folder) {
        File[] files = folder.listFiles();
        advancedStructures = new String[5][50];
        chances = new int[5][50];
        kAdv = new int[5];
        summs = new int[5];
        rooms = new HashMap<>();
        for (int q = 0; q < files.length; q++) {
            Scanner input = null;
            try {
                input = new Scanner(files[q]);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            StringBuilder jsonString = new StringBuilder();
            while (input.hasNextLine()) {
                jsonString.append(input.nextLine()).append("\n");
            }
            JSONObject room = new JSONObject(jsonString.toString());
            int advIndex = 0;
            if (room.getString("description").equals("room")) {
                advIndex=0;
            }
            else if (room.getString("description").equals("hall")) {
                advIndex=1;
            }
            else {
                JSONArray exitsInfo = room.getJSONArray("exits");
                JSONObject portInfo = exitsInfo.getJSONObject(0);
                String name = room.getString("name");
                if (name.charAt(name.length()-1)=='X') {
                    advIndex=2;
                }
                else if (name.charAt(name.length()-1)=='Y') {
                    advIndex=3;
                }
                else {
                    advIndex=4;
                }
            }

            chances[advIndex][kAdv[advIndex]]=room.getInt("chance");
            advancedStructures[advIndex][kAdv[advIndex]]=room.getString("name");
            rooms.put(room.getString("name"), room);
            kAdv[advIndex]++;
            summs[advIndex]+=room.getInt("chance");
        }
    }
}
