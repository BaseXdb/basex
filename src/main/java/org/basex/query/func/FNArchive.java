package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class FNArchive extends StandardFunc {
  /** Element: Entry. */
  private static final QNm Q_ENTRY = new QNm("archive:entry", ARCHIVEURI);
  /** Root node test. */
  private static final ExtTest TEST = new ExtTest(NodeType.ELM, Q_ENTRY);

  /** Level. */
  private static final QNm Q_LEVEL = new QNm("compression-level");
  /** Encoding. */
  private static final QNm Q_ENCODING = new QNm("encoding");
  /** Last modified. */
  private static final QNm Q_LAST_MOD = new QNm("last-modified");
  /** Compressed size. */
  private static final QNm Q_COMP_SIZE = new QNm("compressed-size");
  /** Uncompressed size. */
  private static final QNm Q_SIZE = new QNm("size");

  /** Element: options. */
  private static final QNm E_OPTIONS = new QNm("archive:options", ARCHIVEURI);
  /** Option: format. */
  private static final byte[] FORMAT = Token.token("format");
  /** Option: format: zip. */
  private static final byte[] ZIP = Token.token("zip");
  /** Option: algorithm. */
  private static final byte[] ALGORITHM = Token.token("algorithm");
  /** Option: algorithm: deflate. */
  private static final byte[] DEFLATE = Token.token("deflate");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNArchive(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _ARCHIVE_ENTRIES:        return entries(ctx);
      case _ARCHIVE_EXTRACT_TEXT:   return extractText(ctx);
      case _ARCHIVE_EXTRACT_BINARY: return extractBinary(ctx);
      default:                      return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _ARCHIVE_CREATE: return create(ctx);
      case _ARCHIVE_UPDATE: return update(ctx);
      case _ARCHIVE_DELETE: return delete(ctx);
      default:              return super.item(ctx, ii);
    }
  }

  /**
   * Creates a new archive.
   * @param ctx query context
   * @return archive
   * @throws QueryException query exception
   */
  private B64 create(final QueryContext ctx) throws QueryException {
    final Iter elem = ctx.iter(expr[0]);
    final Iter cont = ctx.iter(expr[1]);
    final Item opt = expr.length > 2 ? expr[2].item(ctx, info) : null;
    final TokenMap map = new FuncParams(E_OPTIONS, info).parse(opt);

    // check format
    final byte[] format = map.get(FORMAT);
    if(format != null && !eq(format, ZIP)) ARCH_SUPP.thrw(info, FORMAT, format);
    // check algorithm
    final byte[] alg = map.get(ALGORITHM);
    if(alg != null && !eq(alg, DEFLATE)) ARCH_SUPP.thrw(info, ALGORITHM, alg);

    final ArrayOutput ao = new ArrayOutput();
    final ZipOutputStream zos = new ZipOutputStream(ao);
    try {
      int e = 0;
      int c = 0;
      Item elm, con;
      while(true) {
        elm = elem.next();
        con = cont.next();
        if(elm == null || con == null) break;

        // check entry
        if(!TEST.eq(elm)) Err.type(this, NodeType.ELM, elm);
        add((ANode) elm, con, zos);
        e++;
        c++;
      }
      // count remaining entries
      if(con != null) do c++; while(cont.next() != null);
      if(elm != null) do e++; while(elem.next() != null);
      if(e != c) throw ARCH_DIFF.thrw(info, e, c);

      zos.close();
      return new B64(ao.toArray());
    } catch(final IOException ex) {
      Util.debug(ex);
      throw ARCH_FAIL.thrw(info, ex);
    }
  }

  /**
   * Returns the entries of an archive.
   * @param ctx query context
   * @return entries
   * @throws QueryException query exception
   */
  private Iter entries(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);

    final ValueBuilder vb = new ValueBuilder();
    final ZipInputStream zis = new ZipInputStream(archive.input(info));
    try {
      try {
        for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
          if(ze.isDirectory()) continue;
          final FElem e = new FElem(Q_ENTRY);
          e.add(new FAttr(Q_SIZE, token(ze.getSize())));
          e.add(new FAttr(Q_LAST_MOD, new Dtm(ze.getTime(), info).string(info)));
          e.add(new FAttr(Q_COMP_SIZE, token(ze.getCompressedSize())));
          e.add(new FTxt(token(ze.getName())));
          vb.add(e);
        }
      } finally {
        zis.close();
      }
      return vb;
    } catch(final IOException ex) {
      Util.debug(ex);
      throw ARCH_FAIL.thrw(info, ex);
    }
  }

  /**
   * Extracts text entries.
   * @param ctx query context
   * @return text entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractText(final QueryContext ctx) throws QueryException {
    final String enc = encoding(2, ARCH_ENCODING, ctx);
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(ctx)) vb.add(Str.get(encode(b, enc)));
    return vb;
  }

  /**
   * Extracts binary entries.
   * @param ctx query context
   * @return binary entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractBinary(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(ctx)) vb.add(new B64(b));
    return vb;
  }

  /**
   * Updates an archive.
   * @param ctx query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 update(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    // entries to be updated
    final HashMap<String, Item[]> entries = new HashMap<String, Item[]>();

    final Iter elem = ctx.iter(expr[1]);
    final Iter cont = ctx.iter(expr[2]);
    int e = 0;
    int c = 0;
    Item elm, con;
    while(true) {
      elm = elem.next();
      con = cont.next();
      if(elm == null || con == null) break;
      if(!TEST.eq(elm)) Err.type(this, NodeType.ELM, elm);
      entries.put(string(elm.string(info)), new Item[] { elm, con });
      e++;
      c++;
    }
    // count remaining entries
    if(con != null) do c++; while(cont.next() != null);
    if(elm != null) do e++; while(elem.next() != null);
    if(e != c) throw ARCH_DIFF.thrw(info, e, c);

    final ZipInputStream zis = new ZipInputStream(archive.input(info));
    final ArrayOutput ao = new ArrayOutput();
    final ZipOutputStream zos = new ZipOutputStream(ao);
    try {
      try {
        // delete entries to be updated
        delete(entries, zis, zos);
        // add new and updated entries
        for(final Item[] it : entries.values()) add((ANode) it[0], it[1], zos);
      } finally {
        zos.close();
        zis.close();
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    }
    return new B64(ao.toArray());
  }

  /**
   * Deletes files from an archive.
   * @param ctx query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 delete(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    // entries to be deleted
    final HashMap<String, Item[]> entries = new HashMap<String, Item[]>();
    final Iter names = ctx.iter(expr[1]);
    for(Item it; (it = names.next()) != null;) {
      entries.put(string(checkStr(it, ctx)), null);
    }

    final ZipInputStream zis = new ZipInputStream(archive.input(info));
    final ArrayOutput ao = new ArrayOutput();
    final ZipOutputStream zos = new ZipOutputStream(ao);
    try {
      try {
        delete(entries, zis, zos);
      } finally {
        zos.close();
        zis.close();
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    }
    return new B64(ao.toArray());
  }

  /**
   * Extracts entries from the archive.
   * @param ctx query context
   * @return text entries
   * @throws QueryException query exception
   */
  private TokenList extract(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    HashSet<String> entries = null;
    if(expr.length > 1) {
      // filter result to specified entries
      entries = new HashSet<String>();
      final Iter names = ctx.iter(expr[1]);
      for(Item it; (it = names.next()) != null;) entries.add(string(checkStr(it, ctx)));
    }

    final TokenList tl = new TokenList();
    final byte[] data = new byte[IO.BLOCKSIZE];
    final ZipInputStream zis = new ZipInputStream(archive.input(info));
    try {
      try {
        for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
          if(ze.isDirectory()) continue;
          final String name = ze.getName();
          if(entries != null && !entries.remove(name)) continue;
          final ArrayOutput ao = new ArrayOutput();
          for(int c; (c = zis.read(data)) != -1;) ao.write(data, 0, c);
          tl.add(ao.toArray());
        }
      } finally {
        zis.close();
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    }
    return tl;
  }

  /**
   * Adds all files to the output stream that are not specified in the map.
   * @param entries entries to be deleted
   * @param zis input stream
   * @param zos output stream
   * @throws IOException I/O exception
   */
  private void delete(final HashMap<String, Item[]> entries, final ZipInputStream zis,
      final ZipOutputStream zos) throws IOException {

    final byte[] data = new byte[IO.BLOCKSIZE];
    for(ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
      final String name = ze.getName();
      if(entries.containsKey(name)) continue;
      final ZipEntry zen = new ZipEntry(name);
      zen.setTime(ze.getTime());
      zen.setComment(ze.getComment());
      zos.putNextEntry(zen);
      for(int c; (c = zis.read(data)) != -1;) zos.write(data, 0, c);
    }
  }

  /**
   * Adds the specified entry to the output stream.
   * @param el entry descriptor
   * @param con contents
   * @param zos output stream
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void add(final ANode el, final Item con, final ZipOutputStream zos)
      throws QueryException, IOException {

    // create new zip entry
    final String name = string(el.string());
    if(name.isEmpty()) ARCH_NAME.thrw(info);
    final ZipEntry ze = new ZipEntry(name);

    // compression level
    int lvl = Deflater.DEFAULT_COMPRESSION;
    final byte[] level = el.attribute(Q_LEVEL);
    if(level != null) {
      lvl = toInt(level);
      if(lvl < 0 || lvl > 9) ARCH_LEVEL.thrw(info, level);
    }
    zos.setLevel(lvl);

    // last modified
    final byte[] mod = el.attribute(Q_LAST_MOD);
    if(mod != null) {
      try {
        ze.setTime(new Int(new Dtm(mod, info)).itr());
      } catch(final QueryException qe) {
        ARCH_MODIFIED.thrw(info, mod);
      }
    }

    // data to be compressed
    byte[] val = null;
    if(con.type.isString()) {
      val = con.string(info);
      final byte[] enc = el.attribute(Q_ENCODING);
      if(enc != null) {
        final String en = string(enc);
        if(!Charset.isSupported(en)) ARCH_ENCODING.thrw(info, enc);
        if(en != Token.UTF8) val = encode(val, en);
      }
    } else if(con.type == AtomType.B64) {
      val = ((Bin) con).binary(info);
    } else {
      ARCH_STRB64.thrw(info, con.type);
    }
    zos.putNextEntry(ze);
    zos.write(val);
  }

  /**
   * Encodes the specified string to another encoding.
   * @param val value to be encoded
   * @param en encoding
   * @return encoded string
   * @throws QueryException query exception
   */
  private byte[] encode(final byte[] val, final String en) throws QueryException {
    try {
      return FNConvert.toString(new ArrayInput(val), en);
    } catch(final IOException ex) {
      throw ARCH_ENCODE.thrw(info, ex);
    }
  }
}
