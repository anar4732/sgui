package eu.pb4.sgui.virtual.sign;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignBlockEntity which doesn't invoke {@link SignBlockEntity#markUpdated()}
 */
public class VirtualSignBlockEntity extends SignBlockEntity {
	public VirtualSignBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}
}