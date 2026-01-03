package net.exenco.lightshow.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;


public class VectorUtils {

    public static Vector getDirectionVector(double yaw, double pitch) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);

        double x = Math.sin(yaw) * Math.cos(pitch);
        double y = Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vector(x, y, z);
    }

    public static Vector getDirectionVector(Vector origin, Vector target) {
        return target.clone().subtract(origin).normalize();
    }

    public static Vector rayTrace(Vector location, Vector baseRotation, float yaw, float pitch, double distance, World world) {
        if(distance == 0) {
            return location;
        }

        Vector direction = getDirectionVector(-yaw, -pitch + 90);
        Vector rotatedDirection = rotateVectorByVector(direction, baseRotation);

        // Ray trace
        Location startLocation = location.toLocation(world);
        RayTraceResult rayTraceResult = world.rayTraceBlocks(startLocation, rotatedDirection, distance, FluidCollisionMode.NEVER, true, block -> block.getBlockData().isOccluding());
        if (rayTraceResult != null) {
            return rayTraceResult.getHitPosition();
        }

        // Use rotated direction for fallback
        return location.clone().add(rotatedDirection.clone().multiply(distance));
    }

    public static float[] getYawPitchFromDirection(Vector direction) {
        float dx = (float) direction.getX();
        float dy = (float) direction.getY();
        float dz = (float) direction.getZ();

        float yawRad = (float) Math.atan2(-dx, dz);
        float pitchRad = (float) (Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[] {yawRad, pitchRad};
    }

    public static Quaternionf getRotationFromDirection(Vector direction) {
        float[] yawPitch = getYawPitchFromDirection(direction);
        float yawRad = yawPitch[0];
        float pitchRad = yawPitch[1] + (float) Math.PI / 2;

        Quaternionf rotationY = new Quaternionf().rotationXYZ(0, -yawRad, 0);
        Quaternionf rotationX = new Quaternionf().rotationXYZ(pitchRad, 0, 0);
        return rotationY.mul(rotationX);
    }

    public static Quaternionf getMagicQRotationQuaternion(Vector rotation) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(rotation.getY()))
                .rotateX((float) Math.toRadians(rotation.getX() + 180))
                .rotateZ((float) Math.toRadians(-rotation.getZ()));
    }

    public static Vector rotateVectorByVector(Vector vector, Vector rotation) {
        Quaternionf quaternion = getMagicQRotationQuaternion(rotation);
        return Vector.fromJOML(vector.toVector3f().rotate(quaternion));
    }

    public static Vector getDirectionFromRotation(Vector rotation) {
        Quaternionf quaternion = getMagicQRotationQuaternion(rotation);
        return Vector.fromJOML(new Vector(0, -1, 0).toVector3f().rotate(quaternion));
    }
}
