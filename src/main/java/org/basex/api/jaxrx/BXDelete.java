package org.basex.api.jaxrx;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.DropDB;
import org.jaxrx.interfaces.IDelete;

/**
 * This class offers an implementation of the JAX-RX 'delete' operation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class BXDelete implements IDelete {
  @Override
  public boolean deleteResource(final String resource) {
    final Context ctx = new Context();
    try {
      new DropDB(resource).execute(ctx);
    } catch(final BaseXException ex) {
      BXUtil.notFound(ex.getMessage());
    }
    ctx.close();
    return true;
  }
}
