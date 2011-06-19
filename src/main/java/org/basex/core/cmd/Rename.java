package org.basex.core.cmd;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.util.TokenList;

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
    final TokenList unchanged = new TokenList();
    final int c = rename(context.data, token(path(args[0])),
        token(path(args[1])), unchanged);
    for(final byte[] d : unchanged) info(NAMEINVALID, d);
    info(PATHRENAMED, c, perf);
    return unchanged.size() == 0;
  }

  /**
   * Rename document or document paths.
   * @param data database
   * @param src source path
   * @param trg target path
   * @param r documents which cannot be renamed will be stored here
   * @return number of successfully renamed documents
   */
  public static int rename(final Data data, final byte[] src,
      final byte[] trg, final TokenList r) {
    int c = 0;
    for(final int doc : data.doc(string(src))) {
      final byte[] path = data.text(doc, true);
      byte[] target = trg;
      byte[] name = substring(path, src.length);
      if(name.length != 0) {
        // change file path: replace all paths with the target path
        if(startsWith(name, '/')) name = substring(name, 1);
        target = trg.length != 0 ? concat(trg, SLASH, name) : name;
      }
      if(target.length == 0) {
        r.add(path);
      } else {
        data.replace(doc, Data.DOC, target);
        ++c;
      }
    }
    // data was changed: update context
    if(c != 0) data.flush();
    return c;
  }
}
