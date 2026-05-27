package EdDYON.guaniao;

import com.mojang.logging.LogUtils;
import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import EdDYON.guaniao.registry.GuaniaoCreativeTabs;
import EdDYON.guaniao.registry.GuaniaoBlocks;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import EdDYON.guaniao.registry.GuaniaoItems;
import EdDYON.guaniao.registry.GuaniaoSoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(value="guaniao")
public class GuaniaoMod {
    public static final String MOD_ID = "guaniao";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GuaniaoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();
        GuaniaoBlocks.BLOCKS.register(modEventBus);
        GuaniaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        GuaniaoItems.ITEMS.register(modEventBus);
        GuaniaoEntityTypes.ENTITY_TYPES.register(modEventBus);
        GuaniaoSoundEvents.SOUND_EVENTS.register(modEventBus);
        GuaniaoCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
