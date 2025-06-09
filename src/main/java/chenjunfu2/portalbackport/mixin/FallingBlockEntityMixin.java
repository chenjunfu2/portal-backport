package chenjunfu2.portalbackport.mixin;


import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin
{
	
	@Inject(method = "tick",
			at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/FallingBlockEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
			shift = At.Shift.AFTER))
	private void inj(CallbackInfo ci)
	{
		((EntityInvoker)this).ivk_tickPortal();
	}
}
