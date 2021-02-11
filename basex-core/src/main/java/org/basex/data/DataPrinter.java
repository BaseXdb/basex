package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Serializes the database table to a string representation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DataPrinter {
  /** Data reference. */
  private final Data data;
  /** Table reference. */
  private final Table table;
  /** Namespaces. */
  private final TokenBuilder nsp = new TokenBuilder();

  /**
   * Constructor.
   * @param data data reference
   */
  public DataPrinter(final Data data) {
    this.data = data;
    table = new Table();
    table.header.add(TABLEPRE);
    table.header.add(TABLEDIST);
    table.header.add(TABLESIZE);
    table.header.add(TABLEATS);
    table.header.add(TABLEID);
    table.header.add(TABLENS);
    table.header.add(TABLEKND);
    table.header.add(TABLECON);
    for(int i = 0; i < 6; ++i) table.align.add(true);
  }

  /**
   * Adds the specified table entries.
   * @param start first node to be added
   * @param end last node to be added
   */
  public void add(final int start, final int end) {
    final int ps = Math.max(0, start), pe = Math.min(data.meta.size, end);
    for(int p = ps; p < pe; ++p) add(p);
    final byte[] ns = data.nspaces.table(ps, pe);
    if(ns.length != 0) nsp.add(NL).add(ns).add(data.nspaces.toString(ps, pe)).add(NL);
  }

  /**
   * Adds an entry for the specified pre value.
   * @param pre node to be added
   */
  private void add(final int pre) {
    final int k = data.kind(pre);
    final TokenList tl = new TokenList();
    tl.add(pre);
    tl.add(pre - data.parent(pre, k));
    tl.add(data.size(pre, k));
    tl.add(data.attSize(pre, k));
    tl.add(data.id(pre));
    final int uriId = data.uriId(pre, k);
    if(data.nsFlag(pre)) tl.add("+" + uriId);
    else tl.add(uriId);
    tl.add(TABLEKINDS[k]);

    final byte[] cont;
    if(k == Data.ELEM) {
      cont = data.name(pre, Data.ELEM);
    } else if(k == Data.ATTR) {
      cont = Token.concat(data.name(pre, Data.ATTR), ATT1, data.text(pre, false), ATT2);
    } else {
      cont = data.text(pre, true);
    }
    tl.add(Token.replace(Token.chop(cont, 64), '\n', ' '));
    table.contents.add(tl);
  }

  /**
   * Returns the token representation.
   * @return table
   */
  public byte[] finish() {
    return Token.concat(table.finish(), nsp.finish());
  }

  @Override
  public String toString() {
    return Token.string(finish());
  }
}
