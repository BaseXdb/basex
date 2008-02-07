package org.basex.core.proc;

import static org.basex.Text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.Commands;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.query.xpath.XPathProcessor;

/**
 * Evaluates the 'xpath' command. Evaluates an XPath query.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class XPath extends XQuery {
  /** XPath query pattern. */
  private static final Pattern XPATH =
    Pattern.compile("doc\\((\\\"|')(.+?)\\1\\)(.*)");

  @Override
  protected boolean exec() {
    String query = cmd.args();

    if(query.startsWith("doc")) {
      // extract doc(...) prefix
      final Matcher mat = XPATH.matcher(query);
      if(mat.find()) {
        final String doc = mat.group(2);
        final String path = mat.group(3);
        if(!exec(Commands.CHECK, doc)) return false;
        info(NL);

        final boolean dbl = path.startsWith("//") || path.length() == 0;
        query = path.substring(dbl ? 0 : 1);
      }
    }
    return query(XPathProcessor.class, query);
  }

  @Override
  protected void out(final PrintOutput o) throws Exception {
    final Data data = context.data();
    out(o, data != null ? data.meta.chop : false);
  }
}
