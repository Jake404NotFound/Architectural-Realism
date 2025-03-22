package com.jake404notfound.architecturalrealism.physics;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import com.jake404notfound.architecturalrealism.config.ARConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = ArchitecturalRealism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StructuralIntegrityManager {
    
    private final BlockPropertyManager blockPropertyManager;
    private final Queue<StructuralUpdateTask> updateQueue;
    private final Map<Level, Set<BlockPos>> processedBlocks;
    private final Map<Level, Map<BlockPos, Double>> supportCache;
    private int maxCacheSize;
    
    // Singleton instance
    private static StructuralIntegrityManager instance;
    
    public StructuralIntegrityManager() {
        this.blockPropertyManager = new BlockPropertyManager();
        this.updateQueue = new ConcurrentLinkedQueue<>();
        this.processedBlocks = new HashMap<>();
        this.supportCache = new HashMap<>();
        instance = this;
    }
    
    public static StructuralIntegrityManager getInstance() {
        return instance;
    }
    
    public void initialize() {
        ArchitecturalRealism.LOGGER.info("Initializing Structural Integrity Manager");
        blockPropertyManager.loadBlockProperties();
        maxCacheSize = ARConfig.COMMON.supportCacheSize.get();
    }
    
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!ARConfig.COMMON.enableStructuralIntegrity.get()) return;
        
        // Skip in creative mode if configured to bypass
        if (ARConfig.COMMON.enableCreativeBypass.get() && event.getEntity() != null && 
            event.getEntity() instanceof Player player && player.isCreative()) {
            return;
        }
        
        ArchitecturalRealism.LOGGER.debug("Block placed at {}", event.getPos());
        
        // Schedule structural integrity check for the placed block and surrounding area
        if (event.getLevel() instanceof Level level) {
            getInstance().scheduleStructuralUpdate(level, event.getPos(), 
                ARConfig.COMMON.calculationRadius.get());
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ARConfig.COMMON.enableStructuralIntegrity.get()) return;
        
        // Skip in creative mode if configured to bypass
        if (ARConfig.COMMON.enableCreativeBypass.get() && event.getPlayer() != null && 
            event.getPlayer().isCreative()) {
            return;
        }
        
        ArchitecturalRealism.LOGGER.debug("Block broken at {}", event.getPos());
        
        // Schedule structural integrity check for the area around the broken block
        if (event.getLevel() instanceof Level level) {
            getInstance().scheduleStructuralUpdate(level, event.getPos(), 
                ARConfig.COMMON.calculationRadius.get());
        }
    }
    
    private void scheduleStructuralUpdate(Level level, BlockPos pos, int radius) {
        updateQueue.add(new StructuralUpdateTask(level, pos, radius));
        
        // Clear support cache for this level when a block changes
        if (supportCache.containsKey(level)) {
            supportCache.get(level).clear();
        }
    }
    
    // This would be called every tick to process the update queue
    public void processPendingUpdates() {
        int calculationsThisTick = 0;
        int maxCalculations = ARConfig.COMMON.maxCalculationsPerTick.get();
        
        while (!updateQueue.isEmpty() && calculationsThisTick < maxCalculations) {
            StructuralUpdateTask task = updateQueue.poll();
            if (task != null) {
                processStructuralUpdate(task);
                calculationsThisTick++;
            }
        }
    }
    
    private void processStructuralUpdate(StructuralUpdateTask task) {
        Level level = task.level;
        BlockPos center = task.position;
        int radius = task.radius;
        
        // Clear processed blocks for this level if not already initialized
        processedBlocks.computeIfAbsent(level, k -> new HashSet<>());
        
        // Identify foundation blocks in the area
        Set<BlockPos> foundations = identifyFoundations(level, center, radius);
        
        // Calculate support values
        Map<BlockPos, Double> supportMap = calculateSupport(level, center, radius, foundations);
        
        // Check for unstable blocks
        List<BlockPos> unstableBlocks = findUnstableBlocks(level, supportMap);
        
        // Handle collapse of unstable blocks
        if (!unstableBlocks.isEmpty()) {
            handleCollapse(level, unstableBlocks);
        }
    }
    
    private Set<BlockPos> identifyFoundations(Level level, BlockPos center, int radius) {
        Set<BlockPos> foundations = new HashSet<>();
        
        // Scan the area around the center position
        BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        ).forEach(pos -> {
            // Skip air blocks
            if (level.isEmptyBlock(pos)) return;
            
            // Bedrock is always a foundation
            BlockState state = level.getBlockState(pos);
            if (state.getBlock().defaultDestroyTime() < 0) {
                foundations.add(pos.immutable());
                return;
            }
            
            // Ground level blocks are foundations
            if (pos.getY() == level.getMinBuildHeight()) {
                foundations.add(pos.immutable());
                return;
            }
            
            // Check if block has solid ground beneath it
            if (hasGroundSupport(level, pos)) {
                foundations.add(pos.immutable());
            }
        });
        
        return foundations;
    }
    
    private boolean hasGroundSupport(Level level, BlockPos pos) {
        // Check if there are solid blocks beneath this position down to bedrock or for a significant depth
        int depth = 0;
        int maxDepth = ARConfig.COMMON.foundationDepth.get(); // Configurable depth for ground support
        
        BlockPos checkPos = pos.below();
        while (depth < maxDepth && checkPos.getY() >= level.getMinBuildHeight()) {
            if (level.isEmptyBlock(checkPos)) {
                // Found a gap, not supported by ground
                return false;
            }
            
            BlockState state = level.getBlockState(checkPos);
            if (state.getBlock().defaultDestroyTime() < 0) {
                // Found bedrock, definitely supported
                return true;
            }
            
            depth++;
            checkPos = checkPos.below();
        }
        
        // If we checked the maximum depth without finding a gap, consider it ground-supported
        return depth >= maxDepth;
    }
    
    private Map<BlockPos, Double> calculateSupport(Level level, BlockPos center, int radius, Set<BlockPos> foundations) {
        Map<BlockPos, Double> supportMap = new HashMap<>();
        
        // Initialize support values
        BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (level.isEmptyBlock(pos)) {
                supportMap.put(pos.immutable(), 0.0);
            } else if (foundations.contains(pos)) {
                supportMap.put(pos.immutable(), 100.0); // Maximum support for foundations
            } else {
                // Check if we have a cached support value
                Double cachedSupport = getCachedSupport(level, pos);
                if (cachedSupport != null) {
                    supportMap.put(pos.immutable(), cachedSupport);
                } else {
                    supportMap.put(pos.immutable(), 0.0);
                }
            }
        });
        
        // Propagate support using a queue-based approach
        Queue<BlockPos> queue = new LinkedList<>(foundations);
        Set<BlockPos> processed = new HashSet<>(foundations);
        
        int maxSupportDistance = ARConfig.COMMON.maxSupportDistance.get();
        Map<BlockPos, Integer> distanceFromFoundation = new HashMap<>();
        
        // Initialize distances for foundations
        for (BlockPos foundation : foundations) {
            distanceFromFoundation.put(foundation, 0);
        }
        
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            double currentSupport = supportMap.get(current);
            int currentDistance = distanceFromFoundation.getOrDefault(current, 0);
            
            // Skip if no support to propagate or we've reached max distance
            if (currentSupport <= 0 || currentDistance >= maxSupportDistance) continue;
            
            // Check all six adjacent blocks
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                
                // Skip if outside calculation radius
                if (neighbor.distSqr(center) > radius * radius) continue;
                
                // Skip if air
                if (level.isEmptyBlock(neighbor)) continue;
                
                // Calculate support transfer
                double transferredSupport = calculateSupportTransfer(level, current, neighbor, currentSupport, direction);
                
                // If this provides more support than the neighbor already has
                if (transferredSupport > supportMap.getOrDefault(neighbor, 0.0)) {
                    // Update support value
                    supportMap.put(neighbor.immutable(), transferredSupport);
                    
                    // Add to queue for further propagation if not already processed
                    if (!processed.contains(neighbor)) {
                        queue.add(neighbor.immutable());
                        processed.add(neighbor.immutable());
                        distanceFromFoundation.put(neighbor, currentDistance + 1);
                    }
                }
            }
        }
        
        // Cache support values for future use
        cacheSupport(level, supportMap);
        
        return supportMap;
    }
    
    private Double getCachedSupport(Level level, BlockPos pos) {
        if (supportCache.containsKey(level)) {
            return supportCache.get(level).get(pos);
        }
        return null;
    }
    
    private void cacheSupport(Level level, Map<BlockPos, Double> supportMap) {
        // Initialize cache for this level if needed
        supportCache.computeIfAbsent(level, k -> new HashMap<>());
        Map<BlockPos, Double> levelCache = supportCache.get(level);
        
        // Add new values to cache
        for (Map.Entry<BlockPos, Double> entry : supportMap.entrySet()) {
            levelCache.put(entry.getKey(), entry.getValue());
        }
        
        // Trim cache if it gets too large
        if (levelCache.size() > maxCacheSize) {
            // Simple approach: just clear the cache when it gets too big
            // A more sophisticated approach would be to use a LRU cache
            levelCache.clear();
        }
    }
    
    private double calculateSupportTransfer(Level level, BlockPos source, BlockPos target, double sourceSupport, Direction direction) {
        // Get block properties
        BlockState sourceState = level.getBlockState(source);
        BlockState targetState = level.getBlockState(target);
        
        // Get support factors for the blocks
        double sourceFactor = blockPropertyManager.getSupportFactor(sourceState.getBlock());
        double targetFactor = blockPropertyManager.getSupportFactor(targetState.getBlock());
        
        // Base support transfer is the minimum of the two factors
        double transferFactor = Math.min(sourceFactor, targetFactor);
        
        // Apply direction-specific modifiers
        if (direction == Direction.UP) {
            // Support from below is strongest
            transferFactor *= ARConfig.COMMON.verticalSupportFactor.get();
        } else if (direction == Direction.DOWN) {
            // Support from above (hanging) is weaker
            if (ARConfig.COMMON.enableHangingSupport.get()) {
                transferFactor *= ARConfig.COMMON.hangingSupportFactor.get();
            } else {
                return 0.0; // No hanging support if disabled
            }
        } else {
            // Horizontal support
            transferFactor *= ARConfig.COMMON.horizontalSupportFactor.get();
        }
        
        // Calculate final support value
        double supportTransfer = sourceSupport * transferFactor;
        
        // Apply distance decay
        supportTransfer *= ARConfig.COMMON.supportDecayFactor.get();
        
        return supportTransfer;
    }
    
    private List<BlockPos> findUnstableBlocks(Level level, Map<BlockPos, Double> supportMap) {
        List<BlockPos> unstableBlocks = new ArrayList<>();
        double stabilityThreshold = ARConfig.COMMON.stabilityThreshold.get();
        
        for (Map.Entry<BlockPos, Double> entry : supportMap.entrySet()) {
            BlockPos pos = entry.getKey();
            double support = entry.getValue();
            
            // Skip air blocks
            if (level.isEmptyBlock(pos)) continue;
            
            // Check if block has enough support
            if (support < stabilityThreshold) {
                // Block is unstable
                unstableBlocks.add(pos);
            }
        }
        
        return unstableBlocks;
    }
    
    private void handleCollapse(Level level, List<BlockPos> unstableBlocks) {
        // Sort blocks by height (top to bottom) to simulate natural collapse
        unstableBlocks.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
        
        for (BlockPos pos : unstableBlocks) {
            // Skip if block has been processed already (might have been destroyed by another falling block)
            if (level.isEmptyBlock(pos)) continue;
            
            BlockState blockState = level.getBlockState(pos);
            
            // Create falling block entity
            if (ARConfig.COMMON.enableFallingBlocks.get()) {
                try {
                    // Use the appropriate constructor for FallingBlockEntity in 1.21.1
                    FallingBlockEntity fallingBlock = FallingBlockEntity.fall(
                        level, 
                        pos, 
                        blockState
                    );
                    
                    // Remove the original block
                    level.removeBlock(pos, false);
                    
                    // Add the falling block entity to the level
                    level.addFreshEntity(fallingBlock);
                    
                    ArchitecturalRealism.LOGGER.debug("Block at {} collapsed and is now falling", pos);
                } catch (Exception e) {
                    ArchitecturalRealism.LOGGER.error("Error creating falling block at {}: {}", pos, e.getMessage());
                    // Fallback: just destroy the block
                    level.destroyBlock(pos, true);
                }
            } else {
                // Just destroy the block if falling blocks are disabled
                level.destroyBlock(pos, true);
                ArchitecturalRealism.LOGGER.debug("Unstable block at {} was destroyed", pos);
            }
        }
    }
    
    // Helper class to store structural update tasks
    private static class StructuralUpdateTask {
        final Level level;
        final BlockPos position;
        final int radius;
        
        StructuralUpdateTask(Level level, BlockPos position, int radius) {
            this.level = level;
            this.position = position;
            this.radius = radius;
        }
    }
}
