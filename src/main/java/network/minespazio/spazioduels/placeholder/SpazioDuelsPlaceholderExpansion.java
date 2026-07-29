package network.minespazio.spazioduels.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.koth.KothMatch;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpazioDuelsPlaceholderExpansion extends PlaceholderExpansion {

    private final SpazioDuelsPlugin plugin;

    public SpazioDuelsPlaceholderExpansion(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "spazioduels";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SpazioTeam";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("koth_active")) {
            KothMatch activeKoth = plugin.getKothManager().getActiveMatch();
            return activeKoth != null && activeKoth.isActive() ? "true" : "false";
        }

        if (params.equalsIgnoreCase("koth_name")) {
            KothMatch activeKoth = plugin.getKothManager().getActiveMatch();
            return activeKoth != null && activeKoth.isActive() ? activeKoth.getKoth().getName() : "Ninguno";
        }

        if (params.equalsIgnoreCase("koth_capper")) {
            KothMatch activeKoth = plugin.getKothManager().getActiveMatch();
            if (activeKoth != null && activeKoth.isActive() && activeKoth.getCurrentCapper() != null) {
                return activeKoth.getCurrentCapper().getName();
            }
            return "Nadie";
        }

        if (params.equalsIgnoreCase("koth_time")) {
            KothMatch activeKoth = plugin.getKothManager().getActiveMatch();
            if (activeKoth != null && activeKoth.isActive()) {
                return activeKoth.formatTime(activeKoth.getRemainingSeconds());
            }
            return "00:00";
        }

        if (player == null) return "";

        DuelMatch match = plugin.getDuelManager().getMatch(player);

        if (params.equalsIgnoreCase("in_duel")) {
            return match != null ? "true" : "false";
        }

        if (params.equalsIgnoreCase("mode")) {
            return match != null ? match.getMode().getDisplayName() : "Ninguno";
        }

        if (params.equalsIgnoreCase("kitselect") || params.equalsIgnoreCase("kit")) {
            return match != null && match.getKit() != null ? match.getKit().getName() : "Sin Kit";
        }

        if (params.equalsIgnoreCase("arena")) {
            return match != null ? match.getArena().getName() : "Ninguna";
        }

        return null;
    }
}
