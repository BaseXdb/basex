package org.basex.util;

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

  /** Spotlight library presence flag. */
  private static boolean spotexLoaded;
  /** Joint storage library presence flag. */
  private static boolean jsdbfsLoaded;
  /** jFUSE bindings presence flag. */
  private static boolean jfuseLoaded;
  /** DeepFUSE presence flag. */
  private static boolean deepfuseLoaded;

  /**
   * Loads a native library from java.library.path.
   * @param libName name of the library to be loaded.
   * @return true on success
   */
  private static boolean loadLibrary(final String libName) {
    try {
      System.loadLibrary(libName);
      Main.debug("Loading library (" + libName + ") ... OK.");

      if(libName.equals(SPOTEXLIBNAME)) {
        spotexLoaded = true;
        return true;
      }
      if(libName.equals(JSDBFSLIBNAME)) {
        jsdbfsLoaded = true;
        return true;
      }
      if(libName.equals(JFUSELIBNAME)) {
        jfuseLoaded = true;
        return true;
      }
      if(libName.equals(DEEPFUSELIBNAME)) {
        deepfuseLoaded = true;
        return true;
      }
      return false;
    } catch(final UnsatisfiedLinkError ex) {
//      Main.debug("Loading library (" + libName + ") ... FAILED.\n" + ex);
//      Main.debug("-Djava.library.path is : '"
//          + System.getProperty("java.library.path") + "'");
      return false;
    }
  }

  /**
   * Loads native library if not already present.
   * @param libName name of the library to be loaded.
   * @return true on success 
   */
  public static boolean load(final String libName) {
    if(libName.equals(SPOTEXLIBNAME) && spotexLoaded) return true;
    if(libName.equals(JSDBFSLIBNAME) && jsdbfsLoaded) return true;
    if(libName.equals(JFUSELIBNAME) && jfuseLoaded) return true;
    if(libName.equals(JFUSELIBNAME) && deepfuseLoaded) return true;

    return loadLibrary(libName);
  }

  /**
   * Checks if a library is loaded.
   * @param libName name of the library.
   * @return true if the library is loaded, false otherwise.
   */
  public static boolean isLoaded(final String libName) {
    if(libName.equals(SPOTEXLIBNAME)) return spotexLoaded;
    if(libName.equals(JSDBFSLIBNAME)) return jsdbfsLoaded;
    if(libName.equals(JFUSELIBNAME)) return jfuseLoaded;
    if(libName.equals(DEEPFUSELIBNAME)) return deepfuseLoaded;
    return false;
  }

  /** Default constructor disabled. */
  protected LibraryLoader() {
    throw new UnsupportedOperationException();
  }
}
