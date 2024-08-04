package ro.fr33styler.arcade.addon.lobbyscoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ro.fr33styler.gameengine.api.addon.Addon;
import ro.fr33styler.gameengine.api.addon.annotation.Description;
import ro.fr33styler.gameengine.api.config.annotation.Setting;
import ro.fr33styler.gameengine.api.config.annotation.bounded.BoundedInteger;
import ro.fr33styler.gameengine.api.config.serialize.Configuration;
import ro.fr33styler.gameengine.api.game.GameManager;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;

@Description(id = "LobbyScoreboard", author = "Fr33styler", version = "1.0")
public class LobbyScoreboardAddon extends Addon implements Configuration, Listener {

    private BukkitTask task;
    private BiFunction<Player, String, String> placeholderAPI = (player, string) -> string;

    @Setting("refresh-rate")
    @BoundedInteger(min = 1)
    private int refreshRate = 20;

    @Setting("whitelist")
    private Set<String> whitelist = new HashSet<>(List.of("world_name"));

    @Setting("title")
    private String title = "&e&lArcade Games";

    @Setting("scoreboard")
    private List<String> scoreboard = new ArrayList<>(List.of(
            "&7%localtime_timezone_468,MM/dd/yy%",
            " ",
            "Block Party Wins: &a%block-party_stats_wins%",
            "Bomb Lobbers Wins: &a%bomb-lobbers_stats_wins%",
            "Mini Walls Wins: &a%mini-walls_stats_wins%",
            "Party Games Wins: &a%party-games_stats_wins%",
            "Prop Hunt Wins: &a%prop-hunt_stats_wins%",
            "Quake Wins: &a%quake_stats_wins%",
            "Splegg Wins: &a%splegg_stats_wins%",
            "Temple Run Wins: &a%temple-run_stats_wins%",
            "TNT Tag Wins: &a%tnt-tag_stats_wins%",
            "TNT Run Wins: &a%tnt-run_stats_wins%",
            " ",
            "&ewww.spigotmc.org")
    );

    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();

    private static final Scoreboard EMPTY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();

    @Override
    public void onLoad() {
        getEngine().getConfigManager().tryLoad(this, new File(getDataFolder(), "config.yml"));
        Plugin plugin = getEngine().getPlugin();
        getEngine().getAddonManager().registerListener(this);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::onRun, 0, refreshRate);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPI = PlaceholderAPI::setPlaceholders;
        }
        getEngine().getUsers().forEachUser(user -> {
            setNewScoreboard(user.toPlayer());
        });
    }

    @Override
    public void onUnload() {
        whitelist.clear();
        task.cancel();
    }



    private String limitLength(String text) {
        return text.length() > 48 ? text.substring(0, 48) : text;
    }

    private void setNewScoreboard(Player player) {
        if (!whitelist.contains(player.getWorld().getName())) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective("LobbyScoreboard", "dummy", "LobbyScoreboard");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        setUpTeams(scoreboard);
        updateSidebar(scoreboard, player);

        player.setScoreboard(scoreboard);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    private void setUpTeams(Scoreboard scoreboard) {
        for (int i = 0; i < 15; i++) {
            String name = ChatColor.values()[i] + "§r";
            scoreboard.registerNewTeam(name).addEntry(name);
        }
    }

    private void updateSidebar(Scoreboard scoreboard, Player player) {
        Objective sidebar = scoreboard.getObjective("LobbyScoreboard");
        if (sidebar == null) return;

        sidebar.setDisplayName(ChatColor.translateAlternateColorCodes('&', placeholderAPI.apply(player, title)));
        int length = Math.min(this.scoreboard.size(), 15);
        for (int i = 0; i < 15; i++) {
            String name = ChatColor.values()[i] + "§r";
            if (this.scoreboard.size() > i) {
                Team team = scoreboard.getTeam(name);
                if (team == null) continue;

                team.setPrefix(limitLength(ChatColor.translateAlternateColorCodes('&', placeholderAPI.apply(player, this.scoreboard.get(i)))));
                sidebar.getScore(name).setScore(length - i);
            } else {
                scoreboard.resetScores(name);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        setNewScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        scoreboards.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!whitelist.contains(player.getWorld().getName())) {
            player.setScoreboard(EMPTY_SCOREBOARD);
            scoreboards.remove(player.getUniqueId());
        } else if (!scoreboards.containsKey(player.getUniqueId())) {
            setNewScoreboard(player);
        }
    }

    private void onRun() {
        getEngine().getUsers().forEachUser(user -> {
            Player player = user.toPlayer();
            Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
            if (!user.isInGame() && scoreboard != null) {
                updateSidebar(scoreboard, player);
            }
        });
    }

}
