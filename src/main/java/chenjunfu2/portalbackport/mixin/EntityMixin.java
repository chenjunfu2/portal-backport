package chenjunfu2.portalbackport.mixin;

import chenjunfu2.portalbackport.api.TntEntityMixinExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin
{
	//拷贝状态，以便在传送后仍然保留nbt
	@Inject(at = @At("TAIL"), method = "copyFrom")
	void copyFromMixin(Entity original, CallbackInfo ci)
	{
		if (original instanceof TntEntity tntEntity &&
			((Entity)(Object)this) instanceof TntEntity)
		{
			((TntEntityMixinExt)this).portal_backport_1_20_1$copyFrom(tntEntity);
		}
	}
	
	//然后在传送前设置传送标记（如果在传送后，新实体已经拷贝建立，标记设置后原实体会被抛弃，等于白干）
	@Inject(
		at =
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
				shift = At.Shift.BEFORE//必须在传送前调用
			),
		method = "tickPortal"
	)
	void tickPortalMixin(CallbackInfo ci)
	{
		if (((Entity)(Object)this) instanceof TntEntity tntEntity)
		{
			((TntEntityMixinExt)tntEntity).portal_backport_1_20_1$setTeleported(true);
		}
	}
}
