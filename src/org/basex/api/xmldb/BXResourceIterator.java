package org.basex.api.xmldb;

import org.basex.data.Result;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the ResourceIterator Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXResourceIterator implements ResourceIterator {
  /** Result. */
  Result result;
  /** Start value for iterator. */
  int start = -1;
  /** End value for iterator. */
  int end;

  /**
   * Standard constructor with result.
   * @param r Result
   */
  public BXResourceIterator(final Result r) {
    this.result = r;
    this.end = result.size() - 1;
  }

  public boolean hasMoreResources() {
    return start < end;
  }

  public Resource nextResource() throws XMLDBException {
    if(start < end) {
      start++;
    return new BXResource(result, start);
    }  
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }
}
