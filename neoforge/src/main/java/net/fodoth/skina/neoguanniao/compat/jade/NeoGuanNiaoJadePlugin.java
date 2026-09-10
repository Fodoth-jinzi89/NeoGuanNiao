package net.fodoth.skina.neoguanniao.compat.jade;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("neoguanniao")
public final class NeoGuanNiaoJadePlugin implements IWailaPlugin, IBlockComponentProvider {
    private static final NeoGuanNiaoJadePlugin PROVIDER = new NeoGuanNiaoJadePlugin();

    @Override public net.minecraft.resources.ResourceLocation getUid() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoguanniao", "cage");
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ServerData(), BirdCageBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PROVIDER, BirdCageBlock.class);
    }

    private static BlockEntityInfo findCage(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof BirdCageBlockEntity cage && !cage.isEmpty()) {
            return new BlockEntityInfo(cage, cage.getBlockPos());
        }
        BlockPos pos = accessor.getPosition();
        BlockState state = accessor.getBlockState();
        if (!(state.getBlock() instanceof BirdCageBlock block) || !state.getValue(BirdCageBlock.PART)) return null;
        int height = block.variant().ordinal() == 1 ? 3 : 4;
        for (int x = 0; x < 3; x++) for (int y = 0; y < height; y++) for (int z = 0; z < 3; z++) {
            BlockPos origin = pos.offset(-x, -y, -z);
            if (accessor.getLevel().getBlockEntity(origin) instanceof BirdCageBlockEntity cage && !cage.isEmpty())
                return new BlockEntityInfo(cage, origin);
        }
        return null;
    }

    private static final class ServerData implements IServerDataProvider<BlockAccessor> {
        @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntityInfo info = findCage(accessor);
            if (info == null) return;
            CompoundTag bird = info.cage.lastCapturedBird();
            if (bird == null) return;
            data.putString("id", bird.getString("id"));
            data.putString("name", bird.contains("CustomName") ? bird.getString("CustomName") : "");
            data.putFloat("health", bird.contains("Health") ? bird.getFloat("Health") : 0);
            data.putFloat("maxHealth", bird.contains("MaxHealth") ? bird.getFloat("MaxHealth") : data.getFloat("health"));
        }
        @Override public net.minecraft.resources.ResourceLocation getUid() {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoguanniao", "cage_data");
        }
    }

    private record BlockEntityInfo(BirdCageBlockEntity cage, BlockPos pos) {
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("id")) return;
        String id = data.getString("id");
        Component name = Component.translatable("entity." + id.replace(':', '.'));
        if (!data.getString("name").isEmpty()) {
            try { name = Component.Serializer.fromJson(data.getString("name"), accessor.getLevel().registryAccess()); }
            catch (Exception ignored) { }
        }
        if (name != null) {
            tooltip.add(Component.translatable("item.neoguanniao.bird_cage.contains").append(": ").append(name));
        }
        tooltip.add(Component.translatable("item.neoguanniao.bird_cage.registry").append(": ").append(Component.literal(id)));
        tooltip.add(Component.translatable("item.neoguanniao.bird_cage.health").append(": ")
                .append(Component.literal(format(data.getFloat("health")) + "/" + format(data.getFloat("maxHealth")))));
    }

    private static String format(float value) { return value == (int) value ? Integer.toString((int) value) : String.format(java.util.Locale.ROOT, "%.1f", value); }
}
