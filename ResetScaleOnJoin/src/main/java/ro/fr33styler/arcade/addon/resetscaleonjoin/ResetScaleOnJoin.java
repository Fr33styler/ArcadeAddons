package ro.fr33styler.arcade.addon.resetscaleonjoin;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ro.fr33styler.gameengine.api.addon.Addon;
import ro.fr33styler.gameengine.api.addon.annotation.Description;
import ro.fr33styler.gameengine.api.event.game.player.GameJoinEvent;

@Description(id = "ResetScaleOnJoin", author = "Fr33styler", version = "1.01")
public class ResetScaleOnJoin extends Addon implements Listener {

    private final Attribute scaleAttribute;

    public  ResetScaleOnJoin() {
        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("scale"));
        if (attribute == null) {
            scaleAttribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.scale"));
        } else {
            scaleAttribute = attribute;
        }
    }

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
        if (scaleAttribute == null) return;

        AttributeInstance attribute = event.getPlayer().getAttribute(scaleAttribute);
        if (attribute == null) return;

        attribute.setBaseValue(1);
    }

}
