package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOFile;
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
    } catch(final BaseXException ex) {
      return error(ex.getMessage());
    }
  }

  /**
   * Replace the specified document with a new content.
   * @param path path to replace
   * @param input new content
   * @param ctx database context
   * @param lock if {@code true}, register a write lock in context
   * @return info string
   * @throws BaseXException database exception
   */
  public static String replace(final String path, final InputSource input,
      final Context ctx, final boolean lock) throws BaseXException {

    final Data data = ctx.data();
    if(data == null) throw new BaseXException(PROCNODB);

    String pth = IOFile.normalize(path);
    String trg = "";
    if(pth.isEmpty()) throw new BaseXException(DIRERR, pth);

    final byte[] src = token(pth);
    final IntList docs = data.docs(path);
    final int is = docs.size();
    // check if path points exclusively to files
    for(int i = 0; i < is; i++) {
      if(!eq(data.text(docs.get(i), true), src))
        throw new BaseXException(DIRERR, pth);
    }

    final int i = pth.lastIndexOf('/');
    if(i != -1) {
      trg = pth.substring(0, i);
      pth = pth.substring(i + 1);
    }

    try {
      if(lock) ctx.register(true);

      // replace binary or document
      if(!replace(data, pth, input)) {
        // add new document
        Add.add(pth, trg, input, ctx, null, false);
        // delete old documents if addition was successful
        for(int d = docs.size() - 1; d >= 0; d--) data.delete(docs.get(d));
        // flushes changes
        data.flush();
      }
    } finally {
      if(lock) ctx.unregister(true);
    }
    return Util.info(PATHREPLACED, docs.size());
  }

  /**
   * Replace the specified document with a new content.
   * @param data data reference
   * @param input new content
   * @param path file path
   * @return info string
   * @throws BaseXException database exception
   */
  public static boolean replace(final Data data, final String path,
      final InputSource input) throws BaseXException {

    final IOFile io = data.meta.binary(path);
    if(!io.exists()) return false;
    if(!Add.add(io, input)) throw new BaseXException(PARSEERR, path);
    return true;
  }
}
