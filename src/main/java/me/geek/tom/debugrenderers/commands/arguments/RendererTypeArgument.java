package me.geek.tom.debugrenderers.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.geek.tom.debugrenderers.utils.RenderersState;

import java.util.Collection;
import java.util.stream.Collectors;

public class RendererTypeArgument implements ArgumentType<RenderersState.RendererType> {

    public static RendererTypeArgument rendererType() {
        return new RendererTypeArgument();
    }

    public static <S> RenderersState.RendererType getUuid(String name, CommandContext<S> context) {
        return context.getArgument(name, RenderersState.RendererType.class);
    }

    private static final Collection<String> EXAMPLES = Lists.newArrayList(RenderersState.RendererType.values())
            .stream().map(Enum::toString).map(String::toLowerCase).collect(Collectors.toList());

    @Override
    public RenderersState.RendererType parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }


        while (reader.canRead() && reader.peek() != ' ')
            reader.skip();


        String name = reader.getString().substring(argBeginning, reader.getCursor());

        try {
            return RenderersState.RendererType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(new LiteralMessage(e.getMessage())).createWithContext(reader);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
