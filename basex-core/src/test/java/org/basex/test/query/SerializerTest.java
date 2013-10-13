package org.basex.test.query;

import org.basex.query.util.*;
import org.junit.*;

/**
 * This class tests serializer.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class SerializerTest extends AdvancedQueryTest {
  /** Test: method=xml. */
  @Test
  public void xml() {
    final String option = "declare option output:method 'xml';";
    query(option + "<html/>", "<html/>");
  }

  /** Test: method=xhtml. */
  @Test
  public void xhtml() {
    final String option = "declare option output:method 'xhtml';";
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) query(option + "<" + e + "/>", "<" + e + " />");
  }

  /** Test: method=html. */
  @Test
  public void html() {
    final String option = "declare option output:method 'html';";
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) query(option + "<" + e + "/>", "<" + e + ">");

    query(option + "<html><script>&lt;</script></html>",
        "<html><script><</script></html>");
    query(option + "<html><style>{ serialize(<a/>) }</style></html>",
        "<html><style><a/></style></html>");
    query(option + "<a b='&lt;'/>", "<a b=\"<\"></a>");
    error(option + "<a>&#x90;</a>", Err.SERILL);

    query(option + "<?x y?>", "<?x y>");
    error(option + "<?x > ?>", Err.SERPI);
  }

  /** Test: method=html, version=5.0. */
  @Test
  public void version50() {
    final String option = "declare option output:method 'html';" +
        "declare option output:version '5.0';";
    query(option + "<html/>", "<!DOCTYPE html><html></html>");
    final String[] empties = { "area", "base", "br", "col", "command", "embed", "hr",
        "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr" };
    for(final String e : empties) {
      query(option + "<" + e + "/>", "<!DOCTYPE html><" + e + ">");
    }
    query(option + "<a>&#x90;</a>", "<!DOCTYPE html><a>&#x90;</a>");
  }

  /** Test: method=html, html-version=5.0. */
  @Test
  public void htmlVersion50() {
    final String option = "declare option output:method 'html';" +
        "declare option output:html-version '5.0';";
    query(option + "<html/>", "<!DOCTYPE html><html></html>");
  }

  /** Test: method=text. */
  @Test
  public void text() {
    final String option = "declare option output:method 'text';";
    query(option + "1,2", "1 2");
    query(option + "<a>1</a>", "1");
    query(option + "1,<a>2</a>,3", "123");
  }

  /** Test: item-separator. */
  @Test
  public void itemSeparator() {
    query("declare option output:item-separator '-'; 1,2", "1-2");
    query("declare option output:item-separator ''; 1,2", "12");
    query("declare option output:item-separator 'ABC'; 1 to 3", "1ABC2ABC3");
  }
}
