package org.basex.util;

import java.net.*;
import java.util.*;

import javax.xml.catalog.*;
import javax.xml.transform.*;

import org.basex.core.*;
import org.basex.io.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * Class with convenience methods for retrieving resolver instances of the standard
 * JDK 11 implementation or Norman Walsh’s enhanced XML resolver.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLResolver {
  /** Path to enhanced XML resolver. */
  private static final String RESOLVER = "org.xmlresolver.Resolver";
  /** Path to enhanced XML resolver configuration. */
  private static final String CONFIGURATION = "org.xmlresolver.XMLResolverConfiguration";

  /** Catalog string. */
  private final String catalog;
  /** Resolver data (catalog string or resolver instance). */
  private Object resolver;

  /**
   * Constructor.
   * @param resolver resolver instance (catalog string or resolver instance)
   * @throws BaseXException database exception
   */
  public XMLResolver(final Object resolver) throws BaseXException {
    final Class<?> rclzz = resolver.getClass();
    final Class<?>[] clzzs = { EntityResolver.class, LSResourceResolver.class, URIResolver.class };
    for(final Class<?> clzz : clzzs) {
      if(!clzz.isAssignableFrom(rclzz)) {
        throw new BaseXException("% does not implement the interface %", resolver, clzz);
      }
    }
    this.resolver = resolver;
    catalog = "";
  }

  /**
   * Constructor.
   * @param catalog catalog string
   */
  public XMLResolver(final String catalog) {
    this.catalog = catalog;
  }

  /**
   * Returns the catalog string.
   * @return catalog string
   */
  public String catalog() {
    return catalog;
  }

  /**
   * Indicates if this is a standard resolver.
   * @return result of check
   */
  public boolean standard() {
    return resolver == null && catalog.isEmpty();
  }

  /**
   * Returns an entity resolver.
   * @return entity resolver (can be {@code null})
   */
  public EntityResolver entityResolver() {
    return (EntityResolver) resolver();
  }

  /**
   * Returns a URI resolver.
   * @return URI resolver (can be {@code null})
   */
  public URIResolver uriResolver() {
    return (URIResolver) resolver();
  }

  /**
   * Returns a resource resolver.
   * @return resource resolver (can be {@code null})
   */
  public LSResourceResolver lsResourceResolver() {
    return (LSResourceResolver) resolver();
  }

  /**
   * Returns a resolver.
   * @return catalog resolver (can be {@code null})
   */
  private Object resolver() {
    if(resolver == null) {
      if(!catalog.isEmpty()) {
        if(Reflect.available(RESOLVER)) {
          // instance of Norm’s enhanced XML resolver
          final Class<?> rslvr = Reflect.find(RESOLVER);
          final Class<?> cnfgrtn = Reflect.find(CONFIGURATION);
          final Object cnf = Reflect.get(Reflect.find(cnfgrtn, String.class), catalog);
          resolver = Reflect.get(Reflect.find(rslvr, cnfgrtn), cnf);
        } else {
          // JDK 11 resolver
          final ArrayList<URI> uris = new ArrayList<>();
          for(final String cat : Strings.split(catalog, ';')) {
            uris.add(URI.create(IO.get(cat).url()));
          }
          final CatalogFeatures cf = CatalogFeatures.defaults();
          final Catalog cat = CatalogManager.catalog(cf, uris.toArray(URI[]::new));
          resolver = CatalogManager.catalogResolver(cat);
        }
      }
    }
    return resolver;
  }
}
