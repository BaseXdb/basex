package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.basex.core.Prop;
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
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Functions on files and directories.
 *
 * @author BaseX Team 2005-11, ISC License
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
      case FLIST: return list(ctx);
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkAdmin(ctx);

    final File path = expr.length == 0 ? null : new File(
        string(checkStr(expr[0], ctx)));

    switch(def) {
      case FEXISTS:    return Bln.get(path.exists());
      case ISDIR:      return Bln.get(path.isDirectory());
      case ISFILE:     return Bln.get(path.isFile());
      case ISREAD:     return Bln.get(path.canRead());
      case ISWRITE:    return Bln.get(path.canWrite());
      case LASTMOD:    return lastModified(path);
      case SIZE:       return size(path);
      case PATHSEP:    return Str.get(Prop.SEP);
      case PATHTOFULL: return Str.get(path.getAbsolutePath());
      case PATHTOURI:  return pathToUri(path);
      case CREATEDIR:  return createDirectory(path);
      case DELETE:     return delete(path, ctx);
      case READ:       return read(path, ctx);
      case READBIN:    return readBinary(path);
      case WRITE:      return write(path, ctx);
      case WRITEBIN:   return writeBinary(path, ctx);
      case COPY:       return copy(path, ctx);
      case MOVE:       return move(path, ctx);
      default:         return super.item(ctx, ii);
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
    return Itr.get(path.length());
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

    // Check if directory exists
    if(!dir.exists()) PATHNOTEXISTS.thrw(input, path);
    // Check if not a directory
    if(!dir.isDirectory()) NOTDIR.thrw(input, path);

    final boolean rec = optionalBool(1, ctx);
    final String pat = expr.length != 3 ? null :
      IOFile.regex(string(checkStr(expr[2], ctx)));

    File[] fl;
    if(rec) {
      final List<File> list = new ArrayList<File>();
      recList(dir, list);
      fl = list.toArray(new File[list.size()]);
    } else {
      fl = dir.listFiles();
      if(fl == null) CANNOTLIST.thrw(input, path);
    }
    final File[] files = fl;

    return new Iter() {
      int c = -1;

      @Override
      public Item next() {
        while(++c < files.length) {
          final File f = files[c];
          final String n = Prop.WIN ? f.getName().toLowerCase() : f.getName();
          if(pat == null || n.matches(pat)) return Str.get(f.getPath());
        }
        return null;
      }
    };
  }

  /**
   * Lists the files in a directory recursively.
   * @param dir current directory
   * @param list file list
   */
  private void recList(final File dir, final List<File> list) {
    final File[] files = dir.listFiles();
    if(files == null) return;
    for(final File f : files) {
      list.add(f);
      if(f.isDirectory()) recList(f, list);
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
      f = path.getCanonicalFile();;
    } catch(final IOException ex) {
      PATHINVALID.thrw(input, path);
    }

    // find lowest existing path
    while(!f.exists()) {
      f = f.getParentFile();
      if(f == null) PATHINVALID.thrw(input, path);
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
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item delete(final File path, final QueryContext ctx)
      throws QueryException {

    final boolean rec = optionalBool(1, ctx);
    if(path.exists()) {
      if(rec) recDelete(path);
      else delete(path);
    }
    return null;
  }

  /**
   * Deletes a single file or directory.
   * @param path file/directory to be deleted
   * @throws QueryException query exception
   */
  private void delete(final File path) throws QueryException {
    if(path.isDirectory()) {
      final File[] ch = path.listFiles();
      if(ch != null && ch.length != 0) DIRNOTEMPTY.thrw(input, path);
    }
    if(!path.delete()) CANNOTDEL.thrw(input, path);
  }

  /**
   * Deletes a directory recursively.
   * @param path directory to be deleted
   * @throws QueryException query exception
   */
  private void recDelete(final File path) throws QueryException {
    final File[] ch = path.listFiles();
    if(ch != null) for(final File f : ch) recDelete(f);
    if(!path.delete()) CANNOTDEL.thrw(input, path);
  }

  /**
   * Reads the contents of a file.
   * @param path input path
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str read(final File path, final QueryContext ctx)
      throws QueryException {

    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    if(!path.exists()) PATHNOTEXISTS.thrw(input, path);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    if(enc != null && !Charset.isSupported(enc)) ENCNOTEXISTS.thrw(input, enc);

    try {
      return Str.get(TextInput.content(IO.get(path.getPath()), enc).finish());
    } catch(final IOException ex) {
      FILEERROR.thrw(input, ex);
      return null;
    }
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
      FILEERROR.thrw(input, ex);
      return null;
    }
  }

  /**
   * Writes a sequence of items to a file.
   * @param path file to be written
   * @param ctx query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   */
  private Item write(final File path, final QueryContext ctx)
      throws QueryException {

    final boolean append = optionalBool(3, ctx);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    final Iter ir = expr[1].iter(ctx);
    try {
      final PrintOutput out = new PrintOutput(
          new BufferedOutputStream(new FileOutputStream(path, append)));
      try {
        final XMLSerializer xml = new XMLSerializer(out,
            FNGen.serialPar(this, 2, ctx));
        Item it;
        while((it = ir.next()) != null) it.serialize(xml);
        xml.close();
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
   * @return result
   * @throws QueryException query exception
   */
  private Item writeBinary(final File path, final QueryContext ctx)
      throws QueryException {

    final B64 b64 = (B64) checkType(expr[1].item(ctx, input), Type.B6B);
    final boolean append = optionalBool(2, ctx);
    if(path.isDirectory()) PATHISDIR.thrw(input, path);

    try {
      final FileOutputStream out = new FileOutputStream(path, append);
      try {
        out.write(b64.toJava());
      } finally {
        out.close();
      }
    } catch(final IOException ex) {
      FILEERROR.thrw(input, ex);
    }
    return null;
  }

  /**
   * Copies a file given a source and a target.
   * @param src source file to be copied
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item copy(final File src, final QueryContext ctx)
      throws QueryException {

    File trg = new File(string(checkStr(expr[1], ctx)));
    // attach file name if target is a directory
    if(trg.isDirectory()) {
      trg = new File(trg, src.getName());
    } else if(!trg.isFile()) {
      final File par = trg.getParentFile();
      if(!par.exists() || !par.isDirectory()) PATHINVALID.thrw(input, trg);
    }

    if(!src.exists()) PATHNOTEXISTS.thrw(input, src);
    if(src.isDirectory()) PATHISDIR.thrw(input, src);

    if(!src.equals(trg)) {
      FileChannel sc = null;
      FileChannel dc = null;
      try {
        sc = new FileInputStream(src).getChannel();
        dc = new FileOutputStream(trg).getChannel();
        dc.transferFrom(sc, 0, sc.size());
      } catch(final IOException ex) {
        FILEERROR.thrw(input, ex);
      } finally {
        if(sc != null) try { sc.close(); } catch(final IOException e) { }
        if(dc != null) try { dc.close(); } catch(final IOException e) { }
      }
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

    File trg = new File(string(checkStr(expr[1], ctx)));
    // attach file name if target is a directory
    if(trg.isDirectory()) {
      trg = new File(trg, src.getName());
    } else if(!trg.isFile()) {
      final File par = trg.getParentFile();
      if(!par.exists() || !par.isDirectory()) PATHINVALID.thrw(input, trg);
    }

    if(!src.exists()) PATHNOTEXISTS.thrw(input, src);
    if(!src.renameTo(trg)) CANNOTMOVE.thrw(input, src, trg);
    return null;
  }

  /**
   * Transforms a file system path into a URI with the file:// scheme.
   * @param path file path
   * @return result
   */
  private Uri pathToUri(final File path) {
    return Uri.uri(Token.token(path.toURI().toString()));
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
      checkType(expr[i].item(ctx, input), Type.BLN).bool(input);
  }

  @Override
  public boolean uses(final Use u) {
    // prevent instant execution
    return u == Use.CTX || super.uses(u);
  }
}
