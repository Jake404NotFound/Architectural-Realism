package com.jake404notfound.architecturalrealism.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ARConfig {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final Common COMMON = new Common(COMMON_BUILDER);
    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    public static class Common {
        public final ModConfigSpec.BooleanValue enableStructuralIntegrity;
        public final ModConfigSpec.IntValue maxSupportDistance;
        public final ModConfigSpec.IntValue calculationRadius;
        public final ModConfigSpec.IntValue maxCalculationsPerTick;
        public final ModConfigSpec.BooleanValue enableVisualFeedback;
        public final ModConfigSpec.BooleanValue enableCreativeBypass;
        public final ModConfigSpec.EnumValue<PhysicsMode> physicsMode;
        
        // Physics configuration options
        public final ModConfigSpec.IntValue foundationDepth;
        public final ModConfigSpec.BooleanValue enableHangingSupport;
        public final ModConfigSpec.DoubleValue supportFactor;
        public final ModConfigSpec.DoubleValue verticalSupportFactor;
        public final ModConfigSpec.DoubleValue horizontalSupportFactor;
        public final ModConfigSpec.DoubleValue hangingSupportFactor;
        public final ModConfigSpec.DoubleValue supportDecayFactor;
        public final ModConfigSpec.DoubleValue stabilityThreshold;
        public final ModConfigSpec.IntValue supportCacheSize;
        public final ModConfigSpec.BooleanValue enableDiagonalConnections;
        public final ModConfigSpec.DoubleValue diagonalSupportFactor;
        public final ModConfigSpec.BooleanValue enableFallingBlocks;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Architectural Realism Configuration")
                   .push("general");

            enableStructuralIntegrity = builder
                    .comment("Enable structural integrity mechanics")
                    .define("enableStructuralIntegrity", true);

            physicsMode = builder
                    .comment("Physics calculation mode: REALISTIC (full structural integrity), SIMPLE (only disconnected blocks fall), NONE (disable physics)")
                    .defineEnum("physicsMode", PhysicsMode.REALISTIC);

            enableCreativeBypass = builder
                    .comment("Bypass structural integrity checks in creative mode")
                    .define("enableCreativeBypass", true);

            builder.pop().push("physics");
            
            foundationDepth = builder
                    .comment("How many solid blocks beneath a block are required to consider it a foundation")
                    .defineInRange("foundationDepth", 3, 1, 10);
                    
            enableHangingSupport = builder
                    .comment("Enable support for hanging structures (blocks can hang from above)")
                    .define("enableHangingSupport", true);
                    
            supportFactor = builder
                    .comment("Base multiplier for support calculations")
                    .defineInRange("supportFactor", 1.5, 0.5, 5.0);
                    
            verticalSupportFactor = builder
                    .comment("Support factor for blocks supporting from below (higher values provide more support)")
                    .defineInRange("verticalSupportFactor", 1.0, 0.1, 2.0);
                    
            horizontalSupportFactor = builder
                    .comment("Support factor for blocks supporting from the sides (higher values provide more support)")
                    .defineInRange("horizontalSupportFactor", 0.7, 0.1, 1.0);
                    
            hangingSupportFactor = builder
                    .comment("Support factor for blocks supporting from above (higher values provide more support)")
                    .defineInRange("hangingSupportFactor", 0.5, 0.1, 1.0);
                    
            supportDecayFactor = builder
                    .comment("How much support decays with distance (lower values mean faster decay)")
                    .defineInRange("supportDecayFactor", 0.9, 0.5, 0.99);
                    
            stabilityThreshold = builder
                    .comment("Minimum support value required for a block to be stable")
                    .defineInRange("stabilityThreshold", 10.0, 1.0, 50.0);
                    
            enableDiagonalConnections = builder
                    .comment("Enable support propagation through diagonal connections")
                    .define("enableDiagonalConnections", true);
                    
            diagonalSupportFactor = builder
                    .comment("Support factor for diagonal connections (lower values mean less support through diagonals)")
                    .defineInRange("diagonalSupportFactor", 0.7, 0.1, 1.0);
                    
            enableFallingBlocks = builder
                    .comment("Enable falling block entities for unstable blocks (if false, blocks are just destroyed)")
                    .define("enableFallingBlocks", true);

            builder.pop().push("performance");

            maxSupportDistance = builder
                    .comment("Maximum distance that support can propagate from a foundation block")
                    .defineInRange("maxSupportDistance", 32, 1, 128);

            calculationRadius = builder
                    .comment("Radius around changed blocks to recalculate support")
                    .defineInRange("calculationRadius", 8, 1, 32);

            maxCalculationsPerTick = builder
                    .comment("Maximum number of block calculations per tick (higher values may impact performance)")
                    .defineInRange("maxCalculationsPerTick", 1000, 100, 10000);
                    
            supportCacheSize = builder
                    .comment("Maximum number of block positions to cache support values for (per dimension)")
                    .defineInRange("supportCacheSize", 5000, 1000, 50000);

            builder.pop().push("visual");

            enableVisualFeedback = builder
                    .comment("Enable visual indicators for structural stress")
                    .define("enableVisualFeedback", true);

            builder.pop();
        }
    }

    public enum PhysicsMode {
        REALISTIC,
        SIMPLE,
        NONE
    }
}
