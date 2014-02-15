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
 * @author BaseX Team 2005-14, BSD License
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
   * Decorates the {@link XMLReader} with the catalog resolver if it is found
   * in the classpath. Does nothing otherwise.
   * @param reader XML reader
   * @param cat path to catalog file
   */
  public static void set(final XMLReader reader, final String cat) {
    if(CM == null) return;
    invoke(method(CMP, "setIgnoreMissingProperties", boolean.class), CM, true);
    invoke(method(CMP, "setCatalogFiles", String.class), CM, cat);
    invoke(method(CMP, "setPreferPublic", boolean.class), CM, true);
    invoke(method(CMP, "setUseStaticCatalog", boolean.class), CM, false);
    invoke(method(CMP, "setVerbosity", int.class), CM, 0);
    reader.setEntityResolver((EntityResolver) get(CRP, CM));
  }
}
