package net.exenco.lightshow.commands.artnet;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.exenco.lightshow.LightShowPlugin;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.artnet.ArtNetReceiver;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class StartCommand extends LiteralArgumentBuilder<CommandSourceStack> {

    private static final Permission permission = new Permission("lightshow.artnet.start", "Access Art-Net start command", PermissionDefault.OP);

    public StartCommand(LightShowPlugin plugin) {
        super("start");

        ShowStage stage = plugin.getStage();

        this.requires(context -> context.getSender().hasPermission(permission))
            .executes(context -> {
                ArtNetReceiver artNetReceiver = stage.getArtNetReceiver();

                if (artNetReceiver.isRunning()) {
                    // TODO: Add feedback to the user
                    return 0;
                }
                boolean success = artNetReceiver.start();
                // TODO: Add feedback to the user
                return success ? 1 : 0;
            });
    }
}
