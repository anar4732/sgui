package eu.pb4.sgui.api.gui.layered;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;

class BackendSimpleGui extends SimpleGui {
    public final LayeredGui gui;

    public BackendSimpleGui(ContainerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots, LayeredGui gui) {
        super(type, player, manipulatePlayerSlots);
        this.gui = gui;
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.inventory.container.ClickType action) {
        return this.gui.onAnyClick(index, type, action);
    }
    @Override
    public boolean onClick(int index, ClickType type, net.minecraft.inventory.container.ClickType action, GuiElementInterface element) {
        return this.gui.onClick(index, type, action, element);
    }

    @Override
    public void onClose() {
        this.gui.onClose();
    }

    @Override
    public void onTick() {
        this.gui.onTick();
    }

    @Override
    public void onOpen() {
        this.gui.onOpen();
    }

    @Override
    public void beforeOpen() {
        this.gui.beforeOpen();
    }

    @Override
    public void afterOpen() {
        this.gui.afterOpen();
    }
}
