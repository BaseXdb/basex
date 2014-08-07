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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DOTSerializer extends OutputSerializer {
  /** Compact representation. */
  private final boolean compact;

  /** Cached children. */
  private final ArrayList<IntList> children = new ArrayList<>();
  /** Cached attributes. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Cached nodes. */
  private final IntList nodes = new IntList();
  /** Node counter. */
  private int count;

  /**
   * Constructor, defining colors for the dot output.
   * @param os output stream
   * @param compact compact representation
   * @throws IOException I/O exception
   */
  public DOTSerializer(final OutputStream os, final boolean compact) throws IOException {
    super(os, SerializerOptions.get(true));
    this.compact = compact;
    print(HEADER);
  }

  @Override
  public void close() throws IOException {
    indent();
    print(FOOTER);
  }

  @Override
  protected void startOpen(final byte[] name) {
    tb.reset();
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) {
    tb.addExt(DOTATTR, name, value);
  }

  @Override
  protected void finishOpen() throws IOException {
    final byte[] attr = tb.toArray();
    String color = color(elem);
    if(color == null) color = attr.length == 0 ? ELEM1 : ELEM2;
    print(concat(elem, attr), color);
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    final int c = nodes.get(lvl);
    final IntList il = child(lvl);
    final int is = il.size();
    for(int i = 0; i < is; ++i) {
      indent();
      print(Util.info(DOTLINK, c, il.get(i)));
    }
    il.reset();
  }

  @Override
  protected void finishText(final byte[] value) throws IOException {
    print(norm(value), DOTData.TEXT);
  }

  @Override
  protected void finishComment(final byte[] value) throws IOException {
    print(new TokenBuilder(COMM_O).add(norm(value)).add(COMM_C).finish(), COMM);
  }

  @Override
  protected void finishPi(final byte[] name, final byte[] value) throws IOException {
    print(new TokenBuilder(PI_O).add(name).add(SPACE).add(value).add(PI_C).finish(), DOTData.PI);
  }

  @Override
  protected void atomic(final Item it, final boolean iter) throws IOException {
    try {
      print(norm(it.string(null)), ITEM);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Prints a single node.
   * @param value text
   * @param col color
   * @throws IOException I/O exception
   */
  private void print(final byte[] value, final String col) throws IOException {
    String txt = string(chop(value, 60)).replaceAll("\"|\\r|\\n", "'");
    if(compact) txt = txt.replaceAll("\\\\n\\w+:", "\\\\n");
    indent();
    print(Util.info(DOTNODE, count, txt, col));
    nodes.set(lvl, count);
    if(lvl > 0) child(lvl - 1).add(count);
    ++count;
  }

  /**
   * Returns the children from the stack.
   * @param index index
   * @return children
   */
  private IntList child(final int index) {
    while(index >= children.size()) children.add(new IntList());
    return children.get(index);
  }
}
