package keletu.guaniao.client.entity.sparrow;

import keletu.guaniao.content.bird.sparrow.SparrowDefinition;
import keletu.guaniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SparrowModel extends GeoModel<SparrowEntity> {
    @Override
    public ResourceLocation getModelResource(SparrowEntity animatable) {
        return SparrowDefinition.MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SparrowEntity animatable) {
        return SparrowDefinition.TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SparrowEntity animatable) {
        return SparrowDefinition.ANIMATION;
    }
}
