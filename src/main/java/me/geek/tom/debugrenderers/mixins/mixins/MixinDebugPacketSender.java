package me.geek.tom.debugrenderers.mixins.mixins;

import io.netty.buffer.Unpooled;
import me.geek.tom.debugrenderers.utils.PacketUtils;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.pathfinding.Path;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(DebugPacketSender.class)
public class MixinDebugPacketSender {

    /**
     * Sends the specified data to all players in the given world.
     *
     * @param world The server world with the players
     * @param buf The data to send to all players
     * @param channel The plugin messaging channel to send the data on
     */
    @Shadow
    private static void func_229753_a_(ServerWorld world, PacketBuffer buf, ResourceLocation channel) {}

    /**
     * Path debug packet format
     * |- The entity ID (int)
     * |- The distance of the path (float)
     * |- The path object (see PacketUtils#writePathToBuffer)
     *
     * @param world The world of the entity
     * @param entity The entity
     * @param path The path
     * @param distance How long the path is
     * @param ci CallBackInfo from the mixin injection
     */
    @Inject(at = @At("HEAD"),
            method = "sendPath")
    private static void onSendPath(World world, MobEntity entity, Path path, float distance, CallbackInfo ci) {
        if (!(world instanceof ServerWorld) || path == null) return;

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeInt(entity.getEntityId()).writeFloat(distance);
        PacketUtils.writePathToBuffer(buf, path);
        func_229753_a_((ServerWorld) world, buf, SCustomPayloadPlayPacket.DEBUG_PATH);
    }

    /**
     * Goal debug packet format
     * |- The block pos of the entity (int)
     * |- Length of the following list of goals
     * |- A list of goals (see PacketUtils#writeGoalToBuf)
     *
     * @param world The world of the entity
     * @param entity The entity
     * @param selector The goal selector to send information for
     * @param ci CallbackInfo from the mixin injection
     */
    @Inject(at = @At("HEAD"),
            method = "sendGoal")
    private static void onSendGoal(World world, MobEntity entity, GoalSelector selector, CallbackInfo ci) {
        if (!(world instanceof ServerWorld)) return;

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBlockPos(entity.getPosition());
        List<PrioritizedGoal> goals = selector.getRunningGoals().collect(Collectors.toList());
        buf.writeInt(entity.getEntityId()).writeInt(goals.size());
        for (int i = 0; i < goals.size(); i++) {
            PacketUtils.writeGoalToBuf(buf, i, goals.get(i));
        }
        func_229753_a_((ServerWorld) world, buf, SCustomPayloadPlayPacket.DEBUG_GOAL_SELECTOR);
    }

    /**
     * POI Removed debug packet format
     * |- The position of the removed POI (BlockPos)
     *
     * @param world The world of the event
     * @param pos The position of the POI
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "func_218805_b", // POI Removed
            at = @At("HEAD"))
    private static void onPoiRemoved(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBlockPos(pos);
        func_229753_a_(world, buf, SCustomPayloadPlayPacket.DEBUG_POI_REMOVED);
    }

    /**
     * POI Added debug packet format
     * |- Position (BlockPos)
     * |- POI Type (String)
     * |- Ammount of tickets (int)
     *
     * @param world The world of the event
     * @param pos The position of the POI
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "func_218799_a",
            at = @At("HEAD"))
    private static void onPoiAdded(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeBlockPos(pos);
        buf.writeString(world.getBlockState(pos).getBlock().getClass().getSimpleName());
        buf.writeInt(0);

        func_229753_a_(world, buf, SCustomPayloadPlayPacket.DEBUG_POI_ADDED);
    }

    /**
     * Hive debug info packet format
     * |- Position of the hive (BlockPos)
     * |- Hive type (String)
     * |- Bee count (int)
     * |- Honey level (int)
     * |- Is sedated (bool)
     *
     * @param te The BeehiveTileEntity
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "sendBeehiveDebugData",
            at = @At("HEAD"))
    private static void onSendBeehiveDebugData(BeehiveTileEntity te, CallbackInfo ci) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeBlockPos(te.getPos());
        buf.writeString(te.getClass().getSimpleName());
        buf.writeInt(te.getBeeCount());
        buf.writeInt(te.getBlockState().get(BlockStateProperties.HONEY_LEVEL));
        buf.writeBoolean(te.isSmoked());

        func_229753_a_((ServerWorld) te.getWorld(), buf, SCustomPayloadPlayPacket.field_229728_n_);
    }
}
