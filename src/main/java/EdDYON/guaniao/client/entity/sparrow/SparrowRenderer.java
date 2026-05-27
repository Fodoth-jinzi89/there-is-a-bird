package EdDYON.guaniao.client.entity.sparrow;

import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SparrowRenderer extends GeoEntityRenderer<SparrowEntity> {
    public SparrowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, (GeoModel)new SparrowModel());
        this.shadowRadius = 0.16f;
    }
}
