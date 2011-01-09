package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.data.DOTData.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class serializes trees in the DOT syntax.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class DOTSerializer extends Serializer {
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
    for(int i = 0; i < IO.MAXHEIGHT; ++i) children[i] = new IntList();
    out = o;
    compact = c;
    out.println(HEADER);
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
    tb.addExt(DOTATTR, DOTData.name(string(n)), v);
  }

  @Override
  public void empty() throws IOException {
    finish();
    close(tag);
  }

  @Override
  public void finish() throws IOException {
    final byte[] attr = tb.finish();
    if(color == null) color = DOTData.color(string(tag));
    if(color == null) color = attr.length == 0 ? DOTData.ELEM1 : DOTData.ELEM2;
    print(concat(tag, attr), color);
    ++level;
  }

  @Override
  public void close(final byte[] t) throws IOException {
    if(--level < 0) return;
    final int c = nodes.get(level);
    final IntList il = children[level];
    final int is = il.size();
    for(int i = 0; i < is; ++i) out.println(Util.info(DOTLINK, c, il.get(i)));
    color = null;
    il.reset();
  }

  @Override
  public void text(final byte[] t) throws IOException {
    finishElement();
    print(norm(t), DOTData.TEXT);
  }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException {
    text(b);
  }

  @Override
  public void comment(final byte[] t) throws IOException {
    finishElement();
    print(new TokenBuilder(COM1).add(norm(t)).add(COM2).finish(), DOTData.COMM);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    print(new TokenBuilder(PI1).add(n).add(SPACE).add(v).add(PI2).finish(),
        DOTData.PI);
  }

  @Override
  public void item(final byte[] t) throws IOException {
    finishElement();
    print(norm(t), DOTData.ITEM);
  }

  @Override
  public void cls() throws IOException {
    out.println(FOOTER);
  }

  /**
   * Prints a single node.
   * @param t text
   * @param col color
   * @throws IOException I/O exception
   */
  private void print(final byte[] t, final String col) throws IOException {
    String txt = t.length > 60 ? string(t).substring(0, 60) + "..." : string(t);
    txt = txt.replaceAll("\"|\\r|\\n", "'");

    if(compact) {
      /*while(txt.matches(".*[A-Z][a-z]+[A-Z].*")) {
        txt = txt.replaceAll("([A-Z])[a-z]+([A-Z])", "$1$2");
      }*/
      txt = txt.replaceAll("\\\\n\\w+:", "\\\\n");
    }
    out.println(Util.info(DOTNODE, count, txt, col));
    nodes.set(count, level);
    if(level > 0) children[level - 1].add(count);
    ++count;
  }

  @Override
  protected byte[] name(final ExprInfo expr) {
    color = DOTData.color(expr);
    return token(DOTData.name(expr));
  }
}
