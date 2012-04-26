package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

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
  private final InputInfo info;

  /**
   * Constructor.
   * @param ctx database context
   * @param ii input info
   */
  public JarParser(final Context ctx, final InputInfo ii) {
    context = ctx;
    info = ii;
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
      if(desc.jars.isEmpty()) JARDESCINV.thrw(info, NOJARS);
      else if(desc.classes.isEmpty()) JARDESCINV.thrw(info, NOCLASS);
      return desc;
    } catch(final IOException ex) {
      throw JARREADFAIL.thrw(info, ex.getMessage());
    }
  }
}
