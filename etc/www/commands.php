<? $top ="documentation"; include("inc/header.inc"); ?>

<p>
Here you find the BaseX commands which you can enter in the console
mode or in the Command text field in the GUI:</p>

<a href="#bash">Bash</a> |
<a href="#close">Close</a> |
<a href="#copy">Copy</a> |
<a href="#create">Create</a> |
<a href="#cs">CS</a> |
<a href="#delete">Delete</a> |
<a href="#drop">Drop</a> |
<a href="#export">Export</a> |
<a href="#exit">Exit</a> |
<a href="#find">Find</a> |
<a href="#help">Help</a> |
<a href="#info">Info</a> |
<a href="#insert">Insert</a> |
<a href="#list">List</a> |
<a href="#open">Open</a> |
<a href="#optimize">Optimize</a> |
<a href="#set">Set</a> |
<a href="#update">Update</a> |
<a href="#xpath">XPath</a> |
<a href="#xquery">XQuery</a>
<p>&nbsp;</p>


<h2>Database Commands</h2>

<a name="create"></a><h3>Create</h3>
<p>
<code>create [DB|FS|INDEX] [...]</code><br/><br/>
Creates database from XML or filesystem, or creates index:
<ul>
<li><code>DB [path] [name?]</code>:<br/>
Creates database <code>[name]</code> for the XML file or directory <code>[path]</code>.
</li>
<li><code>FS [path] [name]</code>:<br/>
Creates filesystem database <code>[name]</code> for <code>[path]</code>.</li>
<li><code>INDEX [TEXT|ATTRIBUTE|FULLTEXT]</code>:<br/>
Creates the specified index.</li>
</ul>
</p>

<a name="open"></a><h3>Open</h3>
<p>
<code>open [database]</code>
<br/>
<p>
Opens the specified [database].
</p>

<a name="info"></a><h3>Info</h3>
<p>
<code>info [DB|INDEX|TABLE]?</code><br/>
<br/>
Shows information on the currently opened database:
<ul>
<li>no argument: shows global information</li>
<li><code>DB</code>: shows database information</li>
<li><code>INDEX</code>: shows index information.</li>
<li><code>TABLE [start end] | [query]</code>: shows XML table</li>
</ul>
</p>

<a name="close"></a><h3>Close</h3>
<p>
<code>close</code>  <br/>
<br/>
Closes the current database.
</p>

<a name="list"></a><h3>List</h3>
<p>
<code>list</code> <br/>
<br/>
Lists all available databases.
</p>

<a name="drop"></a><h3>Drop</h3>
<p>
<code>drop [DB|INDEX] [...]</code> <br/>
<br/>
Drops a database or an index:
<ul>
<li><code>DB [name]</code>:<br/>
Drops the database <code>[name]</code>.</li>
<li><code>INDEX [TEXT|ATTRIBUTE|FULLTEXT]</code>:<br/>
Drops the specified index.</li>
</ul>
</p>

<a name="export"></a><h3>Export</h3>
<p>
<code>export [file]</code><br/>
<br/>
Exports the current context set to an XML <code>[file]</code>.
</p>

<a name="optimize"></a><h3>Optimize</h3>
<p>
<code>optimize</code><br/>
<br/>
Optimizes the current database structures.
</p>

<br/>
<h2>Query Commands</h2>

<a name="xpath"></a><h3>XPath</h3>
<p>
<code>xpath [query]</code><br/>
<br/>
Evaluates the specified XPath <code>[query]</code> and prints the result.
</p>

<a name="xquery"></a><h3>XQuery</h3>
<p>
<code>xquery [query]</code><br/>
<br/>
Evaluates an XQuery and prints the result.
</p>

<a name="find"></a><h3>Find</h3>
<p>
<code>find [query]</code><br/>
<br/>
Evaluate a simple keyword <code>[query]</code> and print its results.
This command is used in the simple search mode in the GUI. The following
prefixes are supported:
<ul>
<li>(none): find any tags and substrings</li>
<li><code>=  </code>find exact text nodes</li>
<li><code>@  </code>find attributes and attribute values</li>
<li><code>@= </code>find exact attribute values</li>
</ul>
</p>

<a name="cs"></a><h3>CS</h3>
<p>
<code>cs [query]</code><br/>
<br/>
Evaluates the specified XPath <code>[query]</code> and set the result
as new context set.
</p>

<a name="bash"></a><h3>Bash</h3>
<p>
<code>bash</code><br/>
<br/>
Starts the bash mode.
</p>

<br/>
<h2>Update Commands</h2>

<a name="copy"></a><h3>Copy</h3>
<p>
<code>copy [pos] [source] [target]</code><br/>
<br/>
Copy database nodes.
Evaluate the XPath 1.0 <code>[source]</code> query and insert the
resulting nodes as child nodes into the <code>[target]</code> query.
<code>[pos]</code> specifies the child position; if <code>0</code>
is specified, the nodes are inserted as last child.
The queries should be enclosed by brackets.
</p>

<a name="delete"></a><h3>Delete</h3>
<p>
<code>delete ["target"]</code>
<br/>
Delete database nodes resulting from the specified <code>[target]</code> query.
The query should be enclosed by brackets.
</p>

<a name="insert"></a><h3>Insert</h3>
<p>
<code>insert [fragment|element|attribute|text|comment|pi] [...]</code> <br/>
<br/>
Insert database nodes.
Insert a fragment or a specific node at the specified
child <code>[pos]</code> of the specified <code>[target]</code> query:
<ul>
<li><code>element [name] [pos] [target]</code>:<br/>
Insert an element with the specified tag <code>[name]</code>.</li>
<li><code>text [text] [pos] [target]</code>:<br/>
Insert a text node with the specified <code>[text]</code>.</li>
<li><code>attribute [name] [value] [target]</code>:<br/>
Insert an attribute with the specified <code>[name]</code> and <code>[value]</code></li>
<li><code>comment [text] [pos] [target]</code>:<br/>
Insert a comment with the specified <code>[text]</code>.</li>
<li><code>pi [name] [value] [pos] [target]</code>:<br/>
Insert a processing instruction with the specified
<code>[name]</code> and <code>[value]</code>.</li>
<li><code>fragment [frag] [pos] [target]</code>:<br/>
Insert an XML <code>[frag]</code>.</li>
</ul>
</p>

<a name="update"></a><h3>Update</h3>
<p>
<code>update [element|attribute|text|comment|pi] [...]</code> <br/>
<br/>
Update database nodes satisfying the specified <code>[target]</code> query.
<ul>
<li><code>element [name] [target]</code>:<br/>
Update the resulting elements with the specified tag <code>[name]</code>.</li>
<li><code>text [text] [target]</code>:<br/>
Update the resulting text nodes with the specified <code>[text]</code>.</li>
<li><code>attribute [name] [value] [target]</code>:<br/>
Update the resulting attributes with the specified <code>[name]</code>
and </code>[value]</code>.</li>
<li><code>comment [text] [target]</code>:<br/>
Update the resulting comments with the specified <code>[text]</code>.</li>
<li><code>pi [name] [value] [target]</code>:<br/>
Update the resulting processing instructions with the specified
<code>[name]</code> and <code>[value]</code>.</li>
</ul>
</p>

<br/>
<h2>General Commands</h2>

<a name="help"></a><h3>Help</h3>
<p>
<code>help [command]</code><br/>
<br/>
Get help on BaseX commands.
If <code>[command]</code> is specified, information on the specific
command is printed; otherwise, all commands are listed.
If 'all' is specified, hidden commands are included.
</p>

<a name="set"></a><h3>Set</h3>
<p>
<code>set [option] [value?]</code> <br/>
<br/>
Sets global options. The currently set values can be shown with the
<code>info</code> command. The following <code>[option]</code>s are
available and can be turned [on] or [off]:
<ul>
<li><code>debug</code>: Show internal debug info.</li>
<li><code>mainmem</code>: Use main-memory mode.</li>
</ul>
The following commands apply to the creation of new databases:<br/>&nbsp;
<ul>
<li><code>chop</code>: Chop all XML whitespace nodes.</li>
<li><code>entity</code>: Parse XML entities.</li>
<li><code>textindex</code>: : Index text nodes.</li>
<li><code>attrindex</code>: : Index attribute values.</li>
<li><code>ftindex</code>: Index full-text</li>
</ul>

The following commands apply to querying:<br/>&nbsp;
<ul>
<li><code>info [all]</code>: Show (all) info on query evaluation.</li>
<li><code>serialize</code>: Turn serialization of query results on/off.</li>
<li><code>xmloutput</code>: Serialize query results as XML.</li>
<li><code>runs [nr]</code>: Set the number of query runs.</li>
</ul>
Have a look into the <a href='faq.php'>FAQ</a> to find
more information on the available options.
</p>

<a name="exit"></a><h3>Exit</h3>
<p>
<code>exit/quit </code> <br/>
<br/>
Leave the console mode of BaseX.
</p>

<? include("inc/footer.inc"); ?>
