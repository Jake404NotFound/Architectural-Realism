package com.jake404notfound.architecturalrealism.physics;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import com.jake404notfound.architecturalrealism.config.ARConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = ArchitecturalRealism.MOD_ID)
public class StructuralIntegrityManager {
    
    private final BlockPropertyManager blockPropertyManager;
    private final Queue<StructuralUpdateTask> updateQueue;
    private final Map<Level, Set<BlockPos>> processedBlocks;
    private final Map<Level, Map<BlockPos, Double>> supportCache;
    private int maxCacheSize;
    
    public StructuralIntegrityManager() {
        this.blockPropertyManager = new BlockPropertyManager();
        this.updateQueue = new ConcurrentLinkedQueue<>();
        this.processedBlocks = new HashMap<>();
        this.supportCache = new HashMap<>();
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
            event.getEntity().isCreative()) {
            return;
        }
        
        ArchitecturalRealism.LOGGER.debug("Block placed at {}", event.getPos());
        
        // Schedule structural integrity check for the placed block and surrounding area
        getInstance().scheduleStructuralUpdate(event.getLevel(), event.getPos(), 
            ARConfig.COMMON.calculationRadius.get());
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
        getInstance().scheduleStructuralUpdate(event.getLevel(), event.getPos(), 
            ARConfig.COMMON.calculationRadius.get());
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
                    
                    // Cache the support value
                    cacheSupport(level, neighbor.immutable(), transferredSupport);
                    
                    // Add to queue for further propagation if not already processed
                    if (!processed.contains(neighbor)) {
                        queue.add(neighbor.immutable());
                        processed.add(neighbor.immutable());
                        distanceFromFoundation.put(neighbor, currentDistance + 1);
                    }
                }
            }
            
            // Check diagonal connections if enabled
            if (ARConfig.COMMON.enableDiagonalConnections.get()) {
                checkDiagonalConnections(level, current, currentSupport, currentDistance, 
                                        center, radius, supportMap, queue, processed, 
                                        distanceFromFoundation, maxSupportDistance);
            }
        }
        
        return supportMap;
    }
    
    private void checkDiagonalConnections(Level level, BlockPos current, double currentSupport, 
                                         int currentDistance, BlockPos center, int radius,
                                         Map<BlockPos, Double> supportMap, Queue<BlockPos> queue, 
                                         Set<BlockPos> processed, Map<BlockPos, Integer> distanceFromFoundation,
                                         int maxSupportDistance) {
        // Check diagonal connections (e.g., corner blocks)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    // Skip orthogonal directions (already handled) and center
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= 1) continue;
                    
                    BlockPos diagonal = current.offset(dx, dy, dz);
                    
                    // Skip if outside calculation radius
                    if (diagonal.distSqr(center) > radius * radius) continue;
                    
                    // Skip if air
                    if (level.isEmptyBlock(diagonal)) continue;
                    
                    // Calculate diagonal support transfer with penalty
                    double diagonalFactor = ARConfig.COMMON.diagonalSupportFactor.get();
                    double transferredSupport = calculateDiagonalSupportTransfer(level, current, diagonal, 
                                                                               currentSupport, diagonalFactor);
                    
                    // If this provides more support than the diagonal already has
                    if (transferredSupport > supportMap.getOrDefault(diagonal, 0.0)) {
                        // Update support value
                        supportMap.put(diagonal.immutable(), transferredSupport);
                        
                        // Cache the support value
                        cacheSupport(level, diagonal.immutable(), transferredSupport);
                        
                        // Add to queue for further propagation if not already processed and within distance limit
                        if (!processed.contains(diagonal) && currentDistance + 1 < maxSupportDistance) {
                            queue.add(diagonal.immutable());
                            processed.add(diagonal.immutable());
                            distanceFromFoundation.put(diagonal, currentDistance + 1);
                        }
                    }
                }
            }
        }
    }
    
    private double calculateDiagonalSupportTransfer(Level level, BlockPos source, BlockPos target, 
                                                  double sourceSupport, double diagonalFactor) {
        BlockState sourceState = level.getBlockState(source);
        BlockState targetState = level.getBlockState(target);
        
        // Get block properties
        BlockProperties sourceProps = blockPropertyManager.getBlockProperties(sourceState.getBlock());
        BlockProperties targetProps = blockPropertyManager.getBlockProperties(targetState.getBlock());
        
        // For diagonal connections, use the minimum of tensile and shear strength
        double transferFactor = Math.min(sourceProps.getTensileStrength(), sourceProps.getShearStrength()) / 100.0;
        
        // Apply diagonal penalty
        transferFactor *= diagonalFactor;
        
        // Higher weight penalty for diagonal connections
        double supportLoss = sourceProps.getWeight() * 1.5;
        
        // Calculate transferred support
        double transferredSupport = sourceSupport * transferFactor - supportLoss;
        
        // Ensure support doesn't exceed the maximum the block can transfer
        transferredSupport = Math.min(transferredSupport, sourceProps.getMaxLoad() * diagonalFactor);
        
        // Support can't be negative
        return Math.max(0, transferredSupport);
    }
    
    private double calculateSupportTransfer(Level level, BlockPos source, BlockPos target, double sourceSupport, Direction direction) {
        BlockState sourceState = level.getBlockState(source);
        BlockState targetState = level.getBlockState(target);
        
        // Get block properties
        BlockProperties sourceProps = blockPropertyManager.getBlockProperties(sourceState.getBlock());
        BlockProperties targetProps = blockPropertyManager.getBlockProperties(targetState.getBlock());
        
        double transferFactor;
        double supportLoss;
        
        // Calculate support transfer based on direction
        if (direction == Direction.UP) {
            // Vertical support upward uses compression strength
            transferFactor = sourceProps.getCompressionStrength() / 100.0;
            supportLoss = sourceProps.getWeight() * 0.5;
        } else if (direction == Direction.DOWN) {
            // Support doesn't propagate downward in the same way
            // But we can allow some minimal support for hanging structures
            if (ARConfig.COMMON.enableHangingSupport.get()) {
                transferFactor = sourceProps.getTensileStrength() / 200.0; // Half of normal tensile strength
                supportLoss = sourceProps.getWeight() * 2.0; // Double weight penalty
                return Math.max(0, sourceSupport * transferFactor - supportLoss);
            }
            return 0;
        } else {
            // Horizontal support uses tensile strength
            transferFactor = sourceProps.getTensileStrength() / 100.0;
            supportLoss = sourceProps.getWeight() * 1.0;
        }
        
        // Calculate transferred support
        double transferredSupport = sourceSupport * transferFactor - supportLoss;
        
        // Ensure support doesn't exceed the maximum the block can transfer
        transferredSupport = Math.min(transferredSupport, sourceProps.getMaxLoad());
        
        // Support can't be negative
        return Math.max(0, transferredSupport);
    }
    
    private List<BlockPos> findUnstableBlocks(Level level, Map<BlockPos, Double> supportMap) {
        List<BlockPos> unstableBlocks = new ArrayList<>();
        
        for (Map.Entry<BlockPos, Double> entry : supportMap.entrySet()) {
            BlockPos pos = entry.getKey();
            double support = entry.getValue();
            
            // Skip air blocks
            if (level.isEmptyBlock(pos)) continue;
            
            BlockState state = level.getBlockState(pos);
            BlockProperties props = blockPropertyManager.getBlockProperties(state.getBlock());
            
            // Calculate required support based on block weight
            double requiredSupport = props.getWeight() * ARConfig.COMMON.supportFactor.get();
            
            // Check if block has sufficient support
            if (support < requiredSupport) {
                unstableBlocks.add(pos.immutable());
            }
        }
        
        // Sort unstable blocks by height (top to bottom) to simulate realistic collapse
        unstableBlocks.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
        
        return unstableBlocks;
    }
    
    private void handleCollapse(Level level, List<BlockPos> unstableBlocks) {
        for (BlockPos pos : unstableBlocks) {
            // Skip if block has been removed already (by another collapse)
            if (level.isEmptyBlock(pos)) continue;
            
            BlockState state = level.getBlockState(pos);
            BlockProperties props = blockPropertyManager.getBlockProperties(state.getBlock());
            
            // Determine if block should break or fall based on fragility
            if (Math.random() < props.getFragility()) {
                // Block breaks
                level.destroyBlock(pos, true);
            } else {
                // Block falls
                Block.dropResources(state, level, pos);
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                
                // Spawn falling block entity
                net.minecraft.world.entity.item.FallingBlockEntity fallingBlock = 
                    new net.minecraft.world.entity.item.FallingBlockEntity(
                        level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
                level.addFreshEntity(fallingBlock);
            }
            
            // Schedule update for surrounding blocks
            scheduleStructuralUpdate(level, pos, 2); // Smaller radius for cascade updates
            
            // Clear cache for this position
            clearCachedSupport(level, pos);
        }
    }
    
    // Support value caching methods
    private Double getCachedSupport(Level level, BlockPos pos) {
        Map<BlockPos, Double> levelCache = supportCache.get(level);
        if (levelCache != null) {
            return levelCache.get(pos);
        }
        return null;
    }
    
    private void cacheSupport(Level level, BlockPos pos, double support) {
        supportCache.computeIfAbsent(level, k -> new HashMap<>());
        Map<BlockPos, Double> levelCache = supportCache.get(level);
        
        // Manage cache size
        if (levelCache.size() >= maxCacheSize) {
            // Remove a random entry if cache is full
            if (!levelCache.isEmpty()) {
                BlockPos keyToRemove = levelCache.keySet().iterator().next();
                levelCache.remove(keyToRemove);
            }
        }
        
        levelCache.put(pos, support);
    }
    
    private void clearCachedSupport(Level level, BlockPos pos) {
        Map<BlockPos, Double> levelCache = supportCache.get(level);
        if (levelCache != null) {
            levelCache.remove(pos);
        }
    }
    
    // Singleton instance for static event handlers
    private static StructuralIntegrityManager instance;
    
    public static StructuralIntegrityManager getInstance() {
        if (instance == null) {
            instance = new StructuralIntegrityManager();
        }
        return instance;
    }
    
    // Helper class for direction
    public enum Direction {
        UP, DOWN, NORTH, SOUTH, EAST, WEST;
        
        public BlockPos relative(BlockPos pos) {
            return switch (this) {
                case UP -> pos.above();
                case DOWN -> pos.below();
                case NORTH -> pos.north();
                case SOUTH -> pos.south();
                case EAST -> pos.east();
                case WEST -> pos.west();
            };
        }
        
        public static Direction[] values() {
            return new Direction[]{UP, DOWN, NORTH, SOUTH, EAST, WEST};
        }
    }
    
    // Helper class for structural update tasks
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
