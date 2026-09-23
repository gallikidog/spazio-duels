/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.expansion.PlaceholderExpansion
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package network.minespazio.spazioduels.placeholder;

import java.time.Duration;
import java.time.LocalDateTime;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothCaptureSnapshot;
import network.minespazio.spazioduels.koth.KothMatch;
import network.minespazio.spazioduels.koth.KothSchedule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpazioDuelsPlaceholderExpansion
extends PlaceholderExpansion {
    private final SpazioDuelsPlugin plugin;

    public SpazioDuelsPlaceholderExpansion(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public String getIdentifier() {
        return "spazioduels";
    }

    @NotNull
    public String getAuthor() {
        return "SpazioTeam";
    }

    @NotNull
    public String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    public boolean persist() {
        return true;
    }

    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        KothCaptureSnapshot kothSnapshot = this.getKothSnapshot();
        NextEvent nextEvent = this.getNextEvent();
        if (params.equalsIgnoreCase("next_event")) {
            return nextEvent.format();
        }
        if (params.equalsIgnoreCase("next_event_type")) {
            return nextEvent.type;
        }
        if (params.equalsIgnoreCase("next_event_name")) {
            return nextEvent.name;
        }
        if (params.equalsIgnoreCase("next_event_time")) {
            return nextEvent.formatMinutes();
        }
        if (params.equalsIgnoreCase("koth_active")) {
            return kothSnapshot.active() ? "true" : "false";
        }
        if (params.equalsIgnoreCase("koth_name")) {
            return kothSnapshot.kothName();
        }
        if (params.equalsIgnoreCase("koth_capper")) {
            return kothSnapshot.capperName();
        }
        if (params.equalsIgnoreCase("koth_time")) {
            return kothSnapshot.formatTime();
        }
        if (params.equalsIgnoreCase("koth_progress_percent")) {
            return String.valueOf(kothSnapshot.progressPercent());
        }
        if (params.equalsIgnoreCase("koth_progress_bar")) {
            return kothSnapshot.progressBar();
        }
        if (params.equalsIgnoreCase("koth_remaining_seconds")) {
            return String.valueOf(kothSnapshot.remainingSeconds());
        }
        if (params.equalsIgnoreCase("koth_status")) {
            return kothSnapshot.status();
        }
        String kothValue = this.getNamedKothPlaceholder(params);
        if (kothValue != null) {
            return kothValue;
        }
        if (player == null) {
            return "";
        }
        DuelMatch match = this.plugin.getDuelManager().getMatch(player);
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

    private KothCaptureSnapshot getKothSnapshot() {
        KothMatch activeKoth = this.plugin.getKothManager().getActiveMatch();
        return activeKoth != null ? activeKoth.getCaptureSnapshot() : KothCaptureSnapshot.inactive();
    }

    private String getNamedKothPlaceholder(String params) {
        String[] fields;
        String prefix = "koth_";
        if (!params.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }
        for (String field : fields = new String[]{"activation_time", "next_activation", "time_left", "capper", "time", "active", "status"}) {
            boolean active;
            String suffix = "_" + field;
            if (!params.regionMatches(true, params.length() - suffix.length(), suffix, 0, suffix.length())) continue;
            String name = params.substring(prefix.length(), params.length() - suffix.length());
            Koth koth = this.plugin.getKothManager().getKoth(name);
            if (koth == null) {
                return null;
            }
            KothCaptureSnapshot snapshot = this.getKothSnapshot();
            boolean bl = active = snapshot.active() && snapshot.kothName().equalsIgnoreCase(koth.getName());
            if (field.equals("activation_time")) {
                return this.plugin.getKothManager().getNextActivationTime(koth);
            }
            if (field.equals("next_activation")) {
                return this.plugin.getKothManager().getNextActivationCountdown(koth);
            }
            if (field.equals("capper")) {
                return active ? snapshot.capperName() : "Nadie";
            }
            if (field.equals("time")) {
                return active ? snapshot.formatTime() : "00:00";
            }
            if (field.equals("time_left")) {
                KothMatch activeKoth = this.plugin.getKothManager().getActiveMatch();
                return active && activeKoth != null ? activeKoth.formatEventTimeLeft() : "00:00";
            }
            if (field.equals("active")) {
                return active ? "true" : "false";
            }
            if (!field.equals("status")) continue;
            return active ? snapshot.status() : "INACTIVO";
        }
        return null;
    }

    private NextEvent getNextEvent() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextKothTime = null;
        Koth nextKoth = null;
        for (Koth koth : this.plugin.getKothManager().getKoths()) {
            LocalDateTime candidate = KothSchedule.getNextActivation(koth.getActivationTimes(), now);
            if (candidate == null || nextKothTime != null && !candidate.isBefore(nextKothTime)) continue;
            nextKothTime = candidate;
            nextKoth = koth;
        }
        LocalDateTime nextDuelTime = this.plugin.getDuelEventManager().getNextAutoStartAt();
        if (nextDuelTime != null && nextDuelTime.isAfter(now) && (nextKothTime == null || nextDuelTime.isBefore(nextKothTime))) {
            return new NextEvent("DUELOS", "Evento de Duelos", Duration.between(now, nextDuelTime).getSeconds());
        }
        if (nextKoth != null) {
            return new NextEvent("KOTH", nextKoth.getName(), Duration.between(now, nextKothTime).getSeconds());
        }
        return new NextEvent("NINGUNO", "No programado", -1L);
    }

    private static final class NextEvent {
        private final String type;
        private final String name;
        private final long remainingSeconds;

        private NextEvent(String type, String name, long remainingSeconds) {
            this.type = type;
            this.name = name;
            this.remainingSeconds = remainingSeconds;
        }

        private String format() {
            if (this.type.equals("NINGUNO")) {
                return this.name;
            }
            return "Quedan " + this.formatMinutes() + " para el siguiente evento: " + this.type + " - " + this.name + ".";
        }

        private String formatMinutes() {
            if (this.remainingSeconds < 0L) {
                return "--";
            }
            long minutes = this.remainingSeconds / 60L;
            return minutes == 1L ? "1 minuto" : minutes + " minutos";
        }
    }
}

