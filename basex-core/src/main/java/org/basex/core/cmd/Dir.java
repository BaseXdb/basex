package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'dir' command and returns a directory representation of resources of the
 * currently opened database.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Dir extends Command {
  /**
   * Default constructor.
   * @param path database path (can be {@code null})
   */
  public Dir(final String path) {
    super(Perm.READ, true, path != null ? path : "");
  }

  @Override
  protected boolean run() throws IOException {
    String root = MetaData.normPath(args[0]);
    if(!root.isEmpty() && !Strings.endsWith(root, '/')) root += '/';

    final Table table = new Table();
    table.description = ENTRIES_X;
    table.header.add(INPUT_PATH).add(TYPE).add(DataText.CONTENT_TYPE).add(SIZE);

    final HashSet<String> set = new HashSet<>();
    final Data data = context.data();
    final Resources resources = data.resources;

    // list XML documents
    final IntList docs = resources.docs(root, false);
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      final int pre = docs.get(d);
      add(data.text(pre, true), ResourceType.XML, s -> (long) data.size(pre, Data.DOC), table,
          root, set);
    }
    // list file resources
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile bin = data.meta.dir(type);
      for(final byte[] pt : resources.paths(root, type)) {
        add(pt, type, s -> new IOFile(bin, s).length(), table, root, set);
      }
    }

    out.println(table.sort().finish());
    return true;
  }

  /**
   * Adds a table entry.
   * @param path path to resource
   * @param type resource type
   * @param size function for computing size
   * @param table table
   * @param root root path
   * @param set set with known resources
   */
  private void add(final byte[] path, final ResourceType type, final Function<String, Long> size,
      final Table table, final String root, final HashSet<String> set) {

    String string = string(path);
    boolean dir;
    string = string.substring(root.length());
    final int i = string.indexOf('/');
    dir = i >= 0;
    if(dir) string = string.substring(0, i);
    if(set.add(string)) {
      final TokenList tl = new TokenList(4);
      if(dir) {
        tl.add(string).add(S_DIR).add("").add(0);
      } else {
        tl.add(type.path(string)).add(type.toString());
        tl.add(type.contentType(string).toString()).add(size.apply(string));
      }
      table.contents.add(tl);
    }
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.DB);
  }
}
