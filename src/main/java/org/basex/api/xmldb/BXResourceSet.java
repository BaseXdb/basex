package org.basex.api.xmldb;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-12, BSD License
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
    res = new ArrayList<Resource>((int) r.size());
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
    final ArrayOutput ao = new ArrayOutput();
    try {
      final Serializer ser = Serializer.get(ao);
      for(final Resource r : getIterator()) {
        ser.openResult();
        ser.text(Token.token(r.getContent().toString()));
        ser.closeResult();
      }
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
    return new BXXMLResource(ao.toArray(), coll);
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
