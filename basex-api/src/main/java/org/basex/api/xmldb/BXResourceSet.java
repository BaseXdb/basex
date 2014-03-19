package org.basex.api.xmldb;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class BXResourceSet implements ResourceSet, BXXMLDBText {
  /** Resources. */
  private final ArrayList<Resource> res;
  /** Collection reference. */
  private final Collection coll;

  /**
   * Default constructor with result.
   * @param r result
   * @param c collection
   */
  BXResourceSet(final Result r, final Collection c) {
    // convert result into resource instances
    res = new ArrayList<>((int) r.size());
    for(int s = 0; s < r.size(); ++s) res.add(new BXXMLResource(r, s, c));
    coll = c;
  }

  @Override
  public Resource getResource(final long i) throws XMLDBException {
    if(i >= 0 && i < res.size()) return res.get((int) i);
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  @Override
  public void addResource(final Resource r) {
    res.add(r);
  }

  @Override
  public void removeResource(final long index) {
    res.remove((int) index);
  }

  @Override
  public BXResourceIterator getIterator() {
    return new BXResourceIterator(res);
  }

  @Override
  public Resource getMembersAsResource() throws XMLDBException {
    final TokenBuilder tb = new TokenBuilder().add('<').add(XMLDB).add('>');
    for(final Resource r : getIterator()) {
      tb.add(r.getContent().toString());
    }
    return new BXXMLResource(tb.add('<').add('/').add(XMLDB).add('>').finish(), coll);
  }

  @Override
  public long getSize() {
    return res.size();
  }

  @Override
  public void clear() {
    res.clear();
  }
}
