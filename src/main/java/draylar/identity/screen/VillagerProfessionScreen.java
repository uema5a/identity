package draylar.identity.screen;

import draylar.identity.network.impl.VillagerProfessionPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Identifier;

public class VillagerProfessionScreen extends Screen {

    private final Identifier professionId;
    private final net.minecraft.util.math.BlockPos pos;
    private final Identifier worldId;
    private final String originalName;
    private final String existingProfessionId;
    private EditBox nameField;
    private Button deleteButton;

    public VillagerProfessionScreen(Identifier professionId, net.minecraft.util.math.BlockPos pos, Identifier worldId, String originalName, String existingProfessionId) {
        super(Component.translatable("identity.profession.title"));
        this.professionId = professionId;
        this.pos = pos;
        this.worldId = worldId;
        this.originalName = originalName;
        this.existingProfessionId = existingProfessionId;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        nameField = new EditBox(font, centerX - 100, centerY - 10, 200, 20, Component.empty());
        if (originalName != null) {
            nameField.setValue(originalName);
        }
        addWidget(nameField);
        addRenderableWidget(Button.builder(Component.translatable("identity.profession.confirm"), button -> {
            VillagerProfessionPackets.sendSetProfession(professionId, nameField.getValue(), false, pos, worldId, originalName);
            onClose();
        }).pos(centerX - 100, centerY + 20).size(98, 20).build());
        deleteButton = Button.builder(Component.translatable("identity.profession.delete"), button -> {
            Minecraft.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    VillagerProfessionPackets.sendSetProfession(professionId, nameField.getValue(), true, pos, worldId, originalName);
                }
                Minecraft.getInstance().setScreen(null);
            }, Component.translatable("identity.profession.delete"), Component.translatable("identity.profession.delete_confirm")));
        }).pos(centerX + 2, centerY + 20).size(98, 20).build();
        deleteButton.active = originalName != null;
        addRenderableWidget(deleteButton);
        setInitialFocus(nameField);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        extractBackground(extractor, mouseX, mouseY, delta);
        super.extractRenderState(extractor, mouseX, mouseY, delta);
        int titleY = height / 2 - 50;
        extractor.centeredText(font, title, width / 2, titleY, 0xFFFFFF, true);
        int infoY = titleY + 15;
        if (originalName != null) {
            extractor.centeredText(font, Component.translatable("identity.profession.current_name", originalName), width / 2, infoY, 0xAAAAAA, true);
            infoY += 12;
            if (existingProfessionId != null && !existingProfessionId.isEmpty()) {
                extractor.centeredText(font, Component.translatable("identity.profession.current_profession", resolveProfessionName(existingProfessionId)), width / 2, infoY, 0xAAAAAA, true);
            }
        } else {
            extractor.centeredText(font, Component.translatable("identity.profession.prompt"), width / 2, infoY, 0xAAAAAA, true);
        }
        nameField.extractRenderState(extractor, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    private Component resolveProfessionName(String professionKey) {
        Identifier id = Identifier.tryParse(professionKey);
        return id != null ? Component.literal(id.toString()) : Component.literal(professionKey);
    }
}
