package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.File;
import org.basex.query.QueryContext;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.util.Var;

/**
 * XQuery variables specified in modules.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum VarDef {

  /* FNFile variables. */

  /** XQuery function */
  FILEDIRSEP(FILEURI, "file:directory-separator", Str.get(File.separator)),
  /** XQuery function */
  FILEPATHSEP(FILEURI, "file:path-separator", Str.get(File.pathSeparator));

  /** Function uri. */
  final byte[] uri;
  /** Variable name. */
  final byte[] name;
  /** Variable value. */
  final Value val;

  /**
   * Constructor.
   * @param ur uri
   * @param n name
   * @param v item value
   */
  private VarDef(final byte[] ur, final String n, final Value v) {
    uri = ur;
    name = token(n);
    val = v;
  }

  /**
   * Initializes all variables.
   * @param ctx query context
   */
  public static void init(final QueryContext ctx) {
    for(final VarDef v : values()) {
      ctx.vars.setGlobal(Var.create(ctx, null, new QNm(v.name, v.uri), v.val));
    }
  }
}
