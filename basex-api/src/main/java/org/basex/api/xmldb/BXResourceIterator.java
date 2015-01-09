package org.basex.api.xmldb;

import java.util.*;

import org.xmldb.api.base.*;

/**
 * Implementation of the ResourceIterator Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class BXResourceIterator implements ResourceIterator, BXXMLDBText, Iterable<Resource> {
  /** Resources. */
  private final Iterator<Resource> iter;

  /**
   * Standard constructor with result.
   * @param resources resource iterator
   */
  BXResourceIterator(final ArrayList<Resource> resources) {
    iter = resources.iterator();
  }

  @Override
  public boolean hasMoreResources() {
    return iter.hasNext();
  }

  @Override
  public Resource nextResource() throws XMLDBException {
    if(!iter.hasNext()) throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, ERR_ITER);
    return iter.next();
  }

  @Override
  public Iterator<Resource> iterator() {
    return iter;
  }
}
