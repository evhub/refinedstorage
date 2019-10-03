package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.RSTiles;
import com.raoulvdberge.refinedstorage.api.storage.AccessType;
import com.raoulvdberge.refinedstorage.api.storage.disk.IStorageDisk;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.diskdrive.DiskDriveNetworkNode;
import com.raoulvdberge.refinedstorage.render.constants.ConstantsDisk;
import com.raoulvdberge.refinedstorage.tile.config.*;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DiskDriveTile extends NetworkNodeTile<DiskDriveNetworkNode> {
    public static final TileDataParameter<Integer, DiskDriveTile> PRIORITY = IPrioritizable.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> COMPARE = IComparable.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> WHITELIST_BLACKLIST = IWhitelistBlacklist.createParameter();
    public static final TileDataParameter<Integer, DiskDriveTile> TYPE = IType.createParameter();
    public static final TileDataParameter<AccessType, DiskDriveTile> ACCESS_TYPE = IAccessType.createParameter();
    public static final TileDataParameter<Long, DiskDriveTile> STORED = new TileDataParameter<>(RSSerializers.LONG_SERIALIZER, 0L, t -> {
        long stored = 0;

        for (IStorageDisk storage : t.getNode().getItemDisks()) {
            if (storage != null) {
                stored += storage.getStored();
            }
        }

        for (IStorageDisk storage : t.getNode().getFluidDisks()) {
            if (storage != null) {
                stored += storage.getStored();
            }
        }

        return stored;
    });
    public static final TileDataParameter<Long, DiskDriveTile> CAPACITY = new TileDataParameter<>(RSSerializers.LONG_SERIALIZER, 0L, t -> {
        long capacity = 0;

        for (IStorageDisk storage : t.getNode().getItemDisks()) {
            if (storage != null) {
                if (storage.getCapacity() == -1) {
                    return -1L;
                }

                capacity += storage.getCapacity();
            }
        }

        for (IStorageDisk storage : t.getNode().getFluidDisks()) {
            if (storage != null) {
                if (storage.getCapacity() == -1) {
                    return -1L;
                }

                capacity += storage.getCapacity();
            }
        }

        return capacity;
    });

    private static final String NBT_DISK_STATE = "DiskState_%d";

    private Integer[] diskState = new Integer[8];

    public DiskDriveTile() {
        super(RSTiles.DISK_DRIVE);

        dataManager.addWatchedParameter(PRIORITY);
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addWatchedParameter(WHITELIST_BLACKLIST);
        dataManager.addWatchedParameter(TYPE);
        dataManager.addWatchedParameter(ACCESS_TYPE);
        dataManager.addWatchedParameter(STORED);
        dataManager.addWatchedParameter(CAPACITY);

        initDiskState(diskState);
    }

    @Override
    public CompoundNBT writeUpdate(CompoundNBT tag) {
        super.writeUpdate(tag);

        writeDiskState(tag, 8, getNode().canUpdate(), getNode().getItemDisks(), getNode().getFluidDisks());

        return tag;
    }

    @Override
    public void readUpdate(CompoundNBT tag) {
        super.readUpdate(tag);

        readDiskState(tag, diskState);
    }

    public Integer[] getDiskState() {
        return diskState;
    }

    public static void writeDiskState(CompoundNBT tag, int disks, boolean connected, IStorageDisk[] itemStorages, IStorageDisk[] fluidStorages) {
        for (int i = 0; i < disks; ++i) {
            int state = ConstantsDisk.DISK_STATE_NONE;

            if (itemStorages[i] != null || fluidStorages[i] != null) {
                if (!connected) {
                    state = ConstantsDisk.DISK_STATE_DISCONNECTED;
                } else {
                    state = ConstantsDisk.getDiskState(
                        itemStorages[i] != null ? itemStorages[i].getStored() : fluidStorages[i].getStored(),
                        itemStorages[i] != null ? itemStorages[i].getCapacity() : fluidStorages[i].getCapacity()
                    );
                }
            }

            tag.putInt(String.format(NBT_DISK_STATE, i), state);
        }
    }

    public static void readDiskState(CompoundNBT tag, Integer[] diskState) {
        for (int i = 0; i < diskState.length; ++i) {
            diskState[i] = tag.getInt(String.format(NBT_DISK_STATE, i));
        }
    }

    public static void initDiskState(Integer[] diskState) {
        for (int i = 0; i < diskState.length; ++i) {
            diskState[i] = ConstantsDisk.DISK_STATE_NONE;
        }
    }

    /* TODO
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getNode().getDisks());
        }

        return super.getCapability(capability, facing);
    }*/

    @Override
    @Nonnull
    public DiskDriveNetworkNode createNode(World world, BlockPos pos) {
        return new DiskDriveNetworkNode(world, pos);
    }
}
