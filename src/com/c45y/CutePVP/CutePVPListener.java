package com.c45y.CutePVP;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ws.slide.minecraft.bukkit.servermessenger.NetworkServer;
import ws.slide.minecraft.bukkit.servermessenger.ServerMessengerPlugin;

public class CutePVPListener implements Listener {
	public CutePVPListener() {
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlot() == 39 /* Helmet slot */) {
			event.setCancelled(true);
		};
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (CutePVP.getInstance().getTeamManager().shouldTakeDamageFromBlock(block, player.getName())) {
			player.damage(1);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Team player_team = player.getTeam();

		player.setIsOnline(true);

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bo);
			out.writeUTF("PLAYER");
			out.writeUTF("JOIN");
			out.writeUTF(player.getName());
			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
				server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
		} catch (IOException e) { e.printStackTrace(); }

		if (player.isPortaling()) {
			if (player.getPortalingInventory() != null)
				player.getPlayer().getInventory().setContents(player.getPortalingInventory().getContents());
			if (player.getPortalingArmor() != null)
				player.getPlayer().getEquipment().setArmorContents(player.getPortalingArmor().getContents());
			if (player.getPortalingHealth() > 0)
				player.setHealth(player.getPortalingHealth());
			if (player.getPortalingHunger() > 0)
				player.setExhaustion(player.getPortalingHunger());
			player.teleport(player.getPortalingLocation());
			event.setJoinMessage(null);
			return;
		}
/*
		if (player_team == null)
			CutePVP.getInstance().getLogger().info("player_team == null");
*/
		if (player_team == null) {
			CutePVP.getInstance().getTeamManager().onFirstJoin(player);
			player_team = CutePVP.getInstance().getTeamManager().getTeamForPlayer(player.getName());

			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(bo);
				out.writeUTF("PLAYER");
				out.writeUTF("TEAM");
				out.writeUTF(player.getName());
				out.writeUTF(player_team.getName());
				for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
					server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
			} catch (IOException e) { e.printStackTrace(); }
		}

		if (player_team != null) {
			player.setDisplayName(player_team.encodeTeamColor(player.getName()));
			player_team.setHelmet(player);
			event.setJoinMessage(player.getDisplayName() + " joined the game.");
			CutePVP.getInstance().saveState();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());

		player.setIsOnline(false);

		if (player.isPortaling()) {
			event.setQuitMessage(null);
			return;
		}
		
		if (!player.hasPermission("cutepvp.modmode"))
			event.setQuitMessage(player.getDisplayName() + " left the game.");

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		try {
			out.writeUTF("PLAYER");
			out.writeUTF("QUIT");
			out.writeUTF(player.getName());
		} catch (IOException e) { e.printStackTrace(); }

		for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
			server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());

		player.setIsOnline(false);

		if (player.isPortaling()) {
			event.setQuitMessage(null);
			return;
		}

		Team flagOf = CutePVP.getInstance().getTeamManager().getTeamForFlagBearer(player);
		if (flagOf != null) {
			flagOf.dropFlagAtLocation(event.getPlayer().getLocation().getBlock().getLocation());
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerKick(PlayerKickEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Team flagOf = CutePVP.getInstance().getTeamManager().getTeamForFlagBearer(player);
		if (flagOf != null)
			flagOf.dropFlagAtLocation(event.getPlayer().getLocation().getBlock().getLocation());
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled= true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getEntity());
		Team flagOf = CutePVP.getInstance().getTeamManager().getTeamForFlagBearer(player);
		if (flagOf != null)
			flagOf.dropFlagAtLocation(event.getEntity().getLocation().getBlock().getLocation());

		if (event.getEntity().getKiller() instanceof Player)
			player.addKill();

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		try {
			out.writeUTF("PLAYER");
			out.writeUTF("DEATH");
			out.writeUTF(player.getName());
			out.writeUTF(event.getDeathMessage());
		} catch (IOException e) { e.printStackTrace(); }

		for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
			server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Team team = CutePVP.getInstance().getTeamManager().getTeam(player);
		if (team != null) {
			NetworkLocation home = team.getSpawn();
			if (home.getServer() == null) {
				team.setHelmet(player);
				event.setRespawnLocation(team.getSpawn().getLocation());
			}
			else {
				player.setIsPortaling(true);
	
				try {
					ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
					dataoutputstream.writeUTF("PLAYER");
					dataoutputstream.writeUTF("PORTAL");
					dataoutputstream.writeUTF(player.getName());
					dataoutputstream.writeInt((int)home.getLocation().getX());
					dataoutputstream.writeInt((int)home.getLocation().getY());
					dataoutputstream.writeInt((int)home.getLocation().getZ());
					dataoutputstream.writeUTF(home.getServer().getName());
					dataoutputstream.writeBoolean(false);
					dataoutputstream.writeUTF(InventorySerialization.toBase64(player.getInventory()));
					dataoutputstream.writeUTF(InventorySerialization.toBase64(InventorySerialization.getInventoryFromArray(player.getPlayer().getEquipment().getArmorContents())));
					dataoutputstream.writeInt(player.getHealth());
					dataoutputstream.writeInt((int) player.getExhaustion());
					for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
						server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
					}
				} catch (IOException e) { e.printStackTrace(); }
				
				try {
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(bo);
					out.writeUTF("Connect");
					out.writeUTF(home.getServer().getName());
					player.sendPluginMessage(CutePVP.getInstance(), "BungeeCord", bo.toByteArray());
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if ((event.getDamager() instanceof Player)) {
			Player attacker = (Player) event.getDamager();
			Player player = (Player) event.getEntity();

			Team attackerTeam = CutePVP.getInstance().getTeamManager().getTeamForPlayer(attacker.getName());
			if (attackerTeam.inTeam(player.getName()) || player.hasPermission("cutepvp.modmode")) {
				event.setCancelled(true);
			} else if(CutePVP.getInstance().getTeamManager().inRangeOfEnemyTeamSpawn(attacker)){
				attacker.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
				event.setCancelled(true);
			}
		}
		else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player shooter = (Player) arrow.getShooter();

				Team attackerTeam = CutePVP.getInstance().getTeamManager().getTeamForPlayer(shooter.getName());
				if (attackerTeam.inTeam(player.getName()) || player.hasPermission("cutepvp.modmode")) {
					event.setCancelled(true);
				} else if(CutePVP.getInstance().getTeamManager().inRangeOfEnemyTeamSpawn(shooter)){
					shooter.sendMessage(ChatColor.DARK_RED + "You cannot attack within another teams base");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());

//		player.sendMessage("Player Interact");

		if (player.hasPermission("cutepvp.modmode"))
			return;

		Block block = event.getClickedBlock();

		Team team_block = CutePVP.getInstance().getTeamManager().getTeam(block);
		Team team_attacking = player.getTeam();
		Team team_carrying = CutePVP.getInstance().getTeamManager().getTeamForFlagBearer(player);
		Team team_flag = CutePVP.getInstance().getTeamManager().getTeamFromFlagLocation(block.getLocation());
		ItemStack item = event.getItem();
//		ItemStack server_block = CutePVP.getInstance().getServerItemStack(Bukkit.getServerName());
		NetworkServer server_block = CutePVP.getInstance().getNetworkServer(block);
/*
		if (team_block != null)
			player.sendMessage("1 team_block != null");
		if (server_block != null)
			player.sendMessage("1 server_block != null");
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			player.sendMessage("4 event.getAction().equals(Action.RIGHT_CLICK_BLOCK)");
		if (item != null)
			player.sendMessage("5 item != null");
		if (item.getType() == Material.FLINT_AND_STEEL)
			player.sendMessage("6 item.getType() == Material.FLINT_AND_STEEL");
		if (event.getBlockFace() == BlockFace.UP)
			player.sendMessage("7 event.getBlockFace() == BlockFace.UP");
*/
		if (
				server_block != null &&
				event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
				item != null &&
				item.getType() == Material.FLINT_AND_STEEL &&
				event.getBlockFace() == BlockFace.UP &&
				!server_block.getName().equalsIgnoreCase("Lobby")
		) {
//			player.sendMessage("Attempting to light portal");
//			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
				ItemStack network_server_block = CutePVP.getInstance().getServerItemStack(server_block.getName());
				if (network_server_block != null) {
					Set<Block> portal_blocks = CutePVP.getInstance().isPortalFrame(block, network_server_block);
					if (portal_blocks != null)
						for (Block portal_block : portal_blocks)
							portal_block.setTypeId(Material.PORTAL.getId(), false);
				}
//			}
		}
/*
		if (team_block == null)
			player.sendMessage("team_block == null");
		if (team_attacking == null)
			player.sendMessage("team_attacking == null");
		if (!team_block.isFlagAtLocation(block.getLocation()))
			player.sendMessage("!team_block.isFlagAtLocation(block.getLocation())");
*/
		if (team_block == null || team_attacking == null || !team_block.isFlagAtLocation(block.getLocation()))
			return;

		if (team_carrying != null && team_attacking.isTeamBlock(block)) {
			player.addScore();
			team_attacking.addScore();

			team_carrying.respawnFlag();
			team_carrying.removeCarrier();

			CutePVP.getInstance().getServer().broadcastMessage(player.getDisplayName() + " captured the " + team_carrying.getName() + " flag.");
			CutePVP.getInstance().saveState();

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bo);
			try {
				out.writeUTF("FLAG");
				out.writeUTF("CAPTURE");
				out.writeUTF(team_carrying.getName());
				out.writeUTF(player.getName());
			} catch (IOException e) { e.printStackTrace(); }

			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
				server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());

			return;
		}

		if (team_attacking == team_block && team_block.isFlagAtLocation(block.getLocation())) {
			if (team_block.isFlagAtLocation(team_block.getFlagHomeLocation()))
				return;

			team_block.respawnFlag();
			block.setType(Material.AIR);

			CutePVP.getInstance().getServer().broadcastMessage(player.getDisplayName() + " returned the " + team_block.getName() + " flag.");
			CutePVP.getInstance().saveState();

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bo);
			try {
				out.writeUTF("FLAG");
				out.writeUTF("RETURN");
				out.writeUTF(team_block.getName());
				out.writeUTF(player.getName());
			} catch (IOException e) { e.printStackTrace(); }

			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
				server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
			
			return;
		}
/*
		if (team_flag != null)
			player.sendMessage("1 team_flag != null");
		if (team_carrying == null)
			player.sendMessage("2 team_carrying == null");
		if (team_block != team_attacking)
			player.sendMessage("3 team_block != team_attacking");
*/
		if (team_flag != null && team_carrying == null && team_block != team_attacking) {
			team_block.setCarrier(player);
			block.setType(Material.AIR);

			CutePVP.getInstance().getServer().broadcastMessage(player.getDisplayName() + " has stolen the " + team_block.getName() + " flag.");
			CutePVP.getInstance().saveState();

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bo);
			try {
				out.writeUTF("FLAG");
				out.writeUTF("STEAL");
				out.writeUTF(team_block.getName());
				out.writeUTF(player.getName());
			} catch (IOException e) { e.printStackTrace(); }

			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
				server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());

			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());

		if (player.hasPermission("cutepvp.modmode"))
			return;

		if (CutePVP.getInstance().getTeamManager().inOwnTeamBase(player))
			return;

		Block block = event.getBlock();

		Team team_attacking = CutePVP.getInstance().getTeamManager().getTeam(player);
		Team team_carrying = CutePVP.getInstance().getTeamManager().getTeamForFlagBearer(player);
		Team team_block = CutePVP.getInstance().getTeamManager().getTeam(block);
/*
		if (team_block != null)
			player.sendMessage("1 team_block != null");
		if (team_block == team_attacking)
			player.sendMessage("2 team_block == team_attacking");
		if (CutePVP.getInstance().getKOTHLocation().getServer() == null)
			player.sendMessage("3 CutePVP.getInstance().getKOTHLocation().getServer() == null");
		if (CutePVP.getInstance().getKOTHLocation().getLocation().equals(block.getLocation()))
			player.sendMessage("4 CutePVP.getInstance().getKOTHLocation().getLocation().equals(block.getLocation())");
		player.sendMessage("5 x=" + CutePVP.getInstance().getKOTHLocation().getLocation().getX() + " y=" + CutePVP.getInstance().getKOTHLocation().getLocation().getY() + " z=" + CutePVP.getInstance().getKOTHLocation().getLocation().getZ());
		player.sendMessage("6 x=" + block.getLocation().getX() + " y=" + block.getY() + " z=" + block.getZ());
*/
		if (
				team_block != null &&
				team_block == team_attacking &&
				CutePVP.getInstance().getKOTHLocation().getServer() == null &&
				CutePVP.getInstance().getKOTHLocation().getLocation().equals(block.getLocation())
		) {
			CutePVP.getInstance().getServer().broadcastMessage(ChatColor.DARK_PURPLE + "[NOTICE] " + team_block.getName() + " gets buff!");

			for (Player player_buff : team_block.getPlayer()) {
				if (player_buff.getPlayer() != null) {
					player_buff.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
					player_buff.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1));
				}
			}

			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(bo);
				out.writeUTF("KOTH");
				out.writeUTF(team_block.getName());
				for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
					server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
			} catch (IOException e) { e.printStackTrace(); }

			return;
		}

		Team team_flag = CutePVP.getInstance().getTeamManager().getTeamFromFlagLocation(block.getLocation());
		if (team_flag != null && team_flag == team_attacking) {
			event.setCancelled(true);
		}

		if (team_flag != null && team_flag != team_attacking && team_flag == team_carrying) {
			team_attacking.addScore();
			player.addScore();

			team_carrying.respawnFlag();
			team_carrying.removeCarrier();

			CutePVP.getInstance().getServer().broadcastMessage(player.getDisplayName() + " captured the " + team_carrying.getName() + " flag.");
			CutePVP.getInstance().saveState();

			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(bo);
				out.writeUTF("FLAG");
				out.writeUTF("CAPTURE");
				out.writeUTF(team_carrying.getName());
				out.writeUTF(player.getName());
				for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers())
					server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bo.toByteArray());
			} catch (IOException e) { e.printStackTrace(); }

			
			return;
		}

		if (CutePVP.getInstance().getTeamManager().inRangeOfEnemyTeamSpawn(player)) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Team team = CutePVP.getInstance().getTeamManager().getTeamFromFlagLocation(event.getBlock().getLocation());

		if (team != null)
			event.setCancelled(true);

		if (player.hasPermission("cutepvp.modmode"))
			return;

		if (CutePVP.getInstance().getTeamManager().inOwnTeamBase(player))
			return;

		//If they're in an enemy base...
		if (CutePVP.getInstance().getTeamManager().inRangeOfEnemyTeamSpawn(player)) {
			player.sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		Team playerTeam = CutePVP.getInstance().getTeamManager().getTeamForPlayer(player.getName());

		CutePVP.getInstance().getLogger().info(player.getName() + ": " + ChatColor.stripColor(event.getMessage()));

		event.setCancelled(true);

		if (playerTeam != null)
			playerTeam.message("<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage()));

		try {
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
			dataoutputstream.writeUTF("CHAT");
			dataoutputstream.writeUTF(player.getName());
			dataoutputstream.writeUTF(event.getMessage());
			for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
//				CutePVP.getInstance().getLogger().info("sending chat to " + server.getName());
				server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
/*
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		
	}
*/
	/*
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());
		player.sendMessage("Teleporting via " + event.getCause());
		CutePVP.getInstance().getLogger().info("Teleporting " + player + " via " + event.getCause());

		if (event.getCause() == TeleportCause.UNKNOWN) {
			Location from = event.getFrom();
			event.setTo(from.add(0, 5, 0));
			return;

			for (Team team : CutePVP.getInstance().getTeamManager().getTeams().values()) {
				if (from.getBlock().getRelative(BlockFace.DOWN).getType() == team.getItemStack().getType()) {
					event.setTo(from.add(0, 5, 0));

					return;
				}
			}

		}
	}
*/
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
//		CutePVP.getInstance().getLogger().info("Portal event: " + event.getCause());

		boolean create_portal = true;
		Player player = CutePVP.getInstance().getPlayer(event.getPlayer());

		if (event.getCause() == TeleportCause.NETHER_PORTAL) {
			Location from = event.getFrom();

			// WTF WHY IS IT TRIGGERING INSTANTLY BEFORE EVEN IN PORTAL
			if (from.getBlock().getTypeId() == 0) {
				Block from_block = from.getBlock().getRelative(BlockFace.NORTH);
				if (from_block.getTypeId() == 90)
					from = from_block.getLocation();
				from_block = from.getBlock().getRelative(BlockFace.EAST);
				if (from_block.getTypeId() == 90)
					from = from_block.getLocation();
				from_block = from.getBlock().getRelative(BlockFace.SOUTH);
				if (from_block.getTypeId() == 90)
					from = from_block.getLocation();
				from_block = from.getBlock().getRelative(BlockFace.WEST);
				if (from_block.getTypeId() == 90)
					from = from_block.getLocation();
			}

			Location to = from;

//			CutePVP.getInstance().getLogger().info("Trying to find out what server this portal is for!");
			NetworkServer server_to = CutePVP.getInstance().getNetworkServer(from.getBlock().getRelative(BlockFace.DOWN));

			if (server_to != null) {
				if (Bukkit.getServerName().equalsIgnoreCase("Lobby")) {
//					CutePVP.getInstance().getLogger().info("In lobby");
					if (!player.getTeam().getName().equalsIgnoreCase(server_to.getName())) {
//						CutePVP.getInstance().getLogger().info("Player team != Portal Color");
						event.setCancelled(true);
						return;
					}
					else {
						to = player.getTeam().getSpawnLocation();
						create_portal = false;
					}
				}

				Set<Block> portal_blocks = CutePVP.getInstance().isPortalFrame(from.getBlock().getRelative(BlockFace.DOWN), CutePVP.getInstance().getServerItemStack(server_to.getName()));
				if (portal_blocks != null) {
//					player.sendMessage("Teleporting to " + server_to.getName() + " server");
					player.setIsPortaling(true);

					try {
						ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
						DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
						dataoutputstream.writeUTF("PLAYER");
						dataoutputstream.writeUTF("PORTAL");
						dataoutputstream.writeUTF(player.getName());
						dataoutputstream.writeInt((int)to.getX());
						dataoutputstream.writeInt((int)to.getY());
						dataoutputstream.writeInt((int)to.getZ());
						dataoutputstream.writeUTF(server_to.getName());
						dataoutputstream.writeBoolean(create_portal);
						dataoutputstream.writeUTF(InventorySerialization.toBase64(player.getInventory()));
						dataoutputstream.writeUTF(InventorySerialization.toBase64(InventorySerialization.getInventoryFromArray(player.getPlayer().getEquipment().getArmorContents())));
						dataoutputstream.writeInt(player.getHealth());
						dataoutputstream.writeInt((int) player.getExhaustion());
						for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
							server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
						}
					} catch (IOException e) { e.printStackTrace(); }
					
					try {
						ByteArrayOutputStream bo = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(bo);
						out.writeUTF("Connect");
						out.writeUTF(server_to.getName());
						player.sendPluginMessage(CutePVP.getInstance(), "BungeeCord", bo.toByteArray());
						event.setCancelled(true);
					} catch (IOException e) { e.printStackTrace(); }

					return;
				}
/*
				if (from.getBlock().getRelative(BlockFace.DOWN).getType() == team.getItemStack().getType()) {
					event.setTo(from.add(0, 5, 0));

					return;
				}
*/
			}
		}
	}
}
