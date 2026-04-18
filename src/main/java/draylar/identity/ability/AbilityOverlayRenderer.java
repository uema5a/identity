package draylar.identity.ability;

import com.mojang.blaze3d.systems.RenderSystem;
import draylar.identity.api.PlayerAbilities;
import draylar.identity.api.PlayerIdentity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityOverlayRenderer {

    private static final int fadingTickRequirement = 0;
    private static int lastCooldown = 0;
    private static int ticksSinceUpdate = 0;
    private static boolean isFading = false;
    private static int fadingProgress = 0;

    public static void register() {
        // Fabric API 0.145+26.1: HudElementRegistry replaces the removed HudRenderCallback
        HudElementRegistry.register(
                Identifier.parse("identity:ability_overlay"),
                AbilityOverlayRenderer::render
        );
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null) {
            return;
        }

        LivingEntity identity = PlayerIdentity.getIdentity(player);

        if (identity == null) {
            return;
        }

        //TODO make this tick less often
        IdentityAbility<? extends LivingEntity> identityAbility = AbilityRegistry.get(identity.getType());

        if (identityAbility == null) {
            return;
        }

        if (client.screen instanceof ChatScreen) {
            return;
        }

        int cd = PlayerAbilities.getCooldown(player);
        int max = identityAbility.getCooldown(identity);
        float cooldownScale = 1 - cd / (float) max;

        // CD has NOT updated since last tick. It is most likely full.
        if (cd == lastCooldown) {
            ticksSinceUpdate++;

            // If the CD has not updated, we are above the requirement, and we are not fading, start fading.
            if (ticksSinceUpdate > fadingTickRequirement && !isFading) {
                isFading = true;
                fadingProgress = 0;
            }
        }

        // CD updated in the last tick, and we are fading. Stop fading.
        else if (ticksSinceUpdate > fadingProgress) {
            ticksSinceUpdate = 0;
            isFading = false;
        }

        // Tick fading
        if (isFading) {
            fadingProgress = Math.min(50, fadingProgress + 1);
        } else {
            fadingProgress = Math.max(0, fadingProgress - 1);
        }

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        double d = client.getWindow().getGuiScale();

        guiGraphics.pose().pushPose();
        if (cooldownScale != 1) {
            RenderSystem.enableScissor(
                    (int) ((double) 0 * d),
                    (int) ((double) 0 * d),
                    (int) ((double) width * d),
                    (int) ((double) height * (.02 + .055 * cooldownScale) * d)); // min is 0.21, max is 0.76. diff = .55
        }

        // ending pop
        if (isFading) {
            float fadeScalar = fadingProgress / 50f; // 0f -> 1f, 0 is start, 1 is end
            float scale = 1f + (float) Math.sin(fadeScalar * 1.5 * Math.PI) - .25f;
            scale = Math.max(scale, 0);
            guiGraphics.pose().scale(scale, scale, scale);
        }

        // TODO: cache ability stack?
        ItemStack stack = new ItemStack(identityAbility.getIcon());
        guiGraphics.renderItem(stack, (int) (width * .95f), (int) (height * .92f));

        RenderSystem.disableScissor();
        guiGraphics.pose().popPose();

        lastCooldown = cd;
    }

    private AbilityOverlayRenderer() {
        // NO-OP
    }
}
