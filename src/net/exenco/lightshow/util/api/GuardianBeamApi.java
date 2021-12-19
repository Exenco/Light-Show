package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.EntitySquid;
import net.minecraft.world.entity.monster.EntityGuardian;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
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
    private EntityGuardian entityGuardian;

    private EntitySquid entitySquid;

    private final PacketHandler packetHandler;
    public GuardianBeamApi(Vector location, PacketHandler packetHandler) {
        this.start = location;
        this.packetHandler = packetHandler;
    }

    public void setDestination(Vector destination) {
        if(this.destination != null && this.destination.equals(destination))
            return;
        this.destination = destination;
        entitySquid.e(destination.getX(), destination.getY(), destination.getZ());
        packetHandler.moveEntity(entitySquid);
    }

    public void spawn() {
        if(this.start == null)
            return;
        spawned = true;
        Vector destination = this.destination;
        if(this.destination == null)
            destination = start;

        this.entityGuardian = new EntityGuardian(EntityTypes.K, packetHandler.getWorld());
        this.entityGuardian.e(start.getX(), start.getY(), start.getZ());
        this.entityGuardian.j(true);

        this.entitySquid = new EntitySquid(EntityTypes.aJ, packetHandler.getWorld());
        this.entitySquid.e(destination.getX(), destination.getY(), destination.getZ());
        this.entitySquid.j(true);

        setGuardianTarget(entityGuardian, entitySquid.ae());

        packetHandler.spawnEntity(entitySquid);
        packetHandler.spawnEntity(entityGuardian);

        UUID[] uuids = new UUID[] {entityGuardian.cm(), entitySquid.cm()};
        ScoreboardTeam scoreboardTeam = registerNewTeam("noClip" + teamId++, uuids);
        packetHandler.createTeam(scoreboardTeam);
    }

    public void destroy() {
        spawned = false;
        packetHandler.destroyEntity(entityGuardian.ae());
        packetHandler.destroyEntity(entitySquid.ae());
    }

    public void callColorChange() {
        if(this.entityGuardian == null)
            return;
        packetHandler.updateEntity(entityGuardian);
    }

    private void setGuardianTarget(EntityGuardian entityGuardian, int entityId) {
        try {
            Method setSpikes = entityGuardian.getClass().getDeclaredMethod("v", boolean.class);
            Method setAttackId = entityGuardian.getClass().getDeclaredMethod("a", int.class);
            setSpikes.setAccessible(true);
            setAttackId.setAccessible(true);
            setSpikes.invoke(entityGuardian, false);
            setAttackId.invoke(entityGuardian, entityId);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private ScoreboardTeam registerNewTeam(String teamName, UUID[] uuids) {
        ScoreboardTeam scoreboardTeam = new ScoreboardTeam(new Scoreboard(), teamName);
        scoreboardTeam.a(ScoreboardTeamBase.EnumTeamPush.b);
        Collection<String> entries = scoreboardTeam.g();
        for(UUID uuid : uuids) {
            entries.add(uuid.toString());
        }
        return scoreboardTeam;
    }

    public boolean isSpawned() {
        return spawned;
    }
}