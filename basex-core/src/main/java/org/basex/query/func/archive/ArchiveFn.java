package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.convert.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveFn extends StandardFunc {
  /**
   * Encodes the specified string to another encoding.
   * @param value value to be encoded
   * @param encoding encoding (can be {@code null})
   * @param enforce enforce conversion for UTF-8
   * @param qc query context
   * @return encoded string
   * @throws QueryException query exception
   */
  final byte[] encode(final byte[] value, final String encoding, final boolean enforce,
      final QueryContext qc) throws QueryException {
    if(encoding == Strings.UTF8 && !enforce) return value;
    try {
      final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);
      return ConvertFn.toString(new ArrayInput(value), encoding, !validate);
    } catch(final IOException ex) {
      throw ARCHIVE_ENCODE2_X.get(info, ex);
    }
  }

  /**
   * Returns all archive entries from the specified argument.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param qc query context
   * @return set with all entries, or {@code null} if no entries are specified
   * @throws QueryException query exception
   */
  final HashSet<String> toEntries(final Expr expr, final QueryContext qc) throws QueryException {
    final HashSet<String> entries = new LinkedHashSet<>();
    final Iter names = expr.unwrappedIter(qc);
    for(Item item; (item = qc.next(names)) != null;) {
      entries.add(toString(item, qc));
    }
    return entries.isEmpty() ? null : entries;
  }

  /**
   * Evaluates an expression to an archive reference.
   * @param expr expression
   * @param qc query context
   * @return archive reference: {@link Bin} or {@link IO})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final Object toInput(final Expr expr, final QueryContext qc) throws QueryException, IOException {
    final Item archive = expr.atomItem(qc, info);
    if(archive instanceof final Bin bin) {
      if(bin instanceof final B64Lazy lazy) {
        final IO io = lazy.input();
        if(localZip(io)) return io;
      }
      return bin;
    }
    if(!archive.type.isStringOrUntyped()) throw STRBIN_X_X.get(info, archive.seqType(), archive);

    final IO io = toIO(archive, qc);
    return localZip(io) ? io : new B64Lazy(io, FILE_IO_ERROR_X);
  }

  /**
   * Rewrites an archive into a new binary blob. Iterates entries of the source archive
   * (dispatching between streaming and random access), invokes {@code action} for each entry,
   * and finally runs {@code finish} so callers can append new entries.
   *
   * <p>The action's return value controls verbatim copying: {@code true} copies the original
   * body to {@code out}, {@code false} skips it (the action may have written a replacement
   * to {@code out} before returning).</p>
   *
   * <p>GZIP sources are rejected — rewriting requires multi-entry archive support.</p>
   *
   * @param expr archive expression
   * @param qc query context
   * @param action per-entry action
   * @param finish post-iteration action (writes additional entries; may be a no-op)
   * @return rewritten archive
   * @throws QueryException query exception
   */
  final B64 rewrite(final Expr expr, final QueryContext qc,
      final EntryAction action, final ArchiveOutAction finish) throws QueryException {
    try {
      final Object archive = toInput(expr, qc);
      if(archive instanceof final Bin bin) {
        try(BufferInput bi = bin.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          final String format = in.format();
          if(in instanceof GZIPIn) throw ARCHIVE_MODIFY_X.get(info, format);
          final SpillOutput so = new SpillOutput(qc);
          try(ArchiveOut out = ArchiveOut.get(format, info, so)) {
            while(in.more()) {
              if(action.apply(in.entry(), out)) out.write(in);
            }
            finish.apply(out);
          }
          return so.finish(ARCHIVE_ERROR_X);
        }
      }
      try(ZipFile zip = new ZipFile(new File(archive.toString()), Strings.CP437)) {
        final SpillOutput so = new SpillOutput(qc);
        try(ArchiveOut out = ArchiveOut.get(ZIP, info, so)) {
          for(final ZipEntry raw : entries(zip, null)) {
            final ZipEntry ze = canonical(raw);
            if(action.apply(ze, out)) {
              try(InputStream is = zip.getInputStream(raw)) {
                out.write(ze, is);
              }
            }
          }
          finish.apply(out);
        }
        return so.finish(ARCHIVE_ERROR_X);
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }

  /** Per-entry action for {@link #rewrite}: returns {@code true} to copy the original body. */
  @FunctionalInterface interface EntryAction {
    /**
     * Processes a source entry.
     * @param entry source entry
     * @param out archive output
     * @return {@code true} to copy verbatim, {@code false} to skip
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    boolean apply(ZipEntry entry, ArchiveOut out) throws IOException, QueryException;
  }

  /** Post-iteration action for {@link #rewrite}. */
  @FunctionalInterface interface ArchiveOutAction {
    /**
     * Performs an action on the archive output.
     * @param out archive output
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    void apply(ArchiveOut out) throws IOException, QueryException;
  }

  /**
   * Checks if the specified resource is a local ZIP archive.
   * @param io IO reference
   * @return result of check
   * @throws IOException I/O exception
   */
  static boolean localZip(final IO io) throws IOException {
    if(io instanceof IOFile) {
      if(io.hasSuffix(IO.ZIPSUFFIX)) return true;
      try(InputStream is = io.inputStream()) {
        if(is.read() == 'P' && is.read() == 'K') return true;
      }
    }
    return false;
  }

  /**
   * Returns ZIP entries.
   * @param zip ZIP file
   * @param entries entries to be returned
   * @return ZIP entries
   */
  static ArrayList<? extends ZipEntry> entries(final ZipFile zip, final HashSet<String> entries) {
    if(entries == null) return Collections.list(zip.entries());

    final ArrayList<ZipEntry> list = new ArrayList<>();
    for(final String entry : entries) {
      final ZipEntry ze = lookup(zip, entry);
      if(ze != null) list.add(ze);
    }
    return list;
  }

  /**
   * Looks up an entry by name, falling back to mojibake-fixed name matching so on-disk archives
   * accept the same names that the streaming reader exposes. The returned entry keeps its original
   * name so that {@link ZipFile#getInputStream} can still locate the entry's data.
   * Callers that need the corrected name should use their lookup key.
   * @param zip ZIP file
   * @param name entry name (as seen by callers, after mojibake fix)
   * @return matching entry, or {@code null}
   */
  private static ZipEntry lookup(final ZipFile zip, final String name) {
    final ZipEntry entry = zip.getEntry(name);
    if(entry != null) return entry;

    final String canonical = Strings.canonical(name);
    final Enumeration<? extends ZipEntry> en = zip.entries();
    while(en.hasMoreElements()) {
      final ZipEntry ze = en.nextElement();
      if(canonical.equals(Strings.canonical(ze.getName()))) return ze;
    }
    return null;
  }

  /**
   * Returns a copy of the entry with its name canonicalized (mojibake-fixed), or the entry itself
   * if it is already canonical (or {@code null}).
   * @param entry source entry (can be {@code null})
   * @return entry with canonical name (can be {@code null})
   */
  static ZipEntry canonical(final ZipEntry entry) {
    if(entry == null) return null;
    final String name = entry.getName(), c = Strings.canonical(name);
    if(c.equals(name)) return entry;
    final ZipEntry ze = new ZipEntry(c);
    ze.setCompressedSize(entry.getCompressedSize());
    ze.setTime(entry.getTime());
    ze.setComment(entry.getComment());
    ze.setExtra(entry.getExtra());
    if(entry.getMethod() != -1) ze.setMethod(entry.getMethod());
    if(entry.getSize() != -1) ze.setSize(entry.getSize());
    if(entry.getCrc() != -1) ze.setCrc(entry.getCrc());
    return ze;
  }

  /**
   * Iterates archive entries with their bodies, dispatching between streaming and random access.
   * Local ZIP files are opened with {@link ZipFile} (direct entry lookup, tolerates local-header
   * inconsistencies that {@link java.util.zip.ZipInputStream} would reject); other inputs are
   * streamed via {@link ArchiveIn}. Entry names are canonicalized in both branches; the body is
   * exposed lazily so metadata-only callers (e.g. {@code archive:entries}) avoid unused streams.
   * @param expr archive expression
   * @param qc query context
   * @param filter entry names to include (mutated for the streaming branch; can be {@code null})
   * @param consumer per-entry action
   * @throws QueryException query exception
   */
  final void forEachEntry(final Expr expr, final QueryContext qc, final HashSet<String> filter,
      final EntryConsumer consumer) throws QueryException {
    try {
      final Object archive = toInput(expr, qc);
      if(archive instanceof final Bin bin) {
        try(BufferInput bi = bin.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          // wrapper shields the outer iteration from try-with-resources in the callback
          final InputStream nonClosing = new FilterInputStream(in) {
            @Override public void close() { /* intentionally not propagated */ }
          };
          while(in.more() && (filter == null || !filter.isEmpty())) {
            final ZipEntry ze = in.entry();
            if(filter == null || filter.remove(ze.getName())) {
              consumer.accept(ze, () -> nonClosing);
            }
          }
        }
      } else {
        try(ZipFile zip = new ZipFile(new File(archive.toString()), Strings.CP437)) {
          for(final ZipEntry raw : entries(zip, filter)) {
            consumer.accept(canonical(raw), () -> zip.getInputStream(raw));
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }

  /** Lazy body supplier for {@link #forEachEntry}. */
  @FunctionalInterface interface BodySupplier {
    /**
     * Opens the body stream.
     * @return body input stream (caller closes; streaming branch returns a non-closing view)
     * @throws IOException I/O exception
     */
    InputStream get() throws IOException;
  }

  /** Per-entry callback for {@link #forEachEntry}. */
  @FunctionalInterface interface EntryConsumer {
    /**
     * Processes an archive entry.
     * @param entry archive entry (canonical name)
     * @param body lazy supplier for the entry's body
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    void accept(ZipEntry entry, BodySupplier body) throws IOException, QueryException;
  }
}
