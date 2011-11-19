package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.basex.core.Prop;
import org.basex.core.cmd.Copy;
import org.basex.io.IOFile;
import org.basex.io.in.TextInput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.list.StringList;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
public final class FNFile extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFile(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);

    switch(def) {
      case _FILE_LIST:
        return list(ctx);
      case _FILE_READ_TEXT_LINES:
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
      case _FILE_APPEND:           return write(path, ctx, true);
      case _FILE_APPEND_BINARY:    return writeBinary(path, ctx, true);
      case _FILE_COPY:             return copy(path, ctx, true);
      case _FILE_CREATE_DIRECTORY: return createDirectory(path);
      case _FILE_DELETE:           return del(path);
      case _FILE_MOVE:             return copy(path, ctx, false);
      case _FILE_READ_BINARY:      return readBinary(path);
      case _FILE_READ_TEXT:        return readText(path, ctx);
      case _FILE_WRITE:            return write(path, ctx, false);
      case _FILE_WRITE_BINARY:     return writeBinary(path, ctx, false);
      case _FILE_EXISTS:           return Bln.get(path.exists());
      case _FILE_IS_DIRECTORY:     return Bln.get(path.isDirectory());
      case _FILE_IS_FILE:          return Bln.get(path.isFile());
      case _FILE_LAST_MODIFIED:    return lastModified(path);
      case _FILE_SIZE:             return size(path);
      case _FILE_BASE_NAME:        return baseName(path, ctx);
      case _FILE_DIR_NAME:         return dirName(path);
      case _FILE_PATH_TO_NATIVE:   return pathToNative(path);
      case _FILE_RESOLVE_PATH:     return Str.get(path.getAbsolutePath());
      case _FILE_PATH_TO_URI:      return pathToUri(path);
      default:                     return super.item(ctx, ii);
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
    final File dir = new File(path);

    // check if not a directory
    if(!dir.isDirectory()) NOTDIR.thrw(input, path);

    final boolean rec = optionalBool(1, ctx);
    final Pattern pat = expr.length != 3 ? null :
      Pattern.compile(IOFile.regex(string(checkStr(expr[2], ctx))),
        Prop.WIN ? Pattern.CASE_INSENSITIVE : 0);

    final StringList list = new StringList();
    list(dir, list, rec, pat);

    return new Iter() {
      int c = -1;

      @Override
      public Item next() {
        return ++c < list.size() ? Str.get(list.get(c)) : null;
      }
    };
  }

  /**
   * Collects the sub-directories and files of the specified directory.
   * @param dir root directory
   * @param list file list
   * @param rec recursive flag
   * @param pat file name pattern; ignored if {@code null}
   * @throws QueryException query exception
   */
  private void list(final File dir, final StringList list,
      final boolean rec, final Pattern pat) throws QueryException {

    // skip invalid directories
    final File[] ch = dir.listFiles();
    if(ch == null) return;

    // parse directories
    if(rec) {
      for(final File f : ch) {
        if(!mayBeLink(f) && f.isDirectory()) list(f, list, rec, pat);
      }
    }
    // parse files. ignore directories if a pattern is specified
    for(final File f : ch) {
      if(!mayBeLink(f) && (pat == null || pat.matcher(f.getName()).matches() &&
          !f.isDirectory())) list.add(f.getPath());
    }
  }

  /**
   * Checks if the specified file may be a symbolic link.
   * @param f file to check
   * @return result
   * @throws QueryException query exception
   */
  private boolean mayBeLink(final File f) throws QueryException {
    try {
      final String p1 = f.getAbsolutePath();
      final String f2 = f.getCanonicalPath();
      return !(Prop.WIN ? p1.equalsIgnoreCase(f2) : p1.equals(f2));
    } catch(final IOException ex) {
      throw PATHINVALID.thrw(input, f);
    }
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
      return new B64(new IOFile(path).read());
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
      return Str.get(TextInput.content(new IOFile(path), enc).finish());
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
        final Serializer ser = Serializer.get(out, serialPar(this, 2, ctx));
        for(Item it; (it = ir.next()) != null;) it.serialize(ser);
        ser.close();
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
   * Writes an item to a file.
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
        for(Item it; (it = ir.next()) != null;) out.write(checkBin(it, ctx));
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
  private Item copy(final File src, final QueryContext ctx,
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
    return u == Use.NDT || super.uses(u);
  }
}
