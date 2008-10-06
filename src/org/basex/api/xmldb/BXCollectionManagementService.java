package org.basex.api.xmldb;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.CollectionManagementService;

/**
 * Implementation of the CollectionManagementService Interface for the
 * XMLDB:API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollectionManagementService implements
    CollectionManagementService {
  
  /** BXCollection col. */
  protected BXCollection col;
  
  /**
   * Standard Constructor.
   * @param bxcol BXCollection
   */
  public BXCollectionManagementService(BXCollection bxcol) {
    this.col = bxcol;
  }

  public Collection createCollection(String name) {
    // Creates a new database context
    Context context = new Context();
    new CreateDB(name).execute(context);
    return new BXCollection(context);
  }

  public void removeCollection(String name) {
    DropDB.drop(name);
  }

  public String getName() {
    return "CollectionManagementService";
  }

  public String getVersion() {
    return "1.0";
  }

  public void setCollection(Collection col) {
    this.col = (BXCollection) col;
  }

  public String getProperty(String name) {
    //<CG> Was für Properties gibt es?
    // exist hat hier keine
    BaseX.notimplemented();
    return null;
  }

  public void setProperty(String name, String value) {
    //<CG> Was für Properties gibt es?
    // exist hat hier keine
    BaseX.notimplemented();
  }
}
