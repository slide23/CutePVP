package com.c45y.CutePVP;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ws.slide.minecraft.bukkit.servermessenger.NetworkServer;
import ws.slide.minecraft.bukkit.servermessenger.ServerMessengerPlugin;

public class CutePVP extends JavaPlugin {
	public static CutePVP instance;

	public static CutePVP getInstance() {
		return instance;
	}

	public CutePVP() {
		instance = this;
	}

	private final CutePVPListener listener = new CutePVPListener();
	private YamlConfiguration state_config;
	private TeamManager team_manager;
	private Map<String, Player> players = new HashMap<String, Player>();
	private Map<String, String> server_blocks = new HashMap<String, String>();
	private WorldGuardPlugin wg_plugin = null;
	private NetworkLocation koth_location = null;

	@Override
	public void onEnable() {
		File config_file = new File(getDataFolder(), "config.yml");
		if (!config_file.exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		ServerMessengerPlugin.getMessenger().registerOutgoingPluginChannel(this, "CutePVP");
		ServerMessengerPlugin.getMessenger().registerIncomingPluginChannel(this, "CutePVP", new ServerMessageListener());

		team_manager = new TeamManager();

		loadTeams();
		loadServerBlocks();
		loadKOTH();
		loadState();

		// Register event listener
		PluginManager plugin_manager = getServer().getPluginManager();
		plugin_manager.registerEvents(listener, this);

		// Repeating events
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				runBuff();
				respawnFlags();
			}
		}, 1200, 12000);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Team team : team_manager.getTeams().values())
					if (team.getCarrier() != null)
						team.setFlagLocation(team.getCarrier().getLocation());

				for (Team team : team_manager.getTeams().values())
					team.setCompassTarget();

				saveState();
			}
		}, 5 * 20, 5 * 20);

		getLogger().info(this.toString() + " enabled");
	}

	@Override
	public void onDisable() {
		saveState();

		for (Team team : this.team_manager.getTeams().values()) {
			Player carrier = team.getCarrier();
			if (carrier != null)
				team.dropFlagAtLocation(carrier.getLocation().getBlock().getLocation());

			team.removeCarrier();
		}

		getLogger().info(this.toString() + " disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!(sender instanceof org.bukkit.entity.Player)) {
			sender.sendMessage("Sorry, this plugin cannot be used from console");
			return true;
		}

		Player player = getPlayer((org.bukkit.entity.Player) sender);
		if (command.getName().equalsIgnoreCase("g")) {
			String message = StringUtils.join(args, " ");
			for (org.bukkit.entity.Player playeri : getServer().getOnlinePlayers()) {
				playeri.sendMessage(ChatColor.RED + ">" + ChatColor.BLUE + ">" +  ChatColor.WHITE + " <" + team_manager.getTeamForPlayer(sender.getName()).encodeTeamColor(sender.getName()) + "> " + message);
			}

			try {
				ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
				DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
				dataoutputstream.writeUTF("BROADCAST");
				dataoutputstream.writeUTF(player.getName());
				dataoutputstream.writeUTF(message);
				for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
					server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
				}
			} catch (IOException e) { e.printStackTrace(); }

			return true;
		} else if (command.getName().equalsIgnoreCase("score")) {
			StringBuilder msg = new StringBuilder();
			msg.append("Score:");
			for (Team team : team_manager.getTeams().values()) {
				msg.append(" " + team.encodeTeamColor(team.getName() + ": " + team.getScore()));
			}
			sender.sendMessage(msg.toString());
			return true;
		} else if (command.getName().equalsIgnoreCase("drop")) {
			Team flagOf = team_manager.getTeamForFlagBearer(player);
			if (flagOf != null) {
				flagOf.dropFlagAtLocation(player.getLocation().getBlock().getLocation());
				getServer().broadcastMessage(player.getDisplayName() + " dropped the " + flagOf.getName() + " flag.");

				try {
					ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
					dataoutputstream.writeUTF("FLAG");
					dataoutputstream.writeUTF("DROP");
					dataoutputstream.writeUTF(flagOf.getName());
					dataoutputstream.writeUTF(player.getName());
					for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
						server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
					}
				} catch (IOException e) { e.printStackTrace(); }
			}
			else
				sender.sendMessage("You do not have the flag!");
			return true;
		} else if (command.getName().equalsIgnoreCase("flag")) {
			Team team;
			if (args.length == 1 && player.hasPermission("CutePVP.mod"))
				team = team_manager.getTeam(args[0]);
			else
				team = team_manager.getTeam(player);

			if (team == null) {
				sender.sendMessage("[CutePVP] Unknown Team");
				return true;
			}

			Location flag_location = team.getFlagLocation();
			if (flag_location.equals(team.getFlagHomeLocation()))
				sender.sendMessage(team.getName() + " flag Location: Home");
			else
				sender.sendMessage(team.getName() + " flag Location: " + (int)flag_location.getX() + ", " + (int)flag_location.getY() + ", " + (int)flag_location.getZ());
			return true;
		} else if (command.getName().equalsIgnoreCase("list")) {
			for (Team team : team_manager.getTeams().values()) {
				StringBuilder msg = new StringBuilder();
				msg.append(team.getName() + " Team:");
				for (String playerName : team.getMembersOnline()) {
					msg.append(" " + playerName);
				}
				sender.sendMessage(msg.toString());
			}

			return true;
		} else if (command.getName().equalsIgnoreCase("ctf")) {
			if (args.length == 1 && new String("mod").equalsIgnoreCase(args[0])) {
				if (player.hasPermission("cutepvp.modmode")) {
					player.addAttachment(this, "cutepvp.modmode", false);
					player.setGameMode(GameMode.SURVIVAL);
					sender.sendMessage("[CutePVP] Left modmode");
				} else {
					player.addAttachment(this, "cutepvp.modmode", true);
					player.setGameMode(GameMode.CREATIVE);

					Team flagOf = team_manager.getTeamForFlagBearer(player);
					if (flagOf != null)
						flagOf.dropFlagAtLocation(player.getLocation().getBlock().getLocation());

					sender.sendMessage("[CutePVP] In modmode");
				}
				return true;
			} else if (new String("setteam").equalsIgnoreCase(args[0])) {
				if (args.length == 2) { // Remove player from team
					Team team = team_manager.getTeamForPlayer(args[1]);
					if (team != null) {
						team.removePlayer(args[1]);
						sender.sendMessage("[CutePVP] Removing " + args[1] + " from " + team.getName() + " team");
					}
					else
						sender.sendMessage("[CutePVP] Player " + args[1] + " not on any team");
				} else if (args.length == 3) { // Move them to team
					Team team = team_manager.getTeamForPlayer(args[1]);
					if (team != null) {
						if (team.getName().equalsIgnoreCase(args[2])){
							sender.sendMessage("[CutePVP] Player " + args[1] + " already on " + args[2] + " team");
							return true;
						}

						Team team_new = team_manager.getTeam(args[2]);
						if (team_new != null) {
							team.removePlayer(args[1]);
							team_new.addPlayer(args[1]);
							sender.sendMessage("[CutePVP] Moved " + args[1] + " from " + team.getName() + " team to " + args[2] + " team");
							return true;
						}
					}
					else {
						team = team_manager.getTeam(args[2]);
						if (team != null) {
							team.addPlayer(args[1]);
							sender.sendMessage("[CutePVP] Added " + args[1] + " to " + team.getName() + " team");
							return true;
						}
						else {
							sender.sendMessage("[CutePVP] Unknown team named " + args[2]);
							return true;
						}
					}

					try {
						ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
						DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
						dataoutputstream.writeUTF("TEAM");
						dataoutputstream.writeUTF("RMPLAYER");
						dataoutputstream.writeUTF(player.getName());
						for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
							server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
						}
					} catch (IOException e) { e.printStackTrace(); }

					try {
						ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
						DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
						dataoutputstream.writeUTF("PLAYER");
						dataoutputstream.writeUTF("TEAM");
						dataoutputstream.writeUTF(player.getName());
						dataoutputstream.writeUTF(team.getName());
						for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
							server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
						}
					} catch (IOException e) { e.printStackTrace(); }
				}
			}
		} else if (command.getName().equalsIgnoreCase("cutepvp")) {
			if (args.length == 1 && new String("reload").equals(args[0])) {
				reload();
				sender.sendMessage("[CutePVP] Reloaded");
				return true;
			}
			else if (args.length == 1 && new String("save").equals(args[0])) {
				saveConfig();
				saveState();
				sender.sendMessage("[CutePVP] Saved");
				return true;
			}
			else if (args.length == 1 && new String("runbuff").equals(args[0])) {
				sender.sendMessage("[CutePVP] Running buff");
				runBuff();
				return true;
			}
			else if (args.length == 1 && new String("respawnflags").equals(args[0])) {
				sender.sendMessage("[CutePVP] Respawning flags");
				respawnFlags();
				return true;
			}
			else if (args.length == 3 && args[0].equalsIgnoreCase("setblock")) {
				String team_name = args[1];
				String block_name = args[2];

				Team team = team_manager.getTeam(team_name);
				if (team != null) {
					team.setBlock(stringToItemStack(block_name));
					sender.sendMessage("[CutePVP] Set " + team_name + " teams block to " + block_name);
				}
				else
					sender.sendMessage("[CutePVP] Unknwn team");

				return true;
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("setbuff")) {
				Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
				player.sendMessage("Set to: " + block.getType().name());
				getConfig().set("koth.x", block.getLocation().getX());
				getConfig().set("koth.y", block.getLocation().getY());
				getConfig().set("koth.z", block.getLocation().getZ());
				saveConfig();
			}
			else if (args.length == 2 && new String("rmplayer").equals(args[0])) {
				String playerName = args[1];
				Team team = team_manager.getTeamForPlayer(playerName);
				if (team != null) {
					team.removePlayer(playerName);
					sender.sendMessage("[CutePVP] Removed Player");
				} else
					sender.sendMessage("[CutePVP] No player on any team");

				try {
					ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
					dataoutputstream.writeUTF("TEAM");
					dataoutputstream.writeUTF("RMPLAYER");
					dataoutputstream.writeUTF(player.getName());
					for (NetworkServer server : ServerMessengerPlugin.getInstance().getServers()) {
						server.sendPluginMessage(CutePVP.getInstance(), "CutePVP", bytearrayoutputstream.toByteArray());
					}
				} catch (IOException e) { e.printStackTrace(); }

				return true;
			}
			else if (args.length == 2 && new String("setspawn").equals(args[0])) {
				Team team = team_manager.getTeam(args[1]);
				if (team == null)
					sender.sendMessage("[CutePVP] Unknown team");

				team.setSpawn(player.getLocation());

				sender.sendMessage("[CutePVP] Set Spawn for " + args[0] + " team");
				return true;
			}
			else if (args.length == 2 && new String("setflag").equals(args[0])) {
				Team team = team_manager.getTeam(args[1]);
				if (team == null)
					sender.sendMessage("[CutePVP] Unknown team");
				else {
					team.setFlagLocation(player.getTargetBlock(null, 50).getLocation());
					sender.sendMessage("[CutePVP] Set flag loc for " + args[1] + " team");
				}

				return true;
			}
			else if (args.length == 2 && new String("setflaghome").equals(args[0])) {
				Team team = team_manager.getTeam(args[1]);
				if (team == null)
					sender.sendMessage("[CutePVP] Unknown team");
				else {
					team.setFlagHomeLocation(player.getTargetBlock(null, 50).getLocation());
					sender.sendMessage("[CutePVP] Set flag home loc for " + args[1] + " team");
				}
				return true;
			}
			else if (args.length == 1 && new String("teams").equals(args[0])) {
				StringBuilder msg = new StringBuilder();
				msg.append("[CutePVP] Online:");
				for (Team team : team_manager.getTeams().values()) {
					msg.append(" " + team.getName() + "=" + team.getMembersOnline().size());
				}
				sender.sendMessage(msg.toString());

				msg = new StringBuilder();
				msg.append("[CutePVP] Total:");
				for (Team team : team_manager.getTeams().values()) {
					msg.append(" " + team.getName() + "=" + team.getPlayerNames().size());
				}
				sender.sendMessage(msg.toString());

				return true;
			}
		}

		return false;
	}

	public void reload() {
		team_manager = new TeamManager();

		reloadConfig();
		loadTeams();

		reloadState();
		loadState();
	}

	public void runBuff() {
//		getLogger().info("Running buff");
		Location powerblock = new Location(
			getServer().getWorlds().get(0),
			getConfig().getDouble("block.buff.x"),
			getConfig().getDouble("block.buff.y"),
			getConfig().getDouble("block.buff.z")
		);

		Block gPowerBlock = getServer().getWorlds().get(0).getBlockAt(powerblock);
		if (gPowerBlock != null) {
			Team team = team_manager.getTeam(gPowerBlock);
			if (team != null) {
				getServer().broadcastMessage(ChatColor.DARK_PURPLE + "[NOTICE] " + team.getName() + " gets buff!");
				for (org.bukkit.entity.Player playeri : getServer().getOnlinePlayers()) {
					if (team.inTeam(playeri.getName())) {
						playeri.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
						playeri.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1));
					}
				}
			}
		}

//		getLogger().info("End running buff");
	}

	public void respawnFlags() {
		for (Team team : team_manager.getTeams().values())
			if (team.getCarrier() == null)
				team.respawnFlag();
	}

	public void loadTeams() {
		if (getConfig().contains("teams")) {
			getLogger().info("Loading teams");
			World world = getServer().getWorlds().get(0);
			Map<String, Object> team_names = getConfig().getConfigurationSection("teams").getValues(false);
			for (String team_name : team_names.keySet()) {
				String block_info = getConfig().getString("teams." + team_name + ".block");
				ChatColor color = ChatColor.valueOf(getConfig().getString("teams." + team_name + ".color").toUpperCase());

				Team team = new Team(team_name, block_info, color);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> flag_locations = (List<Map<String, Object>>) getConfig().getList("teams." + team_name + ".flag");
				if (flag_locations != null) {
					for (Map<String, Object> flag_location_map : flag_locations) {
						double x = (Double) flag_location_map.get("x");
						double y = (Double) flag_location_map.get("y");
						double z = (Double) flag_location_map.get("z");
						String server_name = (String) flag_location_map.get("server");
	
						getLogger().info("Flag Home: " + server_name + "@ x=" + x + ", z=" + z + ", y=" + y);

						NetworkServer server = ServerMessengerPlugin.getInstance().getServer(server_name);
/*
						getLogger().info(team.getName() + " team flag home on server " + server_name);
						if (server != null)
							getLogger().info(team.getName() + " team flag home REALLY on server " + server_name);
*/
						NetworkLocation flag_location = new NetworkLocation(server, world, x, y, z);
	
						team.addFlagHomeNetworkLocation(flag_location);
					}
				}

				if (team.getFlagHomeLocations().size() == 0)
					team.addFlagHomeNetworkLocation(new NetworkLocation(null, world, 0, 62, 0));

				double x = getConfig().getDouble("teams." + team_name + ".spawn.x", 0);
				double y = getConfig().getDouble("teams." + team_name + ".spawn.y", 64);
				double z = getConfig().getDouble("teams." + team_name + ".spawn.z", 0);
				float yaw = (float) getConfig().getDouble("teams." + team_name + ".spawn.yaw", 0);
				float pitch = (float) getConfig().getDouble("teams." + team_name + ".spawn.pitch", 0);
				String server_name = getConfig().getString("teams." + team_name + ".spawn.server");

				NetworkServer server = ServerMessengerPlugin.getInstance().getServer(server_name);

				team.setSpawn(new NetworkLocation(server, world, x, y, z, yaw, pitch));

				team_manager.addTeam(team_name, team);
			}
		}
		else
			getLogger().severe("No teams configured!");
	}

	public void loadServerBlocks() {
		if (getConfig().contains("servers")) {
			getLogger().info("Loading server blocks");

			Map<String, Object> servers = getConfig().getConfigurationSection("servers").getValues(false);
			for (String server_name : servers.keySet()) {
				String block = (String) servers.get(server_name);

				this.server_blocks.put(server_name, block);
			}
		}
		else
			getLogger().severe("No servers configured!");
	}

	public void loadKOTH() {
		if (getConfig().contains("KOTH")) {
			getLogger().info("Loading KOTH location");

			double x = getConfig().getDouble("KOTH.x");
			double y = getConfig().getDouble("KOTH.y");
			double z = getConfig().getDouble("KOTH.z");
			String server_name = getConfig().getString("KOTH.server");

//			getLogger().info(server_name + " @x=" + x + ", y=" + y + ", z" + z);
			
			this.koth_location = new NetworkLocation(ServerMessengerPlugin.getInstance().getServer(server_name), getServer().getWorlds().get(0), x, y, z);
		}
		else
			getLogger().severe("No servers configured!");
	}

	public void loadState() {
		state_config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "state.yml"));

		StringBuilder msg = new StringBuilder();
		for (Team team : team_manager.getTeams().values()) {
			ConfigurationSection player_section = getState().getConfigurationSection("teams." + team.getName() + ".players");
			if (player_section != null) {
				Set<String> player_names = getState().getConfigurationSection("teams." + team.getName() + ".players").getKeys(false);
				for (String player_name : player_names) {
					int score = getState().getInt("teams." + team.getName() + ".players." + player_name + ".score");
					int kills = getState().getInt("teams." + team.getName() + ".players." + player_name + ".kills");

					Player player = new Player(player_name, getServer().getPlayer(player_name), score, kills);
					players.put(player_name, player);

					team.addPlayer(player);

//					getLogger().info("Added " + player_name + " to " + team.getName() + " team");
				}

				msg.append(team.getName() + ":" + player_names.size() + " ");
			}
			else {
//				getLogger().info("Player section for " + team.getName() + " is empty");
				msg.append(team.getName() + ": 0 ");
			}

			team.setFlagHomeNetworkLocation(team.getRandomFlagHomeLocation());

			if (getState().isSet("teams." + team.getName() + ".flag.x")) {
				double x = getState().getDouble("teams." + team.getName() + ".flag.x");
				double y = getState().getDouble("teams." + team.getName() + ".flag.y");
				double z = getState().getDouble("teams." + team.getName() + ".flag.z");
				String server_name = getState().getString("teams." + team.getName() + ".flag.server");

				NetworkServer server = ServerMessengerPlugin.getInstance().getServer(server_name);
/*
				getLogger().info(team.getName() + " team flag on server " + server_name);
				if (server != null)
					getLogger().info(team.getName() + " team flag REALLY on server " + server_name);
*/
				team.setFlagNetworkLocation(new NetworkLocation(server, getServer().getWorlds().get(0), x, y, z));
			}
			else
				team.setFlagNetworkLocation(team.getFlagHomeNetworkLocation());

			int score = getState().getInt("teams." + team.getName() + ".score", 0);
			int kills = getState().getInt("teams." + team.getName() + ".score", 0);
			
			team.setScore(score);
			team.setKills(kills);
		}

		getLogger().info(msg.toString());
	}

	public void reloadState() {
		
	}

	public YamlConfiguration getState() {
		return state_config;
	}

	public void saveState() {
		for (Team team : team_manager.getTeams().values()) {
			for (Player player : team.getPlayers().values()) {
				getState().set("teams." + team.getName() + ".players." + player.getName() + ".score", player.getScore());
				getState().set("teams." + team.getName() + ".players." + player.getName() + ".kills", player.getKills());
			}
		}

		try {
			state_config.save(new File(getDataFolder(), "state.yml"));
		} catch (IOException e) { e.printStackTrace(); }
	}

	public TeamManager getTeamManager() {
		return team_manager;
	}

	public Player getPlayer(String player_name) {
		Player player = players.get(player_name);

		if (player == null) {
			org.bukkit.entity.Player bukkit_player = getServer().getPlayer(player_name);
			if (bukkit_player != null) {
				player = getPlayer(bukkit_player);
			} else {
				player = new Player(player_name, null);
				this.players.put(player_name, player);
			}
		}

		return player;
	}

	public Player getPlayer(org.bukkit.entity.Player bukkit_player) {
		if (bukkit_player == null)
			return null;

		for (Player player : players.values()) {
//			bukkit_player.sendMessage(player.getName() + " =? " + bukkit_player.getName());
			if (player.getName().equals(bukkit_player.getName())) {
//				bukkit_player.sendMessage("Found stored player " + player.getName());

				player.setPlayer(bukkit_player);

				return player;
			}
		}

		Player player = new Player(bukkit_player.getName(), bukkit_player);
		players.put(player.getName(), player);

		return player;
	}

	public String getServerBlock(String server_name) {
		return this.server_blocks.get(server_name);
	}

	public ItemStack getServerItemStack(String server_name) {
		return stringToItemStack(getServerBlock(server_name));
	}

	public NetworkServer getNetworkServer(Block block) {
		for (String server_name : this.server_blocks.keySet()) {
			NetworkServer server = ServerMessengerPlugin.getInstance().getServer(server_name);
			if (server != null) {
//				getLogger().info(server.getName() + " looking up block " + this.server_blocks.get(server_name));
				ItemStack server_block = stringToItemStack(this.server_blocks.get(server_name));
				if (server_block != null) {
//					getLogger().info(server.getName() + " found block");
//					getLogger().info(server.getName() + " found block");
//					getLogger().info(block.getTypeId() + " == " + server_block.getTypeId());
//					getLogger().info(block.getData() + " == " + server_block.getData().getData());
					if (block.getTypeId() == server_block.getTypeId() && block.getData() == server_block.getData().getData()) {
//						getLogger().info(server.getName() + " matched!");
						return server;
					}
				}
			}// else
//				getLogger().info(server_name + " wasn't found!");
		}

		return null;
	}

	public NetworkLocation getKOTHLocation() {
		return this.koth_location;
	}

	public void setKOTHLocation(NetworkLocation koth_location) {
		this.koth_location = koth_location;
	}

	public WorldGuardPlugin getWorldGuard() {
		if (wg_plugin == null) {
			wg_plugin = (WorldGuardPlugin)getServer().getPluginManager().getPlugin("WorldGuard");
			if (wg_plugin != null) {
				if (!wg_plugin.isEnabled()) {
					getPluginLoader().enablePlugin(wg_plugin);
				}
			}
			else {
				getLogger().log(Level.INFO, "Could not load worldguard, disabling");
				wg_plugin = null;
			}
		}
		return wg_plugin;
	}

	public ItemStack stringToItemStack(String block_name) throws IllegalArgumentException {
		if (block_name == null)
			return null;
//			throw new IllegalArgumentException("Block cannot be null");

		if (block_name.contains(":")) {
			String[] blockNameSplit = block_name.split(":");
			if (blockNameSplit.length > 2)
				throw new IllegalArgumentException("No material matching: '" + block_name + "'");

			final int data;
			try {
				data = Integer.parseInt(blockNameSplit[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Data type not a valid number: '" + blockNameSplit[1] + "'");
			}

			if (data > 255 || data < 0)
				throw new IllegalArgumentException("Data type out of range (0-255): '" + data + "'");

			Material material = Material.matchMaterial(blockNameSplit[0]);
			if (material == null)
				throw new IllegalArgumentException("No material matching: '" + block_name + "'");

			ItemStack item_stack = new ItemStack(material, 1, Short.parseShort(blockNameSplit[1]));

			return item_stack;
		} else {
			final Material material = Material.matchMaterial(block_name);
			if (material == null)
				throw new IllegalArgumentException("No material matching: '" + block_name + "'");

			ItemStack item_stack = new ItemStack(material, 1);

			return item_stack;
		}
	}

	public Set<Block> isPortalFrame(Block block_start, ItemStack frame_material) {
		Set<Block> portal_blocks = isPortalFrame(block_start, BlockFace.NORTH, frame_material);
		if (portal_blocks != null)
			return portal_blocks;
		portal_blocks = isPortalFrame(block_start, BlockFace.SOUTH, frame_material);
		if (portal_blocks != null)
			return portal_blocks;
		portal_blocks = isPortalFrame(block_start, BlockFace.EAST, frame_material);
		if (portal_blocks != null)
			return portal_blocks;
		portal_blocks = isPortalFrame(block_start, BlockFace.WEST, frame_material);
		if (portal_blocks != null)
			return portal_blocks;

		return null;
	}

	public Set<Block> isPortalFrame(Block block_start, BlockFace face, ItemStack frame_material) {
		Set<Block> frame_blocks = new HashSet<Block>();

		int x = 0;
		int z = 0;
		
		if (face == BlockFace.NORTH)
			z = -1;
		else if (face == BlockFace.SOUTH)
			z = 1;
		else if (face == BlockFace.EAST)
			x = 1;
		else if (face == BlockFace.WEST)
			x = -1;

		frame_blocks.add(block_start);
		frame_blocks.add(block_start.getRelative(x     , 0, z     ));
		frame_blocks.add(block_start.getRelative(x *  2, 1, z *  2));
		frame_blocks.add(block_start.getRelative(x *  2, 2, z *  2));
		frame_blocks.add(block_start.getRelative(x *  2, 3, z *  2));
		frame_blocks.add(block_start.getRelative(x,      4, z     ));
		frame_blocks.add(block_start.getRelative(x *  0, 4, z *  0));
		frame_blocks.add(block_start.getRelative(x * -1, 3, z * -1));
		frame_blocks.add(block_start.getRelative(x * -1, 2, z * -1));
		frame_blocks.add(block_start.getRelative(x * -1, 1, z * -1));

		for (Block block : frame_blocks) {
//			getLogger().info("x= " + block.getX() + ", z= " + block.getZ() + ", y= " + block.getY() + " Material=" + block.getType().name() + " Frame Material=" + frame_material.getType().name());
			if (block.getType() != frame_material.getType() || block.getData() != frame_material.getData().getData())
				return null;
		}

		Set<Block> portal_blocks = new HashSet<Block>();
		portal_blocks.add(block_start.getRelative(x *  0, 1, z *  0));
		portal_blocks.add(block_start.getRelative(x *  0, 2, z *  0));
		portal_blocks.add(block_start.getRelative(x *  0, 3, z *  0));
		portal_blocks.add(block_start.getRelative(x     , 1, z     ));
		portal_blocks.add(block_start.getRelative(x     , 2, z     ));
		portal_blocks.add(block_start.getRelative(x     , 3, z     ));
		
		return portal_blocks;
	}
}
