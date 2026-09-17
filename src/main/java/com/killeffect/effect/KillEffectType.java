package com.killeffect.effect;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * All available kill effects. `icon` is the item shown as its button in the GUI.
 */
public enum KillEffectType {
    LIGHTNING("effect.killeffect.lightning", Items.TRIDENT),
    TNT("effect.killeffect.tnt", Items.TNT),
    TOTEM("effect.killeffect.totem", Items.TOTEM_OF_UNDYING),
    FIREWORK("effect.killeffect.firework", Items.FIREWORK_ROCKET),
    ANVIL("effect.killeffect.anvil", Items.ANVIL);

    public final String translationKey;
    public final Item icon;

    KillEffectType(String translationKey, Item icon) {
        this.translationKey = translationKey;
        this.icon = icon;
    }
}
