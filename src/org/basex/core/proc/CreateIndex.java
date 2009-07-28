package org.basex.core.proc;

import static org.basex.Text.*;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.Type;

/**
 * Creates a new index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    final Data data = context.data();
    if(data instanceof MemData) return error(PROCMM);
    try {
      Type index = null;
      switch(type) {
        case TEXT:
          data.meta.txtindex = true;
          index = Type.TXT;
          break;
        case ATTRIBUTE:
          data.meta.atvindex = true;
          index = Type.ATV;
          break;
        case FULLTEXT:
          data.meta.ftxindex = true;
          data.meta.ftfz = prop.is(Prop.FTFUZZY);
          data.meta.ftst = prop.is(Prop.FTST);
          data.meta.ftcs = prop.is(Prop.FTCS);
          data.meta.ftdc = prop.is(Prop.FTDC);
          index = Type.FTX;
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
    return Cmd.CREATE.name() + " " + CmdCreate.INDEX + " " + type;
  }
}
