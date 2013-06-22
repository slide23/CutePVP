package com.c45y.CutePVP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class TeamManager {
	private Map<String, Team> teams = new HashMap<String, Team>();

	public TeamManager() {
	}

	public void onFirstJoin(Player player) {
//		if (player.hasPermission("cutepvp.mod"))
//			return;

		Team team = decideTeam(player);
		team.addPlayer(player);
		player.sendMessage(team.encodeTeamColor("Welcome to the " + team.getName() + " team!"));
	}

	public Team decideTeam(Player player) {
		int size = 1000000;
		Team team_small = null;
		for (Team team : this.teams.values()) {
			if (team.getPlayer().size() < size) {
				team_small = team;
				size = team.getPlayer().size();
			}
		}

		return team_small;
/*
		List<Integer> size = new ArrayList<Integer>();
		List<Integer> weights = new ArrayList<Integer>();
		int size_total = 0;
		int size_max = 0;
		int team_count = this.teams.values().size();

		StringBuilder message = new StringBuilder("Player = " + player.getName());
		
		int i = 0 ;
		for (Team team : this.teams.values()) {
			int team_size = team.getPlayers().size();
			size.add(i, team_size);

			if (team_size > size_max)
				size_max = team_size;

			size_total += team_size;
			i++;

			message.append(" " + team.getName() + "=" + team_size);
		}

		for (i = 0; i < team_count; i++) {
			int team_weight = 100 / team_count;
			if (size_total > 0)
				team_weight = (int)(((float)(size_max - size.get(i)) / size_total) * 100.0);
			weights.add(i, team_weight);

			message.append(" Weight=" + team_weight);
		}

		int random = 0 + (int)(Math.random() * ((100 - 0) + 1));

		message.append(" Random=" + random);

		int team_index = 0;
		
		int weight_cumulative = 0;
		for (i = 0; i < team_count; i++) {
			if (weight_cumulative < random && random <= weight_cumulative + weights.get(i)) {
				team_index = i;
				break;
			}
			weight_cumulative += weights.get(i);
		}

		int j = 0;
		for (Team team : this.teams.values()) {
			if (j == team_index) {
				message.append(" Team=" + team.getName());
				CutePVP.getInstance().getLogger().info(message.toString());
				return team;
			}
			j++;
		}
*/
/*
		int redSize = redTeam.getMembersOnline().size();
		int blueSize = blueTeam.getMembersOnline().size();
		int yellowSize = yellowTeam.getMembersOnline().size();
		int greenSize = greenTeam.getMembersOnline().size();
		int mostPlayers = Math.max(Math.max(redSize, blueSize), Math.max(yellowSize, greenSize));
		int sumDelta = mostPlayers - redSize + mostPlayers - blueSize + mostPlayers - yellowSize + mostPlayers - greenSize;
//		CutePVP.getInstance().getLogger().info("R:" + redSize + " B:" + blueSize + " Y:" + yellowSize + " G:" + greenSize + " M:" + mostPlayers + " Sum:" + sumDelta);
		int[] weights = {25, 25, 25, 25};
		if (sumDelta > 0) {
//			CutePVP.getInstance().getLogger().info("Creating weights");
			weights[0] = (int)(((float)(mostPlayers - redSize) / sumDelta) * 100.0);
			weights[1] = (int)(((float)(mostPlayers - blueSize) / sumDelta) * 100.0);
			weights[2] = (int)(((float)(mostPlayers - yellowSize) / sumDelta) * 100.0);
			weights[3] = (int)(((float)(mostPlayers - greenSize) / sumDelta) * 100.0);
		}
		int random = 0 + (int)(Math.random() * ((100 - 0) + 1));

		int team = random / 25;
		if (0 <= random && random <= weights[0]) {
			team = 0;
		}
		if (weights[0] < random && random <= (weights[0]+weights[1])) {
			team = 1;
		}
		if ((weights[0]+weights[1]) < random && random <= (weights[0]+weights[1]+weights[2])) {
			team = 2;
		}
		if ((weights[0]+weights[1]+weights[2]) < random && random <= 100) {
			team = 3;
		}

		CutePVP.getInstance().getLogger().info("Player=" + player_name + " Team=" + team + " Random=" + random + " weights=" + weights[0] + "," + weights[1] + "," + weights[2] + "," + weights[3] + " Red=" + redSize + " Blue=" + blueSize + " Yellow=" + yellowSize + " Green=" + greenSize);
		return team;
*/
//		return (Team) teams.values().toArray()[0];

	}

	public void messageTeam(String team_name, String message) {
		/* Make sure staff receive all messages. */
//		staffTeam.message(m1);

		teams.get(team_name).message(message);
	}

	public Team getTeam(Player player) {
		for (Team team : teams.values())
			if (team.inTeam(player))
				return team;

		return null;
	}

	public Team getTeamForPlayer(String player_name) {
		for (Team team : teams.values())
			if (team.inTeam(player_name))
				return team;

		return null;
	}

	public Team getTeam(Block block) {
		for (Team team : teams.values())
			if (team.isTeamBlock(block))
				return team;

		return null;
	}

	public boolean inEnemyTeamSpawn(Player player) {
		if (player.hasPermission("cutepvp.mod"))
			return false;

		Location location = player.getLocation();

		for (Team team : teams.values())
			if (team.inTeamSpawn(location) && !team.inTeam(player.getName()))
				return true;

		return false;
	}

	public boolean inRangeOfEnemyTeamSpawn(Player player) {
		if (player.hasPermission("cutepvp.mod"))
			return false;

		Location location = player.getLocation();

		for (Team team : teams.values())
			if (team.inTeamBase(location) && !team.inTeam(player.getName()))
				return true;

		return false;
	}

	public boolean inOwnTeamBase(Player player) {
		if (player.hasPermission("cutepvp.mod"))
			return false;

		Location location = player.getLocation();
		Team team = getTeamForPlayer(player.getName());

		if (team.inTeamBase(location))
			return true;

		return false;
	}

	public Team getTeamForFlagBearer(Player player) {
		for (Team team : teams.values())
			if (team.getCarrier() == player)
				return team;

		return null;
	}

	public Team getTeamFromFlagLocation(Location loc) {
		for (Team team : teams.values())
			if (team.getFlagLocation().equals(loc) && team.getFlagNetworkLocation().getServer() == null)
				return team;

		return null;
	}

	public boolean shouldTakeDamageFromBlock(Block block, String player_name) {
		Player player = CutePVP.getInstance().getPlayer(player_name);

		if (player.hasPermission("cutepvp.modmode"))
			return false;

		Team team_block = getTeam(block);
		Team team_player = getTeamForPlayer(player_name);

		if (team_block != null && team_player != null && team_block != team_player)
			return true;

		return false;
	}

	public void addTeam(String name, Team team) {
		this.teams.put(name, team);
	}

	public Map<String, Team> getTeams() {
		return teams;
	}

	public void setTeams(Map<String, Team> teams) {
		this.teams = teams;
	}

	public Team getTeam(String name) {
		return teams.get(name);
	}
}
