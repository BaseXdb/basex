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
  /** Distance between table columns. */
  public static final int DIST = 2;
  /** Alignment (false: left, true: right alignment). */
  public BoolList align = new BoolList();
  /** Data (usually strings). */
  public ArrayList<StringList> contents = new ArrayList<StringList>();
  /** Data description. */
  public String desc;

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
      final StringList entry = new StringList();
      for(int e = 0; e < s; e++) {
        entry.add(line.substring(il.get(e), il.get(e + 1)).trim());
      }
      contents.add(entry);
    }
  }

  /**
   * Sorts the table by the first column.
   */
  public void sort() {
    for(int i = 0; i < contents.size() - 2; i++) {
      final String s = contents.get(i).get(0).toLowerCase();
      for(int j = i + 1; j < contents.size(); j++) {
        if(s.compareTo(contents.get(j).get(0).toLowerCase()) > 0) {
          final StringList tmp = contents.get(i);
          contents.set(i, contents.get(j));
          contents.set(j, tmp);
        }
      }
    }
  }

  /**
   * Returns a textual representation of the table.
   * @return text
   */
  public byte[] finish() {
    final int[] ind = new int[header.size()];
    final int sz = header.size();
    for(int s = 0; s < sz; s++) {
      for(final StringList e : contents) {
        ind[s] = Math.max(ind[s], e.get(s).length());
      }
      ind[s] = Math.max(ind[s], header.get(s).length());
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int u = 0; u < sz; u++) {
      final String s = header.get(u);
      final int is = ind[u] - s.length() + DIST;
      tb.add(s);
      for(int i = 0; i < is; i++) tb.add(' ');
    }
    tb.add(NL);
    for(int u = 0; u < sz; u++) {
      for(int i = 0; i < ind[u] + DIST; i++) tb.add('-');
    }
    tb.add(NL);
    for(final StringList e : contents) {
      for(int u = 0; u < sz; u++) {
        final String s = e.get(u);
        final int is = ind[u] - s.length();
        if(u < align.size() && align.get(u)) {
          for(int i = 0; i < is; i++) tb.add(' ');
          tb.add(s);
        } else {
          tb.add(s);
          for(int i = 0; i < is; i++) tb.add(' ');
        }
        for(int i = 0; i < DIST; i++) tb.add(' ');
      }
      tb.add(NL);
    }
    if(desc != null) tb.add(NL + contents.size() + ' ' + desc + " found.");
    return tb.finish();
  }
}
