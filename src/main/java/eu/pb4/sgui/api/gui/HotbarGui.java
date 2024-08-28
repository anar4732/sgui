package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.virtual.hotbar.HotbarContainerMenu;
import eu.pb4.sgui.virtual.inventory.VirtualSlot;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * It's a gui implementation for hotbar/player inventory usage
 * <p>
 * Unlike other Slot based guis, it doesn't extend SimpleGui
 */
public class HotbarGui extends BaseSlotGui {
    public static final int SIZE = 46;
    public static final int[] VANILLA_HOTBAR_SLOT_IDS = createArrayFromTo(36, 44);
    public static final int[] VANILLA_BACKPACK_SLOT_IDS = createArrayFromTo(9, 35);
    public static final int[] VANILLA_ARMOR_SLOT_IDS = createArrayFromTo(5, 8);
    public static final int VANILLA_OFFHAND_SLOT_ID = 45;
    public static final int[] VANILLA_CRAFTING_IDS = new int[]{1, 2, 3, 4, 0};
    public static final int[] GUI_TO_VANILLA_IDS = mergeArrays(VANILLA_HOTBAR_SLOT_IDS, new int[]{VANILLA_OFFHAND_SLOT_ID}, VANILLA_BACKPACK_SLOT_IDS, VANILLA_ARMOR_SLOT_IDS, VANILLA_CRAFTING_IDS);
    public static final int[] VANILLA_TO_GUI_IDS = rotateArray(GUI_TO_VANILLA_IDS);
    protected int selectedSlot = 0;
    protected boolean hasRedirects = false;
    private HotbarContainerMenu screenHandler;
    private int clicksPerTick;

    public HotbarGui(ServerPlayerEntity player) {
        super(player, SIZE);
    }

    private static int[] rotateArray(int[] input) {
        int[] array = new int[input.length];
        for (int i = 0; i < array.length; i++) {
            array[input[i]] = i;
        }
        return array;
    }

    private static int[] createArrayFromTo(int first, int last) {
        IntList list = new IntArrayList(last - first);
        for (int i = first; i <= last; i++) {
            list.add(i);
        }

        return list.toIntArray();
    }

    private static int[] mergeArrays(int[]... idArrays) {
        IntList list = new IntArrayList(SIZE);
        for (var array : idArrays) {
            for (int i : array) {
                list.add(i);
            }
        }
        return list.toIntArray();
    }

    @Override
    public void setSlot(int index, GuiElementInterface element) {
        super.setSlot(index, element);
        if (this.isOpen() && this.autoUpdate) {
            this.screenHandler.setSlot(GUI_TO_VANILLA_IDS[index], new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
        }
    }

    public void setSlotRedirect(int index, Slot slot) {
        super.setSlotRedirect(index, slot);

        if (this.isOpen() && this.autoUpdate) {
            this.screenHandler.setSlot(GUI_TO_VANILLA_IDS[index], slot);
        }
        this.hasRedirects = true;
    }

    @Override
    public void clearSlot(int index) {
        super.clearSlot(index);

        if (this.isOpen() && this.autoUpdate) {
            if (this.screenHandler != null) {
                this.screenHandler.setSlot(GUI_TO_VANILLA_IDS[index], new VirtualSlot(this.screenHandler.inventory, index, 0, 0));
            }
        }
    }

    @Override
    public boolean click(int index, ClickType type, net.minecraft.inventory.container.ClickType action) {
        return super.click(VANILLA_TO_GUI_IDS[index], type, action);
    }

    @Override
    public boolean open() {
        if (this.player.hasDisconnected() || this.isOpen()) {
            return false;
        } else {
            this.beforeOpen();
            this.openScreenHandler();
            this.afterOpen();
            return true;
        }
    }

    private void openScreenHandler() {
        this.onOpen();

        if (this.player.containerMenu != this.player.inventoryMenu && this.player.containerMenu != this.screenHandler) {
            this.player.closeContainer();
        }

        if (this.screenHandler == null) {
            this.screenHandler = new HotbarContainerMenu(null, 0, this, this.player);
        }

        this.player.containerMenu = this.screenHandler;

        GuiHelpers.sendPlayerScreenHandler(this.player);
        this.player.connection.send(new SHeldItemChangePacket(this.selectedSlot));
    }

    /**
     * It's run after player changes selected slot. It can also block switching by returning false
     *
     * @param slot new selected slot
     * @return true to allow or false for canceling switching
     */
    public boolean onSelectedSlotChange(int slot) {
        this.setSelectedSlot(slot);
        return true;
    }

    /**
     * This method is called when player uses an item.
     * The vanilla action is always canceled.
     */
    public void onClickItem() {
        if (this.player.isShiftKeyDown()) {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
        } else {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT, net.minecraft.inventory.container.ClickType.PICKUP);
        }
    }

    /**
     * This method is called when player swings their arm
     * If you return false, vanilla action will be canceled
     */
    public boolean onHandSwing() {
        if (this.player.isShiftKeyDown()) {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
        } else {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT, net.minecraft.inventory.container.ClickType.PICKUP);
        }
        return false;
    }

    /**
     * This method is called when player clicks block
     * If you return false, vanilla action will be canceled
     */
    public boolean onClickBlock(BlockRayTraceResult hitResult) {
        if (this.player.isShiftKeyDown()) {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
        } else {
            this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT, net.minecraft.inventory.container.ClickType.PICKUP);
        }

        return false;
    }

    /**
     * This method is called when player send PlayerAction packet
     * If you return false, vanilla action will be canceled
     */
    public boolean onPlayerAction(CPlayerDiggingPacket.Action action, Direction direction) {
        switch (action) {
            case DROP_ITEM -> this.tickLimitedClick(this.selectedSlot, ClickType.DROP, net.minecraft.inventory.container.ClickType.THROW);
            case DROP_ALL_ITEMS -> this.tickLimitedClick(this.selectedSlot, ClickType.CTRL_DROP, net.minecraft.inventory.container.ClickType.THROW);
            case STOP_DESTROY_BLOCK -> {
                if (this.player.isShiftKeyDown()) {
                    this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
                } else {
                    this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT, net.minecraft.inventory.container.ClickType.PICKUP);
                }
            }
            case SWAP_ITEM_WITH_OFFHAND -> this.tickLimitedClick(this.selectedSlot, ClickType.OFFHAND_SWAP, net.minecraft.inventory.container.ClickType.SWAP);
        }

        return false;
    }

    /**
     * This method is called when player clicks an entity
     * If you return false, vanilla action will be canceled
     */
    public boolean onClickEntity(int entityId, EntityInteraction type, boolean isSneaking, @Nullable Vector3d interactionPos) {
        if (type == EntityInteraction.ATTACK) {
            if (isSneaking) {
                this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
            } else {
                this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_LEFT, net.minecraft.inventory.container.ClickType.PICKUP);
            }
        } else {
            if (isSneaking) {
                this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT_SHIFT, net.minecraft.inventory.container.ClickType.QUICK_MOVE);
            } else {
                this.tickLimitedClick(this.selectedSlot, ClickType.MOUSE_RIGHT, net.minecraft.inventory.container.ClickType.PICKUP);
            }
        }
        return false;
    }

    /**
     * @return selectedSlot
     */
    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    /**
     * Changes selected slot and sends it to player
     *
     * @param value slot between 0 and 8
     */
    public void setSelectedSlot(int value) {
        this.selectedSlot = MathHelper.clamp(value, 0, 8);
        if (this.isOpen()) {
            this.player.connection.send(new SHeldItemChangePacket(this.selectedSlot));
        }
    }

    
    private void tickLimitedClick(int selectedSlot, ClickType type, net.minecraft.inventory.container.ClickType actionType) {
        if (this.clicksPerTick == 0) {
            this.click(GUI_TO_VANILLA_IDS[selectedSlot], type, actionType);
        }
        this.clicksPerTick++;
    }

    @Override
    public void onTick() {
        this.clicksPerTick = 0;
        super.onTick();
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        if ((this.isOpen() || screenHandlerIsClosed) && !this.reOpen) {
            if (!screenHandlerIsClosed && this.player.containerMenu == this.screenHandler) {
                this.player.closeContainer();
                this.player.connection.send(new SHeldItemChangePacket(this.player.inventory.selected));
            }

	        player.connection.send(new SWindowItemsPacket(player.containerMenu.containerId, player.containerMenu.getItems()));

            this.onClose();
        } else {
            this.reOpen = false;
        }
    }

    @Override
    public boolean isIncludingPlayer() {
        return true;
    }

    @Override
    public int getVirtualSize() {
        return SIZE;
    }

    @Override
    public boolean isRedirectingSlots() {
        return this.hasRedirects;
    }

    @Override
    public int getSyncId() {
        return 0;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    @Deprecated
    @Override
    public int getHeight() {
        return 4;
    }

    @Deprecated
    @Override
    public int getWidth() {
        return 9;
    }

    @Deprecated
    @Override
    public ContainerType<?> getType() {
        return null;
    }

    @Deprecated
    @Override
    public boolean getLockPlayerInventory() {
        return true;
    }

    @Deprecated
    @Override
    public void setLockPlayerInventory(boolean value) {

    }

    @Deprecated
    @Override
    public @Nullable ITextComponent getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public void setTitle(ITextComponent title) {

    }

    public enum EntityInteraction {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}