package net.exenco.lightshow.util;

import org.bukkit.util.Vector;

/**
 * Simple class containing a tad more complex Vector-Math.
 */
public class VectorUtils {
    /**
     * Rotates a {@link Vector} with given yaw and pitch.
     * @param vector to rotate.
     * @param yaw rotation around the y-axis
     * @param pitch rotation around the z-axis.
     * @return the rotated {@link Vector}.
     */
    public static Vector getRotatedVector(Vector vector, double yaw, double pitch) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);

        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        double x1 = x * Math.cos(yaw) + z * Math.sin(yaw);
        double z1 = -x * Math.sin(yaw) + z * Math.cos(yaw);

        double y2 = y * Math.cos(pitch) - z1 * Math.sin(pitch);
        double z2 = y * Math.sin(pitch) + z1 * Math.cos(pitch);

        return new Vector(x1, y2, z2);
    }

    /**
     * Calculates a directional {@link Vector} from yaw and pitch.
     * @param yaw rotation around the y-axis.
     * @param pitch rotaion around the z-axis.
     * @return the calculated directional {@link Vector}.
     */
    public static Vector getDirectionVector(double yaw, double pitch) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);

        double xzLen = Math.cos(pitch);

        double x = xzLen * Math.cos(-yaw);
        double y = Math.sin(pitch);
        double z = xzLen * Math.sin(yaw);

        return new Vector(x, y, z);
    }
}
