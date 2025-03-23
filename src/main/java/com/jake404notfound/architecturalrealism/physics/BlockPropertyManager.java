package com.jake404notfound.architecturalrealism.physics;

import com.jake404notfound.architecturalrealism.ArchitecturalRealism;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages block properties for the structural integrity system.
 * This class loads and provides access to the physical properties of different block types.
 */
public class BlockPropertyManager {
    
    private final Map<Block, BlockProperties> blockPropertiesMap = new HashMap<>();
    
    /**
     * Loads block properties for all relevant blocks.
     * In a production version, this would load from configuration files.
     */
    public void loadBlockProperties() {
        ArchitecturalRealism.LOGGER.info("Loading block properties for structural integrity calculations");
        
        // Initialize with default properties
        initializeDefaultProperties();
        
        // Load stone-based materials
        loadStoneBasedMaterials();
        
        // Load wood-based materials
        loadWoodBasedMaterials();
        
        // Load earth materials
        loadEarthMaterials();
        
        // Load metal materials
        loadMetalMaterials();
        
        // Load glass and fragile materials
        loadGlassMaterials();
        
        // Load special building materials
        loadSpecialBuildingMaterials();
        
        ArchitecturalRealism.LOGGER.info("Loaded properties for {} block types", blockPropertiesMap.size());
    }
    
    /**
     * Gets the structural properties for a specific block.
     * If no specific properties are defined, returns default properties.
     */
    public BlockProperties getBlockProperties(Block block) {
        return blockPropertiesMap.getOrDefault(block, getDefaultProperties());
    }
    
    /**
     * Gets the support factor for a specific block.
     * This factor determines how well the block can transfer support to adjacent blocks.
     * 
     * @param block The block to get the support factor for
     * @return The support factor value (higher is better at transferring support)
     */
    public double getSupportFactor(Block block) {
        BlockProperties props = getBlockProperties(block);
        // Calculate support factor based on compression and tensile strength
        return (props.getCompressionStrength() + props.getTensileStrength()) / 20.0;
    }
    
    /**
     * Initialize default properties for unknown blocks
     */
    private void initializeDefaultProperties() {
        // Default properties for any block not specifically defined
        BlockProperties defaultProps = new BlockProperties(5, 5, 2, 2, 10, 0.5);
        blockPropertiesMap.put(null, defaultProps); // Null key for default properties
    }
    
    /**
     * Get default properties for blocks not specifically defined
     */
    private BlockProperties getDefaultProperties() {
        return blockPropertiesMap.get(null);
    }
    
    /**
     * Load properties for stone-based materials
     */
    private void loadStoneBasedMaterials() {
        // Stone
        blockPropertiesMap.put(Blocks.STONE, new BlockProperties(8, 10, 2, 4, 20, 0.1));
        blockPropertiesMap.put(Blocks.GRANITE, new BlockProperties(9, 11, 2, 4, 22, 0.1));
        blockPropertiesMap.put(Blocks.DIORITE, new BlockProperties(8, 10, 2, 4, 20, 0.1));
        blockPropertiesMap.put(Blocks.ANDESITE, new BlockProperties(8, 10, 2, 4, 20, 0.1));
        
        // Cobblestone
        blockPropertiesMap.put(Blocks.COBBLESTONE, new BlockProperties(7, 8, 1, 3, 16, 0.3));
        
        // Stone bricks
        blockPropertiesMap.put(Blocks.STONE_BRICKS, new BlockProperties(8, 12, 3, 5, 24, 0.1));
        
        // Deepslate
        blockPropertiesMap.put(Blocks.DEEPSLATE, new BlockProperties(10, 14, 3, 6, 28, 0.1));
        blockPropertiesMap.put(Blocks.COBBLED_DEEPSLATE, new BlockProperties(9, 12, 2, 5, 24, 0.2));
        blockPropertiesMap.put(Blocks.DEEPSLATE_BRICKS, new BlockProperties(10, 15, 4, 6, 30, 0.1));
        
        // Blackstone
        blockPropertiesMap.put(Blocks.BLACKSTONE, new BlockProperties(9, 12, 2, 5, 24, 0.1));
        
        // Basalt
        blockPropertiesMap.put(Blocks.BASALT, new BlockProperties(8, 11, 2, 4, 22, 0.3));
        
        // Obsidian
        blockPropertiesMap.put(Blocks.OBSIDIAN, new BlockProperties(12, 20, 5, 8, 40, 0.05));
        
        // Bedrock - indestructible
        blockPropertiesMap.put(Blocks.BEDROCK, new BlockProperties(100, 100, 100, 100, 100, 0.0));
    }
    
    /**
     * Load properties for wood-based materials
     */
    private void loadWoodBasedMaterials() {
        // Logs
        BlockProperties oakLogProps = new BlockProperties(4, 6, 8, 5, 12, 0.3);
        blockPropertiesMap.put(Blocks.OAK_LOG, oakLogProps);
        blockPropertiesMap.put(Blocks.SPRUCE_LOG, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.BIRCH_LOG, new BlockProperties(3, 5, 7, 4, 10, 0.3));
        blockPropertiesMap.put(Blocks.JUNGLE_LOG, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.ACACIA_LOG, new BlockProperties(5, 7, 9, 6, 14, 0.3));
        blockPropertiesMap.put(Blocks.DARK_OAK_LOG, new BlockProperties(5, 7, 9, 6, 14, 0.3));
        blockPropertiesMap.put(Blocks.MANGROVE_LOG, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.CHERRY_LOG, new BlockProperties(3, 5, 7, 4, 10, 0.3));
        
        // Wood
        blockPropertiesMap.put(Blocks.OAK_WOOD, oakLogProps);
        blockPropertiesMap.put(Blocks.SPRUCE_WOOD, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.BIRCH_WOOD, new BlockProperties(3, 5, 7, 4, 10, 0.3));
        blockPropertiesMap.put(Blocks.JUNGLE_WOOD, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.ACACIA_WOOD, new BlockProperties(5, 7, 9, 6, 14, 0.3));
        blockPropertiesMap.put(Blocks.DARK_OAK_WOOD, new BlockProperties(5, 7, 9, 6, 14, 0.3));
        blockPropertiesMap.put(Blocks.MANGROVE_WOOD, new BlockProperties(4, 6, 8, 5, 12, 0.3));
        blockPropertiesMap.put(Blocks.CHERRY_WOOD, new BlockProperties(3, 5, 7, 4, 10, 0.3));
        
        // Planks
        BlockProperties plankProps = new BlockProperties(3, 4, 7, 4, 8, 0.3);
        blockPropertiesMap.put(Blocks.OAK_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.SPRUCE_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.BIRCH_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.JUNGLE_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.ACACIA_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.DARK_OAK_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.MANGROVE_PLANKS, plankProps);
        blockPropertiesMap.put(Blocks.CHERRY_PLANKS, plankProps);
        
        // Bamboo
        blockPropertiesMap.put(Blocks.BAMBOO, new BlockProperties(2, 4, 6, 3, 8, 0.7));
        blockPropertiesMap.put(Blocks.BAMBOO_PLANKS, plankProps);
    }
    
    /**
     * Load properties for earth materials
     */
    private void loadEarthMaterials() {
        // Dirt
        BlockProperties dirtProps = new BlockProperties(5, 3, 0, 1, 6, 0.7);
        blockPropertiesMap.put(Blocks.DIRT, dirtProps);
        blockPropertiesMap.put(Blocks.GRASS_BLOCK, dirtProps);
        blockPropertiesMap.put(Blocks.PODZOL, dirtProps);
        blockPropertiesMap.put(Blocks.MYCELIUM, dirtProps);
        
        // Sand
        BlockProperties sandProps = new BlockProperties(6, 1, 0, 0, 2, 0.9);
        blockPropertiesMap.put(Blocks.SAND, sandProps);
        blockPropertiesMap.put(Blocks.RED_SAND, sandProps);
        
        // Gravel
        blockPropertiesMap.put(Blocks.GRAVEL, new BlockProperties(6, 2, 0, 1, 4, 0.9));
        
        // Clay
        blockPropertiesMap.put(Blocks.CLAY, new BlockProperties(5, 2, 0, 1, 4, 0.7));
    }
    
    /**
     * Load properties for metal materials
     */
    private void loadMetalMaterials() {
        // Iron
        blockPropertiesMap.put(Blocks.IRON_BLOCK, new BlockProperties(9, 15, 15, 10, 30, 0.05));
        
        // Gold
        blockPropertiesMap.put(Blocks.GOLD_BLOCK, new BlockProperties(12, 10, 8, 6, 20, 0.1));
        
        // Copper
        blockPropertiesMap.put(Blocks.COPPER_BLOCK, new BlockProperties(8, 12, 12, 8, 24, 0.1));
        
        // Netherite
        blockPropertiesMap.put(Blocks.NETHERITE_BLOCK, new BlockProperties(14, 20, 20, 15, 40, 0.0));
        
        // Chain
        blockPropertiesMap.put(Blocks.CHAIN, new BlockProperties(3, 2, 12, 2, 15, 0.1));
        
        // Iron Bars
        blockPropertiesMap.put(Blocks.IRON_BARS, new BlockProperties(2, 3, 8, 4, 10, 0.1));
    }
    
    /**
     * Load properties for glass and fragile materials
     */
    private void loadGlassMaterials() {
        // Glass
        BlockProperties glassProps = new BlockProperties(4, 4, 1, 1, 8, 0.9);
        blockPropertiesMap.put(Blocks.GLASS, glassProps);
        
        // Stained Glass
        blockPropertiesMap.put(Blocks.WHITE_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.ORANGE_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.MAGENTA_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.YELLOW_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.LIME_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.PINK_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.GRAY_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.CYAN_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.PURPLE_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.BLUE_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.BROWN_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.GREEN_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.RED_STAINED_GLASS, glassProps);
        blockPropertiesMap.put(Blocks.BLACK_STAINED_GLASS, glassProps);
        
        // Glass Panes
        BlockProperties glassPaneProps = new BlockProperties(2, 2, 1, 1, 4, 0.9);
        blockPropertiesMap.put(Blocks.GLASS_PANE, glassPaneProps);
        
        // Ice
        blockPropertiesMap.put(Blocks.ICE, new BlockProperties(5, 3, 1, 1, 6, 0.9));
        blockPropertiesMap.put(Blocks.PACKED_ICE, new BlockProperties(6, 5, 2, 2, 10, 0.7));
        blockPropertiesMap.put(Blocks.BLUE_ICE, new BlockProperties(7, 6, 3, 3, 12, 0.3));
    }
    
    /**
     * Load properties for special building materials
     */
    private void loadSpecialBuildingMaterials() {
        // Brick
        blockPropertiesMap.put(Blocks.BRICKS, new BlockProperties(7, 9, 2, 4, 18, 0.3));
        
        // Nether Brick
        blockPropertiesMap.put(Blocks.NETHER_BRICKS, new BlockProperties(7, 9, 2, 4, 18, 0.3));
        
        // Terracotta
        BlockProperties terracottaProps = new BlockProperties(6, 7, 1, 3, 14, 0.3);
        blockPropertiesMap.put(Blocks.TERRACOTTA, terracottaProps);
        
        // Concrete
        BlockProperties concreteProps = new BlockProperties(8, 13, 3, 5, 26, 0.1);
        blockPropertiesMap.put(Blocks.WHITE_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.ORANGE_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.MAGENTA_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.LIGHT_BLUE_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.YELLOW_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.LIME_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.PINK_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.GRAY_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.LIGHT_GRAY_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.CYAN_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.PURPLE_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.BLUE_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.BROWN_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.GREEN_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.RED_CONCRETE, concreteProps);
        blockPropertiesMap.put(Blocks.BLACK_CONCRETE, concreteProps);
        
        // Concrete Powder
        BlockProperties concretePowderProps = new BlockProperties(6, 1, 0, 0, 2, 0.9);
        blockPropertiesMap.put(Blocks.WHITE_CONCRETE_POWDER, concretePowderProps);
        
        // Wool
        BlockProperties woolProps = new BlockProperties(2, 1, 1, 1, 2, 0.7);
        blockPropertiesMap.put(Blocks.WHITE_WOOL, woolProps);
        
        // Slime Block
        blockPropertiesMap.put(Blocks.SLIME_BLOCK, new BlockProperties(4, 2, 6, 6, 8, 0.1));
        
        // Honey Block
        blockPropertiesMap.put(Blocks.HONEY_BLOCK, new BlockProperties(5, 3, 4, 4, 6, 0.1));
    }
}
