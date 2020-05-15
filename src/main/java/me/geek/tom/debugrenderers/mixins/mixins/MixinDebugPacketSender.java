package me.geek.tom.debugrenderers.mixins.mixins;

import io.netty.buffer.Unpooled;
import me.geek.tom.debugrenderers.utils.PacketUtils;
import me.geek.tom.debugrenderers.utils.RenderersState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.pathfinding.Path;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static me.geek.tom.debugrenderers.utils.RenderersState.RendererType.*;

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
     * Path debug packet format<br>
     * |- The entity ID (int)<br>
     * |- The distance of the path (float)<br>
     * |- The path object (see {@link PacketUtils#writePathToBuffer(PacketBuffer, Path)})
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
        if (!(world instanceof ServerWorld) || path == null || !RenderersState.INSTANCE.get(PATHFINDING)) return;

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeInt(entity.getEntityId()).writeFloat(distance);
        PacketUtils.writePathToBuffer(buf, path);
        func_229753_a_((ServerWorld) world, buf, SCustomPayloadPlayPacket.DEBUG_PATH);
    }

    /**
     * Goal debug packet format<br>
     * |- The block pos of the entity (int)<br>
     * |- Length of the following list of goals<br>
     * |- A list of goals (see {@link PacketUtils#writeGoalToBuf(PacketBuffer, int, PrioritizedGoal)} )})
     *
     * @param world The world of the entity
     * @param entity The entity
     * @param selector The goal selector to send information for
     * @param ci CallbackInfo from the mixin injection
     */
    @Inject(at = @At("HEAD"),
            method = "sendGoal")
    private static void onSendGoal(World world, MobEntity entity, GoalSelector selector, CallbackInfo ci) {
        if (!(world instanceof ServerWorld) || !RenderersState.INSTANCE.get(ENTITY_AI)) return;

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
     * POI Removed debug packet format<br>
     * |- The position of the removed POI (BlockPos)
     *
     * @param world The world of the event
     * @param pos The position of the POI
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "func_218805_b", // POI Removed
            at = @At("HEAD"))
    private static void onPoiRemoved(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (!RenderersState.INSTANCE.get(POI)) return;
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBlockPos(pos);
        func_229753_a_(world, buf, SCustomPayloadPlayPacket.DEBUG_POI_REMOVED);
    }

    /**
     * POI Added debug packet format<br>
     * |- Position (BlockPos)<br>
     * |- POI Type (String)<br>
     * |- Ammount of tickets (int)<br>
     *
     * @param world The world of the event
     * @param pos The position of the POI
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "func_218799_a",
            at = @At("HEAD"))
    private static void onPoiAdded(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (!RenderersState.INSTANCE.get(POI)) return;
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeBlockPos(pos);
        buf.writeString(world.getBlockState(pos).getBlock().getClass().getSimpleName());
        buf.writeInt(0);

        func_229753_a_(world, buf, SCustomPayloadPlayPacket.DEBUG_POI_ADDED);
    }

    /**
     * Hive debug info packet format<br>
     * |- Position of the hive (BlockPos)<br>
     * |- Hive type (String)<br>
     * |- Bee count (int)<br>
     * |- Honey level (int)<br>
     * |- Is sedated (bool)
     *
     * @param te The BeehiveTileEntity
     * @param ci The CallbackInfo from the mixin injection
     */
    @Inject(method = "sendBeehiveDebugData",
            at = @At("HEAD"))
    private static void onSendBeehiveDebugData(BeehiveTileEntity te, CallbackInfo ci) {
        if (!RenderersState.INSTANCE.get(BEEHIVE)) return;
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeBlockPos(te.getPos());
        buf.writeString(te.getClass().getSimpleName());
        buf.writeInt(te.getBeeCount());
        buf.writeInt(te.getBlockState().get(BlockStateProperties.HONEY_LEVEL));
        buf.writeBoolean(te.isSmoked());

        func_229753_a_((ServerWorld) te.getWorld(), buf, SCustomPayloadPlayPacket.field_229728_n_);
    }

    /**
     * Bee debug packet format<br>
     * |- The bee data (see {@link PacketUtils#writeBeeToBuf(PacketBuffer, BeeEntity)})
     *
     * @param bee The bee to send
     * @param ci CallbackInfo from the mixin injection
     */
    @Inject(method = "func_229749_a_",
            at = @At("HEAD"))
    private static void onSendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        if (!RenderersState.INSTANCE.get(BEE)) return;
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        PacketUtils.writeBeeToBuf(buf, bee);

        func_229753_a_((ServerWorld) bee.getEntityWorld(), buf, SCustomPayloadPlayPacket.field_229727_m_);
    }

    /**
     * Raid debug packet format
     * |- Amount of raids (int)
     * |- A list of locations of raids (blockpos)
     *
     * @param world The world
     * @param raids THe collection of raid
     * @param ci some info about the callback, what did you think this was lol.
     */
    @Inject(method = "sendRaids",
            at = @At("HEAD"))
    private static void onSendRaids(ServerWorld world, Collection<Raid> raids, CallbackInfo ci) {
        if (!RenderersState.INSTANCE.get(RAID)) return;
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeInt(raids.size());
        for (Raid raid : raids)
            buf.writeBlockPos(raid.getCenter());

        func_229753_a_(world, buf, SCustomPayloadPlayPacket.DEBUG_RAIDS);
    }

    /**
     * Structure debug packet format<br>
     * |- The dimension ID (int)<br>
     * |- Bounding box of the whole structure ({@link PacketUtils#writeBBToBuf(MutableBoundingBox, PacketBuffer)})<br>
     * |- Length of the list of sub-components (int)<br>
     * |- A list of the bounding boxes of the sub-components ({@link PacketUtils#writeBBToBuf(MutableBoundingBox, PacketBuffer)})<br>
     *
     * @param world The world (a WorldGenRegion)
     * @param struct The structure to send.
     * @param ci CallbackInfo from the mixin injector.
     */
    @Inject(method = "sendStructureStart",
            at = @At("HEAD"))
    private static void onSendStructureStart(IWorld world, StructureStart struct, CallbackInfo ci) {
        if (!(world instanceof WorldGenRegion) || !RenderersState.INSTANCE.get(STRUCTURE)) return;
        ServerWorld sWorld = ((WorldGenRegion) world).getWorld();
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeInt(sWorld.getWorld().dimension.getType().getId());
        PacketUtils.writeBBToBuf(struct.getBoundingBox(), buf);
        List<StructurePiece> parts = struct.getComponents();
        buf.writeInt(parts.size());
        for (StructurePiece part : parts) {
            PacketUtils.writeBBToBuf(part.getBoundingBox(), buf);
            buf.writeBoolean(false);
        }

        func_229753_a_(sWorld, buf, SCustomPayloadPlayPacket.DEBUG_STRUCTURES);
    }

}
