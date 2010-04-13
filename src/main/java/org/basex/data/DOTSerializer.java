package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class serializes trees in the DOT syntax.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DOTSerializer extends Serializer {
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

  /** Compact representation. */
  private final boolean compact;
  /** Output stream. */
  private final PrintOutput out;

  /** Cached children. */
  private final IntList[] children = new IntList[IO.MAXHEIGHT];
  /** Cached attributes. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Cached nodes. */
  private final IntList nodes = new IntList();
  /** Cached tag name. */
  private byte[] tag;

  /** Current color. */
  private String color;
  /** Current level. */
  private int level;
  /** Node counter. */
  private int count;

  /**
   * Constructor, defining colors for the dot output.
   * @param o output stream
   * @param c compact representation
   * @throws IOException I/O exception
   */
  public DOTSerializer(final PrintOutput o, final boolean c)
      throws IOException {
    for(int i = 0; i < IO.MAXHEIGHT; i++) children[i] = new IntList();
    out = o;
    compact = c;

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
    tb.add("\\n");
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
    final int is = il.size();
    for(int i = 0; i < is; i++) out.println(Main.info(LINK, c, il.get(i)));
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
    String txt = t.length > 60 ? string(t).substring(0, 60) + "..." : string(t);
    if(compact) {
      while(txt.matches(".*[A-Z][a-z]+[A-Z].*")) {
        txt = txt.replaceAll("([A-Z])[a-z]+([A-Z])", "$1$2");
      }
      txt = txt.replaceAll("\\\\n\\w+:", "\\\\n");
    }
    out.println(Main.info(NODE, count, txt, col));
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
