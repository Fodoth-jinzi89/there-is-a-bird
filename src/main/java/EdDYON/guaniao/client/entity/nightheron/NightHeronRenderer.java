package EdDYON.guaniao.client.entity.nightheron;

import EdDYON.guaniao.client.entity.nightheron.NightHeronModel;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
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

