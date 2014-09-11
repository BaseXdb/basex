package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreate extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter entr = qc.iter(exprs[0]), cont = qc.iter(exprs[1]);
    final Options opts = toOptions(2, Q_OPTIONS, new ArchOptions(), qc);

    final String format = opts.get(ArchOptions.FORMAT);
    final ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info);
    // check algorithm
    final String alg = opts.get(ArchOptions.ALGORITHM);
    int level = ZipEntry.DEFLATED;
    if(alg != null) {
      if(format.equals(ZIP)  && !eq(alg, STORED, DEFLATE) ||
         format.equals(GZIP) && !eq(alg, DEFLATE)) {
        throw ARCH_SUPP_X_X.get(info, ArchOptions.ALGORITHM.name(), alg);
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
        add(checkElemToken(en), cn, out, level, qc);
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
   * Adds the specified entry to the output stream.
   * @param entry entry descriptor
   * @param cont contents
   * @param out output archive
   * @param level default compression level
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final void add(final Item entry, final Item cont, final ArchiveOut out, final int level,
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
      out.level(lvl == null ? level : toInt(lvl));
    } catch(final IllegalArgumentException ex) {
      throw ARCH_LEVEL_X.get(info, lvl);
    }
    out.write(ze, val);
  }
}
