package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.zip.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreate extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayOutput ao = new ArrayOutput();
    create(ao, qc);
    return B64.get(ao.finish());
  }

  /**
   * Creates the archive.
   * @param os output stream
   * @param qc query context
   * @throws QueryException query exception
   */
  public final void create(final OutputStream os, final QueryContext qc) throws QueryException {
    final Map<String, Entry<Item, Item>> files = toFiles(arg(0), arg(1), qc);
    final CreateOptions options = toOptions(arg(2), new CreateOptions(), qc);
    create(files, options, os, qc);
  }

  /**
   * Creates the archive.
   * @param files entries to add to the archive
   * @param opts create options
   * @param os output stream
   * @param qc query context
   * @throws QueryException query exception
   */
  final void create(final Map<String, Entry<Item, Item>> files, final CreateOptions opts,
      final OutputStream os, final QueryContext qc) throws QueryException {

    // check options
    final String format = opts.get(CreateOptions.FORMAT).toLowerCase(Locale.ENGLISH);
    if(format.equals(GZIP) && files.size() > 1) throw ARCHIVE_SINGLE_X.get(info, format);

    final int level = level(opts);
    try(ArchiveOut out = ArchiveOut.get(format, info, os)) {
      out.level(level);
      try {
        for(final Entry<Item, Item> file : files.values()) {
          add(file, out, level, "", qc);
        }
      } catch(final IOException ex) {
        throw ARCHIVE_ERROR_X.get(info, ex);
      }
    }
  }

  /**
   * Returns the compression level.
   * @param options create options
   * @return level
   * @throws QueryException query exception
   */
  final int level(final CreateOptions options) throws QueryException {
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
   * @param file archive entry
   * @param out output archive
   * @param level default compression level
   * @param root root path (empty, or ending with slash)
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final void add(final Entry<Item, Item> file, final ArchiveOut out, final int level,
      final String root, final QueryContext qc) throws QueryException, IOException {

    // create new zip entry
    final Item header = file.getKey();
    final ZipEntry ze = new ZipEntry(root + name(header));

    final long time = timestamp(header, qc);
    if(time >= 0) ze.setTime(time);
    final int lvl = level(header);
    out.level(lvl >= 0 ? lvl : level);

    // data to be compressed
    final Item content = file.getValue();
    if(content instanceof Bin) {
      out.write(ze, (Bin) content, info);
    } else {
      out.write(ze, encode(toBytes(content), encoding(header), false, qc));
    }
  }

  /**
   * Returns archive entries and contents.
   * @param entries entries
   * @param contents contents
   * @param qc query context
   * @return map with file names, and header/content data
   * @throws QueryException query exception
   */
  final Map<String, Entry<Item, Item>> toFiles(final Expr entries, final Expr contents,
      final QueryContext qc) throws QueryException {
    final Iter entrs = entries.iter(qc), cntnts = contents.iter(qc);
    final Map<String, Entry<Item, Item>> files = new LinkedHashMap<>();
    int e = 0, c = 0;
    while(true) {
      final Item item = qc.next(entrs), cn = cntnts.next();
      if(item == null || cn == null) {
        // count remaining entries
        if(cn != null) do c++; while(cntnts.next() != null);
        if(item != null) do e++; while(entrs.next() != null);
        if(e != c) throw ARCHIVE_NUMBER_X_X.get(info, e, c);
        break;
      }
      files.put(toString(item, qc), new SimpleEntry<>(item, cn));
      e++;
      c++;
    }
    return files;
  }

  /**
   * Returns the name of a ZIP entry.
   * @param header header
   * @return name
   * @throws QueryException query exception
   */
  final String name(final Item header) throws QueryException {
    final String name = string(header.string(info));
    if(name.isEmpty()) throw ARCHIVE_NAME.get(info);
    return Prop.WIN  ? name.replace('\\', '/') : name;
  }

  /**
   * Returns the compression level attribute.
   * @param header header
   * @return level (negative if unknown)
   * @throws QueryException query exception
   */
  final int level(final Item header) throws QueryException {
    if(header instanceof ANode) {
      final byte[] value  = ((ANode) header).attribute(Q_LEVEL);
      if(value != null) {
        final int level = toInt(value);
        if(level < 0 || level > 9) throw ARCHIVE_LEVEL_X.get(info, value);
        return level;
      }
    }
    return -1;
  }

  /**
   * Returns timestamp of an archive entry.
   * @param header header
   * @param qc query context
   * @return timestamp in milliseconds (negative if unknown)
   * @throws QueryException query exception
   */
  final long timestamp(final Item header, final QueryContext qc) throws QueryException {
    if(header instanceof ANode) {
      final byte[] value  = ((ANode) header).attribute(Q_LAST_MODIFIED);
      try {
        if(value != null) return toMs(new Dtm(value, info), qc);
      } catch(final QueryException qe) {
        Util.debug(qe);
        throw ARCHIVE_TIMESTAMP_X.get(info, value);
      }
    }
    return -1;
  }

  /**
   * Returns the encoding of an archive entry.
   * @param header header
   * @return encoding ({@code UTF-8} if unknown)
   * @throws QueryException query exception
   */
  final String encoding(final Item header) throws QueryException {
    if(header instanceof ANode) {
      final byte[] value = ((ANode) header).attribute(Q_ENCODING);
      if(value != null) {
        final String encoding = Strings.normEncoding(string(value));
        if(!Charset.isSupported(encoding)) throw ARCHIVE_ENCODE1_X.get(info, value);
        return encoding;
      }
    }
    return Strings.UTF8;
  }
}
