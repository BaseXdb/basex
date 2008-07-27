package org.basex.api.xmldb;

import org.basex.core.Context;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.XPathProcessor;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;
import org.basex.query.QueryException;

/**
 * Implementation of the Service Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXPathQueryService implements XPathQueryService {
  
  /** Context ctx */
  Context ctx;
  
  /**
   * Standard constructor.
   * @param ctx for Context
   */
  public BXXPathQueryService(Context ctx) {
    this.ctx = ctx;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#clearNamespaces()
   */
  public void clearNamespaces() {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.base.Service#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#getNamespace(java.lang.String)
   */
  public String getNamespace(String prefix) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.base.Configurable#getProperty(java.lang.String)
   */
  public String getProperty(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.base.Service#getVersion()
   */
  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#query(java.lang.String)
   */
  public ResourceSet query(String query) {
 // Creates a query instance
    QueryProcessor xpath = new XPathProcessor(query);
    // Start a query with the default context set (root node).
    Nodes nodes = ctx.current();
 // Executes the query
    try{
      Result result = xpath.query(nodes);
      return new BXResourceSet(result);
    } catch (QueryException qe) {
      System.out.println(qe);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#queryResource(java.lang.String, java.lang.String)
   */
  public ResourceSet queryResource(String id, String query) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#removeNamespace(java.lang.String)
   */
  public void removeNamespace(String prefix) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.base.Service#setCollection(org.xmldb.api.base.Collection)
   */
  public void setCollection(Collection col) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.modules.XPathQueryService#setNamespace(java.lang.String, java.lang.String)
   */
  public void setNamespace(String prefix, String uri) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmldb.api.base.Configurable#setProperty(java.lang.String, java.lang.String)
   */
  public void setProperty(String name, String value) {
    // TODO Auto-generated method stub
    
  }

}
