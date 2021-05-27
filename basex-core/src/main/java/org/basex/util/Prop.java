package org.basex.util;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.jar.Attributes;

import org.basex.io.*;
import org.basex.util.options.*;

/**
 * This class contains constants and system properties which are used all around the project.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Prop {
  /** Project name. */
  public static final String NAME = "BaseX";
  /** Code version (may contain major, minor and optional patch number). */
  public static final String VERSION = version("9.5.2");

  /** Project name. */
  public static final String PROJECT = NAME.toLowerCase(Locale.ENGLISH);

  /** System-specific newline string. */
  public static final String NL = System.getProperty("line.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");
  /** OS flag (source: {@code http://lopica.sourceforge.net/os.html}). */
  private static final String OS = System.getProperty("os.name");
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.startsWith("Mac");
  /** Flag denoting if OS belongs to Windows family. */
  public static final boolean WIN = OS.startsWith("Windows");
  /** Respect lower/upper case when doing file comparisons. */
  public static final boolean CASE = !(MAC || WIN);
  /** Java version. */
  public static final String JAVA = System.getProperty("java.specification.version");
  /** JDK version is Java 1.8. */
  public static final boolean JAVA8 = JAVA.equalsIgnoreCase("1.8");

  /** Prefix for project specific options. */
  public static final String DBPREFIX = "org.basex.";
  /** System property for specifying database home directory. */
  public static final String PATH = DBPREFIX + "path";

  /** Application URL. */
  public static final URL LOCATION = location();
  /** System's temporary directory. */
  public static final String TEMPDIR = dir(System.getProperty("java.io.tmpdir"));
  /** Project home directory. */
  public static final String HOMEDIR;

  /** Global options, assigned by the starter classes and the web.xml context parameters. */
  private static final Map<String, String> OPTIONS = new ConcurrentHashMap<>();

  // determine project home directory for storing property files and directories...
  static {
    // check system property 'org.basex.path'
    String homedir = System.getProperty(PATH);
    // check if current working directory contains configuration file
    if(homedir == null) homedir = configDir(System.getProperty("user.dir"));
    // check if application directory contains configuration file
    if(homedir == null) homedir = configDir(applicationDir(LOCATION));
    // fallback: choose home directory (linux: check HOME variable, GH-773)
    if(homedir == null) {
      final String home = WIN ? null : System.getenv("HOME");
      homedir = dir(home != null ? home : System.getProperty("user.home")) + PROJECT;
    }
    HOMEDIR = dir(homedir);
  }

  // STATIC OPTIONS ===============================================================================

  /** Language (applied after restart). */
  public static String language = "English";
  /** Flag for prefixing texts with their keys (helps while translating texts). */
  public static boolean langkeys;
  /** Rendering orientation (right vs. left). */
  public static boolean langright;
  /** Debug mode. */
  public static boolean debug;
  /** GUI mode. */
  public static boolean gui;

  /** Private constructor. */
  private Prop() { }

  // STATIC METHODS ===============================================================================

  /**
   * Checks if one of the files .basexhome or .basex are found in the specified directory.
   * @param dir directory (can be {@code null})
   * @return configuration directory (can be {@code null})
   */
  private static String configDir(final String dir) {
    if(dir != null) {
      final String home = IO.BASEXSUFFIX + "home";
      final IOFile file = new IOFile(dir, home);
      if(file.exists() || new IOFile(dir, IO.BASEXSUFFIX).exists()) return dir;
    }
    return null;
  }

  /**
   * Returns the application directory.
   * @param location location of application
   * @return application directory (can be {@code null})
   */
  private static String applicationDir(final URL location) {
    try {
      if(location != null) return new IOFile(Paths.get(location.toURI()).toString()).dir();
    } catch(final Exception ex) {
      Util.stack(ex);
    }
    return null;
  }

  /**
   * Attaches a directory separator to the specified directory string.
   * @param path directory path
   * @return directory string
   */
  private static String dir(final String path) {
    return path.isEmpty() || Strings.endsWith(path, '/') || Strings.endsWith(path, '\\') ?
      path : path + File.separator;
  }

  /**
   * Retrieves the location of the application.
   * @return application URL, or {@code null} if not available
   */
  private static URL location() {
    final ProtectionDomain pd = Prop.class.getProtectionDomain();
    if(pd != null) {
      final CodeSource cs = pd.getCodeSource();
      if(cs != null) return cs.getLocation();
    }
    return null;
  }

  /**
   * Returns a build version string using data from the JAR manifest.
   * @param devVersion version used during development;
   *        returned if there is no implementation version or no manifest
   * @return version string
   */
  private static String version(final String devVersion) {
    final String version = JarManifest.get(Attributes.Name.IMPLEMENTATION_VERSION);
    if(version == null) return devVersion;
    if(!version.contains("-SNAPSHOT")) return version;

    final StringBuilder result = new StringBuilder(version.replace("-SNAPSHOT", " beta"));
    final Object revision = JarManifest.get("Implementation-Build");
    if(revision != null) result.append(' ').append(revision);
    return result.toString();
  }

  /**
   * Sets a global option.
   * @param option option
   * @param value value
   */
  public static void put(final Option<?> option, final String value) {
    put(key(option), value);
  }

  /**
   * Sets a global option.
   * @param name name of the option
   * @param value value
   */
  public static void put(final String name, final String value) {
    OPTIONS.put(name, value);
  }

  /**
   * Removes all global options.
   */
  public static void clear() {
    OPTIONS.clear();
  }

  /**
   * Returns a system property or global option. System properties override global options.
   * @param name name of the option
   * @return global option
   */
  public static String get(final String name) {
    final String value = System.getProperty(name);
    return value != null ? value : OPTIONS.get(name);
  }

  /**
   * Returns all global options and system properties.
   * System properties override global options.
   * @return entry set
   */
  public static Set<Entry<String, String>> entries() {
    // properties from starter classes and web.xml context parameters
    final HashMap<String, String> entries = new HashMap<>(OPTIONS);
    // override with system properties
    System.getProperties().forEach((key, value) -> entries.put(key.toString(), value.toString()));
    return entries.entrySet();
  }

  /**
   * Sets a system property if it has not been set before.
   * @param name name of the property
   * @param value value
   */
  public static void setSystem(final String name, final String value) {
    if(System.getProperty(name) == null) System.setProperty(name, value);
  }

  /**
   * Returns the key of an option. The returned key is prefixed with {@link #DBPREFIX}.
   * @param option option
   * @return key
   */
  private static String key(final Option<?> option) {
    return DBPREFIX + option.name().toLowerCase(Locale.ENGLISH);
  }
}
