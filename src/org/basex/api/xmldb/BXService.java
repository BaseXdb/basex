package org.basex.api.xmldb;

import org.basex.BaseX;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Service;

/**
 * Implementation of the Service Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXService implements Service {

  public String getName() {
    BaseX.notimplemented();
    return null;
  }

  public String getProperty(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public String getVersion() {
    BaseX.notimplemented();
    return null;
  }

  public void setCollection(final Collection col) {
    BaseX.notimplemented();
  }

  public void setProperty(final String name, final String value) {
    BaseX.notimplemented();
  }
}
