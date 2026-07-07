package com.enzo.n2tmine.ic.recipe;

import com.enzo.n2tmine.ic.ModComponents;
import com.enzo.n2tmine.ic.ModIcBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * Receita informe (shapeless), estilo clonagem de mapa:
 *   1 IC Block (com sala) + 1 bloco de ferro  ->  2 IC Blocks apontando pra MESMA sala.
 *
 * Como o roomId (dado customizado) precisa passar do ingrediente pro resultado,
 * isso nao da pra fazer so com JSON -- precisa desta receita em codigo.
 */
public class IcCopyRecipe extends SpecialCraftingRecipe {

    public IcCopyRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int icComRoom = 0;
        int ferro = 0;
        int outros = 0;

        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.isOf(ModIcBlocks.IC_BLOCK.asItem())) {
                // So copia CI que ja tem sala associada.
                if (stack.get(ModComponents.ROOM_ID) == null) return false;
                icComRoom++;
            } else if (stack.isOf(Blocks.IRON_BLOCK.asItem())) {
                ferro++;
            } else {
                outros++;
            }
        }

        return icComRoom == 1 && ferro == 1 && outros == 0;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isOf(ModIcBlocks.IC_BLOCK.asItem())) {
                Integer roomId = stack.get(ModComponents.ROOM_ID);
                if (roomId != null) {
                    // Resultado: 2 copias identicas, mesma sala. Ambos os ingredientes
                    // sao consumidos, entao voce sai com 2 CIs (original + copia).
                    ItemStack result = new ItemStack(ModIcBlocks.IC_BLOCK, 2);
                    result.set(ModComponents.ROOM_ID, roomId);
                    if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                        result.set(DataComponentTypes.CUSTOM_NAME, stack.get(DataComponentTypes.CUSTOM_NAME));
                    }
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.IC_COPY;
    }
}
