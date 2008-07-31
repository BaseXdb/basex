package org.basex.api.xmldb;

import org.basex.data.Result;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.basex.query.xpath.values.NodeSet;

/**
 * Implementation of the Resource Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResource implements Resource {
  
  /** Result */
  Result result;
  /** Position for value */
  int pos;
  
  /**
   * Standard Constructor.
   * @param result
   * @param pos
   */
  public BXResource(Result result, int pos) {
    this.result = result;
    this.pos = pos;
  }

  /**
   * @see org.xmldb.api.base.Resource#getContent()
   */
  public Object getContent() {
    if(result instanceof NodeSet) {
      NodeSet nodes = (NodeSet) result;
      System.out.println(result.toString());
      return new String(nodes.data.atom(nodes.nodes[pos]));
    } else {
      return result.toString();
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
    return result.toString();
  }

  /**
   * @see org.xmldb.api.base.Resource#setContent(java.lang.Object)
   */
  public void setContent(Object value) {
  // TODO Auto-generated method stub

  }
}
