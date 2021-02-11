package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Parses the jar descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
final class JarParser {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   */
  JarParser(final InputInfo info) {
    this.info = info;
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
      final ANode node = new DBNode(io).childIter().next();
      for(final ANode next : node.childIter()) {
        if(next.type != NodeType.ELEMENT) continue;

        final QNm name = next.qname();
        // ignore namespace to improve compatibility
        if(eq(E_JAR, name.local())) desc.jars.add(next.string());
        else if(eq(E_CLASS, name.local())) desc.classes.add(next.string());
      }
      if(desc.jars.isEmpty()) throw REPO_PARSE_X_X.get(info, io.name(), NOJARS);
      if(desc.classes.isEmpty()) throw REPO_PARSE_X_X.get(info, io.name(), NOCLASSES);
      return desc;
    } catch(final IOException ex) {
      throw REPO_PARSE_X_X.get(info, io.name(), ex);
    }
  }
}
