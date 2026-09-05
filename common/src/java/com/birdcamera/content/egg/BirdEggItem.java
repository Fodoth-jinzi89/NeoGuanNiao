package com.birdcamera.content.egg;

import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.model.BirdModelRarity;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import com.birdcamera.registry.BirdCameraBirdModels;
import com.birdcamera.registry.BirdCameraBirdSkins;
import com.birdcamera.registry.BirdCameraDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BirdEggItem extends Item {
   public BirdEggItem(Properties properties) {
      super(properties);
   }

   public static void setEggData(ItemStack stack, BirdEggData data) {
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_EGG_DATA, data);
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_EGG_RARITY, BirdCameraBirdSkins.get(data.skin()).rarity().getRarity());
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_EGG_MODEL_RARITY, BirdCameraBirdModels.get(data.model()).rarity().getRarity());
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_EGG_GENDER, data.gender() ? 1 : 0);
   }

   public static BirdEggData getEggData(ItemStack stack) {
      return (BirdEggData)stack.get((DataComponentType)BirdCameraDataComponents.BIRD_EGG_DATA);
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      BirdEggData data = getEggData(stack);
      if (data == null) {
         tooltip.add(Component.translatable("tooltip.birdcamera.empty_egg"));
      } else {
         tooltip.add(Component.translatable("tooltip.birdcamera.bird_type").append(translateResource("entity", data.birdType(), data.birdType())));
         int rarityId1 = (Integer)stack.getOrDefault(
            (DataComponentType)BirdCameraDataComponents.BIRD_EGG_MODEL_RARITY, BirdModelRarity.COMMON.getRarity()
         );
         BirdModelRarity rarity1 = BirdModelRarity.byRarity(rarityId1);
         String modelId = data.model().getPath();
         modelId = modelId.replaceAll("_(male|female)$", "");
         ResourceLocation cleanedModelId = ResourceLocation.fromNamespaceAndPath(data.model().getNamespace(), modelId);
         Component modelText = ((MutableComponent)translateResource("model", cleanedModelId, data.birdType())).withStyle(rarity1.getChatColor());
         tooltip.add(Component.translatable("tooltip.birdcamera.model").append(modelText));
         int rarityId = (Integer)stack.getOrDefault((DataComponentType)BirdCameraDataComponents.BIRD_EGG_RARITY, BirdSkinRarity.COMMON.getRarity());
         BirdSkinRarity rarity = BirdSkinRarity.byRarity(rarityId);
         String skinId = data.skin().getPath();
         skinId = skinId.replaceAll("_(male|female)$", "");
         ResourceLocation cleanedSkinId = ResourceLocation.fromNamespaceAndPath(data.skin().getNamespace(), skinId);
         Component skinText = ((MutableComponent)translateResource("skin", cleanedSkinId, data.birdType())).withStyle(rarity.getChatColor());
         tooltip.add(Component.translatable("tooltip.birdcamera.skin").append(skinText));
         tooltip.add(Component.translatable(data.gender() ? "tooltip.birdcamera.male" : "tooltip.birdcamera.female"));
         tooltip.add(Component.translatable("tooltip.birdcamera.egg_count", new Object[]{data.eggCount()}));
         tooltip.add(Component.translatable("tooltip.birdcamera.feather_count", new Object[]{data.featherCount()}));
         tooltip.add(Component.translatable("tooltip.birdcamera.feather_interval").append(formatTime(data.featherInterval())));
         tooltip.add(Component.translatable("tooltip.birdcamera.size", new Object[]{String.format("%.4f", data.size())}));
         tooltip.add(Component.translatable("tooltip.birdcamera.hatch_time").append(formatTime(data.hatchTime())));
         tooltip.add(Component.translatable(data.alive() ? "tooltip.birdcamera.alive" : "tooltip.birdcamera.dead"));
      }
   }

   private static Component translateResource(String prefix, ResourceLocation id, ResourceLocation birdType) {
      String path = id.getPath();
      path = path.substring(path.lastIndexOf(47) + 1);
      if (path.endsWith(".geo.json")) {
         path = path.substring(0, path.length() - ".geo.json".length());
      } else if (path.endsWith(".png")) {
         path = path.substring(0, path.length() - ".png".length());
      }

      String entityName = birdType.getPath();
      return "entity".equals(prefix)
         ? Component.translatable(prefix + "." + id.getNamespace() + "." + entityName)
         : Component.translatable(prefix + "." + id.getNamespace() + "." + entityName + "." + path);
   }

   @NotNull
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null && player.getAbilities().instabuild) {
         Level level = context.getLevel();
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            ItemStack stack = context.getItemInHand();
            BirdEggData data = getEggData(stack);
            if (data != null && data.alive()) {
               EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(data.birdType());
               if (type.create(level) instanceof AbstractBirdEntity<?> bird) {
                  BlockPos var9 = context.getClickedPos().relative(context.getClickedFace());
                  bird.moveTo((double)var9.getX() + 0.5, (double)var9.getY(), (double)var9.getZ() + 0.5, context.getRotation(), 0.0F);
                  bird.applyEggData(data);
                  if (player.isShiftKeyDown()) {
                     bird.setAge(-24000);
                  }

                  level.addFreshEntity(bird);
                  return InteractionResult.CONSUME;
               } else {
                  return InteractionResult.FAIL;
               }
            } else {
               return InteractionResult.FAIL;
            }
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   private static Component formatTime(int ticks) {
      int totalSeconds = ticks / 20;
      int hours = totalSeconds / 3600;
      int minutes = totalSeconds % 3600 / 60;
      int seconds = totalSeconds % 60;
      MutableComponent result = Component.empty();
      if (hours > 0) {
         result.append(Component.translatable("tooltip.birdcamera.time.hour", new Object[]{hours}));
      }

      if (minutes > 0) {
         if (!result.getString().isEmpty()) {
            result.append(" ");
         }

         result.append(Component.translatable("tooltip.birdcamera.time.minute", new Object[]{minutes}));
      }

      if (seconds > 0 || result.getString().isEmpty()) {
         if (!result.getString().isEmpty()) {
            result.append(" ");
         }

         result.append(Component.translatable("tooltip.birdcamera.time.second", new Object[]{seconds}));
      }

      return result;
   }

   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      Component name = super.getName(stack);
      BirdEggData data = getEggData(stack);
      if (data == null) {
         return name;
      } else {
         BirdSkinRarity rarity = BirdCameraBirdSkins.get(data.skin()).rarity();
         return name.copy().withStyle(rarity.getChatColor());
      }
   }
}
