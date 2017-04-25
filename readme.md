BaseX Readme
============

Welcome to the source code of BaseX.

To get the project running as smooth as possible, please consider the following notes:

Compiling BaseX
---------------

JDK 1.6 and JUnit is currently needed to compile the complete sources of the main project. If you are using another environment than Eclipse or don't have JUnit installed, just delete the 'test' package inside the project and rebuild the project.

If you want to use Maven, have a look at our [Maven documentation].

[Maven documentation]: http://docs.basex.org/wiki/Maven

You can launch the following classes, which are all placed in the main package `org.basex`:

 - `BaseX`:			console mode
 - `BaseXServer`:	server instance, waiting for requests
 - `BaseXClient`:	console mode, interacting with the server
 - `BaseXGUI`:		graphical user interface

Moreover, try `-h` to list the available command line options. For example, you can use BaseX to process XQuery expressions without entering the console.

Using Eclipse
-------------

BaseX is being developed with the Eclipse environment. Some style guidelines are integrated in the sources of BaseX; they are being embedded as soon as you open the project.

### Running BaseX

The following steps can be performed to start BaseX with Eclipse:

 - Press Run -> Run...
 - Create a new "Java Application" launch configuration
 - Select "basex" as "Project"
 - Choose a "Main class" (e.g., `org.basex.BaseXGUI`)
 - Launch the project via "Run"

### Adding Checkstyle

Some additional Checkstyle guidelines are defined in the project:

 - Open Eclipse
 - Press "Help" -> "Install new Software..."
 - Press "Search for new features to install"
 - Enter the URL: http://eclipse-cs.sourceforge.net/update
 - Follow the installation procedure and restart Eclipse

Using Git
---------

The code base of BaseX can be accessed via [GitHub](http://www.github.com):

 - **Main Package**: `http://github.com/BaseXdb/basex`
 - **APIs**: REST, WebDAV, XML:DB, XQJ: `http://github.com/BaseXdb/basex-api`
 - **Examples**: `http://github.com/BaseXdb/basex-examples``
 - **Tests**, Stress Tests: `http://github.com/BaseXdb/basex-tests`

------------------------------------------------------------------------

Any kind of feedback is welcome; please check out the [documentation]().

[docs]: http://docs.basex.org

Contact us if you run into any troubles installing BaseX on our [mailing list]().

[mailing list]: basex-talk@mailman.uni-konstanz.de

You are as well invited to contribute to our [bug tracker]().

[bug tracker]: https://github.com/BaseXdb/BaseX/issues

All the best,
BaseX Team, 2012

------------------------------------------------------------------------

