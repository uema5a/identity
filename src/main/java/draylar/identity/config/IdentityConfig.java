package draylar.identity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GSON-backed config replacement for the old omega-config / AutoConfig
 * {@code IdentityFabricConfig}. Field layout is the serialization contract
 * — keep field names stable between versions.
 *
 * The instance methods below mirror the xGabou-era abstract {@code IdentityConfig}
 * so the content layer can be migrated mechanically.
 */
public class IdentityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "identity.json");

    private static IdentityConfig INSTANCE = new IdentityConfig();

    // --- Visual / Behavior ---
    public boolean overlayIdentityUnlocks = true;
    public boolean overlayIdentityRevokes = true;
    public boolean revokeIdentityOnDeath = false;
    public boolean identitiesEquipItems = true;
    public boolean identitiesEquipArmor = true;
    public boolean useIdentitySounds = true;
    public boolean playAmbientSounds = true;
    public boolean hearSelfAmbient = false;
    public boolean showPlayerNametag = false;
    public boolean renderOwnNametag = false;
    public boolean forceChangeNew = false;
    public boolean forceChangeAlways = false;
    public boolean logCommands = true;

    // --- Mob Interactions ---
    public boolean hostilesIgnoreHostileIdentityPlayer = true;
    public boolean hostilesForgetNewHostileIdentityPlayer = false;
    public boolean wolvesAttackIdentityPrey = true;
    public boolean ownedWolvesAttackIdentityPrey = false;
    public boolean villagersRunFromIdentities = true;
    public boolean foxesAttackIdentityPrey = true;
    public boolean wardenIsBlinded = true;
    public boolean wardenBlindsNearby = true;

    // --- Flight & Movement ---
    public boolean enableFlight = true;
    public float flySpeed = 0.05f;
    public List<String> advancementsRequiredForFlight = new ArrayList<>();
    public int endermanAbilityTeleportDistance = 32;

    // --- Health & Combat ---
    public boolean scalingHealth = true;
    public int maxHealth = 40;
    public int hostilityTime = 20 * 15;

    // --- Identity Gaining ---
    public boolean killForIdentity = false;
    public int requiredKillsForIdentity = 50;
    public Map<String, Integer> requiredKillsByType = new HashMap<>() {{
        put("minecraft:ender_dragon", 1);
        put("minecraft:elder_guardian", 1);
        put("minecraft:wither", 1);
    }};
    public String forcedIdentity = null;

    // --- Server/Client ---
    public boolean enableClientSwapMenu = true;
    public boolean enableSwaps = true;
    public boolean canTradeWithHimSelf = false;
    public List<String> allowedSwappers = new ArrayList<>();

    // --- Tag extensions (xGabou) ---
    public List<String> extraAquaticEntities = new ArrayList<>();
    public List<String> removedAquaticEntities = new ArrayList<>();
    public List<String> extraFlyingEntities = new ArrayList<>();
    public List<String> removedFlyingEntities = new ArrayList<>();

    // --- Ability Cooldowns ---
    public Map<String, Integer> abilityCooldownMap = new HashMap<>() {{
        put("minecraft:ghast", 60);
        put("minecraft:blaze", 20);
        put("minecraft:ender_dragon", 20);
        put("minecraft:enderman", 100);
        put("minecraft:creeper", 100);
        put("minecraft:wither", 200);
        put("minecraft:snow_golem", 10);
        put("minecraft:witch", 200);
        put("minecraft:evoker", 10);
    }};

    // --- Static Access ---

    public static IdentityConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, IdentityConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new IdentityConfig();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load Identity config", e);
            INSTANCE = new IdentityConfig();
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LOGGER.error("Failed to save Identity config", e);
        }
    }

    // --- Getters (maintain API compatibility with xGabou's abstract IdentityConfig) ---

    public boolean enableFlight() { return enableFlight; }
    public List<String> advancementsRequiredForFlight() { return advancementsRequiredForFlight; }
    public Map<String, Integer> getAbilityCooldownMap() { return abilityCooldownMap; }
    public boolean requiresKillsForIdentity() { return killForIdentity; }
    public int getRequiredKillsForIdentity() { return requiredKillsForIdentity; }
    public Map<String, Integer> getRequiredKillsByType() { return requiredKillsByType; }
    public boolean shouldOverlayIdentityUnlocks() { return overlayIdentityUnlocks; }
    public boolean forceChangeNew() { return forceChangeNew; }
    public boolean forceChangeAlways() { return forceChangeAlways; }
    public boolean logCommands() { return logCommands; }
    public boolean enableClientSwapMenu() { return enableClientSwapMenu; }
    public boolean wolvesAttackIdentityPrey() { return wolvesAttackIdentityPrey; }
    public boolean ownedWolvesAttackIdentityPrey() { return ownedWolvesAttackIdentityPrey; }
    public boolean villagersRunFromIdentities() { return villagersRunFromIdentities; }
    public boolean revokeIdentityOnDeath() { return revokeIdentityOnDeath; }
    public boolean overlayIdentityRevokes() { return overlayIdentityRevokes; }
    public float flySpeed() { return flySpeed; }
    public boolean scalingHealth() { return scalingHealth; }
    public int maxHealth() { return maxHealth; }
    public boolean identitiesEquipItems() { return identitiesEquipItems; }
    public boolean identitiesEquipArmor() { return identitiesEquipArmor; }
    public boolean showPlayerNametag() { return showPlayerNametag; }
    public boolean shouldRenderOwnNameTag() { return renderOwnNametag; }
    public boolean foxesAttackIdentityPrey() { return foxesAttackIdentityPrey; }
    public boolean hostilesForgetNewHostileIdentityPlayer() { return hostilesForgetNewHostileIdentityPlayer; }
    public boolean hostilesIgnoreHostileIdentityPlayer() { return hostilesIgnoreHostileIdentityPlayer; }
    public boolean playAmbientSounds() { return playAmbientSounds; }
    public boolean useIdentitySounds() { return useIdentitySounds; }
    public boolean hearSelfAmbient() { return hearSelfAmbient; }
    public double endermanAbilityTeleportDistance() { return endermanAbilityTeleportDistance; }
    public boolean enableSwaps() { return enableSwaps; }
    public int hostilityTime() { return hostilityTime; }
    public boolean wardenIsBlinded() { return wardenIsBlinded; }
    public boolean wardenBlindsNearby() { return wardenBlindsNearby; }
    public String getForcedIdentity() { return forcedIdentity; }

    // --- xGabou extensions ---

    public boolean allowSelfTrading() { return canTradeWithHimSelf; }
    public List<String> allowedSwappers() { return allowedSwappers; }

    public List<String> extraAquaticEntities() { return extraAquaticEntities; }
    public List<String> removedAquaticEntities() { return removedAquaticEntities; }
    public List<String> extraFlyingEntities() { return extraFlyingEntities; }
    public List<String> removedFlyingEntities() { return removedFlyingEntities; }

    // --- Setters (runtime toggles from /identity config command) ---

    public void setAllowSelfTrading(boolean allow) { this.canTradeWithHimSelf = allow; }
    public void setEnableSwaps(boolean enabled) { this.enableSwaps = enabled; }
    public void setOverlayIdentityUnlocks(boolean value) { this.overlayIdentityUnlocks = value; }
    public void setOverlayIdentityRevokes(boolean value) { this.overlayIdentityRevokes = value; }
    public void setRevokeIdentityOnDeath(boolean value) { this.revokeIdentityOnDeath = value; }
    public void setIdentitiesEquipItems(boolean value) { this.identitiesEquipItems = value; }
    public void setIdentitiesEquipArmor(boolean value) { this.identitiesEquipArmor = value; }
    public void setShowPlayerNametag(boolean value) { this.showPlayerNametag = value; }
    public void setRenderOwnNameTag(boolean value) { this.renderOwnNametag = value; }
    public void setHostilesIgnoreHostileIdentityPlayer(boolean value) { this.hostilesIgnoreHostileIdentityPlayer = value; }
    public void setHostilesForgetNewHostileIdentityPlayer(boolean value) { this.hostilesForgetNewHostileIdentityPlayer = value; }
    public void setWolvesAttackIdentityPrey(boolean value) { this.wolvesAttackIdentityPrey = value; }
    public void setOwnedWolvesAttackIdentityPrey(boolean value) { this.ownedWolvesAttackIdentityPrey = value; }
    public void setVillagersRunFromIdentities(boolean value) { this.villagersRunFromIdentities = value; }
    public void setFoxesAttackIdentityPrey(boolean value) { this.foxesAttackIdentityPrey = value; }
    public void setUseIdentitySounds(boolean value) { this.useIdentitySounds = value; }
    public void setPlayAmbientSounds(boolean value) { this.playAmbientSounds = value; }
    public void setHearSelfAmbient(boolean value) { this.hearSelfAmbient = value; }
    public void setEnableFlight(boolean value) { this.enableFlight = value; }
    public void setHostilityTime(int ticks) { this.hostilityTime = ticks; }
    public void setScalingHealth(boolean value) { this.scalingHealth = value; }
    public void setMaxHealth(int value) { this.maxHealth = value; }
    public void setEnableClientSwapMenu(boolean value) { this.enableClientSwapMenu = value; }
    public void setForceChangeNew(boolean value) { this.forceChangeNew = value; }
    public void setForceChangeAlways(boolean value) { this.forceChangeAlways = value; }
    public void setLogCommands(boolean value) { this.logCommands = value; }
    public void setFlySpeed(float value) { this.flySpeed = value; }
    public void setKillForIdentity(boolean value) { this.killForIdentity = value; }
    public void setRequiredKillsForIdentity(int value) { this.requiredKillsForIdentity = value; }
    public void setEndermanAbilityTeleportDistance(int value) { this.endermanAbilityTeleportDistance = value; }
    public void setWardenIsBlinded(boolean value) { this.wardenIsBlinded = value; }
    public void setWardenBlindsNearby(boolean value) { this.wardenBlindsNearby = value; }
    public void setForcedIdentity(@Nullable String id) { this.forcedIdentity = id; }
}
