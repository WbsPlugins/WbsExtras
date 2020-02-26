package wbs.extras.listeners;

import java.util.Formatter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import net.ess3.api.events.LocalChatSpyEvent;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.configurations.Replacement;

import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;
import wbs.utils.util.plugin.WbsMessenger;

public class EssentialsListener extends WbsMessenger implements Listener {

	private ExtrasSettings settings;
	public EssentialsListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}

	
	@EventHandler(priority=EventPriority.LOWEST)
	public void localSpyEvent(LocalChatSpyEvent event) {
		if (!settings.doLocalSpyFilter()) {
			return;
		}
		
		for (String ignoreStart : settings.getLocalFilters()) {
			if (event.getMessage().startsWith(ignoreStart)) {
				event.setCancelled(true);
				return;
			}
		}
		event.getPlayer();
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void localSpyPAPIHook(LocalChatSpyEvent event) {
		if (!settings.doEssentialsPAPIHook()) {
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void catchChatForLocalSpy(AsyncPlayerChatEvent event) {
		if (!settings.doEssentialsPAPIHook()) {
			return;
		}

		String format = event.getFormat();

		Player sender = event.getPlayer();
		format = PlaceholderAPIWrapper.setPlaceholders(sender, format);

		String message = event.getMessage();
		Formatter formatter = new Formatter();
		
		String fullMessage = formatter.format(format, sender, message).toString();
		
		formatter.close();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("essentials.chat.spy")) {
				sendMessage(fullMessage, player);
			}
		}
	}
}
