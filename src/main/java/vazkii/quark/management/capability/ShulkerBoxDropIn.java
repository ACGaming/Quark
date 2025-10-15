package vazkii.quark.management.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.arl.util.AbstractDropIn;
import vazkii.arl.util.ItemNBTHelper;

public class ShulkerBoxDropIn extends AbstractDropIn implements ICapabilityProvider {

	@Override
	public boolean canDropItemIn(EntityPlayer player, ItemStack stack, ItemStack incoming) {
		return tryAddToShulkerBox(stack, incoming, true);
	}

	@Override
	public ItemStack dropItemIn(EntityPlayer player, ItemStack stack, ItemStack incoming) {
		tryAddToShulkerBox(stack, incoming, false);
		return stack;
	}

	private boolean tryAddToShulkerBox(ItemStack shulkerBox, ItemStack stack, boolean simulate) {
		if (stack.getItem() instanceof ItemShulkerBox || shulkerBox.getCount() > 1)
			return false;

		NBTTagCompound stackTag = shulkerBox.getTagCompound();
		NBTTagCompound blockEntityTag = (stackTag != null && stackTag.hasKey("BlockEntityTag"))
			? stackTag.getCompoundTag("BlockEntityTag")
			: new NBTTagCompound();

		// Use a generous upper bound to avoid truncation
		ItemStackHandler handler = new ItemStackHandler(128);
		handler.deserializeNBT(blockEntityTag);

		ItemStack result = ItemHandlerHelper.insertItem(handler, stack, simulate);
		boolean did = result.isEmpty();

		if (!simulate && did) {
			NBTTagCompound newTag = handler.serializeNBT();
			ItemNBTHelper.setCompound(shulkerBox, "BlockEntityTag", newTag);
		}

		return did;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == AbstractDropIn.DROP_IN_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == AbstractDropIn.DROP_IN_CAPABILITY ? (T) this : null;
	}
}
