package org.basex.api.xmldb;

import java.util.ArrayList;
import java.util.Iterator;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the ResourceIterator Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BXResourceIterator implements ResourceIterator, BXXMLDBText,
    Iterable<Resource> {
  /** Resources. */
  private final Iterator<Resource> res;

  /**
   * Standard constructor with result.
   * @param r resources
   */
  public BXResourceIterator(final ArrayList<Resource> r) {
    res = r.iterator();
  }

  public boolean hasMoreResources() {
    return res.hasNext();
  }

  public Resource nextResource() throws XMLDBException {
    if(!res.hasNext())
      throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, ERR_ITER);
    return res.next();
  }

  public Iterator<Resource> iterator() {
    return res;
  }
}
