package org.basex.query.util;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Class for constructing decorated full-text nodes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class DataFTBuilder {
  /** Dots. */
  private static final byte[] DOTS = token(Text.DOTS);
  /** Full-text position data. */
  private final FTPosData pos;
  /** Length of full-text extract. */
  private final int len;

  /**
   * Constructor.
   * @param pos full-text position data
   * @param len length of extract
   */
  DataFTBuilder(final FTPosData pos, final int len) {
    this.pos = pos;
    this.len = len;
  }

  /**
   * Builds full-text information.
   * @param node node to be added
   * @return added strings, or {@code null} if no full-text positions exist
   */
  ArrayList<DataFTMarker> build(final ANode node) {
    // only database nodes can have full-text positions
    if(!(node instanceof DBNode)) return null;

    // not all nodes have full-text positions
    final DBNode dbnode = (DBNode) node;
    final FTPos ftp = pos.get(dbnode.data, dbnode.pre);
    if(ftp == null) return null;

    final ArrayList<DataFTMarker> marks = new ArrayList<>();
    final TokenBuilder token = new TokenBuilder();
    // indicates if the currently parsed text is marked
    final byte[] string = node.string();
    for(final FTLexer lex = new FTLexer().all().init(string); lex.hasNext();) {
      final FTSpan span = lex.next();
      // check if current text is still to be marked or already marked
      if(!span.del && ftp.contains(span.pos)) {
        // write current text node
        if(!token.isEmpty()) marks.add(new DataFTMarker(token.next(), false));
        marks.add(new DataFTMarker(span.text, true));
      } else {
        // add span
        token.add(span.text);
      }
    }
    // write last text node
    if(!token.isEmpty()) marks.add(new DataFTMarker(token.finish(), false));

    // chop text
    int ln = -len + string.length;
    if(ln > 0) {
      final int ms = marks.size();
      final DataFTMarker first = marks.get(0);
      final int firstl = first.mark ? 0 : first.token.length;
      final DataFTMarker last = marks.get(ms - 1);
      final int lastl = last.mark ? 0 : last.token.length;

      // remove leading characters of first text
      if(!first.mark) {
        final int l = Math.min(firstl, (int) ((long) ln * firstl / (firstl + lastl)));
        if(l > 0) {
          first.token = concat(DOTS, subtoken(first.token, l));
          ln -= l;
        }
      }

      // remove trailing characters of last text
      if(!last.mark && ln > 0) {
        final int l = Math.min(lastl, ln);
        last.token = concat(subtoken(last.token, 0, lastl - l), DOTS);
        ln -= l;
      }

      // still too much text: shorten inner texts
      for(int m = ms - 2; m > 0 && ln > 0; m--) {
        final DataFTMarker dm = marks.get(m);
        // skip elements
        if(dm.mark) continue;
        final int txtl = dm.token.length;
        final int l = Math.min(txtl, ln);
        dm.token = concat(subtoken(dm.token, 0, (txtl - l) / 2), DOTS,
                subtoken(dm.token, (txtl + l) / 2));
        ln -= l;
      }

      // still too much text: remove hits
      for(int m = ms - 1; m >= 0 && ln > 0; m--) {
        DataFTMarker dm = marks.get(m);
        if(dm.mark) ln -= marks.remove(m).token.length;
      }

      // merge adjacent text nodes
      for(int m = marks.size() - 2; m >= 0; m--) {
        DataFTMarker dm1 = marks.get(m), dm2 = marks.get(m + 1);
        if(!dm1.mark && !dm2.mark) {
          if(!(eq(dm1.token, DOTS) && eq(dm2.token, DOTS))) {
            dm1.token = concat(dm1.token, dm2.token);
          }
          marks.remove(m + 1);
        }
      }
    }
    return marks;
  }

  /** Data full-text marker. */
  static final class DataFTMarker {
    /** Token. */
    byte[] token;
    /** Marker flag. */
    boolean mark;

    /**
     * Constructor.
     * @param token text
     * @param mark marker flag
     */
    private DataFTMarker(final byte[] token, final boolean mark) {
      this.token = token;
      this.mark = mark;
    }

    @Override
    public String toString() {
      return string(token) + " (" + mark + ")";
    }
  }
}