package net.exenco.lightshow.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.exenco.lightshow.LightShowPlugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class ReloadCommand extends LiteralArgumentBuilder<CommandSourceStack> {

    private static final Permission permission = new Permission("lightshow.reload", "Reload command access", PermissionDefault.OP);

    protected ReloadCommand(LightShowPlugin plugin) {
        super("reload");

        this.requires(context -> context.getSender().hasPermission(permission))
            .executes(context -> {
                plugin.reload();
                // TODO: Add feedback to the user
                return 0;
            });
    }
}
