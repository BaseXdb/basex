package org.basex.core.cmd;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.xml.sax.InputSource;

/**
 * Evaluates the 'replace' command and replaces documents in a collection.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Replace extends ACreate {
  /**
   * Default constructor.
   * @param source source path
   * @param input input file or XML string
   */
  public Replace(final String source, final String input) {
    super(DATAREF | User.WRITE, source, input);
  }

  @Override
  protected boolean run() {
    // check if input exists
    final IO io = IO.get(args[1]);
    if(!io.exists()) return error(FILEWHICH, io);

    try {
      return info(replace(args[0], io.inputSource(), context, false), perf);
    } catch(final Exception ex) {
      return error(ex.getMessage());
    }
  }

  /**
   * Replace the specified document with a new content.
   * @param p path to replace
   * @param input new content
   * @param ctx database context
   * @param lock if {@code true}, register a write lock in context
   * @return info string
   * @throws BaseXException database exception
   */
  public static String replace(final String p, final InputSource input,
      final Context ctx, final boolean lock) throws BaseXException {

    final Data data = ctx.data();
    String path = path(p);
    if(path.isEmpty()) return Util.info(DIRERR, path);

    final byte[] src = token(path);
    final IntList docs = data.doc(p);
    // check if path was found
    if(docs.size() == 0) return Util.info(FILEWHICH, path);
    // check if path points exclusively to files
    for(int i = 0, is = docs.size(); i < is; i++) {
      if(!eq(data.text(docs.get(i), true), src)) return Util.info(DIRERR, path);
    }

    final String target;
    final int i = path.lastIndexOf('/');
    if(i != -1) {
      target = path.substring(0, i);
      path = path.substring(i + 1);
    } else {
      target = "";
    }

    try {
      if(lock) ctx.register(true);
      // add new document
      Add.add(path, target, input, ctx, null, false);
      // delete old documents if addition was successful
      for(int d = docs.size() - 1; d >= 0; d--) data.delete(docs.get(d));
      // flushes changes
      data.flush();
    } finally {
      if(lock) ctx.unregister(true);
    }
    return Util.info(PATHREPLACED, docs.size());
  }
}
