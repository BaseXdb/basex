package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.HashSet;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.SerializerProp;
import org.basex.io.serial.XMLSerializer;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * Evaluates the 'export' command and saves the currently opened database
 * to disk.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Export extends Command {
  /**
   * Default constructor, specifying a target path.
   * @param path export path
   */
  public Export(final String path) {
    super(DATAREF | User.CREATE, path);
  }

  @Override
  protected boolean run() {
    try {
      final Data data = context.data();
      export(prop, data, args[0]);
      return info(DBEXPORTED, data.meta.name, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Exports the current database to the specified path.
   * Files and Folders contained in {@code path} will be possibly overwritten.
   * @param prop property
   * @param data data reference
   * @param target directory
   * @throws IOException I/O exception
   */
  public static void export(final Prop prop, final Data data,
      final String target) throws IOException {

    final SerializerProp sp = new SerializerProp(prop.get(Prop.EXPORTER));
    final IOFile root = new IOFile(target);
    root.md();

    final HashSet<String> exported = new HashSet<String>();
    final IntList il = data.doc();
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      // create file path
      final IOFile file = root.merge(Token.string(data.text(pre, true)));
      // create dir if necessary
      final IOFile dir = new IOFile(file.dir());
      if(!dir.exists()) dir.md();

      // attach counter to duplicate file names
      final String fp = file.path();
      String path = fp;
      int c = 1;
      while(exported.contains(path)) {
        path = fp.indexOf('.') == -1 ? fp + '(' + ++c + ')' :
             fp.replaceAll("(.*)\\.(.*)", "$1(" + ++c + ").$2");
      }
      exported.add(fp);

      // serialize file
      final PrintOutput po = new PrintOutput(path);
      final XMLSerializer xml = new XMLSerializer(po, sp);
      xml.node(data, pre);
      xml.close();
      po.close();
    }
  }
}
