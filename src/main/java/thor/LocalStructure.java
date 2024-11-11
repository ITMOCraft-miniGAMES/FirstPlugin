package thor;

import json.JSONArray;
import json.JSONObject;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalStructure {
    public int x;
    public int y;
    public int z;
    public boolean isSimple = true;
    public String[] command;
    public String[] map;
    public int dx;
    public int dy;
    public int dz;
    public int type;
    public int kPorts;
    public String name;
    public LocalBlock[] exit;
    public boolean rotate;
    public LocalBlock port;
    public static LocalStructure spawn(Generation map, int x, int y, int z, LocalStructure ex) throws IOException {

        LocalStructure res = map.spawn(map.rooms.get(ex.name));
        res.x=x;
        res.y=y;
        res.z=z;
        res.exitCoords();
        if (res.name.equals("little")) {
            ((Little) res).number= map.number;
            map.number++;
        }
        else if (res.name.equals("teleport")) {
            ((Teleport)res).index= map.kStr;
        }
        return res;
    }
    public static LocalStructure spawn(Generation map, int x, int y, int z, String descr) throws IOException {
        int advI = 0;
        if (descr.equals("hall")) {
            advI = 1;
        }
        else if (descr.equals("room")) {
            advI=0;
        }
        else {
            if (Character.getNumericValue(descr.charAt(0))==1) {
                advI = 2;
            }
            else if (Character.getNumericValue(descr.charAt(2))==1) {
                advI = 4;
            }
            else {
                advI = 3;
            }
        }
        int number = (int)( Math.random()* map.summs[advI]);
        int s = 0;
        LocalStructure res = null;
        for (int i = 0; i < map.kAdv[advI]; i++) {
            s+= map.chances[advI][i];
            if (s>number) {
                String name = map.advancedStructures[advI][i];
                res = map.spawn(map.rooms.get(name));
                res.x=x;
                res.y=y;
                res.z=z;
                res.exitCoords();
                break;
            }
        }
        return res;
    }
    public void exitCoords() {
        for (int i = 0; i < exit.length; i++) {
            exit[i].X=exit[i].x+x;
            exit[i].Y=exit[i].y+y;
            exit[i].Z=exit[i].z+z;
        }
    }
    public LocalStructure(JSONObject room) {
        this.name=room.getString("name");
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.rotate = false;
        kPorts=0;
        dx = room.getJSONArray("size").getInt(0);
        dy = room.getJSONArray("size").getInt(1);
        dz = room.getJSONArray("size").getInt(2);
        exit = new LocalBlock[dx * dy * dz];
        //System.out.println(dx);
        for (int i = 0; i < exit.length; i++) {
            exit[i] = new LocalBlock(i % dx, i / (dx * dz), (i % (dx * dz)) / dx, -1);
        }
        int qx;
        int qy;
        int qz;
        if (!room.isNull("exits")) {
            JSONArray exitsData = room.getJSONArray("exits");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                int ox = 0;
                int oy = 0;
                int oz = 0;
                if (qy==dy-1) {
                    oy=1;
                }
                else if (qy==0) {
                    oy=-1;
                }
                else if (qx==dx-1) {
                    ox=1;
                }
                else if (qx==0) {
                    ox=-1;
                }
                else if (qz==dz-1) {
                    oz=1;
                }
                else {
                    oz=-1;
                }
                //JSONArray offset = exitData.getJSONArray("offset");
                exit[n].setType(1, ox, oy, oz);
                exit[n].name=exitData.getString("block_name");
                port=exit[n];
            }
        }
        if (!room.isNull("chests")) {
            JSONArray exitsData = room.getJSONArray("chests");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                qx = 3;
                if (!exitData.isNull("size")) {
                    qx=exitData.getInt("size");
                }
                qy = 0;
                if (!exitData.isNull("probability")) {
                    double probability = exitData.getDouble("probability");
                    if (probability != 1) {
                        qy = (int) (probability * 10);
                    }
                }
                exit[n].setType(2, qx, qy, 0);
                if (exitData.has("block_name")) {
                    exit[n].name=exitData.getString("block_name");
                }
                else {
                    exit[n].name="chest";
                }
            }
        }
        if (!room.isNull("barrels")) {
            JSONArray exitsData = room.getJSONArray("barrels");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                qx = 3;
                if (!exitData.isNull("size")) {
                    qx=exitData.getInt("size");
                }
                qy = 0;
                if (!exitData.isNull("probability")) {
                    double probability = exitData.getDouble("probability");
                    if (probability != 1) {
                        qy = (int) (probability * 10);
                    }
                }
                exit[n].setType(3, qx, qy, 0);
                if (exitData.has("block_name")) {
                    exit[n].name=exitData.getString("block_name");
                }
                else {
                    exit[n].name="barrel";
                }
            }
        }
        if (!room.isNull("spawn_player")) {
            JSONObject exitData = room.getJSONObject("spawn_player");
            JSONArray coords = exitData.getJSONArray("coordinates");
            qx = coords.getInt(0);
            qy = coords.getInt(1);
            qz = coords.getInt(2);
            int n = qy * dx * dz + qz * dx + qx;
            exit[n].setType(4, 0, 0, 0);
        }
        if (!room.isNull("spawn_entity")) {
            JSONArray exitsData = room.getJSONArray("spawn_entity");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                qy = 0;
                if (!exitData.isNull("probability")) {
                    double probability = exitData.getDouble("probability");
                    if (probability != 1) {
                        qy = (int) (probability * 10);
                    }
                }
                exit[n].setType(5, qy, 0, 0);
                exit[n].name=exitData.getString("entity_name");
            }
        }
        if (!room.isNull("setblock")) {
            JSONArray exitsData = room.getJSONArray("setblock");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                qy = 0;
                qx=0;
                qz=0;
                if (!exitData.isNull("probability")) {
                    double probability = exitData.getDouble("probability");
                    if (probability != 1) {
                        int prob = (int) (probability*1000);
                        qz=prob%10;
                        qy=(prob/10)%10;
                        qx=prob/100;
                    }
                }
                exit[n].setType(6, qx, qy, qz);
                exit[n].name=exitData.getString("block_name");
            }
        }
        if (!room.isNull("custom")) {
            JSONArray exitsData = room.getJSONArray("custom");
            for (int i = 0; i < exitsData.length(); i++) {
                JSONObject exitData = exitsData.getJSONObject(i);
                JSONArray coords = exitData.getJSONArray("coordinates");
                qx = coords.getInt(0);
                qy = coords.getInt(1);
                qz = coords.getInt(2);
                int n = qy * dx * dz + qz * dx + qx;
                qy = 0;
                qx=0;
                qz=0;
                if (!exitData.isNull("probability")) {
                    double probability = exitData.getDouble("probability");
                    if (probability != 1) {
                        int prob = (int) (probability*1000);
                        qz=prob%10;
                        qy=(prob/10)%10;
                        qx=prob/100;
                    }
                }
                exit[n].setType(9, qx, qy, qz);
            }
        }
        exit[0].base=true;
        exit[0].count=exit.length;
    }
    public int initBlocks(Generation map, LocalBlock[] blocks, int kB) {
        kB+=exit.length;
        for (int i = 0; i < exit.length; i++) {
            if (exit[i].type==1) {
                map.exits[map.kExits]=exit[i];
                map.kExits++;
            }
            blocks[kB-exit.length+i]=exit[i];
        }
        return kB;
    }
    public boolean canLoad (Generation map) {
        boolean canLoad = true;
        if (map.gameMode==0) {
            for (int i = 0; i < exit.length; i++) {
                if (exit[i].X <= 0 || exit[i].X >= 256 || exit[i].Y <= 0 || exit[i].Y >= 64 || exit[i].Z <= 0 || exit[i].Z >= 256) {
                    canLoad = false;
                    break;
                }
            }
            if (canLoad) {
                for (int i = 0; i < map.kB; i++) {
                    if (map.blocks[i].X > x && map.blocks[i].X < x + dx && map.blocks[i].Y > y && map.blocks[i].Y < y + dy && map.blocks[i].Z > z && map.blocks[i].Z < z + dz) {
                        //System.out.println("Блок изза которого !canLoad: "+Main.blocks[i].X+" "+Main.blocks[i].Y+" "+Main.blocks[i].Z+" "+name);
                        canLoad = false;
                        break;
                    }
                }
            }
        }
        else if (map.gameMode==1) {
            if (x+dx<512&&y+dy<128&&z+dz<512) {
                for (int i = 0; i < map.kStr; i++) {
                    LocalStructure s = map.str[i];
                    if (dx + s.dx > Math.max(x + dx, s.x + s.dx) - Math.min(x, s.x) && dy + s.dy > Math.max(y + dy, s.y + s.dy) - Math.min(y, s.y) && dz + s.dz > Math.max(z + dz, s.z + s.dz) - Math.min(z, s.z)) {
                        canLoad = false;
                        break;
                    }
                }
            }
            else {
                canLoad=false;
            }
        }
        return canLoad;
    }
    public boolean canLoadWater(Generation map) {
        boolean canLoad = true;
            if (x+dx<256&&y+dy<0&&z+dz<256) {
                for (int i = 0; i < map.kWater; i++) {
                    LocalStructure s = map.water[i];
                    if (dx + s.dx > Math.max(x + dx, s.x + s.dx) - Math.min(x, s.x) && dy + s.dy > Math.max(y + dy, s.y + s.dy) - Math.min(y, s.y) && dz + s.dz > Math.max(z + dz, s.z + s.dz) - Math.min(z, s.z)) {
                        canLoad = false;
                        break;
                    }
                }
            }
            else {
                canLoad=false;
            }
        return canLoad;
    }
    public void end(Generation map) throws FileNotFoundException {

    }
    public void onTap(PlayerInteractEvent event, int index) {

    }
    public void withGeneration(int n) {

    }
    public void onEntityDamage(EntityDamageByEntityEvent event, int index) {

    }
    public void onBlockBreak(BlockBreakEvent event, int n) {

    }
    public void onPortal(PlayerTeleportEvent event, int n) {

    }
    public void onExplosion(EntityExplodeEvent eEvent, BlockExplodeEvent bEvent, int n) {

    }
    public void onHangingBreak(HangingBreakEvent event, int n) {

    }
}
