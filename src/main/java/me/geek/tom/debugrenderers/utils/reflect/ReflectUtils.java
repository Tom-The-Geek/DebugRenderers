package me.geek.tom.debugrenderers.utils.reflect;

import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

public class ReflectUtils {

    public static final MappedMethod DebugPacketSender_func_229753_a_ =
            new MappedMethod.Builder(DebugPacketSender.class)
            .mcp("func_229753_a_")
            .srg("func_229753_a_")
                    .arg(ServerWorld.class)
                    .arg(PacketBuffer.class)
                    .arg(ResourceLocation.class)
            .build();

}
