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
        
        // New configuration options
        public final ModConfigSpec.IntValue foundationDepth;
        public final ModConfigSpec.BooleanValue enableHangingSupport;
        public final ModConfigSpec.DoubleValue supportFactor;
        public final ModConfigSpec.IntValue supportCacheSize;
        public final ModConfigSpec.BooleanValue enableDiagonalConnections;
        public final ModConfigSpec.DoubleValue diagonalSupportFactor;

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
                    .comment("Multiplier for required support based on block weight (higher values require more support)")
                    .defineInRange("supportFactor", 1.5, 0.5, 5.0);
                    
            enableDiagonalConnections = builder
                    .comment("Enable support propagation through diagonal connections")
                    .define("enableDiagonalConnections", true);
                    
            diagonalSupportFactor = builder
                    .comment("Support factor for diagonal connections (lower values mean less support through diagonals)")
                    .defineInRange("diagonalSupportFactor", 0.7, 0.1, 1.0);

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
