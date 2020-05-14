package me.geek.tom.debugrenderers.utils;

import me.geek.tom.debugrenderers.commands.DebugRenderersCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import static me.geek.tom.debugrenderers.DebugRenderers.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class RenderersState {

    public static RenderersState INSTANCE;

    public boolean PATHFINDING = false;
    public boolean ENTITY_AI = false;
    public boolean POI = false;
    public boolean BEEHIVE = false;
    public boolean BEE = false;

    public void disableAll() {
        PATHFINDING = ENTITY_AI = POI = BEEHIVE = BEE = false;
    }

    public void setPATHFINDING(boolean PATHFINDING) {
        this.PATHFINDING = PATHFINDING;
    }

    public void setENTITY_AI(boolean ENTITY_AI) {
        this.ENTITY_AI = ENTITY_AI;
    }

    public void setPOI(boolean POI) {
        this.POI = POI;
    }

    public void setBEEHIVE(boolean BEEHIVE) {
        this.BEEHIVE = BEEHIVE;
    }

    public void setBEE(boolean BEE) {
        this.BEE = BEE;
    }

    @SubscribeEvent
    public static void onServerStart(FMLServerStartingEvent event) {
        INSTANCE = new RenderersState();
        DebugRenderersCommand.register(event.getCommandDispatcher());
    }

}
