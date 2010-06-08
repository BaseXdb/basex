package org.basex.examples.create;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.BufferInput;
import org.basex.util.TokenBuilder;

/**
 * This class parses files in the CSV format
 * and sends events to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CSVParser extends Parser {
  /** CSV root element. */
  private static final byte[] CSV = token("csv");
  /** CSV record element. */
  private static final byte[] RECORD = token("record");
  /** CSV field element. */
  private static final byte[] FIELD = token("field");
  /** CSV row attribute. */
  private static final byte[] ROW = token("row");
  /** CSV column attribute. */
  private static final byte[] COLUMN = token("col");

  /** Separator. */
  private final char separator;
  /** Encoding. */
  private final String encoding;

  /**
   * Constructor.
   * @param path file path
   */
  public CSVParser(final String path) {
    this(path, ',');
  }

  /**
   * Constructor, specifying a field separator.
   * @param path file path
   * @param sep separator
   */
  public CSVParser(final String path, final char sep) {
    this(path, sep, UTF8);
  }

  /**
   * Constructor, specifying an encoding.
   * @param path file path
   * @param enc encoding
   */
  public CSVParser(final String path, final String enc) {
    this(path, ',', enc);
  }

  /**
   * Constructor, specifying a field separator and an encoding.
   * @param path file path
   * @param sep separator
   * @param enc encoding
   */
  public CSVParser(final String path, final char sep, final String enc) {
    super(path);
    separator = sep;
    encoding = enc;
  }
  
  @Override
  public void parse(final Builder b) throws IOException {
    b.startDoc(token(file.name()));
    b.startElem(CSV, atts);

    boolean quoted = false;
    boolean nl = true;
    final TokenBuilder tb = new TokenBuilder();
    
    final BufferInput bi = new BufferInput(file.path());
    bi.encoding(encoding);

    int r = 0;
    int c = 0;
    int ch = 0;
    while(true) {
      if(ch == 0) ch = bi.readChar();
      if(ch == 0) break;

      if(quoted) {
        if(ch == '"') {
          ch = bi.readChar();
          if(ch != '"') {
            quoted = false;
            continue;
          }
        }
        if(ch != 0x0D) tb.addUTF(ch);
      } else {
        // separator
        if(ch == separator) {
          if(nl) {
            record(r++, b);
            nl = false;
          }
          field(tb, c++, b);
        } else if(ch == 0x0A) {
          field(tb, c++, b);
          b.endElem(RECORD);
          c = 0;
          nl = true;
        } else if(ch == '"') {
          quoted = true;
        } else if(ch != 0x0D) {
          tb.addUTF(ch);
        }
      }
      ch = 0;
    }
    bi.close();

    if(!nl) b.endElem(RECORD);
    b.endElem(CSV);
    b.endDoc();
    b.meta.deepfs = true;
  }
  
  /**
   * Adds a record to the database.
   * @param r row
   * @param b builder instance
   * @throws IOException I/O exception
   */
  private void record(final int r, final Builder b) throws IOException {
    atts.reset();
    atts.add(ROW, token(r));
    b.startElem(RECORD, atts);
  }
  
  /**
   * Adds a field to the database.
   * @param tb token builder
   * @param c column
   * @param b builder instance
   * @throws IOException I/O exception
   */
  private void field(final TokenBuilder tb, final int c, final Builder b)
      throws IOException {
    atts.reset();
    atts.add(COLUMN, token(c));
    b.startElem(FIELD, atts);
    b.text(tb);
    b.endElem(FIELD);
    tb.reset();
  }
}
