package thor;

import java.io.FileNotFoundException;

public class LocalChest {
    String name;
    Item[] items;
    int k;
    int x;
    int y;
    int z;
    int type;

    public LocalChest(LocalBlock block, Item[] items, int kItem, Item[] food, int kFood, int itemSumm, int foodSumm) throws FileNotFoundException {
        x = block.X;
        y = block.Y;
        z = block.Z;
        k = (int) (6 * Math.random()) + 6 + block.sx;
        this.type=block.type;
        this.items = new Item[k];
        for (int i = 0; i < this.items.length; i++) {
            int a;
            if (block.type == 2) {
                a = (int) (Math.random() * itemSumm);
                this.name = block.name;
                for (int j = 0; j < kItem; j++) {
                    if (a >= items[j].minW && a < items[j].maxW) {
                        this.items[i] = new Item(items[j].isStr, items[j].min, items[j].max, items[j].minW, items[j].maxW, items[j].name, items[j].data, items[j].nbt, items[j].cmd, items[j].cooldown, items[j].color, items[j].displayName);
                    }
                }
            } else {
                a = (int) (Math.random() * foodSumm);
                this.name = block.name;
                for (int j = 0; j < kFood; j++) {
                    if (a >= food[j].minW && a < food[j].maxW) {
                        this.items[i] = new Item(food[j].isStr, food[j].min, food[j].max, food[j].minW, food[j].maxW, food[j].name, food[j].data, food[j].nbt, food[j].cmd, food[j].cooldown, food[j].color, food[j].displayName);
                    }
                }
            }
            this.items[i].k = (int) ((this.items[i].max - this.items[i].min + 1) * Math.random()) + this.items[i].min;
        }
    }
}
