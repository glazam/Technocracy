package net.cydhra.technocracy.foundation.content.items.components

import net.cydhra.technocracy.foundation.api.ecs.ComponentType
import net.cydhra.technocracy.foundation.api.tileentities.TEInventoryProvider
import net.cydhra.technocracy.foundation.api.upgrades.*
import net.cydhra.technocracy.foundation.content.capabilities.inventory.DynamicInventoryCapability
import net.cydhra.technocracy.foundation.model.items.api.UpgradeItem
import net.cydhra.technocracy.foundation.model.items.capability.AbstractItemCapabilityComponent
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.*
import net.minecraftforge.common.capabilities.Capability
import java.util.concurrent.CopyOnWriteArrayList


class ItemUpgradesComponent(val numberOfUpgradeSlots: Int, val upgradeType: UpgradeClass) : AbstractItemCapabilityComponent(), TEInventoryProvider {

    /**
     * A set of descriptive lines about installed upgrades
     */
    val description = CopyOnWriteArrayList<Pair<ITextComponent, ITextComponent>>()

    override val type: ComponentType = ComponentType.OTHER

    /**
     * The inventory where to place upgrade items. Is public because GUI must be able to access it for modification
     */
    val inventory: DynamicInventoryCapability = DynamicInventoryCapability(numberOfUpgradeSlots, this)


    init {
        this.inventory.componentParent = this
    }

    override fun serializeNBT(): NBTTagCompound {
        return inventory.serializeNBT()
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        inventory.deserializeNBT(nbt)
        this.updateDescription()
    }

    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        TODO("Not yet implemented")
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isItemValid(inventory: DynamicInventoryCapability, slot: Int, stack: ItemStack): Boolean {
        assert(inventory == this.inventory)
        val item = stack.item

        // only accept upgrade items
        if (item !is UpgradeItem<*>) return false

        // check whether the upgrade is a machine-type upgrade
        if (item.upgradeClass != upgradeType) return false

        // ask the item whether it can be installed
        if (!item.upgrades.all { upgrade -> (upgrade as ItemUpgrade).canInstallUpgrade(wrapper, this) })
            return false

        return true
    }

    override fun onSlotUpdate(inventory: DynamicInventoryCapability, slot: Int, stack: ItemStack,
                              originalStack: ItemStack) {
        if (stack.item == originalStack.item) return

        if (stack.item == Items.AIR) {
            val upgradeItem = originalStack.item

            if (upgradeItem !is UpgradeItem<*>) {
                throw IllegalStateException("Non-upgrade item was installed in upgrade slot.")
            }

            upgradeItem.upgrades.forEach { upgrade ->
                (upgrade as ItemUpgrade).onUninstallUpgrade(wrapper, this)
            }
        } else {
            val upgradeItem = stack.item

            if (upgradeItem !is UpgradeItem<*>) {
                throw IllegalStateException("Non-upgrade item installed in upgrade slot.")
            }

            upgradeItem.upgrades.forEach { upgrade ->
                (upgrade as ItemUpgrade).onInstallUpgrade(wrapper, this)
            }
        }

        this.wrapper.updateItemStack()
        this.updateDescription()
    }

    fun getInstalledUpgrades(): List<ItemUpgrade> {
        return this.inventory.stacks
                .asSequence()
                .filter { !it.isEmpty }
                .map { it.item }
                .filterIsInstance<UpgradeItem<*>>()
                .map { it.upgrades.toList() }
                .flatten()
                .filterIsInstance<ItemUpgrade>()
                .toList()
    }

    override fun onLoadAggregate() {
        super.onLoadAggregate()
        this.getInstalledUpgrades().forEach { it.onUpgradeLoad(wrapper, this) }
    }

    /**
     * Update the [description] list with the current upgrade modifiers.
     */
    private fun updateDescription() {
        this.description.clear()

        this.description.add(TextComponentTranslation("tooltips.upgrades.title.description")
                .appendSibling(TextComponentString(":")) to TextComponentString(
                "${this.inventory.stacks.filter { !it.isEmpty }.count()}/${this.numberOfUpgradeSlots}")
                .setStyle(Style().setColor(TextFormatting.DARK_GREEN)))

//        this.multipliers.forEach { multiplier ->
//            this.description.add(TextComponentTranslation("tooltips.upgrades.parameter.${multiplier.upgradeParameter}")
//                    .appendSibling(TextComponentString(":")) to TextComponentString(
//                    "${(multiplier.getCappedMultiplier() * 100).roundToInt()}%")
//                    .setStyle(Style().setColor(TextFormatting.DARK_GREEN)))
//        }

        this.description.add(TextComponentString("\n") to TextComponentString("\n"))
    }
}