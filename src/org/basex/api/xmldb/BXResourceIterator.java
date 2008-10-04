package org.basex.api.xmldb;

import org.basex.data.Result;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;

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

  /**
   * Standard constructor with result.
   * @param r Result
   */
  public BXResourceIterator(final Result r) {
    result = r;
  }

  public boolean hasMoreResources() {
    return start < result.size() - 1;
  }

  public Resource nextResource() {
    start++;
    return new BXResource(result, start);
  }
}
