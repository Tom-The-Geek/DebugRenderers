package me.geek.tom.debugrenderers.utils;

import io.netty.buffer.Unpooled;
import me.geek.tom.debugrenderers.DebugRenderers;
import me.geek.tom.debugrenderers.utils.reflect.ReflectUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.stream.Collectors;

public class PacketUtils {

    /**
     * Path info debug packet structure<br>
     * |- Reaches target flag (bool)<br>
     * |- Current path index (int)<br>
     * |- Length of the list of FlaggedPathPoint (appears that the recieved data is not used)<br>
     * |- A list of the FlaggedPathPoint s<br>
     * |- X,Y,Z of the target (ints)<br>
     * |- Amount of points in path (int)<br>
     * |- The list of all points in the path. (Structure for points in {@link #writePointToBuf(PacketBuffer, PathPoint)})<br>
     * |- Amount of open points in the following array (int)<br>
     * |- The array of open points (Again, see {@link #writePointToBuf(PacketBuffer, PathPoint)} for the structure of points)<br>
     * |- Amount of closed points (int)<br>
     * |- Array of closed points {you know what to do, look at @link #writePointToBuf(PacketBuffer, PathPoint)})<br>
     *
     * @param buf Buffer to write to.
     * @param path The path to write there.
     */
    public static void writePathToBuffer(PacketBuffer buf, Path path) {
        buf.writeBoolean(path.reachesTarget());
        buf.writeInt(path.getCurrentPathIndex());

        // Certain stuff has been stripped, lets hope its not neccessary
        // We just skip this step as it seems that the code that reads this bit
        // back does not do anything with the recieved data.
        buf.writeInt(0);

        BlockPos target = path.getTarget();
        buf.writeInt(target.getX()).writeInt(target.getY()).writeInt(target.getZ());
        List<PathPoint> points = path.func_215746_d();
        buf.writeInt(points.size());
        for (PathPoint point : points)
            writePointToBuf(buf, point);
        PathPoint[] openSet = path.getOpenSet();
        buf.writeInt(openSet.length);
        for (PathPoint point : openSet)
            writePointToBuf(buf, point);
        PathPoint[] closedSet = path.getClosedSet();
        buf.writeInt(closedSet.length);
        for (PathPoint point : closedSet)
            writePointToBuf(buf, point);
    }

    /**
     * Structure of a point in a packet<br>
     * |- X,Y,Z of the point (int)<br>
     * |- Some value (field_222861_j) (float)<br>
     * |- costMalus (float)<br>
     * |- Whether the point has been visited (bool)<br>
     * |- The index of the PathNodeType#values() array for the type of point (int)<br>
     * |- The distance to the target from this point (float)<br>
     *
     * @param buf Buffer to write to
     * @param point The point to write to it
     */
    private static void writePointToBuf(PacketBuffer buf, PathPoint point) {
        buf.writeInt(point.x).writeInt(point.y).writeInt(point.z);
        buf.writeFloat(point.field_222861_j);
        buf.writeFloat(point.costMalus);
        buf.writeBoolean(point.visited);
        int idx = -1;
        PathNodeType[] types = PathNodeType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(point.nodeType)) {
                idx = i;
                break;
            }
        }
        assert idx != -1;
        buf.writeInt(idx);
        buf.writeFloat(point.distanceToTarget);
    }

    /**
     * Goal packet format<br>
     * |- Some int (I assume an index)<br>
     * |- A bool (is running?)<br>
     * |- String (assumed to be the name)<br>
     *
     * @param buf Buffer to write to
     * @param idx Index of the goal (assuming thats what could be put there)
     * @param goal The goal to write
     */
    public static void writeGoalToBuf(PacketBuffer buf, int idx, PrioritizedGoal goal) {
        buf.writeInt(idx);
        buf.writeBoolean(goal.isRunning());
        buf.writeString(goal.getGoal().toString(), 255);
    }

    /**
     * Bee debug packet format<br>
     * |- Position of the bee (double,double,double)<br>
     * |- The UUID of the bee (UUID)<br>
     * |- Entity ID (int)<br>
     * |- Does a bee hive location follow (bool)<br>
     * |- If the previous was true, the position of the bee's hive (BlockPos)<br>
     * |- Does a flower location follow (bool)<br>
     * |- If the previous was true, the position of the flower (BlockPos)<br>
     * |- Travelling ticks (? couldn't find where this value is on the bee entity) (int)<br>
     * |- Does a path follow (bool)<br>
     * |- If the previous was true, the bee's current path ({@link #writePathToBuffer(PacketBuffer, Path)})<br>
     * |- Number of strings in the following array (int)<br>
     * |- A list of the bee's goals? (List\<String\>)<br>
     * |- Length of the list of blacklisted Hives (int)<br>
     * |- List of the bee's blacklisted hives (BlockPos)<br>
     *
     * @param buf The buffer to write to
     * @param bee The bee entity to write
     */
    public static void writeBeeToBuf(PacketBuffer buf, BeeEntity bee) {
        buf.writeDouble(bee.getPosX()).writeDouble(bee.getPosY()).writeDouble(bee.getPosZ());
        buf.writeUniqueId(bee.getUniqueID());
        buf.writeInt(bee.getEntityId());
        boolean hive = bee.hasHive();
        buf.writeBoolean(hive);
        if (hive)
            buf.writeBlockPos(bee.getHivePos());
        boolean flower = bee.hasFlower();
        buf.writeBoolean(flower);
        if (flower)
            buf.writeBlockPos(bee.getFlowerPos());
        buf.writeInt(1); // TODO: Find travel ticks?
        boolean hasPath = bee.hasPath();
        buf.writeBoolean(hasPath);
        if (hasPath)
            writePathToBuffer(buf, bee.getNavigator().getPath());
        List<String> goals = bee.goalSelector.getRunningGoals()
                .map(PrioritizedGoal::getGoal).map(Goal::toString)
                .collect(Collectors.toList());
        buf.writeInt(goals.size());
        for (String goal : goals)
            buf.writeString(goal);
        buf.writeInt(0); // TODO: Find where to get the blacklist from.
    }

    /**
     * Sends a packet instructing the client to reset all debug renderers
     */
    public static void sendReset(ServerWorld world) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        ReflectUtils.DebugPacketSender_func_229753_a_.call(null, world, buf, new ResourceLocation(DebugRenderers.MODID, "clear_all"));
    }

    /**
     * Writes the given bounding box to the buffer.
     *
     * @param bb The bounding box
     * @param buf The buffer to write to
     */
    public static void writeBBToBuf(MutableBoundingBox bb, PacketBuffer buf) {
        buf.writeInt(bb.minX);
        buf.writeInt(bb.minY);
        buf.writeInt(bb.minZ);
        buf.writeInt(bb.maxX);
        buf.writeInt(bb.maxY);
        buf.writeInt(bb.maxZ);
    }
}
