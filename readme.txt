
BASEX ==========================================================================

 Welcome to the source code of BaseX.

 To get the project running as smooth as possible, please consider the
 following notes:

COMPILING BASEX ----------------------------------------------------------------

 JDK 1.6 and JUnit is currently needed to compile the complete sources of the
 main project. If you are using another environment than Eclipse or don't have
 JUnit installed, just delete the 'test' package inside the project and rebuild
 the project.

 You can launch the following classes, which are all placed in the main package
 org.basex:

 BaseX        : console mode
 BaseXServer  : server instance, waiting for requests
 BaseXClient  : console mode, interacting with the server
 BaseXWin     : graphical user interface

 Moreover, try -h to list the available command line options. For example, you
 can use BaseX to process XQuery expressions without entering the console.

USING ECLIPSE ------------------------------------------------------------------

 BaseX is being developed with the Eclipse environment. Some style guidelines
 are integrated in the sources of BaseX; they are being embedded as soon as you
 open the project.

RUNNING BASEX ------------------------------------------------------------------

 The following steps can be performed to start BaseX with Eclipse:

 - Press Run -> Run...
 - Create a new "Java Application" launch configuration
 - Select "basex" as "Project"
 - Choose a "Main class" (e.g., org.basex.BaseXWin for the GUI version)
 - Launch the project via 'Run'

ADDING CHECKSTYLE --------------------------------------------------------------

 Some additional guidelines are defined via the CheckStyle plugin:

 - Open Eclipse
 - Press "Help" -> "Install new Software..."
 - Press "Search for new features to install"
 - Enter the URL "http://eclipse-cs.sourceforge.net/update"
 - Follow the installation procedure and restart Eclipse

--------------------------------------------------------------------------------

 Any kind of feedback is welcome; please check out the online documentation at
 http://basex.org/documentation and tell us if you run into any troubles
 while installing and running BaseX: basex-talk@mailman.uni-konstanz.de

 BaseX Team, 2010

============================================= DBIS Group, University of Konstanz
