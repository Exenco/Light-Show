package net.exenco.lightshow.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.exenco.lightshow.LightShowPlugin;
import net.exenco.lightshow.commands.artnet.StartCommand;
import net.exenco.lightshow.commands.artnet.StopCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class ArtNetCommand extends LiteralArgumentBuilder<CommandSourceStack> {

    private static final Permission permission = new Permission("lightshow.artnet", "Access Art-Net configuring", PermissionDefault.OP);

    protected ArtNetCommand(LightShowPlugin plugin) {
        super("artnet");

        this.requires(context -> context.getSender().hasPermission(permission))
            .then(
                new StartCommand(plugin)
            ).then(
                new StopCommand(plugin)
            );
    }
}
