package net.xqj.basex.local;

import javax.xml.xquery.*;

import net.xqj.basex.*;

import org.junit.jupiter.api.*;

/**
 * Base class for all XQJ local tests.
 *
 * @author Charles Foster
 */
public abstract class XQJBaseTest {
  /** Data source. */
  BaseXXQDataSource xqds;
  /** Connection. */
  XQConnection xqc;

  /**
   * Initializes a test.
   * @throws XQException query exception
   */
  @SuppressWarnings("unused")
  @BeforeEach public void setUp() throws XQException {
    xqds = new BaseXXQDataSource();
    xqc = xqds.getConnection();
  }

  /**
   * Finalizes a test.
   * @throws XQException xquery exception
   */
  @AfterEach public void tearDown() throws XQException {
    xqc.close();
  }

  /**
   * Creates a document-node(element()) with some text content.
   * @param content the text() content the element() will contain
   * @return a XQItem representing a document-node(element()) item
   * @throws XQException query exception
   */
  final XQItem createDocument(final String content) throws XQException {
    return
      xqc.createItemFromDocument(
        content,
        null,
        xqc.createDocumentElementType(
          xqc.createElementType(null, XQItemType.XQBASETYPE_ANYTYPE)
        )
      );
  }

  /**
   * Checks if a document is available.
   * @param uri URI
   * @return result of check
   * @throws XQException query exception
   */
  boolean docAvailable(final String uri) throws XQException {
    final XQResultSequence rs =
      xqc.createExpression().executeQuery(
        "fn:doc-available('" + uri + "')"
      );
    rs.next();
    return rs.getBoolean();
  }

  /**
   * Sets and returns options.
   * @param strategy insert strategy
   * @return options
   */
  static BaseXXQInsertOptions options(final int strategy) {
    final BaseXXQInsertOptions options = new BaseXXQInsertOptions();
    options.setInsertStrategy(strategy);
    return options;
  }
}
