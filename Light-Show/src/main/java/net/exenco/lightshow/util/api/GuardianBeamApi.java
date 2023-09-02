package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public class GuardianBeamApi {
    private static int teamId = 2000000;

    private final Vector start;
    private boolean spawned;
    private Vector destination;
    private Guardian entityGuardian;

    private Squid entitySquid;

    private final PacketHandler packetHandler;
    public GuardianBeamApi(Vector location, PacketHandler packetHandler) {
        this.start = location;
        this.packetHandler = packetHandler;
    }

    public void setDestination(Vector destination) {
        if(this.destination != null && this.destination.equals(destination))
            return;
        this.destination = destination;
        entitySquid.setPos(destination.getX(), destination.getY(), destination.getZ());
        packetHandler.moveEntity(entitySquid);
    }

    public void spawn() {
        if(this.start == null)
            return;
        spawned = true;
        Vector destination = this.destination;
        if(this.destination == null)
            destination = start;

        this.entityGuardian = new Guardian(EntityType.GUARDIAN, packetHandler.getLevel());
        this.entityGuardian.setPos(start.getX(), start.getY(), start.getZ());
        this.entityGuardian.setInvisible(true);
        this.entityGuardian.setSilent(true);

        this.entitySquid = new Squid(EntityType.SQUID, packetHandler.getLevel());
        this.entitySquid.setPos(destination.getX(), destination.getY(), destination.getZ());
        this.entitySquid.setInvisible(true);
        this.entitySquid.setSilent(true);

        setGuardianTarget(entityGuardian, entitySquid.getId());

        packetHandler.spawnEntity(entitySquid);
        packetHandler.spawnEntity(entityGuardian);

        UUID[] uuids = new UUID[] {entityGuardian.getUUID(), entitySquid.getUUID()};
        PlayerTeam scoreboardTeam = registerNewTeam("noClip" + teamId++, uuids);
        packetHandler.createTeam(scoreboardTeam);
    }

    public void destroy() {
        spawned = false;
        packetHandler.destroyEntity(entityGuardian.getId());
        packetHandler.destroyEntity(entitySquid.getId());
    }

    public void callColorChange() {
        if(this.entityGuardian == null)
            return;
        packetHandler.updateEntity(entityGuardian);
    }

    private void setGuardianTarget(Guardian entityGuardian, int entityId) {
        try {
            Method setSpikes = entityGuardian.getClass().getDeclaredMethod("w", boolean.class);
            Method setAttackId = entityGuardian.getClass().getDeclaredMethod("b", int.class);
            setSpikes.setAccessible(true);
            setAttackId.setAccessible(true);
            setSpikes.invoke(entityGuardian, false);
            setAttackId.invoke(entityGuardian, entityId);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private PlayerTeam registerNewTeam(String teamName, UUID[] uuids) {
        PlayerTeam scoreboardTeam = new PlayerTeam(new Scoreboard(), teamName);
        scoreboardTeam.setCollisionRule(Team.CollisionRule.NEVER);
        Collection<String> entries = scoreboardTeam.getPlayers();
        for(UUID uuid : uuids) {
            entries.add(uuid.toString());
        }
        return scoreboardTeam;
    }

    public boolean isSpawned() {
        return spawned;
    }
}