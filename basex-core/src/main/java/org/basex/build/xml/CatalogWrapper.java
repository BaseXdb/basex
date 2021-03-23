package org.basex.build.xml;

import static org.basex.util.Reflect.*;

import java.io.*;
import java.lang.reflect.*;

import javax.xml.transform.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * Wraps the CatalogResolver object.
 * Searches for presence of one of the XML resolver packages
 * {@code org.apache.xml.resolver.tools.CatalogResolver} or
 * {@code code com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver}.
 *
 * The catalog manager is part of Java 9; in future, we can possibly drop this class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Michael Seiferle
 * @author Liam Quin
 */
public final class CatalogWrapper {
  /** Package declaration for CatalogManager. */
  private static final Class<?> MANAGER;
  /** Package declaration for CatalogResolver constructor. */
  private static final Constructor<?> RESOLVER;
  /** Package declaration for XMLCatalogResolver constructor. */
  private static final Constructor<?> XMLRESOLVER;

  static {
    // try to locate catalog manager from xml-resolver-1.2.jar library
    Class<?> manager = find("org.apache.xml.resolver.CatalogManager"), resolver, ls;
    if(manager != null) {
      resolver = find("org.apache.xml.resolver.tools.CatalogResolver");
    } else {
      // try to resort to internal catalog manager
      manager = find("com.sun.org.apache.xml.internal.resolver.CatalogManager");
      resolver = find("com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver");
    }
    MANAGER = manager;
    RESOLVER = find(resolver, manager);

    ls = find("com.sun.org.apache.xerces.internal.util.XMLCatalogResolver");
    XMLRESOLVER = find(ls, String[].class);
  }

  /**
   * Hidden constructor.
   */
  private CatalogWrapper() { }

  /**
   * Checks if the catalog manager is available.
   * @return result of check
   */
  public static boolean available() {
    return MANAGER != null;
  }

  /**
   * Returns a URI resolver.
   * @param options database options
   * @return URI resolver
   */
  public static URIResolver getURIResolver(final MainOptions options) {
    final Object cm = cm(options);
    return cm != null ? (URIResolver) Reflect.get(RESOLVER, cm) : null;
  }

  /**
   * Returns an entity resolver.
   * @param options database options
   * @return entity resolver
   */
  public static EntityResolver getEntityResolver(final MainOptions options) {
    final Object cm = cm(options);
    return cm != null ? (EntityResolver) Reflect.get(RESOLVER, cm) : null;
  }

  /**
   * Returns an instance of the catalog wrapper.
   * @param options database options
   * @return instance, or {@code null} if no catalog manager is available or if the list is empty
   */
  public static LSResourceResolver getLSResourceResolver(final MainOptions options) {
    final String catfile = options.get(MainOptions.CATFILE);
    if(XMLRESOLVER != null && !catfile.isEmpty()) {
      final StringList catalogs = new StringList();
      for(String path : catfile.split(";")) catalogs.add(new File(path).getAbsolutePath());
      return (LSResourceResolver) Reflect.get(XMLRESOLVER, new Object[] { catalogs.finish() });
    }
    return null;
  }

  /**
   * Returns an instance of the catalog wrapper.
   * @param options database options
   * @return instance, or {@code null} if no catalog manager is available or if the list is empty
   */
  private static Object cm(final MainOptions options) {
    final String catfile = options.get(MainOptions.CATFILE);
    if(MANAGER != null && !catfile.isEmpty()) {
      final Object cm = Reflect.get(MANAGER);
      if(System.getProperty("xml.catalog.ignoreMissing") == null) {
        invoke(method(MANAGER, "setIgnoreMissingProperties", boolean.class), cm, true);
      }
      invoke(method(MANAGER, "setCatalogFiles", String.class), cm, catfile);
      return cm;
    }
    return null;
  }
}
