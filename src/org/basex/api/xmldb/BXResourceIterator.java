package org.basex.api.xmldb;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.basex.data.Result;

/**
 * Implementation of the ResourceIterator Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXResourceIterator implements ResourceIterator {
  
  /** Result */
  Result result;
  /** Start value for iterator */
  int start = -1;
  
  /**
   * Standard constructor with result.
   * @param result Result
   */
  public BXResourceIterator(Result result) {
    this.result = result;
  }

  /**
   * @see org.xmldb.api.base.ResourceIterator#hasMoreResources()
   */
  public boolean hasMoreResources() {
    if(start < result.size() - 1) {
      return true;
    }
    return false;
  }

  /**
   * @see org.xmldb.api.base.ResourceIterator#nextResource()
   */
  public Resource nextResource() {
    start++;
    return new BXResource(result, start);
  }
}
