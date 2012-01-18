package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * Evaluates the 'export' command and saves the currently opened database
 * to disk.
 *
 * @author BaseX Team 2005-12, BSD License
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
      export(data, args[0]);
      return info(DBEXPORTED, data.meta.name, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Exports the current database to the specified path.
   * Files and directories in {@code path} will be possibly overwritten.
   * @param data data reference
   * @param target directory
   * @throws IOException I/O exception
   */
  public static void export(final Data data, final String target)
      throws IOException {

    final String exp = data.meta.prop.get(Prop.EXPORTER);
    final SerializerProp sp = new SerializerProp(exp);
    final IOFile root = new IOFile(target);
    root.md();

    final HashSet<String> exported = new HashSet<String>();

    // export raw files
    final IntList il = data.docs();
    for(int i = 0, is = il.size(); i < is; i++) {
      final int pre = il.get(i);
      // create file path
      final IOFile file = root.merge(Token.string(data.text(pre, true)));
      // create dir if necessary
      final IOFile dir = new IOFile(file.dir());
      if(!dir.exists()) dir.md();

      // serialize file
      final PrintOutput po = new PrintOutput(unique(exported, file.path()));
      final Serializer ser = Serializer.get(po, sp);
      ser.node(data, pre);
      ser.close();
      po.close();
    }

    // export raw files
    final IOFile bin = data.meta.binaries();
    for(final String s : bin.descendants()) {
      final String u = unique(exported, new IOFile(root.path(), s).path());
      Copy.copy(new File(bin.file(), s), new File(u));
    }
  }

  /**
   * Returns a unique file path.
   * @param exp exported names
   * @param fp file path
   * @return unique path
   */
  private static String unique(final HashSet<String> exp, final String fp) {
    int c = 1;
    String path = fp;
    while(exp.contains(path)) {
      path = fp.indexOf('.') == -1 ? fp + '(' + ++c + ')' :
           fp.replaceAll("(.*)\\.(.*)", "$1(" + ++c + ").$2");
    }
    exp.add(path);
    return path;
  }
}
