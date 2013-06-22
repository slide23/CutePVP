package com.c45y.CutePVP;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Team {
	private String name;
	private ChatColor teamChatColor;
	private HashMap<String, Player> players = new HashMap<String, Player>();
	private Player carrier;
	private ItemStack block;
	private int score;
	private int kills;
	private NetworkLocation spawn;
	private NetworkLocation flag_home_location;
	private NetworkLocation flag_current_location;
	private List<NetworkLocation> flag_home_locations;

	public Team(String name, String block_info, ChatColor color) {
		this.name = name;
		this.teamChatColor = color;
		this.block = CutePVP.getInstance().stringToItemStack(block_info);
		this.spawn = new NetworkLocation();
		this.flag_home_location = new NetworkLocation();
		this.flag_current_location = new NetworkLocation();
		this.flag_home_locations = new ArrayList<NetworkLocation>();
	}

	public String getName() {
		return name;
	}

	public ChatColor getChatColor() {
		return teamChatColor;
	}

	public Boolean isTeamBlock(Block block) {
//		CutePVP.getInstance().getLogger().info(this.block.getType().name() + " ?= " + block.getType().name());
//		CutePVP.getInstance().getLogger().info(this.block.getData().getData() + " ?= " + block.getData());
		if (block.getType().equals(this.block.getType()) && block.getData() == this.block.getData().getData())
			return true;

		return false;
	}

	public void setBlock(ItemStack block) {
		this.block = block;
//		CutePVP.getInstance().getConfig().createSection("teams." + teamName + ".block", block.serialize());
		CutePVP.getInstance().getConfig().set("teams." + name + ".block", block.getType().name() + ":" + block.getData().getData());
	}

	public ItemStack getItemStack() {
		return this.block;
	}

	public String encodeTeamColor(String s1) {
		return getChatColor() + s1 + ChatColor.WHITE;
	}

	/* Team location configuration */
	public Location getSpawnLocation() {
		return this.spawn.getLocation();
	}

	public NetworkLocation getSpawn() {
		return this.spawn;
	}

	public void setSpawn(NetworkLocation location) {
		this.spawn = location;

		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.x", location.getLocation().getX());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.y", location.getLocation().getY());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.z", location.getLocation().getZ());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.yaw", location.getLocation().getYaw());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.pitch", location.getLocation().getPitch());
		CutePVP.getInstance().saveConfig();
	}

	public void setSpawn(Location l1) {
		this.spawn.setLocation(l1);

		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.x", l1.getX());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.y", l1.getY());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.z", l1.getZ());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.yaw", l1.getYaw());
		CutePVP.getInstance().getConfig().set("teams." + this.name + ".spawn.pitch", l1.getPitch());
		CutePVP.getInstance().saveConfig();
	}

	public boolean inTeamBase(Location l1) {
		RegionManager mgr = CutePVP.getInstance().getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(CutePVP.getInstance().getConfig().getString("teams." + name + ".base.region")))
				return true;
		}

		return false;
	}

	public void setCarrier(Player player) {
		this.carrier = player;
/*
		if (player != null)
			CutePVP.getInstance().getState().set("teams." + name + ".carrier", player.getName());
*/
	}

	public void removeCarrier() {
		carrier = null;
		CutePVP.getInstance().getState().set("teams." + name + ".carrier", null);
		CutePVP.getInstance().saveState();
	}

	public boolean isTeamFlagRegion(Location l1) {
		RegionManager mgr = CutePVP.getInstance().getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(CutePVP.getInstance().getConfig().getString("teams." + name + ".flag.region")))
				return true;
		}

		return false;
	}

	public boolean inTeamSpawn(Location l1) {
		RegionManager mgr = CutePVP.getInstance().getWorldGuard().getGlobalRegionManager().get(l1.getWorld());
		Vector pt = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		for (ProtectedRegion r : set) {
			if (r.getId().equalsIgnoreCase(CutePVP.getInstance().getConfig().getString("teams." + name + ".spawn.region")))
				return true;
		}

		return false;
	}

	/* Flag manipulation */

	public Location getFlagLocation() {
		return this.flag_current_location.getLocation();
	}

	public NetworkLocation getFlagNetworkLocation() {
		return this.flag_current_location;
	}

	public void setFlagNetworkLocation(NetworkLocation location) {
		this.flag_current_location = location;

		CutePVP.getInstance().getState().set("teams." + name + ".flag.x", location.getLocation().getX());
		CutePVP.getInstance().getState().set("teams." + name + ".flag.y", location.getLocation().getY());
		CutePVP.getInstance().getState().set("teams." + name + ".flag.z", location.getLocation().getZ());
		CutePVP.getInstance().saveState();
	}


	public void setFlagLocation(Location l1) {
		this.flag_current_location.setLocation(l1);

		CutePVP.getInstance().getState().set("teams." + name + ".flag.x", l1.getX());
		CutePVP.getInstance().getState().set("teams." + name + ".flag.y", l1.getY());
		CutePVP.getInstance().getState().set("teams." + name + ".flag.z", l1.getZ());
		CutePVP.getInstance().saveState();
	}

	public void dropFlagAtLocation(Location l1) {
		Block flag = CutePVP.getInstance().getServer().getWorlds().get(0).getBlockAt(l1); //Get a handle for the
		flag.setTypeIdAndData(block.getTypeId(), block.getData().getData(), false);
		setFlagLocation(l1);
		removeCarrier();
	}

	public Location getFlagHomeLocation() {
		return this.flag_home_location.getLocation();
	}

	public NetworkLocation getFlagHomeNetworkLocation() {
		return this.flag_home_location;
	}

	public void setFlagHomeNetworkLocation(NetworkLocation location) {
		this.flag_home_location = location;
/*
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.x", location.getLocation().getX());
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.y", location.getLocation().getY());
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.z", location.getLocation().getZ());
		CutePVP.getInstance().saveConfig();
*/
	}

	public void setFlagHomeLocation(Location l1) {
		this.flag_home_location.setLocation(l1);
/*
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.x", l1.getX());
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.y", l1.getY());
		CutePVP.getInstance().getConfig().set("teams." + name + ".flag.z", l1.getZ());
		CutePVP.getInstance().saveConfig();
*/
	}

	public boolean isFlagAtLocation(Location l1) {
		Location teamFlag = getFlagLocation();
		if (l1.getBlockX() == teamFlag.getBlockX() && l1.getBlockY() == teamFlag.getBlockY() &&	l1.getBlockZ() == teamFlag.getBlockZ()) {
			return true;
		}
		return false;
	}

	public void respawnFlag() {
/*
		if (getFlagHomeNetworkLocation().getServer() != null)
			CutePVP.getInstance().getLogger().info(getName() + " home flag on " + getFlagHomeNetworkLocation().getServer().getName());
		else
			CutePVP.getInstance().getLogger().info(getName() + " home flag Server IS null");
*/
		if (getFlagNetworkLocation().equals(getFlagHomeNetworkLocation()) || getFlagHomeNetworkLocation().getServer() != null)
			return;

		CutePVP.getInstance().getLogger().info("Respawning " + getName() + " flag");

		CutePVP.getInstance().getServer().getWorlds().get(0).getBlockAt(getFlagLocation()).setType(Material.AIR);

		for (NetworkLocation location : getFlagHomeLocations())
			CutePVP.getInstance().getServer().getWorlds().get(0).getBlockAt(location.getLocation()).setType(Material.AIR);

		setFlagHomeNetworkLocation(getRandomFlagHomeLocation());

		Block flag_home = CutePVP.getInstance().getServer().getWorlds().get(0).getBlockAt(getFlagHomeLocation());
		flag_home.setTypeIdAndData(this.block.getTypeId(), this.block.getData().getData(), false);

		setFlagLocation(getFlagHomeLocation());
		
		removeCarrier();
	}

	public void addPlayer(Player player) {
		players.put(player.getName(), player);
		player.setTeam(this);
		setHelmet(player);
	}

	public void addPlayer(String playerName) {
		Player player = CutePVP.getInstance().getPlayer(playerName);
		addPlayer(player);
	}

	public void removePlayer(Player player) {
		this.players.remove(player.getName());
		player.setTeam(null);
		CutePVP.getInstance().getLogger().info("teams." + this.name + ".players." + player.getName());
		CutePVP.getInstance().getState().set("teams." + this.name + ".players." + player.getName(), null);
		CutePVP.getInstance().saveState();
	}

	public void removePlayer(String player_name) {
		removePlayer(CutePVP.getInstance().getPlayer(player_name));
	}

	public boolean inTeam(Player player) {
		return inTeam(player.getName());
	}

	public boolean inTeam(String playerName) {
		if (players.containsKey(playerName))
			return true;

		return false;
	}

	public Set<String> getPlayerNames() {
		return players.keySet();
	}

	public Collection<Player> getPlayer() {
		return players.values();
	}

	public Set<String> getMembersOnline() {
		List<String> online = new ArrayList<String>();
		for (String playerName : getPlayerNames()) {
			Player player = CutePVP.getInstance().getPlayer(playerName);
			if (player != null && player.isOnline())
				online.add(playerName);
		}
		return new HashSet<String>(online);
//		return teamMembers.keySet();
	}

	public void message(String m1) {
		for (String player_name: getMembersOnline()) {
			org.bukkit.entity.Player player = CutePVP.getInstance().getServer().getPlayer(player_name);
			if (player != null)
				player.sendMessage(m1);
		}
	}

	public void setHelmet(Player player) {
		if (player != null && player.getPlayer() != null)
			player.getInventory().setHelmet(getItemStack());
	}

	public void setCompassTarget() {
		Location flag_location = getFlagLocation();
		if (flag_location != null)
			for (Player player: getPlayer())
				if (player != null && player.getPlayer() != null)
					player.setCompassTarget(flag_location);
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void addScore() {
		score++;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getKills() {
		return kills;
	}

	public void addKill() {
		kills++;
	}

	public Player getCarrier() {
		return carrier;
	}

	public HashMap<String, Player> getPlayers() {
		return players;
	}

	public void setPlayers(HashMap<String, Player> players) {
		this.players = players;
	}

	public List<NetworkLocation> getFlagHomeLocations() {
		return this.flag_home_locations;
	}

	public NetworkLocation getRandomFlagHomeLocation() {
		Random random = new Random();

		return this.flag_home_locations.get(random.nextInt(this.flag_home_locations.size()));
	}

	public void addFlagHomeNetworkLocation(NetworkLocation location) {
		this.flag_home_locations.add(location);
	}
}
