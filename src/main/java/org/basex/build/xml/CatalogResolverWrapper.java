package org.basex.build.xml;

import java.lang.reflect.Method;

import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;

/**
 * Wraps the CatalogResolver Object.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class CatalogResolverWrapper {
  /** Package declaration for either internal or external CatalogManager. */
  private static final String CMP = cminitpackage();
  /** Package declaration for either internal or external CatalogResolver. */
  private static final String CRP = crinitpackage();

  /** Resolver if availiable. */
  private static final Object CM = init();

  /** private Constructor, no instantiation. */
  private CatalogResolverWrapper() {}

  /**
   * Searches for presence of the xml resolver packages.
   * com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver
   * org.apache.xml.resolver.tools.CatalogResolver
   * @return found package or default (internal) package on unsupported
   *         platforms.
   */
  private static String crinitpackage() {
    try {
      Class.forName("org.apache.xml.resolver.tools.CatalogResolver");
      return "org.apache.xml.resolver.tools.CatalogResolver";
    } catch(Exception e) {
      try {
        Class.forName("com.sun.org.apache.xml.internal."
            + "resolver.tools.CatalogResolver");
        return "com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver";
      } catch(Exception e2) {

      }
    }

    return "com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver";
  }

  /**
   * Searches for presence of the xml resolver packages.
   * org.apache.xml.resolver.tools.CatalogManager
   * com.sun.org.apache.xml.internal.resolver.CatalogManager
   * @return found package or default (internal) package on unsupported
   *         platforms.
   */
  private static String cminitpackage() {
    // TODO Auto-generated method stub
    // "com.sun.org.apache.xml.internal.resolver.CatalogManager";
    try {
      Class.forName("org.apache.xml.resolver.CatalogManager");
      return "org.apache.xml.resolver.CatalogManager";
    } catch(Exception e) {
      try {
        Class.forName("com.sun.org.apache.xml.internal."
            + "resolver.CatalogManager");
        return "com.sun.org.apache.xml.internal.resolver.CatalogManager";
      } catch(Exception e2) {

      }
    }
    return "com.sun.org.apache.xml.internal.resolver.CatalogManager";
  }

  /**
   * Initializes the CatalogManager.
   * @return CatalogManager instance iff found.
   */
  private static Object init() {
    Object cmm = null;
    try {
      cmm = Class.forName(CMP).newInstance();
    } catch(Exception e) { }
    return cmm;
  }

  /**
   * Returns a CatalogResolver instance or null if it could not be found.
   * @return CatalogResolver if availiable
   */
  public static Object getInstance() {
    return CM;
  }

  /**
   * Decorates the XMLReader with the catalog resolver if it has been found on
   * the classpath. Does nothing otherwise.
   * @param r XMLReader
   * @param cat path.
   */
  public static void set(final XMLReader r, final String cat) {
    if(null == CM) return;
    try {
      Class<?> clazz = Class.forName(CMP);
      Method m = clazz.getMethod("setCatalogFiles", String.class);
      m.invoke(CM, cat);
      m = clazz.getMethod("setIgnoreMissingProperties", boolean.class);
      m.invoke(CM, true);
      m = clazz.getMethod("setPreferPublic", boolean.class);
      m.invoke(CM, true);
      m = clazz.getMethod("setUseStaticCatalog", boolean.class);
      m.invoke(CM, false);
      m = clazz.getMethod("setVerbosity", int.class);
      m.invoke(CM, 0);
      r.setEntityResolver((EntityResolver) Class.forName(CRP).getConstructor(
          new Class[] { Class.forName(CMP)}).newInstance(CM));

    } catch(Exception e) { }
  }

}
