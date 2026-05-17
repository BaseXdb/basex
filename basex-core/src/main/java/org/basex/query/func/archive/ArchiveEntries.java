package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;

import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveEntries extends ArchiveFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    forEachEntry(arg(0), qc, null, (entry, body) -> {
      if(!entry.isDirectory()) vb.add(entry(entry));
    });
    return vb.value(this);
  }

  /**
   * Builds an entry element with size, timestamp and compressed-size metadata.
   * @param ze ZIP entry (canonical name)
   * @return entry element
   */
  private FNode entry(final ZipEntry ze) {
    final FBuilder elem = FElem.build(Q_ENTRY).ns();
    long value = ze.getSize();
    if(value != -1) elem.attr(Q_SIZE, value);
    value = ze.getTime();
    if(value != -1) elem.attr(Q_LAST_MODIFIED, Dtm.get(value).string(info));
    value = ze.getCompressedSize();
    if(value != -1) elem.attr(Q_COMPRESSED_SIZE, value);
    return elem.text(ze.getName()).finish();
  }
}
