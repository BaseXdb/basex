package org.basex.query.func;

import java.io.File;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;

/**
 * Functions on files and directories.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class FNFile extends Fun {
  /** File separator system property. */
  private static final String SEPARATOR_PROP = "file.separator";

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {

    switch(func) {
      case FILES:
        return listFiles(ctx);
      default:
        return super.iter(ctx);
    }

  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {

    String path = expr.length == 0 ? null : new String(
        checkStr(expr[0].atomic(ctx)));

    switch(func) {
      case MKDIR:
        return Bln.get(new File(path).mkdir());
      case MKDIRS:
        return Bln.get(new File(path).mkdirs());
      case ISDIR:
        return Bln.get(new File(path).isDirectory());
      case ISFILE:
        return Bln.get(new File(path).isFile());
      case ISREAD:
        return Bln.get(new File(path).canRead());
      case ISWRITE:
        return Bln.get(new File(path).canWrite());
      case PATHSEP:
        return Str.get(System.getProperty(SEPARATOR_PROP));
      case DELETE:
        return Bln.get(new File(path).delete());
      case PATHTOFULL:
        return Str.get(new File(path).getAbsolutePath());
      default:
        return super.atomic(ctx);
    }

  }

  /**
   * Lists all files in a directory
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter listFiles(final QueryContext ctx) throws QueryException {

    String path = new String(checkStr(expr[0].atomic(ctx)));
    String[] files = (new File(path).list());

    final SeqIter si = new SeqIter();

    for(int i = 0; i < files.length; i++) {
      si.add(Str.get(files[i]));
    }

    return si;
  }

}
