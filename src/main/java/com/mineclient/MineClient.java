package com.mineclient;

import com.mineclient.command.ClientCommand;
import com.mineclient.config.ConfigManager;
import com.mineclient.input.KeybindHandler;
import com.mineclient.module.ModuleManager;
import com.mineclient.network.PacketInterceptor;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = MineClient.MODID,
        name = MineClient.NAME,
        version = MineClient.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class MineClient {

    public static final String MODID = "mineclient";
    public static final String NAME = "MineClient";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static MineClient instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} {} carregando...", NAME, VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModuleManager.init();
        ConfigManager.load();

        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new PacketInterceptor());
        ClientCommandHandler.instance.registerCommand(new ClientCommand());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ConfigManager.save();
            }
        }, "MineClient-ConfigSave"));

        LOGGER.info("{} inicializado. GUI: Right Shift | Comando: /mineclient", NAME);
    }
}
