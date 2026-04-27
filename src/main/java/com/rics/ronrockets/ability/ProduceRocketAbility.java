package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ProduceRocketAbility extends Ability {

    public static final int MAX_ROCKETS = 2;

    /**
     * Используется как ключ для системы зарядов BuildingPlacement.
     * Каждое здание хранит свой счётчик зарядов по этому ключу.
     * Синглтон безопасен, т.к. charges хранятся в BuildingPlacement, а не здесь.
     */
    public static final ProduceRocketAbility INSTANCE = new ProduceRocketAbility();

    private ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        this.maxCharges = MAX_ROCKETS;
    }
}
