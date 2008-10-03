package org.basex.api.xmldb;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.values.Item;
import org.basex.query.xquery.XQueryProcessor;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;

/**
 * Implementation of the XPathQueryService Interface for the XMLDB:API.
 * XQuery-Version
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXQueryQueryService implements XPathQueryService {
  /** Context. */
  Context ctx;

  /**
   * Standard constructor.
   * @param c for Context
   */
  public BXXQueryQueryService(final Context c) {
    ctx = c;
  }

  public void clearNamespaces() {
    BaseX.notimplemented();
  }

  public String getName() {
    BaseX.notimplemented();
    return null;
  }

  public String getNamespace(final String prefix) {
    BaseX.notimplemented();
    return null;
  }

  public String getProperty(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public String getVersion() {
    BaseX.notimplemented();
    return null;
  }

  public ResourceSet query(final String query) {
    // Creates a query instance
    final QueryProcessor xquery = new XQueryProcessor(query);
    // Start a query with the default context set (root node).
    final Nodes nodes = ctx.current();
    // Executes the query
    try {
      final Item result = (Item) xquery.query(nodes);
      return new BXResourceSet(result);
    } catch(final QueryException qe) {
      System.out.println(qe);
    }
    return null;
  }

  public ResourceSet queryResource(final String id, final String query) {
    BaseX.notimplemented();
    return null;
  }

  public void removeNamespace(final String prefix) {
    BaseX.notimplemented();
  }

  public void setCollection(final Collection col) {
    BaseX.notimplemented();
  }

  public void setNamespace(final String prefix, final String uri) {
    BaseX.notimplemented();
  }

  public void setProperty(final String name, final String value) {
    BaseX.notimplemented();
  }
}
