package me.geek.tom.debugrenderers.utils;

import me.geek.tom.debugrenderers.commands.DebugRenderersCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static me.geek.tom.debugrenderers.DebugRenderers.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class RenderersState {

    public static RenderersState INSTANCE;

    private static Map<RendererType, Boolean> settings = new HashMap<>();

    public void disableAll() {
        for (RendererType type : RendererType.values()) {
            settings.put(type, false);
        }
    }

    @SubscribeEvent
    public static void onServerStart(FMLServerStartingEvent event) {
        INSTANCE = new RenderersState();
        DebugRenderersCommand.register(event.getCommandDispatcher());
        INSTANCE.disableAll();
    }

    public boolean get(RendererType type) {
        return settings.get(type);
    }

    public void toggle(RendererType type) {
        settings.put(type, !settings.get(type));
    }

    public enum RendererType {
        BEE,
        POI,
        PATHFINDING,
        BEEHIVE,
        ENTITY_AI,
        RAID,
        STRUCTURE
    }
}
