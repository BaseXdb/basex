package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.iter.AxisIter;
import org.basex.util.InputInfo;

/**
 * Parses the jar descriptors and performs schema checks.
 * @author BaseX Team 2005-11, BSD License
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
      final AxisIter ch = node.children();
      for(ANode next; (next = ch.next()) != null;) {
        final QNm name = next.qname();
        if(eqNS(JAR, name)) desc.jars.add(next.string());
        else if(eqNS(CLASS, name)) desc.classes.add(next.string());
      }
      if(desc.jars.size() == 0) JARDESCINV.thrw(input, NOJARS);
      else if(desc.classes.size() == 0) JARDESCINV.thrw(input, NOCLASS);
      return desc;
    } catch(final IOException ex) {
      throw JARREADFAIL.thrw(input, ex.getMessage());
    }
  }

  /**
   * Checks if the specified name equals the qname and if it uses the packaging
   * namespace.
   * @param cmp input
   * @param name name to be compared
   * @return result of check
   */
  private static boolean eqNS(final byte[] cmp, final QNm name) {
    return eq(name.ln(), cmp) && eq(name.uri().string(), QueryText.PACKURI);
  }
}
