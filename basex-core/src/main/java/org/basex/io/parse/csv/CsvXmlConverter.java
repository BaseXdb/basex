package org.basex.io.parse.csv;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML events, which are sent to an {@link XmlHandler}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class CsvXmlConverter extends CsvConverter {
  /** No namespace declarations. */
  static final Atts NO_NSP = new Atts();

  /** Attributes of the next element. */
  final Atts atts = new Atts();
  /** Target of the XML events. */
  XmlHandler handler;
  /** Current line. */
  int line;

  /** External handler (can be {@code null}). */
  private final XmlHandler external;

  /**
   * Constructor.
   * @param copts CSV options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  CsvXmlConverter(final CsvParserOptions copts, final XmlHandler handler) {
    super(copts);
    external = handler;
  }

  @Override
  protected void init(final String uri) throws IOException {
    handler = external != null ? external : new NodeHandler(uri, false);
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws IOException {
    return handler instanceof final NodeHandler ns ? ns.finish() : Empty.VALUE;
  }

  @Override
  public String detailedInfo() {
    return Util.info(LINE_X, line);
  }

  @Override
  public double progressInfo() {
    return ti == null ? 0 : (double) ti.size() / ti.length();
  }
}
