package se.gory_moon.tallgates;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.tallgates.blocks.BlockRegistry;
import se.gory_moon.tallgates.items.ItemRegistry;
import se.gory_moon.tallgates.lib.ModInfo;
import se.gory_moon.tallgates.proxy.CommonProxy;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, certificateFingerprint = ModInfo.FINGERPRINT)
public class TallGatesMod {

    public static Logger LOG = LogManager.getLogger("HorsePower");

    @SidedProxy(modId = ModInfo.MODID, clientSide = ModInfo.CLIENTPROXY_LOCATION, serverSide = ModInfo.COMMONPROXY_LOCATION)
    public static CommonProxy proxy;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        BlockRegistry.preInit();
        ItemRegistry.preInit();
    }

    @EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        LOG.warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }

}
