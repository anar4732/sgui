package eu.pb4.sgui.api;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.impl.PlayerExtensions;
import eu.pb4.sgui.virtual.VirtualContainerMenuInterface;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

public final class GuiHelpers {
    public static final UnaryOperator<Style> STYLE_CLEARER = style -> style.withItalic(style.isItalic()).withColor(style.getColor() != null ? style.getColor() : Color.fromLegacyFormat(TextFormatting.WHITE));

    @Nullable
    public static GuiInterface getCurrentGui(ServerPlayerEntity player) {
        return player.containerMenu instanceof VirtualContainerMenuInterface v ? v.getGui() : null;
    }

    public static void ignoreNextGuiClosing(ServerPlayerEntity player) {
        ((PlayerExtensions) player).sgui$ignoreNextClose();
    }
	
    public static void sendSlotUpdate(ServerPlayerEntity player, int syncId, int slot, ItemStack stack) {
	    player.connection.send(new SSetSlotPacket(syncId, slot, stack));
    }

    public static void sendPlayerScreenHandler(ServerPlayerEntity player) {
        player.connection.send(new SWindowItemsPacket(player.containerMenu.containerId, player.containerMenu.getItems()));
    }

    public static void sendPlayerInventory(ServerPlayerEntity player) {
        player.connection.send(new SWindowItemsPacket(player.inventoryMenu.containerId, player.inventoryMenu.getItems()));
    }

    public static int posToIndex(int x, int y, int height, int width) {
        return x + y * width;
    }

    public static int getHeight(ContainerType<?> type) {
        if (ContainerType.GENERIC_9x6.equals(type)) {
            return 6;
        } else if (ContainerType.GENERIC_9x5.equals(type) || ContainerType.CRAFTING.equals(type)) {
            return 5;
        } else if (ContainerType.GENERIC_9x4.equals(type)) {
            return 4;
        } else if (ContainerType.GENERIC_9x2.equals(type) || ContainerType.ENCHANTMENT.equals(type) || ContainerType.STONECUTTER.equals(type)) {
            return 2;
        } else if (ContainerType.GENERIC_9x1.equals(type) || ContainerType.BEACON.equals(type) || ContainerType.HOPPER.equals(type) || ContainerType.BREWING_STAND.equals(type) || ContainerType.SMITHING.equals(type)) {
            return 1;
        }

        return 3;
    }

    public static int getWidth(ContainerType<?> type) {
        if (ContainerType.CRAFTING.equals(type)) {
            return 2;
        } else if (ContainerType.SMITHING.equals(type)) {
            return 4;
        } else if (ContainerType.GENERIC_3x3.equals(type)) {
            return 3;
        } else if (ContainerType.HOPPER.equals(type) || ContainerType.BREWING_STAND.equals(type)) {
            return 5;
        } else if (ContainerType.ENCHANTMENT.equals(type) || ContainerType.STONECUTTER.equals(type) || ContainerType.BEACON.equals(type) || ContainerType.BLAST_FURNACE.equals(type) || ContainerType.FURNACE.equals(type) || ContainerType.SMOKER.equals(type) || ContainerType.ANVIL.equals(type) || ContainerType.GRINDSTONE.equals(type) || ContainerType.MERCHANT.equals(type) || ContainerType.CARTOGRAPHY_TABLE.equals(type) || ContainerType.LOOM.equals(type)) {
            return 1;
        }

        return 9;
    }
}