/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package keletu.guaniao.client.entity.nightheron;

import keletu.guaniao.content.bird.nightheron.NightHeronDefinition;
import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightHeronModel
extends GeoModel<NightHeronEntity> {
    public ResourceLocation getModelResource(NightHeronEntity animatable) {
        return NightHeronDefinition.MODEL;
    }

    public ResourceLocation getTextureResource(NightHeronEntity animatable) {
        return NightHeronDefinition.TEXTURE;
    }

    public ResourceLocation getAnimationResource(NightHeronEntity animatable) {
        return NightHeronDefinition.ANIMATION;
    }
}

