package org.basex.io.serial;

import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.build.Builder;
import org.basex.data.FTPos;
import org.basex.util.Atts;

/**
 * A serializer that pipes the events directly through to a builder.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class BuilderSerializer extends Serializer {
  /** Current tag name. */
  private byte[] tag;
  /** True while being in an open tag. */
  private boolean open;
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
  public final void text(final byte[] b, final FTPos ftp) throws IOException {
    text(b);
  }

  @Override
  public final void text(final byte[] b) throws IOException {
    finish();
    build.text(b);
  }

  @Override
  protected void start(final byte[] t) throws IOException {
    tag = t;
    open = true;
  }

  @Override
  public final void pi(final byte[] n, final byte[] v) throws IOException {
    build.pi(concat(n, new byte[]{ ' ' }, v));
  }

  @Override
  public final void item(final byte[] b) throws IOException {
    text(b);
  }

  @Override
  protected final void finish() throws IOException {
    if(open) {
      build.startElem(tag, att);
      att.reset();
      open = false;
    }
  }

  @Override
  protected final void empty() throws IOException {
    if(open) {
      build.emptyElem(tag, att);
      open = false;
    } else {
      close(tag);
    }
    tag = null;
    att.reset();
  }

  @Override
  public final void comment(final byte[] b) throws IOException {
    build.comment(b);
  }

  @Override
  protected final void close(final byte[] t) throws IOException {
    build.endElem(t);
    tag = null;
  }

  @Override
  public final void attribute(final byte[] n, final byte[] v)
      throws IOException {
    if(startsWith(n, XMLNS)) {
      if(n.length == 5) {
        build.startNS(EMPTY, v);
      } else if(n[5] == ':') {
        build.startNS(substring(n, 6), v);
      } else att.add(n, v);
    } else {
      att.add(n, v);
    }
  }

  @Override
  protected void openDoc(final byte[] name) throws IOException {
    build.startDoc(name);
  }

  @Override
  protected final void closeDoc() throws IOException {
    build.endDoc();
  }

  @Override
  public final void openResult() throws IOException {
    // ignore this
  }

  @Override
  public final void closeResult() throws IOException {
    // ignore this
  }

  @Override
  protected final void cls() throws IOException {
    // ignore this
  }
}
