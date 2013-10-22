package org.basex.api.xmldb;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.*;

/**
 * Abstract QueryService definition for the XMLDB:API.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class BXQueryService implements XPathQueryService, BXXMLDBText {
  /** XPath service constant. */
  static final String XPATH = "XPathQueryService";
  /** XQuery service constant. */
  static final String XQUERY = "XQueryQueryService";

  /** Namespaces. */
  private final HashMap<String, String> ns = new HashMap<String, String>();
  /** Service name. */
  private final String name;
  /** Service version. */
  private final String version;
  /** Collection reference. */
  private BXCollection coll;

  /**
   * Standard constructor.
   * @param c for collection reference
   * @param n service name
   * @param v version
   */
  BXQueryService(final BXCollection c, final String n, final String v) {
    coll = c;
    name = n;
    version = v;
  }

  @Override
  public void setNamespace(final String pre, final String uri) throws XMLDBException {
    if(uri != null && !uri.isEmpty()) ns.put(pre == null ? "" : pre, uri);
    else throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_NSURI + pre);
  }

  @Override
  public String getNamespace(final String pre) {
    return ns.get(pre == null ? "" : pre);
  }

  @Override
  public void removeNamespace(final String pre) {
    ns.remove(pre == null ? "" : pre);
  }

  @Override
  public void clearNamespaces() {
    ns.clear();
  }

  @Override
  public BXResourceSet query(final String query) throws XMLDBException {
    return query(coll.ctx.current(), query);
  }

  @Override
  public BXResourceSet queryResource(final String id, final String query)
      throws XMLDBException {

    final BXXMLResource xml = coll.getResource(id);
    if(xml != null) return query(new Nodes(xml.pos, xml.data), query);
    // throw exception if id was not found...
    throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_RES + id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public void setCollection(final Collection col) {
    coll = (BXCollection) col;
  }

  @Override
  public String getProperty(final String nm) {
    return null;
  }

  @Override
  public void setProperty(final String nm, final String value) throws XMLDBException {
    throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + nm);
  }

  /**
   * Method for both query actions.
   * @param nodes initial node set
   * @param query query string
   * @return resource set
   * @throws XMLDBException exception
   */
  private BXResourceSet query(final Nodes nodes, final String query) throws XMLDBException {
    // creates a query instance
    final QueryProcessor qp = new QueryProcessor(query, coll.ctx).context(nodes);
    try {
      coll.ctx.register(qp);
      // add default namespaces
      for(final String n : ns.keySet()) {
        qp.sc.ns.add(token(n), token(ns.get(n)), null);
      }
      // perform query and return result
      return new BXResourceSet(qp.execute(), coll);
    } catch(final QueryException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    } finally {
      qp.close();
      coll.ctx.unregister(qp);
    }
  }
}
