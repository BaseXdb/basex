package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'rename' command and renames resources or directories in a collection.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Rename extends ACreate {
  /**
   * Default constructor.
   * @param source source path
   * @param target target path
   */
  public Rename(final String source, final String target) {
    super(Perm.WRITE, true, source, target);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    final String src = MetaData.normPath(args[0]);
    if(src == null) return error(NAME_INVALID_X, args[0]);
    final String trg = MetaData.normPath(args[1]);
    if(trg == null) return error(NAME_INVALID_X, args[1]);

    return update(data, new Code() {
      @Override
      boolean run() {
        return rename(data, src, trg);
      }
    });
  }

  /**
   * Renames files.
   * @param data database
   * @param src source path
   * @param trg target path
   * @return success flag
   */
  private boolean rename(final Data data, final String src, final String trg) {
    boolean ok = true;
    int c = 0;
    final IntList docs = data.resources.docs(src);
    final int ds = docs.size();
    for(int i = 0; i < ds; i++) {
      final int pre = docs.get(i);
      final String target = target(data, pre, src, trg);
      if(target.isEmpty()) {
        ok = !info(NAME_INVALID_X, target);
      } else {
        data.update(pre, Data.DOC, token(target));
        c++;
      }
    }

    final IOFile file = data.inMemory() ? null : data.meta.binary(src);
    if(file != null && file.exists()) {
      final IOFile target = data.meta.binary(trg);
      final IOFile trgdir = target.parent();
      if(!trgdir.md() || !file.rename(target)) ok = !info(NAME_INVALID_X, trg);
      c++;
    }
    // return info message
    return info(RES_RENAMED_X_X, c, jc().performance) && ok;
  }

  /**
   * Generates a target path for the specified document.
   * @param data data reference
   * @param pre pre value of the document
   * @param src source path
   * @param trg target path
   * @return new name
   */
  public static String target(final Data data, final int pre, final String src, final String trg) {
    // source references a file
    final String path = string(data.text(pre, true));
    if(Prop.CASE ? path.equals(src) : path.equalsIgnoreCase(src)) return trg;

    // directory references: add trailing slashes to non-empty paths
    final String source = src.isEmpty() || Strings.endsWith(src, '/') ? src : src + '/';
    final String target = trg.isEmpty() || Strings.endsWith(trg, '/') ? trg : trg + '/';
    // merge target with old path
    return target + path.substring(source.length());
  }
}
