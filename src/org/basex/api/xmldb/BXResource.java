package org.basex.api.xmldb;

import java.io.IOException;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;

/**
 * Implementation of the Resource Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResource implements Resource {
  /** Result. */
  Item result;
  /** Position for value. */
  int pos;
  
  /**
   * Standard Constructor.
   * @param result
   * @param pos
   */
  public BXResource(Item result, int pos) {
    this.result = result;
    this.pos = pos;
  }

  /**
   * @see org.xmldb.api.base.Resource#getContent()
   */
  public Object getContent() throws XMLDBException {
    try {
      final CachedOutput out = new CachedOutput();
      final XMLSerializer ser = new XMLSerializer(out);
      if(result instanceof NodeSet) {
        final NodeSet nodes = (NodeSet) result;
        ser.xml(nodes.data, nodes.nodes[pos]);
      } else {
        ser.item(result.str());
      }
      return out.toString();
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, ex.getMessage());
    }
  }

  /**
   * @see org.xmldb.api.base.Resource#getId()
   */
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#getParentCollection()
   */
  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Resource#getResourceType()
   */
  public String getResourceType() {
    return result.getClass().getSimpleName();
  }

  /**
   * @see org.xmldb.api.base.Resource#setContent(java.lang.Object)
   */
  public void setContent(Object value) {
  // TODO Auto-generated method stub

  }
}
