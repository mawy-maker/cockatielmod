package com.cockatielmod.cockatiel;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CockatielConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HUNGER_THRESHOLD_TICKS;
    public static final ModConfigSpec.IntValue CREEPER_ALERT_RANGE;
    public static final ModConfigSpec.IntValue WHISTLE_RANGE;
    public static final ModConfigSpec.IntValue SONG_DURATION_TICKS;

    static {
        BUILDER.push("Cockatiel");
        HUNGER_THRESHOLD_TICKS = BUILDER
                .comment("Ticks before hungry cockatiel steals crops (24000 = 1 day)")
                .defineInRange("hungerThresholdTicks", 24000, 6000, 72000);
        CREEPER_ALERT_RANGE = BUILDER
                .comment("Creeper detection range in blocks")
                .defineInRange("creeperAlertRange", 16, 4, 64);
        WHISTLE_RANGE = BUILDER
                .comment("Whistle range in blocks")
                .defineInRange("whistleRange", 200, 50, 500);
        SONG_DURATION_TICKS = BUILDER
                .comment("Morning song duration in ticks")
                .defineInRange("songDurationTicks", 120, 20, 400);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
