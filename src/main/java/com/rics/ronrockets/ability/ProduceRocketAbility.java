package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.rocket.RocketManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ProduceRocketAbility extends Ability {

    public ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        BlockPos pos = placement.centrePos;
        int stored = RocketManager.storedRockets.getOrDefault(pos, 0);
        int prod = RocketManager.productionTicks.getOrDefault(pos, 0);
        int cool = RocketManager.cooldownTicks.getOrDefault(pos, 0);

        List<FormattedCharSequence> tooltips = new ArrayList<>();
        tooltips.add(FormattedCharSequence.forward("Produce Rocket", Style.EMPTY.withBold(true)));
        tooltips.add(FormattedCharSequence.forward("Stored Rockets: " + stored + "/2", Style.EMPTY));

        if (prod > 0) {
            tooltips.add(FormattedCharSequence.forward("Producing... " + (prod / 20) + "s remaining", Style.EMPTY.withColor(ChatFormatting.YELLOW)));
        } else if (cool > 0) {
            tooltips.add(FormattedCharSequence.forward("Cooldown: " + (cool / 20) + "s remaining", Style.EMPTY.withColor(ChatFormatting.RED)));
        } else if (stored >= 2) {
            tooltips.add(FormattedCharSequence.forward("Storage Full", Style.EMPTY.withColor(ChatFormatting.RED)));
        } else {
            tooltips.add(FormattedCharSequence.forward("Click to produce (Takes 120s)", Style.EMPTY.withColor(ChatFormatting.GREEN)));
            tooltips.add(FormattedCharSequence.forward("Cooldown after: 180s", Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }

        return new AbilityButton(
                "Produce Rocket",
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> stored >= 2 || prod > 0 || cool > 0, // Locks button if maxed, producing, or on cooldown
                () -> stored < 2 && prod <= 0 && cool <= 0,
                () -> this.use(placement.getLevel(), placement, placement.centrePos),
                null,
                tooltips,
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) return;
        BlockPos pos = buildingUsing.centrePos;
        
        if (RocketManager.storedRockets.getOrDefault(pos, 0) < 2 
            && RocketManager.productionTicks.getOrDefault(pos, 0) <= 0 
            && RocketManager.cooldownTicks.getOrDefault(pos, 0) <= 0) {
            
            RocketManager.productionTicks.put(pos, 2400); // ✅ Starts 2 Minute Creation
        }
    }
}
