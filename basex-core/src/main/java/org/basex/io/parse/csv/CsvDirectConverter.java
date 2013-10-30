package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CsvDirectConverter extends CsvConverter {
  /** CSV token. */
  private static final byte[] CSV = token("csv");
  /** CSV token. */
  private static final byte[] RECORD = token("record");
  /** CSV token. */
  private static final byte[] ENTRY = token("entry");
  /** CSV token. */
  private static final byte[] NAME = token("name");

  /** Headers. */
  private final TokenList headers = new TokenList();
  /** Attributes format. */
  private final boolean atts;
  /** Lax QName conversion. */
  private final boolean lax;

  /** Root node. */
  private final FElem root = new FElem(CSV);
  /** Record. */
  private FElem record;

  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param opts CSV options
   */
  public CsvDirectConverter(final CsvParserOptions opts) {
    super(opts);
    lax = opts.get(CsvOptions.LAX);
    atts = opts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
  }

  @Override
  public void record() {
    record = new FElem(RECORD);
    root.add(record);
    col = 0;
  }

  @Override
  public void header(final byte[] value) {
    headers.add(atts ? value : XMLToken.encode(value, lax));
  }

  @Override
  public void entry(final byte[] entry) {
    final byte[] name = headers.get(col++);
    final FElem e;
    if(atts) {
      e = new FElem(ENTRY);
      if(name != null) e.add(NAME, name);
    } else {
      e = new FElem(name == null ? ENTRY : name);
    }
    record.add(e.add(entry));
  }

  @Override
  public FDoc finish() {
    return new FDoc().add(root);
  }
}
