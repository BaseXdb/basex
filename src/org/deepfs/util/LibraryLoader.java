package org.deepfs.util;

import java.util.HashMap;
import org.basex.core.Main;

/**
 * Utility class to load shared libraries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public final class LibraryLoader {
  /** Name of spotlight extractor library. */
  public static final String SPOTEXLIBNAME = "deepfs_spotex";
  /** Name of joint storage library. */
  public static final String JSDBFSLIBNAME = "deepfs_jsdbfs";
  /** Name of FUSE java bindings library. */
  public static final String JFUSELIBNAME = "jfuse";
  /** Name of DeepFUSE library. */
  public static final String DEEPFUSELIBNAME = "deepfuse";
  /** Map with loaded libraries. */
  private static final HashMap<String, Boolean> LIBS =
    new HashMap<String, Boolean>();

  /** Private constructor, preventing instantiation. */
  private LibraryLoader() { }

  /**
   * Loads native library if not already present.
   * @param lib name of the library to be loaded.
   * @return true on success 
   */
  public static boolean load(final String lib) {
    final Boolean b = LIBS.get(lib);
    return b != null ? b : loadLibrary(lib);
  }

  /**
   * Checks if a library is loaded.
   * @param lib name of the library.
   * @return true if the library is loaded, false otherwise.
   */
  public static boolean isLoaded(final String lib) {
    final Boolean b = LIBS.get(lib);
    return b != null ? b : false;
  }
  
  /**
   * Loads a native library from java.library.path.
   * @param lib name of the library to be loaded.
   * @return true on success
   */
  private static boolean loadLibrary(final String lib) {
    boolean found = LIBS.containsKey(lib);
    if(found) return false;
    try {
      System.loadLibrary(lib);
      Main.debug("Loading library (" + lib + ") ... OK.");
    } catch(final UnsatisfiedLinkError ex) {
      found = false;
    }
    LIBS.put(lib, found);
    return found;
  }
}
