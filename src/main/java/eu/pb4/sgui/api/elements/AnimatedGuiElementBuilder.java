package eu.pb4.sgui.api.elements;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.GuiHelpers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.*;

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
    protected final Map<Enchantment, Integer> enchantments = new HashMap<>();
    protected final List<ItemStack> itemStacks = new ArrayList<>();
    protected Item item = Items.STONE;
    protected CompoundNBT tag;
    protected int count = 1;
    protected ITextComponent name = null;
    protected List<ITextComponent> lore = new ArrayList<>();
    protected int damage = -1;
    protected GuiElement.ClickCallback callback = GuiElement.EMPTY_CALLBACK;
    protected byte hideFlags = 0;
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
        this.itemStacks.add(asStack());

        this.item = Items.STONE;
        this.tag = null;
        this.count = 1;
        this.name = null;
        this.lore = new ArrayList<>();
        this.damage = -1;
        this.hideFlags = 0;
        this.enchantments.clear();

        return this;
    }

    /**
     * Sets the type of Item of the current element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setItem(Item item) {
        this.item = item;
        return this;
    }

    /**
     * Sets the name of the current element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setName(ITextComponent name) {
        this.name = name.copy();
        return this;
    }

    /**
     * Sets the number of items in the current element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the lore lines of the current element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setLore(List<ITextComponent> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Adds a line of lore to the current element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public AnimatedGuiElementBuilder addLoreLine(ITextComponent lore) {
        this.lore.add(lore);
        return this;
    }

    /**
     * Set the damage of the current element. This will only be
     * visible if the item supports has durability.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }
	
    public AnimatedGuiElementBuilder hideFlags() {
        this.hideFlags = 127;
        return this;
    }
	
    public AnimatedGuiElementBuilder hideFlag(ItemStack.TooltipDisplayFlags section) {
        this.hideFlags = (byte) (this.hideFlags | section.getMask());
        return this;
    }
	
    public AnimatedGuiElementBuilder hideFlags(byte value) {
        this.hideFlags = value;
        return this;
    }

    /**
     * Give the current element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public AnimatedGuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Sets the current element to have an enchantment glint.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder glow() {
        this.enchantments.put(Enchantments.FISHING_LUCK, 1);
        return hideFlag(ItemStack.TooltipDisplayFlags.ENCHANTMENTS);
    }

    /**
     * Sets the custom model data of the current element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public AnimatedGuiElementBuilder setCustomModelData(int value) {
        this.getOrCreateNbt().putInt("CustomModelData", value);
        return this;
    }

    /**
     * Sets the current element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public AnimatedGuiElementBuilder unbreakable() {
        this.getOrCreateNbt().putBoolean("Unbreakable", true);
        return hideFlag(ItemStack.TooltipDisplayFlags.UNBREAKABLE);
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
            if (server.getSessionService().getTextures(profile, false).isEmpty()) {
                profile = server.getSessionService().fillProfileProperties(profile, false);
            }
			this.getOrCreateNbt().put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), profile));
        } else {
            this.getOrCreateNbt().putString("SkullOwner", profile.getName());
        }
        return this;
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
        CompoundNBT skullOwner = new CompoundNBT();
        CompoundNBT properties = new CompoundNBT();
        CompoundNBT valueData = new CompoundNBT();
        ListNBT textures = new ListNBT();

        valueData.putString("Value", value);
        if (signature != null) {
            valueData.putString("Signature", signature);
        }

        textures.add(valueData);
        properties.put("textures", textures);

        skullOwner.put("Id", NBTUtil.createUUID(uuid != null ? uuid : Util.NIL_UUID));
        skullOwner.put("Properties", properties);
        this.getOrCreateNbt().put("SkullOwner", skullOwner);

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
        ItemStack itemStack = new ItemStack(this.item, this.count);

        if (this.tag != null) {
            itemStack.getOrCreateTag().merge(this.tag);
        }

        if (this.name != null) {
            IFormattableTextComponent name = this.name.copy().withStyle(GuiHelpers.STYLE_CLEARER);
            itemStack.setHoverName(name);
        }

        if (this.item.canBeDepleted() && this.damage != -1) {
            itemStack.setDamageValue(damage);
        }

        for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
            itemStack.enchant(entry.getKey(), entry.getValue());
        }

        if (this.lore.size() > 0) {
            CompoundNBT display = itemStack.getOrCreateTagElement("display");
            ListNBT loreItems = new ListNBT();
            for (ITextComponent l : this.lore) {
                l = l.copy().withStyle(GuiHelpers.STYLE_CLEARER);
                loreItems.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
        }

        if (this.hideFlags != 0) {
            itemStack.getOrCreateTag().putByte("HideFlags", this.hideFlags);
        }

        return itemStack;
    }

    public CompoundNBT getOrCreateNbt() {
        if (this.tag == null) {
            this.tag = new CompoundNBT();
        }
        return this.tag;
    }

    public AnimatedGuiElement build() {
        return new AnimatedGuiElement(this.itemStacks.toArray(new ItemStack[0]), this.interval, this.random, this.callback);
    }
}