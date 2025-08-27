package com.lucas.dailyevent;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class GlobalListener implements Listener {

    private final SeasonManager seasonManager;
    private final Random random = new Random();

    public GlobalListener(SeasonManager seasonManager) {
        this.seasonManager = seasonManager;
    }

    // Blood: mobs spawn en masse avec buff +30% vie/dégâts, loots x1.5, mobs neutres agressifs
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        if (seasonManager.getCurrentSeason() == Season.BLOOD) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            
            // Buff +30% vie et dégâts
            if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                double base = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(base * 1.3);
                entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
            if (entity.getType() != EntityType.PLAYER && entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                double dmg = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(dmg * 1.3);
            }
            
            // Rendre les mobs neutres agressifs
            if (entity instanceof Enderman) {
                // Enderman devient agressif - on ajoute un effet de force pour simuler l'agressivité
                entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, true, false));
            } else if (entity instanceof PigZombie) {
                ((PigZombie) entity).setAngry(true);
            } else if (entity instanceof Wolf) {
                ((Wolf) entity).setAngry(true);
            }
        }

        // Nocturne: extra spawns at night and possible blood moon buffs
        if (seasonManager.getCurrentSeason() == Season.NOCTURNE) {
            World world = event.getLocation().getWorld();
            if (world != null) {
                long time = world.getTime();
                boolean isNight = time >= 13000 && time <= 23000;
                if (isNight) {
                    if (seasonManager.isBloodMoonTonight()) {
                        if (event.getEntity() instanceof LivingEntity) {
                            LivingEntity le = (LivingEntity) event.getEntity();
                            le.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60 * 10, 1));
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, 0));
                        }
                    }
                }
            }
        }

        // Storm: plus de creepers chargés et squelettes sur chevaux
        if (seasonManager.getCurrentSeason() == Season.STORM) {
            if (event.getEntity() instanceof Creeper) {
                if (random.nextDouble() < 0.3) { // 30% chance
                    ((Creeper) event.getEntity()).setPowered(true);
                }
            } else if (event.getEntity() instanceof Skeleton) {
                if (random.nextDouble() < 0.2) { // 20% chance
                    Skeleton skeleton = (Skeleton) event.getEntity();
                    Horse horse = (Horse) event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.HORSE);
                    horse.setTamed(true);
                    // On ne peut pas définir un skeleton comme propriétaire, on utilise juste le cheval
                    skeleton.teleport(horse.getLocation());
                    horse.addPassenger(skeleton);
                }
            }
        }

        // Tenebre: spawn dans les spawner x2
        if (seasonManager.getCurrentSeason() == Season.TENEBRE) {
            if (event.getLocation().getBlock().getType() == Material.SPAWNER) {
                // Double spawn chance
                if (random.nextDouble() < 0.5) {
                    // Spawn un deuxième mob du même type
                    EntityType entityType = event.getEntity().getType();
                    event.getLocation().getWorld().spawnEntity(event.getLocation(), entityType);
                }
            }
        }
    }

    // Gestion combinée de tous les effets liés au mouvement du joueur
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // Nocturne: longer nights
        if (seasonManager.getCurrentSeason() == Season.NOCTURNE) {
            long time = world.getTime();
            if (time >= 13000 && time <= 23000) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (seasonManager.getCurrentSeason() != Season.NOCTURNE) {
                            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                            cancel();
                            return;
                        }
                        long t = world.getTime();
                        if (t < 23000) {
                            world.setTime(t + 5);
                        }
                    }
                }.runTaskTimer(DailyEventPlugin.getInstance(), 20L, 20L);
            } else {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        }
        
        // Storm: orage/pluie permanent
        if (seasonManager.getCurrentSeason() == Season.STORM) {
            if (!world.hasStorm()) {
                world.setStorm(true);
                world.setThundering(true);
            }
        }
        
        // Tenebre: nuit ultra sombre avec Blindness léger
        if (seasonManager.getCurrentSeason() == Season.TENEBRE) {
            long time = world.getTime();
            if (time >= 13000 && time <= 23000) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false));
            }
        }
    }

    // Famine: nourriture nourrit 50% moins, saturation baisse plus vite, minerais donnent parfois 2 drops
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (seasonManager.getCurrentSeason() == Season.FAMINE) {
            if (event.getEntity() instanceof Player) {
                int old = ((Player) event.getEntity()).getFoodLevel();
                int newLevel = event.getFoodLevel();
                int delta = newLevel - old;
                if (delta > 0) {
                    int reduced = old + Math.max(1, delta / 2);
                    event.setFoodLevel(Math.min(20, reduced));
                }
                
                // Saturation baisse plus vite
                Player player = (Player) event.getEntity();
                float saturation = player.getSaturation();
                player.setSaturation(saturation * 0.7f);
            }
        }
    }

    // Famine: minerais donnent parfois 2 drops (bonus farm)
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (seasonManager.getCurrentSeason() == Season.FAMINE) {
            Material type = event.getBlock().getType();
            List<Material> ores = List.of(
                    Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
                    Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                    Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                    Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                    Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
                    Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                    Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                    Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE
            );
            if (ores.contains(type) && random.nextDouble() < 0.3) { // 30% chance
                // Drop un deuxième item
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(type));
            }
        }
    }

    // Blood: loots des mobs augmentés (x1.5)
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (seasonManager.getCurrentSeason() == Season.BLOOD) {
            List<ItemStack> drops = event.getDrops();
            for (ItemStack drop : drops) {
                if (random.nextDouble() < 0.5) { // 50% chance de doubler
                    drops.add(drop.clone());
                }
            }
        }
    }
}


