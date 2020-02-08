package wbs.extras.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.ess3.api.events.LocalChatSpyEvent;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.util.WbsMessenger;

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
	}
}
