package eu.pb4.sgui.api;

import net.minecraft.inventory.container.ContainerType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Screen Properties
 * <br>
 * Screen properties are values sent to client {@link net.minecraft.world.inventory.AbstractContainerMenu}s which
 * update visual or logical elements of the screen. <br>
 * Screen properties are specific to the {@link MenuType} that they modify.
 *
 * @see eu.pb4.sgui.api.gui.GuiInterface#sendProperty(ScreenProperty, int)
 */
@SuppressWarnings("unused")
public enum ScreenProperty {
    /**
     * {@link MenuType#FURNACE}, {@link MenuType#BLAST_FURNACE}, {@link MenuType#SMOKER}
     * <p>
     * The level of the fire icon in the furnace
     * <ul>
     *     <li>Empty = 0</li>
     *     <li>Full = Value of MAX_FUEL_BURN_TIME</li>
     * </ul>
     */
    FIRE_LEVEL(0, ContainerType.FURNACE, ContainerType.BLAST_FURNACE, ContainerType.SMOKER),
    /**
     * The maximum burn time of the furnace fuel
     */
    MAX_FUEL_BURN_TIME(1, ContainerType.FURNACE, ContainerType.BLAST_FURNACE, ContainerType.SMOKER),
    /**
     * The current progress ticks of the arrow
     * <ul>
     *     <li>No Progress = 0</li>
     *     <li>Complete = Value of MAX_PROGRESS</li>
     * </ul>
     */
    CURRENT_PROGRESS(2, ContainerType.FURNACE, ContainerType.BLAST_FURNACE, ContainerType.SMOKER),
    /**
     * The ticks required for the burn to complete (200 on a vanilla server)
     */
    MAX_PROGRESS(3, ContainerType.FURNACE, ContainerType.BLAST_FURNACE, ContainerType.SMOKER),

    /**
     * {@link MenuType#ENCHANTMENT}
     * <p>
     * The level requirement of the respective enchantment.
     */
    TOP_LEVEL_REQ(0, ContainerType.ENCHANTMENT),
    MIDDLE_LEVEL_REQ(1, ContainerType.ENCHANTMENT),
    BOTTOM_LEVEL_REQ(2, ContainerType.ENCHANTMENT),
    /**
     * Used for drawing the enchantment names (in SGA) clientside.
     * <p>
     * The same seed is used to calculate enchantments, but some of the data isn't sent to the client to prevent easily guessing the entire list (the seed value here is the regular seed bitwise and 0xFFFFFFF0).
     */
    ENCHANT_SEED(3, ContainerType.ENCHANTMENT),
    /**
     * The enchantment id of the respective enchantment (set to -1 to hide).
     * <p>
     * To get the id use {@link Registry#getId(Object)} for {@link BuiltInRegistries#ENCHANTMENT}.
     */
    TOP_ENCHANTMENT_ID(4, ContainerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_ID(5, ContainerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_ID(6, ContainerType.ENCHANTMENT),
    /**
     * The enchantment level of the respective enchantment
     * <ul>
     *     <li>-1 = No Enchantment</li>
     *     <li>1 = I</li>
     *     <li>2 = II</li>
     *     <li>...</li>
     *     <li>6 = VI</li>
     * </ul>
     */
    TOP_ENCHANTMENT_LEVEL(7, ContainerType.ENCHANTMENT),
    MIDDLE_ENCHANTMENT_LEVEL(8, ContainerType.ENCHANTMENT),
    BOTTOM_ENCHANTMENT_LEVEL(9, ContainerType.ENCHANTMENT),

    /**
     * {@link MenuType#BEACON}
     * <p>
     * Controls what effect buttons are enabled, equivalent to the number of layers.
     * <ul>
     *     <li>No Layers = 0</li>
     *     <li>Full Beacon = 4</li>
     * </ul>
     */
    POWER_LEVEL(0, ContainerType.BEACON),
    /**
     * The effect id for the respective effect
     * To get the id use {@link Registry#getId(Object)} for {@link BuiltInRegistries#POTION}
     */
    FIRST_EFFECT(1, ContainerType.BEACON),
    SECOND_EFFECT(2, ContainerType.BEACON),

    /**
     * {@link MenuType#ANVIL}
     * <p>
     * The level cost of the operation. Anything >30 will display as 'Too Expensive!'
     */
    LEVEL_COST(0, ContainerType.ANVIL),

    /**
     * {@link MenuType#BREWING_STAND}
     * <p>
     * The ticks remaining until the operation completes
     * <ul>
     *     <li>Empty Arrow = 400</li>
     *     <li>Full Arrow = 0</li>
     * </ul>
     */
    BREW_TIME(0, ContainerType.BREWING_STAND),
    /**
     * The ticks remaining in the fuel display
     * <ul>
     *     <li>Empty Bubbles = 0</li>
     *     <li>Full Bubbles = 20</li>
     * </ul>
     */
    POWDER_FUEL_TIME(1, ContainerType.BREWING_STAND),

    /**
     * {@link MenuType#STONECUTTER}, {@link MenuType#LOOM}, {@link MenuType#LECTERN}
     * <p>
     * The index of the selected element (Cut, Pattern, Page, ect.)
     * <ul>
     *     <li>No Element Selected = -1</li>
     *     <li>First Element = 0</li>
     *     <li>Second Element = 1</li>
     *     <li><code>n</code> Element = <code>n</code></li>
     * </ul>
     */
    SELECTED(0, ContainerType.STONECUTTER, ContainerType.LOOM, ContainerType.LECTERN);

    private final int id;
    private final ContainerType<?>[] types;

    ScreenProperty(int id, ContainerType<?>... types) {
        this.id = id;
        this.types = types;
    }

    public int id() {
        return id;
    }

    public boolean validFor(ContainerType<?> type) {
        return ArrayUtils.contains(types, type);
    }
}