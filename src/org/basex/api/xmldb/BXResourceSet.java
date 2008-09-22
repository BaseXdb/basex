package org.basex.api.xmldb;

import org.basex.query.xpath.values.Item;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResourceSet implements ResourceSet {
  /** Result. */
  Item result;

  /**
   * Standard Constructor with result.
   * @param r Result
   */
  public BXResourceSet(final Item r) {
    result = r;
  }

  public void addResource(final Resource res) {
    // TODO Auto-generated method stub
  }

  public void clear() {
    // TODO Auto-generated method stub
  }

  public ResourceIterator getIterator() {
    // TODO Auto-generated method stub
    return new BXResourceIterator(result);
  }

  public Resource getMembersAsResource() {
    // TODO Auto-generated method stub
    return null;
  }

  public Resource getResource(final long index) {
    // TODO Auto-generated method stub
    return null;
  }

  public long getSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void removeResource(final long index) {
    // TODO Auto-generated method stub
  }
}
