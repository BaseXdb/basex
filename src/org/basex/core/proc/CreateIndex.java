package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.index.IndexToken;

/**
 * Creates a new index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /** Index type. */
  private final CmdIndex type;

  /**
   * Constructor.
   * @param t index type
   */
  public CreateIndex(final CmdIndex t) {
    super(DATAREF);
    type = t;
  }
  
  @Override
  protected boolean exec() {
    try {
      final Data data = context.data();
      IndexToken.Type index = null;
      switch(type) {
        case TEXT:
          data.meta.txtindex = true;
          index = IndexToken.Type.TXT;
          break;
        case ATTRIBUTE:
          data.meta.atvindex = true;
          index = IndexToken.Type.ATV;
          break;
        case FULLTEXT:
          data.meta.ftxindex = true;
          index = IndexToken.Type.FTX;
          break;
      }
      data.flush();
      buildIndex(index, data);
      return info(DBINDEXED, perf.getTimer());
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
  
  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " +  CmdCreate.INDEX + " " + type;
  }
}
