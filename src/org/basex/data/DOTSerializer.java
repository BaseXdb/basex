package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.ExprInfo;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class allows to output XML results via SAX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DOTSerializer extends Serializer {
  /** Compact flag. */
  private static final boolean COMPACT = false;
  /** Node entry. */
  private static final String NODE = "node% [label=\"%\" color=\"#%\"];";
  /** Link entry. */
  private static final String LINK = "node% -> node%;";
  /** Link entry. */
  private static final String COLELEM1 = "C0C0C0";
  /** Link entry. */
  private static final String COLELEM2 = "E0E0E0";
  /** Link entry. */
  private static final String COLITEM = "66CCCC";
  /** Link entry. */
  private static final String COLTEXT = "9999FF";
  /** Link entry. */
  private static final String COLCOMM = "FFFF66";
  /** Link entry. */
  private static final String COLPI = "FF6666";

  /** Output stream. */
  private final PrintOutput out;
  /** Current level. */
  private int level;
  /** Cached children. */
  private final IntList[] children = new IntList[IO.MAXHEIGHT];
  /** Current color. */
  private String color;
  /** Cached nodes. */
  private final IntList nodes = new IntList();
  /** Node counter. */
  private int count;
  /** Cached tag name. */
  private byte[] tag;
  /** Cached attributes. */
  private final TokenBuilder tb = new TokenBuilder();

  /**
   * Constructor, defining colors for the dot output.
   * @param o output stream
   * @throws IOException I/O exception
   */
  public DOTSerializer(final PrintOutput o) throws IOException {
    for(int i = 0; i < IO.MAXHEIGHT; i++) children[i] = new IntList();
    out = o;

    out.println("digraph BaseXAlgebra {");
    out.println("node[shape=box,style=filled,width=0,height=0];");
    out.println("node[fontsize=14,fontname=Tahoma];");
  }

  @Override
  public void openResult() { }

  @Override
  public void closeResult() { }

  @Override
  protected void start(final byte[] t) {
    tag = t;
    tb.reset();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) {
    tb.add(!COMPACT || tb.size() == 0 ? "\\n" : ", ");
    tb.add(n);
    tb.add(":");
    tb.add(v);
  }

  @Override
  public void empty() throws IOException {
    finish();
    close(tag);
  }

  @Override
  public void finish() throws IOException {
    final byte[] attr = tb.finish();
    if(color == null) color = attr.length == 0 ? COLELEM1 : COLELEM2;
    print(concat(tag, attr), color);
    level++;
  }

  @Override
  public void close(final byte[] t) throws IOException {
    if(--level < 0) return;
    final int c = nodes.get(level);
    final IntList il = children[level];
    for(int i = 0, is = il.size(); i < is; i++) {
      out.println(BaseX.info(LINK, c, il.get(i)));
    }
    color = null;
    il.reset();
  }

  @Override
  public void text(final byte[] t) throws IOException {
    finishElement();
    print(norm(t), COLTEXT);
  }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException {
    text(b);
  }

  @Override
  public void comment(final byte[] t) throws IOException {
    finishElement();
    print(concat(COM1, norm(t), COM2), COLCOMM);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    print(concat(PI1, n, SPACE, v, PI2), COLPI);
  }

  @Override
  public void item(final byte[] t) throws IOException {
    finishElement();
    print(norm(t), COLITEM);
  }

  @Override
  public void cls() throws IOException {
    out.println("}");
  }

  /**
   * Prints a single node.
   * @param t text
   * @param col color
   * @throws IOException I/O exception
   */
  private void print(final byte[] t, final String col) throws IOException {
    final byte[] text = t.length > 60 ? concat(
        substring(t, 0, 60), token("...")) : t;
    out.println(BaseX.info(NODE, count, text, col));
    nodes.set(count, level);
    if(level > 0) children[level - 1].add(count);
    count++;
  }

  @Override
  protected byte[] name(final ExprInfo expr) throws IOException {
    finishElement();
    color = expr.color();
    return token(expr.name());
  }
}
