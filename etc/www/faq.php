<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$subpageOf ="documentation.php";
$webpage= "faq.php";
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== FAQ Section (should be later be generated ===== -->

<div id="main">
<h1>Documentation &ndash; FAQ</h1>

If you can't find an answer on your question, please write to<code> info@basex.org</code>.

<p>
<b><a href="#1">1 Introduction</a></b><br/>
<a href="#1.1">1.1 What is BaseX? </a><br/>
<a href="#1.2">1.2 On what systems does BaseX run? </a><br/>
<a href="#1.3">1.3 Who maintains BaseX? </a><br/>
<a href="#1.4">1.4 What is new in BaseX 4.0? </a><br/>
<a href="#1.5">1.5 Where to get the latest version?</a><br/>
<a href="#1.6">1.6 Where do I find the source code?</a><br/>
<a href="#1.7">1.7 How can I help/contribute?</a><br/>
<a href="#1.8">1.8 I found a bug, how do I file bug report?</a><br/>

<br/><b><a href="#2">2 BaseX Console</a></b><br/>
<a href="#2.1">2.1 How can I launch the console mode of BaseX?</a><br/>
<a href="#2.2">2.2 What can I do in the console mode?</a><br/>
<a href="#2.3">2.3 What is XPath/XQuery?</a><br/>
<a href="#2.4">2.4 What is Whitespace Chopping?</a><br/>
<a href="#2.5">2.5 What is Entity Parsing?</a><br/>
<a href="#2.6">2.6 How can i display the name of attributes?</a><br/>

<br/><b><a href="#3">3 BaseX GUI</a></b><br/>
<dl>
<dt><a href="#3.1">3.1 Views and Panels</a><br/></dt>
<dd><a href="#3.1.1">3.1.1 How many views are there and what do they do?</a><br/>
<a href="#3.1.2">3.1.2 A Table View for hierachical data. Sorry, but sounds brain-damaged.</a><br/></dd>
<dt><a href="#3.2">3.2 What is realtime filtering?</a><br/></dt>
<a href="#3.3">3.3 What does the Filter button exactly do?</a><br/>
<a href="#3.4">3.4 How can I search without XPath/XQuery?</a><br/>
<a href="#3.5">3.5 Is the Search field in the Query panel the same as the Search field right under the buttons?</a><br/>
<a href="#3.6">3.6 How can I configure the look and feel of BaseX?</a><br/>
<a href="#3.7">3.7 What is the Main-Memory Mode?</a><br/>
<a href="#3.8">3.8 How can I import my file system?</a><br/>
<a href="#3.9">3.9 How can I import my data?</a><br/>
</dl>

<br/><b><a href="#4">4 General Issues</a></b><br/>
<a href="#4.1">4.1 What additional files (config etc.) does BaseX use and where are they?</a><br/>
<a href="#4.2">4.2 How do you measure the performance of the queries?</a><br/>
<a href="#4.3">4.3 What indexing techniques are available and what do they do?</a><br/>
<a href="#4.5">4.4 Is it possible to start BaseX as a Server?</a><br/>

<br/><b><a href="#5">5 Features</a></b><br/>
<a href="#5.1">5.1 Can I have more information on the XPath/XQuery implementations?</a><br/>
<a href="#5.2">5.2 OK, you support several Indexes. What exactly can I use and how?</a><br/>
<a href="#5.3">5.3 Tell me more about XQuery Full-Text. How can I use it?</a><br/>
<a href="#5.4">5.4 How do you support XML Updates. Do you accomplish any standard?</a><br/> 
</p>

<p><br/></p>

<a name="1"></a><h2>1 Introduction</h2>

<a name="1.1"></a><h3>1.1 What is BaseX?</h3>
<p>
BaseX is a native XML database.  It features an XPath/XQuery implementation,
supports updates and has a visual frontend, facilitating visual access to the
stored data.
<!--
<br/>
<li/>BaseX is an interactive and innovative exploration and editing tool for XML files or filesystems.<br/>
<li/>BaseX provides an effective and efficient way to display XML files 
or filesystems as normal tree, treemap or table.
In these 3 different visualizations it's possible to explore and edit the data very easy.<br/>
<li/>BaseX is fully implemented in Java and platform-independent.
-->
</p>

<a name="1.2"></a><h3>1.2 On what systems does BaseX run? </h3>
<p>
BaseX is completely written in Java.  As such it is plattform-independant and
should run on any plattform that has a Java Virtual Machine available.  BaseX
uses Java Version 1.5 or later, so this might be a limitation.  BaseX has been
tested on Windows (2000, XP, Vista), Max OS X (10.4, 10.5), Linux(SuSE xxx,
Debian) and OpenBSD (4.2).
</p>

<a name="1.3"></a><h3>1.3 Who maintains BaseX? </h3>
<p>
BaseX is developed by the <a href="http://www.inf.uni-konstanz.de/dbis">Database and Information Systems Group</a> at the University of Konstanz.
Main developer and project leader is <a href="http://www.inf.uni-konstanz.de/~gruen">Christian Gr&uuml;n</a>.
Several <a href="contact.php">students and other group members</a> have contributed to
the project.
</b>

<a name="1.4"></a><h3>1.4 What is new in BaseX 4.0? </h3>
This version of BaseX offers many new features such as...
<ul>
 <li> Support of <b>XQuery 1.0</b>, reaching 99.3% of the W3C XQuery Test Suite</li>
  <li> Partial support of <b>XQuery Full-Text</b>, based on the W3C working draft</li>
	 <li> Several <b>Indexes</b>, including a full-text index<br/>
   (currently only applied by the XPath 1.0 implementation)</li>
   <li> GUI interactions for <b>XML Updates</b></li>
	 <li> A <b>Query Panel</b> has been added for entering XPath and XQuery</li>
   <li> A <b>Table View</b> allows a flat view of regular XML documents</li>
   <li> A <b>Help View</b> offers interactive feedback on the GUI features</li>
   <li> A revised <b>command syntax</b> (try 'help' in the console or the GUI command mode)</li>
</ul>

<a name="1.5"></a><h3>1.5 Where to get the latest version?</h3>
<p>
The latest version is found at <a href='download.php'>www.basex.org</a>
and on the <a href="http://sourceforge.net/projects/basex">Sourceforge Project Site</a>.
</p>

<a name="1.6"></a><h3>1.6 Is there access to the source code?</h3>
<p>
The BaseX sources are available in a Subversion repository via Sourceforge.
<a href="http://sourceforge.net/projects/basex">Sourceforge Project Site</a>.
You also find them as part of the complete, zipped distribution of BaseX:
<a href="http://sourceforge.net/projects/BaseX-Complete.zip">BaseX-Complete.zip</a>
</p>

<a name="1.7"></a><h3>1.7 How can I help/contribute?</h3>
<p>
BaseX is still work in progress. So we are definitely interested in all kind
of experiences you gain during usage. Please provide feedback, bug reports,
report about missing features, your application domain. This all can be done
by posting to <code>info@basex.org</code>.
</p>

<a name="1.8"></a><h3>1.8 I found a bug, how do I file bug report?</h3>
<p>
There is a bug reporting system on SourceForge. Otherwise send an email to <code>info@basex.org</code>.
</p>


<a name="2"></a><h2>2 BaseX Console</h2>

<a name="2.1"></a><h3>2.1 How can I launch the console mode of BaseX?</h3>
<p>
Just enter<code> java -cp BaseX.jar org.base.BaseX </code>on the command line.
The Java option<code> -Xmx... </code>reserve more memory, and the<code> -h </code>
flag of BaseX lists all available flags:
<blockquote>
<code>
java -Xmx512m -cp BaseX.jar org.basex.BaseX -h

BaseX 4.0; DBIS, University of Konstanz
Usage: BaseX [options] [query]
  [query]    specify query file
  -c         chop whitespaces
  -d         debug mode
  -e         skip entity parsing
  -o [file]  specify output file
  -p         handle query file as XPath
  -q<cmd>    send BaseX commands
  -v/V       show (all) process info
  -x         print result as xml
  -z         skip query output

</code>
</blockquote>
</p>

<a name="2.2"></a><h3>2.2 What can I do in the console mode?</h3>
<p>
Type in<code> help </code> to get a list of all 
<a href="commands.php">BaseX commands</a>. Several commands can
be separated by semicolons. To evaluate commands
without entering the console mode, you can use the
<code>-q</code> option on the command line: 


<blockquote>
<pre>
java -jar BaseX.jar -vq "create xml input.xml; xpath /"

Process Info ON
Database created in 195.2 ms.
&lt;html&gt;
  &lt;!-- Header --&gt;
  &lt;head id="0"&gt;
    &lt;title&gt;XML&lt;/title&gt;
  &lt;/head&gt;
  &lt;!-- Body --&gt;
  &lt;body id="1" bgcolor="#FFFFFF" text="#000000" link="#0000CC"&gt;
    &lt;h1&gt;Databases &amp; XML&lt;/h1&gt;
    &lt;div align="right"&gt;
      &lt;b&gt;Assignments&lt;/b&gt;
      &lt;ul&gt;
        &lt;li&gt;Exercise 1&lt;/li&gt;
        &lt;li&gt;Exercise 2&lt;/li&gt;
      &lt;/ul&gt;
    &lt;/div&gt;
  &lt;/body&gt;
&lt;/html&gt;

Compiling : 43.28 ms
Evaluating: 0.15 ms
Printing  : 10.7 ms
Total Time: 106.64 ms
Results   : 1 Item
Printed   : 360 Bytes
</pre>
</blockquote>
</p>

<a name="2.3"></a><h3>2.3 What is XPath/XQuery?</h3>
<ul>
  <li>XPath is a simple query language for finding information in an
XML document. XPath is used to navigate through elements and attributes
in an XML document
(<a href="http://www.w3.org/TR/xpath">XPath - W3C Website</a>).</li>
  <li/> XQuery is a complete query language from the W3C that
provides an easy way to query, transform, and integrate XML data.
(<a href="http://www.w3.org/TR/xquery">XQuery - W3C Website</a>).</li>
</ul>

<a name="2.4"></a><h3>2.4 What is Whitespace Chopping?</h3>
<div>This option removes all whitespaces between tags and contents. In the current
version of BaseX, there is no DTD or XML schema detection which would make
it possible to explicitly ignore special whitespaces.</div>

<a name="2.5"></a><h3>2.5 What is Entity Parsing?</h3>
<div>The parsing of entities in the document can be skipped or switched on with this
option. Entities are important for DTD declarations and definitions of special
characters.</div>

<a name="2.6"></a><h3>2.6 How can i display the name of attributes?</h3>
<div>There is an option in the Options/Preferences menu where it's possible to choose,
replace tags with @name attributes.</div>

<a name="3"></a><h2>3 BaseX GUI</h2>

<a name="3.1"></a><h3>3.1 Views and Panels</h3>

<a name="3.1.1"></a><h4>3.1.1 How many views are there and what do they do?</h4>
<div>
<div>
  <dl>
    <dt>Query View</dt>
    <dd>Executes XPath, XQuery and simple queries to explore the xml document.</dd>

    <dt>Query Info View</dt>
    <dd>Displays information on the compilation and evaluation of an XPath or XQuery request, including some simple profiling.</dd>

    <dt>Text View</dt>
    <dd>Displays query results and other textual output.</dd>

    <dt>Map View</dt>
    <dd>This visualization represents all data in a TreeMap. All nodes of the XML document
		are represented as rectangles, filling the complete area.</dd>

    <dt>Tree View</dt>
    <dd>This visualization displays all XML nodes in a usual tree view.</dd>

    <dt>Table View</dt>
    <dd>This visualization displays all XML nodes in a table with rows and columns.</dd>

    <dt>Help View</dt>
    <dd>This view provides context sensitive information to use all the BaseX features.</dd>
  </dl>
</div>

<a name="3.1.2"></a><h4>3.1.2 A Table View for hierachical data. Sorry, but sounds brain-damaged.</h4>
It sounds brain-damaged but it's really useful for many kinds of data. It's just another visualization which provides
an efficient and effective way to display data. See the following example:

<h6>Food Menu</h6>

<p><img border="1" src="gfx/TableView.png"/></p>

It's possible to edit/add/delete elements in the table view directly.

<a name="3.2"></a><h3>3.2 What is Realtime Filtering?</h3>
<div>
If realtime filtering is enabled, all visualizations directly show the query results
while entering the query. If this feature is disabled, the query results are highlighted
in the visualizations and can be ecplicitly filtered using the 'Filter' button.
</div>

<a name="3.3"></a><h3>3.3 What does the Filter button exactly do?</h3>
<div>
After pressing this button, the visualizations display the previously highlighted
XML nodes, omitting all the other nodes of the document. 
If realtime filtering is enabled, the Filter button will be disabled, and
all results are automatically filtered.
</div>

<a name="3.4"></a><h3>3.4 How can I search without XPath/XQuery?</h3>

<div>
The Search field triggers a simple search query in the XML document. The following syntax is supported:<br/>
<center>
<table>
<tr>
  <th align='left'>Query &nbsp; &nbsp; &nbsp;</th>
  <th align='left'>Description</th>
</tr>
<tr>
  <td align='left'><i>foo</i></td>
  <td align='left'>Find tags and texts containing <i>foo</i></td>
</tr>
<tr>
  <td align='left'>=<i>foo</i></td>
  <td align='left'>Find exact matching text nodes</td>
</tr>
<tr>
  <td align='left'>@<i>foo</i></td>
  <td align='left'>Find attributes and attribute values</td>
</tr>
<tr>
  <td align='left'>@=<i>foo</i></td>
  <td align='left'>Find exact attribute values</td>
</tr>
</table>
</center>
</div>

<a name="3.5"></a><h3>3.5 The search field in the query panel and the search
field right beyond the buttons, do they differ?</h3>

<div>
Not really, both offer the same functionality, but in the query panel there is
a dropdown menu where it's possible to choose tags or attributes of the XML
document.
</div>

<a name="3.6"></a><h3>3.6 How can I configure the look and feel of BaseX?</h3>
<div>
All panels are freely adjust- and dragable.  Changing of colors, fonts and the
layout is possible via the 'Layout' entry in the 'Options' menu.
</div>

<a name="3.7"></a><h3>3.7 What is the Main-Memory Mode?</h3>
<div>
The Main-Memory Mode speeds up querying but disables updates.
The table data is kept in memory and the text of a document is still accesseed
from disk.
</div>

<a name="3.8"></a><h3>3.8 How can I import my file system? What does that mean?</h3>
<div>
To import a file system, go to 'File/Import Filesystem...'.  You will be
prompted for the next steps.  A file hierarchy traversal is performed and the
directory structure of the filesystem is mapped into an XML representation.
Additionally some metadata is extracted from some known file types.  What you
get is an 'XML view' of your current file system.  You can query the filesystem
just as any other database instance.  Manipulation (update, deletion) of the
XML database instance representing a file hierarchy has no effect on the real
filesystem.
</div>

<a name="3.9"></a><h3>3.9 How can I import my data aka how do i create a new database instance aka how to shred an XML file?</h3>
<div>
<ol>
  <li>If your input data is an XML file,  you can use the 'New' command.  BaseX
will create a new database with the name of your XML file. (GUI only)</li>
  <li>You can use the <code>create</code> command. (GUI and console)</li>
</ol>
</div>

<a name="4"></a><h2>4 General Issues</h2>

<a name="4.1"></a><h3>4.1 Where does BaseX store data and configuration files?</h3>
<div>
<ul>
<li>The databases are stored in a <code>BaseXData</code> directory in your home directory.
The path can be changed in the GUI via the Options/Preferences menu.</li>
<li>The two configuration files <code>.basex</code>/<code>.basewin</code> are stored
in the same directory. This path can't be changed by the user.</li>
</ul>
</div>

<a name="4.2"></a><h3>4.2 How do you measure the performance of the queries?</h3>
<div>
The measurements include the compilation, evaluation and printing time of a query.
There are different ways to get the performance information:
<ul>
<li>Console: use "-v" flag as command line argument<</li>
<li>Console Mode: enter "set info" or "set info all" in the console mode</li>
<li>GUI Mode: display performance results in the QueryInfo view</li>
</ul>
</div>

<a name="4.3"></a><h3>4.3 What indexing techniques are available and what do they do?</h3>
<div>
Indexes can speedup queries by magnitudes.
Currently, four indexes exist:<br/>
<ul>
<li/> Text Index: All text nodes are indexed to speedup XPath predicates.<br/>
<li/> Attribute Value Index: All attributes are indexed to speedup XPath predicates.<br/>
<li/> Word Index: All terms in text nodes are indexed to speedup word based queries.<br/>
<li/> Fulltext Index: A full-fledged fulltext index is created to speedup fulltext queries in XPath.<br/>
</ul>
Note that the indexes only speedup XPath and simple user queries.
Currently, XQuery does not make use of the indexes.<br/>
</div>

<a name="4.4"></a><h3>4.4 Is it possible to start BaseX as a Server?</h3>
<div>
In addition to the standalone version, it is possible to run BaseX
in a Client/Server environment:
<blockquote>
<div>
<h3>Server</h3>
To start BaseX in server mode, just type:<br/><br/>
<code>java -cp BaseX.jar org.basex.BaseXServer</code><br/>
<h3>Client</h3>
To run a BaseX client, please type:<br/><br/>
<code>java -cp BaseX.jar org.basex.BaseXClient -s server.org</code><br/><br/>
The<code> -s </code> flag specifies the computer on which the server is running.<br/>
To list all available flags, just add<code> -h </code>to the call, e.g.:
<pre>
java -cp BaseX.jar org.basex.BaseXServer -h

BaseX 4.0; DBIS, University of Konstanz
Usage: java BaseXServer [options]
 stop     stop server
 -d       debug mode
 -h       show this help
 -p&lt;port&gt; specify server port
</pre>

</div></blockquote>

<a name="5"></a><h2>5 Features</h2>
<a name="5.1"></a><h3>5.1 Can I have more information on the XPath/XQuery implementations?</h3>
Note that, in BaseX, XPath and XQuery are based on different implementations. Whereas XPath was
especially tuned for efficient querying, XQuery strives for a high conformity with the
XQuery standard. As soon as our XQuery implementation includes the optimizations that have
been applied to XPath, the XPath implementation might get obsolete. 

<a name="5.2"></a><h3>5.2 OK, you support several Indexes. What exactly can I use and how?</h3>
Text indexes allow a speedup of order of magnitudes for text-based queries. Currently, the
indexes are only utilized by XPath queries. For example, the following query will be internally
rewritten to access the the text index first:

<blockquote>
<div>
<h3>Text-Based Query</h3>
<code>/descendant::node[text() = 'findme']
 </code>
</div></blockquote>

<a name="5.3"></a><h3>5.3 Tell me more about XQuery Full-Text. How can I use it?</h3>

The full-text features can be used in XPath as well as in XQuery. Again, queries will be
evaluated much faster in XPath, and XQuery covers more features of the language specification.
In the following, two simple examples for full-text queries are shown, based on the 
<a href="http://www.w3.org/TR/xpath-full-text-10-use-cases/">W3C Sample Document</a>.

<blockquote>
<div>
<h3>XPath Full-Text</h3>
Return all title tags which contain the word 'Usability':<br/><br/>
<code>//title[text() ftcontains 'Usability']</code><br/>
<h3>XQuery Full-Text</h3>
Return all authors, containing 'Marigold':<br/><br/>
<code>for   $i in //author score $s
where $i/text() ftcontains 'Marigold'
return &lt;hit score="{ $s }"&gt;{ $i/text() }&lt;/hit&gt;</code><br/>
&nbsp;
</div></blockquote>

<a name="5.4"></a><h3>5.4 How do you support XML Updates. Do you accomplish any standard?</h3> 
BaseX provides internal commands to perform updates on XML (see also <a href="commands.php">BaseX Commands</a>).
Moreover, the GUI provides a convenient way to perform updates on the data.
Support of the <a href="http://www.w3.org/TR/xqupdate/">W3C XQuery Update Facility</a>
is still on our (extensive) todo list..
</div>

<hr/>
                

<!-- ===== FAQ Section (should be later be generated ===== -->

<br/><br/>
<? include("inc/footer.inc"); ?>
