package eu.pb4.sgui.mixin;

import eu.pb4.sgui.virtual.inventory.VirtualInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public class ScreenHandlerMixin {
	@Inject(method = "canItemQuickReplace", at = @At("HEAD"), cancellable = true)
	private static void sgui$blockIfVirtual(Slot slot, ItemStack stack, boolean allowOverflow, CallbackInfoReturnable<Boolean> cir) {
		if (slot.container instanceof VirtualInventory) {
			cir.setReturnValue(false);
		}
	}
}