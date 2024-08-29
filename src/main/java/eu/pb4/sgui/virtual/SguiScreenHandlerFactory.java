package eu.pb4.sgui.virtual;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualContainerMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SguiScreenHandlerFactory<T extends GuiInterface> implements INamedContainerProvider {
	private final T gui;
	private final IContainerProvider factory;
	
	public SguiScreenHandlerFactory(T gui, IContainerProvider factory) {
		this.gui = gui;
		this.factory = factory;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		ITextComponent text = this.gui.getTitle();
		if (text == null) {
			text = new StringTextComponent("");
		}
		return text;
	}
	
	@Override
	public Container createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return factory.createMenu(syncId, playerInventory, player);
	}
	
	public static <T extends SlotGuiInterface> SguiScreenHandlerFactory<T> ofDefault(T gui) {
		return new SguiScreenHandlerFactory<>(gui, (syncId, inv, player) -> new VirtualContainerMenu(gui.getType(), syncId, gui, player));
	}
	
	public T getGui() {
		return gui;
	}
}