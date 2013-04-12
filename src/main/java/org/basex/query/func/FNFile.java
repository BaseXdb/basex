package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
public final class FNFile extends StandardFunc {
  /** Line separator. */
  private static final byte[] NL = Token.token(Prop.NL);

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
    checkCreate(ctx);
    switch(sig) {
      case _FILE_LIST:            return list(file(0, ctx), ctx);
      case _FILE_READ_TEXT_LINES: return readTextLines(file(0, ctx), ctx);
      default:                    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    try {
      switch(sig) {
        case _FILE_APPEND:            return write(file(0, ctx), true, ctx);
        case _FILE_APPEND_BINARY:     return writeBinary(file(0, ctx), ctx, true);
        case _FILE_APPEND_TEXT:       return writeText(file(0, ctx), true, ctx);
        case _FILE_APPEND_TEXT_LINES: return writeTextLines(file(0, ctx), true, ctx);
        case _FILE_COPY:              return copy(file(0, ctx), ctx, true);
        case _FILE_CREATE_DIR:        return createDirectory(file(0, ctx));
        case _FILE_DELETE:            return delete(file(0, ctx), ctx);
        case _FILE_MOVE:              return copy(file(0, ctx), ctx, false);
        case _FILE_READ_BINARY:       return readBinary(file(0, ctx));
        case _FILE_READ_TEXT:         return readText(file(0, ctx), ctx);
        case _FILE_WRITE:             return write(file(0, ctx), false, ctx);
        case _FILE_WRITE_BINARY:      return writeBinary(file(0, ctx), ctx, false);
        case _FILE_WRITE_TEXT:        return writeText(file(0, ctx), false, ctx);
        case _FILE_WRITE_TEXT_LINES:  return writeTextLines(file(0, ctx), false, ctx);
        case _FILE_PATH_SEPARATOR:    return Str.get(File.pathSeparator);
        case _FILE_DIR_SEPARATOR:     return Str.get(File.separator);
        case _FILE_LINE_SEPARATOR:    return Str.get(NL);
        case _FILE_EXISTS:            return Bln.get(file(0, ctx).exists());
        case _FILE_IS_DIR:            return Bln.get(file(0, ctx).isDirectory());
        case _FILE_IS_FILE:           return Bln.get(file(0, ctx).isFile());
        case _FILE_LAST_MODIFIED:     return lastModified(file(0, ctx));
        case _FILE_SIZE:              return size(file(0, ctx));
        case _FILE_BASE_NAME:         return baseName(file(0, ctx), ctx);
        case _FILE_DIR_NAME:          return dirName(file(0, ctx));
        case _FILE_PATH_TO_NATIVE:    return pathToNative(file(0, ctx));
        case _FILE_RESOLVE_PATH:      return Str.get(file(0, ctx).getAbsolutePath());
        case _FILE_PATH_TO_URI:       return pathToUri(file(0, ctx));
        default:                      return super.item(ctx, ii);
      }
    } catch(final IOException ex) {
      throw FILE_IO.thrw(info, ex);
    }
  }

  /**
   * Returns the last modified date of the specified path.
   * @param path file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item lastModified(final File path) throws QueryException {
    if(!path.exists()) FILE_WHICH.thrw(info, path.getAbsolutePath());
    return new Dtm(path.lastModified(), info);
  }

  /**
   * Returns the size of the specified path.
   * @param path file to be deleted
   * @return result
   * @throws QueryException query exception
   */
  private Item size(final File path) throws QueryException {
    if(!path.exists()) FILE_WHICH.thrw(info, path.getAbsolutePath());
    if(path.isDirectory()) FILE_DIR.thrw(info, path.getAbsolutePath());
    return Int.get(path.length());
  }

  /**
   * Returns the base name of the specified path.
   * @param path file path
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str baseName(final File path, final QueryContext ctx) throws QueryException {
    if(path.getPath().isEmpty()) return Str.get(".");
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
  private static Str dirName(final File path) {
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
      throw FILE_PATH.thrw(info, path);
    }
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param path file path
   * @return result
   */
  private static Uri pathToUri(final File path) {
    return Uri.uri(path.toURI().toString());
  }

  /**
   * Lists all files in a directory.
   * @param path root directory
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter list(final File path, final QueryContext ctx) throws QueryException {
    // get canonical representation to resolve symbolic links
    final File dir;
    try {
      dir = new File(path.getCanonicalPath());
    } catch(final IOException ex) {
      throw FILE_PATH.thrw(info, path);
    }

    // check if the addresses path is a directory
    if(!dir.isDirectory()) FILE_NODIR.thrw(info, dir);

    final boolean rec = optionalBool(1, ctx);
    final Pattern pat = expr.length != 3 ? null :
      Pattern.compile(IOFile.regex(string(checkStr(expr[2], ctx))),
        Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE);

    final StringList list = new StringList();
    final String p = dir.getPath();
    final int l = p.length() + (p.endsWith(File.separator) ? 0 : 1);
    list(l, dir, list, rec, pat);

    return new Iter() {
      int c;
      @Override
      public Item next() {
        return c < list.size() ? Str.get(list.get(c++)) : null;
      }
    };
  }

  /**
   * Collects the sub-directories and files of the specified directory.
   * @param root length of root path
   * @param dir root directory
   * @param list file list
   * @param rec recursive flag
   * @param pat file name pattern; ignored if {@code null}
   * @throws QueryException query exception
   */
  private void list(final int root, final File dir, final StringList list,
      final boolean rec, final Pattern pat) throws QueryException {

    // skip invalid directories
    final File[] ch = dir.listFiles();
    if(ch == null) return;

    // parse directories, do not follow links
    if(rec) {
      for(final File f : ch) {
        if(f.isDirectory() && !mayBeLink(f)) list(root, f, list, rec, pat);
      }
    }
    // parse files. ignore directories if a pattern is specified
    for(final File f : ch) {
      if(pat == null || pat.matcher(f.getName()).matches()) {
        list.add(f.getPath().substring(root));
      }
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
      final String p2 = f.getCanonicalPath();
      return !(Prop.CASE ? p1.equals(p2) : p1.equalsIgnoreCase(p2));
    } catch(final IOException ex) {
      throw FILE_PATH.thrw(info, f);
    }
  }

  /**
   * Creates a directory.
   * @param path directory to be created
   * @return result
   * @throws QueryException query exception
   */
  private synchronized Item createDirectory(final File path) throws QueryException {
    // resolve symbolic links
    File f;
    try {
      f = path.getCanonicalFile();
    } catch(final IOException ex) {
      throw FILE_PATH.thrw(info, path);
    }

    // find lowest existing path
    while(!f.exists()) {
      f = f.getParentFile();
      if(f == null) throw FILE_PATH.thrw(info, path);
    }
    // warn if lowest path points to a file
    if(f.isFile()) FILE_EXISTS.thrw(info, path);

    // only create directories if path does not exist yet
    if(!path.exists() && !path.mkdirs()) FILE_CREATE.thrw(info, path);
    return null;
  }

  /**
   * Deletes a file or directory.
   * @param path file to be deleted
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private synchronized Item delete(final File path, final QueryContext ctx)
      throws QueryException {

    if(!path.exists()) FILE_WHICH.thrw(info, path.getAbsolutePath());
    if(optionalBool(1, ctx)) {
      deleteRec(path);
    } else if(!path.delete()) {
      throw (path.isDirectory() ? FILE_NEDIR : FILE_DEL).thrw(info, path);
    }
    return null;
  }

  /**
   * Recursively deletes a file path.
   * @param path path to be deleted
   * @throws QueryException query exception
   */
  private synchronized void deleteRec(final File path) throws QueryException {
    final File[] ch = path.listFiles();
    if(ch != null) for(final File f : ch) deleteRec(f);
    if(!path.delete()) FILE_DEL.thrw(info, path);
  }

  /**
   * Reads the contents of a binary file.
   * @param path input path
   * @return Base64Binary
   * @throws QueryException query exception
   */
  private B64Stream readBinary(final File path) throws QueryException {
    if(!path.exists()) FILE_WHICH.thrw(info, path.getAbsolutePath());
    if(path.isDirectory()) FILE_DIR.thrw(info, path.getAbsolutePath());
    return new B64Stream(new IOFile(path), FILE_IO);
  }

  /**
   * Reads the contents of a file.
   * @param path input path
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private StrStream readText(final File path, final QueryContext ctx)
      throws QueryException {

    final String enc = encoding(1, FILE_ENCODING, ctx);
    if(!path.exists()) FILE_WHICH.thrw(info, path.getAbsolutePath());
    if(path.isDirectory()) FILE_DIR.thrw(info, path.getAbsolutePath());
    return new StrStream(new IOFile(path), enc, FILE_IO);
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
    return FNGen.textIter(readText(path, ctx).string(info));
  }

  /**
   * Writes items to a file.
   * @param path file to be written
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item write(final File path, final boolean append,
      final QueryContext ctx) throws QueryException, IOException {

    check(path);
    final Iter ir = expr[1].iter(ctx);
    final SerializerProp sp = FuncParams.serializerProp(
        expr.length > 2 ? expr[2].item(ctx, info) : null);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      final Serializer ser = Serializer.get(out, sp);
      for(Item it; (it = ir.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes items to a file.
   * @param path file to be written
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeText(final File path, final boolean append,
      final QueryContext ctx) throws QueryException, IOException {

    check(path);
    final byte[] s = checkStr(expr[1], ctx);
    final String enc = encoding(2, FILE_ENCODING, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      out.write(cs == null ? s : Token.string(s).getBytes(cs));
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes items to a file.
   * @param path file to be written
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeTextLines(final File path, final boolean append,
      final QueryContext ctx) throws QueryException, IOException {

    check(path);
    final Iter ir = expr[1].iter(ctx);
    final String enc = encoding(2, FILE_ENCODING, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      for(Item it; (it = ir.next()) != null;) {
        if(!it.type.isStringOrUntyped()) Err.type(this, AtomType.STR, it);
        final byte[] s = it.string(info);
        out.write(cs == null ? s : Token.string(s).getBytes(cs));
        out.write(cs == null ? NL : Prop.NL.getBytes(cs));
      }
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes binary items to a file.
   * @param path file to be written
   * @param ctx query context
   * @param append append flag
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeBinary(final File path, final QueryContext ctx,
      final boolean append) throws QueryException, IOException {

    check(path);
    final Iter ir = expr[1].iter(ctx);
    final BufferOutput out = new BufferOutput(new FileOutputStream(path, append));
    try {
      for(Item it; (it = ir.next()) != null;) {
        if(!(it instanceof Bin)) BINARYTYPE.thrw(info, it.type);
        final InputStream is = it.input(info);
        try {
          for(int i; (i = is.read()) != -1;)  out.write(i);
        } finally {
          is.close();
        }
      }
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Checks the target directory of the specified file.
   * @param path file to be written
   * @throws QueryException query exception
   */
  private void check(final File path) throws QueryException {
    final IOFile io = new IOFile(path);
    if(io.isDir()) FILE_DIR.thrw(info, io);
    final IOFile dir = io.dir();
    if(!dir.exists()) FILE_NODIR.thrw(info, dir);
  }

  /**
   * Transfers a file path, given a source and a target.
   * @param src source file to be copied
   * @param ctx query context
   * @param copy copy flag (no move)
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item copy(final File src, final QueryContext ctx,
      final boolean copy) throws QueryException, IOException {

    File trg = file(1, ctx).getAbsoluteFile();
    if(!src.getCanonicalFile().exists()) FILE_WHICH.thrw(info, src.getAbsolutePath());

    if(trg.isDirectory()) {
      // target is a directory: attach file name
      trg = new File(trg, src.getName());
      if(trg.isDirectory()) FILE_DIR.thrw(info, trg);
    } else if(!trg.isFile()) {
      // target does not exist: ensure that parent exists
      if(!trg.getParentFile().isDirectory()) FILE_NODIR.thrw(info, trg);
    } else if(src.isDirectory()) {
      // if target is file, source cannot be a directory
      FILE_DIR.thrw(info, src);
    }

    // ignore operations on identical, canonical source and target path
    final String spath = src.getCanonicalPath();
    final String tpath = trg.getCanonicalPath();
    if(!spath.equals(tpath)) {
      if(copy) {
        copy(src, trg);
      } else {
        // delete target if it is different to source (case is ignored on Windows and Mac)
        if(trg.exists() && (Prop.CASE || !spath.equalsIgnoreCase(tpath)) && !trg.delete())
          FILE_DEL.thrw(info, src, trg);
        if(!src.renameTo(trg)) FILE_MOVE.thrw(info, src, trg);
      }
    }
    return null;
  }

  /**
   * Recursively copies files.
   * @param src source path
   * @param trg target path
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized void copy(final File src, final File trg)
      throws QueryException, IOException {

    if(src.isDirectory()) {
      if(!trg.mkdir()) FILE_CREATE.thrw(info, trg);
      final File[] files = src.listFiles();
      if(files == null) FILE_LIST.thrw(info, src);
      for(final File f : files) copy(f, new File(trg, f.getName()));
    } else {
      new IOFile(src).copyTo(new IOFile(trg));
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
    return i < expr.length && checkBln(expr[i], ctx);
  }

  /**
   * Converts the specified argument to a file instance.
   * @param i argument index
   * @param ctx query context
   * @return file instance
   * @throws QueryException query exception
   */
  private File file(final int i, final QueryContext ctx) throws QueryException {
    return i >= expr.length ? null :
      new IOFile(IOUrl.file(string(checkStr(expr[i], ctx)))).file();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT && !oneOf(sig, _FILE_BASE_NAME, _FILE_DIR_NAME,
        _FILE_DIR_SEPARATOR, _FILE_PATH_SEPARATOR, _FILE_PATH_TO_NATIVE,
        _FILE_PATH_TO_URI, _FILE_RESOLVE_PATH) || super.uses(u);
  }
}
