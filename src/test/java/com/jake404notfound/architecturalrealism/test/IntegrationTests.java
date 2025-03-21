package com.jake404notfound.architecturalrealism.test;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Integration tests for the Architectural Realism mod.
 * These tests validate real-world scenarios for the structural integrity system.
 */
@GameTestHolder(ArchitecturalRealism.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntegrationTests {

    /**
     * Tests a simple column structure with proper support.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void testStableColumn(GameTestHelper helper) {
        // Build a simple column from the ground up
        for (int y = 0; y < 5; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
        }
        
        // Wait a few ticks to ensure stability is calculated
        helper.runAfterDelay(20, () -> {
            // Verify all blocks are still there
            for (int y = 0; y < 5; y++) {
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, y, 0));
            }
            helper.succeed();
        });
    }
    
    /**
     * Tests a simple bridge structure with proper support.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void testStableBridge(GameTestHelper helper) {
        // Build two columns
        for (int y = 0; y < 3; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
            helper.setBlock(4, y, 0, Blocks.STONE);
        }
        
        // Build a bridge between them
        for (int x = 1; x < 4; x++) {
            helper.setBlock(x, 2, 0, Blocks.STONE);
        }
        
        // Wait a few ticks to ensure stability is calculated
        helper.runAfterDelay(20, () -> {
            // Verify all blocks are still there
            for (int y = 0; y < 3; y++) {
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, y, 0));
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(4, y, 0));
            }
            for (int x = 1; x < 4; x++) {
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(x, 2, 0));
            }
            helper.succeed();
        });
    }
    
    /**
     * Tests an unstable structure that should collapse.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void testUnstableStructure(GameTestHelper helper) {
        // Build a column
        for (int y = 0; y < 3; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
        }
        
        // Build an unsupported horizontal extension
        for (int x = 1; x < 5; x++) {
            helper.setBlock(x, 2, 0, Blocks.STONE);
        }
        
        // Wait a few ticks to ensure stability is calculated
        helper.runAfterDelay(20, () -> {
            // Verify column is still there
            for (int y = 0; y < 3; y++) {
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, y, 0));
            }
            
            // Verify the furthest blocks have collapsed
            // Note: The exact behavior will depend on your implementation
            // This test assumes blocks beyond a certain distance will collapse
            helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(4, 2, 0));
            
            helper.succeed();
        });
    }
    
    /**
     * Tests removing a support block and observing the collapse.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void testSupportRemoval(GameTestHelper helper) {
        // Build a column
        for (int y = 0; y < 5; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
        }
        
        // Wait to ensure stability is calculated
        helper.runAfterDelay(10, () -> {
            // Remove a middle block
            helper.setBlock(0, 2, 0, Blocks.AIR);
            
            // Wait to observe collapse
            helper.runAfterDelay(10, () -> {
                // Verify bottom blocks are still there
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 0, 0));
                helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 1, 0));
                
                // Verify top blocks have collapsed
                helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(0, 3, 0));
                helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(0, 4, 0));
                
                helper.succeed();
            });
        });
    }
    
    /**
     * Tests different materials with different structural properties.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void testDifferentMaterials(GameTestHelper helper) {
        // Build a stone column
        for (int y = 0; y < 2; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
        }
        
        // Add different materials on top
        helper.setBlock(0, 2, 0, Blocks.OAK_LOG);
        helper.setBlock(0, 3, 0, Blocks.GLASS);
        
        // Build a horizontal extension from the glass (should be unstable)
        for (int x = 1; x < 3; x++) {
            helper.setBlock(x, 3, 0, Blocks.GLASS);
        }
        
        // Wait to ensure stability is calculated
        helper.runAfterDelay(20, () -> {
            // Verify stone and wood are still there
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 0, 0));
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.OAK_LOG, new BlockPos(0, 2, 0));
            helper.assertBlockPresent(Blocks.GLASS, new BlockPos(0, 3, 0));
            
            // Verify the furthest glass block has collapsed
            // Note: The exact behavior will depend on your implementation
            helper.assertBlockNotPresent(Blocks.GLASS, new BlockPos(2, 3, 0));
            
            helper.succeed();
        });
    }
}
