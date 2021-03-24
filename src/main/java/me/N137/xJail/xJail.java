package me.N137.xJail;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;



public class xJail extends JavaPlugin {
    private ArrayList<Location> jailLocation = new ArrayList<>();
    private Map<UUID, Long> jailReleaseData = new HashMap<>();
    private Map<Player, BossBar> bossBarMap = new HashMap<>();
    private Map<UUID, Integer> jailCellNumber = new HashMap<>();
    private Map<UUID, ItemStack[]> inventoryData = new HashMap<>();

    private FileConfiguration jailReleaseDataConfig;
    private File jailReleaseDataFile;
    private FileConfiguration jailCellNumberConfig;
    private File jailCellNumberFile;
    private Location jailReleaseLocation;

    public UUID developerUUID = UUID.fromString("c8984808-54ca-4a6a-a3cd-b518af0df4f7");
    public String developerName = null;

    public Map<UUID, Integer> killCount = new HashMap<>();
    public Map<UUID, Integer> receivedHits = new HashMap<>();
    public ArrayList<String> allowedCommands = new ArrayList<>();



    public String resolveUsername(String UUIDString) {
        String url = "https://api.mojang.com/user/profiles/"+UUIDString.replace("-", "")+"/names";
        try {
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url));
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            String playerSlot = nameValue.get(nameValue.size()-1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
            return nameObject.get("name").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "?";
    }

    public void sendHelp(CommandSender sender) {

        if (sender.hasPermission("xjail.free") || sender.hasPermission("xjail.getstick") || sender.hasPermission("xjail.admin") || sender.hasPermission("xjail.jail")) {
            sender.sendMessage( "");
            sender.sendMessage("§2§n§lCommand Reference:");
            sender.sendMessage("");
        }

        if (sender.hasPermission("xjail.free")) {sender.sendMessage("§7/jail free <name> » Release a player from jail");}
        if (sender.hasPermission("xjail.jail")) {sender.sendMessage( "§7/jail jail <user> <terms> » Send a <user> to jail for <terms> terms");}
        if (sender.hasPermission("xjail.getstick")) {sender.sendMessage( "§7/jail getstick » Give yourself a jailstick");}

        if (sender.hasPermission("xjail.admin")) {sender.sendMessage( "§7/jail admin add » Add your current location as a cell");}
        if (sender.hasPermission("xjail.admin")) {sender.sendMessage( "§7/jail admin release » Set the release location to your location");}
        if (sender.hasPermission("xjail.admin")) {sender.sendMessage("§7/jail admin list » Get a list of all of the cells.");}
        if (!sender.hasPermission("xjail.free") && !sender.hasPermission("xjail.getstick") && !sender.hasPermission("xjail.admin") && !sender.hasPermission("xjail.jail")) {sender.sendMessage("§7You do not have any permissions to use any commands.");} else {
            sender.sendMessage( "§7");
            sender.sendMessage( "§6xJail§7 developed by §6" + this.developerName);
        }

    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        allowedCommands.addAll(getConfig().getStringList("allowedCommands"));

        JailCommand jailCommand = new JailCommand(this);
        Objects.requireNonNull(getCommand("jail")).setExecutor(jailCommand);
       getServer().getPluginManager().registerEvents(new JailListener(this), this);

        this.developerName = this.resolveUsername(developerUUID.toString());


        if (getConfig().getConfigurationSection("jailLocation") != null) {

            int amountOfJails = Objects.requireNonNull(getConfig().getConfigurationSection("jailLocation")).getKeys(false).size();
            assert amountOfJails != 0;


            for (int i = 0; i < amountOfJails; i++) {
              int x = getConfig().getInt("jailLocation."+i+".x");
              int y = getConfig().getInt("jailLocation."+i+".y");
              int z = getConfig().getInt("jailLocation."+i+".z");
              String world =  getConfig().getString("jailLocation."+i+".world");

                if (Bukkit.getWorld(Objects.requireNonNull(world)) != null) {
                    jailLocation.add(new Location(Bukkit.getWorld(world), x+0.5, y+0.5, z+0.5));
                } else {
                    getServer().getPluginManager().disablePlugin(this);

                }
            }
        } else {
            getServer().getPluginManager().disablePlugin(this); }

        if (getConfig().getConfigurationSection("jailReleaseLocation") != null) {
            int x = getConfig().getInt("jailReleaseLocation.x");
            int y = getConfig().getInt("jailReleaseLocation.y");
            int z = getConfig().getInt("jailReleaseLocation.z");
            String world = getConfig().getString("jailReleaseLocation.world");

            assert world != null;
            if (Bukkit.getWorld(world) != null) {
                jailReleaseLocation = new Location(Bukkit.getWorld(world), x+0.5, y+0.5, z+0.5);
            } else {
                getServer().getPluginManager().disablePlugin(this);
            }} else {
            getServer().getPluginManager().disablePlugin(this); }

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        this.jailReleaseDataFile = new File(getDataFolder(), "data.yml");
        this.jailCellNumberFile = new File(getDataFolder(), "cell.yml");
        try {
            this.jailReleaseDataFile.createNewFile();
            this.jailCellNumberFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.jailReleaseDataConfig = YamlConfiguration.loadConfiguration(this.jailReleaseDataFile);
        this.jailCellNumberConfig = YamlConfiguration.loadConfiguration(this.jailCellNumberFile);

        for (String uuidString : this.jailReleaseDataConfig.getKeys(false)) {
            long release=  this.jailReleaseDataConfig.getLong(uuidString);
            UUID uuid = UUID.fromString(uuidString);

            if (hasTimePassed(release)) {
                this.jailReleaseDataConfig.set(uuidString, null);
                continue;
                            }

            this.jailReleaseData.put(uuid, release);
        }
        for (String uuidString : this.jailCellNumberConfig.getKeys(false)) {
            int cellNumber =  this.jailCellNumberConfig.getInt(uuidString);
            UUID uuid = UUID.fromString(uuidString);
            this.jailCellNumber.put(uuid, cellNumber);
        }
        this.saveJailNumberData();



        new BukkitRunnable(){
            @Override
            public void run() {
                for (Map.Entry<UUID, Long> entry : jailReleaseData.entrySet()) {
            OfflinePlayer player = Bukkit.getPlayer(entry.getKey());
            if (hasTimePassed(entry.getValue())) {
                if (player != null) {
                releasePlayer(player.getPlayer(), true);
            }
            } else {

                if (player != null) {
                long current = System.currentTimeMillis();
                long difference = entry.getValue() - current;

                int secondsUntilRelease = (int) (difference / 1000);

                if (bossBarMap.containsKey(player)) {
                    bossBarMap.get(player).removePlayer(Objects.requireNonNull(player.getPlayer()));
                }

                BossBar bossBar = Bukkit.createBossBar("§cTime Left: " + getReadableTime(secondsUntilRelease), BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY);
                  bossBar.addPlayer(Objects.requireNonNull(player.getPlayer()));
                  bossBarMap.put(player.getPlayer(), bossBar);
              }
            }
        }
            }
        }.runTaskTimer(this, 0, 20);



    }


    private void saveJailNumberData() {
        try {
            this.jailCellNumberConfig.save(this.jailCellNumberFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void saveJailReleaseData() {
        try {
            this.jailReleaseDataConfig.save(this.jailReleaseDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {
        this.saveJailReleaseData();
        this.saveJailNumberData();
        super.onDisable();
    }

    public boolean isJailed(Player player) {
       return this.jailReleaseData.containsKey(player.getUniqueId()); }

    public int getCellNumber(Player player) {
        return this.jailCellNumber.getOrDefault(player.getUniqueId(), 10000);
    }

    public Map<UUID, Integer> getJailCellNumber() {
        return jailCellNumber;
    }

    public long getReleaseTime(OfflinePlayer player) {
        return this.jailReleaseData.getOrDefault(player.getUniqueId(), System.currentTimeMillis() - 5);
    }

    public boolean hasTimePassed(long time) {
        return time <= System.currentTimeMillis();
    }

    public Map<UUID, Integer> getReceivedHits() {
        return receivedHits;
    }

    public String getReadableTime(int time) {
        int sec = time % 60;
        int min = (time / 60) % 60;
        int hours = (time /60)/60;

        String out = "";
        if (hours != 0) out+= hours + "h ";
        if (min != 0) out+= min + "m ";
        if (sec != 0) out+= sec + "s";

        return out;

    }

    public Location getJailLocation(int nextCell) {
        return jailLocation.get(nextCell);
    }

    public Map<UUID, Long> getJailReleaseData() {
        return jailReleaseData;
    }

    public Map<Player, BossBar> getBossBarMap() {
        return bossBarMap;
    }

    public Location getJailReleaseLocation() {
        return jailReleaseLocation;
    }

    public ArrayList<String> getAllowedCommands() {
        return allowedCommands;
    }


    public void addToJail(Player player, long release) {
        int nextCell = this.jailReleaseData.size();
        this.jailReleaseData.put(player.getUniqueId(), release);
        this.jailCellNumber.put(player.getUniqueId(), nextCell);

        this.inventoryData.put(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();

        Objects.requireNonNull(player.getPlayer()).teleport(getJailLocation(nextCell));
    }

    public FileConfiguration getJailReleaseDataConfig() {
        return jailReleaseDataConfig;
    }


    public void jailPlayer(Player target, Player hitter, Integer terms) {
        if (target != null) {

                try {
                int kills = this.killCount.getOrDefault(target.getUniqueId(), 0);
                int defaultTime = this.getConfig().getInt("defaultTime");
                int minutesPerKill = this.getConfig().getInt("minutesPerKill");

                int timeInMiliSeconds = (defaultTime + (kills * minutesPerKill)) * 60000;
                long release = System.currentTimeMillis() + (timeInMiliSeconds);

                this.addToJail(target, release);
                hitter.sendMessage("§cJailed " + target.getName() + "§c for " + (defaultTime + (kills * minutesPerKill)) + " minutes!");
                target.sendMessage("\n§cYou have been jailed for " + (defaultTime + (kills * minutesPerKill)) + " minutes\n§cYou have killed a total of " + kills + " people.\n");
            } catch (Exception ex) {
                hitter.sendMessage("Please do not use partial numbers!");
                getServer().getConsoleSender().sendMessage(String.valueOf(ex));
            }
        } else {
            hitter.sendMessage("That player is offline!");
        }
    }

    public void jailPlayer(Player target, Integer terms) {
        if (terms == null) { terms = 1; }
        if (target != null) {

            try {
                int kills = this.killCount.getOrDefault(target.getUniqueId(), 0);
                int defaultTime = this.getConfig().getInt("defaultTime") * terms;
                int minutesPerKill = this.getConfig().getInt("minutesPerKill");

                int timeInMiliSeconds = (defaultTime + (kills * minutesPerKill)) * 60000;
                long release = System.currentTimeMillis() + (timeInMiliSeconds);

                this.addToJail(target, release);
               target.sendMessage("\n§cYou have been jailed by a judge for " + ((defaultTime * terms ) + (kills * minutesPerKill)) + " minutes\n§cYou have killed a total of " + kills + " people.\n");
            } catch (Exception ex) {
                getServer().getConsoleSender().sendMessage(String.valueOf(ex));
            }
        }
    }



    public void releasePlayer(Player player, boolean legit) {

        if (legit) {
            Objects.requireNonNull(player.getPlayer()).teleport(jailReleaseLocation);
            player.getPlayer().sendMessage("\n§7════════════════════════\n§a You have been released from jail\n§7════════════════════════\n ");
            jailReleaseDataConfig.set(player.getUniqueId().toString(), null);
            jailReleaseData.remove(player.getUniqueId());


            player.getPlayer().getInventory().setContents(inventoryData.get(player.getUniqueId()));
            player.getPlayer().updateInventory();

            saveJailReleaseData();

            if (bossBarMap.containsKey(player)) {
                bossBarMap.get(player).removePlayer(player.getPlayer());
            }
        } else {
            player.getPlayer().sendMessage("\n§7════════════════════════\n§c You have escaped from jail\n§7════════════════════════\n ");

            jailReleaseDataConfig.set(player.getUniqueId().toString(), null);
            jailReleaseData.remove(player.getUniqueId());


            player.getPlayer().getInventory().setContents(inventoryData.get(player.getUniqueId()));
            player.getPlayer().updateInventory();

            getConfig().getStringList("escapeCommands").forEach((command) -> {
                getServer().dispatchCommand(getServer().getConsoleSender(), command.replace("%player%", player.getName()));
            });

            saveJailReleaseData();
            if (bossBarMap.containsKey(player)) {
                bossBarMap.get(player).removePlayer(player.getPlayer());
            }


        }

    }
}
