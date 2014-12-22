package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.list.*;

/**
 * This is a table representation for textual table output.
 * It should be guaranteed that the {@link #header} object has the
 * same number of entries as all {@link #contents} string arrays.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Table {
  /** Distance between table columns. */
  private static final int DIST = 2;

  /** Table header. */
  public final TokenList header = new TokenList();
  /** Alignment (false: left, true: right alignment). */
  public final BoolList align = new BoolList();
  /** Table contents. */
  public final ArrayList<TokenList> contents = new ArrayList<>();
  /** Data description; if available, will be added as footer. */
  public String description;

  /**
   * Default constructor.
   */
  public Table() { }

  /**
   * Constructor with table input.
   * @param in textual table input
   */
  public Table(final String in) {
    if(in.isEmpty()) return;

    // parse table header
    final Scanner scan = new Scanner(in);
    byte[] line = token(scan.nextLine());
    final IntList il = new IntList();
    int l = 0;
    final int ll = line.length;
    while(l < ll) {
      il.add(l);
      // find next two spaces
      while(++l + 1 < ll && (line[l] != ' ' || line[l + 1] != ' '));
      header.add(substring(line, il.get(il.size() - 1), l));
      while(++l < ll && line[l] == ' ');
    }
    il.add(l);

    // skip delimiter
    scan.nextLine();

    // parse table entries
    final int s = il.size() - 1;
    while((line = token(scan.nextLine())).length != 0) {
      final TokenList entry = new TokenList();
      for(int e = 0; e < s; ++e) {
        entry.add(trim(substring(line, il.get(e), il.get(e + 1))));
      }
      contents.add(entry);
    }
  }

  /**
   * Sorts the table by the first column.
   * @return self reference
   */
  public Table sort() {
    Collections.sort(contents, new Comparator<TokenList>() {
      @Override
      public int compare(final TokenList tl1, final TokenList tl2) {
        return diff(lc(tl1.get(0)), lc(tl2.get(0)));
      }
    });
    return this;
  }

  /**
   * Moves the specified string to top.
   * @param top entry to be moved to the top
   * @return self reference
   */
  public Table toTop(final byte[] top) {
    for(int i = 0; i < contents.size(); ++i) {
      if(eq(top, contents.get(i).get(0))) {
        contents.add(0, contents.remove(i));
        break;
      }
    }
    return this;
  }

  /**
   * Returns a textual representation of the table.
   * @return text
   */
  public byte[] finish() {
    final int[] ind = new int[header.size()];
    final int sz = header.size();
    for(int s = 0; s < sz; ++s) {
      for(final TokenList e : contents) {
        ind[s] = Math.max(ind[s], e.get(s).length);
      }
      ind[s] = Math.max(ind[s], header.get(s).length);
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int u = 0; u < sz; ++u) {
      final byte[] s = header.get(u);
      final int is = ind[u] - s.length + DIST;
      tb.add(s);
      for(int i = 0; i < is; ++i) tb.add(' ');
    }
    tb.add(NL);
    for(int u = 0; u < sz; ++u) {
      for(int i = 0; i < ind[u] + (u + 1 == sz ? 0 : DIST); ++i) tb.add('-');
    }
    tb.add(NL);
    for(final TokenList e : contents) {
      for(int u = 0; u < sz; ++u) {
        final byte[] s = e.get(u);
        final int is = ind[u] - s.length;
        if(u < align.size() && align.get(u)) {
          for(int i = 0; i < is; ++i) tb.add(' ');
          tb.add(s);
        } else {
          tb.add(s);
          for(int i = 0; i < is; ++i) tb.add(' ');
        }
        for(int i = 0; i < DIST; ++i) tb.add(' ');
      }
      tb.add(NL);
    }
    if(description != null) {
      tb.add(NL).addExt(description, contents.size()).add(DOT);
    }
    return tb.finish();
  }

  @Override
  public String toString() {
    return string(finish());
  }
}
