package com.rics.ronrockets.client;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.mixin.MinimapAccessMixin;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

/**
 * Intercepts minimap right-clicks to support building ability targeting
 * (e.g., Launch Rocket's ATTACK_GROUND). Fires at HIGH priority before
 * the RoN minimap handler, so we can send the building command and cancel
 * the event if a building ability targeting cursor is active.
 */
@Mod.EventBusSubscriber(modid = RonRocketsMod.MODID, value = Dist.CLIENT,
    bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MinimapTargetHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseClickPre(ScreenEvent.MouseButtonPressed.Pre evt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!OrthoviewClientEvents.isEnabled()) return;
        if (OrthoviewClientEvents.isCameraLocked()) return;
        if (!(mc.screen instanceof com.solegendary.reignofnether.guiscreen.TopdownGui)) return;

        // Only intercept right-click
        if (evt.getButton() != GLFW.GLFW_MOUSE_BUTTON_2) return;

        // Check if a building ability targeting cursor is active
        UnitAction leftAction = CursorClientEvents.getLeftClickAction();
        if (leftAction != UnitAction.ATTACK_GROUND) return;

        // Get the selected building
        BuildingPlacement selectedBuilding = HudClientEvents.hudSelectedPlacement;
        if (selectedBuilding == null) return;

        // Convert minimap click position to world coordinates via accessor mixin
        BlockPos targetPos = MinimapAccessMixin.ronrockets$getWorldPosOnMinimap(
            (float) evt.getMouseX(), (float) evt.getMouseY(), false);

        if (targetPos == null) return;

        // Send the building ability command via the standard RoN flow
        // This runs both client-side (immediate UI feedback) and server-side (actual logic)
        UnitClientEvents.sendUnitCommandManual(
            leftAction,
            -1,
            new int[0],
            targetPos,
            selectedBuilding.originPos
        );

        // Clear the cursor action
        CursorClientEvents.setLeftClickAction(null);

        // Cancel so RoN's minimap handler doesn't also fire a MOVE command
        evt.setCanceled(true);
    }
}
