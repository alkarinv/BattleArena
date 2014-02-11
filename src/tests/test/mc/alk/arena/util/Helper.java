package test.mc.alk.arena.util;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.serializers.BaseConfig;
import mc.alk.arena.serializers.ConfigSerializer;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Created by alkarin on 1/16/14.
 */
public class Helper {
    public static MatchParams loadParams(String configFile, Plugin plugin, String node) throws Exception{
        BaseConfig bc = new BaseConfig( new File(configFile));
        ConfigSerializer config = new ConfigSerializer(plugin, bc.getFile(),node);
        MatchParams mp = config.loadMatchParams();
        return mp;
    }

}
