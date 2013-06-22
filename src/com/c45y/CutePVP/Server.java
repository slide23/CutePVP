package com.c45y.CutePVP;

import org.bukkit.inventory.ItemStack;

public class Server {
	private String name;
	private String host;
	private int port;
	private ItemStack block;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ItemStack getBlock() {
		return block;
	}

	public void setBlock(ItemStack block) {
		this.block = block;
	}
}
