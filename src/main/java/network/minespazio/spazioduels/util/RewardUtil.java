package network.minespazio.spazioduels.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RewardUtil {

    public static class RewardItem {
        private final String name;
        private final String command;

        public RewardItem(String name, String command) {
            this.name = name;
            this.command = command;
        }

        public String getName() {
            return name;
        }

        public String getCommand() {
            return command;
        }
    }

    private static final Random RANDOM = new Random();

    public static List<RewardItem> loadRewardsFromConfig(ConfigurationSection section) {
        List<RewardItem> rewards = new ArrayList<>();
        if (section == null) return rewards;

        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                String name = section.getString(key + ".name", "&fRecompensa Misteriosa");
                String command = section.getString(key + ".command", "");
                rewards.add(new RewardItem(name, command));
            } else if (section.isList(key)) {
                List<?> list = section.getList(key);
                if (list != null) {
                    for (Object item : list) {
                        if (item instanceof ConfigurationSection sec) {
                            rewards.add(new RewardItem(sec.getString("name", "Recompensa"), sec.getString("command", "")));
                        }
                    }
                }
            }
        }

        // Fallback default rewards if empty
        if (rewards.isEmpty()) {
            rewards.add(new RewardItem("&a10,000 Monedas Spazio", "eco give %player% 10000"));
            rewards.add(new RewardItem("&bLlave de Crate Mítica", "crate give %player% mythic 1"));
            rewards.add(new RewardItem("&cEspada de Duelos Especial", "give %player% diamond_sword 1"));
            rewards.add(new RewardItem("&e500 Puntos de Duelos", "eco give %player% 500"));
            rewards.add(new RewardItem("&61x Manzana de Oro Encantada", "give %player% enchanted_golden_apple 1"));
        }

        return rewards;
    }

    public static List<RewardItem> getRandomRewards(List<RewardItem> pool, int count) {
        if (pool == null || pool.isEmpty()) return new ArrayList<>();
        List<RewardItem> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, RANDOM);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public static void executeRewards(Player player, List<RewardItem> rewards) {
        if (player == null || !player.isOnline() || rewards == null) return;
        for (RewardItem reward : rewards) {
            if (reward.getCommand() != null && !reward.getCommand().isEmpty()) {
                String cmd = reward.getCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }
}
