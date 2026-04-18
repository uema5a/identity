package draylar.identity.screen;

import draylar.identity.Identity;
import draylar.identity.api.PlayerFavorites;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.PlayerUnlocks;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.mixin.accessor.ScreenAccessor;
import draylar.identity.network.impl.SwapPackets;
import draylar.identity.screen.widget.EntityWidget;
import draylar.identity.screen.widget.HelpWidget;
import draylar.identity.screen.widget.PlayerWidget;
import draylar.identity.screen.widget.SearchWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityScreen extends Screen {

    private final List<IdentityType<?>> unlocked = new ArrayList<>();
    private final Map<IdentityType<?>, LivingEntity> renderEntities = new LinkedHashMap<>();
    private final List<EntityWidget> entityWidgets = new ArrayList<>();
    private SearchWidget searchBar;
    private PlayerWidget playerButton;
    private Button helpButton;
    private Checkbox babyCheckbox;
    private String lastSearchContents = "";

    public IdentityScreen() {
        super(Component.literal(""));
    }

    @Override
    protected void init() {
        super.init();

        // don't initialize if the player is null
        if (minecraft.player == null) {
            minecraft.setScreen(null);
            return;
        }

        searchBar = createSearchBar();
        playerButton = createPlayerButton();
        helpButton = createHelpButton();
        babyCheckbox = createBabyCheckbox();

        populateRenderEntities();
        addRenderableWidget(searchBar);
        addRenderableWidget(playerButton);
        addRenderableWidget(helpButton);
        addRenderableWidget(babyCheckbox);

        // collect unlocked entities
        unlocked.clear();
        unlocked.addAll(collectUnlockedEntities(minecraft.player));

        // Some users were experiencing a crash with this sort method, so we catch potential errors here
        // https://github.com/Draylar/identity/issues/87
        try {
            unlocked.sort((first, second) -> PlayerFavorites.has(minecraft.player, first) ? -1 : 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        entityWidgets.clear();
        populateEntityWidgets(minecraft.player, unlocked);

        searchBar.setResponder(text -> {
            setFocused(searchBar);

            // Only re-filter if the text contents changed
            if (!lastSearchContents.equals(text)) {
                ((ScreenAccessor) this).getSelectables().removeIf(button -> button instanceof EntityWidget);
                entityWidgets.clear();

                List<IdentityType<?>> filtered = unlocked
                        .stream()
                        .filter(type -> text.isEmpty() || type.getEntityType().getDescriptionId().contains(text))
                        .collect(Collectors.toList());

                populateEntityWidgets(minecraft.player, filtered);
            }

            lastSearchContents = text;
        });
    }

    @Override
    protected void clearWidgets() {
        // intentionally empty to prevent re-initialization from clearing state
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        // Don't call extractBackground - it uses blur which can only be called once per frame.
        // Instead render a semi-transparent dark overlay since this screen overlays gameplay.
        extractor.fill(0, 0, this.width, this.height, 0x80000000);

        // Render background hint when no identities have been collected
        if (unlocked.isEmpty()) {
            Component message = Component.translatable("identity.menu_hint");
            float xPosition = (getWindow().getGuiScaledWidth() / 2f) - (font.width(message) / 2f);
            float yPosition = (getWindow().getGuiScaledHeight() / 2f);
            extractor.text(font, message, (int) xPosition, (int) yPosition, 0xFFFFFF, true);
        }

        searchBar.extractRenderState(extractor, mouseX, mouseY, delta);
        playerButton.extractRenderState(extractor, mouseX, mouseY, delta);
        helpButton.extractRenderState(extractor, mouseX, mouseY, delta);
        if (babyCheckbox != null) babyCheckbox.extractRenderState(extractor, mouseX, mouseY, delta);
        renderEntityWidgets(extractor, mouseX, mouseY, delta);
    }

    public void renderEntityWidgets(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        int top = 35;

        extractor.pose().pushMatrix();
        extractor.enableScissor(0, 0, width, this.height - top);
        entityWidgets.forEach(widget -> widget.extractRenderState(extractor, mouseX, mouseY, delta));
        extractor.disableScissor();
        extractor.pose().popMatrix();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!entityWidgets.isEmpty()) {
            float firstPos = entityWidgets.get(0).getY();

            // Top section should always have mobs, prevent scrolling the entire list down the screen
            if (scrollY == 1 && firstPos >= 35) {
                return false;
            }

            entityWidgets.forEach(widget -> widget.setY((int) (widget.getY() + scrollY * 10)));
        }

        return false;
    }

    private void populateEntityWidgets(LocalPlayer player, List<IdentityType<?>> unlocked) {
        int x = 15;
        int y = 35;
        int rows = (int) Math.ceil(unlocked.size() / 7f);

        IdentityType<LivingEntity> currentType = IdentityType.from(PlayerIdentity.getIdentity(player));

        for (int yIndex = 0; yIndex <= rows; yIndex++) {
            for (int xIndex = 0; xIndex < 7; xIndex++) {
                int listIndex = yIndex * 7 + xIndex;

                if (listIndex < unlocked.size()) {
                    IdentityType<?> type = unlocked.get(listIndex);
                    boolean isCurrent = currentType != null && currentType.equals(type);

                    EntityWidget entityWidget = new EntityWidget(
                            (getWindow().getGuiScaledWidth() - 27) / 7f * xIndex + x,
                            getWindow().getGuiScaledHeight() / 5f * yIndex + y,
                            (getWindow().getGuiScaledWidth() - 27) / 7f,
                            getWindow().getGuiScaledHeight() / 5f,
                            type,
                            renderEntities.get(type),
                            this,
                            PlayerFavorites.has(player, type),
                            isCurrent
                    );

                    addRenderableWidget(entityWidget);
                    entityWidgets.add(entityWidget);
                }
            }
        }
    }

    private void populateRenderEntities() {
        if (renderEntities.isEmpty()) {
            var level = minecraft.level;
            List<IdentityType<?>> types = IdentityType.getAllTypes(level);
            for (IdentityType<?> type : types) {
                Entity entity = type.create(level);
                if (entity instanceof LivingEntity living) {
                    renderEntities.put(type, living);
                }
            }

            Identity.LOGGER.info(String.format("Loaded %d entities for rendering", types.size()));
        }
    }

    private List<IdentityType<?>> collectUnlockedEntities(LocalPlayer player) {
        List<IdentityType<?>> unlocked = new ArrayList<>();

        // collect current unlocked identities (or allow all for creative users)
        renderEntities.forEach((type, instance) -> {
            if (PlayerUnlocks.has(player, type) || player.isCreative()) {
                unlocked.add(type);
            }
        });

        return unlocked;
    }

    private SearchWidget createSearchBar() {
        return new SearchWidget(
                getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 5,
                5,
                getWindow().getGuiScaledWidth() / 4f,
                20f);
    }

    private PlayerWidget createPlayerButton() {
        return new PlayerWidget(
                getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 5,
                7,
                15,
                15,
                this);
    }

    private Button createHelpButton() {
        return new HelpWidget(
                (int) (getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 5) - 30,
                5,
                20,
                20);
    }

    private Checkbox createBabyCheckbox() {
        Window window = getWindow();
        int x = (int) (window.getGuiScaledWidth() / 2f) + 80;
        int y = 5;
        return Checkbox.builder(Component.translatable("identity.baby"), Minecraft.getInstance().font)
                .pos(x, y)
                .onValueChange((checkbox, selected) -> {
                    // Immediately re-apply current identity with new baby state
                    if (minecraft.player != null) {
                        IdentityType<?> currentType = PlayerIdentity.getIdentityType(minecraft.player);
                        if (currentType != null) {
                            SwapPackets.sendSwapRequest(currentType, selected);
                        }
                    }
                })
                .build();
    }

    public boolean isBabyMode() {
        return babyCheckbox != null && babyCheckbox.selected();
    }

    public Window getWindow() {
        return Minecraft.getInstance().getWindow();
    }

    public void disableAll() {
        entityWidgets.forEach(button -> button.setSelected(false));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean forwarded) {
        if (event.y() < 35) {
            return searchBar.mouseClicked(event, forwarded)
                    || playerButton.mouseClicked(event, forwarded)
                    || helpButton.mouseClicked(event, forwarded)
                    || (babyCheckbox != null && babyCheckbox.mouseClicked(event, forwarded));
        } else {
            return super.mouseClicked(event, forwarded);
        }
    }
}
