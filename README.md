BaseX
=====

Welcome to the source code of BaseX.

To get the project running as smooth as possible, please consider the following notes:

Compiling BaseX
---------------

JDK 1.6 and JUnit is currently required to compile the complete sources
of the main project. If you are using another environment than Eclipse
or don't have JUnit installed, just delete the `test` packages inside
the project and rebuild the project.

Please take a look at the [Maven documentation](https://docs.basex.org/wiki/Maven) for information on how to use Maven.

You can launch the following classes, which are all placed in the basex-core directory and the `org.basex` main package:

    BaseX        : console mode
    BaseXServer  : server instance, waiting for requests
    BaseXClient  : console mode, interacting with the server
    BaseXGUI     : graphical user interface

Moreover, try `-h` to list the available command line options. For
example, you can use BaseX to process XQuery expressions without
entering the console.

Using Eclipse
-------------

BaseX is being developed with the Eclipse environment. Some style
guidelines are integrated in the sources of BaseX; they are being
embedded as soon as you open the project.

Running BaseX
-------------

The following steps can be performed to start BaseX with Eclipse:

 - Press `Run` -> `Run...`
 - Create a new `Java Application` launch configuration
 - Select `basex` as Project
 - Choose a `Main class` (e.g., org.basex.BaseXGUI)
 - Launch the project via `Run`

Adding Checkstyle
-----------------

Some additional Checkstyle guidelines are defined in the project:

 - Open Eclipse
 - Press `Help` -> `Install new Software...`
 - Press `Search for new features to install`
 - Enter the URL: `http://eclipse-cs.sourceforge.net/update`
 - Follow the installation procedure and restart Eclipse

Using Git
---------

The code base of BaseX can be accessed via [GitHub](https://www.github.com).

Feedback
--------

Any kind of feedback is welcome; please check out the [documentation](https://docs.basex.org).

Tell us if you run into any troubles installing BaseX:

    basex-talk@mailman.uni-konstanz.de.

You are as well invited to contribute to our [bug tracker](https://github.com/BaseXdb/BaseX/issues).

All the best,

BaseX Team, 2015
