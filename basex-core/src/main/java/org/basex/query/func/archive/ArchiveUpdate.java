package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArchiveUpdate extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

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
      hm.put(checkElemToken(en).string(info), new Item[] { en, cn });
      e++;
      c++;
    }
    // count remaining entries
    if(cn != null) do c++; while(cont.next() != null);
    if(en != null) do e++; while(entr.next() != null);
    if(e != c) throw ARCH_DIFF_X_X.get(info, e, c);

    try(final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
        final ArchiveOut out = ArchiveOut.get(in.format(), info)) {
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
      return new B64(out.toArray());
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    }
  }
}
