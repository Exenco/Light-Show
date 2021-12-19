package net.exenco.lightshow.util;

import org.bukkit.Particle;

public enum ParticleRegistry {
    EXPLOSION_NORMAL(0, Particle.EXPLOSION_NORMAL),
    EXPLOSION_LARGE(1, Particle.EXPLOSION_LARGE),
    EXPLOSION_HUGE(2, Particle.EXPLOSION_HUGE),
    FIREWORKS_SPARK(3, Particle.FIREWORKS_SPARK),
    WATER_BUBBLE(4, Particle.WATER_BUBBLE),
    WATER_SPLASH(5, Particle.WATER_SPLASH),
    WATER_WAKE(6, Particle.WATER_WAKE),
    SUSPENDED(7, Particle.SUSPENDED),
    SUSPENDED_DEPTH(8, Particle.SUSPENDED_DEPTH),
    CRIT(9, Particle.CRIT),
    CRIT_MAGIC(10, Particle.CRIT_MAGIC),
    SMOKE_NORMAL(11, Particle.SMOKE_NORMAL),
    SMOKE_LARGE(12, Particle.SMOKE_LARGE),
    SPELL(13, Particle.SPELL),
    SPELL_INSTANT(14, Particle.SPELL_INSTANT),
    SPELL_MOB(15, Particle.SPELL_MOB),
    SPELL_MOB_AMBIENT(16, Particle.SPELL_MOB_AMBIENT),
    SPELL_WITCH(17, Particle.SPELL_WITCH),
    DRIP_WATER(18, Particle.DRIP_WATER),
    DRIP_LAVA(19, Particle.DRIP_LAVA),
    VILLAGER_ANGRY(20, Particle.VILLAGER_ANGRY),
    VILLAGER_HAPPY(21, Particle.VILLAGER_HAPPY),
    TOWN_AURA(21, Particle.TOWN_AURA),
    NOTE(22, Particle.NOTE),
    PORTAL(23, Particle.PORTAL),
    ENCHANTMENT_TABLE(24, Particle.ENCHANTMENT_TABLE),
    FLAME(25, Particle.FLAME),
    LAVA(26, Particle.LAVA),
    CLOUD(27, Particle.CLOUD),
    SNOWBALL(28, Particle.SNOWBALL),
    SNOW_SHOVEL(29, Particle.SNOW_SHOVEL),
    SLIME(30, Particle.SLIME),
    HEART(31, Particle.HEART),
    WATER_DROP(33, Particle.WATER_DROP),
    MOB_APPEARANCE(34, Particle.MOB_APPEARANCE),
    DRAGON_BREATH(35, Particle.DRAGON_BREATH),
    END_ROD(36, Particle.END_ROD),
    DAMAGE_INDICATOR(37, Particle.DAMAGE_INDICATOR),
    SWEEP_ATTACK(38, Particle.SWEEP_ATTACK),
    TOTEM(39, Particle.TOTEM),
    SPIT(40, Particle.SPIT),
    SQUID_INK(41, Particle.SQUID_INK),
    BUBBLE_POP(42, Particle.BUBBLE_POP),
    CURRENT_DOWN(43, Particle.CURRENT_DOWN),
    BUBBLE_COLUMN_UP(44, Particle.BUBBLE_COLUMN_UP),
    NAUTILUS(45, Particle.NAUTILUS),
    DOLPHIN(46, Particle.DOLPHIN),
    SNEEZE(47, Particle.SNEEZE),
    CAMPFIRE_COSY_SMOKE(48, Particle.CAMPFIRE_COSY_SMOKE),
    CAMPFIRE_SIGNAL_SMOKE(49, Particle.CAMPFIRE_COSY_SMOKE),
    COMPOSTER(50, Particle.COMPOSTER),
    FLASH(51, Particle.FLASH),
    FALLING_LAVA(52, Particle.FALLING_LAVA),
    LANDING_LAVA(53, Particle.LANDING_LAVA),
    FALLING_WATER(54, Particle.FALLING_WATER),
    DRIPPING_HONEY(55, Particle.DRIPPING_HONEY),
    FALLING_HONEY(56, Particle.FALLING_HONEY),
    LANDING_HONEY(57, Particle.LANDING_HONEY),
    FALLING_NECTAR(58, Particle.FALLING_NECTAR),
    SOUL_FIRE_FLAME(59, Particle.SOUL_FIRE_FLAME),
    ASH(60, Particle.ASH),
    CRIMSON_SPORE(61, Particle.CRIMSON_SPORE),
    WARPED_SPORE(62, Particle.WARPED_SPORE),
    SOUL(63, Particle.SOUL),
    DRIPPING_OBSIDIAN_TEAR(64, Particle.DRIPPING_OBSIDIAN_TEAR),
    FALLING_OBSIDIAN_TEAR(65, Particle.FALLING_OBSIDIAN_TEAR),
    LANDING_OBSIDIAN_TEAR(66, Particle.LANDING_OBSIDIAN_TEAR),
    REVERSE_PORTAL(67, Particle.REVERSE_PORTAL),
    WHITE_ASH(68, Particle.WHITE_ASH),
    FALLING_SPORE_BLOSSOM(70, Particle.FALLING_SPORE_BLOSSOM),
    SPORE_BLOSSOM_AIR(71, Particle.SPORE_BLOSSOM_AIR),
    SMALL_FLAME(72, Particle.SMALL_FLAME),
    SNOWFLAKE(73, Particle.SNOWFLAKE),
    DRIPPING_DRIPSTONE_LAVA(74, Particle.DRIPPING_DRIPSTONE_LAVA),
    FALLING_DRIPSTONE_LAVA(75, Particle.DRIPPING_DRIPSTONE_LAVA),
    DRIPPING_DRIPSTONE_WATER(76, Particle.DRIPPING_DRIPSTONE_WATER),
    FALLING_DRIPSTONE_WATER(77, Particle.FALLING_DRIPSTONE_WATER),
    GLOW_SQUID_INK(78, Particle.GLOW_SQUID_INK),
    GLOW(79, Particle.GLOW),
    WAX_ON(80, Particle.WAX_ON),
    WAX_OFF(81, Particle.WAX_OFF),
    ELECTRIC_SPARK(82, Particle.ELECTRIC_SPARK),
    SCRAPE(83, Particle.SCRAPE),
    REDSTONE(100, Particle.REDSTONE);

    private final int id;
    private final Particle bukkitParticle;

    ParticleRegistry(int id, Particle bukkitParticle) {
        this.id = id;
        this.bukkitParticle = bukkitParticle;
    }

    public static Particle getById(int id) {
        for(ParticleRegistry particleRegistry : ParticleRegistry.values())
            if(particleRegistry.getId() == id)
                return particleRegistry.getBukkitParticle();
            return null;
    }

    private int getId() {
        return id;
    }

    public Particle getBukkitParticle() {
        return bukkitParticle;
    }
}
