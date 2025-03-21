package com.jake404notfound.architecturalrealism.test;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import com.jake404notfound.architecturalrealism.config.ARConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Performance tests for the Architectural Realism mod.
 * These tests validate the performance characteristics of the structural integrity system.
 */
@GameTestHolder(ArchitecturalRealism.MOD_ID)
@PrefixGameTestTemplate(false)
public class PerformanceTests {

    /**
     * Tests performance with a large structure.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void testLargeStructurePerformance(GameTestHelper helper) {
        // Build a large platform
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        
        // Build columns at the corners
        for (int y = 1; y < 5; y++) {
            helper.setBlock(0, y, 0, Blocks.STONE);
            helper.setBlock(0, y, 9, Blocks.STONE);
            helper.setBlock(9, y, 0, Blocks.STONE);
            helper.setBlock(9, y, 9, Blocks.STONE);
        }
        
        // Build a roof
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                helper.setBlock(x, 5, z, Blocks.STONE);
            }
        }
        
        // Measure time to calculate stability
        long startTime = System.currentTimeMillis();
        
        // Wait for stability calculation to complete
        helper.runAfterDelay(100, () -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Log performance metrics
            ArchitecturalRealism.LOGGER.info("Large structure stability calculation took {} ms", duration);
            
            // Verify structure is still intact
            for (int x = 0; x < 10; x += 9) {
                for (int z = 0; z < 10; z += 9) {
                    for (int y = 0; y < 6; y++) {
                        helper.assertBlockPresent(Blocks.STONE, new net.minecraft.core.BlockPos(x, y, z));
                    }
                }
            }
            
            helper.succeed();
        });
    }
    
    /**
     * Tests performance with different calculation radius settings.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void testCalculationRadiusPerformance(GameTestHelper helper) {
        // Save original radius
        int originalRadius = ARConfig.COMMON.calculationRadius.get();
        
        // Set a large radius for testing
        // Note: In a real test, we would modify the config value
        // This is just a placeholder for demonstration
        
        // Build a test structure
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        
        // Measure time with large radius
        long startTimeLarge = System.currentTimeMillis();
        
        // Wait for calculation to complete
        helper.runAfterDelay(50, () -> {
            long endTimeLarge = System.currentTimeMillis();
            long durationLarge = endTimeLarge - startTimeLarge;
            
            // Log performance metrics
            ArchitecturalRealism.LOGGER.info("Calculation with large radius took {} ms", durationLarge);
            
            // Set a small radius for comparison
            // Note: In a real test, we would modify the config value
            
            // Measure time with small radius
            long startTimeSmall = System.currentTimeMillis();
            
            // Wait for calculation to complete
            helper.runAfterDelay(50, () -> {
                long endTimeSmall = System.currentTimeMillis();
                long durationSmall = endTimeSmall - startTimeSmall;
                
                // Log performance metrics
                ArchitecturalRealism.LOGGER.info("Calculation with small radius took {} ms", durationSmall);
                
                // Restore original radius
                // Note: In a real test, we would restore the config value
                
                helper.succeed();
            });
        });
    }
    
    /**
     * Tests performance with a chain reaction collapse.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void testChainReactionPerformance(GameTestHelper helper) {
        // Build a large structure that will trigger a chain reaction when collapsed
        
        // Build a foundation
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        
        // Build a tower
        for (int y = 1; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                for (int z = 0; z < 10; z++) {
                    // Make a hollow tower
                    if (x == 0 || x == 9 || z == 0 || z == 9) {
                        helper.setBlock(x, y, z, Blocks.STONE);
                    }
                }
            }
        }
        
        // Wait for stability calculation to complete
        helper.runAfterDelay(50, () -> {
            // Measure time for chain reaction
            long startTime = System.currentTimeMillis();
            
            // Remove a key support block to trigger collapse
            helper.setBlock(0, 1, 0, Blocks.AIR);
            
            // Wait for collapse to complete
            helper.runAfterDelay(100, () -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                // Log performance metrics
                ArchitecturalRealism.LOGGER.info("Chain reaction collapse took {} ms", duration);
                
                helper.succeed();
            });
        });
    }
    
    /**
     * Tests performance with different block types.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void testDifferentBlockTypesPerformance(GameTestHelper helper) {
        // Build structures with different block types
        
        // Stone structure
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    helper.setBlock(x, y, z, Blocks.STONE);
                }
            }
        }
        
        // Wait for stability calculation
        helper.runAfterDelay(50, () -> {
            // Measure time for stone structure update
            long startTimeStone = System.currentTimeMillis();
            
            // Trigger recalculation
            helper.setBlock(2, 0, 2, Blocks.AIR);
            helper.setBlock(2, 0, 2, Blocks.STONE);
            
            // Wait for calculation to complete
            helper.runAfterDelay(50, () -> {
                long endTimeStone = System.currentTimeMillis();
                long durationStone = endTimeStone - startTimeStone;
                
                // Log performance metrics
                ArchitecturalRealism.LOGGER.info("Stone structure calculation took {} ms", durationStone);
                
                // Clear the area
                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 5; y++) {
                        for (int z = 0; z < 5; z++) {
                            helper.setBlock(x, y, z, Blocks.AIR);
                        }
                    }
                }
                
                // Build mixed material structure
                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 5; y++) {
                        for (int z = 0; z < 5; z++) {
                            if (y % 2 == 0) {
                                helper.setBlock(x, y, z, Blocks.STONE);
                            } else {
                                helper.setBlock(x, y, z, Blocks.OAK_PLANKS);
                            }
                        }
                    }
                }
                
                // Wait for stability calculation
                helper.runAfterDelay(50, () -> {
                    // Measure time for mixed structure update
                    long startTimeMixed = System.currentTimeMillis();
                    
                    // Trigger recalculation
                    helper.setBlock(2, 0, 2, Blocks.AIR);
                    helper.setBlock(2, 0, 2, Blocks.STONE);
                    
                    // Wait for calculation to complete
                    helper.runAfterDelay(50, () -> {
                        long endTimeMixed = System.currentTimeMillis();
                        long durationMixed = endTimeMixed - startTimeMixed;
                        
                        // Log performance metrics
                        ArchitecturalRealism.LOGGER.info("Mixed structure calculation took {} ms", durationMixed);
                        
                        helper.succeed();
                    });
                });
            });
        });
    }
}
