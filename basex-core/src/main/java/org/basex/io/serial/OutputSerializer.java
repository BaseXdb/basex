package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.out.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** Output stream. */
  protected final PrintOutput out;
  /** Item separator. */
  protected byte[] itemsep;

  /** Newline token. */
  private final byte[] nl;
  /** Number of spaces to indent. */
  private final int indents;
  /** Tabular character. */
  private final char tab;

  /**
   * Constructor.
   * @param out print output
   * @param sopts serializer options
   */
  protected OutputSerializer(final PrintOutput out, final SerializerOptions sopts) {
    this.out = out;

    // project-specific options
    indents = sopts.get(INDENTS);
    tab = sopts.yes(TABULATOR) ? '\t' : ' ';
    nl = token(sopts.get(NEWLINE).newline());
  }

  @Override
  public final boolean finished() {
    return out.finished();
  }

  @Override
  public void close() throws IOException {
    out.flush();
  }

  /**
   * Indents the next text.
   * @throws IOException I/O exception
   */
  protected void indent() throws IOException {
    if(indent) {
      out.print(nl);
      final int ls = level * indents;
      for(int l = 0; l < ls; l++) out.print(tab);
    }
  }

  /**
   * Prints a newline.
   * @throws IOException I/O exception
   */
  protected final void newline() throws IOException {
    out.print(nl);
  }

  /**
   * Sets the item separator.
   * @param sopts serialization options
   * @param def default separator
   */
  protected final void itemsep(final SerializerOptions sopts, final byte[] def) {
    itemsep = sopts.contains(ITEM_SEPARATOR) ? token(sopts.get(ITEM_SEPARATOR).replace("\\n", "\n").
      replace("\\r", "\r").replace("\\t", "\t")) : def;
  }
}
