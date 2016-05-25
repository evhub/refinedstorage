package refinedstorage.tile;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import refinedstorage.RefinedStorage;
import refinedstorage.RefinedStorageBlocks;
import refinedstorage.RefinedStorageUtils;
import refinedstorage.block.BlockStorage;
import refinedstorage.block.EnumStorageType;
import refinedstorage.container.ContainerStorage;
import refinedstorage.inventory.BasicItemHandler;
import refinedstorage.network.MessagePriorityUpdate;
import refinedstorage.storage.*;
import refinedstorage.tile.config.ICompareConfig;
import refinedstorage.tile.config.IModeConfig;
import refinedstorage.tile.config.IRedstoneModeConfig;
import refinedstorage.tile.config.ModeConstants;

import java.util.List;

public class TileStorage extends TileMachine implements IStorageProvider, IStorageGui, ICompareConfig, IModeConfig {
    public static final String NBT_STORAGE = "Storage";
    public static final String NBT_PRIORITY = "Priority";
    public static final String NBT_COMPARE = "Compare";
    public static final String NBT_MODE = "Mode";

    private BasicItemHandler filters = new BasicItemHandler(9, this);

    private NBTTagCompound storageTag = NBTStorage.createNBT();

    private StorageBlockStorage storage;

    private int priority = 0;
    private int compare = 0;
    private int mode = ModeConstants.WHITELIST;
    private int stored;

    private EnumStorageType type;

    @Override
    public int getEnergyUsage() {
        return 3;
    }

    @Override
    public void updateMachine() {
        if (storage == null && storageTag != null) {
            storage = new StorageBlockStorage(this);
        }

        if (storage != null && storage.isDirty()) {
            storage.writeToNBT(storageTag);
            storage.markClean();

            markDirty();
        }
    }

    @Override
    public void provide(List<IStorage> storages) {
        if (storage != null) {
            storages.add(storage);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        RefinedStorageUtils.restoreItems(filters, 0, nbt);

        if (nbt.hasKey(NBT_PRIORITY)) {
            priority = nbt.getInteger(NBT_PRIORITY);
        }

        if (nbt.hasKey(NBT_STORAGE)) {
            storageTag = nbt.getCompoundTag(NBT_STORAGE);
        }

        if (nbt.hasKey(NBT_COMPARE)) {
            compare = nbt.getInteger(NBT_COMPARE);
        }

        if (nbt.hasKey(NBT_MODE)) {
            mode = nbt.getInteger(NBT_MODE);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        RefinedStorageUtils.saveItems(filters, 0, nbt);

        nbt.setInteger(NBT_PRIORITY, priority);
        nbt.setTag(NBT_STORAGE, storageTag);
        nbt.setInteger(NBT_COMPARE, compare);
        nbt.setInteger(NBT_MODE, mode);
    }

    public EnumStorageType getType() {
        if (type == null && worldObj.getBlockState(pos).getBlock() == RefinedStorageBlocks.STORAGE) {
            this.type = ((EnumStorageType) worldObj.getBlockState(pos).getValue(BlockStorage.TYPE));
        }

        return type == null ? EnumStorageType.TYPE_1K : type;
    }

    @Override
    public void sendContainerData(ByteBuf buf) {
        super.sendContainerData(buf);

        buf.writeInt(NBTStorage.getStored(storageTag));
        buf.writeInt(priority);
        buf.writeInt(compare);
        buf.writeInt(mode);
    }

    @Override
    public void receiveContainerData(ByteBuf buf) {
        super.receiveContainerData(buf);

        stored = buf.readInt();
        priority = buf.readInt();
        compare = buf.readInt();
        mode = buf.readInt();
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerStorage.class;
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;

        markDirty();
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;

        markDirty();
    }

    @Override
    public String getGuiTitle() {
        return "block.refinedstorage:storage." + getType().getId() + ".name";
    }

    @Override
    public IItemHandler getFilters() {
        return filters;
    }

    @Override
    public IRedstoneModeConfig getRedstoneModeConfig() {
        return this;
    }

    @Override
    public ICompareConfig getCompareConfig() {
        return this;
    }

    @Override
    public IModeConfig getModeConfig() {
        return this;
    }

    @Override
    public void onPriorityChanged(int priority) {
        RefinedStorage.NETWORK.sendToServer(new MessagePriorityUpdate(pos, priority));
    }

    public NBTTagCompound getStorageTag() {
        return storageTag;
    }

    public void setStorageTag(NBTTagCompound storageTag) {
        this.storageTag = storageTag;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;

        markDirty();
    }

    @Override
    public int getStored() {
        return stored;
    }

    @Override
    public int getCapacity() {
        return getType().getCapacity();
    }
}
