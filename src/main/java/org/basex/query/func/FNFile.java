package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.basex.core.cmd.Copy;
import org.basex.data.SerializerException;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.PrintOutput;
import org.basex.io.TextInput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
public final class FNFile extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFile(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);

    switch(def) {
      case FLIST:
        return list(ctx);
      case READLINES:
        return readTextLines(new File(string(checkStr(expr[0], ctx))), ctx);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    checkAdmin(ctx);
    final File path = expr.length == 0 ? null : new File(
        string(checkStr(expr[0], ctx)));

    switch(def) {
      case APPEND:      return write(path, ctx, true);
      case APPENDBIN:   return writeBinary(path, ctx, true);
      case COPY:        return transfer(path, ctx, true);
      case CREATEDIR:   return createDirectory(path);
      case DELETE:      return del(path);
      case MOVE:        return transfer(path, ctx, false);
      case READBIN:     return readBinary(path);
      case READTEXT:    return readText(path, ctx);
      case WRITE:       return write(path, ctx, false);
      case WRITEBIN:    return writeBinary(path, ctx, false);
      case FEXISTS:     return Bln.get(path.exists());
      case ISDIR:       return Bln.get(path.isDirectory());
      case ISFILE:      return Bln.get(path.isFile());
      case LASTMOD:     return lastModified(path);
      case SIZE:        return size(path);
      case BASENAME:    return baseName(path, ctx);
      case DIRNAME:     return dirName(path);
      case PATHNATIVE:  return pathToNative(path);
      case RESOLVEPATH: return Str.get(path.getAbsolutePath());
      case PATHTOURI:   return pathToUri(path);
      default:          return super.item(ctx, ii);
    }
  }

  /**
   * Returns the last modified date of the specified path.
   * @param path file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item lastModified(final File path) throws QueryException {
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    return new Dtm(path.lastModified(), input);
  }

  /**
   * Returns the size of the specified path.
   * @param path file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item size(final File path) throws QueryException {
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);
    return Itr.get(path.length());
  }

  /**
   * Returns the base name of the specified path.
   * @param path file path
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str baseName(final File path, final QueryContext ctx)
      throws QueryException {

    if(path.getPath().length() == 0) return Str.get(".");
    final String suf = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    String pth = path.getName();
    if(suf != null && pth.endsWith(suf))
      pth = pth.substring(0, pth.length() - suf.length());
    return Str.get(pth);
  }

  /**
   * Returns the dir name of the specified path.
   * @param path file path
   * @return result
   */
  private Str dirName(final File path) {
    final String pth = path.getParent();
    return Str.get(pth == null ? "." : pth);
  }

  /**
   * Returns the native name of the specified path.
   * @param path file path
   * @return result
   * @throws QueryException query exception
   */
  private Str pathToNative(final File path) throws QueryException {
    try {
      return Str.get(path.getCanonicalFile());
    } catch(final IOException ex) {
      throw PATHINVALID.thrw(input, path);
    }
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param path file path
   * @return result
   */
  private Uri pathToUri(final File path) {
    return Uri.uri(token(path.toURI().toString()));
  }

  /**
   * Lists all files in a directory.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter list(final QueryContext ctx) throws QueryException {
    final String path = string(checkStr(expr[0], ctx));
    final IO dir = new IOFile(path);

    // Check if not a directory
    if(!dir.isDir()) NOTDIR.thrw(input, path);

    final boolean rec = optionalBool(1, ctx);
    final String pat = expr.length != 3 ? ".*" :
      IOFile.regex(string(checkStr(expr[2], ctx)));

    IO[] fl;
    if(rec) {
      final List<IO> list = new ArrayList<IO>();
      recList(dir, list, pat);
      fl = list.toArray(new IO[list.size()]);
    } else {
      fl = dir.children(pat);
    }
    final IO[] files = fl;

    return new Iter() {
      int c = -1;

      @Override
      public Item next() {
        return ++c < files.length ? Str.get(files[c].path()) : null;
      }
    };
  }

  /**
   * Lists the files in a directory recursively.
   * @param dir current directory
   * @param list file list
   * @param pat file name pattern
   */
  private void recList(final IO dir, final List<IO> list, final String pat) {
    for(final IO f : dir.children()) if(f.isDir()) recList(f, list, pat);
    for(final IO f : dir.children(pat)) list.add(f);
  }

  /**
   * Creates a directory.
   * @param path directory to be created
   * @return result
   * @throws QueryException query exception
   */
  private Item createDirectory(final File path) throws QueryException {
    // resolve symbolic links
    File f = null;
    try {
      f = path.getCanonicalFile();
    } catch(final IOException ex) {
      throw PATHINVALID.thrw(input, path);
    }

    // find lowest existing path
    while(!f.exists()) {
      f = f.getParentFile();
      if(f == null) throw PATHINVALID.thrw(input, path);
    }
    // warn if lowest path points to a file
    if(f.isFile()) FILEEXISTS.thrw(input, path);

    // only create directories if path does not exist yet
    if(!path.exists() && !path.mkdirs()) CANNOTCREATE.thrw(input, path);
    return null;
  }

  /**
   * Deletes a file or directory.
   * @param path file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item del(final File path) throws QueryException {
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    deleteRec(path);
    return null;
  }

  /**
   * Recursively deletes a file path.
   * @param path path to be deleted
   * @throws QueryException query exception
   */
  private void deleteRec(final File path) throws QueryException {
    final File[] ch = path.listFiles();
    if(ch != null) for(final File f : ch) deleteRec(f);
    if(!path.delete()) CANNOTDEL.thrw(input, path);
  }

  /**
   * Reads the contents of a binary file.
   * @param path input path
   * @return Base64Binary
   * @throws QueryException query exception
   */
  private B64 readBinary(final File path) throws QueryException {
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    try {
      return new B64(new IOFile(path).content());
    } catch(final IOException ex) {
      throw FILEERROR.thrw(input, ex);
    }
  }

  /**
   * Reads the contents of a file.
   * @param path input path
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str readText(final File path, final QueryContext ctx)
      throws QueryException {

    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);
    if(enc != null && !Charset.isSupported(enc)) ENCNOTEXISTS.thrw(input, enc);

    try {
      byte[] txt = TextInput.content(new IOFile(path), enc).finish();
      if(contains(txt, '\r')) txt = contains(txt, '\n') ?
          delete(txt, '\r') : replace(txt, '\r', '\n');
      return Str.get(txt);
    } catch(final IOException ex) {
      throw FILEERROR.thrw(input, ex);
    }
  }

  /**
   * Returns the contents of a file line by line.
   * @param path input path
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Iter readTextLines(final File path, final QueryContext ctx)
      throws QueryException {
    return FNGen.textIter(readText(path, ctx).atom());
  }

  /**
   * Writes a sequence of items to a file.
   * @param path file to be written
   * @param ctx query context
   * @param append append flag
   * @return true if file was successfully written
   * @throws QueryException query exception
   */
  private Item write(final File path, final QueryContext ctx,
      final boolean append) throws QueryException {

    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    final Iter ir = expr[1].iter(ctx);
    try {
      final PrintOutput out = PrintOutput.get(
          new FileOutputStream(path, append));
      try {
        final XMLSerializer xml = new XMLSerializer(out,
            FNGen.serialPar(this, 2, ctx));
        for(Item it; (it = ir.next()) != null;) it.serialize(xml);
        xml.close();
      } catch(final SerializerException ex) {
        throw new QueryException(input, ex);
      } finally {
        out.close();
      }
    } catch(final IOException ex) {
      FILEERROR.thrw(input, ex);
    }
    return null;
  }

  /**
   * Writes the content of a binary file.
   * @param path file to be written
   * @param ctx query context
   * @param append append flag
   * @return result
   * @throws QueryException query exception
   */
  private Item writeBinary(final File path, final QueryContext ctx,
      final boolean append) throws QueryException {

    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    try {
      final FileOutputStream out = new FileOutputStream(path, append);
      try {
        final Iter ir = expr[1].iter(ctx);
        for(Item it; (it = ir.next()) != null;) {
          out.write(((B64) checkType(it, AtomType.B64)).toJava());
        }
      } finally {
        out.close();
      }
    } catch(final IOException ex) {
      FILEERROR.thrw(input, ex);
    }
    return null;
  }

  /**
   * Transfers a file path, given a source and a target.
   * @param src source file to be copied
   * @param ctx query context
   * @param copy copy flag (no move)
   * @return result
   * @throws QueryException query exception
   */
  private Item transfer(final File src, final QueryContext ctx,
      final boolean copy) throws QueryException {

    File trg = new File(string(checkStr(expr[1], ctx))).getAbsoluteFile();
    if(!src.exists()) PATHNOTEXISTS.thrw(input, src);

    if(trg.isDirectory()) {
      // target is a directory: attach file name
      trg = new File(trg, src.getName());
    } else if(!trg.isFile()) {
      // target does not exist: ensure that parent exists
      if(!trg.getParentFile().isDirectory()) NOTDIR.thrw(input, trg);
    } else if(src.isDirectory()) {
      // if target is file, source cannot be a directory
      PATHISDIR.thrw(input, src);
    }

    // ignore operations on same source and target path
    if(!src.equals(trg)) {
      if(copy) copy(src, trg);
      else if(!src.renameTo(trg)) CANNOTMOVE.thrw(input, src, trg);
    }
    return null;
  }

  /**
   * Recursively copies files.
   * @param src source path
   * @param trg target path
   * @throws QueryException query exception
   */
  private void copy(final File src, final File trg) throws QueryException {
    if(src.isDirectory()) {
      if(!trg.mkdir()) CANNOTCREATE.thrw(input, trg);
      final File[] files = src.listFiles();
      if(files == null) CANNOTLIST.thrw(input, src);
      for(final File f : files) copy(f, new File(trg, f.getName()));
    } else {
      try {
        Copy.copy(src, trg);
      } catch(final IOException ex) {
        FILEERROR.thrw(input, ex);
      }
    }
  }

  /**
   * Returns the value of an optional boolean.
   * @param i argument index
   * @param ctx query context
   * @return boolean value
   * @throws QueryException query exception
   */
  private boolean optionalBool(final int i, final QueryContext ctx)
      throws QueryException {
    return expr.length > i &&
      checkType(expr[i].item(ctx, input), AtomType.BLN).bool(input);
  }

  @Override
  public boolean uses(final Use u) {
    // prevent instant execution
    return u == Use.CTX || super.uses(u);
  }
}
