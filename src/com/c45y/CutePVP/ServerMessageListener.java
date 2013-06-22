package com.c45y.CutePVP;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ws.slide.minecraft.bukkit.servermessenger.NetworkServer;
import ws.slide.minecraft.bukkit.servermessenger.PluginMessageListener;

public class ServerMessageListener implements PluginMessageListener {
	public void onPluginMessageReceived(String channel, NetworkServer arg1, byte[] data) {
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			
			String subchannel = in.readUTF();

//			CutePVP.getInstance().getLogger().info(subchannel);

			if (subchannel.equals("CHAT")) {
				String player_name = in.readUTF();
				String message = in.readUTF();

				CutePVP.getInstance().getLogger().info("<" + player_name + "> " + message);

				Player player = CutePVP.getInstance().getPlayer(player_name);
/*
				if (player == null)
					CutePVP.getInstance().getLogger().info("No such player: " + player_name);
				else
					CutePVP.getInstance().getLogger().info("Player: " + player.getName());
*/
				String message_out = "<" + player.getDisplayName() + "> " + message;
				if (player.getTeam() != null)
					player.getTeam().message(message_out);
				else
					Bukkit.getServer().broadcastMessage(message_out);
			} else if (subchannel.equals("BROADCAST")) {
				String player_name = in.readUTF();
				String message = in.readUTF();

				Player player = CutePVP.getInstance().getPlayer(player_name);

				CutePVP.getInstance().getLogger().info(ChatColor.RED + ">" + ChatColor.BLUE + ">" +  ChatColor.WHITE + " <" + player.getDisplayName() + "> " + message);
/*
				if (player == null)
					CutePVP.getInstance().getLogger().info("No such player: " + player_name);
				else
					CutePVP.getInstance().getLogger().info("Player: " + player.getName());
*/
				String message_out = ChatColor.RED + ">" + ChatColor.BLUE + ">" +  ChatColor.WHITE + " <" + player.getDisplayName() + "> " + message;

				Bukkit.getServer().broadcastMessage(message_out);
			}
			else if (subchannel.equals("FLAG")) {
				String event = in.readUTF();
				String flag_team_name = in.readUTF();
				String player_name = in.readUTF();

				Team team = CutePVP.getInstance().getTeamManager().getTeam(flag_team_name);
				Player player = CutePVP.getInstance().getPlayer(player_name);

				if (event.equals("STEAL")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " stole the " + flag_team_name + " flag");
					team.setCarrier(player);
				}
				else if (event.equals("DROP")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " dropped the " + flag_team_name + " flag");
					team.removeCarrier();
				}
				else if (event.equals("CAPTURE")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " captured the " + flag_team_name + " flag");
					team.removeCarrier();
				}
				else if (event.equals("RETURN")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " returned the " + flag_team_name + " flag");
				}
			}
			else if (subchannel.equals("PLAYER")) {
				String event = in.readUTF();
				String player_name = in.readUTF();

				Player player = CutePVP.getInstance().getPlayer(player_name);

				if (event.equals("JOIN")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " joined the game");
					player.setIsOnline(true);
				}
				else if (event.equals("QUIT")) {
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " quit the game");
					player.setIsOnline(false);
				}
				else if (event.equals("DEATH")) {
					String message = in.readUTF();
					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " " + message);
				}
				else if (event.equals("PORTAL")) {
//					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " portaling");
					int x = in.readInt();
					int y = in.readInt();
					int z = in.readInt();
					String server_to = in.readUTF();
					Boolean create_portal = in.readBoolean();
					String inventory_serialized = in.readUTF();
					String armor_serialized = in.readUTF();
					int health = in.readInt();
					int hunger = in.readInt();;

					if (server_to.equalsIgnoreCase(Bukkit.getServerName())) {
						player.setIsPortaling(true);
						player.setPortalingLocation(new Location(Bukkit.getServer().getWorlds().get(0), x, y, z));
						player.setPortalingInventory(InventorySerialization.fromBase64(inventory_serialized));
						player.setPortalingArmor(InventorySerialization.fromBase64(armor_serialized));
						player.setPortalingHealth(health);
						player.setPortalingHunger(hunger);
					}
				}
				else if (event.equals("DISGUISE")) {
				}
				else if (event.equals("TEAM")) {
					String team_name = in.readUTF();

					Bukkit.getServer().broadcastMessage(player.getDisplayName() + " joined the " + team_name + " team");

					Team team = CutePVP.getInstance().getTeamManager().getTeam(team_name);
					team.addPlayer(player);
					player.setTeam(team);
				}
				else if (event.equals("REMOVE")) {
					Team team = CutePVP.getInstance().getTeamManager().getTeamForPlayer(player_name);
					if (team != null)
						team.removePlayer(player_name);
				}
			}
			else if (subchannel.equals("TEAM")) {
				String event = in.readUTF();
				if (event.equals("RMPLAYER")) {
					String player_name = in.readUTF();
					Team team = CutePVP.getInstance().getTeamManager().getTeamForPlayer(player_name);
					if (team != null) {
						team.removePlayer(player_name);
						CutePVP.getInstance().getLogger().info("Removing player " + player_name + " from team " + team.getName());
					}
				}
			}
			else if (subchannel.equals("KOTH")) {
				String team_name = in.readUTF();
				Team team = CutePVP.getInstance().getTeamManager().getTeam(team_name);
				CutePVP.getInstance().getServer().broadcastMessage(ChatColor.DARK_PURPLE + "[NOTICE] " + team.getName() + " gets buff!");
				for (Player player_buff : team.getPlayer()) {
					if (player_buff.getPlayer() != null) {
						player_buff.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
						player_buff.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1));
					}
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	public void onPluginMessageReceived(String channel, org.bukkit.entity.Player player, byte[] message) {
	}
}
