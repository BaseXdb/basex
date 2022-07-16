package org.basex.util;

import java.net.*;

import javax.xml.catalog.*;
import javax.xml.transform.*;

import org.basex.core.*;
import org.basex.io.*;
import org.w3c.dom.ls.*;
import org.xml.sax.*;

/**
 * Catalog resolver: Convenience methods for retrieving resolver instances of the standard
 * JDK 11 implementation or Norman Walsh’s enhanced XML resolver.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Resolver {
  /** Path to enhanced XML resolver. */
  private static final String RESOLVER = "org.xmlresolver.Resolver";
  /** Path to enhanced XML resolver configuration. */
  private static final String CONFIGURATION = "org.xmlresolver.XMLResolverConfiguration";

  /** Private Constructor. */
  private Resolver() { }

  /**
   * Returns an entity resolver.
   * @param mopts main options
   * @return entity resolver (can be {@code null})
   */
  public static EntityResolver entities(final MainOptions mopts) {
    return (EntityResolver) resolver(mopts);
  }

  /**
   * Returns a URI resolver.
   * @param mopts main options
   * @return URI resolver (can be {@code null})
   */
  public static URIResolver uris(final MainOptions mopts) {
    return (URIResolver) resolver(mopts);
  }

  /**
   * Returns a resource resolver.
   * @param mopts main options
   * @return resource resolver (can be {@code null})
   */
  public static LSResourceResolver resources(final MainOptions mopts) {
    return (LSResourceResolver) resolver(mopts);
  }

  /**
   * Returns a catalog resolver.
   * @param mopts main options
   * @return catalog resolver (can be {@code null})
   */
  private static Object resolver(final MainOptions mopts) {
    final String catalog = mopts.get(MainOptions.CATALOG);
    if(!catalog.isEmpty()) {
      if(Reflect.available(RESOLVER)) {
        // return enhanced XML resolver
        final Class<?> resolver = Reflect.find(RESOLVER);
        final Class<?> configuration = Reflect.find(CONFIGURATION);
        final Object conf = Reflect.get(Reflect.find(configuration, String.class), catalog);
        return Reflect.get(Reflect.find(resolver, configuration), conf);
      }
      // return JDK 11 resolver
      final URI uri = URI.create(IO.get(catalog).url());
      final CatalogFeatures cf = CatalogFeatures.defaults();
      return CatalogManager.catalogResolver(CatalogManager.catalog(cf, uri));
    }
    return null;
  }
}
