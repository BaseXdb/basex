package org.basex.query.util;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * Constructor for marked full-text results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DataFTBuilder {
  /** Dots. */
  private static final byte[] DOTS = token(Text.DOTS);
  /** Full-text position data. */
  private final FTPosData pos;
  /** Length of full-text extract. */
  private final int len;
  /** ID of marker element name. */
  private final int name;

  /** Node with the full-text positions of the last text node (can be {@code null}). */
  private XNode ftNode;
  /** Marked ranges in the string value of {@link #ftNode} (can be {@code null}). */
  private IntList ranges;
  /** Index of the next marked range. */
  private int range;
  /** Offset of the next text node in the string value of {@link #ftNode}. */
  private int offset;

  /**
   * Constructor.
   * @param pos full-text position data
   * @param len length of extract
   * @param name ID of marker element name
   */
  DataFTBuilder(final FTPosData pos, final int len, final int name) {
    this.pos = pos;
    this.len = len;
    this.name = name;
  }

  /**
   * Returns the ID of the marker element name.
   * @return ID
   */
  int name() {
    return name;
  }

  /**
   * Returns the full-text positions of a node.
   * @param node node
   * @return positions, or {@code null} if no full-text positions exist
   */
  private FTPos get(final XNode node) {
    return node instanceof final DBNode dbnode ? pos.get(dbnode.data(), dbnode.pre()) :
      pos.get(node);
  }

  /**
   * Builds full-text information.
   * @param node node to be added
   * @return added strings, or {@code null} if no full-text positions exist
   */
  ArrayList<DataFTMarker> build(final XNode node) {
    // positions may have been assigned to the text node or to one of its ancestors
    XNode nd = node;
    FTPos ftp = get(nd);
    while(ftp == null && (nd = nd.parent()) != null) ftp = get(nd);
    // not all nodes have full-text positions
    if(ftp == null) return null;

    // determine marked ranges and offset of the text node in the string value
    final int off;
    if(ftNode != null && nd.is(ftNode)) {
      off = offset;
    } else {
      off = offset(node, nd);
      ranges = ranges(ftp, nd.string());
      range = 0;
      ftNode = nd;
    }
    final byte[] string = node.string();
    final int sl = string.length;
    offset = off + sl;

    // split the text into marked and unmarked parts
    final ArrayList<DataFTMarker> marks = new ArrayList<>();
    final int rs = ranges.size();
    int s = 0, r = range;
    for(; r < rs; r += 2) {
      final int start = Math.max(ranges.get(r) - off, 0);
      final int end = Math.min(ranges.get(r + 1) - off, sl);
      // skip ranges that end before, and stop at ranges that start after the text
      if(end <= s) continue;
      if(start >= sl) break;
      if(start > s) marks.add(new DataFTMarker(subtoken(string, s, start), false));
      marks.add(new DataFTMarker(subtoken(string, start, end), true));
      s = end;
      // range may be continued in the next text node
      if(end == sl) break;
    }
    range = r;
    // write last text node
    if(s < sl) marks.add(new DataFTMarker(subtoken(string, s, sl), false));

    // chop text
    int ln = -len + string.length;
    if(ln > 0) {
      final int ms = marks.size();
      final DataFTMarker first = marks.getFirst();
      final int firstl = first.mark ? 0 : first.token.length;
      final DataFTMarker last = marks.getLast();
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
        final DataFTMarker dm = marks.get(m);
        if(dm.mark) ln -= marks.remove(m).token.length;
      }

      // merge adjacent text nodes
      for(int m = marks.size() - 2; m >= 0; m--) {
        final DataFTMarker dm1 = marks.get(m), dm2 = marks.get(m + 1);
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

  /**
   * Returns the marked ranges in a string value.
   * @param ftp full-text positions
   * @param string string value
   * @return start and end offsets of the marked ranges
   */
  private IntList ranges(final FTPos ftp, final byte[] string) {
    // adopt the language of the query: the tokenizer defines the token boundaries
    final FTOpt opt = new FTOpt();
    opt.ln = pos.language();

    final IntList list = new IntList();
    int off = 0;
    for(final FTLexer lexer = new FTLexer(opt).original().init(string); lexer.hasNext();) {
      final FTSpan span = lexer.next();
      final int tl = span.text.length;
      if(!span.del && ftp.contains(span.pos)) list.add(off).add(off + tl);
      off += tl;
    }
    return list;
  }

  /**
   * Returns the offset of a node in the string value of one of its ancestors.
   * @param node node
   * @param ancestor ancestor node
   * @return offset
   */
  private static int offset(final XNode node, final XNode ancestor) {
    int off = 0;
    for(XNode nd = node; !nd.is(ancestor); nd = nd.parent()) {
      for(final GNode sibling : nd.precedingSiblingIter(false)) {
        // comments and processing instructions are no part of the string value
        if(sibling.kind().oneOf(Kind.ELEMENT, Kind.TEXT)) off += sibling.string().length;
      }
    }
    return off;
  }

  /** Data full-text marker. */
  static final class DataFTMarker {
    /** Token. */
    byte[] token;
    /** Marker flag. */
    final boolean mark;

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
      return string(token) + " (" + mark + ')';
    }
  }
}