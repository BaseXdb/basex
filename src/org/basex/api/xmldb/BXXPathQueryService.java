package org.basex.api.xmldb;

import org.basex.core.Context;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.values.Item;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;

/**
 * Implementation of the Service Interface for the XMLDB:API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXPathQueryService implements XPathQueryService {
  /** Context. */
  Context ctx;

  /**
   * Standard constructor.
   * @param c for Context
   */
  public BXXPathQueryService(final Context c) {
    ctx = c;
  }

  public void clearNamespaces() {
    // TODO Auto-generated method stub
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNamespace(final String prefix) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  public ResourceSet query(final String query) {
    // Creates a query instance
    final QueryProcessor xpath = new XPathProcessor(query);
    // Start a query with the default context set (root node).
    final Nodes nodes = ctx.current();
    // Executes the query
    try {
      final Item result = (Item) xpath.query(nodes);
      return new BXResourceSet(result);
    } catch(final QueryException qe) {
      System.out.println(qe);
    }
    return null;
  }

  public ResourceSet queryResource(final String id, final String query) {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeNamespace(final String prefix) {
    // TODO Auto-generated method stub
  }

  public void setCollection(final Collection col) {
    // TODO Auto-generated method stub
  }

  public void setNamespace(final String prefix, final String uri) {
    // TODO Auto-generated method stub
  }

  public void setProperty(final String name, final String value) {
    // TODO Auto-generated method stub
  }
}
