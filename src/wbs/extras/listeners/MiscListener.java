package wbs.extras.listeners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsStrings;
import wbs.extras.util.WbsTime;

public class MiscListener extends WbsMessenger implements Listener {

	private ExtrasSettings settings;
	public MiscListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}
	/************************************************/
	/*					Colour Book			  		*/
	/************************************************/
	
	@EventHandler
	public void onSignBook(PlayerEditBookEvent event) {
		Player player = event.getPlayer();
		if (event.isSigning()) {
			if (player.hasPermission("wbsextras.colourbook")) {
				for (String pageText : event.getNewBookMeta().getPages()) {
					if (!pageText.equals(ChatColor.translateAlternateColorCodes('&', pageText))) {
						sendMessage("You may now colour the book with &h/colourbook&r!", player);
						return;
					}
				}
			}
		}
	}

	/************************************************/
	/*					Sign edit			  		*/
	/************************************************/
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onRightClickOnSign(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		Block block = event.getClickedBlock();
		if (event.getClickedBlock() == null) { // This shouldn't come up but doesn't hurt
			return;
		}
		
		BlockState blockState = block.getState();
		if (!(blockState instanceof Sign)) {
			return;
		}
		Player player = event.getPlayer();
		
		PlayerStore store = PlayerStore.getInstance();
		PlayerData data = store.getPlayerData(player.getName());
		
		if (data.signEditLine != 0) {
			Sign signState = (Sign) blockState;
			signState.setLine(data.signEditLine - 1, data.signEditString);
			signState.update();
			sendMessage("Sign changed!", player);
		
			data.signEditLine = 0;
			data.signEditString = null;
		}

	}
	
	/************************************************/
	/*				Item Cooldown			  		*/
	/************************************************/
	
	private Table<Player, Material, LocalDateTime> itemCooldowns = HashBasedTable.create();
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if (!settings.doItemCooldowns()) {
			return;
		}
		
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		
		switch (event.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			break;
		default:
			return;
		}
		
		Player player = event.getPlayer();
		Material type = player.getInventory().getItemInMainHand().getType();
		
		Double cooldown = settings.getCooldownFor(type);
		if (cooldown != null) {
			LocalDateTime lastUse = itemCooldowns.get(player, type);
			LocalDateTime now = LocalDateTime.now();
			if (lastUse == null) {
				itemCooldowns.put(player, type, now);
			} else {
				Duration between = Duration.between(lastUse, now);
				if (between.toMillis() / 50 < cooldown) {
					LocalDateTime unlockTime = lastUse.plusNanos((long) (cooldown / 20 * 1000000000.0));
					Duration timeLeft = Duration.between(LocalDateTime.now(), unlockTime);
					String timeLeftString = WbsTime.prettyTime(timeLeft);
					sendActionBar("&cYou can use that again in " + timeLeftString + ".", player);
					event.setCancelled(true);
				} else {
					itemCooldowns.put(player, type, now);
				}
			}
		}
	}

	/************************************************/
	/*			Prevent Crazy Fireworks		  		*/
	/************************************************/

	@EventHandler
	public void onFireworkLaunch(FireworkExplodeEvent event) {
		if (!settings.preventOPFireworks()) {
			return;
		}
		
		Firework firework = event.getEntity();
		
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		if (fireworkMeta.getEffectsSize() > settings.getEffectsThreshold()) {
			event.setCancelled(true);
			firework.remove();
			Location loc = firework.getLocation();
			broadcastActionBar("&cThis firework had too many effects.", 10, loc);
		}
	}

	/************************************************/
	/*				Dispenser Cooldown		  		*/
	/************************************************/
	
	private final Map<Location, LocalDateTime> lastDispense = new HashMap<>();
	
	@EventHandler
	public void onDispenseCooldown(BlockDispenseEvent event) {
		if (!settings.doDispenserCooldown()) {
			return;
		}
		Location loc = event.getBlock().getLocation();
		
		if (lastDispense.containsKey(loc)) {
			long ticksSinceLastDispense = Duration.between(lastDispense.get(loc), LocalDateTime.now()).toMillis() / 50;
			if (ticksSinceLastDispense <= settings.getDispenserCooldown()) {
				event.setCancelled(true);
			} else {
				lastDispense.put(loc, LocalDateTime.now());
			}
		} else {
			lastDispense.put(loc, LocalDateTime.now());
		}
	}

	/************************************************/
	/*				Cancel Custom Potions	  		*/
	/************************************************/

	@EventHandler
	public void onPotionThrow(PlayerInteractEvent event) {
		if (!settings.cancelCustomPotions()) {
			return;
		}
		
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		ItemStack item = event.getItem();
		if (item == null) {
			return;
		}
		
		if (item.getType() != Material.SPLASH_POTION && item.getType() != Material.LINGERING_POTION) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if (isCustomPotion(item)) {
			event.setCancelled(true);
			sendActionBar("&cYou cannot use custom potions.", player);
		}
	}
	
	@EventHandler
	public void onDrinkPotion(PlayerItemConsumeEvent event) {
		if (!settings.cancelCustomPotions()) {
			return;
		}
		
		ItemStack item = event.getItem();
		
		Player player = event.getPlayer();
		
		if (isCustomPotion(item)) {
			event.setCancelled(true);
			sendActionBar("&cYou cannot use custom potions.", player);
		}
	}
	
	@EventHandler
	public void onDispense(BlockDispenseEvent event) {
		if (!settings.cancelCustomPotions()) {
			return;
		}
		
		Block block = event.getBlock();
		
		if (block.getType() == Material.DISPENSER) {
			ItemStack item = event.getItem();
			
			if (settings.cancelCustomPotions()) {
				if (isCustomPotion(item)) {
					event.setCancelled(true);
					for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 3, 3, 3)) {
						if (entity instanceof Player) {
							sendActionBar("&cYou cannot dispense custom potions.", (Player) entity);
						}
					}
				}
			}
		} 
	}
	
	private boolean isCustomPotion(ItemStack item) {
		if (item.getType() != Material.POTION && item.getType() != Material.LINGERING_POTION && item.getType() != Material.SPLASH_POTION) {
			return false;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (meta instanceof PotionMeta) {
				PotionMeta potionMeta = (PotionMeta) meta;
				PotionData data = potionMeta.getBasePotionData();
				
				if (data.getType() == PotionType.UNCRAFTABLE) {
					return true;
				}
			}
		}
		return false;
	}
	
	/************************************************/
	/*				Cancel Container Drops	  		*/
	/************************************************/
	
	@EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
	public void onContainerBreak(BlockBreakEvent event) {
		if (!settings.cancelContainerDrops()) {
			return;
		}
		
		Block block = event.getBlock();
		if (block.getState() instanceof Container) {
			if (block.getType() != Material.SHULKER_BOX) {
				((Container) block.getState()).getInventory().clear();
			}
		}
	}
	

	/************************************************/
	/*				   Book Commands	  			*/
	/************************************************/
	
	@EventHandler(ignoreCancelled=true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!settings.doBookCommands()) return;
		
		String command = event.getMessage();

		ItemStack book = settings.getBookForCommand(command);
		if (book != null) {
			event.setCancelled(true);
			
		//	event.getPlayer().openBook(book); // Needs 1.14 rip
		}
	}
	
	/************************************************/
	/*				ALLOW TNT WATERLOG	  			*/
	/************************************************/
	
	@EventHandler(ignoreCancelled=true)
	public void onExplosion(ExplosionPrimeEvent event) {
		if (!settings.allowTntDamagesWaterlogged()) {
			return;
		}
		
		if (event.getEntityType() == EntityType.PRIMED_TNT) {
			Entity entity = event.getEntity();
			Location center = entity.getLocation();
			removeIfWaterLogged(center);
			
			removeIfWaterLogged(center.clone().add(1, 0, 0));
			removeIfWaterLogged(center.clone().add(-1, 0, 0));
			removeIfWaterLogged(center.clone().add(0, 1, 0));
			removeIfWaterLogged(center.clone().add(0, -1, 0));
			removeIfWaterLogged(center.clone().add(0, 0, 1));
			removeIfWaterLogged(center.clone().add(0, 0, -1));
		}
	}
	
	private void removeIfWaterLogged(Location loc) {
		Block block = loc.getBlock();
		BlockData data = block.getBlockData();
		if (data != null) {
			if (data instanceof Waterlogged) {
				Waterlogged waterData = (Waterlogged) data;
				if (waterData.isWaterlogged()) {
					final World world = loc.getWorld();
					block.setType(Material.AIR);
					new BukkitRunnable() {
						@Override
						public void run() {
							world.dropItemNaturally(loc, new ItemStack(block.getType()));
						}
					}.runTaskLater(plugin, 1L);
				}
			}
		}
	}
	
	/************************************************/
	/*					BOSS BAR JOIN	  			*/
	/************************************************/
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		BarAnnouncement.addPlayer(event.getPlayer());
	}

	/************************************************/
	/*				DISABLE CORAL DECAY  			*/
	/************************************************/
	
	@EventHandler
	public void onCoralFade(BlockFadeEvent event) {
		if (!settings.blockCoralFade) {
			return;
		}
		Tag<Material> coralTag = Tag.CORALS;
		Tag<Material> wallCoralTag = Tag.WALL_CORALS;
		Tag<Material> coralBlocksTag = Tag.CORAL_BLOCKS;
		Material blockType = event.getBlock().getType();
		if (coralTag.isTagged(blockType)) {
			event.setCancelled(true);
		} else if (wallCoralTag.isTagged(blockType)) {
			event.setCancelled(true);
		} else if (coralBlocksTag.isTagged(blockType)) {
			event.setCancelled(true);
		}
		
		Block block = event.getBlock();
	}
	

	/************************************************/
	/*					VOTE SLEEP  				*/
	/************************************************/
	@EventHandler
	public void onSleep(PlayerBedEnterEvent event) {
		if (!settings.doVoteSleep()) {
			return;
		}

		Player player = event.getPlayer();
		boolean voteSleepWorld = false;
		for (String worldName : settings.getVoteSleepWorlds()) {
			if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
				voteSleepWorld = true;
				break;
			}
		}
		if (!voteSleepWorld) {
			return;
		}
		
		World world = player.getWorld();
		List<Player> playersInWorld = world.getPlayers();
		
		if (event.getBedEnterResult().equals(BedEnterResult.OK)) {
			
			int amountSleeping = getPlayersSleepingInWorld(world) + 1;
			double playersNeeded = getPlayersNeededInWorld(world);

			int sleepPercent = settings.getSleepPercent();
			
			if (amountSleeping >= playersNeeded/100*(sleepPercent)) {
				
				for (Player playerInWorld : playersInWorld) {
					sendMessage(player.getName() + " went to sleep. Good night everybody!", playerInWorld);
				}
				
				new BukkitRunnable() {
					int escape = 0;
					@Override
		            public void run() {
						world.setTime(world.getTime() + 50 % 24000);
						// Players wake up at 23460. Slightly lower so skippingNight is still on to ignore messages.
						if (world.getTime() > 23400 || world.getTime() < 1000 || escape > 5000) {
							this.cancel();
							world.setTime(23461);
							world.setStorm(false);
						}
						escape++;
		            }
		        }.runTaskTimer(plugin, 1L, 1L);
		        
			} else {
				String display;
				if (amountSleeping == 1) {
					display = player.getName() + " wants to sleep. " + sleepPercent + "% of players must be in a bed to skip the night. (1/" + ((int) playersNeeded) + ")";
				} else {
					display = player.getName() + " wants to sleep. (" + amountSleeping + "/" + ((int) playersNeeded) + ")";
				}
				
				if (settings.voteSleepActionBar()) {
					for (Player playerInWorld : playersInWorld) {
						sendActionBar(display, playerInWorld);
					}
				} else {
					for (Player playerInWorld : playersInWorld) {
						sendMessage(display, playerInWorld);
					}
				}
			}
		}
	}
	
	private int getPlayersNeededInWorld(World world) {
		int playersNeeded = 0;
		for (Player playerInWorld : world.getPlayers()) {
			if (playerInWorld.getGameMode() == GameMode.SURVIVAL) {
				playersNeeded++;
			}
		}
		
		return playersNeeded;
	}
	
	private int getPlayersSleepingInWorld(World world) {
		int amountSleeping = 0;
		for (Player playerInWorld : world.getPlayers()) {
			if (playerInWorld.getGameMode() == GameMode.SURVIVAL) {
				if (playerInWorld.isSleeping()) {
					amountSleeping++;
				}
			}
		}
		return amountSleeping;
	}
	
	@EventHandler
	public void onLeaveBed(PlayerBedLeaveEvent event) {
		if (!settings.doVoteSleep()) {
			return;
		}

		Player player = event.getPlayer();
		boolean voteSleepWorld = false;
		for (String worldName : settings.getVoteSleepWorlds()) {
			if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
				voteSleepWorld = true;
				break;
			}
		}
		if (!voteSleepWorld) {
			return;
		}
		
		World world = player.getWorld();
		List<Player> playersInWorld = world.getPlayers();

		int amountSleeping = getPlayersSleepingInWorld(world);
		double playersNeeded = getPlayersNeededInWorld(world);

		int sleepPercent = settings.getSleepPercent();
		
		String display = "";
		if (amountSleeping < playersNeeded/100*(sleepPercent)) {
			if (world.getTime() > 23400 || world.getTime() < 50) {
				return;
			}
			display = player.getName() + " left their bed. (" + amountSleeping + "/" + ((int) playersNeeded) + ")";


			if (settings.voteSleepActionBar()) {
				for (Player playerInWorld : playersInWorld) {
					sendActionBar(display, playerInWorld);
				}
			} else {
				for (Player playerInWorld : playersInWorld) {
					sendMessage(display, playerInWorld);
				}
			}
		}
	}
	
	/************************************************/
	/*					ANTI-LAVA-PVP  				*/
	/************************************************/

	@EventHandler
	public void onLavaPlace(PlayerInteractEvent event) {
		if (settings.lavaPlaceDistance() <= 0) {
			return;
		}
		
		Action action = event.getAction();
		Player player = event.getPlayer();
		if (action == Action.RIGHT_CLICK_BLOCK) {
			ItemStack tool;
			if (event.getHand() == EquipmentSlot.HAND) {
				tool = player.getInventory().getItemInMainHand();
			} else {
				tool = player.getInventory().getItemInOffHand();
			}
			if (tool.getType() == Material.FLINT_AND_STEEL || tool.getType() == Material.LAVA_BUCKET) {
				
				double distance = settings.lavaPlaceDistance();
				Predicate<Entity> predicate = new Predicate<Entity>() {
					@Override
					public boolean test(Entity entity) {
						if (entity instanceof Player) {
							if (((Player) entity).equals(player)) {
								return false;
							}
							return true;
						}
						return false;
					}
				};
				Collection<? extends Entity> nearbyPlayers = player.getWorld().getNearbyEntities(event.getClickedBlock().getLocation(), distance, distance, distance, predicate);
				if (!nearbyPlayers.isEmpty()) {
					event.setCancelled(true);
					sendMessage("&wYou may not place lava or fire within " + distance + " blocks of another player", player);
					return;
				}
			}
		}
	}
	
	/************************************************/
	/*					DAMAGE INDICATOR			*/
	/************************************************/

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerAttackEntity(EntityDamageByEntityEvent event) {
		if (settings.doDamageIndicator() == false) {
			return;
		}
		if (event.getEntity() instanceof Player) {
			if (settings.showPlayerHealth() == false) {
				return;
			}
		}
		if (event.getEntity() instanceof LivingEntity) {

			if (event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK) {
				return;
			}
			Player attacker;
			if (event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else if (event.getDamager() instanceof Projectile){
				if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
					attacker = (Player) ((Projectile) event.getDamager()).getShooter();
				} else {
					return;
				}
			} else {
				return;
			}
			LivingEntity victim = (LivingEntity) event.getEntity();
			
			String victimDisplay;
			if (victim instanceof Player) {
				victimDisplay = ((Player) victim).getName();
			} else {
				if (victim.getCustomName() == null) {
					victimDisplay = WbsStrings.capitalizeAll(victim.getName());
				} else {
					victimDisplay = victim.getCustomName();
				}
			}
			double damage = event.getFinalDamage();
			double maxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double health = victim.getHealth();
			ChatColor colour = ChatColor.GREEN;
			if (health > maxHealth) {
				return;
			}
			if (health-damage >= maxHealth*3/4) {
				colour = ChatColor.DARK_GREEN;
			} else if (health-damage >= maxHealth*2/4) {
				colour = ChatColor.GOLD;
			} else if (health-damage >= maxHealth/4) {
				colour = ChatColor.RED;
			} else {
				colour = ChatColor.DARK_RED;
			}
			
			double damageInHearts = (((double) Math.ceil(damage))/2);
			
			if (health-damage > 0) {
				sendActionBar("&7" + victimDisplay + "&7: &c-" + damageInHearts + "❤ &7(" + colour + (((double) Math.round(health-damage))/2) + "&7/&a" + (((double) Math.round(maxHealth))/2) + "&7)", attacker);
			} else {
				sendActionBar("&7" + victimDisplay + "&7: &c-" + damageInHearts + "❤  &7(&4Killed&7)", attacker);
			}
		}
	}
	
	/************************************************/
	/*					DISABLE EGG SPAWNER			*/
	/************************************************/
	
	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event) {
		if (!settings.blockSpawnerChange()) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack tool = player.getInventory().getItemInMainHand();
		
		if (event.getHand() == EquipmentSlot.HAND) {
			Action action = event.getAction();
			if (action == Action.RIGHT_CLICK_BLOCK) {
				if (event.getClickedBlock().getType() == Material.SPAWNER) {
					if (tool.getItemMeta() instanceof SpawnEggMeta) {
						event.setCancelled(true);
						sendMessage("&cChanging mob spawners is disabled.", player);
					}
				}
			}
		} else if (event.getHand() == EquipmentSlot.OFF_HAND) {
			Action action = event.getAction();
			tool = player.getInventory().getItemInOffHand();
			if (action == Action.RIGHT_CLICK_BLOCK) {
				if (event.getClickedBlock().getType() == Material.SPAWNER) {
					if (tool.getItemMeta() instanceof SpawnEggMeta) {
						event.setCancelled(true);
						sendMessage("&cChanging mob spawners is disabled.", player);
					}
				}
			}
		}
	}

	/************************************************/
	/*					DISABLE EGG SPAWNER			*/
	/************************************************/
	
}
