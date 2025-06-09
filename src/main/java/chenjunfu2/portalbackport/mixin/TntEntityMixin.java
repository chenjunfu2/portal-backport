package chenjunfu2.portalbackport.mixin;

import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TntEntity.class)
public abstract class TntEntityMixin
{
	@Inject(at = @At("HEAD"), method = "tick")
	public void jni(CallbackInfo info)
	{
		((EntityInvoker)this).ivk_tickPortal();
	}
}

