package org.basex.api.xmldb;

import static org.basex.api.xmldb.BXXMLDBText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.*;

/**
 * Abstract QueryService definition for the XMLDB:API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BXQueryService implements XPathQueryService {
  /** XPath service constant. */
  static final String XPATH = "XPathQueryService";
  /** XQuery service constant. */
  static final String XQUERY = "XQueryQueryService";

  /** Namespaces. */
  private final HashMap<String, String> ns = new HashMap<>();
  /** Service name. */
  private final String name;
  /** Service version. */
  private final String version;
  /** Collection reference. */
  private BXCollection coll;

  /**
   * Standard constructor.
   * @param coll for collection reference
   * @param name service name
   * @param version version
   */
  BXQueryService(final BXCollection coll, final String name, final String version) {
    this.coll = coll;
    this.name = name;
    this.version = version;
  }

  @Override
  public void setNamespace(final String prefix, final String uri) throws XMLDBException {
    if(uri != null && !uri.isEmpty()) ns.put(prefix == null ? "" : prefix, uri);
    else throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_NSURI + prefix);
  }

  @Override
  public String getNamespace(final String prefix) {
    final String key = prefix == null ? "" : prefix;
    return ns.get(key);
  }

  @Override
  public void removeNamespace(final String prefix) {
    final String key = prefix == null ? "" : prefix;
    ns.remove(key);
  }

  @Override
  public void clearNamespaces() {
    ns.clear();
  }

  @Override
  public BXResourceSet query(final String query) throws XMLDBException {
    final Data data = coll.data;
    return query(query, DBNodeSeq.get(data.resources.docs(), data, true, true));
  }

  @Override
  public BXResourceSet queryResource(final String id, final String query) throws XMLDBException {
    final BXXMLResource xml = coll.getResource(id);
    if(xml != null) return query(query, new DBNode(xml.data, xml.pre));
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
   * Runs a query and returns the result set.
   * @param query query string
   * @param nodes nodes
   * @return resource set
   * @throws XMLDBException exception
   */
  private BXResourceSet query(final String query, final Value nodes) throws XMLDBException {
    // creates a query instance
    final Context ctx = coll.ctx;
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      qp.context(nodes);
      qp.parse();
      // add default namespaces
      for(final Entry<String, String> entry : ns.entrySet()) {
        qp.sc.ns.add(token(entry.getKey()), token(entry.getValue()), null);
      }
      // perform query and return result
      qp.register(ctx);
      try {
        return new BXResourceSet(qp.value(), coll);
      } finally {
        qp.close();
        qp.unregister(ctx);
      }
    } catch(final QueryException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }
}
