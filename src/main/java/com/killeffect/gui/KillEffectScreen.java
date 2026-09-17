package com.killeffect.gui;

import com.killeffect.config.KillEffectConfig;
import com.killeffect.effect.KillEffectType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class KillEffectScreen extends Screen {
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_GAP = 14;

    private final List<int[]> slotBounds = new ArrayList<>();
    private final KillEffectConfig config = KillEffectConfig.get();
    private SpeedSlider speedSlider;

    public KillEffectScreen() {
        super(Text.literal("اختيار تأثير القتل"));
    }

    @Override
    protected void init() {
        slotBounds.clear();
        KillEffectType[] types = KillEffectType.values();
        int totalWidth = types.length * SLOT_SIZE + (types.length - 1) * SLOT_GAP;
        int startX = this.width / 2 - totalWidth / 2;
        int slotY = this.height / 2 - 46;

        for (int i = 0; i < types.length; i++) {
            int x = startX + i * (SLOT_SIZE + SLOT_GAP);
            slotBounds.add(new int[]{x, slotY, SLOT_SIZE, SLOT_SIZE});
        }

        rebuildSpeedSlider();

        this.addDrawableChild(ButtonWidget.builder(Text.literal("إغلاق"), btn -> this.close())
                .dimensions(this.width / 2 - 40, this.height / 2 + 66, 80, 20)
                .build());
    }

    private void rebuildSpeedSlider() {
        if (speedSlider != null) {
            this.remove(speedSlider);
        }
        speedSlider = new SpeedSlider(
                this.width / 2 - 110, this.height / 2 + 28, 220, 20,
                config.getSpeed(config.selectedEffect),
                newSpeed -> config.setSpeed(config.selectedEffect, newSpeed)
        );
        this.addDrawableChild(speedSlider);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, this.height / 2 - 80, 0xFFFFFF);

        KillEffectType[] types = KillEffectType.values();
        for (int i = 0; i < types.length; i++) {
            int[] b = slotBounds.get(i);
            boolean selected = types[i] == config.selectedEffect;
            boolean hovered = isHovering(b, mouseX, mouseY);

            int borderColor = selected ? 0xFF7CFC7C : (hovered ? 0xFFAAAAAA : 0xFF000000);
            int bgColor = selected ? 0xFF3B7A3B : (hovered ? 0xFF4A4A4A : 0xFF2B2B2B);

            context.fill(b[0] - 3, b[1] - 3, b[0] + b[2] + 3, b[1] + b[3] + 3, borderColor);
            context.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], bgColor);

            ItemStack stack = new ItemStack(types[i].icon);
            int itemX = b[0] + (b[2] - 16) / 2;
            int itemY = b[1] + (b[3] - 16) / 2;
            context.drawItem(stack, itemX, itemY);

            Text label = Text.translatable(types[i].translationKey);
            int labelWidth = this.textRenderer.getWidth(label);
            context.drawTextWithShadow(this.textRenderer, label,
                    b[0] + b[2] / 2 - labelWidth / 2, b[1] + b[3] + 6,
                    selected ? 0xFFFFFF55 : 0xFFCCCCCC);
        }

        Text speedLabel = Text.literal("سرعة التأثير المختار");
        context.drawCenteredTextWithShadow(this.textRenderer, speedLabel, this.width / 2, this.height / 2 + 14, 0xFFAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean isHovering(int[] bounds, double mouseX, double mouseY) {
        return mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2]
                && mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        KillEffectType[] types = KillEffectType.values();
        for (int i = 0; i < slotBounds.size(); i++) {
            if (isHovering(slotBounds.get(i), mouseX, mouseY)) {
                // Selecting a new effect replaces the previous one (radio-button style).
                config.setSelected(types[i]);
                if (this.client != null) {
                    this.client.getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
                rebuildSpeedSlider();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
