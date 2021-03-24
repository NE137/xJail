package me.N137.xJail;

import com.mewin.WGRegionEvents.MovementWay;
import com.mewin.WGRegionEvents.events.RegionLeftEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class JailListener implements Listener {

    private xJail jail;
    public JailListener(xJail jail) {
        this.jail = jail;
    }




    @EventHandler
    public void onJailstickHit(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {

            Player hitter = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            UUID victimUUID = victim.getUniqueId();
            ItemStack item = hitter.getInventory().getItemInMainHand();

            if (hitter.getInventory().getItemInMainHand().getType().equals(Material.GOLD_HOE)) {
               if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§aJailstick")) {
                   if (hitter.hasPermission("xjail.jailstick.use")) {
                       if (!victim.hasPermission("xjail.immune")) {
                           if (!victim.hasMetadata("NPC")) {
                       if (jail.receivedHits.containsKey(victimUUID)) {

                           if (jail.isJailed(victim)) {
                               // User is already in jail, send back to cells
                               victim.teleport(jail.getJailLocation(jail.getCellNumber(victim)));
                               PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false);
                               victim.addPotionEffect(blindness);
                               victim.sendMessage("§cYou have been sent back to your cell by " + hitter.getName());
                               hitter.sendMessage("§cYou have returned " + victim.getName() + " back to their cell.");
                           } else {

                           if (jail.getReceivedHits().getOrDefault(victimUUID, 0) < jail.getConfig().getInt("jailstickHitsRequired")) {
                               jail.getReceivedHits().put(victimUUID, jail.getReceivedHits().getOrDefault(victimUUID, 0) + 1);
                               hitter.sendMessage("§6You have to hit " + victim.getName() + "§6 " + (jail.getConfig().getInt("jailstickHitsRequired") - jail.getReceivedHits().getOrDefault(victimUUID, 0) + 1) + "§6 more times to jail them.");

                               new BukkitRunnable() {
                                   @Override
                                   public void run() {
                                       try {
                                           if (jail.getReceivedHits().containsKey(victimUUID)) {
                                               jail.getReceivedHits().put(victimUUID, jail.getReceivedHits().get(victimUUID) - 1);
                                               if (jail.getReceivedHits().get(victimUUID) < 0) {
                                                   jail.getReceivedHits().put(victimUUID, 0);
                                               }
                                           }
                                       } catch (Exception ex) {
                                           //
                                       }
                                   }
                               }.runTaskLater(jail, 200);

                           } else {
                               PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false);
                               victim.addPotionEffect(blindness);
                               jail.jailPlayer(victim, hitter, 1);
                               jail.getReceivedHits().put(victimUUID, 0);
                           }
                       } } else {
                           jail.getReceivedHits().put(victimUUID, 1);
                       }
                     }}
                   } else {
                       hitter.sendMessage("§cYou are not allowed to use jailsticks!");
                       hitter.getInventory().getItemInMainHand().setAmount(0);
                   }

               }
}}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)  {

        Player player = event.getPlayer();
        try {
            jail.receivedHits.put(player.getUniqueId(), 0);
        } catch (Exception e) {
            //
        }

        if (jail.isJailed(player)) {
          player.teleport(jail.getJailLocation(jail.getCellNumber(player)));
            for (Map.Entry<UUID, Long> entry : jail.getJailReleaseData().entrySet()) {
                Player op = Bukkit.getPlayer(entry.getKey());
                if (jail.hasTimePassed(entry.getValue())) {
                    assert op != null;
                    jail.getJailReleaseData().remove(op.getUniqueId());
                    if (jail.getBossBarMap().containsKey(player)) {
                        jail.getBossBarMap().get(player).removePlayer(player);
                    }
                   jail.releasePlayer(player, true);
                } }
       }
    }

    @EventHandler
    public void onPrisonEscape(RegionLeftEvent event) {
        if (jail.isJailed(event.getPlayer())) {
            if (event.getMovementWay().equals(MovementWay.MOVE)) {
                    if (event.getRegion().getId().equalsIgnoreCase(jail.getConfig().getString("region"))) {
                        // user escaped from prison, give their inventory
                        jail.releasePlayer(event.getPlayer(), false);
                    }
            }

            if (event.getMovementWay().equals(MovementWay.TELEPORT)) {
                Objects.requireNonNull(event.getPlayer()).teleport(jail.getJailLocation(jail.getCellNumber(event.getPlayer())));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (jail.isJailed(event.getPlayer())) {
          if ((jail.getAllowedCommands().contains(event.getMessage().split(" ")[0]))) {
             event.setCancelled(false);
          } else {
              event.setCancelled(true);
              event.getPlayer().sendMessage("§cYou can't use commands in jail.");

          }
    } }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (jail.isJailed(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou can't place blocks in jail.");
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (jail.isJailed(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou can't break blocks in jail.");
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (jail.isJailed((Player) event.getWhoClicked())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageTaken(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (jail.isJailed(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (jail.isJailed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }




    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            jail.killCount.put(killer.getUniqueId(), jail.killCount.getOrDefault(killer.getUniqueId(), 0)+1);
        }
        jail.killCount.put(event.getEntity().getUniqueId(), 0);


    }



}
