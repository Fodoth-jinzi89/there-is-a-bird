package EdDYON.guaniao.registry;

import EdDYON.guaniao.content.camera.FilmToPhotographRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuaniaoRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "guaniao");
    public static final RegistryObject<RecipeSerializer<FilmToPhotographRecipe>> FILM_TO_PHOTOGRAPH = RECIPE_SERIALIZERS.register("film_to_photograph",
            () -> new SimpleCraftingRecipeSerializer<>(FilmToPhotographRecipe::new));

    private GuaniaoRecipeSerializers() {
    }
}
