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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXResourceIterator implements ResourceIterator, BXXMLDBText,
    Iterable<Resource> {

  /** Resources. */
  private final Iterator<Resource> res;

  /**
   * Standard constructor with result.
   * @param r resources
   */
  BXResourceIterator(final ArrayList<Resource> r) {
    res = r.iterator();
  }

  @Override
  public boolean hasMoreResources() {
    return res.hasNext();
  }

  @Override
  public Resource nextResource() throws XMLDBException {
    if(!res.hasNext())
      throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, ERR_ITER);
    return res.next();
  }

  @Override
  public Iterator<Resource> iterator() {
    return res;
  }
}
