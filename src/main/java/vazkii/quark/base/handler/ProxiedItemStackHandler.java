package vazkii.quark.base.handler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 4:27 PM on 12/15/18.
 */
public class ProxiedItemStackHandler implements IItemHandlerModifiable, ICapabilityProvider {
	protected final ItemStack stack;
	protected final String key;
	protected final int size;

	public ProxiedItemStackHandler(ItemStack stack) {
		this(stack, "Inventory", 1);
	}

	public ProxiedItemStackHandler(ItemStack stack, String key) {
		this(stack, key, 1);
	}

	public ProxiedItemStackHandler(ItemStack stack, int size) {
		this(stack, "Inventory", size);
	}

	public ProxiedItemStackHandler(ItemStack stack, String key, int size) {
		this.stack = stack;
		this.key = key;
		this.size = size;
	}

	private NBTTagList getStackList() {
		NBTTagList list = ItemNBTHelper.getList(stack, key, Constants.NBT.TAG_COMPOUND, true);
		if (list == null)
			ItemNBTHelper.setList(stack, key, list = new NBTTagList());

		while (list.tagCount() < size)
			list.appendTag(new NBTTagCompound());

		return list;
	}

	private void writeStack(int index, @Nonnull ItemStack stack) {
		getStackList().set(index, stack.serializeNBT());
		onContentsChanged(index);
	}

	private ItemStack readStack(int index) {
		return new ItemStack(getStackList().getCompoundTagAt(index));
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);
		writeStack(slot, stack);
	}

	@Override
	public int getSlots() {
		return size;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return readStack(slot);
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		validateSlotIndex(slot);
		if (stack.isEmpty()) return stack;

		ItemStack existing = readStack(slot);
		if (!existing.isEmpty() && !ItemHandlerHelper.canItemStacksStack(existing, stack)) return stack;

		int toTransfer = Math.min(stack.getCount(), getStackLimit(slot, existing) - existing.getCount());
		if (toTransfer <= 0) return stack;
		int remainingCount = stack.getCount() - toTransfer;

		if (simulate) { // awkward, but avoids copying the stack if we don't need to
			if (remainingCount <= 0) return ItemStack.EMPTY;
			stack = stack.copy();
		} else {
			stack = stack.copy();
			stack.setCount(existing.getCount() + toTransfer);
			writeStack(slot, stack); // writes a copy
			if (remainingCount <= 0) return ItemStack.EMPTY;
		}
		stack.setCount(remainingCount); // if we reach this line, the stack must have been copied
		return stack;
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		validateSlotIndex(slot);
		if (amount <= 0) return ItemStack.EMPTY;

		ItemStack existing = readStack(slot); // reads a copy
		if (existing.isEmpty()) return ItemStack.EMPTY;

		if (amount >= existing.getCount()) {
			if (!simulate) writeStack(slot, ItemStack.EMPTY);
		} else {
			if (!simulate) {
				existing.setCount(existing.getCount() - amount);
				writeStack(slot, existing); // writes a copy
			}
			existing.setCount(amount);
		}
		return existing;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return true;
	}

	protected void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= size)
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
	}

	@SuppressWarnings({"EmptyMethod"})
	protected void onContentsChanged(int slot) {
		// NO-OP
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ?
				CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this) : null;
	}
}
