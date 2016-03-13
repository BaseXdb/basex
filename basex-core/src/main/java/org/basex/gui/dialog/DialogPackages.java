package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;
import org.basex.query.util.pkg.*;
import org.basex.query.util.pkg.Pkg;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class DialogPackages extends BaseXDialog {
  /** List of available packages. */
  private final BaseXList packages;
  /** Title of package. */
  private final BaseXLabel title;
  /** Install from button. */
  private final BaseXButton installURL;
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
    packages = new BaseXList(new String[0], this, false);
    packages.setSize(270, 220);

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
    installURL = new BaseXButton(INSTALL_FROM_URL + DOTS, this);
    install = new BaseXButton(INSTALL + DOTS, this);
    delete = new BaseXButton(DELETE + DOTS, this);

    BaseXBack p = new BaseXBack(new BorderLayout());
    p.add(packages, BorderLayout.CENTER);
    final BaseXBack ss = new BaseXBack(new TableLayout(1, 2, 8, 0)).border(8, 0, 0, 0);
    ss.add(new BaseXLabel(PATH + COL, true, true), BorderLayout.NORTH);
    ss.add(new BaseXLabel(main.context.soptions.get(StaticOptions.REPOPATH)));
    p.add(ss, BorderLayout.SOUTH);
    set(p, BorderLayout.CENTER);

    p = new BaseXBack(new BorderLayout());
    p.add(title, BorderLayout.NORTH);
    p.add(table, BorderLayout.CENTER);
    p.add(newButtons(installURL, install, delete), BorderLayout.SOUTH);
    BaseXLayout.setWidth(p, 430);
    set(p, BorderLayout.EAST);

    refresh = true;
    action(null);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    final Context ctx = gui.context;
    if(refresh) {
      // rebuild databases and focus list chooser
      packages.setData(new RepoManager(ctx).list().finish());
      packages.requestFocusInWindow();
      refresh = false;
    }

    final StringList pkgs = packages.getValues();
    final ArrayList<Command> cmds = new ArrayList<>();

    if(cmp == installURL) {
      final DialogInstallURL dialog = new DialogInstallURL(this);
      if(!dialog.ok()) return;
      refresh = true;
      cmds.add(new RepoInstall(dialog.url(), null));

    } else if(cmp == install) {
      final String pp = gui.gopts.get(GUIOptions.WORKPATH);
      final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR, pp, gui);
      fc.filter(XML_ARCHIVES, IO.XARSUFFIX);
      fc.filter(JAVA_ARCHIVES, IO.JARSUFFIX);
      fc.filter(XQUERY_FILES, IO.XQSUFFIXES);
      final IOFile file = fc.select(Mode.FDOPEN);
      if(file == null) return;
      gui.gopts.set(GUIOptions.WORKPATH, file.path());
      refresh = true;
      cmds.add(new RepoInstall(file.path(), null));

    } else if(cmp == delete) {
      if(!BaseXDialog.confirm(gui, Util.info(DELETE_PACKAGES_X, pkgs.size()))) return;
      refresh = true;
      for(final String p : pkgs) cmds.add(new RepoDelete(p, null));

    } else {
      final String key = packages.getValue();
      final Pkg pkg = ctx.repo.pkgDict().get(key);
      if(pkg != null) {
        title.setText(key.isEmpty() ? DOTS : key);
        name.setText(pkg.name());
        version.setText(pkg.version());
        type.setText(PkgText.EXPATH);
        path.setText(pkg.dir());
      } else {
        final IOFile file = new RepoManager(ctx).find(key);
        title.setText(key.isEmpty() ? DOTS : key);
        name.setText(file != null ? file.name() : "-");
        version.setText("-");
        type.setText(PkgText.INTERNAL);
        path.setText(key.replace('.', '/'));
      }
      // enable or disable buttons
      delete.setEnabled(!pkgs.isEmpty());
    }

    // run all commands
    if(!cmds.isEmpty()) {
      DialogProgress.execute(this, cmds.toArray(new Command[cmds.size()]));
    }
  }
}