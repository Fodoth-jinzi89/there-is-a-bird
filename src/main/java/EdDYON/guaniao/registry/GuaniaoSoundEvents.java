package EdDYON.guaniao.registry;

import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GuaniaoMod.MOD_ID);

    public static final RegistryObject<SoundEvent> NIGHT_HERON_AMBIENT = register("entity.night_heron.ambient");
    public static final RegistryObject<SoundEvent> NIGHT_HERON_HURT = register("entity.night_heron.hurt");
    public static final RegistryObject<SoundEvent> NIGHT_HERON_DEATH = register("entity.night_heron.death");
    public static final RegistryObject<SoundEvent> NIGHT_HERON_ATTACK = register("entity.night_heron.attack");

    private GuaniaoSoundEvents() {
    }

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(GuaniaoMod.MOD_ID, id)));
    }
}
