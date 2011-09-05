package org.basex.core.cmd;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.util.list.IntList;

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
    final Data data = context.data();
    final String src = IOFile.normalize(args[0]);
    final String trg = IOFile.normalize(args[1]);

    // ensure that the name contains no slashes and trailing dots
    if(!new IOFile(trg).valid()) return error(NAMEINVALID, trg);

    boolean ok = true;
    int c = 0;
    final IntList il = data.docs(args[0]);
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final String target = newName(data, pre, src, trg);
      if(target.isEmpty()) {
        info(NAMEINVALID, target);
        ok = false;
      } else {
        data.update(pre, Data.DOC, token(target));
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
  public static String newName(final Data d, final int pre, final String src,
      final String trg) {

    final byte[] path = d.text(pre, true);
    String target = trg;
    String name = string(substring(path, src.length()));
    if(!name.isEmpty()) {
      // change file path: replace all paths with the target path
      if(name.startsWith("/")) name = name.substring(1);
      target = !trg.isEmpty() ? trg + '/' + name : name;
    }
    return target;
  }
}
