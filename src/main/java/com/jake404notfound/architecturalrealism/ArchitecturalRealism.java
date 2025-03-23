package com.jake404notfound.architecturalrealism;

import com.jake404notfound.architecturalrealism.config.ARConfig;
import com.jake404notfound.architecturalrealism.physics.StructuralIntegrityManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ArchitecturalRealism.MOD_ID)
public class ArchitecturalRealism {
    public static final String MOD_ID = "architecturalrealism";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    private StructuralIntegrityManager structuralIntegrityManager;

    public ArchitecturalRealism(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Architectural Realism mod");
        
        // Register mod configuration
        modContainer.registerConfig(ModConfig.Type.COMMON, ARConfig.COMMON_SPEC);
        
        // Register setup method
        modEventBus.addListener(this::setup);
        
        // Initialize the structural integrity manager
        structuralIntegrityManager = new StructuralIntegrityManager();
        
        LOGGER.info("Architectural Realism mod initialized");
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Architectural Realism setup phase");
        
        // Initialize the structural integrity system
        event.enqueueWork(() -> {
            structuralIntegrityManager.initialize();
        });
    }
}
