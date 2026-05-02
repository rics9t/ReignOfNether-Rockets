package com.rics.ronrockets.mixin;

import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin to expose the private getWorldPosOnMinimap method
 * so our MinimapTargetHandler can convert minimap clicks to world positions.
 */
@Mixin(MinimapClientEvents.class)
public interface MinimapAccessMixin {

    @Invoker("getWorldPosOnMinimap")
    static BlockPos ronrockets$getWorldPosOnMinimap(float x, float y, boolean offsetForCamera) {
        throw new AssertionError();
    }
}
