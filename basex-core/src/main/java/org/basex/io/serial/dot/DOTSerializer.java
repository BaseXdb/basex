package org.basex.io.serial.dot;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.dot.DOTData.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serializes data in the DOT syntax.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DOTSerializer extends OutputSerializer {
  /** Compact representation. */
  private final boolean compact;

  /** Cached children. */
  private final ArrayList<IntList> children = new ArrayList<IntList>();
  /** Cached attributes. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Cached nodes. */
  private final IntList nodes = new IntList();
  /** Node counter. */
  private int count;

  /**
   * Constructor, defining colors for the dot output.
   * @param os output stream
   * @param c compact representation
   * @throws IOException I/O exception
   */
  public DOTSerializer(final OutputStream os, final boolean c) throws IOException {
    super(os, OPTIONS);
    compact = c;
    print(HEADER);
  }

  @Override
  public void close() throws IOException {
    indent();
    print(FOOTER);
  }

  @Override
  protected void startOpen(final byte[] t) {
    tb.reset();
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) {
    tb.addExt(DOTATTR, n, v);
  }

  @Override
  protected void finishOpen() throws IOException {
    final byte[] attr = tb.finish();
    String color = color(tag);
    if(color == null) color = attr.length == 0 ? ELEM1 : ELEM2;
    print(concat(tag, attr), color);
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final int c = nodes.get(level);
    final IntList il = child(level);
    final int is = il.size();
    for(int i = 0; i < is; ++i) {
      indent();
      print(Util.info(DOTLINK, c, il.get(i)));
    }
    il.reset();
  }

  @Override
  protected void finishText(final byte[] t) throws IOException {
    print(norm(t), DOTData.TEXT);
  }

  @Override
  protected void finishComment(final byte[] t) throws IOException {
    print(new TokenBuilder(COMM_O).add(norm(t)).add(COMM_C).finish(), COMM);
  }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) throws IOException {
    print(new TokenBuilder(PI_O).add(n).add(SPACE).add(v).add(PI_C).finish(), DOTData.PI);
  }

  @Override
  protected void atomic(final Item it) throws IOException {
    try {
      print(norm(it.string(null)), ITEM);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Prints a single node.
   * @param t text
   * @param col color
   * @throws IOException I/O exception
   */
  private void print(final byte[] t, final String col) throws IOException {
    String txt = string(chop(t, 60)).replaceAll("\"|\\r|\\n", "'");
    if(compact) txt = txt.replaceAll("\\\\n\\w+:", "\\\\n");
    indent();
    print(Util.info(DOTNODE, count, txt, col));
    nodes.set(level, count);
    if(level > 0) child(level - 1).add(count);
    ++count;
  }

  /**
   * Returns the children from the stack.
   * @param i index
   * @return children
   */
  private IntList child(final int i) {
    while(i >= children.size()) children.add(new IntList());
    return children.get(i);
  }
}
