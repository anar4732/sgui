package eu.pb4.sgui.mixin;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.*;
import eu.pb4.sgui.virtual.FakeContainerMenu;
import eu.pb4.sgui.virtual.VirtualContainerMenuInterface;
import eu.pb4.sgui.virtual.hotbar.HotbarContainerMenu;
import eu.pb4.sgui.virtual.inventory.VirtualContainerMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private Container sgui$previousScreen = null;

    @Shadow
    public ServerPlayerEntity player;
	
	@Shadow public abstract void send(IPacket<?> packet);
	
	@Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;resetLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$handleGuiClicks(CClickWindowPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualContainerMenu) {
			VirtualContainerMenu handler = (VirtualContainerMenu) this.player.containerMenu;
            try {
                SlotGuiInterface gui = handler.getGui();

                int slot = packet.getSlotNum();
                int button = packet.getButtonNum();
                ClickType type = ClickType.toClickType(packet.getClickType(), button, slot);
                boolean ignore = gui.onAnyClick(slot, type, packet.getClickType());
                if (ignore && !handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0 || handler.getGui().getSlotRedirect(slot) != null)) {
                    if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2)) {
                        GuiHelpers.sendPlayerScreenHandler(this.player);
                    }

                    return;
                }

                boolean allow = gui.click(slot, type, packet.getClickType());
                if (handler.getGui().isOpen()) {
                    if (!allow) {
                        if (slot >= 0 && slot < handler.getGui().getSize()) {
                            this.send(new SSetSlotPacket(handler.containerId, slot, handler.getSlot(slot).getItem()));
                        }
                        GuiHelpers.sendSlotUpdate(this.player, -1, -1, this.player.inventory.getCarried());

                        if (type.numKey) {
                            int x = type.value + handler.slots.size() - 10;
                            GuiHelpers.sendSlotUpdate(player, handler.containerId, x, handler.getSlot(x).getItem());
                        } else if (type == ClickType.MOUSE_DOUBLE_CLICK || type == ClickType.MOUSE_LEFT_SHIFT || type == ClickType.MOUSE_RIGHT_SHIFT || (type.isDragging && type.value == 2)) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                        }
                    }
                }

            } catch (Throwable e) {
                handler.getGui().handleException(e);
                ci.cancel();
            }

            ci.cancel();
        }
    }

    @Inject(method = "handleContainerClick", at = @At("TAIL"))
    private void sgui$resyncGui(CClickWindowPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualContainerMenu) {
			VirtualContainerMenu handler = (VirtualContainerMenu) this.player.containerMenu;
            try {
                int slot = packet.getSlotNum();
                int button = packet.getButtonNum();
                ClickType type = ClickType.toClickType(packet.getClickType(), button, slot);

                if (type == ClickType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    GuiHelpers.sendPlayerScreenHandler(this.player);
                }

            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "handleContainerClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui$storeScreenHandler(CCloseWindowPacket packet, CallbackInfo info) {
        if (this.player.containerMenu instanceof VirtualContainerMenuInterface) {
			VirtualContainerMenuInterface handler = (VirtualContainerMenuInterface) this.player.containerMenu;
            if (handler.getGui().canPlayerClose()) {
                this.sgui$previousScreen = this.player.containerMenu;
            } else {
                Container screenHandler = this.player.containerMenu;
                if (screenHandler.getType() != null) {
                    try {
                        this.send(new SOpenWindowPacket(screenHandler.containerId, screenHandler.getType(), handler.getGui().getTitle()));
	                    player.connection.send(new SWindowItemsPacket(player.containerMenu.containerId, player.containerMenu.getItems()));
                    } catch (Throwable e) {}
                }
                info.cancel();
            }
        }
    }

    @Inject(method = "handleContainerClose", at = @At("TAIL"))
    private void sgui$executeClosing(CCloseWindowPacket packet, CallbackInfo info) {
        try {
            if (this.sgui$previousScreen != null) {
                if (this.sgui$previousScreen instanceof VirtualContainerMenuInterface) {
					VirtualContainerMenuInterface screenHandler = (VirtualContainerMenuInterface) this.sgui$previousScreen;
                    screenHandler.getGui().close(true);
                }
            }
        } catch (Throwable e) {
            if (this.sgui$previousScreen instanceof VirtualContainerMenuInterface) {
				VirtualContainerMenuInterface screenHandler = (VirtualContainerMenuInterface) this.sgui$previousScreen;
                screenHandler.getGui().handleException(e);
            } else {
                e.printStackTrace();
            }
        }
        this.sgui$previousScreen = null;
    }
	
    @Inject(method = "handleRenameItem", at = @At("TAIL"))
    private void sgui$catchRenamingWithCustomGui(CRenameItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualContainerMenu) {
			VirtualContainerMenu handler = (VirtualContainerMenu) this.player.containerMenu;
            try {
                if (handler.getGui() instanceof AnvilInputGui) {
                    ((AnvilInputGui) handler.getGui()).input(packet.getName());
                }
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "handlePlaceRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;resetLastActionTime()V", shift = At.Shift.BEFORE))
    private void sgui$catchRecipeRequests(CPlaceRecipePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualContainerMenu && ((VirtualContainerMenu) this.player.containerMenu).getGui() instanceof SimpleGui) {
	        VirtualContainerMenu handler = (VirtualContainerMenu) this.player.containerMenu;
			SimpleGui gui = (SimpleGui) handler.getGui();
            try {
                gui.onCraftRequest(packet.getRecipe(), packet.isShiftDown());
            } catch (Throwable e) {
                handler.getGui().handleException(e);
            }
        }
    }

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void sgui$catchSignUpdate(CUpdateSignPacket packet, List<String> p_244542_2_, CallbackInfo ci) {
        try {
            if (this.player.containerMenu instanceof FakeContainerMenu && ((FakeContainerMenu) this.player.containerMenu).getGui() instanceof SignGui) {
				SignGui gui = (SignGui) ((FakeContainerMenu) this.player.containerMenu).getGui();
                for (int i = 0; i < packet.getLines().length; i++) {
                    gui.setLineInternal(i, new StringTextComponent(packet.getLines()[i]));
                }
                gui.close(true);
                ci.cancel();
            }
        } catch (Throwable e) {
            if (this.player.containerMenu instanceof VirtualContainerMenuInterface ) {
				VirtualContainerMenuInterface handler = (VirtualContainerMenuInterface) this.player.containerMenu;
                handler.getGui().handleException(e);
            } else {
                e.printStackTrace();
            }
        }
    }
	
    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$catchUpdateSelectedSlot(CHeldItemChangePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
			HotbarContainerMenu handler = (HotbarContainerMenu) this.player.containerMenu;
            if (!handler.getGui().onSelectedSlotChange(packet.getSlot())) {
                this.send(new SHeldItemChangePacket(handler.getGui().getSelectedSlot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$cancelCreativeAction(CCreativeInventoryActionPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualContainerMenuInterface) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAnimate", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$clickHandSwing(CAnimateHandPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
	        HotbarContainerMenu screenHandler = (HotbarContainerMenu) this.player.containerMenu;
            HotbarGui gui = screenHandler.getGui();
            if (!gui.onHandSwing()) {
                ci.cancel();
            }
        }
    }
	
    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$clickWithItem(CPlayerTryUseItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
			HotbarContainerMenu screenHandler = (HotbarContainerMenu) this.player.containerMenu;
            HotbarGui gui = screenHandler.getGui();
            if (screenHandler.slotsOld != null) {
                screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                screenHandler.slotsOld.set(45, ItemStack.EMPTY);
            }
            gui.onClickItem();
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$clickOnBlock(CPlayerTryUseItemOnBlockPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
			HotbarContainerMenu screenHandler = (HotbarContainerMenu) this.player.containerMenu;
            HotbarGui gui = screenHandler.getGui();

            if (!gui.onClickBlock(packet.getHitResult())) {
                BlockPos pos = packet.getHitResult().getBlockPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }

                this.send(new SChangeBlockPacket(pos, this.player. getLevel().getBlockState(pos)));
                pos = pos.relative(packet.getHitResult().getDirection());
                this.send(new SChangeBlockPacket(pos, this.player.level.getBlockState(pos)));

                ci.cancel();
            }
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$onPlayerAction(CPlayerDiggingPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
			HotbarContainerMenu screenHandler = (HotbarContainerMenu) this.player.containerMenu;
            HotbarGui gui = screenHandler.getGui();

            if (!gui.onPlayerAction(packet.getAction(), packet.getDirection())) {
                BlockPos pos = packet.getPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                this.send(new SChangeBlockPacket(pos, this.player.level.getBlockState(pos)));
                pos = pos.relative(packet.getDirection());
                this.send(new SChangeBlockPacket(pos, this.player.level.getBlockState(pos)));
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleInteract", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketThreadUtil;ensureRunningOnSameThread(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"), cancellable = true)
    private void sgui$clickOnEntity(CUseEntityPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarContainerMenu) {
			HotbarContainerMenu screenHandler = (HotbarContainerMenu) this.player.containerMenu;
            HotbarGui gui = screenHandler.getGui();
	        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
	        try {
		        packet.write(buf);
	        } catch (IOException e) {
		        e.printStackTrace();
		        return;
	        }
	        
	        int entityId = buf.readVarInt();
	        HotbarGui.EntityInteraction type = buf.readEnum(HotbarGui.EntityInteraction.class);

            Vector3d interactionPos = null;

            switch (type) {
                case INTERACT:
                    buf.readVarInt();
                    break;
                case INTERACT_AT:
                    interactionPos = new Vector3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
                    buf.readVarInt();
            }

            boolean isSneaking = buf.readBoolean();

            if (!gui.onClickEntity(entityId, type, isSneaking, interactionPos)) {
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                ci.cancel();
            }
        }
    }
}