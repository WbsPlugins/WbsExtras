package wbs.extras.listeners;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsStrings;

public class MiscListener extends WbsMessenger implements Listener {

	private ExtrasSettings settings;
	public MiscListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
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
		
		if (event.getBedEnterResult().equals(BedEnterResult.OK)) {
			Player player = event.getPlayer();
			int amountSleeping = 1;
			double playersNeeded = 0;
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			for (Player p : players) {
				if (p.getGameMode() == GameMode.SURVIVAL) {
					playersNeeded++;
					if (p.equals(player) ) {
						continue;
					}
					if (p.isSleeping()) {
						amountSleeping++;
					}
				}
			}

			int sleepPercent = settings.getSleepPercent();
			
			if (amountSleeping >= playersNeeded/100*(sleepPercent)) {
				
				broadcast(ChatColor.GOLD + player.getName() + " went to sleep. Good night everybody!");
				World world = Bukkit.getWorld("spawn");
				new BukkitRunnable() {
					int escape = 0;
					@Override
		            public void run() {
						world.setTime(world.getTime() + 50 % 24000);
						if (world.getTime() < 12000 || escape > 1000 || !player.isSleeping()) {
							this.cancel();
							world.setStorm(false);
						}
						escape++;
		            }
		        }.runTaskTimer(plugin, 1L, 1L);
		        
			} else {
				String display;
				if (amountSleeping == 1) {
					display = ChatColor.GOLD + player.getName() + " wants to sleep. " + sleepPercent + "% of players must be in a bed to skip the night. (1/" + ((int) playersNeeded) + ")";
				} else {
					display = ChatColor.GOLD + player.getName() + " wants to sleep. (" + amountSleeping + "/" + ((int) playersNeeded) + ")";
				}
				broadcast(display);
			}
		}
	}
	
	@EventHandler
	public void onLeaveBed(PlayerBedLeaveEvent event) {
		if (!settings.doVoteSleep()) {
			return;
		}
		
		Player player = event.getPlayer();
		int amountSleeping = 0;
		double playersNeeded = 0;
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for (Player p : players) {
			if (p.getGameMode() == GameMode.SURVIVAL) {
				playersNeeded++;
				if (p.equals(player) ) {
					continue;
				}
				if (p.isSleeping()) {
					amountSleeping++;
				}
			}
		}
		
		broadcast(ChatColor.GOLD + player.getName() + " left their bed. (" + amountSleeping + "/" + ((int) playersNeeded) + ")");

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
				for (Player check : Bukkit.getOnlinePlayers()) {
					if (check.equals(player)) {
						continue;
					}
					if (event.getClickedBlock().getWorld().equals(check.getWorld())) {
					//	if (event.getClickedBlock().getLocation().distance(check.getLocation()) <= ASMain.lavaFireThreshold) {
					//		event.setCancelled(true);
					//		WbsExtras.sendMessage("&4You may not place lava or fire within " + ASMain.lavaFireThreshold + " blocks of another player", player);
					//		return;
					//	}
					}
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
		if (settings.blockSpawnerChange() == false) {
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
