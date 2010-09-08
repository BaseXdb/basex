package org.basex.query.func;

import static org.basex.util.Token.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.regex.PatternSyntaxException;

import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
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
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
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
        string(checkEStr(expr[0].atomic(ctx, input))));

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
      case PATHTOURI:
        return pathToUri(ctx);
      case READFILE:
        return read(ctx);
      case READBINARY:
        return readBinary(path);
      case WRITE:
        return write(path, ctx);
      case WRITEBIN:
        return writeBinary(path, ctx);
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

    return new Iter() {
      File[] files;
      int c = -1;

      @Override
      public Item next() throws QueryException {
        if(files == null) {
          files = new File(path).listFiles();
          if(files == null) Err.or(input, QueryText.FILELIST, path);
        }

        while(++c < files.length) {
          final String name = files[c].getName();
          if(!files[c].isHidden() && (pattern == null 
              || name.matches(pattern))) return Str.get(name);
        }
        return null;
      }
    };
  }

  /**
   * Reads the content of a file.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str read(final QueryContext ctx) throws QueryException {
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
    try {
      return new B64(new IOFile(file).content());
    } catch(IOException e) {
      Err.or(input, QueryText.FILEREAD, file.getName());
      return null;
    }
  }

  /**
   * Writes a sequence of items to a file.
   * @param file file to be written
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   */
  private Item write(final File file, final QueryContext ctx)
      throws QueryException {

    final Iter ir = expr[1].iter(ctx);
    final TokenBuilder params = interpretSerialParams(ctx);
    Item n;

    try {
      final BufferedOutputStream out = new BufferedOutputStream(
          new FileOutputStream(file, true));

      try {
        while((n = ir.next()) != null) {
          if(n instanceof Nod) {
            final Nod nod = checkNode(checkItem(n, ctx));
            final Str str = FNGen.serialize(nod, params, input);
            out.write(str.atom());
          } else {
            out.write(Token.token(checkItem(n, ctx).toString()));
          }
        }
      } finally {
        out.close();
      }
    } catch(final IOException e) {
      Err.or(input, QueryText.FILEWRITE, file.getName());
    }
    return null;
  }

  /**
   * Writes the content of a binary file.
   * @param file file to be written
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item writeBinary(final File file, final QueryContext ctx)
      throws QueryException {

    final B64 b64 = (B64) checkType(expr[1].atomic(ctx, input), Type.B6B);

    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      out.write(b64.getVal());
    } catch(IOException ex) {
      Err.or(input, QueryText.FILEWRITE, file.getName());
    } finally {
      try {
        out.close();
      } catch(final Exception ex) { }
    }
    return null;
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
  private Item copy(final QueryContext ctx) throws QueryException {
    final String src = string(checkStr(expr[0], ctx));
    final String dest = string(checkStr(expr[1], ctx));

    try {
      final FileChannel srcc = new FileInputStream(new File(src)).getChannel();
      final FileChannel dc = new FileOutputStream(new File(dest)).getChannel();
      try {
        dc.transferFrom(srcc, 0, srcc.size());
      } finally {
        try {
          srcc.close();
        } catch(final IOException ex) { }
        try {
          dc.close();
        } catch(final IOException ex) { }
      }
    } catch(final IOException ex) {
      Err.or(input, ex.getMessage(), src, dest);
    }
    return null;
  }

  /**
   * Moves a file or directory.
   * @param file file/dir to be moved
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item move(final File file, final QueryContext ctx)
      throws QueryException {

    final String dest = string(checkStr(expr[1], ctx));

    final String destFile = dest.endsWith(Prop.SEP) ? dest + file.getName()
        : dest + Prop.SEP + file.getName();

    // Raise an exception if the target file already exists
    if(new File(destFile).exists()) {
      Err.or(input, QueryText.TARGETEXISTS, file.getPath());
    }

    // Raise an exception if the user has no access
    // to the destination
    if(!new File(dest).canWrite()) {
      Err.or(input, QueryText.FILEMOVE, file.getPath(), dest);
    }

    // Raise an exception if a directory is to be moved
    if(file.isDirectory()) {
      Err.or(input, QueryText.DIRMOVE);
    }

    if(!file.renameTo(new File(dest, file.getName()))) {
      Err.or(input, QueryText.CANNOTMOVE, file.getPath());
    }
    return null;

  }

  /**
   * Creates a directory.
   * @param file directory to be created
   * @param includeParents indicator for including nonexistent parent
   *          directories by the creation
   * @return result
   * @throws QueryException query exception
   */
  private Item makeDir(final File file, final boolean includeParents)
      throws QueryException {

    // Raise an exception if the directory cannot be created because
    // a file with the same name already exists
    if(file.exists()) if(!file.isDirectory()) 
      Err.or(input, QueryText.FILEEXISTS, file.getName());

    if(includeParents) {

      // Raise an exception if the existent directory, in which
      // the dirs are to be created, is write-protected
      final File parent = getExistingParent(file);
      if(!parent.canWrite())
        Err.or(input, QueryText.MKDIR, file.getPath(), parent.getPath());

      if(!file.mkdirs())
        Err.or(input, QueryText.CANNOTMKDIR, file.getPath());
    }

      if(!file.mkdir())
        Err.or(input, QueryText.CANNOTMKDIR, file.getPath());

    return null;
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param ctx query context
   * @return result
   * @throws QueryException query context
   */
  private Uri pathToUri(final QueryContext ctx) throws QueryException {

    final String path = string(checkEStr(expr[0].atomic(ctx, input)));

    final URI uri = new File(path).toURI();
    return Uri.uri(Token.token(uri.toString()));

  }

  /**
   * Deletes a file.
   * @param file file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item delete(final File file) throws QueryException {

    if(!file.exists())
      Err.or(input, QueryText.FILENOTEXISTS, file.getPath());

    if(!file.canWrite())
      Err.or(input, QueryText.FILEDEL, file.getPath());

    if(file.isDirectory() && file.listFiles().length != 0) 
      Err.or(input, QueryText.FILEDELDIR, file.getPath());

    if(!file.delete())
      Err.or(input, QueryText.CANNOTDEL, file.getPath());

    return null;
  }

  /**
   * Returns the lowest existing parent of the directory to be created.
   * @param file directory to be created
   * @return existing parent
   * @throws QueryException query exception
   */
  private File getExistingParent(final File file) throws QueryException {

    try {
      File f = file.getCanonicalFile();

      do {

        f = f.getParentFile();
        if(f == null) Err.or(input, QueryText.DIRINV, file);

      } while(!f.exists());
      return f;
    } catch(IOException ex) {
      Err.or(input, QueryText.DIRINV, file);
      return null;
    }

  }
}
