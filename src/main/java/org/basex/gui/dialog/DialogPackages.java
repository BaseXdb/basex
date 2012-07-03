package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.query.util.pkg.*;
import org.basex.query.util.pkg.Package;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogPackages extends BaseXDialog {
  /** List of available packages. */
  private final BaseXList packages;
  /** Title of package. */
  private final BaseXLabel title;
  /** Install button. */
  private final BaseXButton install;
  /** Delete button. */
  private final BaseXButton delete;

  /** Name label. */
  private final BaseXLabel name;
  /** Version label. */
  private final BaseXLabel version;
  /** Type label. */
  private final BaseXLabel type;
  /** Path label. */
  private final BaseXLabel path;

  /** Refresh list of databases. */
  private boolean refresh;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPackages(final GUI main) {
    super(main, PACKAGES);
    panel.setLayout(new BorderLayout(8, 0));

    // create package chooser
    packages = new BaseXList(new String[] {}, this, false);
    packages.setSize(270, 160);

    title = new BaseXLabel(" ").large().border(0, 5, 5, 0);
    name = new BaseXLabel(" ");
    version = new BaseXLabel(" ");
    type = new BaseXLabel(" ");
    path = new BaseXLabel(" ");

    final BaseXBack table = new BaseXBack(new TableLayout(4, 2, 16, 0)).border(5);
    table.add(new BaseXLabel(NAME + COL, false, true));
    table.add(name);
    table.add(new BaseXLabel(VERSINFO + COL, false, true));
    table.add(version);
    table.add(new BaseXLabel(TYPE + COL, false, true));
    table.add(type);
    table.add(new BaseXLabel(PATH + COL, false, true));
    table.add(path);

    // database buttons
    install = new BaseXButton(INSTALL + DOTS, this);
    delete = new BaseXButton(DELETE + DOTS, this);

    final BaseXBack p = new BaseXBack(new BorderLayout());
    p.add(title, BorderLayout.NORTH);
    p.add(table, BorderLayout.CENTER);
    p.add(newButtons(install, delete), BorderLayout.SOUTH);
    BaseXLayout.setWidth(p, 430);

    //BaseXLayout.setWidth(details, 450);
    set(packages, BorderLayout.CENTER);
    set(p, BorderLayout.EAST);

    refresh = true;
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    final Context ctx = gui.context;
    if(refresh) {
      // rebuild databases and focus list chooser
      packages.setData(new RepoManager(ctx).list().toArray());
      packages.requestFocusInWindow();
      refresh = false;
    }

    final StringList pkgs = packages.getValues();
    final ArrayList<Command> cmds = new ArrayList<Command>();

    if(cmp == install) {
      final String pp = gui.gprop.get(GUIProp.PKGPATH);
      final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR, pp, gui);
      fc.addFilter(XML_ARCHIVES, IO.XARSUFFIX);
      fc.addFilter(JAVA_ARCHIVES, IO.JARSUFFIX);
      fc.addFilter(XQUERY_FILES, IO.XQSUFFIXES);
      final IOFile file = fc.select(Mode.FDOPEN);
      if(file == null) return;
      gui.gprop.set(GUIProp.PKGPATH, file.path());
      refresh = true;
      cmds.add(new RepoInstall(file.path(), null));

    } else if(cmp == delete) {
      if(!BaseXDialog.confirm(gui, Util.info(DELETE_PACKAGES_X, pkgs.size()))) return;
      refresh = true;
      for(final String p : pkgs) cmds.add(new RepoDelete(p, null));

    } else {
      final byte[] key = Token.token(packages.getValue());
      final TokenMap pkg = ctx.repo.pkgDict();
      if(pkg.get(key) != null) {
        title.setText(key.length == 0 ? DOTS : Token.string(key));
        name.setText(Token.string(Package.name(key)));
        version.setText(Token.string(Package.version(key)));
        type.setText(PkgText.EXPATH);
        path.setText(Token.string(pkg.get(key)));
      } else {
        final IOFile file = RepoManager.file(key, ctx.repo);
        final String pp = Token.string(key);
        title.setText(key.length == 0 ? DOTS : pp);
        name.setText(file != null ? file.name() : "-");
        version.setText("-");
        type.setText(PkgText.INTERNAL);
        path.setText(pp.replace('.', '/'));
      }
      // enable or disable buttons
      delete.setEnabled(pkgs.size() > 0);
    }

    // run all commands
    if(!cmds.isEmpty()) {
      DialogProgress.execute(this, cmds.toArray(new Command[cmds.size()]));
    }
  }
}