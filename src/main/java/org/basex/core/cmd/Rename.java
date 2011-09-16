package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.util.list.IntList;

/**
 * Evaluates the 'rename' command and renames resources or directories
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
    if(!new IOFile(trg).isValid()) return error(NAMEINVALID, trg);

    boolean ok = true;
    int c = 0;
    final IntList il = data.docs(src);
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      final String target = target(data, pre, src, trg);
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

    // rename binary resources
    final IOFile file = data.meta.binary(src);
    if(file == null || file.exists() && !file.rename(data.meta.binary(trg))) {
      ok = false;
      info(NAMEINVALID, trg);
    } else {
      c++;
    }

    info(PATHRENAMED, c, perf);
    return ok;
  }

  /**
   * Generates a target path for the specified document.
   * @param data data reference
   * @param pre pre value of the document
   * @param src source path
   * @param trg target path
   * @return new name
   */
  public static String target(final Data data, final int pre, final String src,
      final String trg) {

    // source references a file
    final String path = string(data.text(pre, true));
    if(path.equals(src)) return trg;
    // source references a directory: merge target path and file name
    final String name = path.substring(src.length() + 1);
    return !trg.isEmpty() ? trg + '/' + name : name;
  }
}
