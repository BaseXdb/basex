package org.basex.build.xml;

import static org.basex.util.Reflect.*;

import java.lang.reflect.*;

import javax.xml.transform.*;

import org.basex.util.*;
import org.xml.sax.*;

/**
 * Wraps the CatalogResolver object.
 * Searches for presence of one of the XML resolver packages
 * {@code org.apache.xml.resolver.tools.CatalogResolver} or
 * {@code code com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver}.
 *
 * The catalog manager is part of Java 9; in future, we can possibly drop this class.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Michael Seiferle
 * @author Liam Quin
 */
public final class CatalogWrapper {
  /** Package declaration for CatalogManager. */
  private static final Class<?> MANAGER;
  /** Package declaration for CatalogResolver constructor. */
  private static final Constructor<?> RESOLVER;

  static {
    // try to locate catalog manager from xml-resolver-1.2.jar library
    Class<?> manager = find("org.apache.xml.resolver.CatalogManager"), resolver;
    if(manager != null) {
      resolver = find("org.apache.xml.resolver.tools.CatalogResolver");
    } else {
      // try to resort to internal catalog manager
      manager = find("com.sun.org.apache.xml.internal.resolver.CatalogManager");
      resolver = find("com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver");
    }
    MANAGER = manager;
    RESOLVER = find(resolver, manager);
  }

  /** Instance of catalog manager. */
  private final Object cm = Reflect.get(MANAGER);

  /**
   * Hidden constructor.
   * @param paths semicolon-separated list of catalog files
   */
  private CatalogWrapper(final String paths) {
    invoke(method(MANAGER, "setCatalogFiles", String.class), cm, paths);
  }

  /**
   * Returns an instance of the catalog wrapper.
   * @param paths semicolon-separated list of catalog files
   * @return instance, or {@code null} if no catalog manager is available or if the list is empty
   */
  public static CatalogWrapper get(final String paths) {
    return available() && !paths.isEmpty() ? new CatalogWrapper(paths) : null;
  }

  /**
   * Checks if the catalog manager is available.
   * @return result of check
   */
  public static boolean available() {
    return MANAGER != null;
  }

  /**
   * Returns a URI resolver.
   * @return URI resolver
   */
  public URIResolver getURIResolver() {
    return (URIResolver) Reflect.get(RESOLVER, cm);
  }

  /**
   * Returns an entity resolver.
   * @return entity resolver
   */
  public EntityResolver getEntityResolver() {
    return (EntityResolver) Reflect.get(RESOLVER, cm);
  }
}
