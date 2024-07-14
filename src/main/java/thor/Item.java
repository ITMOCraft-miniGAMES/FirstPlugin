package thor;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Item {
    boolean isStr;
    int k;
    int minW;
    int maxW;
    int max;
    int min;
    int data;
    String nbt;
    String name;
    String displayName;
    int cooldown;
    int cmd = 0;
    ChatColor color;
    public Item(boolean isStr, int min, int max, int minW, int maxW, String name, int data, String nbt, int cmd, int cooldown, ChatColor color, String displayName) throws FileNotFoundException {
        this.isStr=isStr;
        this.max=max;
        this.min=min;
        this.name=name;
        this.minW=minW;
        this.maxW=maxW;
        this.data=data;
        this.cmd=cmd;
        this.cooldown=cooldown;
        this.color=color;
        this.displayName=displayName;
        if (nbt!=null&&nbt.equals("nbt")) {
            Scanner fout3 = new Scanner(new File(Generation.inputPath + "nbt.txt"));
            this.nbt = fout3.nextLine();
        }
        else {
            this.nbt=nbt;
        }
    }
}
