package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.registries.LogoRegistry;
import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class LogoApi {
    private final ArrayList<ParticleLineApi> particleLineApis = new ArrayList<>();
    private final ArrayList<ParticleFlareApi> particleFlareApis = new ArrayList<>();

    private final double yaw;
    private final double pitch;

    private final PacketHandler packetHandler;
    public LogoApi(PacketHandler packetHandler, double yaw, double pitch) {
        this.packetHandler = packetHandler;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setLogo(Vector location, LogoRegistry.Logo logo, double size) {
        particleFlareApis.clear();
        particleLineApis.clear();

        if(logo == null)
            return;

        for(LogoRegistry.Logo.ParticleDot particleDot : logo.getParticleDotList()) {
            Vector loc = particleDot.location().clone().multiply(size);
            loc = VectorUtils.getRotatedVector(loc, yaw, pitch);

            ParticleFlareApi particleFlareApi = new ParticleFlareApi(loc.add(location), packetHandler);
            LogoRegistry.Logo.ParticleEntry particleEntry = particleDot.particleEntry();
            particleFlareApi.setParticle(particleEntry.particle());
            particleFlareApi.setData(particleEntry.data());
            particleFlareApis.add(particleFlareApi);
        }
        for(LogoRegistry.Logo.ParticleLine particleLine : logo.getParticleLineList()) {
            Vector origin = particleLine.origin().clone().multiply(size);
            Vector destination = particleLine.destination().clone().multiply(size);
            origin = VectorUtils.getRotatedVector(origin, yaw, pitch);
            destination = VectorUtils.getRotatedVector(destination, yaw, pitch);

            ParticleLineApi particleLineApi = new ParticleLineApi(origin.add(location), packetHandler);
            particleLineApi.setDestination(destination.add(location));
            particleLineApi.setMaxDistance(origin.distance(destination));
            LogoRegistry.Logo.ParticleEntry particleEntry = particleLine.particleEntry();
            particleLineApi.setParticle(particleEntry.particle());
            particleLineApi.setData(particleEntry.data());
            particleLineApis.add(particleLineApi);
        }
    }

    public void playLogo() {
        particleFlareApis.forEach(ParticleFlareApi::play);
        particleLineApis.forEach(ParticleLineApi::play);
    }
}
