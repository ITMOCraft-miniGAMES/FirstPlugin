package thor;

import json.JSONObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Shop extends LocalStructure {
    public LocalBlock[] iconBlock = new LocalBlock[4];
    public Item[] icons = new Item[4];
    public int[] cost = new int[4];
    public ItemFrame[] itemFrames = new ItemFrame[4];
    public Shop(JSONObject room) {
        super(room);
        isSimple = false;
    }
    @Override
    public void end (Generation generation) {
        command=new String[16];
        map=new String[4];
        Item item;
        int cost1;
        int k = 0;
        for (int i = 0; i < exit.length; i++) {
            if (exit[i].type==9) {
                boolean found = false;
                int a = 0;
                while (!found) {
                    a = (int)(Math.random()* generation.kItem);
                    if (generation.items[a].maxW- generation.items[a].minW<=6) {
                        found=true;
                    }
                }
                item= generation.items[a];
                cost1=7-(item.maxW-item.minW)+(int)(Math.random()*4);
                cost[k]=cost1;
                icons[k]=item;
                iconBlock[k]=exit[i];
                k++;
            }
        }
    }
    @Override
    public void withGeneration(int n) {
        Location loc = new Location(FirstPlugin.world, 0, 0, 0);
        for (int i = 0; i < icons.length; i++) {
            loc.set(FirstPlugin.x[n]+iconBlock[i].X, FirstPlugin.y[n]+iconBlock[i].Y, FirstPlugin.z[n]+iconBlock[i].Z);
            ItemFrame itemFrame = (ItemFrame) FirstPlugin.world.spawnEntity(loc, EntityType.ITEM_FRAME);
            itemFrame.setFacingDirection(BlockFace.EAST, true);
            itemFrames[i]=itemFrame;
            ItemStack itemStack;
            if (!icons[i].isStr) {
                itemStack = new ItemStack(Material.matchMaterial(icons[i].name));
            }
            else {
                itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                String name = icons[i].name;
                int value = Character.getNumericValue(name.charAt(name.length()-1));
                name=name.substring(0, name.length()-1);
                //System.out.println(Enchantment.getByName(name));
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                meta.addStoredEnchant(Enchantment.getByName(name), value, true);
                itemStack.setItemMeta(meta);
            }
            itemFrame.setItem(itemStack);
            loc.set(loc.getBlockX(), loc.getBlockY()-2, loc.getBlockZ());
            Block block = FirstPlugin.world.getBlockAt(loc);
            Sign sign = (Sign) block.getState();
            if (icons[i].isStr) {
                sign.setLine(1, ChatColor.WHITE+icons[i].name);
            }
            sign.setLine(0, ChatColor.WHITE+"Стоимость: " + cost[i]);
            sign.update();
        }
    }
    @Override
    public void onTap(PlayerInteractEvent event, int n) {
        Block block = event.getClickedBlock();
        Location blockLoc = block.getLocation();
        BlockData blockData = block.getBlockData();
        if (blockData.getMaterial().equals(Material.CRIMSON_BUTTON)) {
            for (int i = 0; i < iconBlock.length; i++) {
                if (FirstPlugin.x[n]+ iconBlock[i].X==blockLoc.getBlockX()&&FirstPlugin.y[n]+ iconBlock[i].Y-1==blockLoc.getBlockY()&&FirstPlugin.z[n]+ iconBlock[i].Z==blockLoc.getBlockZ()) {
                    Player player = event.getPlayer();
                    Inventory inv = player.getInventory();
                    ItemStack itemStack = new ItemStack(Material.NETHER_STAR, cost[i]);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("Trade token");
                    itemStack.setItemMeta(meta);
                    if (inv.containsAtLeast(itemStack, cost[i])) {
                        int q = inv.first(Material.NETHER_STAR);
                        itemStack = inv.getItem(q);
                        itemStack.setAmount(itemStack.getAmount()-cost[i]);
                        inv.setItem(q, itemStack);
                        org.bukkit.entity.Item item = (org.bukkit.entity.Item) FirstPlugin.world.spawnEntity(player.getLocation(), EntityType.DROPPED_ITEM);
                        item.setItemStack(itemFrames[i].getItem());
                        itemFrames[i].remove();
                        block.setType(Material.AIR);
                        player.sendMessage(ChatColor.GREEN+"Предмет приобретён");
                    }
                    else {
                        player.sendMessage(ChatColor.RED+"Не хватает токенов");
                    }
                }
            }
        }
    }
    @Override
    public void onEntityDamage(EntityDamageByEntityEvent event, int n) {
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        for (int i = 0; i < iconBlock.length; i++) {
            if (itemFrame==itemFrames[i]) {
                Entity entity = event.getDamager();
                if (entity.getType().equals(EntityType.PLAYER)) {
                    Player player = (Player)entity;
                    Inventory inv = player.getInventory();
                    ItemStack itemStack = new ItemStack(Material.NETHER_STAR, cost[i]);
                    if (inv.containsAtLeast(itemStack, cost[i])) {
                        inv.remove(itemStack);
                        org.bukkit.entity.Item item = (org.bukkit.entity.Item) FirstPlugin.world.spawnEntity(player.getLocation(), EntityType.DROPPED_ITEM);
                        item.setItemStack(itemFrames[i].getItem());
                        itemFrames[i].remove();
                        Block block = FirstPlugin.world.getBlockAt((int) itemFrame.getX(), (int) (itemFrame.getY()-1), (int) itemFrame.getZ());
                        block.setType(Material.AIR);
                        player.sendMessage(ChatColor.GREEN+"Предмет приобретён");
                    }
                    else {
                        player.sendMessage(ChatColor.RED+"Не хватает токенов");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    @Override
    public void onBlockBreak(BlockBreakEvent event, int n) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        for (int i = 0; i < iconBlock.length; i++) {
            if (iconBlock[i].X+FirstPlugin.x[n]==loc.getBlockX()&&iconBlock[i].Y+FirstPlugin.y[n]==loc.getBlockY()&&iconBlock[i].Z+FirstPlugin.z[n]==loc.getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }
}
