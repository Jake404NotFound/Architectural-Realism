# Architectural Realism: Block Properties

This document defines the structural properties for different block types in Minecraft. These properties determine how blocks interact with the structural integrity system.

## Property Definitions

- **Weight**: The mass of the block, affecting how much support it needs and how much stress it puts on supporting blocks
- **Compression Strength**: How well the block supports weight directly above it (vertical support)
- **Tensile Strength**: How well the block can support weight when stretched (horizontal support)
- **Shear Strength**: How well the block resists sideways forces (diagonal support)
- **Maximum Load**: The maximum weight a block can support before failing
- **Fragility**: How likely the block is to break rather than fall when support is lost

## Material Categories

### Stone-based Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Stone | 8 | 10 | 2 | 4 | 20 | Low |
| Granite | 9 | 11 | 2 | 4 | 22 | Low |
| Diorite | 8 | 10 | 2 | 4 | 20 | Low |
| Andesite | 8 | 10 | 2 | 4 | 20 | Low |
| Cobblestone | 7 | 8 | 1 | 3 | 16 | Medium |
| Stone Bricks | 8 | 12 | 3 | 5 | 24 | Low |
| Deepslate | 10 | 14 | 3 | 6 | 28 | Low |
| Blackstone | 9 | 12 | 2 | 5 | 24 | Low |
| Basalt | 8 | 11 | 2 | 4 | 22 | Medium |
| Obsidian | 12 | 20 | 5 | 8 | 40 | Very Low |
| Bedrock | 100 | 100 | 100 | 100 | 100 | None |

### Wood-based Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Oak Wood | 4 | 6 | 8 | 5 | 12 | Medium |
| Spruce Wood | 4 | 6 | 8 | 5 | 12 | Medium |
| Birch Wood | 3 | 5 | 7 | 4 | 10 | Medium |
| Jungle Wood | 4 | 6 | 8 | 5 | 12 | Medium |
| Acacia Wood | 5 | 7 | 9 | 6 | 14 | Medium |
| Dark Oak Wood | 5 | 7 | 9 | 6 | 14 | Medium |
| Mangrove Wood | 4 | 6 | 8 | 5 | 12 | Medium |
| Cherry Wood | 3 | 5 | 7 | 4 | 10 | Medium |
| Bamboo | 2 | 4 | 6 | 3 | 8 | High |
| Planks (all types) | 3 | 4 | 7 | 4 | 8 | Medium |

### Earth Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Dirt | 5 | 3 | 0 | 1 | 6 | High |
| Grass Block | 5 | 3 | 0 | 1 | 6 | High |
| Podzol | 5 | 3 | 0 | 1 | 6 | High |
| Mycelium | 5 | 3 | 0 | 1 | 6 | High |
| Sand | 6 | 1 | 0 | 0 | 2 | Very High |
| Red Sand | 6 | 1 | 0 | 0 | 2 | Very High |
| Gravel | 6 | 2 | 0 | 1 | 4 | Very High |
| Clay | 5 | 2 | 0 | 1 | 4 | High |

### Metal Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Iron Block | 9 | 15 | 15 | 10 | 30 | Very Low |
| Gold Block | 12 | 10 | 8 | 6 | 20 | Low |
| Copper Block | 8 | 12 | 12 | 8 | 24 | Low |
| Netherite Block | 14 | 20 | 20 | 15 | 40 | None |
| Chain | 3 | 2 | 12 | 2 | 15 | Low |
| Iron Bars | 2 | 3 | 8 | 4 | 10 | Low |

### Glass and Fragile Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Glass | 4 | 4 | 1 | 1 | 8 | Very High |
| Stained Glass | 4 | 4 | 1 | 1 | 8 | Very High |
| Glass Pane | 2 | 2 | 1 | 1 | 4 | Very High |
| Ice | 5 | 3 | 1 | 1 | 6 | Very High |
| Packed Ice | 6 | 5 | 2 | 2 | 10 | High |
| Blue Ice | 7 | 6 | 3 | 3 | 12 | Medium |

### Special Building Materials

| Block Type | Weight | Compression | Tensile | Shear | Max Load | Fragility |
|------------|--------|-------------|---------|-------|----------|-----------|
| Brick | 7 | 9 | 2 | 4 | 18 | Medium |
| Nether Brick | 7 | 9 | 2 | 4 | 18 | Medium |
| Terracotta | 6 | 7 | 1 | 3 | 14 | Medium |
| Concrete | 8 | 13 | 3 | 5 | 26 | Low |
| Concrete Powder | 6 | 1 | 0 | 0 | 2 | Very High |
| Wool | 2 | 1 | 1 | 1 | 2 | High |
| Slime Block | 4 | 2 | 6 | 6 | 8 | Low |
| Honey Block | 5 | 3 | 4 | 4 | 6 | Low |

## Vanilla Block Combinations for Structural Support

Instead of adding new blocks, players can use strategic combinations of vanilla blocks to create stronger structures:

| Combination | Example | Structural Benefit |
|-------------|---------|-------------------|
| Stone + Iron Bars | Place iron bars adjacent to stone | Increases tensile strength of the structure |
| Double Walls | Two layers of blocks side by side | Significantly improves stability and load capacity |
| Column Design | Vertical stack of solid blocks | Maximizes compression strength for vertical support |
| Arch Design | Curved arrangement of blocks | Distributes weight more effectively than flat spans |
| Diagonal Bracing | Blocks placed in diagonal patterns | Improves resistance to lateral forces |
| Mixed Materials | Combining stone and wood strategically | Utilizes the strengths of different materials |
| Foundation Layering | Multiple layers at the base | Creates stronger foundation support |

## Implementation Notes

1. These values are initial suggestions and will require balancing during testing
2. All values are on a scale of 0-100, with higher numbers representing stronger properties
3. The configuration system will allow server administrators to adjust these values
4. Mod compatibility will be implemented to assign reasonable default values to modded blocks
5. Special consideration will be given to blocks with unique properties (e.g., slime blocks, honey blocks)
