package org.basex.util;

import static org.basex.core.Text.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is a table representation for textual table output.
 * It should be guaranteed that the {@link #header} object has the
 * same number of entries as all {@link #contents} string arrays.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Table {
  /** Table header. */
  public StringList header = new StringList();
  /** Data (usually strings). */
  public ArrayList<String[]> contents = new ArrayList<String[]>();

  /**
   * Default constructor.
   */
  public Table() { }

  /**
   * Constructor with table input.
   * @param in textual table input
   */
  public Table(final String in) {
    // parse table header
    final Scanner scan = new Scanner(in);
    String line = scan.nextLine();
    final IntList il = new IntList();
    int i = 0;
    while(i < line.length()) {
      il.add(i);
      while(++i < line.length() && line.charAt(i) != ' ');
      header.add(line.substring(il.get(il.size() - 1), i));
      while(++i < line.length() && line.charAt(i) == ' ');
    }
    il.add(i);

    // skip delimiter
    scan.nextLine();

    // parse table entries
    final int s = il.size() - 1;
    while((line = scan.nextLine()).length() != 0) {
      final String[] entry = new String[s];
      for(int e = 0; e < entry.length; e++) {
        entry[e] = line.substring(il.get(e),
            e + 1 == entry.length ? line.length() : il.get(e + 1)).trim();
      }
      contents.add(entry);
    }
  }

  @Override
  public String toString() {
    final int[] ind = new int[header.size()];
    final int sz = header.size();
    for(int s = 0; s < sz; s++) {
      for(final String[] e : contents) ind[s] = Math.max(ind[s], e[s].length());
      ind[s] = Math.max(ind[s], header.get(s).length()) + 2;
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int u = 0; u < sz; u++) tb.add(ind[u], header.get(u));
    tb.add(NL);
    for(int u = 0; u < sz; u++) {
      for(int i = 0; i < ind[u]; i++) tb.add('-');
    }
    tb.add(NL);
    for(final String[] e : contents) {
      for(int u = 0; u < sz; u++) tb.add(ind[u], e[u]);
      tb.add(NL);
    }
    return tb.toString();
  }
}
