package com.c45y.CutePVP;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class Player {
	private String name;
	private org.bukkit.entity.Player player;
	private Team team;
	private Integer score = 0;
	private Integer kills = 0;
	private boolean is_online = false;
	private boolean portaling = false;
	private Location portaling_location;
	private Inventory portaling_inventory;
	private Inventory portaling_armor;
	private int portaling_health;
	private int portaling_hunger;

	public Player(String name, org.bukkit.entity.Player player) {
		this.name = name;
		this.player = player;
	}

	public Player(String name, org.bukkit.entity.Player player, Team team) {
		this.name = name;
		this.player = player;
		this.team = team;
	}

	public Player(String name, org.bukkit.entity.Player player, Integer score, Integer kills) {
		this.name = name;
		this.player = player;
		this.score = score;
		this.kills = kills;
	}

	public org.bukkit.entity.Player getPlayer() {
		return player;
	}

	public void setPlayer(org.bukkit.entity.Player player) {
		this.player = player;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public void addScore() {
		score++;
	}

	public Integer getKills() {
		return kills;
	}

	public void setKills(Integer kills) {
		this.kills = kills;
	}

	public void addKill() {
		kills++;
	}

	public void setIsOnline(boolean is_online) {
		this.is_online = is_online;
	}

	public void setIsPortaling(boolean portaling) {
		this.portaling = portaling;
	}

	public boolean isPortaling() {
		return this.portaling;
	}

	public void setPortalingLocation(Location location) {
		this.portaling_location = location;
	}

	public Location getPortalingLocation() {
		return this.portaling_location;
	}

	public void setPortalingInventory(Inventory inventory) {
		this.portaling_inventory = inventory;
	}

	public Inventory getPortalingInventory() {
		return this.portaling_inventory;
	}

	public void setPortalingArmor(Inventory armor) {
		this.portaling_armor = armor;
	}

	public Inventory getPortalingArmor() {
		return this.portaling_armor;
	}

	public void setPortalingHealth(int health) {
		this.portaling_health = health;
	}

	public int getPortalingHealth() {
		return this.portaling_health;
	}

	public void setPortalingHunger(int hunger) {
		this.portaling_hunger = hunger;
	}

	public int getPortalingHunger() {
		return this.portaling_hunger;
	}

	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		if (player != null)
			player.abandonConversation(arg0, arg1);
	}

	public void abandonConversation(Conversation arg0) {
		if (player != null)
			player.abandonConversation(arg0);
	}

	public void acceptConversationInput(String arg0) {
		if (player != null)
			player.acceptConversationInput(arg0);
	}

	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return player.addAttachment(arg0, arg1);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3) {
		return player.addAttachment(arg0, arg1, arg2, arg3);
	}

	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2) {
		return player.addAttachment(arg0, arg1, arg2);
	}

	public PermissionAttachment addAttachment(Plugin arg0) {
		return player.addAttachment(arg0);
	}

	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		if (player != null)
			return player.addPotionEffect(arg0, arg1);

		return false;
	}

	public boolean addPotionEffect(PotionEffect arg0) {
		if (player != null)
			return player.addPotionEffect(arg0);

		return false;
	}

	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		if (player != null)
			return player.addPotionEffects(arg0);

		return false;
	}

	public void awardAchievement(Achievement arg0) {
		if (player != null)
			player.awardAchievement(arg0);
	}

	public boolean beginConversation(Conversation arg0) {
		if (player != null)
			return player.beginConversation(arg0);

		return false;
	}

	public boolean canSee(org.bukkit.entity.Player arg0) {
		if (player != null)
			return player.canSee(arg0);

		return false;
	}

	public void chat(String arg0) {
		if (player != null)
			player.chat(arg0);
	}

	public void closeInventory() {
		player.closeInventory();
	}

	public void damage(int arg0, Entity arg1) {
		if (player != null)
			player.damage(arg0, arg1);
	}

	public void damage(int arg0) {
		if (player != null)
			player.damage(arg0);
	}

	public boolean eject() {
		if (player != null)
			return player.eject();

		return false;
	}

	public Collection<PotionEffect> getActivePotionEffects() {
		return player.getActivePotionEffects();
	}

	public InetSocketAddress getAddress() {
		return player.getAddress();
	}

	public boolean getAllowFlight() {
		if (player != null)
			return player.getAllowFlight();

		return false;
	}

	public Location getBedSpawnLocation() {
		return player.getBedSpawnLocation();
	}

	public boolean getCanPickupItems() {
		if (player != null)
			return player.getCanPickupItems();

		return true;
	}

	public Location getCompassTarget() {
		return player.getCompassTarget();
	}

	public String getCustomName() {
		return player.getCustomName();
	}

	public String getDisplayName() {
		if (this.player != null)
			return this.player.getDisplayName();
		else
			if (this.team != null)
				return this.team.encodeTeamColor(this.name);
			else
				return this.name;
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return player.getEffectivePermissions();
	}

	public Inventory getEnderChest() {
		return player.getEnderChest();
	}

	public int getEntityId() {
		return player.getEntityId();
	}

	public EntityEquipment getEquipment() {
		return player.getEquipment();
	}

	public float getExhaustion() {
		return player.getExhaustion();
	}

	public float getExp() {
		return player.getExp();
	}

	public int getExpToLevel() {
		return player.getExpToLevel();
	}

	public double getEyeHeight() {
		return player.getEyeHeight();
	}

	public double getEyeHeight(boolean arg0) {
		return player.getEyeHeight(arg0);
	}

	public Location getEyeLocation() {
		return player.getEyeLocation();
	}

	public float getFallDistance() {
		return player.getFallDistance();
	}

	public int getFireTicks() {
		return player.getFireTicks();
	}

	public long getFirstPlayed() {
		return player.getFirstPlayed();
	}

	public float getFlySpeed() {
		return player.getFlySpeed();
	}

	public int getFoodLevel() {
		return player.getFoodLevel();
	}

	public GameMode getGameMode() {
		return player.getGameMode();
	}

	public int getHealth() {
		return player.getHealth();
	}

	public PlayerInventory getInventory() {
		if (player != null)
			return player.getInventory();

		return null;
	}

	public ItemStack getItemInHand() {
		return player.getItemInHand();
	}

	public ItemStack getItemOnCursor() {
		return player.getItemOnCursor();
	}

	public org.bukkit.entity.Player getKiller() {
		return player.getKiller();
	}

	public int getLastDamage() {
		return player.getLastDamage();
	}

	public EntityDamageEvent getLastDamageCause() {
		return player.getLastDamageCause();
	}

	public long getLastPlayed() {
		return player.getLastPlayed();
	}

	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		return player.getLastTwoTargetBlocks(arg0, arg1);
	}

	public int getLevel() {
		return player.getLevel();
	}

	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		return player.getLineOfSight(arg0, arg1);
	}

	public Set<String> getListeningPluginChannels() {
		return player.getListeningPluginChannels();
	}

	public Location getLocation() {
		if (player != null)
			return player.getLocation();

		return null;
	}

	public Location getLocation(Location arg0) {
		return player.getLocation(arg0);
	}

	public int getMaxFireTicks() {
		return player.getMaxFireTicks();
	}

	public int getMaxHealth() {
		return player.getMaxHealth();
	}

	public int getMaximumAir() {
		return player.getMaximumAir();
	}

	public int getMaximumNoDamageTicks() {
		return player.getMaximumNoDamageTicks();
	}

	public List<MetadataValue> getMetadata(String arg0) {
		return player.getMetadata(arg0);
	}

	public String getName() {
		return name;
	}

	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		return player.getNearbyEntities(arg0, arg1, arg2);
	}

	public int getNoDamageTicks() {
		return player.getNoDamageTicks();
	}

	public InventoryView getOpenInventory() {
		return player.getOpenInventory();
	}

	public Entity getPassenger() {
		return player.getPassenger();
	}

	public String getPlayerListName() {
		return player.getPlayerListName();
	}

	public long getPlayerTime() {
		return player.getPlayerTime();
	}

	public long getPlayerTimeOffset() {
		return player.getPlayerTimeOffset();
	}

	public WeatherType getPlayerWeather() {
		return player.getPlayerWeather();
	}

	public int getRemainingAir() {
		return player.getRemainingAir();
	}

	public boolean getRemoveWhenFarAway() {
		if (player != null)
			return player.getRemoveWhenFarAway();

		return false;
	}

	public float getSaturation() {
		return player.getSaturation();
	}

	public Scoreboard getScoreboard() {
		return player.getScoreboard();
	}

	public Server getServer() {
		return player.getServer();
	}

	public int getSleepTicks() {
		return player.getSleepTicks();
	}

	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		return player.getTargetBlock(arg0, arg1);
	}

	public int getTicksLived() {
		return player.getTicksLived();
	}

	public int getTotalExperience() {
		return player.getTotalExperience();
	}

	public EntityType getType() {
		return player.getType();
	}

	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	public Entity getVehicle() {
		return player.getVehicle();
	}

	public Vector getVelocity() {
		return player.getVelocity();
	}

	public float getWalkSpeed() {
		return player.getWalkSpeed();
	}

	public World getWorld() {
		return player.getWorld();
	}

	public void giveExp(int arg0) {
		if (player != null)
			player.giveExp(arg0);
	}

	public void giveExpLevels(int arg0) {
		if (player != null)
			player.giveExpLevels(arg0);
	}

	public boolean hasLineOfSight(Entity arg0) {
		if (player != null)
			return player.hasLineOfSight(arg0);

		return false;
	}

	public boolean hasMetadata(String arg0) {
		if (player != null)
			return player.hasMetadata(arg0);

		return false;
	}

	public boolean hasPermission(Permission arg0) {
		if (player != null)
			return player.hasPermission(arg0);

		return false;
	}

	public boolean hasPermission(String arg0) {
		if (player != null)
			return player.hasPermission(arg0);

		return false;
	}

	public boolean hasPlayedBefore() {
		if (player != null)
			return player.hasPlayedBefore();

		return false;
	}

	public boolean hasPotionEffect(PotionEffectType arg0) {
		if (player != null)
			return player.hasPotionEffect(arg0);

		return false;
	}

	public void hidePlayer(org.bukkit.entity.Player arg0) {
		if (player != null)
			player.hidePlayer(arg0);
	}

	public void incrementStatistic(Statistic arg0, int arg1) {
		if (player != null)
			player.incrementStatistic(arg0, arg1);
	}

	public void incrementStatistic(Statistic arg0, Material arg1, int arg2) {
		if (player != null)
			player.incrementStatistic(arg0, arg1, arg2);
	}

	public void incrementStatistic(Statistic arg0, Material arg1) {
		if (player != null)
			player.incrementStatistic(arg0, arg1);
	}

	public void incrementStatistic(Statistic arg0) {
		if (player != null)
			player.incrementStatistic(arg0);
	}

	public boolean isBanned() {
		if (player != null)
			return player.isBanned();

		return false;
	}

	public boolean isBlocking() {
		if (player != null)
			return player.isBlocking();

		return false;
	}

	public boolean isConversing() {
		if (player != null)
			return player.isConversing();

		return false;
	}

	public boolean isCustomNameVisible() {
		if (player != null)
			return player.isCustomNameVisible();

		return false;
	}

	public boolean isDead() {
		if (player != null)
			return player.isDead();

		return false;
	}

	public boolean isEmpty() {
		if (player != null)
			return player.isEmpty();

		return false;
	}

	public boolean isFlying() {
		if (player != null)
			return player.isFlying();

		return false;
	}

	public boolean isInsideVehicle() {
		if (player != null)
			return player.isInsideVehicle();

		return false;
	}

	public boolean isOnGround() {
		if (player != null)
			return player.isOnGround();

		return false;
	}

	public boolean isOnline() {
		if (player != null)
			return player.isOnline();

		return this.is_online;
	}

	public boolean isOp() {
		if (player != null)
			return player.isOp();

		return false;
	}

	public boolean isPermissionSet(Permission arg0) {
		if (player != null)
			return player.isPermissionSet(arg0);

		return false;
	}

	public boolean isPermissionSet(String arg0) {
		if (player != null)
			return player.isPermissionSet(arg0);

		return false;
	}

	public boolean isPlayerTimeRelative() {
		if (player != null)
			return player.isPlayerTimeRelative();

		return false;
	}

	public boolean isSleeping() {
		if (player != null)
			return player.isSleeping();

		return false;
	}

	public boolean isSleepingIgnored() {
		if (player != null)
			return player.isSleepingIgnored();

		return false;
	}

	public boolean isSneaking() {
		if (player != null)
			return player.isSneaking();

		return false;
	}

	public boolean isSprinting() {
		if (player != null)
			return player.isSprinting();

		return false;
	}

	public boolean isValid() {
		if (player != null)
			return player.isValid();

		return false;
	}

	public boolean isWhitelisted() {
		if (player != null)
			return player.isWhitelisted();

		return false;
	}

	public void kickPlayer(String arg0) {
		if (player != null)
			player.kickPlayer(arg0);
	}

	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		return player.launchProjectile(arg0);
	}

	public boolean leaveVehicle() {
		if (player != null)
			return player.leaveVehicle();

		return false;
	}

	public void loadData() {
		if (player != null)
			player.loadData();
	}

	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		return player.openEnchanting(arg0, arg1);
	}

	public InventoryView openInventory(Inventory arg0) {
		return player.openInventory(arg0);
	}

	public void openInventory(InventoryView arg0) {
		if (player != null)
			player.openInventory(arg0);
	}

	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		return player.openWorkbench(arg0, arg1);
	}

	public boolean performCommand(String arg0) {
		if (player != null)
			return player.performCommand(arg0);

		return false;
	}

	public void playEffect(EntityEffect arg0) {
		if (player != null)
			player.playEffect(arg0);
	}

	public void playEffect(Location arg0, Effect arg1, int arg2) {
		if (player != null)
			player.playEffect(arg0, arg1, arg2);
	}

	public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
		if (player != null)
			player.playEffect(arg0, arg1, arg2);
	}

	public void playNote(Location arg0, byte arg1, byte arg2) {
		if (player != null)
			player.playNote(arg0, arg1, arg2);
	}

	public void playNote(Location arg0, Instrument arg1, Note arg2) {
		if (player != null)
			player.playNote(arg0, arg1, arg2);
	}

	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		if (player != null)
			player.playSound(arg0, arg1, arg2, arg3);
	}

	public void recalculatePermissions() {
		if (player != null)
			player.recalculatePermissions();
	}

	public void remove() {
		if (player != null)
			player.remove();
	}

	public void removeAttachment(PermissionAttachment arg0) {
		if (player != null)
			player.removeAttachment(arg0);
	}

	public void removeMetadata(String arg0, Plugin arg1) {
		if (player != null)
			player.removeMetadata(arg0, arg1);
	}

	public void removePotionEffect(PotionEffectType arg0) {
		if (player != null)
			player.removePotionEffect(arg0);
	}

	public void resetMaxHealth() {
		if (player != null)
			player.resetMaxHealth();
	}

	public void resetPlayerTime() {
		if (player != null)
			player.resetPlayerTime();
	}

	public void resetPlayerWeather() {
		if (player != null)
			player.resetPlayerWeather();
	}

	public void saveData() {
		if (player != null)
			player.saveData();
	}

	public void sendBlockChange(Location arg0, int arg1, byte arg2) {
		if (player != null)
			player.sendBlockChange(arg0, arg1, arg2);
	}

	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		if (player != null)
			player.sendBlockChange(arg0, arg1, arg2);
	}

	public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3,
			byte[] arg4) {
		return player.sendChunkChange(arg0, arg1, arg2, arg3, arg4);
	}

	public void sendMap(MapView arg0) {
		if (player != null)
			player.sendMap(arg0);
	}

	public void sendMessage(String arg0) {
		if (player != null)
			player.sendMessage(arg0);
	}

	public void sendMessage(String[] arg0) {
		if (player != null)
			player.sendMessage(arg0);
	}

	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		if (player != null)
			player.sendPluginMessage(arg0, arg1, arg2);
	}

	public void sendRawMessage(String arg0) {
		if (player != null)
			player.sendRawMessage(arg0);
	}

	public Map<String, Object> serialize() {
		return player.serialize();
	}

	public void setAllowFlight(boolean arg0) {
		if (player != null)
			player.setAllowFlight(arg0);
	}

	public void setBanned(boolean arg0) {
		if (player != null)
			player.setBanned(arg0);
	}

	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		if (player != null)
			player.setBedSpawnLocation(arg0, arg1);
	}

	public void setBedSpawnLocation(Location arg0) {
		if (player != null)
			player.setBedSpawnLocation(arg0);
	}

	public void setCanPickupItems(boolean arg0) {
		if (player != null)
			player.setCanPickupItems(arg0);
	}

	public void setCompassTarget(Location arg0) {
		if (player != null)
			player.setCompassTarget(arg0);
	}

	public void setCustomName(String arg0) {
		if (player != null)
			player.setCustomName(arg0);
	}

	public void setCustomNameVisible(boolean arg0) {
		if (player != null)
			player.setCustomNameVisible(arg0);
	}

	public void setDisplayName(String arg0) {
		if (player != null)
			player.setDisplayName(arg0);
	}

	public void setExhaustion(float arg0) {
		if (player != null)
			player.setExhaustion(arg0);
	}

	public void setExp(float arg0) {
		if (player != null)
			player.setExp(arg0);
	}

	public void setFallDistance(float arg0) {
		if (player != null)
			player.setFallDistance(arg0);
	}

	public void setFireTicks(int arg0) {
		if (player != null)
			player.setFireTicks(arg0);
	}

	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		if (player != null)
			player.setFlySpeed(arg0);
	}

	public void setFlying(boolean arg0) {
		if (player != null)
			player.setFlying(arg0);
	}

	public void setFoodLevel(int arg0) {
		if (player != null)
			player.setFoodLevel(arg0);
	}

	public void setGameMode(GameMode arg0) {
		if (player != null)
			player.setGameMode(arg0);
	}

	public void setHealth(int arg0) {
		if (player != null)
			player.setHealth(arg0);
	}

	public void setItemInHand(ItemStack arg0) {
		if (player != null)
			player.setItemInHand(arg0);
	}

	public void setItemOnCursor(ItemStack arg0) {
		if (player != null)
			player.setItemOnCursor(arg0);
	}

	public void setLastDamage(int arg0) {
		if (player != null)
			player.setLastDamage(arg0);
	}

	public void setLastDamageCause(EntityDamageEvent arg0) {
		if (player != null)
			player.setLastDamageCause(arg0);
	}

	public void setLevel(int arg0) {
		if (player != null)
			player.setLevel(arg0);
	}

	public void setMaxHealth(int arg0) {
		if (player != null)
			player.setMaxHealth(arg0);
	}

	public void setMaximumAir(int arg0) {
		if (player != null)
			player.setMaximumAir(arg0);
	}

	public void setMaximumNoDamageTicks(int arg0) {
		if (player != null)
			player.setMaximumNoDamageTicks(arg0);
	}

	public void setMetadata(String arg0, MetadataValue arg1) {
		if (player != null)
			player.setMetadata(arg0, arg1);
	}

	public void setNoDamageTicks(int arg0) {
		if (player != null)
			player.setNoDamageTicks(arg0);
	}

	public void setOp(boolean arg0) {
		if (player != null)
			player.setOp(arg0);
	}

	public boolean setPassenger(Entity arg0) {
		if (player != null)
			return player.setPassenger(arg0);

		return false;
	}

	public void setPlayerListName(String arg0) {
		if (player != null)
			player.setPlayerListName(arg0);
	}

	public void setPlayerTime(long arg0, boolean arg1) {
		if (player != null)
			player.setPlayerTime(arg0, arg1);
	}

	public void setPlayerWeather(WeatherType arg0) {
		if (player != null)
			player.setPlayerWeather(arg0);
	}

	public void setRemainingAir(int arg0) {
		if (player != null)
			player.setRemainingAir(arg0);
	}

	public void setRemoveWhenFarAway(boolean arg0) {
		if (player != null)
			player.setRemoveWhenFarAway(arg0);
	}

	public void setSaturation(float arg0) {
		if (player != null)
			player.setSaturation(arg0);
	}

	public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException, IllegalStateException {
		if (player != null)
			player.setScoreboard(arg0);
	}

	public void setSleepingIgnored(boolean arg0) {
		if (player != null)
			player.setSleepingIgnored(arg0);
	}

	public void setSneaking(boolean arg0) {
		if (player != null)
			player.setSneaking(arg0);
	}

	public void setSprinting(boolean arg0) {
		if (player != null)
			player.setSprinting(arg0);
	}

	public void setTexturePack(String arg0) {
		if (player != null)
			player.setTexturePack(arg0);
	}

	public void setTicksLived(int arg0) {
		if (player != null)
			player.setTicksLived(arg0);
	}

	public void setTotalExperience(int arg0) {
		if (player != null)
			player.setTotalExperience(arg0);
	}

	public void setVelocity(Vector arg0) {
		if (player != null)
			player.setVelocity(arg0);
	}

	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		if (player != null)
			player.setWalkSpeed(arg0);
	}

	public void setWhitelisted(boolean arg0) {
		if (player != null)
			player.setWhitelisted(arg0);
	}

	public boolean setWindowProperty(Property arg0, int arg1) {
		if (player != null)
			return player.setWindowProperty(arg0, arg1);

		return false;
	}

	public Arrow shootArrow() {
		return player.shootArrow();
	}

	public void showPlayer(org.bukkit.entity.Player arg0) {
		if (player != null)
			player.showPlayer(arg0);
	}

	public boolean teleport(Entity arg0, TeleportCause arg1) {
		if (player != null)
			return player.teleport(arg0, arg1);

		return false;
	}

	public boolean teleport(Entity arg0) {
		if (player != null)
			return player.teleport(arg0);

		return false;
	}

	public boolean teleport(Location arg0, TeleportCause arg1) {
		if (player != null)
			return player.teleport(arg0, arg1);

		return false;
	}

	public boolean teleport(Location arg0) {
		if (player != null && arg0 != null)
			return player.teleport(arg0);

		return false;
	}

	public Egg throwEgg() {
		return player.throwEgg();
	}

	public Snowball throwSnowball() {
		return player.throwSnowball();
	}

	public void updateInventory() {
		if (player != null)
			player.updateInventory();
	}
}
