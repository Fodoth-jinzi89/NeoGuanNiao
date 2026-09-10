package net.fodoth.skina.neoguanniao.compat.jade;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin("neoguanniao")
public final class NeoGuanNiaoFabricJadePlugin implements IWailaPlugin, IBlockComponentProvider {
    private static final NeoGuanNiaoFabricJadePlugin PROVIDER = new NeoGuanNiaoFabricJadePlugin();

    @Override public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("neoguanniao", "cage");
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
            // 直接同步笼中实体的原始 NBT，客户端复用 BirdCageItem 的提示格式化逻辑。
            ListTag birds = new ListTag();
            for (CompoundTag bird : info.cage.capturedBirds()) birds.add(bird.copy());
            if (!birds.isEmpty()) data.put("birds", birds);
        }
        @Override public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath("neoguanniao", "cage_data");
        }
    }

    private record BlockEntityInfo(BirdCageBlockEntity cage, BlockPos pos) {
    }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        ListTag birds = accessor.getServerData().getList("birds", Tag.TAG_COMPOUND);
        List<CompoundTag> list = new ArrayList<>(birds.size());
        for (int i = 0; i < birds.size(); i++) list.add(birds.getCompound(i));
        tooltip.addAll(BirdCageItem.birdLines(list));
    }
}
