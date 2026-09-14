package net.fodoth.skina.neoguanniao.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.List;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.compat.PhotoRecipeDisplays;
import net.minecraft.world.item.ItemStack;

@EmiEntrypoint
public final class NeoGuanNiaoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (PhotoRecipeDisplays.Display display : PhotoRecipeDisplays.all()) {
            List<EmiIngredient> displayInputs = new ArrayList<>(display.inputs().size());
            for (List<ItemStack> slot : display.inputs()) {
                List<EmiStack> stacks = new ArrayList<>(slot.size());
                for (ItemStack stack : slot) {
                    stacks.add(EmiStack.of(stack));
                }
                displayInputs.add(EmiIngredient.of(stacks));
            }
            registry.addRecipe(new EmiCraftingRecipe(displayInputs, EmiStack.of(display.output()), display.id(), true));
        }
    }
}
