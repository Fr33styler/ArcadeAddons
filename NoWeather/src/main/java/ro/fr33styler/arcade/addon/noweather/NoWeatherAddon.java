package ro.fr33styler.arcade.addon.noweather;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import ro.fr33styler.arcade.api.engine.addon.Addon;
import ro.fr33styler.arcade.api.engine.addon.annotation.Description;
import ro.fr33styler.arcade.api.engine.config.annotation.Setting;
import ro.fr33styler.arcade.api.engine.config.serialize.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Description(id = "NoWeather", author = "Fr33styler", version = "1.0")
public class NoWeatherAddon extends Addon implements Configuration, Listener {

    @Setting("blacklist")
    private Set<String> blacklist = new HashSet<>(List.of("world_name"));

    @Override
    public void onLoad() {
        getEngine().getConfigManager().tryLoad(this, new File(getDataFolder(), "config.yml"));
        for (World world : Bukkit.getWorlds()) {
            if (!blacklist.contains(world.getName())) {
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(0);
            }
        }
        getAddonManager().registerListener(this);
    }

    @Override
    public void onUnload() {
        blacklist.clear();
        getAddonManager().unregisterListener(this);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState() && !blacklist.contains(event.getWorld().getName())) {
            event.setCancelled(true);
        }
    }

}
