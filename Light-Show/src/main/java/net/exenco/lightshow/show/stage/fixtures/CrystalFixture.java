package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.api.EndCrystalApi;
import org.bukkit.FluidCollisionMode;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CrystalFixture extends ShowFixture {

    private final double maxDistance;
    private final EndCrystalApi endCrystalApi;

    private final PacketHandler packetHandler;
    public CrystalFixture(JsonObject configJson, StageManager stageManager) {
        super(configJson, stageManager);
        this.packetHandler = stageManager.getPacketHandler();

        this.endCrystalApi = new EndCrystalApi(this.location, stageManager.getPacketHandler());
        this.maxDistance = configJson.has("MaxDistance") ? configJson.get("MaxDistance").getAsDouble() : 100;

        this.endCrystalApi.spawn();
    }

    @Override
    public int getDmxSize() {
        return 5;
    }

    @Override
    public void applyState(int[] data) {
        double distance = valueOfMax(this.maxDistance, data[0]);
        float pan = 360 * -((float) (data[1]<<8 | data[2]) / 65535);
        float tilt = 360 * -((float) (data[3]<<8 | data[4]) / 65535);

        Vector destination = getDestination(pan, tilt, distance);
        endCrystalApi.setDestination(destination);
    }

    private Vector getDestination(float yaw, float pitch, double distance) {
        Vector vector = VectorUtils.getDirectionVector(yaw - 90, pitch + 90);
        Vector start = this.location.clone();

        if(distance == 0)
            return start.add(new Vector(0, -2, 0));

        return rayTrace(start, start.clone().add(vector.multiply(distance)));
    }

    private Vector rayTrace(Vector location, Vector destination) {
        Vector direction = destination.clone().subtract(location).normalize();
        World world = packetHandler.getLevel().getWorld();
        RayTraceResult rayTraceResult = world.rayTraceBlocks(location.toLocation(world), direction, location.distance(destination), FluidCollisionMode.NEVER, true);
        if(rayTraceResult != null)
            return rayTraceResult.getHitPosition();
        return destination;
    }
}
