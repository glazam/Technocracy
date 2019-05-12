package net.cydhra.technocracy.foundation.tileentity

import net.cydhra.technocracy.foundation.tileentity.api.TCTileEntity
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity



abstract class AbstractTileEntity : TileEntity(), TCTileEntity {

    /**
     * Cached attached block's BlockState.
     */
    protected var state: IBlockState? = null

    /**
     * Query the world for the [IBlockState] associated with this entity
     *
     * @return the block state of the associated block in world
     */
    protected fun getBlockState(): IBlockState {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.getPos())
        }
        return this.state!!
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        return SPacketUpdateTileEntity(this.pos, 3, this.updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return this.writeToNBT(NBTTagCompound())
    }

    /**
     * Mark the block for a block update.
     */
    fun markForUpdate() {
        if (this.world != null) {
            this.world.markBlockRangeForRenderUpdate(pos, pos)
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3)
            markDirty()
        }
    }
}