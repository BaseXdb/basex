package org.basex.api.xmldb;

import java.util.Hashtable;
import java.util.Iterator;

import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xquery.XQueryProcessor;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

/**
 * Abstract QueryService definition for the XMLDB:API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQueryService implements XPathQueryService {
  /** XPath service constant. */
  public static final String XPATH = "XPathQueryService";
  /** XQuery service constant. */
  public static final String XQUERY = "XQueryQueryService";
  /** Service name. */
  private final String name;
  /** Collection reference. */
  private BXCollection coll;
  /** Hashtable for namespaceMapping. */
  private Hashtable<String, String> nsMaps;

  /**
   * Standard constructor.
   * @param c for collection reference
   * @param n service name
   */
  public BXQueryService(final BXCollection c, final String n) {
    coll = c;
    name = n;
    nsMaps = new Hashtable<String, String>(5);
  }

  public void clearNamespaces() {
    nsMaps.clear();
  }

  public String getName() {
    return name;
  }

  public String getNamespace(String prefix) {
    return nsMaps.get(prefix);
  }

  public String getProperty(final String name) {
  //<CG> Was für Properties gibt es?
    BaseX.notimplemented();
    return null;
  }

  public String getVersion() {
    return "1.0";
  }

  public ResourceSet query(final String query) throws XMLDBException {
    return queryAll(null, query);
  }

  public ResourceSet queryResource(final String id, final String query) throws XMLDBException {
    return queryAll(id, query);
}
  
  /**
   * Method for both query Actions.
   * @param id
   * @param query
   * @return BXResourceSet
   * @throws XMLDBException
   */
  private ResourceSet queryAll(final String id, final String query) throws XMLDBException {
 // Creates a query instance
    final QueryProcessor proc = name.equals(XPATH) ? new XPathProcessor(query)
        : new XQueryProcessor(query);
    try {
      if(id == null) {
     // Executes the query and returns the result
        return new BXResourceSet(proc.query(coll.ctx.current()), coll);
      }
   // Executes the query and returns the result
      return new BXResourceSet(proc.query(new Nodes(((BXXMLResource) coll.getResource(id)).getData())), coll);
    } catch(final QueryException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    } catch(final Exception ex) {
      BaseX.notexpected();
      return null;
    }
  }

  public void removeNamespace(final String prefix) {
    Iterator i = nsMaps.values().iterator();
    while(i.hasNext()) {
      if(((String)i.next()).equals(prefix)) {
        i.remove();
      }
    }
  }

  public void setCollection(final Collection col) {
    coll = (BXCollection) col;
  }

  public void setNamespace(final String prefix, final String uri) {
    nsMaps.put(prefix, uri);
  }

  public void setProperty(final String name, final String value) {
  //<CG> Was für Properties gibt es?
    BaseX.notimplemented();
  }
}
