package org.basex.api.xmldb;

import java.io.IOException;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the Resource Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXResource implements Resource {
  /** Result. */
  Result result;
  /** Position for value. */
  int pos;

  /**
   * Standard Constructor.
   * @param r result
   * @param p position
   */
  public BXResource(final Result r, final int p) {
    result = r;
    pos = p;
  }

  public Object getContent() throws XMLDBException {
    try {
      final CachedOutput out = new CachedOutput();
      result.serialize(new XMLSerializer(out), pos);
      return out.toString();
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  public String getId() {
  //<CG> Methode zur Erstellung einer eindeutigen ID?
    return null;
  }

  public Collection getParentCollection() {
  //<CG> Wie erhalte ich denn die zugehörige Collection?
    // bei exist gibts gar keine normale Resource
    return null;
  }

  public String getResourceType() {
    return result.getClass().getSimpleName();
  }

  public void setContent(final Object value) {
    result = (Result) value;
  }
}
