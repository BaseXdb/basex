package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.random.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 * @author Christian Gruen
 */
public final class FNFile extends StandardFunc {
  /** Line separator. */
  private static final byte[] NL = token(Prop.NL);

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFile(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _FILE_LIST:            return list(ctx);
      case _FILE_READ_TEXT_LINES: return readTextLines(ctx);
      default:                    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    try {
      switch(sig) {
        case _FILE_APPEND:            return write(true, ctx);
        case _FILE_APPEND_BINARY:     return writeBinary(true, ctx);
        case _FILE_APPEND_TEXT:       return writeText(true, ctx);
        case _FILE_APPEND_TEXT_LINES: return writeTextLines(true, ctx);
        case _FILE_NAME:              return name(ctx);
        case _FILE_COPY:              return relocate(true, ctx);
        case _FILE_CREATE_DIR:        return createDir(ctx);
        case _FILE_CREATE_TEMP_DIR:   return createTemp(true, ctx);
        case _FILE_CREATE_TEMP_FILE:  return createTemp(false, ctx);
        case _FILE_DELETE:            return delete(ctx);
        case _FILE_PARENT:            return parent(ctx);
        case _FILE_DIR_SEPARATOR:     return Str.get(File.separator);
        case _FILE_EXISTS:            return Bln.get(checkFile(0, ctx).exists());
        case _FILE_IS_DIR:            return Bln.get(checkFile(0, ctx).isDirectory());
        case _FILE_IS_FILE:           return Bln.get(checkFile(0, ctx).isFile());
        case _FILE_LAST_MODIFIED:     return lastModified(ctx);
        case _FILE_LINE_SEPARATOR:    return Str.get(NL);
        case _FILE_MOVE:              return relocate(false, ctx);
        case _FILE_PATH_SEPARATOR:    return Str.get(File.pathSeparator);
        case _FILE_PATH_TO_NATIVE:    return pathToNative(ctx);
        case _FILE_PATH_TO_URI:       return pathToUri(ctx);
        case _FILE_READ_BINARY:       return readBinary(ctx);
        case _FILE_READ_TEXT:         return readText(ctx);
        case _FILE_RESOLVE_PATH:      return resolvePath(ctx);
        case _FILE_SIZE:              return size(ctx);
        case _FILE_TEMP_DIR:          return Str.get(Prop.TMP);
        case _FILE_WRITE:             return write(false, ctx);
        case _FILE_WRITE_BINARY:      return writeBinary(false, ctx);
        case _FILE_WRITE_TEXT:        return writeText(false, ctx);
        case _FILE_WRITE_TEXT_LINES:  return writeTextLines(false, ctx);
        default:                      return super.item(ctx, ii);
      }
    } catch(final IOException ex) {
      throw FILE_IE.get(info, ex);
    }
  }

  /**
   * Returns the last modified date of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item lastModified(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    if(!path.exists()) throw FILE_NF.get(info, path.getAbsolutePath());
    return new Dtm(path.lastModified(), info);
  }

  /**
   * Returns the size of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item size(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    if(!path.exists()) throw FILE_NF.get(info, path.getAbsolutePath());
    return Int.get(path.isDirectory() ? 0 : path.length());
  }

  /**
   * Returns the base name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str name(final QueryContext ctx) throws QueryException {
    return Str.get(checkFile(0, ctx).getName());
  }

  /**
   * Returns the directory name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str parent(final QueryContext ctx) throws QueryException {
    final String parent = checkFile(0, ctx).getAbsoluteFile().getParent();
    return parent == null ? null : Str.get(dir(parent));
  }

  /**
   * Returns the native name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str pathToNative(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    try {
      final String nat = path.getCanonicalFile().getPath();
      return Str.get(path.isDirectory() ? dir(nat) : nat);
    } catch(final IOException ex) {
      throw FILE_IE_PATH.get(info, path);
    }
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Uri pathToUri(final QueryContext ctx) throws QueryException {
    return Uri.uri(checkFile(0, ctx).toURI().toString());
  }

  /**
   * Returns an absolute file path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str resolvePath(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    final File abs = path.getAbsoluteFile();
    return Str.get(abs.isDirectory() ? dir(abs.getPath()) : abs.getPath());
  }

  /**
   * Lists all files in a directory.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter list(final QueryContext ctx) throws QueryException {
    // get canonical representation to resolve symbolic links
    File dir = checkFile(0, ctx);
    try {
      dir = new File(dir.getCanonicalPath());
    } catch(final IOException ex) {
      throw FILE_IE_PATH.get(info, dir);
    }

    // check if the addresses path is a directory
    if(!dir.isDirectory()) throw FILE_ND.get(info, dir);

    final boolean rec = optionalBool(1, ctx);
    final Pattern pat = expr.length == 3 ? Pattern.compile(IOFile.regex(
        string(checkStr(expr[2], ctx))), Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE) : null;

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
  private void list(final int root, final File dir, final StringList list, final boolean rec,
      final Pattern pat) throws QueryException {

    // skip invalid directories
    final File[] ch = dir.listFiles();
    if(ch == null) return;

    // parse directories, do not follow links
    if(rec) {
      for(final File f : ch) {
        if(f.isDirectory() && !mayBeLink(f)) list(root, f, list, true, pat);
      }
    }
    // parse files. ignore directories if a pattern is specified
    for(final File f : ch) {
      if(pat == null || pat.matcher(f.getName()).matches()) {
        final String file = f.getPath().substring(root);
        list.add(f.isDirectory() ? dir(file) : file);
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
      throw FILE_IE_PATH.get(info, f);
    }
  }

  /**
   * Creates a directory.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private synchronized Item createDir(final QueryContext ctx) throws QueryException {
    // resolve symbolic links
    final File path = checkFile(0, ctx);
    File f;
    try {
      f = path.getCanonicalFile();
    } catch(final IOException ex) {
      throw FILE_IE_PATH.get(info, path);
    }

    // find lowest existing path
    while(!f.exists()) {
      f = f.getParentFile();
      if(f == null) throw FILE_IE_PATH.get(info, path);
    }
    // warn if lowest path points to a file
    if(f.isFile()) throw FILE_E.get(info, path);

    // only create directories if path does not exist yet
    if(!path.exists() && !path.mkdirs()) throw FILE_IE_DIR.get(info, path);
    return null;
  }

  /**
   * Creates a temporary file or directory.
   * @param ctx query context
   * @param dir create a directory instead of a file
   * @return path of created file or directory
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item createTemp(final boolean dir, final QueryContext ctx)
      throws QueryException, IOException {

    final String pref = string(checkStr(expr[0], ctx));
    final String suf = expr.length > 1 ? string(checkStr(expr[1], ctx)) : "";
    final File root;
    if(expr.length > 2) {
      root = checkFile(2, ctx);
      if(root.isFile()) throw FILE_ND.get(info, root);
    } else {
      root = new File(Prop.TMP);
    }

    // choose non-existing file path
    final Random rnd = new Random();
    File file;
    do {
      file = new File(root, pref + rnd.nextLong() + suf);
    } while(file.exists());

    // create directory or file
    String path = file.getPath();
    if(dir) {
      if(!file.mkdirs()) throw FILE_IE_DIR.get(info, file);
      path = dir(path);
    } else {
      new IOFile(file).write(EMPTY);
    }
    return Str.get(path);
  }

  /**
   * Deletes a file or directory.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private synchronized Item delete(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    if(!path.exists()) throw FILE_NF.get(info, path.getAbsolutePath());
    if(optionalBool(1, ctx)) {
      deleteRec(path);
    } else if(!path.delete()) {
      throw (path.isDirectory() ? FILE_ID_FULL : FILE_IE_DEL).get(info, path);
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
    if(!path.delete()) throw FILE_IE_DEL.get(info, path);
  }

  /**
   * Reads the contents of a binary file.
   * @param ctx query context
   * @return Base64Binary
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private B64 readBinary(final QueryContext ctx) throws QueryException, IOException {
    final File path = checkFile(0, ctx);
    final long off = expr.length > 1 ? checkItr(expr[1], ctx) : 0;
    long len = expr.length > 2 ? checkItr(expr[2], ctx) : 0;

    if(!path.exists()) throw FILE_NF.get(info, path.getAbsolutePath());
    if(path.isDirectory()) throw FILE_ID.get(info, path.getAbsolutePath());

    // read full file
    if(expr.length == 1) return new B64Stream(new IOFile(path), FILE_IE);

    // read file chunk
    final DataAccess da = new DataAccess(new IOFile(path));
    try {
      final long dlen = da.length();
      if(expr.length == 2) len = dlen - off;
      if(off < 0 || off > dlen || len < 0 || off + len > dlen)
        throw FILE_OOR.get(info, off, off + len);
      da.cursor(off);
      return new B64(da.readBytes((int) len));
    } finally {
      da.close();
    }
  }

  /**
   * Reads the contents of a file.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private StrStream readText(final QueryContext ctx) throws QueryException {
    final File path = checkFile(0, ctx);
    final String enc = encoding(1, FILE_UE, ctx);
    if(!path.exists()) throw FILE_NF.get(info, path.getAbsolutePath());
    if(path.isDirectory()) throw FILE_ID.get(info, path.getAbsolutePath());
    return new StrStream(new IOFile(path), enc, FILE_IE, ctx);
  }

  /**
   * Returns the contents of a file line by line.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Iter readTextLines(final QueryContext ctx) throws QueryException {
    return FNGen.textIter(readText(ctx).string(info));
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item write(final boolean append, final QueryContext ctx)
      throws QueryException, IOException {

    final File path = check(checkFile(0, ctx));
    final Iter ir = expr[1].iter(ctx);
    final SerializerOptions sopts = FuncOptions.serializer(
        expr.length > 2 ? expr[2].item(ctx, info) : null, info);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      final Serializer ser = Serializer.get(out, sopts);
      for(Item it; (it = ir.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeText(final boolean append, final QueryContext ctx)
      throws QueryException, IOException {

    final File path = check(checkFile(0, ctx));
    final byte[] s = checkStr(expr[1], ctx);
    final String enc = encoding(2, FILE_UE, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      out.write(cs == null ? s : string(s).getBytes(cs));
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeTextLines(final boolean append, final QueryContext ctx)
      throws QueryException, IOException {

    final File path = check(checkFile(0, ctx));
    final Iter ir = expr[1].iter(ctx);
    final String enc = encoding(2, FILE_UE, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    final PrintOutput out = PrintOutput.get(new FileOutputStream(path, append));
    try {
      for(Item it; (it = ir.next()) != null;) {
        if(!it.type.isStringOrUntyped()) throw Err.typeError(this, AtomType.STR, it);
        final byte[] s = it.string(info);
        out.write(cs == null ? s : string(s).getBytes(cs));
        out.write(cs == null ? NL : Prop.NL.getBytes(cs));
      }
    } finally {
      out.close();
    }
    return null;
  }

  /**
   * Writes binary items to a file.
   * @param append append flag
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item writeBinary(final boolean append, final QueryContext ctx)
      throws QueryException, IOException {

    final File path = check(checkFile(0, ctx));
    final Bin bin = checkBinary(expr[1], ctx);
    final long off = expr.length > 2 ? checkItr(expr[2], ctx) : 0;

    // write full file
    if(expr.length == 2) {
      final BufferOutput out = new BufferOutput(new FileOutputStream(path, append));
      try {
        final InputStream is = bin.input(info);
        try {
          for(int i; (i = is.read()) != -1;)  out.write(i);
        } finally {
          is.close();
        }
      } finally {
        out.close();
      }
    } else {
      // write file chunk
      final RandomAccessFile raf = new RandomAccessFile(path, "rw");
      try {
        final long dlen = raf.length();
        if(off < 0 || off > dlen) throw FILE_OOR.get(info, off, dlen);
        raf.seek(off);
        raf.write(bin.binary(info));
      } finally {
        raf.close();
      }
    }
    return null;
  }

  /**
   * Checks the target directory of the specified file.
   * @param path file to be written
   * @return specified file
   * @throws QueryException query exception
   */
  private File check(final File path) throws QueryException {
    final IOFile io = new IOFile(path);
    if(io.isDir()) throw FILE_ID.get(info, io);
    final IOFile dir = io.dir();
    if(!dir.exists()) throw FILE_ND.get(info, dir);
    return path;
  }

  /**
   * Transfers a file path, given a source and a target.
   * @param copy copy flag (no move)
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item relocate(final boolean copy, final QueryContext ctx)
      throws QueryException, IOException {

    final File src = checkFile(0, ctx).getCanonicalFile();
    File trg = checkFile(1, ctx).getCanonicalFile();
    if(!src.exists()) throw FILE_NF.get(info, src.getAbsolutePath());

    if(trg.isDirectory()) {
      // target is a directory: attach file name
      trg = new File(trg, src.getName());
      if(trg.isDirectory()) throw FILE_ID.get(info, trg);
    } else if(!trg.isFile()) {
      // target does not exist: ensure that parent exists
      if(!trg.getParentFile().isDirectory()) throw FILE_ND.get(info, trg);
    } else if(src.isDirectory()) {
      // if target is file, source cannot be a directory
      throw FILE_ID.get(info, src);
    }

    // ignore operations on identical, canonical source and target path
    final String spath = src.getPath();
    final String tpath = trg.getPath();
    if(!spath.equals(tpath)) {
      if(copy) {
        copy(src, trg);
      } else {
        // delete target if it is different to source (case is ignored on Windows and Mac)
        if(trg.exists() && (Prop.CASE || !spath.equalsIgnoreCase(tpath)) && !trg.delete())
          throw FILE_IE_DEL.get(info, src, trg);
        if(!src.renameTo(trg)) throw FILE_IE_MOVE.get(info, src, trg);
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
      if(!trg.mkdir()) throw FILE_IE_DIR.get(info, trg);
      final File[] files = src.listFiles();
      if(files == null) throw FILE_IE_ACCESS.get(info, src);
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
  private boolean optionalBool(final int i, final QueryContext ctx) throws QueryException {
    return i < expr.length && checkBln(expr[i], ctx);
  }

  /**
   * Attaches a directory separator to the specified directory string.
   * @param dir input string
   * @return directory string
   */
  private static String dir(final String dir) {
    return dir.endsWith(File.separator) ? dir : dir + File.separator;
  }
}
