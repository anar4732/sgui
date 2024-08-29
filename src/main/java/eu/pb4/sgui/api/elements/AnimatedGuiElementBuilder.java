package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Animated Gui Element Builder
 * <br>
 * The {@link AnimatedGuiElementBuilder} is the best way of constructing
 * an {@link AnimatedGuiElement}.
 * It supplies all the methods needed to construct each frame and mesh
 * them together to create the full animation.
 *
 * @see GuiElementBuilderInterface
 */
@SuppressWarnings({"unused"})
public class AnimatedGuiElementBuilder implements GuiElementBuilderInterface<AnimatedGuiElementBuilder> {
    protected final List<ItemStack> itemStacks = new ArrayList<>();
    protected ItemStack itemStack = new ItemStack(Items.STONE);
    protected GuiElement.ClickCallback callback = GuiElement.EMPTY_CALLBACK;
    protected int interval = 1;
    protected boolean random = false;

    /**
     * Constructs a AnimatedGuiElementBuilder with the default options
     */
    public AnimatedGuiElementBuilder() {
    }

    /**
     * Constructs a AnimatedGuiElementBuilder with the supplied interval
     *
     * @param interval the time between frame changes
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Sets if the frames should be randomly chosen or more in order
     * of addition.
     *
     * @param value <code>true</code> to select random frames
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setRandom(boolean value) {
        this.random = value;
        return this;
    }

    /**
     * Saves the current stack that is being created.
     * This will add it to the animation and reset the
     * settings awaiting another creation.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder saveItemStack() {
        this.itemStacks.add(this.itemStack.copy());
        this.itemStack = new ItemStack(Items.STONE);
        return this;
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setItem(Item item) {
        this.itemStack = new ItemStack(item.builtInRegistryHolder(), this.itemStack.getCount(), this.itemStack.getComponentsPatch());
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setName(Component name) {
        this.itemStack.set(DataComponents.ITEM_NAME, name.copy());
        return this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setRarity(Rarity rarity) {
        this.itemStack.set(DataComponents.RARITY, rarity);
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCount(int count) {
        this.itemStack.setCount(count);
        return this;
    }

    /**
     * Sets the max number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setMaxCount(int count) {
        this.itemStack.set(DataComponents.MAX_STACK_SIZE, count);
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setLore(List<Component> lore) {
        this.itemStack.set(DataComponents.LORE, new ItemLore(lore));
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public AnimatedGuiElementBuilder addLoreLine(Component lore) {
        this.itemStack.update(DataComponents.LORE, ItemLore.EMPTY, lore, ItemLore::withLineAdded);
        return this;
    }

    /**
     * Set the damage of the element. This will only be
     * visible if the item supports has durability.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setDamage(int damage) {
        this.itemStack.set(DataComponents.DAMAGE, damage);
        return this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setMaxDamage(int damage) {
        this.itemStack.set(DataComponents.MAX_DAMAGE, damage);
        return this;
    }

    /**
     * Disables all default components on an item.
     * @return this element builder
     */
    public AnimatedGuiElementBuilder noDefaults() {
        for (var x : this.itemStack.getItem().components()) {
            if (this.itemStack.get(x.type()) == x.value()) {
                this.itemStack.set(x.type(), null);
            }
        }
        return this;
    }

    @Nullable
    public <T> T getComponent(DataComponentType<T> type) {
        return this.itemStack.get(type);
    }

    public <T> AnimatedGuiElementBuilder setComponent(DataComponentType<T> type, @Nullable T value) {
        this.itemStack.set(type, value);
        return this;
    }

    /**
     * Hides all component-item related tooltip added by item's or non name/lore components.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder hideDefaultTooltip() {
        this.itemStack.update(DataComponents.TRIM, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.UNBREAKABLE, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.ENCHANTMENTS, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.STORED_ENCHANTMENTS, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.ATTRIBUTE_MODIFIERS, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.DYED_COLOR, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.CAN_BREAK, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.update(DataComponents.CAN_PLACE_ON, null, comp -> comp != null ? comp.withTooltip(false) : null);
        this.itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(Holder<Enchantment> enchantment, int level) {
        this.itemStack.enchant(enchantment, level);
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param server MinecraftServer
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(MinecraftServer server, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(server.registryAccess(), enchantment, level);
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param lookup WrapperLookup
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(HolderLookup.Provider lookup, ResourceKey<Enchantment> enchantment, int level) {
        return enchant(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), level);
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow() {
        this.itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow(boolean value) {
        this.itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCustomModelData(int value) {
        this.itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(value));
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder unbreakable() {
        this.itemStack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * If the server parameter is not supplied it may lag the client while it loads the texture,
     * otherwise if the server is provided and the {@link GameProfile} contains a UUID then the
     * textures will be loaded by the server. This can take some time the first load,
     * however the skins are cached for later uses so its often less noticeable to let the
     * server load the textures.
     *
     * @param profile the {@link GameProfile} of the owner
     * @param server  the server instance, used to get the textures
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            if (server.getSessionService().getTextures(profile) == MinecraftProfileTextures.EMPTY) {
                var tmp = server.getSessionService().fetchProfile(profile.getId(), false);
                if (tmp != null) {
                    profile = tmp.profile();
                }
            }

        }
        this.itemStack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setSkullOwner(String value) {
        return this.setSkullOwner(value, null, null);
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @param signature optional signature, will be ignored when set to null
     * @param uuid      UUID of skin owner, if null default will be used
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setSkullOwner(String value, @Nullable String signature, @Nullable UUID uuid) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        this.itemStack.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(), Optional.ofNullable(uuid), map));
        return this;
    }

    @Override
    public AnimatedGuiElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public AnimatedGuiElementBuilder setCallback(GuiElementInterface.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Constructs an ItemStack from the current builder options.
     * Note that this ignores the callback as it is stored in
     * the {@link GuiElement}.
     *
     * @return this builder as a stack
     * @see AnimatedGuiElementBuilder#build()
     */
    public ItemStack asStack() {
        return this.itemStack.copy();
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}