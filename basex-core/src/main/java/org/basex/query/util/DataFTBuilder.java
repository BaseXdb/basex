package org.basex.query.util;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

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
   * @param nd node to be added
   * @return number of added nodes
   */
  TokenList build(final ANode nd) {
    // check full-text mode
    if(!(nd instanceof DBNode)) return null;

    // check if full-text data exists for the current node
    final DBNode node = (DBNode) nd;
    return build(node.data, node.pre, nd.string());
  }

  /**
   * Builds full-text information.
   * @param data data reference
   * @param pre pre value
   * @param string string value
   * @return number of added nodes
   */
  private TokenList build(final Data data, final int pre, final byte[] string) {
    final FTPos ftp = pos.get(data, pre);
    if(ftp == null) return null;

    boolean marked = false;
    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    final FTLexer lex = new FTLexer().all().init(string);
    int ln = -len;
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      // check if current text is still to be marked or already marked
      if(ftp.contains(span.pos) || marked) {
        if(!tb.isEmpty()) {
          // write current text node
          ln += tb.size();
          tl.add(tb.next());
          // skip construction
          if(ln >= 0 && tl.size() > 1 && !marked) break;
        }
        if(!marked) tl.add((byte[]) null);
        marked ^= true;
      }
      // add span
      tb.add(span.text);
    }
    // write last text node
    if(!tb.isEmpty()) {
      ln += tb.size();
      tl.add(tb.finish());
    }

    // chop first and last text
    if(ln > 0) {
      final int ts = tl.size();
      // get first text (empty if it is a full-text match)
      final byte[] first = tl.get(0) != null ? tl.get(0) : EMPTY;
      final int firstl = first.length;
      // get last text (empty if it is a full-text match)
      final byte[] last = tl.get(ts - 2) != null ? tl.get(ts - 1) : EMPTY;
      final int lastl = last.length;

      // remove leading characters of first text
      if(first != EMPTY) {
        final double fl = firstl + lastl;
        final int l = Math.min(firstl, (int) (firstl / fl * ln));
        tl.set(0, concat(DOTS, subtoken(first, l)));
        ln -= l;
      }
      // remove trailing characters of last text
      if(last != EMPTY && ln > 0) {
        final int ll = Math.min(lastl, ln);
        tl.set(ts - 1, concat(subtoken(last, 0, lastl - ll), DOTS));
        ln -= ll;
      }
      // still too much text: shorten inner texts
      for(int t = ts - 2; t > 0 && ln > 0; t--) {
        final byte[] txt = tl.get(t);
        // skip elements, marked texts and too short text snippets
        if(txt == null || tl.get(t - 1) == null) continue;
        final int txtl = txt.length;
        final int ll = Math.min(txtl, ln);
        tl.set(t, concat(subtoken(txt, 0, (txtl - ll) / 2), DOTS,
                subtoken(txt, (txtl + ll) / 2)));
        ln -= ll;
      }
    }
    return tl;
  }
}