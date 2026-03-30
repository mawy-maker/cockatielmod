package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CockatielMod.MOD_ID);

    public static final Supplier<SoundEvent> COCKATIEL_AMBIENT =
            reg("cockatiel_ambient");
    public static final Supplier<SoundEvent> COCKATIEL_HURT =
            reg("cockatiel_hurt");
    public static final Supplier<SoundEvent> COCKATIEL_DEATH =
            reg("cockatiel_death");
    public static final Supplier<SoundEvent> COCKATIEL_SONG =
            reg("cockatiel_song");
    public static final Supplier<SoundEvent> COCKATIEL_ALERT =
            reg("cockatiel_alert");
    public static final Supplier<SoundEvent> COCKATIEL_EAT =
            reg("cockatiel_eat");
    public static final Supplier<SoundEvent> COCKATIEL_HAPPY =
            reg("cockatiel_happy");
    public static final Supplier<SoundEvent> WHISTLE_BLOW =
            reg("whistle_blow");

    private static Supplier<SoundEvent> reg(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl(name)));
    }
}
