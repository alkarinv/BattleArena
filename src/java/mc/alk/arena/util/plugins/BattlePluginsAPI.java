package mc.alk.arena.util.plugins;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

/**
 * @author alkarin
 */

public class BattlePluginsAPI {
    Plugin plugin;
    static final String PROTOCOL = "http";
    static final String HOST = "battleplugins.com";
    static final String USER_AGENT = "BattlePluginsAPI/v1.0";
    String apiKey;

    final Map<String, String> pairs;

    public BattlePluginsAPI() throws IOException {
        pairs = new TreeMap<String, String>();
    }

    private static String urlEncode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    public void pasteFile(String title, String file) throws IOException {
        FileConfiguration c = getConfig();
        apiKey = c.getString("API-Key", null);

        if (apiKey == null) {
            throw new IOException("API Key was not found. You need to register before sending pastes");}
        File f = new File(file);
        addPair("title", title);
        addPair("content", toString(f.getPath()));
        this.post(new URL(PROTOCOL+"://"+HOST+"/api/web/paste/create"), pairs);
    }

    public void addPair(String key,String value) throws UnsupportedEncodingException {
        pairs.put(key, urlEncode((value)));
    }

    public void addZippedPair(String key,String value) throws IOException {
        pairs.put(key, gzip(value));
    }

    public void addPluginInfo(Plugin plugin) throws UnsupportedEncodingException {
        PluginDescriptionFile d = plugin.getDescription();
        addPair("pName", d.getName());
        addPair("pVersion", d.getVersion());
    }

    public void addServerInfo() throws UnsupportedEncodingException {
        addPair("bServerName", Bukkit.getServerName());
        addPair("bVersion", Bukkit.getVersion());
        addPair("bOnlineMode", String.valueOf(Bukkit.getServer().getOnlineMode()));
        addPair("bPlayersOnline", String.valueOf(Bukkit.getServer().getOnlinePlayers().length));
    }

    public void addSystemInfo() throws UnsupportedEncodingException {
        addPair("osArch", System.getProperty("os.arch"));
        addPair("osName", System.getProperty("os.name"));
        addPair("osVersion", System.getProperty("os.version"));
        addPair("jVersion", System.getProperty("java.version"));
        addPair("nCores", String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    private void get(URL baseUrl) throws IOException {
        /// Connect
        URL url = new URL (baseUrl.getProtocol()+"://"+baseUrl.getHost()+baseUrl.getPath() + "?" + encode(pairs));
        URLConnection connection = url.openConnection(Proxy.NO_PROXY);

        /// Connection information
        connection.addRequestProperty("GET", "/api/web/blog/all HTTP/1.1");
        connection.addRequestProperty("Host", HOST);
        connection.addRequestProperty("X-API-Key", apiKey);
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);

        /// write the data to the stream
        OutputStream os = connection.getOutputStream();
        os.flush();

        /// Get our response
        final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null){
            System.out.println(line);
        }

        os.close();
        br.close();
    }

    private void post(URL url, Map<String, String> pairs) throws IOException {
        /// Connect
        URLConnection connection = url.openConnection(Proxy.NO_PROXY);

        byte[] data = encode(pairs).getBytes();
        /// Connection information
        connection.addRequestProperty("POST", "/api/web/blog/all HTTP/1.1");
        connection.addRequestProperty("Host", HOST);
        connection.addRequestProperty("X-API-Key", apiKey);
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Content-length",String.valueOf(data.length));
        connection.setDoOutput(true);

        /// write the data to the stream
        OutputStream os = connection.getOutputStream();
        os.write(data);
        os.flush();

        /// Get our response
        final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null){
            System.out.println(line);
        }

        os.close();
        br.close();
    }

    String gzip(String str) throws IOException {
        GZIPOutputStream out = new GZIPOutputStream(new ByteArrayOutputStream());
        out.write(str.getBytes());
        out.close();
        return out.toString();
    }

    String encode(Map<String, String> pairs) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> e : pairs.entrySet()) {
            if (!first) sb.append("&");
            else first = false;
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }


    @Override
    public String toString(){
        return "[BattlePluginsAPI]";
    }

    /** Code from erikson, http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file*/
    private static String toString(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    public FileConfiguration getConfig(){
        File pluginFolder = Bukkit.getServer().getUpdateFolderFile().getParentFile();
        File f = new File(pluginFolder,"BattlePluginsAPI");
        FileConfiguration c;
        if (!f.exists())
            f.mkdirs();
        try {
            f = new File(f, "config.yml");
            if (!f.exists()){
                f.createNewFile();
            }
            c = YamlConfiguration.loadConfiguration(f);
            if (c.get("API-Key", null) == null) {
                c.options().header("#checking");
                c.addDefault("API-Key", "");
                c.options().copyDefaults(true);
                c.save(f);
            }
        } catch (IOException e) {
            return null;
        }
        return c;
    }
}
