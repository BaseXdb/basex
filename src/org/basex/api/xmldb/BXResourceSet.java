package org.basex.api.xmldb;

import java.io.StringWriter;
import java.util.Vector;

import org.basex.data.Result;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the ResourceSet Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXResourceSet implements ResourceSet {
  /** Result. */
  Result result;
  /** Collection reference. */
  Collection coll;
  /** Vector for resources. */
  Vector<Resource> resources;

  /**
   * Standard Constructor with result.
   * @param r Result
   * @param c Collection
   */
  public BXResourceSet(final Result r, final Collection c) {
    this.result = r;
    this.coll = c;
    this.resources = new Vector<Resource>();
  }

  public void addResource(final Resource res) {
    resources.addElement(res);
  }

  public void clear() {
    resources.clear();
  }

  public ResourceIterator getIterator() {
    return new BXResourceIterator(result);
  }

  public Resource getMembersAsResource() throws XMLDBException {
 // <CG> Alle Resources auslesen und in eine XMLResource stecken? 
    StringWriter content = new StringWriter();
    for(int i = 0; i < resources.size(); i++) {
      content.append(coll.getResource(((BXXMLResource)resources.get(i)).getDocumentId()).getContent().toString());
      BXXMLResource test = new BXXMLResource(null, "TEST", -1, coll);
      test.setContent(content.toString());
      return test;
    }
    return null;
  }

  public Resource getResource(final long index) throws XMLDBException {
    if(index < resources.size()) return resources.get((int) index);
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  public long getSize() {
    return resources.size();
  }

  public void removeResource(final long index) {
    resources.removeElementAt((int) index);
  }
}
