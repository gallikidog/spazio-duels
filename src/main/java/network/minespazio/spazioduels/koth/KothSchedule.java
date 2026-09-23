/*
 * Decompiled with CFR 0.152.
 */
package network.minespazio.spazioduels.koth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class KothSchedule {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private KothSchedule() {
    }

    public static String normalizeTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT).format(TIME_FORMAT);
        }
        catch (DateTimeParseException exception) {
            return null;
        }
    }

    public static LocalDateTime getNextActivation(List<String> activationTimes, LocalDateTime now) {
        if (activationTimes == null || activationTimes.isEmpty()) {
            return null;
        }
        LocalDateTime next = null;
        for (int dayOffset = 0; dayOffset <= 1; ++dayOffset) {
            for (String activationTime : activationTimes) {
                LocalDateTime candidate;
                String normalized = KothSchedule.normalizeTime(activationTime);
                if (normalized == null || (candidate = LocalDateTime.of(now.toLocalDate().plusDays(dayOffset), LocalTime.parse(normalized, TIME_FORMAT))).isBefore(now) || next != null && !candidate.isBefore(next)) continue;
                next = candidate;
            }
        }
        return next;
    }

    public static String formatCountdown(LocalDateTime nextActivation, LocalDateTime now) {
        if (nextActivation == null) {
            return "--:--:--";
        }
        long seconds = Math.max(0L, Duration.between(now, nextActivation).getSeconds());
        return String.format("%02d:%02d:%02d", seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
    }
}

