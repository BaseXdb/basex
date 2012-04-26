package org.basex.io.serial;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * A serializer that pipes the events directly through to a builder.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class BuilderSerializer extends Serializer {
  /** Attribute cache. */
  private final Atts att = new Atts();
  /** The builder. */
  private final Builder build;

  /**
   * Constructor taking a Builder.
   * @param b builder to be used
   */
  public BuilderSerializer(final Builder b) {
    build = b;
  }

  @Override
  public final void finishText(final byte[] b) throws IOException {
    build.text(b);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
  }

  @Override
  public final void finishPi(final byte[] n, final byte[] v) throws IOException {
    build.pi(concat(n, SPACE, v));
  }

  @Override
  public final void finishAtomic(final Item b) throws IOException {
    Util.notexpected();
  }

  @Override
  protected final void finishOpen() throws IOException {
    build.startElem(tag, att);
    att.reset();
  }

  @Override
  protected void finishEmpty() throws IOException {
    build.emptyElem(tag, att);
    att.reset();
  }

  @Override
  protected void finishClose() throws IOException {
    build.endElem();
  }

  @Override
  public final void finishComment(final byte[] b) throws IOException {
    build.comment(b);
  }

  @Override
  public final void attribute(final byte[] n, final byte[] v) throws IOException {
    if(startsWith(n, XMLNS)) {
      if(n.length == 5) {
        build.startNS(EMPTY, v);
      } else if(n[5] == ':') {
        build.startNS(substring(n, 6), v);
      } else {
        att.add(n, v);
      }
    } else {
      att.add(n, v);
    }
  }

  @Override
  public void openDoc(final byte[] name) throws IOException {
    build.startDoc(name);
  }

  @Override
  public final void closeDoc() throws IOException {
    build.endDoc();
  }
}
