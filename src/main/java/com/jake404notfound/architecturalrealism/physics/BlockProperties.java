package com.jake404notfound.architecturalrealism.physics;

/**
 * Represents the physical properties of a block for structural integrity calculations.
 */
public class BlockProperties {
    private final double weight;
    private final double compressionStrength;
    private final double tensileStrength;
    private final double shearStrength;
    private final double maxLoad;
    private final double fragility;
    
    /**
     * Creates a new BlockProperties instance.
     *
     * @param weight The weight of the block (0-100)
     * @param compressionStrength How well the block supports weight directly above it (0-100)
     * @param tensileStrength How well the block can support weight when stretched horizontally (0-100)
     * @param shearStrength How well the block resists sideways forces (0-100)
     * @param maxLoad The maximum weight a block can support before failing (0-100)
     * @param fragility How likely the block is to break rather than fall when support is lost (0.0-1.0)
     */
    public BlockProperties(double weight, double compressionStrength, double tensileStrength, 
                          double shearStrength, double maxLoad, double fragility) {
        this.weight = weight;
        this.compressionStrength = compressionStrength;
        this.tensileStrength = tensileStrength;
        this.shearStrength = shearStrength;
        this.maxLoad = maxLoad;
        this.fragility = fragility;
    }
    
    /**
     * Gets the weight of the block.
     * Higher values mean the block puts more stress on supporting blocks.
     *
     * @return The weight value (0-100)
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * Gets the compression strength of the block.
     * Higher values mean the block is better at supporting weight directly above it.
     *
     * @return The compression strength value (0-100)
     */
    public double getCompressionStrength() {
        return compressionStrength;
    }
    
    /**
     * Gets the tensile strength of the block.
     * Higher values mean the block is better at supporting weight horizontally.
     *
     * @return The tensile strength value (0-100)
     */
    public double getTensileStrength() {
        return tensileStrength;
    }
    
    /**
     * Gets the shear strength of the block.
     * Higher values mean the block is better at resisting sideways forces.
     *
     * @return The shear strength value (0-100)
     */
    public double getShearStrength() {
        return shearStrength;
    }
    
    /**
     * Gets the maximum load the block can support.
     * This is the upper limit of support that can be transferred through this block.
     *
     * @return The maximum load value (0-100)
     */
    public double getMaxLoad() {
        return maxLoad;
    }
    
    /**
     * Gets the fragility of the block.
     * Higher values mean the block is more likely to break rather than fall when support is lost.
     *
     * @return The fragility value (0.0-1.0)
     */
    public double getFragility() {
        return fragility;
    }
}
