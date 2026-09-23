/*
 * Decompiled with CFR 0.152.
 */
package network.minespazio.spazioduels.koth;

import java.util.Locale;

public record KothCaptureSnapshot(boolean active, String kothName, String capperName, int remainingSeconds, int progressPercent, String progressBar, String status) {
    public static KothCaptureSnapshot inactive() {
        return new KothCaptureSnapshot(false, "Ninguno", "Nadie", 0, 0, "", "INACTIVO");
    }

    public String formatTime() {
        int seconds = Math.max(0, this.remainingSeconds);
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }
}

