Introduction
------------

Welcome to the BaseX Open Source project. We are interested in your feedback:

* Please send new suggestions and bug reports to our
  [basex-talk](https://basex.org/open-source/) mailing list.
* Confirmed bugs and feature requests are discussed in our issue tracker.
* We invite you to contribute to our [Documentation](https://docs.basex.org/).

Compiling BaseX
---------------

The code base of BaseX can be accessed on [GitHub]: https://github.com/BaseXdb/basex

JDK 11 and JUnit are currently required to compile the complete sources of the
main project. Our default IDE is Eclipse.

You can launch the following classes, which are all placed in the basex-core
directory and the `org.basex` main package:

    BaseX        : console mode
    BaseXServer  : server instance, waiting for requests
    BaseXClient  : console mode, interacting with the server
    BaseXGUI     : graphical user interface

Moreover, try `-h` to list the available command line options. For example, you
can use BaseX to process XQuery expressions without entering the console.

Using Eclipse
-------------

BaseX is being developed with the Eclipse environment. Some style guidelines
are integrated in the sources of BaseX; they are being embedded as soon as you
open the project.

### Running BaseX

The following steps can be performed to start BaseX with Eclipse:

 - Press `Run` → `Run...`
 - Create a new `Java Application` launch configuration
 - Select `basex` as Project
 - Choose a `Main class` (e.g., org.basex.BaseXGUI)
 - Launch the project via `Run`

Best regards, have fun,

Your BaseX Team

[documentation]: https://docs.basex.org
[bug tracker]: https://github.com/BaseXdb/BaseX/issues
