package work.xeltica.craft.otanoshimiplugin.gui;

import java.util.function.Consumer;

import org.bukkit.Material;

public class MenuItem {
    public MenuItem(String name) { this(name, null); }

    public MenuItem(String name, Consumer<MenuItem> onClick) { this(name, onClick, Material.STONE_BUTTON); }

    public MenuItem(String name, Consumer<MenuItem> onClick, Material icon) { this(name, onClick, icon, null); }

    public MenuItem(String name, Consumer<MenuItem> onClick, Material icon, Object customData) { this(name, onClick, icon, customData, 1); }

    public MenuItem(String name, Consumer<MenuItem> onClick, Material icon, Object customData, int count) {
        this.name = name;
        this.onClick = onClick;
        this.icon = icon;
        this.count = count;
        this.customData = customData;
    }

    public String getName() { return name; }
    public Consumer<MenuItem> getOnClick() { return onClick; }
    public int getCount() { return count; }
    public Material getIcon() { return icon; }
    public Object getCustomData() { return customData; }

    private String name;
    private Material icon;
    private int count;
    private Consumer<MenuItem> onClick;
    private Object customData;
}
