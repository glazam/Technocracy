package net.cydhra.technocracy.foundation.content.tileentities.logic

import net.cydhra.technocracy.foundation.api.ecs.logic.ILogic
import net.cydhra.technocracy.foundation.content.tileentities.components.TileEntityHeatStorageComponent
import net.cydhra.technocracy.foundation.content.tileentities.components.TileEntityMultiplierComponent

/**
 * A logic that generates heat whenever the machine is doing work. How much heat is generated depends on the energy
 * consumption of the machine. If the heat buffer is full, the machine stops working.
 *
 * @param baseHeatGeneration how much heat is generated per RF energy consumed
 * @param energyMultiplierComponent the energy multiplier of the machine
 * @param heatBuffer the heat buffer of the machine.
 */
class GenerateHeatLogic(
        private val baseHeatGeneration: Int,
        private val energyMultiplierComponent: TileEntityMultiplierComponent,
        private val heatBuffer: TileEntityHeatStorageComponent
) : ILogic {

    override fun preProcessing(): Boolean {
        if (heatBuffer.heatCapacity - heatBuffer.heat < this.getGeneratedHeat())
            return false

        return true
    }

    override fun processing() {

    }

    override fun postProcessing(wasProcessing: Boolean) {
        if (wasProcessing) {
            heatBuffer.heat += this.getGeneratedHeat().coerceAtMost(heatBuffer.heatCapacity - heatBuffer.heat)
        }
    }

    /**
     * Calculate how much heat is generated this tick
     */
    private fun getGeneratedHeat(): Int {
        return (this.baseHeatGeneration * energyMultiplierComponent.getCappedMultiplier()).toInt()
    }
}