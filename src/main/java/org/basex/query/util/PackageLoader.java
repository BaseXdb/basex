package org.basex.query.util;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.util.Package.Component;
import org.basex.query.util.Package.Dependency;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;

/**
 * Package loader.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PackageLoader {

  /**
   * @param pkgsInst installed packages
   * @param pkgName package to be loaded
   * @param uris loaded uris
   * @param modules loaded modules
   * @param pkgsLoaded loaded packages
   * @param ctx context
   * @throws IOException IO exception
   * @throws QueryException 
   */
  public static void loadPackage(final TokenMap pkgsInst, final byte[] pkgName,
      final TokenList uris, final StringList modules,
      final TokenList pkgsLoaded, final Context ctx) throws IOException, QueryException {
    // TODO: here use later pkg name + version
    if(!pkgsLoaded.contains(pkgName)) {
      // Package is still not loaded
      // TODO: here use later pkg name + version
      if(pkgsInst.get(pkgName) != null) {
        // Package is installed
        // Get package directory
        // todo: use later package name + version
        final byte[] pkgDir = pkgsInst.get(pkgName);
        final TokenBuilder tb = new TokenBuilder();
        tb.add(pkgDir);
        final IOFile io = new IOFile(new File(string(tb.add(
            token("/expath-pkg.xml")).finish())));
        // Parse package descriptor
        final Parser parse = Parser.xmlParser(io, ctx.prop, "");
        final ANode pkgNode = new DBNode(MemBuilder.build(parse, ctx.prop, ""),
            0).children().next();
        final Package pkg = PackageParser.parse(io, ctx, null);
        // Load package components
        final Iterator<Component> c = pkg.comps.iterator();
        Component comp;
        String module;
        while(c.hasNext()) {
          comp = c.next();
          module = string(tb.add(token('/')).add(comp.file).finish());
          if(uris.contains(comp.namespace)) {
            // Namespace is already loaded
            if(!modules.contains(module)) {
              // Module is still not loaded
              modules.add(module);
            }
          } else {
            uris.add(comp.namespace);
            modules.add(module);
          }
        }
        Iterator<Dependency> d = pkg.dep.iterator();
        Dependency dep;
        while(d.hasNext()) {
          dep = d.next();
          loadPackage(pkgsInst, dep.pkg, uris, modules, pkgsLoaded, ctx);
        }
      } else {
        // Package is not installed -> Error

      }
    }
  }
}
