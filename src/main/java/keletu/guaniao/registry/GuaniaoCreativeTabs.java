/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package keletu.guaniao.registry;

import keletu.guaniao.registry.GuaniaoItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"guaniao");
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.guaniao.main")).icon(() -> new ItemStack((ItemLike)GuaniaoItems.NIGHT_HERON_SPAWN_EGG.get())).displayItems((parameters, output) -> {
        output.accept((ItemLike)GuaniaoItems.BIRD_GUIDE.get());
        output.accept((ItemLike)GuaniaoItems.BREADCRUMBS.get());
        output.accept((ItemLike)GuaniaoItems.NIGHT_HERON_SPAWN_EGG.get());
        output.accept((ItemLike)GuaniaoItems.SPARROW_SPAWN_EGG.get());
    }).build());

    private GuaniaoCreativeTabs() {
    }
}
