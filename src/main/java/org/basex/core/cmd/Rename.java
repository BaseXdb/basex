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
    final int[] docs = data.doc(args[0]);
    final byte[] src = token(path(args[0]));
    final byte[] trg = token(path(args[1]));

    for(final int doc : docs) {
      final byte[] path = data.text(doc, true);
      byte[] target = trg;
      byte[] name = substring(path, src.length);
      if(name.length != 0) {
        // change file path: replace all paths with the target path
        if(startsWith(name, '/')) name = substring(name, 1);
        target = trg.length != 0 ? concat(trg, SLASH, name) : name;
      }
      data.replace(doc, Data.DOC, target);
    }

    // data was changed: update context
    if(docs.length != 0) data.flush();
    return info(PATHRENAMED, docs.length, perf);
  }
}
