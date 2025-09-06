package chenjunfu2.portalbackport.mixin;

import chenjunfu2.portalbackport.api.TntEntityMixinExt;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;


@Mixin(TntEntity.class)
public abstract class TntEntityMixin implements TntEntityMixinExt
{
	//字段访问器
	@Shadow private @Nullable LivingEntity causingEntity;
	
	//添加新字段，用于获取是否进行过传送
	@Unique
	private boolean teleported = false;
	
	@Unique
	@Override
	public boolean portal_backport_1_20_1$getTeleported()
	{
		return this.teleported;
	}
	
	@Unique
	@Override
	public void portal_backport_1_20_1$setTeleported(boolean teleported)
	{
		this.teleported = teleported;
	}
	
	//添加新字段，用于处理破坏行为
	@Unique
	private static final ExplosionBehavior TELEPORTED_EXPLOSION_BEHAVIOR = new ExplosionBehavior(){
		public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
			return state.isOf(Blocks.NETHER_PORTAL) ? false : super.canDestroyBlock(explosion, world, pos, state, power);
		}
		
		public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
			return blockState.isOf(Blocks.NETHER_PORTAL) ? Optional.empty() : super.getBlastResistance(explosion, world, pos, blockState, fluidState);
		}
	};
	
	@Inject(at = @At("HEAD"), method = "explode", cancellable = true)
	public void explodeMixin(CallbackInfo ci)
	{
		TntEntity thisTntEntity = (TntEntity)((Object)this);
		
		thisTntEntity.getWorld().createExplosion(
			thisTntEntity,
			null,
			this.teleported ? TELEPORTED_EXPLOSION_BEHAVIOR : null,
			thisTntEntity.getX(),
			thisTntEntity.getBodyY(0.0625),
			thisTntEntity.getZ(),
			4.0F,
			false,
			World.ExplosionSourceType.TNT);
		
		//取消剩下代码，直接返回
		ci.cancel();
	}
	
	//添加新字段，用于处理实体拷贝
	@Unique
	@Override
	public void portal_backport_1_20_1$copyFrom(TntEntity tntEntity)
	{
		//不拷贝这个传送之后点燃tnt的实体就丢失了
		this.causingEntity = (((TntEntityAccessor)tntEntity).getCausingEntity());
		//注意哪怕传送可能会设置Teleported标记，也必须拷贝，因为只有传送门可以影响这个标记，跨纬度tp则理应不行
		//但是传送门传送后标记为true的情况下，跨纬度tp也不应该使其为false，总之拷贝就对了
		this.teleported = ((TntEntityMixinExt)tntEntity).portal_backport_1_20_1$getTeleported();
	}
	
	//把是否传送写入nbt内，以便在游戏关闭后保存，修复重启后tnt炸掉传送门的问题
	@Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
	public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo ci)
	{
		nbt.putBoolean("Teleported", this.teleported);//boolean == NbtElement.BYTE_TYPE
		if(this.causingEntity != null)
		{
			nbt.putUuid("Owner", this.causingEntity.getUuid());//uuid == NbtElement.INT_ARRAY_TYPE
		}
	}
	
	@Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
	public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo ci)
	{
		if(nbt.contains("Teleported", NbtElement.BYTE_TYPE))//防止mod第一次注入访问不存在的东西
		{
			this.teleported = nbt.getBoolean("Teleported");
		}
		
		if(nbt.contains("Owner",NbtElement.INT_ARRAY_TYPE))
		{
			UUID uuid = nbt.getUuid("Owner");
			TntEntity thisTntEntity = (TntEntity)((Object)this);
			if(thisTntEntity.getWorld() instanceof ServerWorld serverWorld &&
			   serverWorld.getEntity(uuid) instanceof LivingEntity livingEntity)
			{
				causingEntity = livingEntity;
			}
		}
	}
	
	//添加传送门tick方法调用，以便tnt能进行传送
	@Inject(at = @At("HEAD"), method = "tick")
	public void tickMixin(CallbackInfo info)
	{
		((EntityInvoker)this).ivk_tickPortal();
	}
}

