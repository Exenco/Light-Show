package net.exenco.lightshow.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.exenco.lightshow.LightShowPlugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class LsCommand extends LiteralArgumentBuilder<CommandSourceStack> {

    private static final Permission permission = new Permission("lightshow.ls", "Default access", PermissionDefault.OP);

    public LsCommand(LightShowPlugin plugin) {
        super("ls");

        this.requires(context -> context.getSender().hasPermission(permission))
            .then(
                new ReloadCommand(plugin)
            ).then(
                new ArtNetCommand(plugin)
            );
    }
}
