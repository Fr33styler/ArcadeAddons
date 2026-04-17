package ro.fr33styler.arcade.addon.hidefromlobbyonjoin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ro.fr33styler.arcade.api.engine.addon.Addon;
import ro.fr33styler.arcade.api.engine.addon.annotation.Description;
import ro.fr33styler.arcade.api.engine.event.game.player.GameJoinEvent;
import ro.fr33styler.arcade.api.engine.event.game.player.GameLeaveEvent;

@Description(id = "HideFromLobbyOnJoin", author = "Fr33styler", version = "1.0")
public class HideFromLobbyOnJoinAddon extends Addon implements Listener {

    private int feature;

    @Override
    public void onLoad() {
        getEngine().getAddonManager().registerListener(this);
        String[] version = Bukkit.getServer().getBukkitVersion().split("\\.");
        feature = Integer.parseInt(version[0].equals("1") ? version[1] : version[0]);
    }

    @Override
    public void onUnload() {
        getEngine().getAddonManager().unregisterListener(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getEngine().getUsers().forEachUser(user -> {
            if (!user.isInGame()) return;

            if (feature > 12) {
                player.hidePlayer(getEngine().getPlugin(), user.toPlayer());
            } else {
                player.hidePlayer(user.toPlayer());
            }
        });
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent event) {
        Player player = event.getPlayer();
        getEngine().getUsers().forEachUser(user -> {
            if (user.isInGame()) return;

            if (feature > 12) {
                user.toPlayer().hidePlayer(getEngine().getPlugin(), player);
            } else {
                user.toPlayer().hidePlayer(player);
            }
        });
    }

    @EventHandler
    public void onGameLeave(GameLeaveEvent event) {
        Player player = event.getPlayer();
        getEngine().getUsers().forEachUser(user -> {
            if (feature > 12) {
                if (user.isInGame()) {
                    player.hidePlayer(getEngine().getPlugin(), user.toPlayer());
                } else {
                    user.toPlayer().showPlayer(getEngine().getPlugin(), player);
                }
            } else {
                if (user.isInGame()) {
                    player.hidePlayer(user.toPlayer());
                } else {
                    user.toPlayer().showPlayer(player);
                }
            }
        });
    }

}
