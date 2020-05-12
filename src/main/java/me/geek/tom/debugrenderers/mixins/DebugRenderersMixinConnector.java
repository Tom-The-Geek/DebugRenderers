package me.geek.tom.debugrenderers.mixins;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class DebugRenderersMixinConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("drenders.mixins.json");
    }
}
