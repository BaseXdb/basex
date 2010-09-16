package org.basex.query.func;

import static org.basex.util.Token.*;
import static org.basex.query.util.Err.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.regex.PatternSyntaxException;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkAdmin(ctx);

    final File path = expr.length == 0 ? null : new File(
        string(checkEStr(expr[0].item(ctx, input))));

    switch(def) {
      case FILEEXISTS:
        return Bln.get(path.exists());
      case ISDIR:
        return Bln.get(path.isDirectory());
      case ISFILE:
        return Bln.get(path.isFile());
      case ISREAD:
        return Bln.get(path.canRead());
      case ISWRITE:
        return Bln.get(path.canWrite());
      case SIZE:
        return Itr.get(path.length());
      case LASTMOD:
        return new Dtm(path.lastModified(), input);
      case PATHSEP:
        return Str.get(Prop.SEP);
      case PATHTOFULL:
        return Str.get(path.getAbsolutePath());
      case PATHTOURI:
        return pathToUri(ctx);
      case MKDIR:
        return makeDir(path, false);
      case MKDIRS:
        return makeDir(path, true);
      case DELETE:
        return delete(path);
      case READFILE:
        return read(ctx);
      case READBIN:
        return readBinary(path);
      case WRITE:
        return write(path, ctx);
      case WRITEBIN:
        return writeBinary(path, ctx);
      case COPY:
        return copy(path, ctx);
      case MOVE:
        return move(path, ctx);
      default:
        return super.item(ctx, ii);
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
      FILEPATTERN.thrw(input, expr[1]);
      return null;
    }

    return new Iter() {
      File[] files;
      int c = -1;

      @Override
      public Item next() throws QueryException {
        if(files == null) {
          files = new File(path).listFiles();
          if(files == null) FILELIST.thrw(input, path);
        }

        while(++c < files.length) {
          final String name = files[c].getName();
          if(!files[c].isHidden() && (pattern == null  ||
              name.matches(pattern))) return Str.get(name);
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
    // TODO [RS] check if encoding exists
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
      FILEREAD.thrw(input, file.getName());
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
    try {
      final BufferedOutputStream out = new BufferedOutputStream(
          new FileOutputStream(file, true));
      try {
        final XMLSerializer xml = new XMLSerializer(out,
            FNGen.serialPar(this, 2, ctx));
        Item it;
        while((it = ir.next()) != null) it.serialize(xml);
        xml.close();
      } finally {
        out.close();
      }
    } catch(final IOException e) {
      FILEWRITE.thrw(input, file.getName());
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

    final B64 b64 = (B64) checkType(expr[1].item(ctx, input), Type.B6B);
    try {
      final FileOutputStream out = new FileOutputStream(file);
      try {
        out.write(b64.getVal());
      } finally {
        out.close();
      }
    } catch(IOException ex) {
      FILEWRITE.thrw(input, file.getName());
    }
    return null;
  }

  /**
   * Copies a file given a source and a destination.
   * @param src source file to be copied
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item copy(final File src, final QueryContext ctx)
    throws QueryException {

    final File dst = new File(string(checkStr(expr[1], ctx)));
    if(!src.exists()) PATHNOTEXISTS.thrw(input, src);

    try {
      final FileChannel sc = new FileInputStream(src).getChannel();
      final FileChannel dc = new FileOutputStream(dst).getChannel();
      try {
        dc.transferFrom(sc, 0, sc.size());
      } finally {
        sc.close();
        dc.close();
      }
    } catch(final IOException ex) {
      COPYFAILED.thrw(input, src, dst, ex.getMessage());
    }
    return null;
  }

  /**
   * Moves a file or directory.
   * @param src source file/dir to be moved
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item move(final File src, final QueryContext ctx)
      throws QueryException {

    final String dest = string(checkStr(expr[1], ctx));
    if(!src.exists()) PATHNOTEXISTS.thrw(input, src);

    final String destFile = dest.endsWith(Prop.SEP) ? dest + src.getName() :
      dest + Prop.SEP + src.getName();

    // Raise an exception if the target file already exists
    if(new File(destFile).exists()) TARGETEXISTS.thrw(input, destFile);

    // Raise an exception if the user has no access
    // to the destination
    if(!new File(dest).canWrite()) FILEMOVE.thrw(input, src.getPath(), dest);

    // Raise an exception if a directory is to be moved
    if(src.isDirectory()) DIRMOVE.thrw(input);

    if(!src.renameTo(new File(dest, src.getName())))
      CANNOTMOVE.thrw(input, src.getPath());
    return null;
  }

  /**
   * Creates a directory.
   * @param file directory to be created
   * @param recursive indicator for including nonexistent parent
   *          directories by the creation
   * @return result
   * @throws QueryException query exception
   */
  private Item makeDir(final File file, final boolean recursive)
      throws QueryException {

    // Raise an exception if the directory cannot be created because
    // a file with the same name already exists
    if(file.exists() && !file.isDirectory())
      FILEEXISTS.thrw(input, file.getName());

    if(recursive) {
      // Raise an exception if the existent directory, in which
      // the dirs are to be created, is write-protected
      final File parent = getExistingParent(file);
      if(!parent.canWrite())
        MKDIR.thrw(input, file.getPath(), parent.getPath());

      if(!file.mkdirs()) CANNOTMKDIR.thrw(input, file.getPath());
    } else {
      if(!file.mkdir()) CANNOTMKDIR.thrw(input, file.getPath());
    }
    
    return null;
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param ctx query context
   * @return result
   * @throws QueryException query context
   */
  private Uri pathToUri(final QueryContext ctx) throws QueryException {

    final String path = string(checkEStr(expr[0].item(ctx, input)));

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

    if(!file.exists()) PATHNOTEXISTS.thrw(input, file.getPath());

    if(!file.canWrite()) FILEDEL.thrw(input, file.getPath());

    if(file.isDirectory() && file.listFiles().length != 0) 
      FILEDELDIR.thrw(input, file.getPath());

    if(!file.delete()) CANNOTDEL.thrw(input, file.getPath());

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
        if(f == null) DIRINV.thrw(input, file);

      } while(!f.exists());
      return f;
    } catch(final IOException ex) {
      DIRINV.thrw(input, file);
      return null;
    }

  }
}
