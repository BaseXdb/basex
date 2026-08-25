(:~
 : Panels of the databases view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/db-panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Page the deep links of the panels refer to. :)
declare %private variable $panels:CAT := 'databases';

(:~
 : Creates the contents of the databases panel: the databases to choose from, and the backups
 : that belong to no database of their own.
 : @param  $sort  sort key of the database list
 : @param  $page  current page of the database list
 : @param  $name  selected database
 : @return panel contents
 :)
declare function panels:databases(
  $sort  as xs:string,
  $page  as xs:integer,
  $name  as xs:string?
) as element()+ {
  let $db-names := db:list()
  return (
    (: the page is the list of databases: a heading would only repeat the navigation :)
    <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
      {
        let $headers := (
          (: the date is the longest value of a known length, and the count needs room for its
             header and sort arrow; the name gives up what it does not need :)
          { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '35%' },
          { 'key': 'resources', 'label': 'Count', 'type': 'number', 'order': 'desc',
            'width': '18%' },
          { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc', 'width': '16%' },
          { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
            'width': '31%' }
        )
        let $databases :=
          for $db in utils:slice(db:list-details(), $page, $sort)
          return {
            'name': panels:select($db, { 'name': $db }, $db = $name),
            'resources': $db/@resources,
            'size': $db/@size,
            'date': $db/@modified-date
          }
        (: a dropped database stays listed as long as a backup of it exists, so that it can
           be selected and restored :)
        let $dropped := (
          for $backup in db:backups()
          where matches($backup, $utils:BACKUP-REGEX)
          group by $db := replace($backup, $utils:BACKUP-REGEX, '$1')
          where $db and not($db-names = $db)
          return {
            'name': panels:select($db, { 'name': $db }, $db = $name),
            'size': (),
            (: the backups are listed with the most recent one first :)
            'date': replace(head($backup), $utils:BACKUP-REGEX, '$2T$3:$4:$5Z')
          }
        )
        let $buttons := (
          <button type='button' onclick='showDialog("create")'>New…</button>,
          form:button('databases/optimize', 'Optimize', 'CHECK'),
          form:button('databases/drop', 'Drop', ('CHECK', 'CONFIRM')),
          form:button('databases/backups-create', 'Back up', 'CHECK'),
          form:button('databases/backups-restore', 'Restore', ('CHECK', 'CONFIRM'))
        )
        let $options := {
          'sort': $sort,
          'page': $page,
          'count': count($db-names) + count($dropped),
          (: nothing but the buttons above the list, and they stay in reach :)
          'sticky': ()
        }
        return table:create($headers, ($databases, $dropped), $buttons, {}, $options)
      }
    </form>,

    (: a new database is named and configured in one dialog; the defaults are the ones a
       database gets when nothing is chosen :)
    form:dialog('create', 'Create Database', 'databases/create', false(), (
      form:field('Name:', <input type='text' name='name' autofocus='' required=''/>),
      (: the initial content is addressed on the server: a database that is created without
         it is empty, and is filled by the Add dialog :)
      form:field('Input:', <input type='text' name='input' class='wide'
                                  placeholder='File, directory, archive or URL'/>),
      form:parsing-fields(),
      form:language-field('en'),
      (: how the input is parsed, and what is indexed: two columns, as one would be a list
         that is longer than the screen :)
      <div class='field-columns'>{
        <div>{ form:parsing-options(()) }</div>,
        <div>{ form:index-options(('textindex', 'attrindex'), true()) }</div>
      }</div>
    ))
  )
};

(:~
 : Creates the contents of the database panel: one level of the selected database, its backups,
 : and the index configuration that its next optimization applies.
 : @param  $name      selected database
 : @param  $sort      sort key of the resource list
 : @param  $page      current page of the resource list
 : @param  $resource  selected resource
 : @param  $dir       directory that is listed; empty string for the root of the database
 : @param  $filter    filter for the resource paths; empty string to list the directory
 : @return panel contents; empty if no database is selected
 :)
declare function panels:database(
  $name      as xs:string?,
  $sort      as xs:string,
  $page      as xs:integer,
  $resource  as xs:string?,
  $dir       as xs:string,
  $filter    as xs:string
) as element()* {
  if (not($name)) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else if (not(db:exists($name))) {
    (: the name is only known from the backups of the dropped database :)
    <h2>{ 'Database: ' || $name }</h2>,
    <div class='note'>The database does not exist; one of its backups can be restored.</div>
  } else {
    panels:resource-list($name, $sort, $page, $resource, $dir, $filter),
    panels:add-dialog($name),
    panels:optimize-dialog($name),
    (: the new name of the database is asked for; the chosen action decides what is done with it :)
    <form method='post' autocomplete='off' id='database-form'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='newname' id='database-newname'/>
    </form>
  }
};

(:~
 : Creates the list of one level of a database, or of what a filter finds in it.
 : @param  $name      database
 : @param  $sort      table sort key
 : @param  $page      current page
 : @param  $resource  selected resource
 : @param  $dir       shown directory
 : @param  $filter    resource filter
 : @return list
 :)
declare %private function panels:resource-list(
  $name      as xs:string,
  $sort      as xs:string,
  $page      as xs:integer,
  $resource  as xs:string?,
  $dir       as xs:string,
  $filter    as xs:string
) as element(form) {
  <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
    <input type='hidden' name='name' value='{ $name }'/>
    {
      let $headers := (
        { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '47%' },
        { 'key': 'type', 'label': 'Type', 'width': '10%' },
        { 'key': 'size', 'label': 'Size', 'type': 'number', 'order': 'desc', 'width': '14%' },
        { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
          'width': '29%' }
      )
      (: one level of the database, directories first; a level is what a database of many
         resources is browsed by, and what its total is counted over. A filter looks past the
         levels: what it matches is the path of a resource, wherever it is stored :)
      let $level := if ($filter) {
        let $lower := lower-case($filter)
        for $entry in db:list-details($name)
        where contains(lower-case($entry), $lower)
        return $entry
      } else {
        for $entry in db:dir($name, $dir)
        order by boolean($entry/self::dir) descending, string($entry) collation '?lang=en'
        return $entry
      }
      let $entries :=
        for $entry in utils:slice($level, $page, $sort)
        let $label := string($entry)
        let $directory := boolean($entry/self::dir)
        (: a directory is entered, a resource is opened; the path of a directory ends with a
           slash, so it is never taken for the resource that is selected. What a filter finds
           is named by its full path, as it is not what the shown level holds :)
        let $path := if ($filter) then $label else $dir || $label || '/'[$directory]
        return {
          'name': if ($directory) {
            fn() { panels:enter($label || '/', $name, $path) }
          } else {
            panels:select($label, { 'name': $name, 'resource': $path }, $path = $resource)
          },
          (: the checkbox submits the full path, which is what an action addresses :)
          'resource': $path,
          'type': $entry/@type,
          'size': $entry/@size,
          'date': $entry/@modified-date
        }
      (: what applies to the database comes first; the row wraps only if it has to :)
      let $buttons := (
        <button type='button' onclick='enterDbDir("..")'
                title='Go to the parent directory'>{
          attribute disabled { }[not($dir)], '..'
        }</button>,
        <button type='button' onclick='renameDatabase()'>Rename…</button>,
        <button type='button' onclick='copyDatabase()'>Copy…</button>,
        <button type='button' onclick='showDialog("optimize")'>Optimize…</button>,
        <button type='button' onclick='showDialog("add")'>Add…</button>,
        form:button('databases/resource-delete', 'Delete', ('CHECK', 'CONFIRM')),
        (: the filter is a control of the list, and shares the row of its buttons :)
        <input type='text' id='resource-filter' class='smallinput' placeholder='Filter'
               title='Find resources of the database, wherever they are stored'
               value='{ $filter }' onkeyup='filterResources(event.key);'/>
      )
      let $options := {
        'sort': $sort,
        'page': $page,
        (: the entries of one level are known, so the total is what they are counted by :)
        'count': if ($sort) { () } else { count($level) },
        'select': 'resource',
        (: the database and what can be done with it stay in view while its resources scroll :)
        'sticky': panels:database-heading($name, $dir)
      }
      return table:create($headers, $entries, $buttons, { 'name': $name }, $options)
    }
  </form>
};

(:~
 : Creates the heading of a database: its name, and the path of the shown level.
 : @param  $name  database
 : @param  $dir   shown directory
 : @return heading
 :)
declare %private function panels:database-heading(
  $name  as xs:string,
  $dir   as xs:string
) as element(h2) {
  <h2>{
    'Database: ',
    (: the name returns to the root of the database and clears the selected resource, and with
       it the document that is shown :)
    <a href='{ web:create-url($panels:CAT, { 'name': $name }) }'
       onclick='enterDbDir(""); selectResource(""); return false;'>{ $name }</a>,
    (: every step of the path enters the level it names; the document stays open :)
    let $steps := tokenize($dir, '/')[.]
    for $step at $pos in $steps
    return ('/', panels:enter($step, $name,
      string-join(subsequence($steps, 1, $pos), '/') || '/'))
  }</h2>
};

(:~
 : Creates the dialog that adds resources to a database. The resources are stored under their
 : own names; how they are parsed is chosen here. Files are uploaded by the browser, an input is
 : read by the server: either of them adds resources, and both of them may be supplied at once.
 : @param  $name  database
 : @return dialog
 :)
declare %private function panels:add-dialog(
  $name  as xs:string
) as element(dialog) {
  form:dialog('add', 'Add Resources', 'databases/put', true(), (
    <input type='hidden' name='name' value='{ $name }'/>,
    (: what is added, and how it is parsed: two columns, as one would be a list that is
       longer than the screen :)
    <div class='field-columns'>{
      <div>{
        <h3>Resources</h3>,
        form:field('Files:', <input type='file' name='files' multiple='multiple'/>),
        form:field('Input:', <input type='text' name='input' class='wide'
                                    placeholder='File, directory, archive or URL'
                                    oninput='deriveTarget(this);'/>),
        (: the path the input is stored under: it replaces what is found there, and an
           empty one would address the database as a whole :)
        form:field('Target:', <input type='text' name='target' id='add-target' class='wide'/>),
        form:field('Binary Storage:', form:checkbox('binary', 'true', false(), '')),
        form:parsing-fields()
      }</div>,
      <div>{ form:parsing-options(()) }</div>
    }</div>
  ))
};

(:~
 : Creates the dialog that optimizes a database. The index configuration is not a report: it is
 : what the next optimization applies.
 : @param  $name  database
 : @return dialog
 :)
declare %private function panels:optimize-dialog(
  $name  as xs:string
) as element(dialog) {
  (: one read of the database properties supplies both the index flags and the language :)
  let $info := db:info($name)
  return form:dialog('optimize', 'Optimize Database', 'databases/optimize-db', false(), (
    <input type='hidden' name='name' value='{ $name }'/>,
    form:language-field($info//language),
    form:checkbox('all', 'true', false(), 'Full optimization'),
    form:index-options($info//*[text() = 'true']/name(), false())
  ))
};

(:~
 : Creates the contents of the backups panel: the backups of the selected database, and the ones
 : of the general data. Both are recovery corners, so the panel opens on demand.
 : @param  $name  selected database
 : @return panel contents
 :)
declare function panels:backups(
  $name  as xs:string?
) as element()+ {
  (: a selected database supersedes the general backups: its own are what is asked for :)
  if ($name) {
    <h2>{ 'Backup: ' || $name }</h2>,
    panels:backup-section($name)
  } else {
    <h2>Backups</h2>,
    <div class='note'>
      Comprises
      <a target='_blank' href='https://docs.basex.org/main/User_Management'>users</a>,
      <a target='_blank'
         href='https://docs.basex.org/main/Job_Functions#services'>services</a>, and
      <a target='_blank' href='https://docs.basex.org/main/Store_Functions'>stores</a>.
    </div>,
    panels:backup-section('')
  }
};

(:~ Number of characters of an index entry that are listed. :)
declare %private variable $panels:PREVIEW := 200;

(:~ Indexes that can be browsed, how they are labeled, the database option that builds them,
    and what lists their entries: what a database is built of comes before what it holds. The
    name indexes are part of every database, so they have no option of their own. An index that
    holds values looks a prefix up; the others are listed and filtered. :)
declare %private variable $panels:INDEXES := (
  { 'name': 'element-name', 'label': 'Element Names',
    'entries': fn($db, $prefix) { index:element-names($db)[starts-with(., $prefix)] } },
  { 'name': 'attribute-name', 'label': 'Attribute Names',
    'entries': fn($db, $prefix) { index:attribute-names($db)[starts-with(., $prefix)] } },
  { 'name': 'text', 'label': 'Texts', 'option': 'textindex',
    'entries': index:texts#2 },
  { 'name': 'attribute', 'label': 'Attributes', 'option': 'attrindex',
    'entries': index:attributes#2 },
  { 'name': 'token', 'label': 'Tokens', 'option': 'tokenindex',
    'entries': fn($db, $prefix) { index:tokens($db)[starts-with(., $prefix)] } },
  { 'name': 'full-text', 'label': 'Full-Text', 'option': 'ftindex',
    'entries': ft:tokens#2 }
);

(:~
 : Returns the index that is browsed under the supplied name.
 : @param  $index  name of the index
 : @return index; empty sequence if the name is unknown
 :)
declare function panels:index-type(
  $index  as xs:string
) as map(*)? {
  $panels:INDEXES[?name = $index]
};

(:~
 : Creates the contents of the index panel: the entries of one index of the selected database,
 : with the number of times each of them occurs.
 : @param  $name    selected database
 : @param  $index   index that is listed
 : @param  $prefix  prefix of the entries to be listed
 : @param  $sort    sort key of the entry list
 : @param  $page    current page of the entry list
 : @return panel contents; empty if no existing database is selected
 :)
declare function panels:index(
  $name    as xs:string?,
  $index   as xs:string,
  $prefix  as xs:string,
  $sort    as xs:string,
  $page    as xs:integer
) as element()* {
  if (not($name) or not(db:exists($name))) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else {
    <form autocomplete='off' action='javascript:void(0);' data-sort='{ $sort }'
          data-page='{ $page }'>
      {
        (: which index is browsed, what is done with it, and where its entries are entered:
           controls of the list, so they share the row of its buttons and are pinned with them.
           The buttons address the index as a whole; they submit its name, not a selection,
           and post to a form of their own :)
        (: an unknown name falls back to the first index, which every database has :)
        let $type := panels:index-type($index) otherwise head($panels:INDEXES)
        let $controls := (
          <select id='index-select' onchange='refreshIndex();'>{
            for $entry in $panels:INDEXES
            return element option {
              attribute value { $entry?name },
              attribute selected { }[$entry?name = $index],
              $entry?label
            }
          }</select>,
          (: an index that every database has is neither created nor dropped :)
          let $indexable := exists($type?option)
          return (
            <button form='index-form' formaction='databases/index-create'>{
              attribute disabled { }[not($indexable)], 'Create'
            }</button>,
            <button form='index-form' formaction='databases/index-drop'
                    onclick='return confirmAction(this, "Drop");'>{
              attribute disabled { }[not($indexable)], 'Drop'
            }</button>
          ),
          <input type='text' id='index-prefix' class='smallinput' placeholder='Prefix'
                 title='Entries that start with the supplied string'
                 value='{ $prefix }' onkeyup='filterIndex(event.key);'/>
        )
        let $headers := (
          { 'key': 'entry', 'label': 'Entry', 'width': '75%' },
          { 'key': 'count', 'label': 'Count', 'type': 'number', 'order': 'desc' }
        )
        (: what the panel is and what it offers; the same head is shown if the list cannot be :)
        let $head := (<h2>Indexes</h2>, <div class='buttons'>{ $controls }</div>)
        (: an index that a database does not have is reported, not raised :)
        return try {
          let $entries := $type?entries($name, $prefix)
          (: one entry more than the page needs: it is what tells that there are further ones :)
          let $max := config:get($config:MAXROWS) * $page + 1
          let $shown := subsequence($entries, 1, $max)
          let $note := <div class='note'>{
            'More entries exist; supply a prefix to narrow the list.'
          }</div>[count($shown) = $max]
          (: the entry that was read to detect the further ones is not one of them :)
          return table:create($headers,
            subsequence($shown, 1, $max - 1) !
              { 'entry': panels:entry(.), 'count': @count },
            (), {}, {
              'sort': $sort, 'page': $page,
              (: the head stays in view while the entries scroll. The controls are no actions
                 on the entries, so the list has nothing to check :)
              'sticky': $head,
              'below': $note
            })
        } catch * {
          <div class='sticky'>{ $head, <div class='note warn'>{ $err:description }</div> }</div>
        }
      }
    </form>,
    (: the buttons of the panel are no submits of the list: browsing it must not build an
       index, so the values they post are kept in a form that nothing else submits :)
    <form method='post' autocomplete='off' id='index-form'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='index' value='{ $index }'/>
    </form>
  }
};

(:~
 : Returns what an index entry is listed as. A text node holds what it holds: a code block
 : spans lines, and the whitespace between two elements is an entry of its own. What is listed
 : is one line of it, so that the table stays a table, and an entry that holds nothing but
 : whitespace is named instead of shown.
 : @param  $entry  index entry
 : @return label
 :)
declare %private function panels:entry(
  $entry  as element()
) as xs:string {
  let $text := normalize-space($entry)
  return if ($text) { utils:chop($text, $panels:PREVIEW) } else { '(whitespace)' }
};

(:~
 : Creates the contents of the information panel: the index configuration that the next
 : optimization of the selected database applies, and its properties.
 : @param  $name  selected database
 : @return panel contents; empty if no existing database is selected
 :)
declare function panels:information(
  $name  as xs:string?
) as element()* {
  if (not($name) or not(db:exists($name))) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else {
    (: a report: what can be changed is asked for by the Optimize dialog :)
    <h2>Information</h2>,
    table:properties(db:info($name))
  }
};

(:~
 : Creates the contents of the resource panel: the actions that apply to the shown document,
 : the reason why it cannot be edited, and the field for querying it.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @param  $document  document properties, as returned by panels:document
 : @return panel contents; empty if no resource is selected
 :)
declare function panels:resource(
  $name      as xs:string?,
  $resource  as xs:string?,
  $document  as map(*)
) as element()* {
  if (not($document?exists)) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else {
    <h2>{ 'Resource: ' || $resource }</h2>,
    <form method='post' autocomplete='off'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='resource' value='{ $resource }'/>
      <div class='buttons'>{
        (: enabled by the client once it knows that the document can be edited :)
        <button type='button' id='save-resource' onclick='saveResource()'
                disabled=''>Save</button>,
        <button type='button' onclick='copyResource()'>Copy</button>,
        (: a query on a large document takes time, and can be given up on :)
        if ($document?xml) {
          <button type='button' id='stop' onclick='stopQuery()' disabled=''>Stop</button>
        },
        <button type='button' onclick='renameResource()'>Rename…</button>,
        form:button('db-download', 'Download'),
        <button type='button' onclick='replaceResource()'>Upload…</button>,
        <label>{
          <input type='checkbox' id='indent' onchange='indentChanged()'/>, ' Indent'
        }</label>
      }</div>
    </form>,
    (: the line is reserved: the client writes to it as well :)
    <div id='note' class='note{ ' warn'[$document?note] }'>{ $document?note }</div>,
    if ($document?xml) {
      <input type='text' class='query' name='input' id='input'
             placeholder='Enter your query…' onkeyup='queryResource(false)'/>
    },

    (: the new path of the resource is asked for and submitted :)
    <form method='post' action='databases/resource-rename' autocomplete='off' id='rename-form'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='resource' value='{ $resource }'/>
      <input type='hidden' name='target' id='rename-target'/>
    </form>,
    <form method='post' action='databases/replace' enctype='multipart/form-data'
          autocomplete='off' onsubmit='uploading(this);'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='resource' value='{ $resource }'/>
      <input type='file' name='files' id='replace-file' hidden=''
             onchange='this.form.requestSubmit();'/>
    </form>
  }
};

(:~
 : Returns the document that is shown in the editor, and what can be done with it. The value is
 : serialized with a bounded limit: db:list-details/@size counts nodes, not characters, so the
 : length of the shown text is only known once the text has been produced.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @return properties: whether the resource exists, is XML, is truncated and can be edited,
 :         the reason why it cannot be edited, and its text
 :)
declare function panels:document(
  $name      as xs:string?,
  $resource  as xs:string?
) as map(*) {
  if (not($name and $resource and db:exists($name, $resource))) {
    { 'exists': false(), 'text': '' }
  } else {
    let $type := db:type($name, $resource)
    let $max := config:get($config:MAXCHARS)
    let $value := if ($type = 'binary') {
      db:get-binary($name, $resource)
    } else if ($type = 'value') {
      db:get-value($name, $resource)
    } else {
      db:get($name, $resource)
    }
    let $text := serialize($value, {
      'method': if ($type = 'xml') then 'xml' else 'basex',
      'limit': $max * 2 + 1
    })
    let $truncated := string-length($text) > $max
    let $editable := $type = 'xml' and not($truncated)
    return {
      'exists'   : true(),
      'xml'      : $type = 'xml',
      'truncated': $truncated,
      'editable' : $editable,
      'text'     : if ($editable) then $text else substring($text, 1, $max),
      (: reason why the resource cannot be edited; extended by the client for query results :)
      'note': string-join((
        'Read-only ' || (
          if ($type = 'xml') then '(too large for editing)' else '(only XML can be edited)'
        ),
        ', and truncated: download it to see the full content.'[$truncated]
      ))[not($editable)]
    }
  }
};

(:~
 : Creates a link that enters a directory of a database. The reference is a deep link naming
 : the directory in full; following it in place leaves the other panels alone.
 : @param  $label  link label
 : @param  $name   database
 : @param  $dir    directory the link refers to
 : @return function creating the link
 :)
declare %private function panels:enter(
  $label  as xs:string,
  $name   as xs:string,
  $dir    as xs:string
) as element(a) {
  <a href='{ web:create-url($panels:CAT, { 'name': $name, 'dir': $dir }) }'
     data-dir='{ $dir }'
     onclick='enterDbDir(this.dataset.dir); return false;'>{ $label }</a>
};

(:~
 : Creates a link that selects a database or one of its resources. The reference is a deep link
 : naming the whole selection; following it in place leaves the other panels alone.
 : @param  $label     link label
 : @param  $params    selection the link refers to
 : @param  $selected  whether the link refers to what is shown
 : @return function creating the link
 :)
declare %private function panels:select(
  $label     as xs:string,
  $params    as map(*),
  $selected  as xs:boolean
) as fn() as element(a) {
  fn() {
    <a href='{ web:create-url($panels:CAT, $params) }'>{
      attribute class { 'selected' }[$selected],
      if (map:contains($params, 'resource')) {
        attribute data-select { $params?resource },
        attribute onclick { 'selectResource(this.dataset.select); return false;' }
      } else {
        attribute data-select { $params?name },
        attribute onclick { 'selectDatabase(this.dataset.select); return false;' }
      },
      $label
    }</a>
  }
};

(:~
 : Creates the backups of a database: a facet of it, not a sibling of its resources. The two
 : sections of the view never show the same backups, so one function serves both.
 : @param  $name  database; empty string for the backups of the general data
 : @return the forms that create, upload and list the backups
 :)
declare %private function panels:backup-section(
  $name  as xs:string
) as element()+ {
  (: one section is shown at a time, so its fields need no names of their own.
     One form for the whole section, so that its actions share a single row of buttons.
     Every button carries its own 'formaction' :)
  <form method='post' autocomplete='off'>
    <input type='hidden' name='name' value='{ $name }'/>
    {
        let $headers := (
          (: a backup is named after its timestamp and a size never grows beyond four digits
             and a unit: both are of a known length and take no more than they need. The
             comment is free text, and is given whatever is left :)
          { 'key': 'backup', 'label': 'Name', 'type': 'dynamic', 'order': 'desc',
            'width': '11.5rem' },
          { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'width': '4.5rem' },
          { 'key': 'comment', 'label': 'Comment' }
        )
        let $entries :=
          for $backup in db:backups($name)
          return {
            (: the name is the download: a column that repeats it as a link adds nothing :)
            'backup': fn() {
              html:link(substring-after($backup, $name || '-'),
                'backup/' || encode-for-uri($backup) || '.zip')
            },
            'size': $backup/@size,
            'comment': $backup/@comment
          }
        let $buttons := (
          <button type='button' onclick='showDialog("backup")'>{
            (: there is nothing to back up if the name is only known from a backup :)
            attribute disabled { }[$name][not(db:exists($name))],
            'Back up…'
          }</button>,
          <button type='button' onclick='chooseUpload("upload-backups")'>Upload…</button>,
          form:button('databases/backup-restore', 'Restore', ('CHECK', 'CONFIRM')),
          form:button('databases/backup-drop', 'Drop', ('CHECK', 'CONFIRM'))
        )
        return table:create($headers, $entries, $buttons, { 'name': $name })
      }
  </form>,

  form:dialog('backup', 'Create Backup', 'databases/backup-create', false(), (
    <input type='hidden' name='name' value='{ $name }'/>,
    form:field('Comment:', <input type='text' name='comment' placeholder='optional' autofocus=''/>),
    form:field('Compress:', form:checkbox('compress', 'true', true(), ''))
  )),

  (: the file chooser is opened by the Upload button and submits what it collects. An upload
     lands in the database directory, so the server checks that it belongs where it is put :)
  <form method='post' action='backup-upload' enctype='multipart/form-data' autocomplete='off'
        onsubmit='uploading(this);'>
    <input type='hidden' name='name' value='{ $name }'/>
    <input type='file' name='files' id='upload-backups' multiple='multiple' hidden=''
           onchange='this.form.requestSubmit();'/>
  </form>
};
