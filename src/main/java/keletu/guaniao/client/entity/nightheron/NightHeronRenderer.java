/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoEntityRenderer
 */
package keletu.guaniao.client.entity.nightheron;

import keletu.guaniao.client.entity.nightheron.NightHeronModel;
import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightHeronRenderer
extends GeoEntityRenderer<NightHeronEntity> {
    public NightHeronRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, (GeoModel)new NightHeronModel());
        this.shadowRadius = 0.45f;
    }
}

