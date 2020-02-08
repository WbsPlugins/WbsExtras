package wbs.extras.commands.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsStrings;

public class SignEditCommand extends WbsMessenger implements CommandExecutor, TabCompleter  {

	private ExtrasSettings settings;
	public SignEditCommand(WbsExtras plugin) {
		super(plugin);
		
		settings = plugin.settings;
	}
	
	private String permission = "wbsextras.signedit";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage("This command is only usable by players.", sender);
			return true;
		}
		
		if (!checkPermission(sender, permission)) {
			return true;
		}
		
		if (args.length == 0) {
			sendMessage("Usage: &b/signedit <line> <text>", sender);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("looking")) {
			PlayerStore store = PlayerStore.getInstance();
			
			PlayerData data = store.getPlayerData(sender.getName());
			
			if (data.signEditLine == 0) {
				sendMessage("You don't have a sign edit pending! Do &h/signedit <1-4> <text>&r first.", sender);
				return true;
			}
			
			Player player = (Player) sender;
			
			Block targetBlock = player.getTargetBlock(null, 4);
			
			if (targetBlock == null) {
				sendMessage("Look at a sign to change it!", sender);
				return true;
			}
			
			BlockState blockState = targetBlock.getState();
			
			if (blockState instanceof Sign) {
				Sign signState = (Sign) blockState;
				signState.setLine(data.signEditLine - 1, data.signEditString);
				sendMessage("Sign changed!", sender);
				signState.update();
				
				data.signEditLine = 0;
				data.signEditString = null;
			} else {
				sendMessage("You are not looking at a sign! (Looking at " + targetBlock.getType().toString().toLowerCase() + ")", sender);
			}
			
			return true;
		} else if (args[0].equalsIgnoreCase("cancel")) {
			PlayerStore store = PlayerStore.getInstance();
			
			PlayerData data = store.getPlayerData(sender.getName());
			
			if (data.signEditLine == 0) {
				sendMessage("You don't have a sign edit pending!", sender);
				return true;
			}
			
			
		}
		
		byte line;
		try {
			line = Byte.parseByte(args[0]);
		} catch (NumberFormatException e) {
			sendMessage("Invalid line number. Use 1-4.", sender);
			return true;
		}
		
		if (line < 1 || line > 4) {
			sendMessage("Invalid line number. Use 1-4.", sender);
			return true;
		}
		
		if (args.length == 1) {
			sendMessage("Usage: &b/signedit " + args[0] + " <text>", sender);
			return true;
		}
		
		PlayerStore store = PlayerStore.getInstance();
		
		PlayerData data = store.getPlayerData(sender.getName());
		
		if (data.signEditLine == 0) {
			sendMessage("You have a sign edit pending! Right click a sign, or do &h/signedit cancel&r.", sender);
			return true;
		}
		
		data.signEditString = ChatColor.translateAlternateColorCodes('&', WbsStrings.combineLast(args, 1));
		data.signEditLine = line;
		
		sendMessage("Right click a sign to change it!", sender);
		if (sender.hasPermission("wbsextras.signedit.looking")) {
			sendMessage("If you are unable to click it, do &h/signedit looking&r while looking at it.", sender);
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> choices = new LinkedList<>();

		if (sender.hasPermission(permission)) {
			int length = args.length;
			if (length == 1) {
				choices.add("1");
				choices.add("2");
				choices.add("3");
				choices.add("4");
				if (sender.hasPermission("wbsextras.signedit.looking")) {
					choices.add("looking");
				}
			}
		}
		
    	List<String> result = new ArrayList<String>();
		for (String add : choices) {
    		if (add.toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
    			result.add(add);
    		}
		}
		
		return result;
	}

}
