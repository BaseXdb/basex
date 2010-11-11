package org.basex.build.xml;

import static org.basex.util.Reflect.*;
import java.lang.reflect.Constructor;
import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;

/**
 * Wraps the CatalogResolver Object.
 * Searches for presence of one of the xml resolver packages
 * {@code org.apache.xml.resolver.tools.CatalogResolver} or
 * {@code code com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class CatalogResolverWrapper {
  /** Package declaration for CatalogManager. */
  private static final Class<?> CMP = find(
    "org.apache.xml.resolver.CatalogManager",
    "com.sun.org.apache.xml.internal.resolver.CatalogManager");
  /** Package declaration for CatalogResolver. */
  private static final Constructor<?> CRP = find(find(
    "org.apache.xml.resolver.tools.CatalogResolver",
    "com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver"), CMP);
  /** Instance of catalog manager. */
  private static final Object CM = get(CMP);

  /** Hidden constructor. */
  private CatalogResolverWrapper() { }

  /**
   * Returns a CatalogResolver instance or a {@code null} reference.
   * @return CatalogResolver if available
   */
  public static boolean available() {
    return CM != null;
  }

  /**
   * Decorates the XMLReader with the catalog resolver if it has been found on
   * the classpath. Does nothing otherwise.
   * @param r XMLReader
   * @param cat path.
   */
  public static void set(final XMLReader r, final String cat) {
    if(CM == null) return;
    invoke(find(CMP, "setIgnoreMissingProperties", boolean.class), CM, true);
    invoke(find(CMP, "setCatalogFiles", String.class), CM, cat);
    invoke(find(CMP, "setPreferPublic", boolean.class), CM, true);
    invoke(find(CMP, "setUseStaticCatalog", boolean.class), CM, false);
    invoke(find(CMP, "setVerbosity", int.class), CM, 0);
    r.setEntityResolver((EntityResolver) get(CRP, CM));
  }
}
