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
import org.basex.io.IO;

/**
 * Evaluates the 'create db' command and creates a new index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /**
   * Default constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public CreateIndex(final Object type) {
    super(DATAREF, type.toString());
  }

  @Override
  protected boolean exec() {
    final Data data = context.data();

    try {
      Type index = null;
      switch(getType(args[0])) {
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
        case SUMMARY:
          return summary(data);
        default:
          return false;
      }
      if(data instanceof MemData) return error(PROCMM);

      data.flush();
      buildIndex(index, data);
      return info(DBINDEXED, perf.getTimer());
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Creates a new path summary.
   * @param data data reference
   * @return true if operation was successful
   */
  public static boolean summary(final Data data) {
    final int[] parStack = new int[IO.MAXHEIGHT];
    int h = 0;
    int level = 0;

    data.path.init();
    for(int pre = 0; pre < data.meta.size; pre++) {
      final byte kind = (byte) data.kind(pre);
      final int par = data.parent(pre, kind);
      while(level > 0 && parStack[level - 1] > par) --level;

      if(kind == Data.DOC) {
        parStack[level++] = pre;
        data.path.add(0, level, kind);
      } else if(kind == Data.ELEM) {
        data.path.add(data.tagID(pre), level, kind);
        parStack[level++] = pre;
      } else if(kind == Data.ATTR) {
        data.path.add(data.attNameID(pre), level, kind);
      } else {
        data.path.add(0, level, kind);
      }
      if(h < level) h = level;
    }
    data.meta.pathindex = true;
    data.flush();
    return true;
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.INDEX + " " + args();
  }
}
