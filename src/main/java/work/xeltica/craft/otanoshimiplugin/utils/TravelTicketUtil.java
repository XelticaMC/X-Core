package work.xeltica.craft.otanoshimiplugin.utils;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import work.xeltica.craft.otanoshimiplugin.travels.TicketType;

public class TravelTicketUtil {

    public static ItemStack GenerateTravelTicket(String ticketType) {
        return GenerateTravelTicket(ticketType);
    }

    public static ItemStack GenerateTravelTicket(int amount, TicketType ticketType) {
        var ticket = new ItemStack(Material.WRITTEN_BOOK, amount);
        var meta = ticket.getItemMeta();
        meta.setDisplayName(String.format(TRAVEL_TICKET_NAME, ticketType.getDisplayName()));
        var list = new ArrayList<String>();
        list.add(TRAVEL_TICKET_LORE1);
        list.add(TRAVEL_TICKET_LORE2);
        list.add(TRAVEL_TICKET_LORE3);
        list.add("");
        list.add(ticketType.toString());
        meta.setLore(list);
        ticket.setItemMeta(meta);
        return ticket;
    }

    public static boolean isTravelTicket(ItemStack i) {
        if (i.getType() != Material.WRITTEN_BOOK) return false;
        var meta = i.getItemMeta();

        var lores = meta.getLore();
        if (lores == null || lores.size() != 5) return false;
        if (!lores.get(0).equals(TRAVEL_TICKET_LORE1)) return false;
        if (!lores.get(1).equals(TRAVEL_TICKET_LORE2)) return false;
        if (!lores.get(2).equals(TRAVEL_TICKET_LORE3)) return false;

        return true;
    }

    public static TicketType getTicketType(ItemStack i) {
        var meta = i.getItemMeta();
        var lores = meta.getLore();

        return TicketType.valueOf(lores.get(4));
    }

    private static final String TRAVEL_TICKET_NAME = ChatColor.AQUA + "%s行き旅行券";
    private static final String TRAVEL_TICKET_LORE1 = "手に持って右クリックまたはタップすると、";
    private static final String TRAVEL_TICKET_LORE2 = "遠く離れた土地にひとっ飛び。";
    private static final String TRAVEL_TICKET_LORE3 = "使う前に必ず準備をすること。";
}
