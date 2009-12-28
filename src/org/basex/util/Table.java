package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
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
public final class Table {
  /** Table header. */
  public TokenList header = new TokenList();
  /** Distance between table columns. */
  public static final int DIST = 2;
  /** Alignment (false: left, true: right alignment). */
  public BoolList align = new BoolList();
  /** Data (usually strings). */
  public ArrayList<TokenList> contents = new ArrayList<TokenList>();
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
    if(in.length() == 0) return;

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
    while(!(line = scan.nextLine()).isEmpty()) {
      final TokenList entry = new TokenList();
      for(int e = 0; e < s; e++) {
        entry.add(token(line.substring(il.get(e), il.get(e + 1)).trim()));
      }
      contents.add(entry);
    }
  }

  /**
   * Sorts the table by the first column.
   */
  public void sort() {
    for(int i = 0; i < contents.size() - 1; i++) {
      for(int j = i + 1; j < contents.size(); j++) {
        if(diff(lc(contents.get(i).get(0)), lc(contents.get(j).get(0))) > 0) {
          final TokenList tmp = contents.get(i);
          contents.set(i, contents.get(j));
          contents.set(j, tmp);
        }
      }
    }
  }

  /**
   * Returns the value for the specified table position.
   * @param r row
   * @param c column
   * @return value
   */
  public String value(final int r, final int c) {
    return Token.string(contents.get(r).get(c));
  }

  /**
   * Returns the number of rows.
   * @return number of rows
   */
  public int rows() {
    return contents.size();
  }

  /**
   * Returns the number of columns.
   * @return number of columns
   */
  public int cols() {
    return header.size();
  }

  /**
   * Moves the specified string to top.
   * @param top entry to be moved to the top
   */
  public void toTop(final byte[] top) {
    TokenList tl = null;
    int i = -1;
    while(++i < contents.size()) {
      tl = contents.get(i);
      if(eq(top, lc(tl.get(0)))) break;
    }
    if(i == contents.size()) return;
    while(--i >= 0) contents.set(i + 1, contents.get(i));
    contents.set(0, tl);
  }

  /**
   * Returns a textual representation of the table.
   * @return text
   */
  public byte[] finish() {
    final int[] ind = new int[header.size()];
    final int sz = header.size();
    for(int s = 0; s < sz; s++) {
      for(final TokenList e : contents) {
        ind[s] = Math.max(ind[s], e.get(s).length);
      }
      ind[s] = Math.max(ind[s], header.get(s).length);
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int u = 0; u < sz; u++) {
      final byte[] s = header.get(u);
      final int is = ind[u] - s.length + DIST;
      tb.add(s);
      for(int i = 0; i < is; i++) tb.add(' ');
    }
    tb.add(NL);
    for(int u = 0; u < sz; u++) {
      for(int i = 0; i < ind[u] + (u + 1 == sz ? 0 : DIST); i++) tb.add('-');
    }
    tb.add(NL);
    for(final TokenList e : contents) {
      for(int u = 0; u < sz; u++) {
        final byte[] s = e.get(u);
        final int is = ind[u] - s.length;
        if(u < align.size() && align.get(u)) {
          for(int i = 0; i < is; i++) tb.add((byte) ' ');
          tb.add(s);
        } else {
          tb.add(s);
          for(int i = 0; i < is; i++) tb.add((byte) ' ');
        }
        for(int i = 0; i < DIST; i++) tb.add((byte) ' ');
      }
      tb.add(NL);
    }
    if(desc != null) tb.add(NL + contents.size() + ' ' + desc + DOT);
    return tb.finish();
  }
}
