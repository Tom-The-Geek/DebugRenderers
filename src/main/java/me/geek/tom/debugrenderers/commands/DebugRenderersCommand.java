package me.geek.tom.debugrenderers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.geek.tom.debugrenderers.commands.arguments.RendererTypeArgument;
import me.geek.tom.debugrenderers.utils.PacketUtils;
import me.geek.tom.debugrenderers.utils.RenderersState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("SameReturnValue")
public class DebugRenderersCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("drenders")
                    .then(helpCommand(dispatcher))
                    .then(alloffCommand(dispatcher))
                    .then(toggleCommand(dispatcher))
                    .then(resetCommand(dispatcher))
                    .executes(DebugRenderersCommand::help)
        );
    }

    private static final ITextComponent HELP_TITLE = new TranslationTextComponent("drenders.command.help.title");
    private static final ITextComponent HELP_USAGE = new TranslationTextComponent("drenders.command.usage");

    static {
        HELP_TITLE.setStyle(HELP_TITLE.getStyle().setColor(TextFormatting.DARK_PURPLE));
        HELP_USAGE.setStyle(HELP_USAGE.getStyle().setColor(TextFormatting.LIGHT_PURPLE));
    }

    private static ArgumentBuilder<CommandSource, ?> helpCommand(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("help")
                .requires((sender) -> sender.hasPermissionLevel(0))
                .executes(DebugRenderersCommand::help);
    }

    private static ArgumentBuilder<CommandSource, ?> alloffCommand(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("alloff")
                .requires((sender) -> sender.hasPermissionLevel(0))
                .executes(DebugRenderersCommand::alloff);
    }

    private static ArgumentBuilder<CommandSource, ?> toggleCommand(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("toggle")
                .requires((sender) -> sender.hasPermissionLevel(0))
                .then(Commands.argument("renderer", RendererTypeArgument.rendererType())
                    .executes(DebugRenderersCommand::toggle));
    }

    private static ArgumentBuilder<CommandSource, ?> resetCommand(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("reset")
                .requires((sender) -> sender.hasPermissionLevel(0))
                .executes(DebugRenderersCommand::reset);
    }

    private static int help(CommandContext<CommandSource> ctx) {
        ctx.getSource().sendFeedback(HELP_TITLE, true);
        ctx.getSource().sendFeedback(HELP_USAGE, true);

        return 0;
    }

    private static int reset(CommandContext<CommandSource> ctx) {
        PacketUtils.sendReset(ctx.getSource().getWorld());
        ctx.getSource().sendFeedback(new TranslationTextComponent("drenders.command.reset.ok"), true);

        return 0;
    }

    private static int toggle(CommandContext<CommandSource> ctx) {
        ctx.getSource().sendFeedback(new TranslationTextComponent("drenders.command.toggle.ok"), true);
        RenderersState.RendererType renderer = RendererTypeArgument.getUuid("renderer", ctx);
        RenderersState.INSTANCE.toggle(renderer);

        return 0;
    }

    private static int alloff(CommandContext<CommandSource> ctx) {
        ctx.getSource().sendFeedback(new TranslationTextComponent("drenders.command.alloff.ok"), true);
        RenderersState.INSTANCE.disableAll();

        return 0;
    }
}
