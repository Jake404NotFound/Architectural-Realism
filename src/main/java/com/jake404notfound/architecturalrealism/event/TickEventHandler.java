package com.jake404notfound.architecturalrealism.event;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import com.jake404notfound.architecturalrealism.physics.StructuralIntegrityManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelTickEvent;

/**
 * Handles game tick events for the Architectural Realism mod.
 * This is where we process structural integrity updates on a regular basis.
 */
@Mod.EventBusSubscriber(modid = ArchitecturalRealism.MOD_ID)
public class TickEventHandler {
    
    /**
     * Handles the server tick event to process structural integrity updates.
     * This ensures that our physics calculations are spread out over time.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        // Only process on the end phase to ensure all other tick operations are complete
        if (event.phase == ServerTickEvent.Phase.END) {
            // Process pending structural updates
            StructuralIntegrityManager.getInstance().processPendingUpdates();
        }
    }
    
    /**
     * Handles the level tick event for additional level-specific processing.
     * This could be used for level-specific physics calculations in the future.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event) {
        // Only process on server side and in the end phase
        if (event.level instanceof ServerLevel && event.phase == LevelTickEvent.Phase.END) {
            // Currently no level-specific processing needed
            // This hook is here for future expansion
        }
    }
}
