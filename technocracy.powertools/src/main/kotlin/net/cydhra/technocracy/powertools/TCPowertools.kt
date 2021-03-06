package net.cydhra.technocracy.powertools

import net.cydhra.technocracy.powertools.proxy.CommonProxy
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.FMLLog
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger


@Mod(modid = TCPowertools.MODID, name = TCPowertools.NAME, version = TCPowertools.VERSION,
        modLanguageAdapter = TCPowertools.LANGUAGE_ADAPTER, dependencies = TCPowertools.DEPENDENCIES)
@Mod.EventBusSubscriber(modid = TCPowertools.MODID)
object TCPowertools {

    /**
     * Module-internal constant for the forge mod identifier
     */
    const val MODID = "technocracy.powertools"

    /**
     * Mod version
     */
    const val VERSION = "1.0"

    /**
     * Mod name used by forge
     */
    const val NAME = "Technocracy Powertools"

    /**
     * The adapter responsible to load this mod class, as it is not a default java mod class
     */
    internal const val LANGUAGE_ADAPTER = "net.shadowfacts.forgelin.KotlinAdapter"

    /**
     * All forge mod hard-dependencies that must be provided
     */
    internal const val DEPENDENCIES = "required-after:forgelin;" +
            "required-after:technocracy.foundation"

    /**
     * Mod logger
     */
    lateinit var logger: Logger

    @SidedProxy(
            serverSide = "net.cydhra.technocracy.powertools.proxy.CommonProxy",
            clientSide = "net.cydhra.technocracy.powertools.proxy.ClientProxy")
    lateinit var proxy: CommonProxy

    @Suppress("unused")
    @Mod.EventHandler
    fun preInit(@Suppress("UNUSED_PARAMETER") event: FMLPreInitializationEvent) {
        logger = FMLLog.log

        MinecraftForge.EVENT_BUS.register(proxy)

        proxy.initializeProxy()
        proxy.preInit()
    }

    @Suppress("unused")
    @Mod.EventHandler
    fun init(@Suppress("UNUSED_PARAMETER") event: FMLInitializationEvent) {
        proxy.init()
    }

    @Suppress("unused")
    @Mod.EventHandler
    fun postInit(@Suppress("UNUSED_PARAMETER") event: FMLPostInitializationEvent) {
        proxy.postInit()
    }

}
