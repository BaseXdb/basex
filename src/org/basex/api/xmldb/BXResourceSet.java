package org.basex.api.xmldb;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.basex.query.xpath.values.Item;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResourceSet implements ResourceSet {
  /** Result */
  Item result;
  
  /**
   * Standard Constructor with result.
   * @param result Result
   */
  public BXResourceSet(Item result) {
    this.result = result;
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#addResource(org.xmldb.api.base.Resource)
   */
  public void addResource(Resource res) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#clear()
   */
  public void clear() {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#getIterator()
   */
  public ResourceIterator getIterator() {
    // TODO Auto-generated method stub
    return new BXResourceIterator(result);
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#getMembersAsResource()
   */
  public Resource getMembersAsResource() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#getResource(long)
   */
  public Resource getResource(long index) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#getSize()
   */
  public long getSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.xmldb.api.base.ResourceSet#removeResource(long)
   */
  public void removeResource(long index) {
    // TODO Auto-generated method stub
    
  }
}
