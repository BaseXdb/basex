package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.archive.*;
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
  /** Archive namespace. */
  private static final Atts NS = new Atts(ARCHIVE, ARCHIVEURI);
  /** Element: Entry. */
  private static final QNm Q_ENTRY = new QNm("archive:entry", ARCHIVEURI);
  /** Element: options. */
  private static final QNm Q_OPTIONS = new QNm("archive:options", ARCHIVEURI);
  /** Option: algorithm. */
  private static final QNm Q_FORMAT = new QNm("archive:format", ARCHIVEURI);
  /** Option: algorithm. */
  private static final QNm Q_ALGORITHM = new QNm("archive:algorithm", ARCHIVEURI);
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
  /** Value. */
  private static final QNm Q_VALUE = new QNm("value");

  /** Option: format. */
  private static final byte[] FORMAT = Token.token("format");
  /** Option: algorithm. */
  private static final byte[] ALGORITHM = Token.token("algorithm");
  /** Option: algorithm: deflate. */
  private static final byte[] DEFLATE = Token.token("deflate");
  /** Option: algorithm: stored. */
  private static final byte[] STORED = Token.token("stored");
  /** Option: algorithm: unknown. */
  private static final byte[] UNKNOWN = Token.token("unknown");

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
      case _ARCHIVE_CREATE:  return create(ctx);
      case _ARCHIVE_UPDATE:  return update(ctx);
      case _ARCHIVE_DELETE:  return delete(ctx);
      case _ARCHIVE_OPTIONS: return options(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Creates a new archive.
   * @param ctx query context
   * @return archive
   * @throws QueryException query exception
   */
  private B64 create(final QueryContext ctx) throws QueryException {
    final Iter entr = ctx.iter(expr[0]);
    final Iter cont = ctx.iter(expr[1]);
    final Item opt = expr.length > 2 ? expr[2].item(ctx, info) : null;
    final TokenMap map = new FuncParams(Q_OPTIONS, info).parse(opt);

    final byte[] f = map.get(FORMAT);
    final String format = f != null ? string(lc(f)) : "zip";
    final ArchiveOut out = ArchiveOut.get(format, info);
    // check algorithm
    final byte[] alg = map.get(ALGORITHM);
    if(alg != null) {
      if(format.equals("zip") && !eq(alg, STORED, DEFLATE) ||
         format.equals("gzip") && !eq(alg, DEFLATE)) {
        ARCH_SUPP.thrw(info, ALGORITHM, alg);
      }
      if(alg.equals(STORED)) out.level(ZipEntry.STORED);
      else if(alg.equals(DEFLATE)) out.level(ZipEntry.DEFLATED);
    }

    try {
      int e = 0;
      int c = 0;
      Item en, cn;
      while(true) {
        en = entr.next();
        cn = cont.next();
        if(en == null || cn == null) break;
        if(out instanceof GZIPOut && c > 0)
          ARCH_ONE.thrw(info, format.toUpperCase(Locale.ENGLISH));
        add(checkElmStr(en), cn, out);
        e++;
        c++;
      }
      // count remaining entries
      if(cn != null) do c++; while(cont.next() != null);
      if(en != null) do e++; while(entr.next() != null);
      if(e != c) throw ARCH_DIFF.thrw(info, e, c);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw ARCH_FAIL.thrw(info, ex);
    } finally {
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Returns the options of an archive.
   * @param ctx query context
   * @return entries
   * @throws QueryException query exception
   */
  private FElem options(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    String format = null;
    int level = -1;

    final ArchiveIn arch = ArchiveIn.get(archive.input(info), info);
    try {
      format = arch.format();
      while(arch.more()) {
        final ZipEntry ze = arch.entry();
        if(ze.isDirectory()) continue;
        level = ze.getMethod();
        break;
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    } finally {
      arch.close();
    }

    // create result element
    final FElem e = new FElem(Q_OPTIONS, NS);
    if(format != null) e.add(new FElem(Q_FORMAT).add(Q_VALUE, token(format)));
    if(level >= 0) {
      final byte[] lvl = level == 8 ? DEFLATE : level == 0 ? STORED : UNKNOWN;
      e.add(new FElem(Q_ALGORITHM).add(Q_VALUE, lvl));
    }
    return e;
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
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        final FElem e = new FElem(Q_ENTRY, NS);
        long s = ze.getSize();
        if(s != -1) e.add(Q_SIZE, token(s));
        s = ze.getTime();
        if(s != -1) e.add(Q_LAST_MOD, new Dtm(s, info).string(info));
        s = ze.getCompressedSize();
        if(s != -1) e.add(Q_COMP_SIZE, token(s));
        e.add(token(ze.getName()));
        vb.add(e);
      }
      return vb;
    } catch(final IOException ex) {
      Util.debug(ex);
      throw ARCH_FAIL.thrw(info, ex);
    } finally {
      in.close();
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
    final TokenObjMap<Item[]> hm = new TokenObjMap<Item[]>();

    final Iter entr = ctx.iter(expr[1]);
    final Iter cont = ctx.iter(expr[2]);
    int e = 0;
    int c = 0;
    Item en, cn;
    while(true) {
      en = entr.next();
      cn = cont.next();
      if(en == null || cn == null) break;
      hm.add(checkElmStr(en).string(info), new Item[] { en, cn });
      e++;
      c++;
    }
    // count remaining entries
    if(cn != null) do c++; while(cont.next() != null);
    if(en != null) do e++; while(entr.next() != null);
    if(e != c) ARCH_DIFF.thrw(info, e, c);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        ARCH_MODIFY.thrw(info, in.format().toUpperCase(Locale.ENGLISH));
      // delete entries to be updated
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
      // add new and updated entries
      for(final byte[] h : hm) {
        if(h == null) continue;
        final Item[] it = hm.get(h);
        add(it[0], it[1], out);
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
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
    final TokenObjMap<Item[]> hm = new TokenObjMap<Item[]>();
    final Iter names = ctx.iter(expr[1]);
    for(Item en; (en = names.next()) != null;) {
      hm.add(checkElmStr(en).string(info), null);
    }

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        ARCH_MODIFY.thrw(info, in.format().toUpperCase(Locale.ENGLISH));
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Extracts entries from the archive.
   * @param ctx query context
   * @return text entries
   * @throws QueryException query exception
   */
  private TokenList extract(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    TokenSet hs = null;
    if(expr.length > 1) {
      // filter result to specified entries
      hs = new TokenSet();
      final Iter names = ctx.iter(expr[1]);
      for(Item en; (en = names.next()) != null;) {
        hs.add(checkElmStr(en).string(info));
      }
    }

    final TokenList tl = new TokenList();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        if(hs == null || hs.delete(token(ze.getName())) != 0) tl.add(in.read());
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      ARCH_FAIL.thrw(info, ex);
    } finally {
      in.close();
    }
    return tl;
  }

  /**
   * Adds the specified entry to the output stream.
   * @param entry entry descriptor
   * @param con contents
   * @param out output archive
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void add(final Item entry, final Item con, final ArchiveOut out)
      throws QueryException, IOException {

    // create new zip entry
    final String name = string(entry.string(info));
    if(name.isEmpty()) ARCH_EMPTY.thrw(info);
    final ZipEntry ze = new ZipEntry(name);
    String en = null;

    // compression level
    if(entry instanceof ANode) {
      final ANode el = (ANode) entry;
      final byte[] level = el.attribute(Q_LEVEL);
      if(level != null) {
        final int l = toInt(level);
        try {
          ze.setMethod(l);
        } catch(final IllegalArgumentException ex) {
          ARCH_LEVEL.thrw(info, level);
        }
      }

      // last modified
      final byte[] mod = el.attribute(Q_LAST_MOD);
      if(mod != null) {
        try {
          ze.setTime(new Int(new Dtm(mod, info)).itr());
        } catch(final QueryException qe) {
          ARCH_DATETIME.thrw(info, mod);
        }
      }

      // encoding
      final byte[] enc = el.attribute(Q_ENCODING);
      if(enc != null) {
        en = string(enc);
        if(!Charset.isSupported(en)) ARCH_ENCODING.thrw(info, enc);
      }
    }

    // data to be compressed
    byte[] val = null;
    if(con.type.isString()) {
      val = con.string(info);
      if(en != null && en != Token.UTF8) val = encode(val, en);
    } else if(con.type == AtomType.B64) {
      val = ((Bin) con).binary(info);
    } else {
      STRB64TYPE.thrw(info, con.type);
    }
    out.write(ze, val);
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

  /**
   * Checks if the specified item is a string or element.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  private Item checkElmStr(final Item it) throws QueryException {
    if(it.type.isString() || TEST.eq(it)) return it;
    throw ELMSTRTYPE.thrw(info, Q_ENTRY.string(), it.type);
  }
}
