package org.basex.build;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.util.Atts;

/**
 * This class provides an interface for building database instances.
 * The specified {@link Parser} sends events to this class whenever nodes
 * are to be added or closed. The builder implementation decides whether
 * the nodes are stored on disk or kept in memory.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Builder extends Progress {
  /** Parser instance. */
  protected final Parser parser;

  /** Number of cached size values. */
  protected int ssize;
  /** Currently stored size value. */
  protected int spos;
  
  /**
   * Constructor.
   * @param parse parser
   */
  protected Builder(final Parser parse) {
    parser = parse;
  }

  /**
   * Builds the database by running the specified parser.
   * @return data database instance
   * @throws IOException I/O exception
   */
  public abstract Data build() throws IOException;
  
  /**
   * Opens a document node.
   * @param value document name
   * @throws IOException I/O exception
   */
  public abstract void startDoc(final byte[] value) throws IOException;

  /**
   * Closes a document node.
   * @throws IOException I/O exception
   */
  public abstract void endDoc() throws IOException;

  /**
   * Adds a new namespace; called by the building instance.
   * @param pref the namespace prefix
   * @param uri namespace uri
   */
  public abstract void startNS(final byte[] pref, final byte[] uri);

  /**
   * Opens a new element node.
   * @param nm tag name
   * @param att attributes
   * @throws IOException I/O exception
   */
  public abstract void startElem(final byte[] nm, final Atts att) throws IOException;

  /**
   * Stores an empty element.
   * @param nm tag name
   * @param att attributes
   * @throws IOException I/O exception
   */
  public abstract void emptyElem(final byte[] nm, final Atts att) throws IOException;

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public abstract void endElem() throws IOException;

  /**
   * Stores a text node.
   * @param value text value
   * @throws IOException I/O exception
   */
  public abstract void text(final byte[] value) throws IOException;

  /**
   * Stores a comment.
   * @param value comment text
   * @throws IOException I/O exception
   */
  public abstract void comment(final byte[] value) throws IOException;

  /**
   * Stores a processing instruction.
   * @param pi processing instruction name and value
   * @throws IOException I/O exception
   */
  public abstract void pi(final byte[] pi) throws IOException;
  
  /**
   * Sets the document encoding.
   * @param enc encoding
   */
  public abstract void encoding(final String enc);

  /**
   * Closes open references.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  // PROGRESS INFORMATION =====================================================

  @Override
  public final String tit() {
    return PROGCREATE;
  }

  @Override
  public final String det() {
    return spos == 0 ? parser.detail() : DBFINISH;
  }

  @Override
  public final double prog() {
    return spos == 0 ? parser.progress() : (double) spos / ssize;
  }

  /**
   * Throws an error message.
   * @param msg message
   * @param ext message extension
   * @throws IOException I/O exception
   */
  protected void error(final String msg, final Object... ext) throws IOException {
    throw new BuildException(msg, ext);
  }
}
