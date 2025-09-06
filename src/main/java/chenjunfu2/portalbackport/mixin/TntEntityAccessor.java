package chenjunfu2.portalbackport.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TntEntity.class)
public interface TntEntityAccessor
{
	@Accessor("causingEntity")
	void setCausingEntity(LivingEntity livingEntity);
	
	@Accessor("causingEntity")
	LivingEntity getCausingEntity();
}