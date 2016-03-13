package org.basex.util;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.options.*;

/**
 * This class contains constants and system properties which are used all around the project.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Prop {
  /** Global options, assigned by the starter classes and the web.xml context parameters. */
  private static final Map<String, String> OPTIONS = new ConcurrentHashMap<>();

  /** User's home directory. */
  public static final String USERHOME;
  /** Application URL. */
  public static final URL LOCATION;

  static {
    // linux: check HOME variable (#773)
    final String home = System.getenv("HOME");
    USERHOME = dir(home != null ? home : System.getProperty("user.home"));

    // retrieve application URL
    URL loc = null;
    final ProtectionDomain pd = MainOptions.class.getProtectionDomain();
    if(pd != null) {
      final CodeSource cs = pd.getCodeSource();
      if(cs != null) loc = cs.getLocation();
    }
    LOCATION = loc;
  }

  /** Project name. */
  public static final String NAME = "BaseX";
  /** Code version (may contain major, minor and optional patch number). */
  public static final String VERSION = version("8.4.2 beta");
  /** Main author. */
  public static final String AUTHOR = "Christian Gr\u00FCn";
  /** Co-authors (1). */
  public static final String TEAM1 = "Alexander Holupirek, Michael Seiferle";
  /** Co-authors (2). */
  public static final String TEAM2 = "Lukas Kircher, Leo W\u00F6rteler";
  /** Entity. */
  public static final String ENTITY = NAME + " Team";
  /** Project namespace. */
  public static final String PROJECT_NAME = NAME.toLowerCase(Locale.ENGLISH);
  /** URL. */
  public static final String URL = "http://" + PROJECT_NAME + ".org";
  /** URL of the community page. */
  public static final String COMMUNITY_URL = URL + "/community";
  /** URL of the documentation. */
  public static final String DOC_URL = "http://docs." + PROJECT_NAME + ".org";
  /** URL of the update page. */
  public static final String UPDATE_URL = URL + "/products/download/all-downloads/";
  /** Version URL. */
  public static final String VERSION_URL = "http://files." + PROJECT_NAME + ".org/version.txt";
  /** Repository URL. */
  public static final String REPO_URL = "http://files." + PROJECT_NAME + ".org/modules";
  /** Mail. */
  public static final String MAILING_LIST = PROJECT_NAME + "-talk@mailman.uni-konstanz.de";
  /** Title and version. */
  public static final String TITLE = NAME + ' ' + VERSION;

  /** System-specific newline string. */
  public static final String NL = System.getProperty("line.separator");
  /** Returns the system's default encoding. */
  public static final String ENCODING = System.getProperty("file.encoding");

  /** System's temporary directory. */
  public static final String TMP = dir(System.getProperty("java.io.tmpdir"));

  /** OS flag (source: {@code http://lopica.sourceforge.net/os.html}). */
  private static final String OS = System.getProperty("os.name");
  /** Flag denoting if OS belongs to Mac family. */
  public static final boolean MAC = OS.startsWith("Mac");
  /** Flag denoting if OS belongs to Windows family. */
  public static final boolean WIN = OS.startsWith("Windows");
  /** Respect lower/upper case when doing file comparisons. */
  public static final boolean CASE = !(MAC || WIN);

  /** Prefix for project specific options. */
  public static final String DBPREFIX = "org.basex.";
  /** System property for specifying database home directory. */
  public static final String PATH = DBPREFIX + "path";

  /** Directory for storing the property files, database directory, etc. */
  public static final String HOME = dir(homeDir());

  // STATIC OPTIONS =====================================================================

  /** Language (applied after restart). */
  public static String language = "English";
  /** Flag for prefixing texts with their keys (helps while translating texts). */
  public static boolean langkeys;
  /** Language direction (right vs. left). */
  public static boolean langright;
  /** Debug mode. */
  public static boolean debug;
  /** GUI mode. */
  public static boolean gui;

  /** Private constructor. */
  private Prop() { }

  // STATIC METHODS =====================================================================

  /**
   * <p>Determines the project's home directory for storing property files
   * and directories. The directory is chosen as follows:</p>
   * <ol>
   *   <li> First, the <b>system property</b> {@code "org.basex.path"} is checked.
   *        If it contains a value, it is adopted as home directory.</li>
   *   <li> If not, the <b>current working directory</b> (defined by the system
   *        property {@code "user.dir"}) is chosen if the file {@code .basex} or
   *        {@code .basexhome} is found in this directory.</li>
   *   <li> Otherwise, the files are searched in the <b>application directory</b>
   *        (the folder in which the application code is located).</li>
   *   <li> Otherwise, the <b>user's home directory</b> (defined in
   *        {@code "user.home"}) is chosen.</li>
   * </ol>
   * @return path to home directory
   */
  private static String homeDir() {
    // check for system property
    String dir = System.getProperty(PATH);
    if(dir != null) return dir;

    // not found; check working directory for property file
    dir = System.getProperty("user.dir");
    final String home = IO.BASEXSUFFIX + "home";
    IOFile file = new IOFile(dir, home);
    if(!file.exists()) file = new IOFile(dir, IO.BASEXSUFFIX);
    if(file.exists()) return file.dir();

    // not found; check application directory
    if(LOCATION != null) {
      try {
        dir = new IOFile(Paths.get(LOCATION.toURI()).toString()).dir();
        file = new IOFile(dir, home);
        if(!file.exists()) file = new IOFile(dir, IO.BASEXSUFFIX);
        if(file.exists()) return file.dir();
      } catch(final Exception ex) {
        Util.stack(ex);
      }
    }

    // not found; choose user home directory as default
    return USERHOME;
  }

  /**
   * Attaches a directory separator to the specified directory string.
   * @param dir input string
   * @return directory string
   */
  private static String dir(final String dir) {
    return dir.endsWith("\\") || dir.endsWith("/") ? dir : dir + File.separator;
  }

  /**
   * Build version string using data from the JAR manifest.
   * @param devVersion version used during development;
   *        returned if there is no implementation version or no manifest.
   * @return version string
   */
  private static String version(final String devVersion) {
    final String version = Prop.class.getPackage().getImplementationVersion();
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
   * Removes a global option.
   * @param option option
   */
  public static void remove(final Option<?> option) {
    remove(key(option));
  }

  /**
   * Removes a global option.
   * @param name name of the option
   */
  public static void remove(final String name) {
    OPTIONS.remove(name);
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
   * Returns a system property or global option. System properties override global options.
   * @param option option
   * @return value, or {@code null}
   */
  public static String get(final Option<?> option) {
    return get(key(option));
  }

  /**
   * Returns all global options and system properties.
   * System properties override global options.
   * @return entry set
   */
  public static Set<Entry<String, String>> entries() {
    // properties from starter classes and web.xml context parameters
    final HashMap<String, String> entries = new HashMap<>();
    entries.putAll(OPTIONS);
    // override with system properties
    for(final Entry<Object, Object> entry : System.getProperties().entrySet()) {
      entries.put(entry.getKey().toString(), entry.getValue().toString());
    }
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
