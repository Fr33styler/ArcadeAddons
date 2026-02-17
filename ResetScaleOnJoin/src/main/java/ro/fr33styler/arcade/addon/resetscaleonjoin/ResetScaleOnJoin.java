package ro.fr33styler.arcade.addon.resetscaleonjoin;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ro.fr33styler.gameengine.api.addon.Addon;
import ro.fr33styler.gameengine.api.addon.annotation.Description;
import ro.fr33styler.gameengine.api.event.game.player.GameJoinEvent;

@Description(id = "ResetScaleOnJoin", author = "Fr33styler", version = "1.0")
public class ResetScaleOnJoin extends Addon implements Listener {

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
        event.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(1);
    }

}
