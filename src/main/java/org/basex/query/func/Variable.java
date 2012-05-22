package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Statically available XQuery variables.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum Variable {

  /** File variable. */
  _FILE_DIRECTORY_SEPARATOR(FILEURI, "directory-separator", Str.get(File.separator)),
  /** File variable. */
  _FILE_PATH_SEPARATOR(FILEURI, "path-separator", Str.get(File.pathSeparator)),

  /** XSLT variable. */
  _XSLT_PROCESSOR(XSLTURI, "processor", Str.get(FNXslt.get(true))),
  /** XSLT variable. */
  _XSLT_VERSION(XSLTURI, "version", Str.get(FNXslt.get(false))),

  /** Util variable. */
  _UTIL_NL(UTILURI, "nl", Str.get("\n")),
  /** Util variable. */
  _UTIL_TAB(UTILURI, "tab", Str.get("\t"));

  /** Variable name. */
  private final QNm qname;
  /** Variable value. */
  private final Value value;

  /**
   * Constructor.
   * @param uri uri
   * @param name name
   * @param val item value
   */
  Variable(final byte[] uri, final String name, final Value val) {
    qname = new QNm(name, uri);
    value = val;
  }

  /**
   * Initializes all variables.
   * @param ctx query context
   */
  public static void init(final QueryContext ctx) {
    for(final Variable v : values()) {
      ctx.vars.updateGlobal(Var.create(ctx, null, v.qname, v.value, null));
    }
  }

  @Override
  public final String toString() {
    final byte[] pref = NSGlobal.prefix(qname.uri());
    return new TokenBuilder("$").add(pref).add(':').add(
        qname.local()).toString();
  }
}
