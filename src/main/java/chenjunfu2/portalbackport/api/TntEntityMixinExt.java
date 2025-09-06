package chenjunfu2.portalbackport.api;

import net.minecraft.entity.TntEntity;

public interface TntEntityMixinExt
{
	boolean portal_backport_1_20_1$getTeleported();
	void portal_backport_1_20_1$setTeleported(boolean teleported);
	
	void portal_backport_1_20_1$copyFrom(TntEntity tntEntity);
}
