package com.enzo.n2tmine.ic.recipe;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRecipes {

    public static final RecipeSerializer<IcCopyRecipe> IC_COPY = Registry.register(
            Registries.RECIPE_SERIALIZER,
            N2TMine.id("ic_copy"),
            new SpecialRecipeSerializer<>(IcCopyRecipe::new)
    );

    public static void initialize() {
    }
}
