package me.geek.tom.debugrenderers.mixins.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class MixinDebugRenderer {

    @Final @Shadow public PathfindingDebugRenderer pathfinding;
    @Final @Shadow public DebugRenderer.IDebugRenderer water;
    @Final @Shadow public DebugRenderer.IDebugRenderer chunkBorder;
    @Final @Shadow public DebugRenderer.IDebugRenderer heightMap;
    @Final @Shadow public DebugRenderer.IDebugRenderer collisionBox;
    @Final @Shadow public DebugRenderer.IDebugRenderer neighborsUpdate;
    @Final @Shadow public CaveDebugRenderer cave;
    @Final @Shadow public StructureDebugRenderer structure;
    @Final @Shadow public DebugRenderer.IDebugRenderer light;
    @Final @Shadow public DebugRenderer.IDebugRenderer worldGenAttempts;
    @Final @Shadow public DebugRenderer.IDebugRenderer solidFace;
    @Final @Shadow public DebugRenderer.IDebugRenderer field_217740_l; // Chunk info
    @Final @Shadow public PointOfInterestDebugRenderer field_217741_m; // POI Info
    @Final @Shadow public BeeDebugRenderer field_229017_n_; // Bee info
    @Final @Shadow public RaidDebugRenderer field_222927_n; // Raid info
    @Final @Shadow public EntityAIDebugRenderer field_217742_n; // Entity AI
    @Final @Shadow public GameTestDebugRenderer field_229018_q_; // Game test

    @Inject(method = "render",
            at = @At("HEAD"))
    public void onRender(MatrixStack stack, IRenderTypeBuffer.Impl buf, double camX, double camY, double camZ, CallbackInfo ci) {
        pathfinding.render(stack, buf, camX, camY, camZ); // Pathfinding info
        field_217742_n.render(stack, buf, camX, camY, camZ); // Entity AI
        field_217741_m.render(stack, buf, camX, camY, camZ); // POI Info
        field_229017_n_.render(stack, buf, camX, camY, camZ); // Bee info
        //field_222927_n.render(stack, buf, camX, camY, camZ); // Raid info
    }
}
