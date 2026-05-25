/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  org.slf4j.Logger
 *  software.bernie.geckolib.GeckoLib
 */
package keletu.guaniao;

import com.mojang.logging.LogUtils;
import keletu.guaniao.registry.GuaniaoCreativeTabs;
import keletu.guaniao.registry.GuaniaoBlocks;
import keletu.guaniao.registry.GuaniaoEntityTypes;
import keletu.guaniao.registry.GuaniaoItems;
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
        GuaniaoItems.ITEMS.register(modEventBus);
        GuaniaoEntityTypes.ENTITY_TYPES.register(modEventBus);
        GuaniaoCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
