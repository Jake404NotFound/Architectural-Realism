package com.jake404notfound.architecturalrealism.event;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import com.jake404notfound.architecturalrealism.physics.StructuralIntegrityManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Handles game tick events for the Architectural Realism mod.
 * This is where we process structural integrity updates on a regular basis.
 */
@EventBusSubscriber(modid = ArchitecturalRealism.MOD_ID)
public class TickEventHandler {
    
    /**
     * Handles the server tick event to process structural integrity updates.
     * This ensures that our physics calculations are spread out over time.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Process pending structural updates
        StructuralIntegrityManager.getInstance().processPendingUpdates();
    }
    
    /**
     * Handles the level tick event for additional level-specific processing.
     * This could be used for level-specific physics calculations in the future.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel) {
            // Currently no level-specific processing needed
            // This hook is here for future expansion
        }
    }
}
