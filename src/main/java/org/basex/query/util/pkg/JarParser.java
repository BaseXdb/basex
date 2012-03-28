package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.util.InputInfo;

/**
 * Parses the jar descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class JarParser {
  /** Context. */
  private final Context context;
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param ctx database context
   * @param ii input info
   */
  public JarParser(final Context ctx, final InputInfo ii) {
    context = ctx;
    input = ii;
  }

  /**
   * Parses a jar descriptor.
   * @param io XML input
   * @return jar descriptor container
   * @throws QueryException query exception
   */
  public JarDesc parse(final IO io) throws QueryException {
    final JarDesc desc = new JarDesc();
    try {
      final ANode node = new DBNode(io, context.prop).children().next();
      for(final ANode next : node.children()) {
        final QNm name = next.qname();
        // ignore namespace to improve compatibility
        if(eq(JAR, name.local())) desc.jars.add(next.string());
        else if(eq(CLASS, name.local())) desc.classes.add(next.string());
        // [CG] Packaging: add warning if unknown elements are encountered
      }
      if(desc.jars.size() == 0) JARDESCINV.thrw(input, NOJARS);
      else if(desc.classes.size() == 0) JARDESCINV.thrw(input, NOCLASS);
      return desc;
    } catch(final IOException ex) {
      throw JARREADFAIL.thrw(input, ex.getMessage());
    }
  }
}
