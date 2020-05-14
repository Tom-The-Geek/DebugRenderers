package me.geek.tom.debugrenderers.mixins.mixins;

import me.geek.tom.debugrenderers.DebugRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkHooks.class)
public class MixinNetworkHooks {

    @Inject(at = @At("HEAD"),
            method = "onCustomPayload",
            cancellable = true,
            remap = false)
    private static void onOnCustomPayload(ICustomPacket<?> packet, NetworkManager manager, CallbackInfoReturnable<Boolean> ci) {
        if (new ResourceLocation(DebugRenderers.MODID, "clear_all").equals(packet.getName())) {
            Minecraft.getInstance().debugRenderer.clear();
            ci.setReturnValue(true);
            ci.cancel();
        }
    }

}
