package org.basex.query.func.archive;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.archive.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNArchive extends StandardFunc {
  /** Packer format: gzip. */
  public static final String GZIP = "gzip";
  /** Packer format: zip. */
  public static final String ZIP = "zip";

  /** Module prefix. */
  private static final String PREFIX = "archive";
  /** QName. */
  private static final QNm Q_ENTRY = QNm.get(PREFIX, "entry", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(PREFIX, "options", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_FORMAT = QNm.get(PREFIX, "format", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_ALGORITHM = QNm.get(PREFIX, "algorithm", ARCHIVEURI);
  /** Root node test. */
  private static final NodeTest TEST = new NodeTest(Q_ENTRY);

  /** Level. */
  private static final String LEVEL = "compression-level";
  /** Encoding. */
  private static final String ENCODING = "encoding";
  /** Last modified. */
  private static final String LAST_MOD = "last-modified";
  /** Compressed size. */
  private static final String COMP_SIZE = "compressed-size";
  /** Uncompressed size. */
  private static final String SIZE = "size";
  /** Value. */
  private static final String VALUE = "value";

  /** Option: algorithm: deflate. */
  private static final String DEFLATE = "deflate";
  /** Option: algorithm: stored. */
  private static final String STORED = "stored";
  /** Option: algorithm: unknown. */
  private static final String UNKNOWN = "unknown";

  /** Archive options. */
  public static class ArchiveOptions extends Options {
    /** Archiving format. */
    public static final StringOption FORMAT = new StringOption("format", ZIP);
    /** Archiving algorithm. */
    public static final StringOption ALGORITHM = new StringOption("algorithm", DEFLATE);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _ARCHIVE_ENTRIES:        return entries(qc);
      case _ARCHIVE_EXTRACT_TEXT:   return extractText(qc);
      case _ARCHIVE_EXTRACT_BINARY: return extractBinary(qc);
      default:                      return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _ARCHIVE_CREATE:  return create(qc);
      case _ARCHIVE_UPDATE:  return update(qc);
      case _ARCHIVE_DELETE:  return delete(qc);
      case _ARCHIVE_OPTIONS: return options(qc);
      case _ARCHIVE_WRITE:   return write(qc);
      default:               return super.item(qc, ii);
    }
  }

  /**
   * Creates a new archive.
   * @param qc query context
   * @return archive
   * @throws QueryException query exception
   */
  private B64 create(final QueryContext qc) throws QueryException {
    final Iter entr = qc.iter(exprs[0]), cont = qc.iter(exprs[1]);
    final Options opts = toOptions(2, Q_OPTIONS, new ArchiveOptions(), qc);

    final String format = opts.get(ArchiveOptions.FORMAT);
    final ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info);
    // check algorithm
    final String alg = opts.get(ArchiveOptions.ALGORITHM);
    int level = ZipEntry.DEFLATED;
    if(alg != null) {
      if(format.equals(ZIP)  && !eq(alg, STORED, DEFLATE) ||
         format.equals(GZIP) && !eq(alg, DEFLATE)) {
        throw ARCH_SUPP_X_X.get(info, ArchiveOptions.ALGORITHM.name(), alg);
      }
      if(eq(alg, STORED)) level = ZipEntry.STORED;
      else if(eq(alg, DEFLATE)) level = ZipEntry.DEFLATED;
    }
    out.level(level);

    try {
      Item en, cn;
      int e = 0, c = 0;
      while(true) {
        en = entr.next();
        cn = cont.next();
        if(en == null || cn == null) break;
        if(out instanceof GZIPOut && c > 0)
          throw ARCH_ONE_X.get(info, format.toUpperCase(Locale.ENGLISH));
        add(checkElmStr(en), cn, out, level, qc);
        e++;
        c++;
      }
      // count remaining entries
      if(cn != null) do c++; while(cont.next() != null);
      if(en != null) do e++; while(entr.next() != null);
      if(e != c) throw ARCH_DIFF_X_X.get(info, e, c);
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Returns the options of an archive.
   * @param qc query context
   * @return entries
   * @throws QueryException query exception
   */
  private FElem options(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
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
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      arch.close();
    }

    // create result element
    final FElem e = new FElem(Q_OPTIONS).declareNS();
    if(format != null) e.add(new FElem(Q_FORMAT).add(VALUE, format));
    if(level >= 0) {
      final String lvl = level == 8 ? DEFLATE : level == 0 ? STORED : UNKNOWN;
      e.add(new FElem(Q_ALGORITHM).add(VALUE, lvl));
    }
    return e;
  }

  /**
   * Returns the entries of an archive.
   * @param qc query context
   * @return entries
   * @throws QueryException query exception
   */
  private Iter entries(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final ValueBuilder vb = new ValueBuilder();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        final FElem e = new FElem(Q_ENTRY).declareNS();
        long s = ze.getSize();
        if(s != -1) e.add(SIZE, token(s));
        s = ze.getTime();
        if(s != -1) e.add(LAST_MOD, new Dtm(s, info).string(info));
        s = ze.getCompressedSize();
        if(s != -1) e.add(COMP_SIZE, token(s));
        e.add(ze.getName());
        vb.add(e);
      }
      return vb;
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
  }

  /**
   * Extracts text entries.
   * @param qc query context
   * @return text entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractText(final QueryContext qc) throws QueryException {
    final String enc = toEncoding(2, ARCH_ENCODING_X, qc);
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(qc)) vb.add(Str.get(encode(b, enc, qc)));
    return vb;
  }

  /**
   * Extracts binary entries.
   * @param qc query context
   * @return binary entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractBinary(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(qc)) vb.add(new B64(b));
    return vb;
  }

  /**
   * Updates an archive.
   * @param qc query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 update(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    // entries to be updated
    final TokenObjMap<Item[]> hm = new TokenObjMap<>();

    final Iter entr = qc.iter(exprs[1]), cont = qc.iter(exprs[2]);
    int e = 0, c = 0;
    Item en, cn;
    while(true) {
      en = entr.next();
      cn = cont.next();
      if(en == null || cn == null) break;
      hm.put(checkElmStr(en).string(info), new Item[] { en, cn });
      e++;
      c++;
    }
    // count remaining entries
    if(cn != null) do c++; while(cont.next() != null);
    if(en != null) do e++; while(entr.next() != null);
    if(e != c) throw ARCH_DIFF_X_X.get(info, e, c);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        throw ARCH_MODIFY_X.get(info, in.format().toUpperCase(Locale.ENGLISH));
      // delete entries to be updated
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
      // add new and updated entries
      for(final byte[] h : hm) {
        if(h == null) continue;
        final Item[] it = hm.get(h);
        add(it[0], it[1], out, ZipEntry.DEFLATED, qc);
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Deletes files from an archive.
   * @param qc query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 delete(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    // entries to be deleted
    final TokenObjMap<Item[]> hm = new TokenObjMap<>();
    final Iter names = qc.iter(exprs[1]);
    for(Item en; (en = names.next()) != null;) hm.put(checkElmStr(en).string(info), null);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        throw ARCH_MODIFY_X.get(info, in.format().toUpperCase(Locale.ENGLISH));
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Extracts entries from the archive.
   * @param qc query context
   * @return text entries
   * @throws QueryException query exception
   */
  private TokenList extract(final QueryContext qc) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final TokenSet hs = entries(1, qc);

    final TokenList tl = new TokenList();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(!ze.isDirectory() && (hs == null || hs.delete(token(ze.getName())) != 0))
          tl.add(in.read());
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
    return tl;
  }

  /**
   * Writes entries from an archive to disk.
   * @param qc query context
   * @return text entries
   * @throws QueryException query exception
   */
  private Item write(final QueryContext qc) throws QueryException {
    final java.nio.file.Path path = toPath(0, qc);
    final B64 archive = toB64(exprs[1], qc, false);
    final TokenSet hs = entries(2, qc);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        final String name = ze.getName();
        if(hs == null || hs.delete(token(name)) != 0) {
          final java.nio.file.Path file = path.resolve(name);
          if(ze.isDirectory()) {
            Files.createDirectories(file);
          } else {
            Files.createDirectories(file.getParent());
            Files.write(file, in.read());
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    } finally {
      in.close();
    }
    return null;
  }

  /**
   * Returns all archive entries from the specified argument.
   * A {@code null} reference is returned if no entries are specified.
   * @param e argument index
   * @param qc query context
   * @return set with all entries
   * @throws QueryException query exception
   */
  private TokenSet entries(final int e, final QueryContext qc) throws QueryException {
    TokenSet hs = null;
    if(e < exprs.length) {
      // filter result to specified entries
      hs = new TokenSet();
      final Iter names = qc.iter(exprs[e]);
      for(Item en; (en = names.next()) != null;) hs.add(checkElmStr(en).string(info));
    }
    return hs;
  }

  /**
   * Adds the specified entry to the output stream.
   * @param entry entry descriptor
   * @param cont contents
   * @param out output archive
   * @param level default compression level
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void add(final Item entry, final Item cont, final ArchiveOut out, final int level,
      final QueryContext qc) throws QueryException, IOException {

    // create new zip entry
    String name = string(entry.string(info));
    if(name.isEmpty()) throw ARCH_EMPTY.get(info);
    if(Prop.WIN) name = name.replace('\\', '/');

    final ZipEntry ze = new ZipEntry(name);
    String enc = null;

    // compression level
    byte[] lvl = null;
    if(entry instanceof ANode) {
      final ANode el = (ANode) entry;
      lvl = el.attribute(LEVEL);

      // last modified
      final byte[] mod = el.attribute(LAST_MOD);
      if(mod != null) {
        try {
          ze.setTime(dateTimeToMs(new Dtm(mod, info), qc));
        } catch(final QueryException qe) {
          throw ARCH_DATETIME_X.get(info, mod);
        }
      }

      // encoding
      final byte[] ea = el.attribute(ENCODING);
      if(ea != null) {
        enc = string(ea);
        if(!Charset.isSupported(enc)) throw ARCH_ENCODING_X.get(info, ea);
      }
    }

    // data to be compressed
    byte[] val = toBinary(cont);
    if(cont instanceof AStr && enc != null && enc != UTF8) val = encode(val, enc, qc);

    try {
      out.level(lvl == null ? level : Token.toInt(lvl));
    } catch(final IllegalArgumentException ex) {
      throw ARCH_LEVEL_X.get(info, lvl);
    }
    out.write(ze, val);
  }

  /**
   * Encodes the specified string to another encoding.
   * @param value value to be encoded
   * @param encoding encoding
   * @param qc query context
   * @return encoded string
   * @throws QueryException query exception
   */
  private byte[] encode(final byte[] value, final String encoding, final QueryContext qc)
      throws QueryException {
    try {
      return FNConvert.toString(new ArrayInput(value), encoding, qc);
    } catch(final IOException ex) {
      throw ARCH_ENCODE_X.get(info, ex);
    }
  }

  /**
   * Checks if the specified item is a string or element.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  private Item checkElmStr(final Item it) throws QueryException {
    if(it instanceof AStr || TEST.eq(it)) return it;
    throw ELMSTR_X_X_X.get(info, Q_ENTRY.prefixId(XML), it.type, it);
  }
}
