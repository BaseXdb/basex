package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.ExprInfo;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class allows to output XML results via SAX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  private static final String COLITEM = "33FFFF";
  /** Link entry. */
  private static final String COLTEXT = "9999FF";
  /** Link entry. */
  private static final String COLCOMM = "FFFF66";
  /** Link entry. */
  private static final String COLPI = "FF6666";
  
  /** Output stream. */
  public final PrintOutput out;
  /** Cached children. */
  private IntList[] children = new IntList[IO.MAXHEIGHT];
  /** Current color. */
  private String color;
  /** Cached nodes. */
  private IntList nodes = new IntList();
  /** Node counter. */
  private int count;

  /**
   * Constructor, defining colors for the dot output.
   * @param o output stream
   */
  public DOTSerializer(final PrintOutput o) {
    for(int i = 0; i < IO.MAXHEIGHT; i++) children[i] = new IntList();
    out = o;
  }

  /** Caches a tag name. */
  byte[] tag;
  /** Caches attributes. */
  TokenBuilder tb = new TokenBuilder();

  @Override
  public void open(final int s) throws IOException {
    out.println("digraph BaseXAlgebra {");
    out.println("node[shape=box,style=filled,width=0.1,height=0.3];");
    out.println("node[fontsize=12,fontname=SansSerif];");
    if(s != 1) openElement(RESULTS);
  }

  @Override
  public void close(final int s) throws IOException {
    if(s != 1) closeElement();
    out.println("}");
  }

  @Override
  public void openResult() throws IOException {
    openElement(RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    closeElement();
  }
  
  @Override
  protected void start(final byte[] t) {
    tag = t;
    tb.reset();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) {
    tb.add("\\n");
    tb.add(n);
    tb.add("='");
    tb.add(v);
    tb.add("'");
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
    print(Token.concat(tag, attr), color);
  }

  @Override
  public void close(final byte[] t) throws IOException {
    final int c = nodes.list[tags.size];
    final IntList il = children[tags.size];
    for(int i = 0; i < il.size; i++) {
      out.println(BaseX.info(LINK, c, il.list[i]));
    }
    il.reset();
  }

  @Override
  public void text(final byte[] t) throws IOException {
    print(Token.norm(t), COLTEXT);
  }

  @Override
  public void comment(final byte[] t) throws IOException {
    print(Token.concat(COM1, Token.norm(t), COM2), COLCOMM);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    print(Token.concat(PI1, n, Token.SPACE, v, PI2), COLPI);
  }

  @Override
  public void item(final byte[] t) throws IOException {
    print(Token.norm(t), COLITEM);
  }

  /**
   * Prints a single node.
   * @param t text
   * @param col color
   * @throws IOException exception
   */
  protected void print(final byte[] t, final String col) throws IOException {
    final byte[] text = t.length > 60 ? Token.concat(
        Token.substring(t, 0, 60), Token.token("...")) : t;
    out.println(BaseX.info(NODE, count, text, col));
    nodes.set(count, tags.size);
    if(tags.size > 0) children[tags.size - 1].add(count);
    color = null;
    count++;
  }

  @Override
  protected byte[] name(final ExprInfo expr) {
    color = expr.color();
    return expr.name();
  }
}
