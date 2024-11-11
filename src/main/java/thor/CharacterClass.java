package thor;

import java.util.ArrayList;

public class CharacterClass {
    public ArrayList<String> exceptions = new ArrayList<>();
    public ArrayList<String> removedCategorys = new ArrayList<>();
    public String name;
    public String description = "";
    public String russianDescription = "";
    public CharacterClass(String name) {
        this.name=name;
    }
    public boolean containsItem(String s) {
        return exceptions.contains(s);
    }
    public void setDescription() {
        if (removedCategorys.size()>0) {
            description+="Can't use:\n";
            russianDescription+="Не умеет использовать:\n";
            for (int i = 0; i < removedCategorys.size(); i++) {
                description+=("    -"+removedCategorys.get(i)+'\n');
                russianDescription+=("    -"+FirstPlugin.translate(removedCategorys.get(i), null)+'\n');
            }
        }
    }
}
