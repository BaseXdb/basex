package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;
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

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreate extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Map<String, Item[]> map = toMap(0, qc);
    final CreateOptions opts = toOptions(2, new CreateOptions(), qc);

    // check options
    final String format = opts.get(CreateOptions.FORMAT);
    if(format.equals(GZIP) && map.size() > 1) throw ARCHIVE_SINGLE_X.get(info, format);

    final int level = level(opts);
    try(ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info)) {
      out.level(level);
      try {
        for(final Item[] entry : map.values()) {
          add(entry, out, level, "", qc);
        }
      } catch(final IOException ex) {
        throw ARCHIVE_ERROR_X.get(info, ex);
      }
      return B64.get(out.finish());
    }
  }

  /**
   * Returns the compression level.
   * @param options options
   * @return level
   * @throws QueryException query exception
   */
  protected int level(final CreateOptions options) throws QueryException {
    int level = ZipEntry.DEFLATED;
    final String format = options.get(CreateOptions.FORMAT);
    final String alg = options.get(CreateOptions.ALGORITHM);
    if(alg != null) {
      if(format.equals(ZIP)  && !Strings.eq(alg, STORED, DEFLATE) ||
         format.equals(GZIP) && !Strings.eq(alg, DEFLATE)) {
        throw ARCHIVE_FORMAT_X_X.get(info, CreateOptions.ALGORITHM.name(), alg);
      }
      if(Strings.eq(alg, STORED)) level = ZipEntry.STORED;
    }
    return level;
  }

  /**
   * Adds the specified entry to the output stream.
   * @param entry archive entry
   * @param out output archive
   * @param level default compression level
   * @param root root path (empty, or ending with slash)
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected final void add(final Item[] entry, final ArchiveOut out, final int level,
      final String root, final QueryContext qc) throws QueryException, IOException {

    // create new zip entry
    final Item header = entry[0], content = entry[1];
    String name = string(header.string(info));
    if(name.isEmpty()) throw ARCHIVE_DESCRIPTOR1.get(info);
    if(Prop.WIN) name = name.replace('\\', '/');

    final ZipEntry ze = new ZipEntry(root + name);
    String encoding = Strings.UTF8;

    // compression level
    byte[] lvl = null;
    if(header instanceof ANode) {
      final ANode el = (ANode) header;
      lvl = el.attribute(LEVEL);

      // last modified
      final byte[] mod = el.attribute(LAST_MODIFIED);
      if(mod != null) {
        try {
          ze.setTime(dateTimeToMs(new Dtm(mod, info), qc));
        } catch(final QueryException qe) {
          Util.debug(qe);
          throw ARCHIVE_DESCRIPTOR3_X.get(info, mod);
        }
      }

      // encoding
      final byte[] ea = el.attribute(ENCODING);
      if(ea != null) {
        encoding = Strings.normEncoding(string(ea));
        if(!Charset.isSupported(encoding)) throw ARCHIVE_ENCODE1_X.get(info, ea);
      }
    }

    // data to be compressed
    byte[] value = toBytes(content);
    if(!(content instanceof Bin) && encoding != Strings.UTF8) value = encode(value, encoding, qc);

    try {
      out.level(lvl == null ? level : toInt(lvl));
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw ARCHIVE_DESCRIPTOR2_X.get(info, lvl);
    }
    out.write(ze, value);
  }

  /**
   * Returns archive entries and contents.
   * @param i index for supplied entries
   * @param qc query context
   * @return map with file names and entries and contents
   * @throws QueryException query exception
   */
  final Map<String, Item[]> toMap(final int i, final QueryContext qc) throws QueryException {
    final Iter entries = exprs[i].iter(qc), contents = exprs[i + 1].iter(qc);
    final Map<String, Item[]> map = new LinkedHashMap<>();
    int e = 0, c = 0;
    while(true) {
      final Item en = qc.next(entries), cn = contents.next();
      if(en == null || cn == null) {
        // count remaining entries
        if(cn != null) do c++; while(contents.next() != null);
        if(en != null) do e++; while(entries.next() != null);
        if(e != c) throw ARCHIVE_NUMBER_X_X.get(info, e, c);
        break;
      }
      map.put(string(checkElemToken(en).string(info)), new Item[] { en, cn });
      e++;
      c++;
    }
    return map;
  }
}
