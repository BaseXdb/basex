package org.basex.api.xmldb;

import org.basex.BaseX;
import org.basex.data.Result;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXResourceSet implements ResourceSet {
  /** Result. */
  Result result;

  /**
   * Standard Constructor with result.
   * @param r Result
   */
  public BXResourceSet(final Result r) {
    result = r;
  }

  public void addResource(final Resource res) {
    BaseX.notimplemented();
  }

  public void clear() {
    BaseX.notimplemented();
  }

  public ResourceIterator getIterator() {
    return new BXResourceIterator(result);
  }

  public Resource getMembersAsResource() {
    BaseX.notimplemented();
    return null;
  }

  public Resource getResource(final long index) {
    BaseX.notimplemented();
    return null;
  }

  public long getSize() {
    BaseX.notimplemented();
    return 0;
  }

  public void removeResource(final long index) {
    BaseX.notimplemented();
  }
}
