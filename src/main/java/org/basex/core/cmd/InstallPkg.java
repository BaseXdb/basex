package org.basex.core.cmd;

import java.io.IOException;

import org.basex.core.Command;

public final class InstallPkg extends Command {

  private String pkg;
  
  public InstallPkg(final String pkg) {
    super(STANDARD);
    this.pkg = pkg;
    // TODO Auto-generated constructor stub
  }

  @Override
  protected boolean run() throws IOException {
    System.out.println("install: " + pkg);
    return true;
  }

}
