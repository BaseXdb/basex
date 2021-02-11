package org.basex.api.xmldb;

import static org.basex.api.xmldb.BXXMLDBText.*;
import java.util.*;

import org.basex.query.value.*;
import org.basex.util.*;
import org.xmldb.api.base.*;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BXResourceSet implements ResourceSet {
  /** Resources. */
  private final ArrayList<Resource> list;
  /** Collection reference. */
  private final BXCollection coll;

  /**
   * Default constructor with result.
   * @param result result
   * @param coll collection
   */
  BXResourceSet(final Value result, final BXCollection coll) {
    // convert result into resource instances
    final int rs = (int) result.size();
    list = new ArrayList<>(rs);
    for(int s = 0; s < rs; ++s) list.add(new BXXMLResource(result.itemAt(s), coll));
    this.coll = coll;
  }

  @Override
  public Resource getResource(final long index) throws XMLDBException {
    if(index >= 0 && index < list.size()) return list.get((int) index);
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  @Override
  public void addResource(final Resource resource) {
    list.add(resource);
  }

  @Override
  public void removeResource(final long index) {
    list.remove((int) index);
  }

  @Override
  public BXResourceIterator getIterator() {
    return new BXResourceIterator(list);
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
    return list.size();
  }

  @Override
  public void clear() {
    list.clear();
  }
}
