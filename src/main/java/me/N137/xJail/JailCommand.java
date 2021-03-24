package me.N137.xJail;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;

public class JailCommand implements CommandExecutor {

    private xJail jail;

    public JailCommand(xJail jail) {
        this.jail = jail;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jail")) {

            if (args.length == 0) {
                this.jail.sendHelp(sender);
                return false;
            } else if (args[0].equalsIgnoreCase("free")) {
                if (sender.hasPermission("xjail.free")) {
                    if (args.length == 2) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage("§cThat player is not online or has never joined: " + args[1]);
                        } else {
                            if (jail.isJailed(target)) {
                                if (jail.getBossBarMap().containsKey(target)) {
                                    jail.getBossBarMap().get(target).removePlayer(target);
                                }
                                jail.releasePlayer(target, true);
                            }
                        }
                    } else {
                        sender.sendMessage("§cUsage: /jail free <name>");
                    }

                } else { sender.sendMessage("§cNo Permission!");}
            } else if (args[0].equalsIgnoreCase("jail")) { // /jail jail <user> <terms>
                if (sender.hasPermission("xjail.jail")) {
                    if (args.length == 2) {

                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage("§cThat player is not online or has never joined: " + args[1]);
                        } else {
                            if (!jail.isJailed(target)) {
                                PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false);
                                target.addPotionEffect(blindness);
                                jail.jailPlayer(target, 1);
                            }
                        }
                    } else if (args.length == 3) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage("§cThat player is not online or has never joined: " + args[1]);
                        } else {
                            if (!jail.isJailed(target)) {
                                PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false);
                                target.addPotionEffect(blindness);
                                jail.jailPlayer(target, Integer.parseInt(args[2]));
                            }
                        }
                    }
                } else {
                    sender.sendMessage("§cNo permission.");
                }
            } else if (args[0].equalsIgnoreCase("getstick")) {
                if (sender.hasPermission("xjail.getstick")) {
                    Player target = (Player) sender;

                        ItemStack jailstick = new ItemStack(Material.GOLD_HOE);
                        ItemMeta jailstickMeta = jailstick.getItemMeta();
                        assert jailstickMeta != null;
                        jailstickMeta.setDisplayName("§aJailstick");
                            ArrayList<String> jailstickLore = new ArrayList<>();
                            jailstickLore.add("§8Hit a player 3 times");
                            jailstickLore.add("§8to send them to jail");
                            jailstickMeta.setLore(jailstickLore);

                        jailstickMeta.spigot().setUnbreakable(true);
                        jailstick.setItemMeta(jailstickMeta);

                        target.getInventory().addItem(jailstick);

                }
            }  else if (args[0].equalsIgnoreCase("admin")) {
                if (sender.hasPermission("xjail.admin")) {
                    if (args[1].equalsIgnoreCase("add")) {
                        Player player = (Player) sender;
                        int amountOfJails = Objects.requireNonNull(jail.getConfig().getConfigurationSection("jailLocation")).getKeys(false).size();

                        int x = (int) Math.round(player.getLocation().getX());
                        int y = (int) Math.round(player.getLocation().getY());
                        int z = (int) Math.round(player.getLocation().getZ());
                        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();
                        // Adding a location
                        jail.getConfig().set("jailLocation." + amountOfJails+ ".x", x);
                        jail.getConfig().set("jailLocation." + amountOfJails+ ".y", y);
                        jail.getConfig().set("jailLocation." + amountOfJails+ ".z", z);
                        jail.getConfig().set("jailLocation." + amountOfJails+ ".world", world);

                        jail.saveConfig();
                        player.sendMessage("§7You have added ("+x+", "+y+", "+z+") as a jail in the world "+ world);

                    } else if (args[1].equalsIgnoreCase("list")) {
                        // Listing all locations
                        int amountOfJails = Objects.requireNonNull(jail.getConfig().getConfigurationSection("jailLocation")).getKeys(false).size();
                        sender.sendMessage("§7═════════════ §a xJail §7══════════════");
                        for (int i = 0; i < amountOfJails; i++) {
                            sender.sendMessage("§7" + i + "» " + jail.getConfig().get("jailLocation."+i+".world") +  " (" + jail.getConfig().get("jailLocation."+i+".x") + ", "+ jail.getConfig().get("jailLocation."+i+".y") + ", " + jail.getConfig().get("jailLocation."+i+".z") + ")");
                        }
                    } else if (args[1].equalsIgnoreCase("release")) {
                        Player player = (Player) sender;

                        int x = (int) Math.round(player.getLocation().getX());
                        int y = (int) Math.round(player.getLocation().getY());
                        int z = (int) Math.round(player.getLocation().getZ());
                        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();
                        // Adding a location
                        jail.getConfig().set("jailReleaseLocation.x", x);
                        jail.getConfig().set("jailReleaseLocation.y", y);
                        jail.getConfig().set("jailReleaseLocation.z", z);
                        jail.getConfig().set("jailReleaseLocation.world", world);

                        jail.saveConfig();
                        jail.getJailReleaseLocation().setX(x);
                        jail.getJailReleaseLocation().setY(y);
                        jail.getJailReleaseLocation().setZ(z);
                        jail.getJailReleaseLocation().setWorld(Objects.requireNonNull(player.getLocation().getWorld()));

                        player.sendMessage("§7You have changed the release location to ("+x+", "+y+", "+z+").");
                    }
                } else {
                    sender.sendMessage("§cYou are not allowed to use that command.");
                }
            } else {
                this.jail.sendHelp(sender);
            }
        }
        return false;
    }




}
