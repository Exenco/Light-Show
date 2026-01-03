package net.exenco.lightshow.show.effects;

import net.exenco.lightshow.util.VectorUtils;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.function.Supplier;

public class ParticleEffect extends ShowEffect {

    private final Particle particle;
    private final Vector direction;
    private final Vector randomOffset;

    public ParticleEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier, Particle particleType, Vector randomOffset) {
        super(world, origin, rotation, audienceSupplier);

        this.direction = VectorUtils.getDirectionFromRotation(rotation);
        this.particle = particleType;
        this.randomOffset = randomOffset;
    }

    public void spawnParticle(float speed) {
        World world = level.getWorld();

        world.spawnParticle(particle,
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                0,
                direction.getX() + (2 * randomOffset.getX() * Math.random() - randomOffset.getX()),
                direction.getY() + (2 * randomOffset.getY() * Math.random() - randomOffset.getY()),
                direction.getZ() + (2 * randomOffset.getZ() * Math.random() - randomOffset.getZ()),
                speed,
                null,
                true
        );
    }
}
