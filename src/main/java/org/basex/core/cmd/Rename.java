package org.basex.core.cmd;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import org.basex.core.User;
import org.basex.data.Data;

/**
 * Evaluates the 'rename' command and renames document or document paths
 * in a collection.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Rename extends ACreate {
  /**
   * Default constructor.
   * @param source source path
   * @param target target path
   */
  public Rename(final String source, final String target) {
    super(DATAREF | User.WRITE, source, target);
  }

  @Override
  protected boolean run() {
    final Data data = context.data;
    final byte[] src = token(path(args[0]));
    final byte[] trg = token(path(args[1]));

    boolean ok = true;
    int c = 0;
    for(final int doc : data.doc(args[0])) {
      final byte[] target = newName(data, doc, src, trg);
      if(target.length == 0) {
        info(NAMEINVALID, target);
        ok = false;
      } else {
        data.replace(doc, Data.DOC, target);
        c++;
      }
    }
    // data was changed: update context
    if(c != 0) data.flush();

    info(PATHRENAMED, c, perf);
    return ok;
  }

  /**
   * Generate a new name for a document.
   * @param d data
   * @param pre pre value of the document
   * @param src source path
   * @param trg target path
   * @return new name
   */
  public static byte[] newName(final Data d, final int pre, final byte[] src,
      final byte[] trg) {
    final byte[] path = d.text(pre, true);
    byte[] target = trg;
    byte[] name = substring(path, src.length);
    if(name.length != 0) {
      // change file path: replace all paths with the target path
      if(startsWith(name, '/')) name = substring(name, 1);
      target = trg.length != 0 ? concat(trg, SLASH, name) : name;
    }
    return target;
  }
}
