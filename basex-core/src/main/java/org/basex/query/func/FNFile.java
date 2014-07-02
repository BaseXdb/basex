package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
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
import org.basex.query.value.seq.*;
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
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNFile(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    try {
      switch(func) {
        case _FILE_CHILDREN:        return children(ctx);
        case _FILE_LIST:            return list(ctx);
        case _FILE_READ_TEXT_LINES: return readTextLines(ctx);
        default:                    return super.iter(ctx);
      }
    } catch(final NoSuchFileException ex) {
      throw FILE_NOT_FOUND.get(info, ex);
    } catch(final NotDirectoryException ex) {
      throw FILE_NO_DIR.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR.get(info, ex);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    try {
      switch(func) {
        case _FILE_APPEND:            return write(true, ctx);
        case _FILE_APPEND_BINARY:     return writeBinary(true, ctx);
        case _FILE_APPEND_TEXT:       return writeText(true, ctx);
        case _FILE_APPEND_TEXT_LINES: return writeTextLines(true, ctx);
        case _FILE_BASE_DIR:          return baseDir();
        case _FILE_COPY:              return relocate(true, ctx);
        case _FILE_CREATE_DIR:        return createDir(ctx);
        case _FILE_CREATE_TEMP_DIR:   return createTemp(true, ctx);
        case _FILE_CREATE_TEMP_FILE:  return createTemp(false, ctx);
        case _FILE_CURRENT_DIR:       return currentDir();
        case _FILE_DELETE:            return delete(ctx);
        case _FILE_DIR_SEPARATOR:     return Str.get(File.separator);
        case _FILE_EXISTS:            return Bln.get(Files.exists(checkPath(0, ctx)));
        case _FILE_IS_DIR:            return Bln.get(Files.isDirectory(checkPath(0, ctx)));
        case _FILE_IS_FILE:           return Bln.get(Files.isRegularFile(checkPath(0, ctx)));
        case _FILE_LAST_MODIFIED:     return lastModified(ctx);
        case _FILE_LINE_SEPARATOR:    return Str.get(NL);
        case _FILE_MOVE:              return relocate(false, ctx);
        case _FILE_NAME:              return name(ctx);
        case _FILE_PARENT:            return parent(ctx);
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
    } catch(final NoSuchFileException ex) {
      throw FILE_NOT_FOUND.get(info, ex);
    } catch(final AccessDeniedException ex) {
      throw FILE_IE_ERROR_ACCESS.get(info, ex);
    } catch(final FileAlreadyExistsException ex) {
      throw FILE_EXISTS.get(info, ex);
    } catch(final DirectoryNotEmptyException ex) {
      throw FILE_ID_DIR2.get(info, ex);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR.get(info, ex);
    }
  }

  /**
   * Returns the last modified date of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Item lastModified(final QueryContext ctx) throws QueryException, IOException {
    final Path path = checkPath(0, ctx);
    final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    return new Dtm(attrs.lastModifiedTime().toMillis(), info);
  }

  /**
   * Returns the size of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Item size(final QueryContext ctx) throws QueryException, IOException {
    final Path path = checkPath(0, ctx);
    final BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    return Int.get(attrs.isDirectory() ? 0 : attrs.size());
  }

  /**
   * Returns the base name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str name(final QueryContext ctx) throws QueryException {
    final Path path = checkPath(0, ctx).getFileName();
    return path == null ? Str.ZERO : Str.get(path.toString());
  }

  /**
   * Returns the current working directory.
   * @return result
   */
  private Str currentDir() {
    return get(absolute(Paths.get(".")), true);
  }

  /**
   * Returns the current base directory.
   * @return result
   */
  private Str baseDir() {
    final IO base = sc.baseIO();
    return base instanceof IOFile ? get(absolute(Paths.get(base.dir())), true) : null;
  }

  /**
   * Returns the directory name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str parent(final QueryContext ctx) throws QueryException {
    final Path parent = absolute(checkPath(0, ctx)).getParent();
    return parent == null ? null : get(parent, true);
  }

  /**
   * Returns the native name of the specified path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Str pathToNative(final QueryContext ctx) throws QueryException, IOException {
    final Path nat = checkPath(0, ctx).toRealPath();
    return get(nat, Files.isDirectory(nat));
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Uri pathToUri(final QueryContext ctx) throws QueryException {
    return Uri.uri(checkPath(0, ctx).toUri().toString());
  }

  /**
   * Returns an absolute file path.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str resolvePath(final QueryContext ctx) throws QueryException {
    final Path abs = absolute(checkPath(0, ctx));
    return get(abs, Files.isDirectory(abs));
  }

  /**
   * Returns paths to all children of a directory.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Iter children(final QueryContext ctx) throws QueryException, IOException {
    final TokenList children = new TokenList();
    try(DirectoryStream<Path> paths = Files.newDirectoryStream(checkPath(0, ctx))) {
      for(final Path child : paths) children.add(get(child, Files.isDirectory(child)).string());
    }
    return StrSeq.get(children).iter();
  }

  /**
   * Lists all files of a directory.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private Iter list(final QueryContext ctx) throws QueryException, IOException {
    final Path dir = checkPath(0, ctx).toRealPath();
    final boolean rec = optionalBool(1, ctx);
    final Pattern pat = exprs.length == 3 ? Pattern.compile(IOFile.regex(
        string(checkStr(exprs[2], ctx))), Prop.CASE ? 0 : Pattern.CASE_INSENSITIVE) : null;

    final TokenList list = new TokenList();
    list(dir.getNameCount(), dir, list, rec, pat);
    return StrSeq.get(list).iter();
  }

  /**
   * Collects the sub-directories and files of the specified directory.
   * @param index index of root path
   * @param dir root path
   * @param list file list
   * @param rec recursive flag
   * @param pat file name pattern; ignored if {@code null}
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void list(final int index, final Path dir, final TokenList list, final boolean rec,
      final Pattern pat) throws QueryException, IOException {

    // skip invalid directories
    final ArrayList<Path> children = new ArrayList<>();
    try(DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
      for(final Path child : paths) children.add(child);
    } catch(final IOException ex) {
      // only throw exception on root level
      if(index == dir.getNameCount()) throw ex;
    }

    // parse directories, do not follow links
    if(rec) {
      for(final Path child : children) {
        if(Files.isDirectory(child)) list(index, child, list, rec, pat);
      }
    }

    // parse files. ignore directories if a pattern is specified
    for(final Path child : children) {
      if(pat == null || pat.matcher(child.getFileName().toString()).matches()) {
        final Path path = child.subpath(index, child.getNameCount());
        list.add(get(path, Files.isDirectory(child)).string());
      }
    }
  }

  /**
   * Creates a directory.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item createDir(final QueryContext ctx) throws QueryException, IOException {
    final Path path = absolute(checkPath(0, ctx));

    // find lowest existing path
    for(Path p = path; p != null;) {
      if(Files.exists(p)) {
        if(Files.isRegularFile(p)) throw FILE_EXISTS.get(info, p);
        break;
      }
      p = p.getParent();
    }

    Files.createDirectories(path);
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

    final String pref = string(checkStr(exprs[0], ctx));
    final String suf = exprs.length > 1 ? string(checkStr(exprs[1], ctx)) : "";
    final Path root;
    if(exprs.length > 2) {
      root = checkPath(2, ctx);
      if(Files.isRegularFile(root)) throw FILE_NO_DIR.get(info, root);
    } else {
      root = Paths.get(Prop.TMP);
    }

    // choose non-existing file path
    final Random rnd = new Random();
    Path file;
    do {
      file = root.resolve(pref + rnd.nextLong() + suf);
    } while(Files.exists(file));

    // create directory or file
    if(dir) {
      Files.createDirectory(file);
    } else {
      Files.createFile(file);
    }
    return get(file, dir);
  }

  /**
   * Deletes a file or directory.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized Item delete(final QueryContext ctx) throws QueryException, IOException {
    final Path path = checkPath(0, ctx);
    if(optionalBool(1, ctx)) {
      delete(path);
    } else {
      Files.delete(path);
    }
    return null;
  }

  /**
   * Recursively deletes a file path.
   * @param path path to be deleted
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized void delete(final Path path) throws QueryException, IOException {
    if(Files.isDirectory(path)) {
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
        for(final Path p : paths) delete(p);
      }
    }
    Files.delete(path);
  }

  /**
   * Reads the contents of a binary file.
   * @param ctx query context
   * @return Base64Binary
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private B64 readBinary(final QueryContext ctx) throws QueryException, IOException {
    final Path path = checkPath(0, ctx);
    final long off = exprs.length > 1 ? checkItr(exprs[1], ctx) : 0;
    long len = exprs.length > 2 ? checkItr(exprs[2], ctx) : 0;

    if(!Files.exists(path)) throw FILE_NOT_FOUND.get(info, path);
    if(Files.isDirectory(path)) throw FILE_IS_DIR.get(info, path);

    // read full file
    if(exprs.length == 1) return new B64Stream(new IOFile(path.toFile()), FILE_IO_ERROR);

    // read file chunk
    try(final DataAccess da = new DataAccess(new IOFile(path.toFile()))) {
      final long dlen = da.length();
      if(exprs.length == 2) len = dlen - off;
      if(off < 0 || off > dlen || len < 0 || off + len > dlen)
        throw FILE_OUT_OF_RANGE.get(info, off, off + len);
      da.cursor(off);
      return new B64(da.readBytes((int) len));
    }
  }

  /**
   * Reads the contents of a file.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private StrStream readText(final QueryContext ctx) throws QueryException {
    final Path path = checkPath(0, ctx);
    final String enc = encoding(1, FILE_UNKNOWN_ENCODING, ctx);
    if(!Files.exists(path)) throw FILE_NOT_FOUND.get(info, path);
    if(Files.isDirectory(path)) throw FILE_IS_DIR.get(info, path);
    return new StrStream(new IOFile(path.toFile()), enc, FILE_IO_ERROR, ctx);
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

    final Path path = checkParentDir(checkPath(0, ctx));
    final Iter ir = exprs[1].iter(ctx);
    final SerializerOptions sopts = FuncOptions.serializer(
        exprs.length > 2 ? exprs[2].item(ctx, info) : null, info);

    //Files.newOutputStream(path, append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

    try(final PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      final Serializer ser = Serializer.get(out, sopts);
      for(Item it; (it = ir.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
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

    final Path path = checkParentDir(checkPath(0, ctx));
    final byte[] s = checkStr(exprs[1], ctx);
    final String enc = encoding(2, FILE_UNKNOWN_ENCODING, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    try(final PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      out.write(cs == null ? s : string(s).getBytes(cs));
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

    final Path path = checkParentDir(checkPath(0, ctx));
    final Iter ir = exprs[1].iter(ctx);
    final String enc = encoding(2, FILE_UNKNOWN_ENCODING, ctx);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    try(final PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      for(Item it; (it = ir.next()) != null;) {
        if(!it.type.isStringOrUntyped()) throw Err.typeError(this, AtomType.STR, it);
        final byte[] s = it.string(info);
        out.write(cs == null ? s : string(s).getBytes(cs));
        out.write(cs == null ? NL : Prop.NL.getBytes(cs));
      }
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

    final Path path = checkParentDir(checkPath(0, ctx));
    final Bin bin = checkBinary(exprs[1], ctx);
    final long off = exprs.length > 2 ? checkItr(exprs[2], ctx) : 0;

    // write full file
    if(exprs.length == 2) {
      try(final BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile(), append));
          final InputStream is = bin.input(info)) {
        for(int i; (i = is.read()) != -1;)  out.write(i);
      }
    } else {
      // write file chunk
      try(final RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long dlen = raf.length();
        if(off < 0 || off > dlen) throw FILE_OUT_OF_RANGE.get(info, off, dlen);
        raf.seek(off);
        raf.write(bin.binary(info));
      }
    }
    return null;
  }

  /**
   * Checks that the parent of the specified path is a directory, but is no directory itself.
   * @param path file to be written
   * @return specified file
   * @throws QueryException query exception
   */
  private Path checkParentDir(final Path path) throws QueryException {
    if(Files.isDirectory(path)) throw FILE_IS_DIR.get(info, path);
    final Path parent = path.getParent();
    if(parent != null && !Files.exists(parent)) throw FILE_NO_DIR.get(info, parent);
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

    final Path source = checkPath(0, ctx);
    if(!Files.exists(source)) throw FILE_NOT_FOUND.get(info, source);
    final Path src = absolute(source);
    Path trg = absolute(checkPath(1, ctx));

    if(Files.isDirectory(trg)) {
      // target is a directory: attach file name
      trg = trg.resolve(src.getFileName());
      if(Files.isDirectory(trg)) throw FILE_IS_DIR.get(info, trg);
    } else if(!Files.exists(trg)) {
      // target does not exist: ensure that parent exists
      if(!Files.isDirectory(trg.getParent())) throw FILE_NO_DIR.get(info, trg);
    } else if(Files.isDirectory(src)) {
      // if target is file, source cannot be a directory
      throw FILE_IS_DIR.get(info, src);
    }

    // ignore operations on identical, canonical source and target path
    if(copy) {
      copy(src, trg);
    } else {
      Files.move(src, trg, StandardCopyOption.REPLACE_EXISTING);
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
  private synchronized void copy(final Path src, final Path trg)
      throws QueryException, IOException {

    if(Files.isDirectory(src)) {
      Files.createDirectory(trg);
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(src)) {
        for(final Path p : paths) copy(p, trg.resolve(p.getFileName()));
      }
    } else {
      Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
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
    return i < exprs.length && checkBln(exprs[i], ctx);
  }

  /**
   * Returns a unified string representation of the path.
   * @param path directory path
   * @param dir directory flag
   * @return path string
   */
  private static Str get(final Path path, final boolean dir) {
    final String string = path.toString();
    return Str.get(dir && !string.endsWith(File.separator) ? string + File.separator : string);
  }

  /**
   * Returns the absolute, normalized path.
   * @param path input path
   * @return normalized path
   */
  private static Path absolute(final Path path) {
    return path.toAbsolutePath().normalize();
  }
}
