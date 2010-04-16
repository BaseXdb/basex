package org.basex.examples.tagsoup;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.transform.sax.SAXSource;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Test class for the use of the TagSoup HTML parser with BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leo Woerteler
 */
public final class TagSoupExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Private Constructor. */
  private TagSoupExample() { }

  /**
   * Main class.
   * @param args (ignored) command-line arguments
   * @throws Exception exceptions
   */
  public static void main(final String[] args) throws Exception {
    // URL to be parsed
    String url = "http://www.spiegel.de";
    // Query to be evaluated
    String query = "(//div[@class eq 'spTopThema']/h3)[1]//text()";

    useBaseXString(url, "Spiegel", query);
    System.out.println();
    useBaseXSax(url, query);
    System.out.println();
  }

  /**
   * Creates a database instance from a serialized XML doc obtained from
   * TagSoup. This is the two phase approach.
   * @param addr URL of the web page
   * @param name name of the database
   * @param query query to be evaluated
   * @throws Exception toy example...
   */
  public static void useBaseXString(final String addr, final String name,
      final String query) throws Exception {

    XMLReader parser = getParser();
    InputSource is = getInputSource(addr, null);
    StringWriter sw = new StringWriter();
    parser.setContentHandler(new XMLWriter(sw));
    parser.parse(is);

    new CreateDB(sw.toString(), name).execute(CONTEXT);
    new XQuery(query).execute(CONTEXT, OUT);
    new DropDB(name).execute(CONTEXT);
  }

  /**
   * Creates a memory-based BaseX DB using the SAX events from TagSoup. This is
   * the direct approach.
   * @param addr address of the web page
   * @param query query to be evaluated
   * @throws Exception exception
   */
  public static void useBaseXSax(final String addr, final String query)
      throws Exception {

    // create database instance
    XMLReader parser = getParser();
    InputSource is = getInputSource(addr, null);
    CONTEXT.openDB(CreateDB.xml(new SAXSource(parser, is), CONTEXT.prop));

    // iterate through and serialize result
    QueryProcessor qp = new QueryProcessor(query, CONTEXT);
    Iter iter = qp.iter();
    XMLSerializer serializer = new XMLSerializer(OUT);
    Item item;
    while((item = iter.next()) != null) {
      item.serialize(serializer);
    }
  }

  /**
   * Creates a new TagSoup parser that isn't namespace-aware.
   * @return the parser
   * @throws Exception toy example...
   */
  private static XMLReader getParser() throws Exception {
    XMLReader parser = new Parser();
    parser.setFeature("http://xml.org/sax/features/namespaces", false);
    return parser;
  }

  /**
   * Creates a new InputSource for the address, using the given encoding or
   * guessing it, if it is <tt>null</tt>.
   * @param addr URL of the web page
   * @param encoding encoding of the web page, or null
   * @return the new InputSource for <tt>addr</tt>
   * @throws IOException toy example...
   */
  private static InputSource getInputSource(final String addr,
      final String encoding) throws IOException {
    InputSource is;
    if(encoding != null) {
      is = new InputSource(addr);
      is.setEncoding(encoding);
    } else { // let's find out
      URL url = new URL(addr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      is = new InputSource(conn.getInputStream());
      String enc = conn.getContentType();
      if(enc != null) {
        String[] parts = enc.split(";\\s*charset\\s*=");
        if(parts.length > 1) {
          enc = parts[1].trim();
          if(!enc.isEmpty()) is.setEncoding(enc);
        }
      }
    }
    return is;
  }
}
