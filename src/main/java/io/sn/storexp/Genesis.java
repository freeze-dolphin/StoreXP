package io.sn.storexp;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Genesis extends JavaPlugin {

    public static Genesis PLUGIN;
    public static FileConfiguration CONFIG;
    public static NamespacedKey PDC_KEY;
    public static final MiniMessage MINIMSG = MiniMessage.miniMessage();

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        PLUGIN = this;
        PDC_KEY = new NamespacedKey(PLUGIN, "stored-xp");

        saveDefaultConfig();
        updateConfig();

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("storexp")
                            .then(Commands.literal("reload")
                                    .requires(source -> source.getSender().hasPermission("storexp.command.reload"))
                                    .executes(ctx -> {
                                        updateConfig();
                                        ctx.getSource().getSender().sendPlainMessage("StoreXP Reloaded!");
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .build()
                            )
                            .then(Commands.literal("give")
                                    .requires(src -> src.getSender().hasPermission("storexp.command.give"))
                                    .then(Commands.argument("target", ArgumentTypes.player())
                                            .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                                    .executes(ctx -> {
                                                        final List<Player> plr = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                                        final Integer value = ctx.getArgument("value", Integer.class);
                                                        plr.forEach(p -> Utils.giveBottleWithValue(p, value));
                                                        return Command.SINGLE_SUCCESS;
                                                    })))
                                    .build())
                            .build(),
                    "Admin command of StoreXP",
                    List.of()
            );
        });

        getServer().getPluginManager().registerEvents(new InteractListener(), this);
    }

    @Override
    public void onDisable() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        ProjectileLaunchEvent.getHandlerList().unregister(this);
        BlockDispenseEvent.getHandlerList().unregister(this);
    }

    public static void updateConfig() {
        PLUGIN.reloadConfig();
        CONFIG = PLUGIN.getConfig();
    }

}
