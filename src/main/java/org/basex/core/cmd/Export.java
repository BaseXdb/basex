package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.PrintOutput;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Evaluates the 'export' command and saves the currently opened database
 * to disk.
 *
 * @author BaseX Team 2005-11, ISC License
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
      final Data data = context.data;
      export(context.prop, data, args[0]);
      return info(DBEXPORTED, data.meta.name, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Exports the specified database.
   * @param context context
   * @param data data reference
   * @throws IOException I/O exception
   */
  public static void export(final Context context, final Data data)
      throws IOException {
    export(context.prop, data, data.meta.file.path());
  }

  /**
   * Exports the current database to the specified path.
   * Files and Folders contained in {@code path} will be possibly overwritten.
   * @param prop property
   * @param data data reference
   * @param target file path
   * @throws IOException I/O exception
   */
  private static void export(final Prop prop, final Data data,
      final String target) throws IOException {

    final SerializerProp sp = new SerializerProp(prop.get(Prop.EXPORTER));
    final IO root = IO.get(target);
    if(!(root instanceof IOFile))
      throw new IOException(Util.info(DBNOTEXPORTED, target));

    if(!root.exists()) root.md();

    for(final int pre : data.doc()) {
      // create file path
      final IO file = root.merge(Token.string(data.text(pre, true)));
      // create dir if necessary

      final IO dir = IO.get(file.dir());
      if(!dir.exists()) dir.md();

      // existing files are overwritten
      // (including files inside a collection that have the same name)
      final PrintOutput po = new PrintOutput(file.path());
      final XMLSerializer xml = new XMLSerializer(po, sp);
      xml.node(data, pre);
      xml.close();
      po.close();
    }
  }
}
