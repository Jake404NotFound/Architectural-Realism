package com.jake404notfound.architecturalrealism.test;

import com.jake404notfound.architecturalrealism.physics.BlockProperties;
import com.jake404notfound.architecturalrealism.physics.BlockPropertyManager;
import com.jake404notfound.architecturalrealism.physics.StructuralIntegrityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for the structural integrity mechanics.
 * These tests verify that the physics calculations work correctly.
 */
@GameTestHolder("architecturalrealism")
@PrefixGameTestTemplate(false)
public class StructuralIntegrityTests {

    /**
     * Tests that foundation blocks are correctly identified.
     */
    @GameTest(template = "empty")
    public void testFoundationIdentification(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place some blocks
        BlockPos groundPos = new BlockPos(1, 0, 1);
        BlockPos aboveGroundPos = groundPos.above();
        BlockPos floatingPos = groundPos.above(3);
        
        helper.setBlock(groundPos, Blocks.STONE);
        helper.setBlock(aboveGroundPos, Blocks.STONE);
        helper.setBlock(floatingPos, Blocks.STONE);
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private method
            Method identifyFoundationsMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "identifyFoundations", Level.class, BlockPos.class, int.class);
            identifyFoundationsMethod.setAccessible(true);
            
            // Call the method
            Set<BlockPos> foundations = (Set<BlockPos>) identifyFoundationsMethod.invoke(
                manager, level, groundPos, 5);
            
            // Verify results
            helper.assertTrue(foundations.contains(groundPos), "Ground block should be identified as foundation");
            helper.assertTrue(foundations.contains(aboveGroundPos), "Block directly above ground should be identified as foundation");
            helper.assertFalse(foundations.contains(floatingPos), "Floating block should not be identified as foundation");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that support is correctly calculated and propagated.
     */
    @GameTest(template = "empty")
    public void testSupportPropagation(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place a column of blocks
        BlockPos basePos = new BlockPos(1, 0, 1);
        helper.setBlock(basePos, Blocks.STONE);
        helper.setBlock(basePos.above(1), Blocks.STONE);
        helper.setBlock(basePos.above(2), Blocks.STONE);
        helper.setBlock(basePos.above(3), Blocks.STONE);
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private methods
            Method identifyFoundationsMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "identifyFoundations", Level.class, BlockPos.class, int.class);
            identifyFoundationsMethod.setAccessible(true);
            
            Method calculateSupportMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "calculateSupport", Level.class, BlockPos.class, int.class, Set.class);
            calculateSupportMethod.setAccessible(true);
            
            // Identify foundations
            Set<BlockPos> foundations = (Set<BlockPos>) identifyFoundationsMethod.invoke(
                manager, level, basePos, 5);
            
            // Calculate support
            Map<BlockPos, Double> supportMap = (Map<BlockPos, Double>) calculateSupportMethod.invoke(
                manager, level, basePos, 5, foundations);
            
            // Verify results
            helper.assertTrue(supportMap.get(basePos) > 0, "Base block should have support");
            helper.assertTrue(supportMap.get(basePos.above(1)) > 0, "Block above base should have support");
            helper.assertTrue(supportMap.get(basePos.above(2)) > 0, "Block 2 above base should have support");
            helper.assertTrue(supportMap.get(basePos.above(3)) > 0, "Block 3 above base should have support");
            
            // Support should decrease as we go up
            helper.assertTrue(supportMap.get(basePos) >= supportMap.get(basePos.above(1)),
                "Support should decrease as height increases");
            helper.assertTrue(supportMap.get(basePos.above(1)) >= supportMap.get(basePos.above(2)),
                "Support should decrease as height increases");
            helper.assertTrue(supportMap.get(basePos.above(2)) >= supportMap.get(basePos.above(3)),
                "Support should decrease as height increases");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that diagonal support is correctly calculated.
     */
    @GameTest(template = "empty")
    public void testDiagonalSupport(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place blocks in an L shape
        BlockPos basePos = new BlockPos(1, 0, 1);
        helper.setBlock(basePos, Blocks.STONE);
        helper.setBlock(basePos.above(1), Blocks.STONE);
        helper.setBlock(basePos.above(1).north(1), Blocks.STONE); // Diagonal connection
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private methods
            Method identifyFoundationsMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "identifyFoundations", Level.class, BlockPos.class, int.class);
            identifyFoundationsMethod.setAccessible(true);
            
            Method calculateSupportMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "calculateSupport", Level.class, BlockPos.class, int.class, Set.class);
            calculateSupportMethod.setAccessible(true);
            
            // Identify foundations
            Set<BlockPos> foundations = (Set<BlockPos>) identifyFoundationsMethod.invoke(
                manager, level, basePos, 5);
            
            // Calculate support
            Map<BlockPos, Double> supportMap = (Map<BlockPos, Double>) calculateSupportMethod.invoke(
                manager, level, basePos, 5, foundations);
            
            // Verify results
            helper.assertTrue(supportMap.get(basePos.above(1).north(1)) > 0,
                "Diagonally connected block should have support");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that hanging support is correctly calculated.
     */
    @GameTest(template = "empty")
    public void testHangingSupport(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place blocks with a hanging structure
        BlockPos ceilingPos = new BlockPos(1, 5, 1);
        helper.setBlock(ceilingPos, Blocks.STONE);
        helper.setBlock(ceilingPos.below(1), Blocks.STONE); // Hanging block
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private methods
            Method calculateSupportTransferMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "calculateSupportTransfer", Level.class, BlockPos.class, BlockPos.class, double.class, 
                StructuralIntegrityManager.Direction.class);
            calculateSupportTransferMethod.setAccessible(true);
            
            // Calculate support transfer from ceiling to hanging block
            double supportTransfer = (double) calculateSupportTransferMethod.invoke(
                manager, level, ceilingPos, ceilingPos.below(1), 100.0, 
                StructuralIntegrityManager.Direction.DOWN);
            
            // Verify results
            helper.assertTrue(supportTransfer > 0, "Hanging block should receive some support");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that unstable blocks are correctly identified.
     */
    @GameTest(template = "empty")
    public void testUnstableBlockIdentification(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place some blocks
        BlockPos basePos = new BlockPos(1, 0, 1);
        helper.setBlock(basePos, Blocks.STONE);
        helper.setBlock(basePos.above(1), Blocks.STONE);
        helper.setBlock(basePos.above(2), Blocks.STONE);
        
        // Place a floating block with no support
        BlockPos floatingPos = new BlockPos(5, 3, 5);
        helper.setBlock(floatingPos, Blocks.STONE);
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private methods
            Method identifyFoundationsMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "identifyFoundations", Level.class, BlockPos.class, int.class);
            identifyFoundationsMethod.setAccessible(true);
            
            Method calculateSupportMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "calculateSupport", Level.class, BlockPos.class, int.class, Set.class);
            calculateSupportMethod.setAccessible(true);
            
            Method findUnstableBlocksMethod = StructuralIntegrityManager.class.getDeclaredMethod(
                "findUnstableBlocks", Level.class, Map.class);
            findUnstableBlocksMethod.setAccessible(true);
            
            // Create a combined test area that includes both structures
            BlockPos centerPos = new BlockPos(3, 2, 3);
            int radius = 5;
            
            // Identify foundations
            Set<BlockPos> foundations = (Set<BlockPos>) identifyFoundationsMethod.invoke(
                manager, level, centerPos, radius);
            
            // Calculate support
            Map<BlockPos, Double> supportMap = (Map<BlockPos, Double>) calculateSupportMethod.invoke(
                manager, level, centerPos, radius, foundations);
            
            // Find unstable blocks
            java.util.List<BlockPos> unstableBlocks = (java.util.List<BlockPos>) findUnstableBlocksMethod.invoke(
                manager, level, supportMap);
            
            // Verify results
            helper.assertTrue(unstableBlocks.contains(floatingPos), 
                "Floating block should be identified as unstable");
            helper.assertFalse(unstableBlocks.contains(basePos), 
                "Foundation block should not be identified as unstable");
            helper.assertFalse(unstableBlocks.contains(basePos.above(1)), 
                "Supported block should not be identified as unstable");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests that support caching works correctly.
     */
    @GameTest(template = "empty")
    public void testSupportCaching(net.neoforged.neoforge.gametest.GameTestHelper helper) {
        // Create a test environment
        Level level = helper.getLevel();
        
        // Place a block
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        
        // Get the StructuralIntegrityManager instance
        StructuralIntegrityManager manager = StructuralIntegrityManager.getInstance();
        
        try {
            // Use reflection to access private methods and fields
            Method cacheSupport = StructuralIntegrityManager.class.getDeclaredMethod(
                "cacheSupport", Level.class, BlockPos.class, double.class);
            cacheSupport.setAccessible(true);
            
            Method getCachedSupport = StructuralIntegrityManager.class.getDeclaredMethod(
                "getCachedSupport", Level.class, BlockPos.class);
            getCachedSupport.setAccessible(true);
            
            Field supportCacheField = StructuralIntegrityManager.class.getDeclaredField("supportCache");
            supportCacheField.setAccessible(true);
            
            // Cache a support value
            double supportValue = 42.0;
            cacheSupport.invoke(manager, level, pos, supportValue);
            
            // Retrieve the cached value
            Double cachedValue = (Double) getCachedSupport.invoke(manager, level, pos);
            
            // Verify results
            helper.assertTrue(cachedValue != null, "Support value should be cached");
            helper.assertTrue(Math.abs(cachedValue - supportValue) < 0.001, 
                "Cached support value should match original value");
            
            // Verify cache structure
            Map<Level, Map<BlockPos, Double>> supportCache = 
                (Map<Level, Map<BlockPos, Double>>) supportCacheField.get(manager);
            helper.assertTrue(supportCache.containsKey(level), "Cache should contain the level");
            helper.assertTrue(supportCache.get(level).containsKey(pos), 
                "Level cache should contain the position");
            
            helper.succeed();
        } catch (Exception e) {
            helper.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
