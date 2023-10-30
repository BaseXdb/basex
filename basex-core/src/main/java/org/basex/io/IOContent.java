package org.basex.io;

import javax.xml.transform.stream.*;

import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a byte array.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class IOContent extends IO {
  /** Content. */
  private final byte[] content;
  /** Encoding (can be {@code null}). */
  private final String encoding;

  /**
   * Constructor.
   * @param content content
   */
  public IOContent(final byte[] content) {
    this(content, "");
  }

  /**
   * Constructor.
   * @param content content
   */
  public IOContent(final String content) {
    this(content, "");
  }

  /**
   * Constructor.
   * @param content content
   * @param path content path
   */
  public IOContent(final String content, final String path) {
    this(Token.token(content), path, Strings.UTF8);
  }

  /**
   * Constructor.
   * @param content content
   * @param path content path
   */
  public IOContent(final byte[] content, final String path) {
    this(content, path, null);
  }

  /**
   * Constructor.
   * @param content content
   * @param path content path
   * @param encoding encoding (can be {@code null})
   */
  public IOContent(final byte[] content, final String path, final String encoding) {
    super(path);
    this.content = content;
    this.encoding = encoding;
    len = content.length;
  }

  @Override
  public byte[] read() {
    return content;
  }

  @Override
  public InputSource inputSource() {
    final InputSource is = new InputSource(inputStream());
    is.setSystemId(pth);
    return is;
  }

  @Override
  public StreamSource streamSource() {
    return new StreamSource(inputStream(), pth);
  }

  @Override
  public ArrayInput inputStream() {
    return new ArrayInput(content);
  }

  @Override
  public String encoding() {
    return encoding;
  }

  @Override
  public String toString() {
    return Token.string(content);
  }
}
