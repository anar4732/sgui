package eu.pb4.sgui.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {
	@Invoker
	void callWrite(FriendlyByteBuf p_134058_);
}