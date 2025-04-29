package vazkii.quark.client.feature;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.quark.base.module.Feature;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.util.ItemMetaHelper;

import java.util.HashMap;
import java.util.Map;

public class GlowingItems extends Feature {

    public static double maxDistance;
    public static final Map<ResourceLocation, TIntSet> glowingItems = new HashMap<>();

    @Override
    public void setupConfig() {
        glowingItems.clear();
        String[] tempGlowingItems = loadPropStringList("Glowing Items",
                "Items that should glow when near the player\nFormat is modid:item[:meta]",
                new String[0]);
        for (ItemStack stack : ItemMetaHelper.getFromStringArray("glowing item", tempGlowingItems)) {
            glowingItems.computeIfAbsent(stack.getItem().getRegistryName(), k -> new TIntHashSet())
                    .add(stack.getMetadata());
        }
        maxDistance = loadPropDouble("Maximum Distance", "The maximum distance from the player at which items will glow. Default is 32 blocks", 32);
    }

    @SideOnly(Side.CLIENT)
    public static boolean isOutlineActive(Entity entity, Entity viewer) {
        if (!(entity instanceof EntityItem) || !ModuleLoader.isFeatureEnabled(GlowingItems.class)) {
            return false;
        }

        ItemStack stack = ((EntityItem) entity).getItem();
        if (stack.isEmpty()) return false;

        TIntSet metas = glowingItems.get(stack.getItem().getRegistryName());
        return metas != null && metas.contains(stack.getMetadata())
                && viewer.getDistanceSq(entity) <= maxDistance * maxDistance;
    }

}
