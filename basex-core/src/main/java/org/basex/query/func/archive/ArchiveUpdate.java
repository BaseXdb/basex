package org.basex.query.func.archive;

import java.util.*;
import java.util.Map.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveUpdate extends ArchiveFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Map<String, Entry<Item, Item>> files = toFiles(arg(1), arg(2), qc);

    return rewrite(arg(0), qc, (entry, out) -> {
      final Entry<Item, Item> file = files.remove(entry.getName());
      if(file == null) return true;
      add(file, out, ZipEntry.DEFLATED, "", qc);
      return false;
    }, out -> {
      // add remaining entries
      for(final Entry<Item, Item> entry : files.values()) {
        add(entry, out, ZipEntry.DEFLATED, "", qc);
      }
    });
  }
}
