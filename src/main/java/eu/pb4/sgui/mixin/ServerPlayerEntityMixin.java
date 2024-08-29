package eu.pb4.sgui.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.impl.PlayerExtensions;
import eu.pb4.sgui.virtual.SguiScreenHandlerFactory;
import eu.pb4.sgui.virtual.VirtualContainerMenuInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerExtensions {
    @Shadow
    public abstract void doCloseContainer();

    @Unique
    private boolean sgui$ignoreNext = false;
	
	public ServerPlayerEntityMixin(World pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile, boolean sgui$ignoreNext) {
		super(pLevel, pPos, pYRot, pGameProfile);
		this.sgui$ignoreNext = sgui$ignoreNext;
	}
	
	@Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;closeContainer()V", shift = At.Shift.BEFORE))
    private void sgui$dontForceCloseFor(INamedContainerProvider factory, CallbackInfoReturnable<OptionalInt> cir) {
		if (factory instanceof SguiScreenHandlerFactory<?>) {
			SguiScreenHandlerFactory<?> sguiScreenHandlerFactory = (SguiScreenHandlerFactory<?>) factory;
			if (!sguiScreenHandlerFactory.getGui().resetMousePosition()) {
				this.sgui$ignoreNext = true;
			}
		}
    }

    @Inject(method = "closeContainer", at = @At("HEAD"), cancellable = true)
    private void sgui$ignoreClosing(CallbackInfo ci) {
        if (this.sgui$ignoreNext) {
            this.sgui$ignoreNext = false;
            this.doCloseContainer();
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void sgui$onDeath(DamageSource source, CallbackInfo ci) {
        if (this.containerMenu instanceof VirtualContainerMenuInterface) {
	        VirtualContainerMenuInterface handler = (VirtualContainerMenuInterface) this.containerMenu;
            handler.getGui().close(true);
        }
    }

    @Override
    public void sgui$ignoreNextClose() {
        this.sgui$ignoreNext = true;
    }
}