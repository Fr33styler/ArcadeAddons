package ro.fr33styler.arcade.addon.revokepetonjoin;

import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ro.fr33styler.gameengine.api.addon.Addon;
import ro.fr33styler.gameengine.api.addon.annotation.Description;
import ro.fr33styler.gameengine.api.event.game.player.GameJoinEvent;

@Description(id = "RevokePetOnJoin", author = "Fr33styler", version = "1.0")
public class RevokePetOnJoin extends Addon implements Listener {

    @Override
    public void onLoad() {
        getEngine().getAddonManager().registerListener(this);
    }

    @Override
    public void onUnload() {
        getEngine().getAddonManager().unregisterListener(this);
    }

    @EventHandler
    public void onJoin(GameJoinEvent event) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pets revoke " + event.getPlayer().getName());
    }

}
