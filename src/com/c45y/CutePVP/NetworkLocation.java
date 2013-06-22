package com.c45y.CutePVP;

import org.bukkit.Location;
import org.bukkit.World;

import ws.slide.minecraft.bukkit.servermessenger.NetworkServer;

public class NetworkLocation {
	private NetworkServer server = null;
	private Location location = null;

	public NetworkLocation(NetworkServer server, World world, double x, double y, double z) {
		this.server = server;
		this.location = new Location(world, x, y, z);
	}

	public NetworkLocation(World world, double x, double y, double z, float yaw, float pitch) {
		this.location = new Location(world, x, y, z, yaw, pitch);
	}

	public NetworkLocation(World world, double x, double y, double z) {
		this.location = new Location(world, x, y, z);
	}

	public NetworkLocation() {
		
	}

	public NetworkLocation(NetworkServer server, World world, double x, double y, double z, float yaw, float pitch) {
		this.server = server;
		this.location = new Location(world, x, y, z);
	}

	public NetworkServer getServer() {
		return this.server;
	}

	public void setServer(NetworkServer server) {
		this.server = server;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean equals(NetworkLocation location) {
		return (this.location.equals(location.getLocation()) && this.server == location.server);
	}
}
