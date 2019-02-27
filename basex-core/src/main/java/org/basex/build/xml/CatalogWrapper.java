package org.basex.build.xml;

import static org.basex.util.Reflect.*;

import java.lang.reflect.*;

import org.xml.sax.*;

/**
 * Wraps the CatalogResolver object.
 * Searches for presence of one of the xml resolver packages
 * {@code org.apache.xml.resolver.tools.CatalogResolver} or
 * {@code code com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver}.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Michael Seiferle
 */
public final class CatalogWrapper {
  /** Package declaration for CatalogManager. */
  private static final Class<?> CMP = find(new String[] {
    "org.apache.xml.resolver.CatalogManager",
    "com.sun.org.apache.xml.internal.resolver.CatalogManager" });
  /** Package declaration for CatalogResolver constructor. */
  private static final Constructor<?> CRP = find(find(new String[] {
    "org.apache.xml.resolver.tools.CatalogResolver",
    "com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver" }), CMP);
  /** Instance of catalog manager. */
  private static final Object CM = get(CMP);

  /** Hidden constructor. */
  private CatalogWrapper() { }

  /**
   * Checks if a CatalogResolver is available.
   * @return result of check
   */
  public static boolean available() {
    return CM != null;
  }

  /**
   * Returns the resolver, which could be of any class that implements the
   * CatalogManager interface.
   */
  public static Object getCM() {
    return get(CRP, CM);
  }

  public static void setDefaults(final String path) {
    if(CM == null) return;

    // IgnoreMissingProperties - default is to print a warning if properties
    // are unset; this is not usually what we want, but can be overridden
    // for debugging. Not all resolvers produce errors even if this is false,
    // so better to set it to true.
    invoke(method(CMP, "setIgnoreMissingProperties", boolean.class), CM, true);

    // CatalogFiles - semicolon-separated list of files
    invoke(method(CMP, "setCatalogFiles", String.class), CM, path);

    // StaticCatalog:
    // If this manager uses static catalogs, the same static catalog will
    // always be returned.   Otherwise a new catalog will be returned.
    // We would probably get better performance by using true here. You get a
    // new catalogmanager instance if you change PATH.
    invoke(method(CMP, "setUseStaticCatalog", boolean.class), CM, false);

    // You can also set Verbosity, but that's best left for the properties file,
    // CatalogManager.propertie,  or system property, to help debugging.
    // The higher the number, the more messages.
    // NOTE messages go to output, not err stream!
    // invoke(method(CMP, "setVerbosity", int.class), CM, 0);

  }

  /**
   * Decorates the {@link XMLReader} with the catalog resolver if it is found in the classpath.
   * Does nothing otherwise.
   * @param reader XML reader
   * @param path path to catalog file
   */
  static void set(final XMLReader reader, final String path) {
    if(CM == null) return;

    setDefaults(path);

    reader.setEntityResolver((EntityResolver) get(CRP, CM));
  }
}
