package wbs.extras.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;

public class NightVisionCommand extends WbsMessenger implements CommandExecutor {

	public NightVisionCommand(WbsPlugin plugin) {
		super(plugin);
		
		permission = plugin.getCommand("nightvision").getPermission();
	}
	
	private String permission = null;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		if (!checkPermission(sender, permission)) {
			return true;
		}
		
		Player player = (Player) sender;
		if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
			sendMessage("Night vision removed.", sender);
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		} else {
			sendMessage("Night vision applied. Repeat command to remove.", sender);
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 0, false, false));
		}
		return true;
	}

}
