package org.basex.query.func;

import static org.basex.util.Token.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.PatternSyntaxException;

import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Functions on files and directories.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Rositsa Shadura
 */
final class FNFile extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNFile(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);

    switch(def) {
      case FILES:
        return listFiles(ctx);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkAdmin(ctx);

    final File path = expr.length == 0 ? null : new File(
        string(checkStrEmp(expr[0].atomic(ctx, input))));

    switch(def) {
      case MKDIR:
        return makeDir(path, false);
      case MKDIRS:
        return makeDir(path, true);
      case ISDIR:
        return Bln.get(path.isDirectory());
      case ISFILE:
        return Bln.get(path.isFile());
      case ISREAD:
        return Bln.get(path.canRead());
      case ISWRITE:
        return Bln.get(path.canWrite());
      case PATHSEP:
        return Str.get(Prop.SEP);
      case DELETE:
        return delete(path);
      case PATHTOFULL:
        return Str.get(path.getAbsolutePath());
      case READFILE:
        return readFile(ctx);
      case READBINARY:
        return readBinary(path);
      case WRITE:
        return writeFile(path, ctx);
      case COPY:
        return copy(ctx);
      case MOVE:
        return move(path, ctx);
      case LASTMOD:
        return new Dtm(path.lastModified(), input);
      default:
        return super.atomic(ctx, ii);
    }
  }

  /**
   * Lists all files in a directory.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter listFiles(final QueryContext ctx) throws QueryException {

    final String path = string(checkStr(expr[0], ctx));

    final String pattern;
    try {
      pattern = expr.length == 2 ? string(checkStr(expr[1], ctx)).replaceAll(
          "\\.", "\\\\.").replaceAll("\\*", ".*") : null;
    } catch(final PatternSyntaxException ex) {
      Err.or(input, QueryText.FILEPATTERN, expr[1]);
      return null;
    }

    final File[] files = new File(path).listFiles(new FileFilter() {
      @Override
      public boolean accept(final File pathname) {
        if(pathname.isHidden()) return false;

        return pattern == null ? true : pathname.getName().matches(pattern);

      }
    });

    if(files == null) {
      Err.or(input, QueryText.FILELIST, path);
    }

    return new Iter() {

      int c = -1;

      @Override
      public Item next() {
        return ++c < files.length ? Str.get(files[c].getName()) : null;
      }
    };
  }

  /**
   * Reads the content of a file.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str readFile(final QueryContext ctx) throws QueryException {

    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkEStr(expr[1], ctx));

    return Str.get(FNGen.unparsedText(io, enc, input));

  }

  /**
   * Reads the content of a binary file.
   * @param file input file
   * @return Base64Binary
   * @throws QueryException query exception
   */
  private B64 readBinary(final File file) throws QueryException {

    if(file.length() > Integer.MAX_VALUE) {
      Err.or(input, QueryText.FILEREAD, file.getName());
      return null;
    }
    int cap = new Long(file.length()).intValue();
    final ByteBuffer byteBuffer = ByteBuffer.allocate(cap);
    try {

      final BufferedInputStream bufferInput = new BufferedInputStream(
          new FileInputStream(file));

      int b;

      try {
        while((b = bufferInput.read()) != -1)
          byteBuffer.put((byte) b);

      } finally {
        bufferInput.close();
      }

    } catch(IOException ex) {

      Err.or(input, QueryText.FILEREAD, file.getName());

    }

    return new B64(byteBuffer.array());

  }

  /**
   * Writes a sequence of items to a file.
   * @param file file to be written
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   */
  private Bln writeFile(final File file, final QueryContext ctx)
      throws QueryException {

    final BufferedOutputStream out;
    final Iter ir = expr[1].iter(ctx);
    final TokenBuilder params = interpretSerialParams(ctx);
    Item n;

    try {
      out = new BufferedOutputStream(new FileOutputStream(file, true));

      try {
        while((n = ir.next()) != null) {

          if(n instanceof Nod) {
            final Nod nod = checkNode(checkItem(n, ctx));

            Str str = FNGen.serialize(nod, params, input);
            out.write(str.atom());

          } else {

            out.write(checkItem(n, ctx).toString().getBytes());
          }
        }
      } finally {
        out.close();
      }

    } catch(IOException e) {

      Err.or(input, QueryText.FILEWRITE, file.getName());
      return Bln.FALSE;
    }

    return Bln.TRUE;
  }

  /**
   * Interprets serialization parameters.
   * @param ctx query context
   * @return serialization params
   * @throws QueryException query exception
   */
  private TokenBuilder interpretSerialParams(final QueryContext ctx)
      throws QueryException {

    // interpret query parameters
    final TokenBuilder tb = new TokenBuilder();
    if(expr.length == 3) {
      final Iter ir = expr[2].iter(ctx);
      Item n;
      while((n = ir.next()) != null) {
        final Nod p = checkNode(n);
        if(tb.size() != 0) tb.add(',');
        tb.add(p.nname()).add('=').add(p.atom());
      }
    }

    return tb;
  }

  /**
   * Copies a file given a source and a destination.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln copy(final QueryContext ctx) throws QueryException {

    final String src = string(checkStr(expr[0], ctx));
    final String dest = string(checkStr(expr[1], ctx));

    try {

      final FileChannel srcChannel = new FileInputStream(new File(src)).getChannel();
      final FileChannel destChannel = new FileOutputStream(new File(dest)).getChannel();

      try {
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
      } finally {

        srcChannel.close();
        destChannel.close();
      }

    } catch(IOException ex) {

      Err.or(input, QueryText.FILECOPY, src, dest);
      return Bln.FALSE;
    }

    return Bln.TRUE;
  }

  /**
   * Moves a file or directory.
   * @param file file/dir to be moved
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln move(final File file, final QueryContext ctx)
      throws QueryException {

    final String dest = string(checkStr(expr[1], ctx));

    try {
      return Bln.get(file.renameTo(new File(dest, file.getName())));
    } catch(RuntimeException ex) {
      Err.or(input, QueryText.FILEMOVE, ex);
      return Bln.FALSE;
    }

  }

  /**
   * Deleted a file or directory.
   * @param file file/dir to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Bln delete(final File file) throws QueryException {

    try {

      return Bln.get(file.delete());

    } catch(SecurityException ex) {
      Err.or(input, QueryText.FILEDELETE, ex);
      return Bln.FALSE;
    }

  }

  /**
   * Creates a directory.
   * @param file dir to be created
   * @param includeParents indicator for including nonexistent parent
   *          directories by the creation
   * @return result
   * @throws QueryException query exception
   */
  private Bln makeDir(final File file, final boolean includeParents)
      throws QueryException {

    try {

      return includeParents ? Bln.get(file.mkdirs()) : Bln.get(file.mkdir());

    } catch(SecurityException ex) {

      Err.or(input, QueryText.DIRCREATE, ex);
      return Bln.FALSE;

    }

  }

}
