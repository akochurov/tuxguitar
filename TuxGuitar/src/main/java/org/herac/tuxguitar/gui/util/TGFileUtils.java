package org.herac.tuxguitar.gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.system.config.TGConfigKeys;
import org.herac.tuxguitar.util.TGClassLoader;
import org.herac.tuxguitar.util.TGLibraryLoader;
import org.herac.tuxguitar.util.TGVersion;

public class TGFileUtils {

    private static final String TG_CONFIG_PATH = "tuxguitar.config.path";
    private static final String TG_SHARE_PATH = "tuxguitar.share.path";
    private static final String TG_CLASS_PATH = "tuxguitar.class.path";
    private static final String TG_LIBRARY_PATH = "tuxguitar.library.path";
    private static final String TG_LIBRARY_PREFIX = "tuxguitar.library.prefix";
    private static final String TG_LIBRARY_EXTENSION = "tuxguitar.library.extension";

    public static final String PATH_USER_CONFIG = getUserConfigDir();
    public static final String PATH_USER_PLUGINS_CONFIG = getUserPluginsConfigDir();
    public static final String[] TG_STATIC_SHARED_PATHS = getStaticSharedPaths();

    public static InputStream getResourceAsStream(String resource) {
        try {
            if (TG_STATIC_SHARED_PATHS != null) {
                for (String tgStaticSharedPath : TG_STATIC_SHARED_PATHS) {
                    File file = new File(tgStaticSharedPath + File.separator + resource);
                    if (file.exists()) {
                        return new FileInputStream(file);
                    }
                }
            }
            return TGClassLoader.instance().getClassLoader().getResourceAsStream(resource);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static URL getResourceUrl(String resource) {
        try {
            if (TG_STATIC_SHARED_PATHS != null) {
                for (String tgStaticSharedPath : TG_STATIC_SHARED_PATHS) {
                    File file = new File(tgStaticSharedPath + File.separator + resource);
                    if (file.exists()) {
                        return file.toURI().toURL();
                    }
                }
            }
            return TGClassLoader.instance().getClassLoader().getResource(resource);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static Enumeration getResourceUrls(String resource) {
        try {
            Vector<URL> vector = new Vector<URL>();
            if (TG_STATIC_SHARED_PATHS != null) {
                for (String tgStaticSharedPath : TG_STATIC_SHARED_PATHS) {
                    File file = new File(tgStaticSharedPath + File.separator + resource);
                    if (file.exists()) {
                        vector.addElement(file.toURI().toURL());
                    }
                }
            }
            Enumeration<URL> resources = TGClassLoader.instance().getClassLoader().getResources(resource);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!vector.contains(url)) {
                    vector.addElement(url);
                }
            }
            return vector.elements();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private static File getResourcePath(String resource) {
        try {
            if (TG_STATIC_SHARED_PATHS != null) {
                for (String tgStaticSharedPath : TG_STATIC_SHARED_PATHS) {
                    File file = new File(tgStaticSharedPath + File.separator + resource);
                    if (file.exists()) {
                        return file;
                    }
                }
            }
            URL url = TGClassLoader.instance().getClassLoader().getResource(resource);
            if (url != null) {
                return new File(URLDecoder.decode(url.getPath(), "UTF-8"));
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static void loadClasspath() {
        File plugins = getResourcePath("plugins");
        if (plugins != null) {
            TGClassLoader.instance().addPaths(plugins);
        }

        String custompath = System.getProperty(TG_CLASS_PATH);
        if (custompath != null) {
            String[] paths = custompath.split(File.pathSeparator);
            for (String path : paths) {
                TGClassLoader.instance().addPaths(new File(path));
            }
        }
    }

    public static void loadLibraries() {
        String libraryPath = System.getProperty(TG_LIBRARY_PATH);
        if (libraryPath != null) {
            String[] libraryPaths = libraryPath.split(File.pathSeparator);
            String libraryPrefix = System.getProperty(TG_LIBRARY_PREFIX);
            String libraryExtension = System.getProperty(TG_LIBRARY_EXTENSION);
            for (String libraryPath1 : libraryPaths) {
                TGLibraryLoader.instance().loadLibraries(new File(libraryPath1), libraryPrefix, libraryExtension);
            }
        }
    }

    public static String[] getFileNames(String resource) {
        try {
            File path = getResourcePath(resource);
            if (path != null) {
                if (path.exists() && path.isDirectory()) {
                    return path.list();
                }
            }
            URL list = getResourceUrl(resource + "/list.properties");
            if (list != null) {
                List<String> fileNames = Resources.readLines(list, Charsets.UTF_8);
                return fileNames.toArray(new String[fileNames.size()]);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static Image loadImage(String name) {
        return loadImage(TuxGuitar.instance().getConfig().getStringConfigValue(TGConfigKeys.SKIN), name);
    }

    public static Image loadImage(String skin, String name) {
        try {
            InputStream stream = getResourceAsStream("skins/" + skin + "/" + name);
            if (stream != null) {
                return new Image(TuxGuitar.instance().getDisplay(), new ImageData(stream));
            }
            System.err.println(name + ": not found");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return new Image(TuxGuitar.instance().getDisplay(), 16, 16);
    }

    public static boolean isLocalFile(URL url) {
        try {
            if (url.getProtocol().equals(new File(url.getFile()).toURI().toURL().getProtocol())) {
                return true;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    private static String getUserConfigDir() {
        // Look for the system property
        String configPath = System.getProperty(TG_CONFIG_PATH);

        // Default System User Home
        if (configPath == null) {
            configPath = ((System.getProperty("user.home") + File.separator + ".tuxguitar-" + TGVersion.CURRENT.getVersion()));
        }

        // Check if the path exists
        File file = new File(configPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return configPath;
    }

    private static String getUserPluginsConfigDir() {
        String configPluginsPath = (getUserConfigDir() + File.separator + "plugins");

        //Check if the path exists
        File file = new File(configPluginsPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return configPluginsPath;
    }

    private static String[] getStaticSharedPaths() {
        String staticSharedPaths = System.getProperty(TG_SHARE_PATH);
        if (staticSharedPaths != null) {
            return staticSharedPaths.split(File.pathSeparator);
        }
        return new String[] {"."};
    }
}
