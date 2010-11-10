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
  /** Resolver if availiable. */
  private static Object cm;

  static {
    try {
      cm = Class.
        forName("org.apache.xml.resolver.CatalogManager").newInstance();
    } catch(Exception e) { }

  }

  /** private Constructor, no instantiation. */
  private CatalogResolverWrapper() {

  }

  /**
   * Returns a CatalogResolver instance or null if it could not be found.
   * @return CatalogResolver if availiable
   */
  public static Object getInstance() {
    return cm;
  }

  /**
   * Decorates the XMLReader with the catalog resolver 
   * if it has been found on the classpath. Does nothing otherwise.
   * @param r XMLReader
   * @param cat path.
   */
  public static void set(final XMLReader r, final String cat)  {
    if(null == cm) return;
      try {
        Class<?> clazz = Class.
          forName("org.apache.xml.resolver.CatalogManager");
        Method m = clazz.getMethod("setCatalogFiles", String.class);
        m.invoke(cm, cat);
        m = clazz.getMethod("setIgnoreMissingProperties", boolean.class);
        m.invoke(cm, true);
        m = clazz.getMethod("setPreferPublic", boolean.class);
        m.invoke(cm, true);
        m = clazz.getMethod("setUseStaticCatalog", boolean.class);
        m.invoke(cm, false);
        m = clazz.getMethod("setVerbosity", int.class);
        m.invoke(cm, 0);
        r.setEntityResolver((EntityResolver) Class.forName(
            "org.apache.xml.resolver.tools.CatalogResolver").getConstructor(
            new Class[] {
                Class.forName("org.apache.xml.resolver.CatalogManager")}).
                newInstance(cm));

      } catch(Exception e) { }
  }

}
