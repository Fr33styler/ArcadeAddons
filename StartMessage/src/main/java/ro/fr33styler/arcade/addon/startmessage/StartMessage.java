package ro.fr33styler.arcade.addon.startmessage;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import ro.fr33styler.arcade.api.engine.addon.Addon;
import ro.fr33styler.arcade.api.engine.addon.annotation.Description;
import ro.fr33styler.arcade.api.engine.config.annotation.Setting;
import ro.fr33styler.arcade.api.engine.config.annotation.bounded.BoundedInteger;
import ro.fr33styler.arcade.api.engine.config.serialize.Configuration;
import ro.fr33styler.arcade.api.engine.event.game.GameStartEvent;
import ro.fr33styler.arcade.api.engine.event.game.player.GameJoinEvent;
import ro.fr33styler.arcade.api.engine.game.Game;

import java.io.File;
import java.util.*;

@Description(id = "StartMessage", author = "Fr33styler, Rafael Auler", version = "1.0.3")
public class StartMessage extends Addon implements Configuration, Listener {

    @Setting("sound")
    private String soundName = "entity.arrow.hit_player";

    @Setting("send-message-every")
    @BoundedInteger(min = 1)
    private int sendMessageEvery = 60;

    @Setting("on-start-message")
    private String onStartMessage = "&eA game room of %game_name% with id %id% started with %players% players.";

    @Setting("starting-message")
    private String startingMessage = "&eA game room of %game_name% with id %id% is about to start with %players% players. Click here to join it!";

    @Setting("lobby-worlds")
    private Set<String> lobbyWorlds = new HashSet<>(List.of("world_name"));

    private Sound sound;
    private BukkitTask task;
    private final Map<String, StartingGame> starting = new LinkedHashMap<>();

    @Override
    public void onLoad() {
        getEngine().getAddonManager().registerListener(this);
        getEngine().getConfigManager().tryLoad(this, new File(getDataFolder(), "config.yml"));

        onStartMessage = ChatColor.translateAlternateColorCodes('&', onStartMessage);
        startingMessage = ChatColor.translateAlternateColorCodes('&', startingMessage);

        task = Bukkit.getScheduler().runTaskTimer(getEngine().getPlugin(), this::onRun, 0, 20);

        try {
            Class.forName("org.bukkit.Registry");
            sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName));
        } catch (ClassNotFoundException ignored) {
            try {
                sound = Sound.valueOf(soundName);
            } catch (IllegalArgumentException ignored2) {}
        }
    }

    @Override
    public void onUnload() {
        task.cancel();
        getEngine().getAddonManager().unregisterListener(this);
    }

    private void onRun() {
        starting.entrySet().removeIf(entry -> {
            Game game = entry.getValue().getGame();
            return game.getGamers().size() < game.getMap().getMinimumPlayers();
        });
        for (StartingGame startingGame : starting.values()) {

            Game game = startingGame.getGame();
            if (startingGame.getTimer() == 0 || startingGame.getTimer() % sendMessageEvery == 0) {
                TextComponent startingMessageComponent = new TextComponent(startingMessage
                        .replace("%game_name%", game.getManager().getName())
                        .replace("%id%", game.getMap().getId())
                        .replace("%players%", String.valueOf(game.getGamers().size()))
                );
                startingMessageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        game.getManager().getId().replace("-", "") + " join " + game.getMap().getId()));
                getEngine().getUsers().forEachUser(user -> {
                    Player player = user.toPlayer();
                    if (!user.isInGame() && lobbyWorlds.contains(player.getWorld().getName())) {
                        player.spigot().sendMessage(startingMessageComponent);
                        user.sendActionBar(startingMessageComponent.getText());

                        if (sound != null) {
                            player.playSound(player.getLocation(), sound, 1f, 1f);
                        }
                    }
                });
            }

            startingGame.setTimer(startingGame.getTimer() + 1);
        }
    }

    @EventHandler
    public void onJoin(GameJoinEvent event) {
        Game game = event.getGame();
        if (game.getGamers().size() >= game.getMap().getMinimumPlayers() && !starting.containsKey(game.getMap().getId())) {
            starting.put(game.getMap().getId(), new StartingGame(game));
        }
    }

    @EventHandler
    public void onStart(GameStartEvent event) {
        Game game = event.getGame();
        if (starting.remove(game.getMap().getId()) == null) return;

        String startMessage = onStartMessage
                .replace("%game_name%", game.getManager().getName())
                .replace("%id%", game.getMap().getId())
                .replace("%players%", String.valueOf(game.getGamers().size()));

        getEngine().getUsers().forEachUser(user -> {
            Player player = user.toPlayer();

            if (!user.isInGame() && lobbyWorlds.contains(player.getWorld().getName())) {
                player.sendMessage(startMessage);
                user.sendActionBar(startMessage);

                if (sound != null) {
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                }
            }
        });
    }

    private static class StartingGame {

        private int timer;
        private final Game game;

        public StartingGame(Game game) {
            this.game = game;
        }

        public Game getGame() {
            return game;
        }

        public int getTimer() {
            return timer;
        }

        public void setTimer(int timer) {
            this.timer = timer;
        }

    }

}
