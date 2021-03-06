package net.cydhra.technocracy.foundation.api.upgrades

/**
 * Every upgrade item has a class assigned and certain machines may only accept certain upgrade classes (so a
 * pulverizer is not upgradeable with a "semipermeable membrane" or something of that kind)
 */
enum class UpgradeClass(val unlocalizedName: String) {
    MACHINE("machine"),
    HELMET("helmet"),
    CHEST_PLATE("plate"),
    LEGGINGS("leggings"),
    BOOTS("boots"),
    TOOL("tool")
}