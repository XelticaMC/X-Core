package work.xeltica.craft.core.utils;

import java.util.ArrayList;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.travels.TicketType;

/**
 * 旅行券に関する便利機能を用意します。
 * @author Xeltica
 * @deprecated
 */
public class TravelTicketUtil {

    public static ItemStack GenerateTravelTicket(TicketType ticketType) {
        return GenerateTravelTicket(1, ticketType);
    }

    public static ItemStack GenerateTravelTicket(int amount, TicketType ticketType) {
        final var ticket = new ItemStack(Material.WRITTEN_BOOK, amount);
        final var meta = ticket.getItemMeta();
        meta.displayName(Component.text(String.format(TRAVEL_TICKET_NAME, ticketType.getDisplayName())));
        final var list = new ArrayList<String>();
        list.add(TRAVEL_TICKET_LORE1);
        list.add(TRAVEL_TICKET_LORE2);
        list.add(TRAVEL_TICKET_LORE3);
        list.add("");
        list.add(ticketType.toString());
        meta.lore(list.stream().map(l -> Component.text(l).asComponent()).toList());
        ticket.setItemMeta(meta);
        return ticket;
    }

    public static boolean isTravelTicket(ItemStack i) {
        if (i == null) return false;
        if (i.getType() != Material.WRITTEN_BOOK) return false;
        final var meta = i.getItemMeta();

        final var lores = Objects.requireNonNull(meta.lore()).stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        if (lores == null || lores.size() != 5) return false;
        if (!lores.get(0).equals(TRAVEL_TICKET_LORE1)) return false;
        if (!lores.get(1).equals(TRAVEL_TICKET_LORE2)) return false;
        return lores.get(2).equals(TRAVEL_TICKET_LORE3);
    }

    public static TicketType getTicketType(ItemStack i) {
        final var meta = i.getItemMeta();
        final var lores = meta.lore();

        return TicketType.valueOf(PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(lores).get(4)));
    }

    private static final String TRAVEL_TICKET_NAME = ChatColor.AQUA + "%s行き旅行券";
    private static final String TRAVEL_TICKET_LORE1 = "手に持って右クリックまたはタップすると、";
    private static final String TRAVEL_TICKET_LORE2 = "遠く離れた土地にひとっ飛び。";
    private static final String TRAVEL_TICKET_LORE3 = "使う前に必ず準備をすること。";
}
