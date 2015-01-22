package org.basex.io.serial.dot;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.dot.DOTData.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serializes items in the DOT syntax.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DOTSerializer extends OutputSerializer {
  /** Compact representation. */
  private final boolean compact;

  /** Cached children. */
  private final ArrayList<IntList> children = new ArrayList<>(1);
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
    super(PrintOutput.get(os), SerializerOptions.get(true));
    this.compact = compact;
    out.print(HEADER);
  }

  @Override
  public void close() throws IOException {
    indent();
    out.print(FOOTER);
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
    final int c = nodes.get(level);
    final IntList il = child(level);
    final int is = il.size();
    for(int i = 0; i < is; ++i) {
      indent();
      out.print(Util.info(DOTLINK, c, il.get(i)));
    }
    il.reset();
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    print(normalize(value), DOTData.TEXT);
  }

  @Override
  protected void comment(final byte[] value) throws IOException {
    print(new TokenBuilder(COMM_O).add(normalize(value)).add(COMM_C).finish(), COMM);
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    print(new TokenBuilder(PI_O).add(name).add(SPACE).add(value).add(PI_C).finish(), DOTData.PI);
  }

  @Override
  protected void atomic(final Item it, final boolean iter) throws IOException {
    try {
      print(normalize(it.string(null)), ITEM);
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
    out.print(Util.info(DOTNODE, count, txt, col));
    nodes.set(level, count);
    if(level > 0) child(level - 1).add(count);
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
