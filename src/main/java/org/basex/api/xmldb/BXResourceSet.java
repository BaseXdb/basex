package org.basex.api.xmldb;

import java.io.IOException;
import java.util.ArrayList;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class BXResourceSet implements ResourceSet, BXXMLDBText {
  /** Resources. */
  private final ArrayList<Resource> res = new ArrayList<Resource>();
  /** Collection reference. */
  private final Collection coll;

  /**
   * Default constructor with result.
   * @param r result
   * @param c collection
   */
  BXResourceSet(final Result r, final Collection c) {
    // convert result into resource instances
    for(int s = 0; s < r.size(); s++) res.add(new BXXMLResource(r, s, c));
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
    final CachedOutput co = new CachedOutput();
    try {
      final XMLSerializer xml = new XMLSerializer(co);
      for(final Resource r : getIterator()) {
        xml.openResult();
        co.print(r.getContent().toString());
        xml.closeResult();
      }
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
    return new BXXMLResource(co.toArray(), coll);
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
