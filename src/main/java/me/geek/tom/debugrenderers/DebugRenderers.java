package me.geek.tom.debugrenderers;

import me.geek.tom.debugrenderers.commands.arguments.RendererTypeArgument;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DebugRenderers.MODID)
public class DebugRenderers {

    public static final String MODID = "drenders";

    public DebugRenderers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        //SharedConstants.developmentMode = true;
    }

    private void init(FMLCommonSetupEvent event) {
        StartupMessageManager.addModMessage("drenders::init");
        ArgumentTypes.register(MODID+":rtype", RendererTypeArgument.class, new ArgumentSerializer<>(RendererTypeArgument::rendererType));
    }
}
