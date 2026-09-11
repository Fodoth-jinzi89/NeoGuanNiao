package net.fodoth.skina.neoguanniao.compat.jei;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiShapedRecipeBuilder;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.compat.PhotoRecipeDisplays;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** 把相机的自定义合成挂到 JEI 的合成分类里展示（Fabric 端由 fabric.mod.json 的 jei_mod_plugin 入口加载）。 */
@JeiPlugin
public final class NeoGuanNiaoFabricJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = NeoGuanNiao.resource("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (PhotoRecipeDisplays.Display display : PhotoRecipeDisplays.all()) {
            IJeiShapedRecipeBuilder builder = registration.getVanillaRecipeFactory()
                    .createShapedRecipeBuilder(CraftingBookCategory.MISC, List.of(display.output()));
            List<List<ItemStack>> inputs = display.inputs();
            for (int slot = 0; slot < inputs.size(); slot++) {
                builder.define((char)('A' + slot), Ingredient.of(inputs.get(slot).toArray(ItemStack[]::new)));
            }
            for (String row : display.patternRows()) {
                builder.pattern(row);
            }
            recipes.add(new RecipeHolder<>(display.id(), builder.build()));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
    }
}
