package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.File;

import org.basex.query.QueryContext;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.util.NSGlobal;
import org.basex.query.util.Var;
import org.basex.util.TokenBuilder;

/**
 * Statically available XQuery variables.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum Variable {

  /** File variable. */
  FILEDIRSEP(FILEURI, "directory-separator", Str.get(File.separator)),
  /** File variable. */
  FILEPATHSEP(FILEURI, "path-separator", Str.get(File.pathSeparator)),

  /** XSLT variable. */
  XSLTPROC(XSLTURI, "processor", Str.get(FNXslt.get(true))),
  /** XSLT variable. */
  XSLTVERSION(XSLTURI, "version", Str.get(FNXslt.get(false)));

  /** Variable name. */
  final QNm qname;
  /** Variable value. */
  final Value value;

  /**
   * Constructor.
   * @param uri uri
   * @param name name
   * @param val item value
   */
  private Variable(final byte[] uri, final String name, final Value val) {
    qname = new QNm(token(name), uri);
    value = val;
  }

  /**
   * Initializes all variables.
   * @param ctx query context
   */
  public static void init(final QueryContext ctx) {
    for(final Variable v : values()) {
      ctx.vars.setGlobal(Var.create(ctx, null, v.qname, v.value));
    }
  }

  @Override
  public final String toString() {
    final byte[] pref = NSGlobal.prefix(qname.uri());
    return new TokenBuilder("$").add(pref).add(':').add(
        qname.local()).toString();
  }
}
