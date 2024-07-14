package thor;

import java.io.IOException;

public class LocalBlock {
    int x;
    int y;
    int z;
    int type;
    int sx;
    int sy;
    int sz;
    boolean b;
    int X;
    int Y;
    int Z;
    String name;
    boolean useToFindDist = false;
    boolean base = false;
    int count;
    public LocalBlock(int x, int y, int z, int type) {
        this.x=x;
        this.y=y;
        this.z=z;
        this.type=type;
        this.sx=0;
        this.sy=0;
        this.sz=0;
        this.b=true;
    }
    public void setType(int type, int sx, int sy, int sz) {
        this.type=type;
        this.sx=sx;
        this.sy=sy;
        this.sz=sz;
    }
    public int canPlaceOn(LocalBlock[] blocks, int kB) {
        int min=300;
        if (sx != 0) {
            if (sx > 0&&(256-X)<min) {
                min=256-X;
            }
            else if (sx<0&&X<min) {
                min=X;
            }
        }
        else if (sy != 0) {
            if (sy > 0&&(64-Y)<min) {
                min=64-Y;
            }
            else if (sy<0&&Y<min) {
                min=Y;
            }
        }
        else if (sz != 0) {
            if (sz > 0&&(256-Z)<min) {
                min=256-Z;
            }
            else if (sz<0&&Z<min) {
                min=Z;
            }
        }
        //System.out.println("Расстояние до границы: "+min+" Параметры блока: "+X+", "+Y+", "+Z+", "+sx+","+sy+","+sz);
        for (int i = 0; i < kB; i++) {
            if ((blocks[i].X-X)*sx<min&&sx!=0&&Math.abs(blocks[i].Y-Y)<4&&Math.abs(blocks[i].Z-Z)<4&&(blocks[i].X-X)*sx>0) {
                min=(blocks[i].X-X)*sx;
                //System.out.println("Блок который хотим поставить: "+X+", "+Y+", "+Z+", sx = "+sx+"Кордината Х, блок номер "+i+" min = "+min+" sx = "+blocks[i].sx+".: "+blocks[i].X+", "+blocks[i].Y+", "+blocks[i].Z);
            }
            else if ((blocks[i].Y-Y)*sy<min&&sy!=0&&Math.abs(blocks[i].X-X)<4&&Math.abs(blocks[i].Z-Z)<4&&(blocks[i].Y-Y)*sy>0) {
                min=(blocks[i].Y-Y)*sy;
                //System.out.println("Блок который хотим поставить: "+X+", "+Y+", "+Z+", sy = "+sy+"Кордината Y, блок номер "+i+" min = "+min+" sy = "+blocks[i].sy+".: "+blocks[i].X+", "+blocks[i].Y+", "+blocks[i].Z);
            }
            else if((blocks[i].Z-Z)*sz<min&&sz!=0&&Math.abs(blocks[i].Y-Y)<4&&Math.abs(blocks[i].X-X)<4&&(blocks[i].Z-Z)*sz>0) {
                min=(blocks[i].Z-Z)*sz;
                //System.out.println("Блок который хотим поставить: "+X+", "+Y+", "+Z+", sz = "+sz+"Кордината Z, блок номер "+i+" min = "+min+" sz = "+blocks[i].sz+".: "+blocks[i].X+", "+blocks[i].Y+", "+blocks[i].Z);
            }
        }
        //System.out.println(r);
        return min;
    }
    public void canLink(Generation map, LocalBlock block) throws IOException {
        boolean b=false;
        LocalStructure[] str = null;
        LocalBlock[] blocks = null;
        int kStr = 0;
        int kB = 0;
        int dx = Math.abs(block.X-X);
        int dy = Math.abs(block.Y-Y);
        int dz = Math.abs(block.Z-Z);
        boolean done = false;
        if (sx*block.sx!=-1&&sy*block.sy!=-1&&sz*block.sz!=-1&&dx>=7&&dy>=9&&dz>=7&&block!=this&&(block.X-X)*sx>=0&&(X-block.X)*block.sx>=0&&(block.Y-Y)*sy>=0&&(Y-block.Y)*block.sy>=0&&(block.Z-Z)*sz>=0&&(Z-block.Z)*block.sz>=0) {
            if ((sx!=0&&canPlaceOn(map.blocks, map.kB)>dx+2||sy!=0&&canPlaceOn(map.blocks, map.kB)>dy+3||sz!=0&&canPlaceOn(map.blocks, map.kB)>dz)&&(block.sx!=0&&block.canPlaceOn(map.blocks, map.kB)>dx+2||block.sy!=0&&block.canPlaceOn(map.blocks, map.kB)>dy+3||block.sz!=0&&block.canPlaceOn(map.blocks, map.kB)>dz+2)) {
                int errorCount = 0;
                while (!done&&errorCount<1000) {
                    str = new LocalStructure[500];
                    blocks = new LocalBlock[10000];
                    kStr=0;
                    kB=0;
                    //System.out.println("Начало линкования");
                    int d;
                    String name;
                    LocalStructure ex1 = null;
                    LocalStructure ex2 = null;
                    LocalBlock to1 = null;
                    LocalBlock to2 = null;
                    LocalBlock out1 = null;
                    LocalBlock out2 = null;
                    LocalBlock example = null;
                    boolean contin = false;
                    if (sx != 0) {
                        example = new LocalBlock(0, 0, 0, 1);
                        example.X = X + dx * sx;
                        example.Y = Y;
                        example.Z = Z;
                        example.sx = 0;
                        if (block.sy != 0) {
                            example.sy = 0;
                            example.sz = (int) Math.signum(block.Z - Z);
                            d = Math.abs(block.Z - Z);
                        } else {
                            example.sz = 0;
                            example.sy = (int) (Math.signum(block.Y - Y));
                            d = Math.abs(block.Y - Y);
                        }
                        if (example.canPlaceOn(map.blocks, map.kB) > d) {
                            contin = true;
                            boolean found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex1 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex1.exit.length; i++) {
                                    if (ex1.exit[i].type == 1) {
                                        if (ex1.exit[i].sx == -sx) {
                                            f1 = true;
                                            to1 = ex1.exit[i];
                                        }
                                        if (ex1.exit[i].sy == example.sy && ex1.exit[i].sz == example.sz) {
                                            f2 = true;
                                            out1 = ex1.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                            found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex2 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex2.exit.length; i++) {
                                    if (ex2.exit[i].type == 1) {
                                        if (ex2.exit[i].sy == -block.sy && ex2.exit[i].sz == -block.sz) {
                                            f1 = true;
                                            to2 = ex2.exit[i];
                                        }
                                        if (ex2.exit[i].sy == -example.sy && ex2.exit[i].sz == -example.sz) {
                                            f2 = true;
                                            out2 = ex2.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                        }
                    } else if (sy != 0) {
                        example = new LocalBlock(0, 0, 0, 1);
                        example.Y = Y + dy * sy;
                        example.X = X;
                        example.Z = Z;
                        example.sy = 0;
                        if (block.sx != 0) {
                            example.sx = 0;
                            example.sz = (int) Math.signum(block.Z - Z);
                            d = Math.abs(block.Z - Z);
                        } else {
                            example.sz = 0;
                            example.sx = (int) (Math.signum(block.X - X));
                            d = Math.abs(block.X - X);
                        }
                        if (example.canPlaceOn(map.blocks, map.kB) > d) {
                            contin = true;
                            boolean found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex1 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex1.exit.length; i++) {
                                    if (ex1.exit[i].type == 1) {
                                        if (ex1.exit[i].sy == -sy) {
                                            f1 = true;
                                            to1 = ex1.exit[i];
                                        }
                                        if (ex1.exit[i].sx == example.sx && ex1.exit[i].sz == example.sz) {
                                            f2 = true;
                                            out1 = ex1.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                            found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex2 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex2.exit.length; i++) {
                                    if (ex2.exit[i].type == 1) {
                                        if (ex2.exit[i].sx == -block.sx && ex2.exit[i].sz == -block.sz) {
                                            f1 = true;
                                            to2 = ex2.exit[i];
                                        }
                                        if (ex2.exit[i].sx == -example.sx && ex2.exit[i].sz == -example.sz) {
                                            f2 = true;
                                            out2 = ex2.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                        }
                    } else {
                        example = new LocalBlock(0, 0, 0, 1);
                        example.Z = Z + dz * sz;
                        example.Y = Y;
                        example.X = X;
                        example.sz = 0;
                        if (block.sy != 0) {
                            example.sy = 0;
                            example.sx = (int) Math.signum(block.X - X);
                            d = Math.abs(block.X - X);
                        } else {
                            example.sx = 0;
                            example.sy = (int) (Math.signum(block.Y - Y));
                            d = Math.abs(block.Y - Y);
                        }
                        if (example.canPlaceOn(map.blocks, map.kB) > d) {
                            contin = true;
                            boolean found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex1 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex1.exit.length; i++) {
                                    if (ex1.exit[i].type == 1) {
                                        if (ex1.exit[i].sz == -sz) {
                                            f1 = true;
                                            to1 = ex1.exit[i];
                                        }
                                        if (ex1.exit[i].sy == example.sy && ex1.exit[i].sx == example.sx) {
                                            f2 = true;
                                            out1 = ex1.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                            found = false;
                            while (!found) {
                                boolean f1 = false;
                                boolean f2 = false;
                                ex2 = LocalStructure.spawn(map, 0, 0, 0, "room");
                                for (int i = 0; i < ex2.exit.length; i++) {
                                    if (ex2.exit[i].type == 1) {
                                        if (ex2.exit[i].sy == -block.sy && ex2.exit[i].sx == -block.sx) {
                                            f1 = true;
                                            to2 = ex2.exit[i];
                                        }
                                        if (ex2.exit[i].sy == -example.sy && ex2.exit[i].sx == -example.sx) {
                                            f2 = true;
                                            out2 = ex2.exit[i];
                                        }
                                    }
                                }
                                if (f1 && f2) {
                                    found = true;
                                }
                            }
                        }
                    }
                    //System.out.println(" Out1 = null!");
                    //System.out.println("Out1.sy = " + out1.sy);
                    if (!contin) {
                        done=true;
                    }
                    if (contin) {
                        block.b = false;
                        this.b = false;
                        //System.out.println("Инфа о this  " + X + " " + Y + " " + Z + ": " + sx + " " + sy + " " + sz);
                        //System.out.println("Инфа о block  " + block.X + " " + block.Y + " " + block.Z + ": " + block.sx + " " + block.sy + " " + block.sz);
                        if (sx != 0) {
                            out1.X = sx * dx + X - to2.x + out2.x;
                            out2.X = out1.X;
                        } else if (block.sx != 0) {
                            out2.X = block.X + dx * block.sx + out1.x - to1.x;
                            out1.X = out2.X;
                        } else {
                            out2.X = example.sx * dx + X + out2.x - to2.x;
                            out1.X = out1.x - to1.x + block.X - dx * example.sx;
                        }
                        if (sy != 0) {
                            out1.Y = sy * dy + Y - to2.y + out2.y;
                            out2.Y = out1.Y;
                        } else if (block.sy != 0) {
                            out2.Y = block.Y + dy * block.sy + out1.y - to1.y;
                            out1.Y = out2.Y;
                        } else {
                            out2.Y = example.sy * dy + Y + out2.y - to2.y;
                            out1.Y = out1.y - to1.y + block.Y - dy * example.sy;
                        }
                        if (sz != 0) {
                            out1.Z = sz * dz + Z - to2.z + out2.z;
                            out2.Z = out1.Z;
                        } else if (block.sz != 0) {
                            out2.Z = block.Z + dz * block.sz + out1.z - to1.z;
                            out1.Z = out2.Z;
                        } else {
                            out2.Z = example.sz * dz + Z + out2.z - to2.z;
                            out1.Z = out1.z - to1.z + block.Z - dz * example.sz;
                        }
                        to1.X = out1.X + to1.x - out1.x;
                        to2.X = out2.X + to2.x - out2.x;
                        to1.Y = out1.Y + to1.y - out1.y;
                        to2.Y = out2.Y + to2.y - out2.y;
                        to1.Z = out1.Z + to1.z - out1.z;
                        to2.Z = out2.Z + to2.z - out2.z;
                        //System.out.println("to1 " + to1.X + " " + to1.Y + " " + to1.Z);
                        //System.out.println("to2 " + to2.X + " " + to2.Y + " " + to2.Z);
                        //System.out.println("out1 " + out1.X + " " + out1.Y + " " + out1.Z);
                        //System.out.println("out2 " + out2.X + " " + out2.Y + " " + out2.Z);
                        b = true;
                        int k = Math.abs(to1.X - X) - 1;
                        if (k < 0) {
                            k = Math.abs(to1.Y - Y) - 1;
                        }
                        if (k < 0) {
                            k = Math.abs(to1.Z - Z) - 1;
                        }
                        int type = 1000 + Math.abs(sx) * 100 + Math.abs(sy) * 10 + Math.abs(sz);
                        //System.out.println(type);
                        LocalStructure ex = LocalStructure.spawn(map, 0, 0, 0, ""+Math.abs(sx)+Math.abs(sy)+Math.abs(sz));
                        if (k > 0) {
                            str[kStr] = LocalStructure.spawn(map, X + sx - ex.port.x, Y + sy - ex.port.y, Z + sz - ex.port.z, ex);
                            kB = str[kStr].initBlocks(map, blocks, kB);
                            str[kStr].port.b = false;
                            kStr++;
                        }
                        for (int i = 1; i < k; i++) {
                            str[kStr] = LocalStructure.spawn(map, str[kStr - 1].port.X + sx - ex.port.x, str[kStr - 1].port.Y + sy - ex.port.y, str[kStr - 1].port.Z + sz - ex.port.z, ex);
                            kB = str[kStr].initBlocks(map, blocks, kB);
                            str[kStr].port.b = false;
                            kStr++;
                        }
                        str[kStr] = LocalStructure.spawn(map, out1.X - out1.x, out1.Y - out1.y, out1.Z - out1.z, ex1);
                        if (str[kStr].canLoad(map)) {
                            kB = str[kStr].initBlocks(map, blocks, kB);
                            for (int i = 0; i < str[kStr].exit.length; i++) {
                                if (str[kStr].exit[i].type == 1 && str[kStr].exit[i].x == to1.x && str[kStr].exit[i].y == to1.y && str[kStr].exit[i].z == to1.z) {
                                    str[kStr].exit[i].b = false;
                                }
                                if (str[kStr].exit[i].type == 1 && str[kStr].exit[i].x == out1.x && str[kStr].exit[i].y == out1.y && str[kStr].exit[i].z == out1.z) {
                                    str[kStr].exit[i].b = false;
                                }
                            }
                            kStr++;
                            k = Math.abs(out2.X - out1.X) - 1;
                            if (k < 0) {
                                k = Math.abs(out2.Y - out1.Y) - 1;
                            }
                            if (k < 0) {
                                k = Math.abs(out2.Z - out1.Z) - 1;
                            }
                            type = 1000 + Math.abs(out1.sx) * 100 + Math.abs(out1.sy) * 10 + Math.abs(out1.sz);
                            //System.out.println(type);
                            ex = LocalStructure.spawn(map, 0, 0, 0, ""+Math.abs(out1.sx)+Math.abs(out1.sy)+Math.abs(out1.sz));
                            if (k > 0) {
                                str[kStr] = LocalStructure.spawn(map, out1.X + out1.sx - ex.port.x, out1.Y + out1.sy - ex.port.y, out1.Z + out1.sz - ex.port.z, ex);
                                kB = str[kStr].initBlocks(map, blocks, kB);
                                str[kStr].port.b = false;
                                kStr++;
                            }
                            for (int i = 1; i < k; i++) {
                                str[kStr] = LocalStructure.spawn(map, str[kStr - 1].port.X + out1.sx - ex.port.x, str[kStr - 1].port.Y + out1.sy - ex.port.y, str[kStr - 1].port.Z + out1.sz - ex.port.z, ex);
                                kB = str[kStr].initBlocks(map, blocks, kB);
                                str[kStr].port.b = false;
                                kStr++;
                            }
                                str[kStr] = LocalStructure.spawn(map, out2.X - out2.x, out2.Y - out2.y, out2.Z - out2.z, ex2);
                            if (str[kStr].canLoad(map)) {
                                kB = str[kStr].initBlocks(map, blocks, kB);
                                for (int i = 0; i < str[kStr].exit.length; i++) {
                                    if (str[kStr].exit[i].type == 1 && str[kStr].exit[i].x == to2.x && str[kStr].exit[i].y == to2.y && str[kStr].exit[i].z == to2.z) {
                                        str[kStr].exit[i].b = false;
                                    }
                                    if (str[kStr].exit[i].type == 1 && str[kStr].exit[i].x == out2.x && str[kStr].exit[i].y == out2.y && str[kStr].exit[i].z == out2.z) {
                                        str[kStr].exit[i].b = false;
                                    }
                                }
                                kStr++;
                                k = Math.abs(block.X - to2.X) - 1;
                                if (k < 0) {
                                    k = Math.abs(block.Y - to2.Y) - 1;
                                }
                                if (k < 0) {
                                    k = Math.abs(block.Z - to2.Z) - 1;
                                }
                                type = 1000 + Math.abs(to2.sx) * 100 + Math.abs(to2.sy) * 10 + Math.abs(to2.sz);
                                ex = LocalStructure.spawn(map, 0, 0, 0, ""+Math.abs(to2.sx)+Math.abs(to2.sy)+Math.abs(to2.sz));
                                if (k > 0) {
                                    str[kStr] = LocalStructure.spawn(map, to2.X + to2.sx - ex.port.x, to2.Y + to2.sy - ex.port.y, to2.Z + to2.sz - ex.port.z, ex);
                                    kB = str[kStr].initBlocks(map, blocks, kB);
                                    str[kStr].port.b = false;
                                    kStr++;
                                }
                                for (int i = 1; i < k; i++) {
                                    str[kStr] = LocalStructure.spawn(map, str[kStr - 1].port.X + to2.sx - ex.port.x, str[kStr - 1].port.Y + to2.sy - ex.port.y, str[kStr - 1].port.Z + to2.sz - ex.port.z, ex);
                                    kB = str[kStr].initBlocks(map, blocks, kB);
                                    str[kStr].port.b = false;
                                    kStr++;
                                }
                                //System.out.println(Main.kB);
                                //System.out.println("k = " + k);
                                //System.out.println("Тип тоннеля: " + type);
                                //System.out.println("Out1.sy в конце= " + out1.sy);
                                //System.out.println("Конец линкования");
                                done=true;
                            }
                            else {
                                errorCount++;
                                //System.out.println("Не удалось провести линкование (второй блок) "+ex2.x+" "+ex2.y+" "+ex2.z+" "+ex2.name);
                            }
                        }
                        else {
                            errorCount++;
                            //System.out.println("Не удалось провести линкование (первый блок) "+ex1.x+" "+ex1.y+" "+ex1.z+" "+ex1.name);
                        }
                    }
                }
                if (errorCount>=999) {
                    str=new LocalStructure[1];
                }
            }
        }
        if (str!=null&&str.length>1) {
            //System.out.println(kB+" "+kStr);
            //System.out.println(str.length);
            //System.out.println("kStr = "+kStr);
            for (int k = 0; k < kStr; k++) {
                map.str[map.kStr] = str[k];
                map.kStr++;
            }
            for (int k = 0; k < kB; k++) {
                map.blocks[map.kB]=blocks[k];
                map.kB++;
            }
        }
    }
}
