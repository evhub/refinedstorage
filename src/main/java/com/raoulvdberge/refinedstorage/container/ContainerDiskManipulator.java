package com.raoulvdberge.refinedstorage.container;

import com.raoulvdberge.refinedstorage.RSContainers;
import com.raoulvdberge.refinedstorage.container.slot.filter.SlotFilter;
import com.raoulvdberge.refinedstorage.container.slot.filter.SlotFilterFluid;
import com.raoulvdberge.refinedstorage.tile.TileDiskManipulator;
import com.raoulvdberge.refinedstorage.tile.config.IType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDiskManipulator extends ContainerBase {
    public ContainerDiskManipulator(TileDiskManipulator diskManipulator, PlayerEntity player, int windowId) {
        super(RSContainers.DISK_MANIPULATOR, diskManipulator, player, windowId);

        for (int i = 0; i < 4; ++i) {
            addSlot(new SlotItemHandler(diskManipulator.getNode().getUpgrades(), i, 187, 6 + (i * 18)));
        }

        for (int i = 0; i < 3; ++i) {
            addSlot(new SlotItemHandler(diskManipulator.getNode().getInputDisks(), i, 44, 57 + (i * 18)));
        }

        for (int i = 0; i < 3; ++i) {
            addSlot(new SlotItemHandler(diskManipulator.getNode().getOutputDisks(), i, 116, 57 + (i * 18)));
        }

        for (int i = 0; i < 9; ++i) {
            addSlot(new SlotFilter(diskManipulator.getNode().getItemFilters(), i, 8 + (18 * i), 20).setEnableHandler(() -> diskManipulator.getNode().getType() == IType.ITEMS));
        }

        for (int i = 0; i < 9; ++i) {
            addSlot(new SlotFilterFluid(diskManipulator.getNode().getFluidFilters(), i, 8 + (18 * i), 20).setEnableHandler(() -> diskManipulator.getNode().getType() == IType.FLUIDS));
        }

        addPlayerInventory(8, 129);

        transferManager.addBiTransfer(player.inventory, diskManipulator.getNode().getUpgrades());
        transferManager.addBiTransfer(player.inventory, diskManipulator.getNode().getInputDisks());
        transferManager.addTransfer(diskManipulator.getNode().getOutputDisks(), player.inventory);
        transferManager.addFilterTransfer(player.inventory, diskManipulator.getNode().getItemFilters(), diskManipulator.getNode().getFluidFilters(), diskManipulator.getNode()::getType);
    }
}
