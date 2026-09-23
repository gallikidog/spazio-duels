/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RewardUtil {
    private static final Random RANDOM = new Random();

    public static List<RewardItem> loadRewardsFromConfig(ConfigurationSection section) {
        ArrayList<RewardItem> rewards = new ArrayList<RewardItem>();
        if (section == null) {
            return rewards;
        }
        for (String key : section.getKeys(false)) {
            List list;
            if (section.isConfigurationSection(key)) {
                String name = section.getString(key + ".name", "&fRecompensa Misteriosa");
                String command = section.getString(key + ".command", "");
                rewards.add(new RewardItem(name, command));
                continue;
            }
            if (!section.isList(key) || (list = section.getList(key)) == null) continue;
            for (Object item : list) {
                if (item instanceof ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection)item;
                    rewards.add(new RewardItem(sec.getString("name", "Recompensa"), sec.getString("command", "")));
                    continue;
                }
                if (!(item instanceof Map)) continue;
                Map map = (Map)item;
                Object name = map.get("name");
                Object command = map.get("command");
                rewards.add(new RewardItem(name == null ? "Recompensa" : String.valueOf(name), command == null ? "" : String.valueOf(command)));
            }
        }
        if (rewards.isEmpty()) {
            rewards.add(new RewardItem("&a10,000 Monedas Spazio", "eco give %player% 10000"));
            rewards.add(new RewardItem("&bLlave de Crate M\u00edtica", "crate give %player% mythic 1"));
            rewards.add(new RewardItem("&cEspada de Duelos Especial", "give %player% diamond_sword 1"));
            rewards.add(new RewardItem("&e500 Puntos de Duelos", "eco give %player% 500"));
            rewards.add(new RewardItem("&61x Manzana de Oro Encantada", "give %player% enchanted_golden_apple 1"));
        }
        return rewards;
    }

    public static List<RewardItem> getRandomRewards(List<RewardItem> pool, int count) {
        if (pool == null || pool.isEmpty()) {
            return new ArrayList<RewardItem>();
        }
        ArrayList<RewardItem> shuffled = new ArrayList<RewardItem>(pool);
        Collections.shuffle(shuffled, RANDOM);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public static void executeRewards(Player player, List<RewardItem> rewards) {
        if (player == null || !player.isOnline() || rewards == null) {
            return;
        }
        for (RewardItem reward : rewards) {
            if (reward.getCommand() == null || reward.getCommand().isEmpty()) continue;
            String cmd = reward.getCommand().replace("%player_name%", player.getName()).replace("%player%", player.getName());
            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)cmd);
        }
    }

    public static class RewardItem {
        private final String name;
        private final String command;

        public RewardItem(String name, String command) {
            this.name = name;
            this.command = command;
        }

        public String getName() {
            return this.name;
        }

        public String getCommand() {
            return this.command;
        }
    }
}

