# Architectural Realism: Support Calculation Algorithm

This document outlines the algorithm for calculating structural support in the Architectural Realism mod.

## Algorithm Overview

The support calculation algorithm determines whether blocks have sufficient support to remain in place or if they should fall due to insufficient structural integrity.

### Key Principles

1. Support propagates from foundation blocks (bedrock, ground level)
2. Support decreases as it travels through blocks based on their properties
3. Different materials transmit support differently in different directions
4. Blocks require a minimum support value based on their weight to remain stable

## Support Calculation Process

### 1. Foundation Identification

```
function identifyFoundations(world):
    foundations = new Set()
    
    // Bedrock is always a foundation
    for each block in world:
        if block.type == BEDROCK:
            foundations.add(block)
    
    // Ground level blocks are foundations
    for each block at y=0:
        foundations.add(block)
    
    // Blocks directly above solid ground with sufficient depth
    for each block in world:
        if hasGroundSupport(block, 3): // Check 3 blocks deep
            foundations.add(block)
            
    return foundations
```

### 2. Initial Support Assignment

```
function assignInitialSupport(world, foundations):
    supportMap = new Map()
    
    // Assign maximum support to foundation blocks
    for each block in foundations:
        supportMap[block] = MAX_SUPPORT_VALUE
        
    // Initialize all other blocks with zero support
    for each block in world not in foundations:
        supportMap[block] = 0
        
    return supportMap
```

### 3. Support Propagation

```
function propagateSupport(world, supportMap, foundations):
    // Queue for processing blocks
    queue = new Queue()
    
    // Start with foundation blocks
    for each block in foundations:
        queue.enqueue(block)
    
    // Process queue until empty
    while not queue.isEmpty():
        currentBlock = queue.dequeue()
        currentSupport = supportMap[currentBlock]
        
        // Get neighboring blocks
        neighbors = getNeighbors(currentBlock)
        
        for each neighbor in neighbors:
            // Calculate support transfer based on direction and block properties
            transferredSupport = calculateSupportTransfer(currentBlock, neighbor, currentSupport)
            
            // If this provides more support than the neighbor already has
            if transferredSupport > supportMap[neighbor]:
                // Update support value
                supportMap[neighbor] = transferredSupport
                
                // Add to queue for further propagation
                queue.enqueue(neighbor)
                
    return supportMap
```

### 4. Support Transfer Calculation

```
function calculateSupportTransfer(sourceBlock, targetBlock, sourceSupport):
    // Get direction from source to target
    direction = getDirection(sourceBlock, targetBlock)
    
    // Calculate support transfer based on direction
    if direction == VERTICAL_UP:
        // Vertical support uses compression strength
        transferFactor = sourceBlock.compressionStrength / 100.0
        supportLoss = sourceBlock.weight * 0.5
    else if direction == HORIZONTAL:
        // Horizontal support uses tensile strength
        transferFactor = sourceBlock.tensileStrength / 100.0
        supportLoss = sourceBlock.weight * 1.0
    else if direction == DIAGONAL:
        // Diagonal support uses shear strength and tensile strength
        transferFactor = (sourceBlock.shearStrength + sourceBlock.tensileStrength) / 200.0
        supportLoss = sourceBlock.weight * 1.5
    else: // VERTICAL_DOWN
        // Support doesn't propagate downward in the same way
        return 0
    
    // Calculate transferred support
    transferredSupport = sourceSupport * transferFactor - supportLoss
    
    // Ensure support doesn't exceed the maximum the block can transfer
    transferredSupport = min(transferredSupport, sourceBlock.maxLoad)
    
    // Support can't be negative
    return max(0, transferredSupport)
```

### 5. Stability Check

```
function checkStability(world, supportMap):
    unstableBlocks = new Set()
    
    for each block in world:
        // Skip foundation blocks
        if block in foundations:
            continue
            
        // Get required support based on block weight
        requiredSupport = block.weight * SUPPORT_FACTOR
        
        // Check if block has sufficient support
        if supportMap[block] < requiredSupport:
            unstableBlocks.add(block)
            
    return unstableBlocks
```

### 6. Collapse Handling

```
function handleCollapse(world, unstableBlocks):
    // Sort unstable blocks by height (top to bottom)
    sortedUnstableBlocks = sortByHeightDescending(unstableBlocks)
    
    for each block in sortedUnstableBlocks:
        // Check if block is still in world (might have been removed in cascade)
        if block in world:
            // Determine if block should break or fall based on fragility
            if random() < block.fragility:
                // Block breaks
                breakBlock(block)
            else:
                // Block falls
                convertToFallingBlock(block)
                
            // Remove block from world
            world.removeBlock(block)
            
            // Recalculate support for affected blocks
            recalculateLocalSupport(world, block.position)
```

## Optimization Strategies

### 1. Localized Recalculation

Instead of recalculating the entire world when a block changes, only recalculate the affected area:

```
function recalculateLocalSupport(world, position, radius=8):
    // Get blocks within radius of the position
    affectedArea = getBlocksInRadius(world, position, radius)
    
    // Identify local foundations
    localFoundations = identifyLocalFoundations(world, affectedArea)
    
    // Recalculate support for the affected area
    localSupportMap = assignInitialSupport(affectedArea, localFoundations)
    propagateSupport(affectedArea, localSupportMap, localFoundations)
    
    // Check stability of affected blocks
    unstableBlocks = checkStability(affectedArea, localSupportMap)
    
    // Handle collapse of unstable blocks
    handleCollapse(world, unstableBlocks)
```

### 2. Support Caching

Cache support values and only update when necessary:

```
function updateSupportCache(world, changedPositions):
    // Check if changes affect cached values
    if affectsCache(changedPositions, supportCache):
        // Determine affected regions
        affectedRegions = getAffectedRegions(changedPositions)
        
        // Update cache for affected regions
        for each region in affectedRegions:
            recalculateAndCacheSupport(region)
```

### 3. Calculation Throttling

Limit the number of calculations per tick to prevent lag:

```
function throttledSupportUpdate(world, changedPositions):
    // Add changes to queue
    for each position in changedPositions:
        updateQueue.add(position)
    
    // Process a limited number of updates per tick
    processedCount = 0
    while not updateQueue.isEmpty() and processedCount < MAX_UPDATES_PER_TICK:
        position = updateQueue.poll()
        recalculateLocalSupport(world, position)
        processedCount++
```

## Implementation Considerations

1. **Event Handling**: Use NeoForge events to detect block changes and trigger recalculations
2. **Multi-threading**: Perform support calculations in a separate thread to minimize impact on game performance
3. **Chunk Loading**: Handle chunk loading/unloading gracefully to prevent calculation errors at chunk boundaries
4. **Configuration**: Allow adjustment of calculation parameters through configuration files
5. **Visualization**: Provide debug tools to visualize support values and propagation paths

## Next Steps

1. Implement the core support calculation algorithm
2. Create test scenarios to validate the algorithm
3. Optimize performance for large-scale structures
4. Integrate with the block property system
5. Implement visual feedback for players
