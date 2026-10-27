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
 : @param  $page  number of shown pages of the database list
 : @param  $name  selected database
 : @return panel contents
 :)
declare function panels:databases(
  $sort  as xs:string,
  $page  as xs:integer,
  $name  as xs:string?
) as element()+ {
  let $db-names := db:list()
  let $presort := 'name'
  let $sort := $sort[.] otherwise $presort
  return (
    (: the page is the list of databases: a heading would only repeat the navigation :)
    <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
      {
        let $headers := (
          (: the values of a known format get the width they need; the name takes the rest :)
          { 'key': 'name', 'label': 'Name' },
          { 'key': 'resources', 'label': 'Count', 'type': 'number', 'order': 'desc',
            'width': 'calc(8ch + 0.5rem)' },
          { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc',
            'width': 'calc(9ch + 0.5rem)' },
          { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
            'width': 'calc(11ch + 0.5rem)' },
          (: what Restore falls back to, and where a backup that was just written shows up :)
          { 'key': 'backup', 'label': 'Backup', 'type': 'dateTime', 'order': 'desc',
            'width': 'calc(11ch + 0.5rem)', 'link': fn($entry, $date) {
              html:action($date, 'showBackups', { 'name': $entry?db }, {
                'href': web:create-url($panels:CAT,
                  { 'name': $entry?db, 'show': 'properties', 'tab': 'backups-panel' })
              })
            } }
        )
        (: the date of the latest backup of each database; the backups are listed with the most
           recent one first :)
        let $backups := map:merge(
          for $backup in db:backups()
          group by $db := string($backup/@database)
          where $db
          return { $db: string(head($backup)/@date) }
        )
        (: a dropped database stays listed as long as a backup of it exists, so that it can
           be selected and restored :)
        let $dropped := (
          for key $db value $backup in $backups
          where not($db-names = $db)
          return {
            'name': panels:select($db, $db = $name),
            'db': $db,
            'backup': $backup
          }
        )
        (: dropped databases are appended; if there are any, the table sorts all entries :)
        let $presorted := $presort[empty($dropped)]
        let $databases :=
          for $db in utils:slice(db:list-details(), $page, $sort, $presorted)
          return {
            'name': panels:select($db, $db = $name),
            'resources': $db/@resources,
            'size': $db/@size,
            'date': $db/@modified-date,
            'db': string($db),
            'backup': $backups?(string($db))
          }
        let $buttons := (
          <button type='button' onclick='showDialog("create")'
                  title='Create a new database'>New…</button>,
          form:button('databases/optimize', 'Optimize', 'CHECK',
            title := 'Update the outdated indexes and statistics of the selected databases'),
          form:button('databases/drop', 'Drop', ('CHECK', 'CONFIRM'),
            title := 'Delete the selected databases permanently'),
          form:button('databases/backups-create', 'Back up', 'CHECK',
            title := 'Write a zipped backup of each selected database'),
          form:button('databases/backups-restore', 'Restore', ('CHECK', 'CONFIRM'),
            title := 'Restore each selected database from its latest backup'),
          <button type='button' onclick='openDialog("storage")'
                  title='Show the backups of the users, services, and stores, and the stored data'>{
            'Storage…'
          }</button>
        )
        let $options := {
          'sort': $sort,
          'presort': $presorted,
          'page': $page,
          'count': count($db-names) + count($dropped),
          (: the heading and the buttons stay in reach while the list scrolls :)
          'sticky': <h2>Databases</h2>,
          'noun': 'database',
          'empty': 'There are no databases yet. Create one with New…'
        }
        return table:create($headers, ($databases, $dropped), $buttons, options := $options)
      }
    </form>,

    (: a new database is named and configured in one dialog; the defaults are the ones a
       database gets when nothing is chosen :)
    form:dialog('create', 'New Database', 'databases/create', true(), (
      (: the database and what is added, how it is parsed, the value indexes and the full-text
         index: a column each, of similar length, so the dialog fits on the screen :)
      <div class='field-columns'>{
        <div>{
          <h3>Database</h3>,
          form:field('Name:', <input type='text' name='name' autofocus='' required=''/>),
          (: the initial content is uploaded, or addressed on the server; a database that is
             created without it is empty :)
          form:field('Files:', <input type='file' name='files' multiple='multiple'/>),
          form:field('Input:', <input type='text' name='input'
                                      placeholder='File, directory, archive or URL'/>),
          form:parsing-fields(),
          form:checkbox('binary', 'true', false(), 'Binary Storage',
            title := 'Store the uploaded files as binary data, without parsing them')
        }</div>,
        <div>{ form:parsing-options(()) }</div>,
        (: the defaults of the server :)
        form:index-fields(('textindex', 'attrindex'), { 'language': 'English' })
      }</div>,
      (: incremental indexing is enabled by default :)
      form:update-fields('updindex')
    ))
  )
};

(:~
 : Creates the contents of the database panel: one level of the selected database, and the
 : dialogs that act on it.
 : @param  $name      selected database
 : @param  $sort      sort key of the resource list
 : @param  $page      number of shown pages of the resource list
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
  if (not($name)) then (
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  ) else if (not(db:exists($name))) then (
    (: the name is only known from the backups of the dropped database :)
    <h2>Database: { $name }</h2>,
    <div class='note'>The database does not exist; one of its backups can be restored.</div>,
    <div class='buttons'>
      <button type='button' onclick='openDialog("properties", "backups-panel")'
              title='Show the backups of the database'>Backups…</button>
    </div>
  ) else (
    panels:resource-list($name, $sort, $page, $resource, $dir, $filter),
    panels:add-dialog($name),
    (: the new name of the database is asked for; the chosen action decides what is done with it :)
    form:prompt('database-newname', 'newname', (),
      <input type='hidden' name='name' value='{ $name }'/>)
  )
};

(:~
 : Creates the list of one level of a database, or of what a filter finds in it.
 : @param  $name      database
 : @param  $sort      table sort key
 : @param  $page      number of shown pages
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
  let $presort := 'name'
  let $sort := $sort[.] otherwise $presort
  return <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
    <input type='hidden' name='name' value='{ $name }'/>
    {
      let $headers := (
        (: the values of a known format get the width they need; the name takes the rest :)
        { 'key': 'name', 'label': 'Name' },
        { 'key': 'type', 'label': 'Type', 'width': 'calc(6ch + 0.5rem)' },
        { 'key': 'size', 'label': 'Size', 'type': 'number', 'order': 'desc',
          'width': 'calc(9ch + 0.5rem)' },
        { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
          'width': 'calc(11ch + 0.5rem)' }
      )
      (: one level of the database, directories first; a level is what a database of many
         resources is browsed by, and what its total is counted over. A filter looks past the
         levels: what it matches is the path of a resource, wherever it is stored :)
      let $level := if ($filter) then (
        let $lower := lower-case($filter)
        for $entry in db:list-details($name)
        where contains(lower-case($entry), $lower)
        return $entry
      ) else (
        for $entry in db:dir($name, $dir)
        order by boolean($entry/self::dir) descending, string($entry) collation '?lang=en'
        return $entry
      )
      let $entries :=
        for $entry in utils:slice($level, $page, $sort, $presort)
        let $label := string($entry)
        let $directory := boolean($entry/self::dir)
        (: a directory is entered, a resource is opened; the path of a directory ends with a
           slash, so it is never taken for the resource that is selected. What a filter finds
           is named by its full path, as it is not what the shown level holds :)
        let $path := if ($filter) then $label else $dir || $label || '/'[$directory]
        return {
          'name': if ($directory) then (
            fn() { panels:enter($label || '/', $name, $path) }
          ) else (
            html:select($label, $panels:CAT, { 'name': $name, 'resource': $path },
              $path = $resource, 'resource', 'selectResource')
          ),
          (: the checkbox submits the full path, which is what an action addresses :)
          'resource': $path,
          'type': $entry/@type,
          'size': $entry/@size,
          'date': $entry/@modified-date
        }
      (: the resources are acted on here; the database as a whole in its properties dialog :)
      let $buttons := (
        <button type='button' onclick='enterDbDir("..")'
                title='Go to the parent directory'>{
          attribute disabled { }[not($dir)], '..'
        }</button>,
        <button type='button' onclick='openDialog("properties")'
                title='Show the options, indexes, backups and information of the database'>{
          'Properties…'
        }</button>,
        <button type='button' onclick='showDialog("add")'
                title='Add files, a directory, an archive or a URL'>Add…</button>,
        form:button('resources-download', 'Download', 'CHECK',
          title := 'Download the selected resources'),
        form:button('databases/resource-delete', 'Delete', ('CHECK', 'CONFIRM'),
          title := 'Delete the selected resources'),
        (: the filter is a control of the list, and shares the row of its buttons :)
        <input type='text' id='resource-filter' class='smallinput' placeholder='Filter, e.g. .xml'
               title='Find resources of the database, wherever they are stored'
               value='{ $filter }' data-filter='filterResources'/>
      )
      let $options := {
        'sort': $sort,
        'presort': $presort,
        'page': $page,
        (: the entries of one level are known, so the total is what they are counted by :)
        'count': count($level),
        'select': 'resource',
        'noun': 'resource',
        'empty': if ($filter) then 'No resources match the filter.'
          else if ($dir) then 'The directory is empty.'
          else 'The database is empty. Add resources with Add…',
        (: the database and what can be done with it stay in view while its resources scroll :)
        'sticky': panels:database-heading($name, $dir, $filter)
      }
      return table:create($headers, $entries, $buttons, { 'name': $name }, $options)
    }
  </form>
};

(:~
 : Creates the heading of a database: its name, and the path of the shown level.
 : @param  $name  database
 : @param  $dir     shown directory
 : @param  $filter  filter for the resource paths
 : @return heading
 :)
declare %private function panels:database-heading(
  $name    as xs:string,
  $dir     as xs:string,
  $filter  as xs:string
) as element(h2) {
  <h2>{
    'Database: ',
    (: below the root, or with a filter, the name returns to the root of the database; the
       document stays open :)
    if ($dir or $filter) then (
      <a href='{ web:create-url($panels:CAT, { 'name': $name }) }'
         onclick='enterDbDir(""); return false;'>{ $name }</a>
    ) else (
      text { $name }
    ),
    (: every step of the path enters the level it names; the document stays open :)
    let $steps := tokenize($dir, '/')[.]
    for $step at $pos in $steps
    return ('/', panels:enter($step, $name,
      string-join(subsequence($steps, 1, $pos), '/') || '/'))
  }</h2>
};

(:~
 : Creates the dialog that adds resources to a database.
 : @param  $name  database
 : @return dialog
 :)
declare %private function panels:add-dialog(
  $name  as xs:string
) as element(dialog) {
  (: the resources are stored under their own names; how they are parsed is chosen here. Files
     are uploaded by the browser, an input is read by the server: either of them adds resources,
     and both of them may be supplied at once :)
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
        form:parsing-fields(),
        form:checkbox('binary', 'true', false(), 'Binary Storage',
          title := 'Store the uploaded files as binary data, without parsing them')
      }</div>,
      <div>{ form:parsing-options(()) }</div>
    }</div>
  ))
};

(:~
 : Creates the tab bar of a dialog and the blocks that its tabs show.
 : @param  $dialog  name of the dialog
 : @param  $tabs    tabs: the id of the block each of them shows, its label, and its contents
 : @return tab bar and blocks
 :)
declare %private function panels:tabs(
  $dialog  as xs:string,
  $tabs    as map(*)+
) as element(div)+ {
  (: the selected tab is marked, and made the only one reached with Tab, by showTab :)
  <div class='tabs' role='tablist' onkeydown='tabKey("{ $dialog }", event)'>{
    for $tab in $tabs
    return <button type='button' class='tab' role='tab'
                   id='{ $tab?id }-tab' aria-controls='{ $tab?id }'
                   onclick='showTab("{ $dialog }", "{ $tab?id }")'>{ $tab?label }</button>
  }</div>,
  for $tab in $tabs
  return <div id='{ $tab?id }' role='tabpanel' aria-labelledby='{ $tab?id }-tab'>{
    $tab?contents
  }</div>
};

(:~
 : Creates the properties dialog of the selected database.
 : @param  $name  selected database
 : @return dialog
 :)
declare function panels:properties-dialog(
  $name  as xs:string?
) as element(dialog) {
  (: outside the panels, which are replaced; once another database is chosen, the contents are
     requested when the dialog is opened; see openDialog :)
  <dialog id='properties-dialog' class='tabbed'>{
    panels:properties($name, 'element-name', '')
  }</dialog>
};

(:~
 : Creates the contents of the properties dialog: everything that concerns a database as a whole.
 : @param  $name    selected database
 : @param  $index   index that is listed
 : @param  $prefix  prefix of the listed index entries
 : @return contents; empty if no database is selected
 :)
declare function panels:properties(
  $name    as xs:string?,
  $index   as xs:string,
  $prefix  as xs:string
) as element()* {
  (: a database that is only known from its backups offers nothing but them. A tab only shows
     another block: what was entered in the options is kept :)
  if ($name) {
    let $exists := db:exists($name)
    (: one read of the database properties supplies the options and the information :)
    let $info := if ($exists) { db:info($name) }
    return (
      <h2>Properties: { $name }</h2>,
      panels:tabs('properties', (
        if ($exists) {
          { 'id': 'options-panel', 'label': 'Options', 'contents': panels:options($name, $info) },
          { 'id': 'index-panel', 'label': 'Indexes',
            'contents': panels:index($name, $index, $prefix, '', 1) }
        },
        { 'id': 'backups-panel', 'label': 'Backups', 'contents': panels:backups($name) },
        if ($exists) {
          (: a report: what can be changed is offered by the options block :)
          { 'id': 'information-panel', 'label': 'Information',
            'contents': table:properties($info) }
        }
      )),
      <form method='dialog' class='buttons'>{
        (: what is done with the database as a whole; the options are applied from every tab :)
        if ($exists) {
          <button type='button' onclick='renameDatabase()'
                  title='Rename the database'>Rename…</button>,
          <button type='button' onclick='copyDatabase()'
                  title='Copy the database under a new name'>Copy…</button>,
          <button form='options-form'>Optimize</button>
        },
        <button>Close</button>
      }</form>
    )
  }
};

(:~
 : Creates the contents of the options block: the index configuration that the next
 : optimization of the selected database applies.
 : @param  $name  selected database
 : @param  $info  properties of the database
 : @return form
 :)
declare %private function panels:options(
  $name  as xs:string,
  $info  as element()
) as element(form) {
  (: the index flags and the text values of the properties :)
  let $indexes := $info//indexes/*
  return <form method='post' action='databases/optimize-db' autocomplete='off'
               id='options-form'>{
    <input type='hidden' name='name' value='{ $name }'/>,
    let $opts := $indexes[. = 'true']/name()
    return (
      <div class='field-columns'>{
        form:index-fields($opts, map:build($indexes, name#1, string#1))
      }</div>,
      (: a change of incremental indexing requires a full optimization :)
      form:update-fields($opts, form:checkbox('all', 'true', false(), 'Full optimization'))
    )
  }</form>
};

(:~
 : Creates the contents of the backups block: the backups of the selected database.
 : @param  $name  selected database
 : @param  $sort  sort key of the backup list
 : @param  $page  number of shown pages of the backup list
 : @return block contents; empty if no database is selected
 :)
declare function panels:backups(
  $name  as xs:string?,
  $sort  as xs:string := '',
  $page  as xs:integer := 1
) as element()* {
  (: the general backups have a dialog of their own :)
  if ($name) {
    panels:backup-section($name, $sort, $page)
  }
};

(:~
 : Creates the storage dialog.
 : @return dialog
 :)
declare function panels:storage-dialog() as element(dialog) {
  (: outside the panels. Its contents are rarely looked at: they are requested when the dialog is
     opened; see openDialog :)
  <dialog id='storage-dialog' class='tabbed'/>
};

(:~
 : Creates the contents of the storage dialog: the backups of the data that belongs to no
 : database, and what is stored.
 : @return contents
 :)
declare function panels:storage() as element()+ {
  (: what the server is configured with is listed in the settings :)
  <h2>Storage</h2>,
  panels:tabs('storage', (
    { 'id': 'general-backups-panel', 'label': 'Backups',
      'contents': panels:general-backups('', 1) },
    { 'id': 'storage-information-panel', 'label': 'Information',
      'contents': panels:storage-information() }
  )),
  <form method='dialog' class='buttons'><button>Close</button></form>
};

(:~
 : Creates the contents of the general backups block.
 : @param  $sort  sort key of the backup list
 : @param  $page  number of shown pages of the backup list
 : @return block contents
 :)
declare function panels:general-backups(
  $sort  as xs:string,
  $page  as xs:integer
) as element()+ {
  <div class='note'>
    The data that belongs to no database:
    <a target='_blank' href='https://docs.basex.org/main/User_Management'>users</a>,
    <a target='_blank'
       href='https://docs.basex.org/main/Job_Functions#services'>services</a>, and
    <a target='_blank' href='https://docs.basex.org/main/Store_Functions'>stores</a>.
  </div>,
  panels:backup-section('', $sort, $page)
};

(:~
 : Creates the contents of the storage information block: the totals of the stored data.
 : @return block contents
 :)
declare %private function panels:storage-information() as element()+ {
  let $databases := db:list-details()
  let $backups := db:backups()
  let $dbpath := db:option('dbpath')
  let $file := Q{java.io.File}new($dbpath)
  let $free := Q{java.io.File}getUsableSpace($file)
  let $latest := max($backups/@date ! xs:dateTime(.))
  return (
    (: an optimization or backup of a database needs about as much space as the database :)
    if ($free < max($databases/@size)) {
      <div class='note warn'>
        The free space is smaller than the largest database: a full optimization or backup
        may fail.
      </div>
    },
    table:properties(
      <server>
        <drive>
          <dbpath>{ $dbpath }</dbpath>
          <free>{ prof:human($free) }</free>
          <total>{ prof:human(Q{java.io.File}getTotalSpace($file)) }</total>
        </drive>
        <databases>
          <count>{ count($databases) }</count>
          <resources>{ sum($databases/@resources) }</resources>
          <size>{ prof:human(sum($databases/@size)) }</size>
        </databases>
        <backups>
          <count>{ count($backups) }</count>
          <general>{ count($backups[not(@database/string())]) }</general>
          <size>{ prof:human(sum($backups/@size)) }</size>
          <latest>{ ($latest ! html:exact(.)) otherwise '–' }</latest>
          <without-backup>{ count($databases[not(. = $backups/@database)]) }</without-backup>
        </backups>
      </server>
    )
  )
};

(:~ Number of characters of an index entry that are listed. :)
declare %private variable $panels:PREVIEW := 200;

(:~ Indexes that can be browsed, how they are labeled, the option that builds them, and what
    lists their entries. :)
(: what a database is built of comes before what it holds. The name indexes are part of every
   database, so they have no option of their own; an index that holds values looks a prefix up,
   the others are listed and filtered :)
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
 : Creates the contents of the index block: the entries of one index of the selected database,
 : with the number of times each of them occurs.
 : @param  $name    selected database
 : @param  $index   index that is listed
 : @param  $prefix  prefix of the entries to be listed
 : @param  $sort    sort key of the entry list
 : @param  $page    number of shown pages of the entry list
 : @return block contents; empty if no existing database is selected
 :)
declare function panels:index(
  $name    as xs:string?,
  $index   as xs:string,
  $prefix  as xs:string,
  $sort    as xs:string,
  $page    as xs:integer
) as element()* {
  (: no existing database: the tab is not shown, so it needs no placeholder :)
  if ($name and db:exists($name)) {
    let $presort := 'entry'
    let $sort := $sort[.] otherwise $presort
    return <form autocomplete='off' action='javascript:void(0);' data-sort='{ $sort }'
          data-page='{ $page }'>
      {
        (: which index is browsed, what is done with it, and where its entries are entered:
           controls of the list, so they share the row of its buttons and are pinned with them.
           The buttons address the index as a whole; they submit its name, not a selection,
           and post to a form of their own :)
        (: an unknown name falls back to the first index, which every database has :)
        let $type := panels:index-type($index) otherwise head($panels:INDEXES)
        let $controls := (
          <select id='index-select' onchange='selectIndex();'>{
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
            <button form='index-form' formaction='databases/index-create'
                    title='Create the chosen index'>{
              attribute disabled { }[not($indexable)], 'Create'
            }</button>,
            <button form='index-form' formaction='databases/index-drop'
                    onclick='return confirmAction(this, "Drop");'
                    title='Drop the chosen index'>{
              attribute disabled { }[not($indexable)], 'Drop'
            }</button>
          ),
          <input type='text' id='index-prefix' class='smallinput' placeholder='Prefix, e.g. abc'
                 title='Entries that start with the supplied string'
                 value='{ $prefix }' data-filter='filterIndex'/>
        )
        let $headers := (
          { 'key': 'entry', 'label': 'Entry' },
          { 'key': 'count', 'label': 'Count', 'type': 'number', 'order': 'desc',
            'width': 'calc(8ch + 0.5rem)' }
        )
        (: the same head is shown if the list cannot be :)
        let $head := <div class='buttons'>{ $controls }</div>
        (: an index that a database does not have is reported, not raised :)
        return try {
          let $entries := $type?entries($name, $prefix)
          (: one entry more than the page needs: it is what tells that there are further ones :)
          let $max := $config:ROWS * $page + 1
          let $shown := subsequence($entries, 1, $max)
          let $note := <div class='note'>{
            'More entries exist; supply a prefix to narrow the list.'
          }</div>[count($shown) = $max]
          (: the entry that was read to detect the further ones is not one of them :)
          return table:create($headers,
            subsequence($shown, 1, $max - 1) !
              { 'entry': panels:entry(.), 'count': @count },
            options := {
              'sort': $sort, 'presort': $presort, 'page': $page,
              (: the head stays in view while the entries scroll. The controls are no actions
                 on the entries, so the list has nothing to check :)
              'sticky': $head,
              'below': $note,
              'empty': if ($prefix) then 'No index entries start with the prefix.'
                else 'The index has no entries.'
            })
        } catch * {
          <div class='sticky'>{ $head, <div class='note warn'>{ $err:description }</div> }</div>
        }
      }
    </form>,
    (: the buttons of the panel are no submits of the list: browsing it must not build an
       index, so the values they post are kept in a form that nothing else submits :)
    <form method='post' autocomplete='off' id='index-form' onsubmit='clearPrefix();'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='index' value='{ $index }'/>
    </form>
  }
};

(:~
 : Returns what an index entry is listed as.
 : @param  $entry  index entry
 : @return label
 :)
declare %private function panels:entry(
  $entry  as element()
) as xs:string {
  (: a text node holds what it holds: a code block spans lines, and the whitespace between two
     elements is an entry of its own. What is listed is one line of it, so that the table stays a
     table, and an entry that holds nothing but whitespace is named instead of shown :)
  let $text := normalize-space($entry)
  return if ($text) then utils:chop($text, $panels:PREVIEW) else '(whitespace)'
};

(:~
 : Creates the contents of the resource panel: the actions that apply to the shown document,
 : the reason why it cannot be edited, and the field for querying it.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @param  $document  document properties, as returned by panels:document
 : @return panel contents; empty if no database is selected
 :)
declare function panels:resource(
  $name      as xs:string?,
  $resource  as xs:string?,
  $document  as utils:editor
) as element()* {
  switch () {
    case $document?exists return (
      let $hidden := (
        <input type='hidden' name='name' value='{ $name }'/>,
        <input type='hidden' name='resource' value='{ $resource }'/>
      )
      return (
        <h2>Resource: { $resource }</h2>,
        <form method='post' autocomplete='off'>{
          $hidden,
          <div class='buttons'>{
            (: enabled by the client once the document was edited :)
            <button type='button' id='save-resource' onclick='saveResource()'
                    disabled='' title='Save the edited resource'>Save</button>,
            <button type='button' onclick='renameResource()'
                    title='Rename the resource'>Rename…</button>,
            form:button('db-download', 'Download', title := 'Download the resource'),
            <button type='button' onclick='chooseUpload("replace-file")'
                    title='Replace the resource with an uploaded file'>Replace…</button>,
            <label>{
              <input type='checkbox' id='indent' onchange='indentChanged()'/>, ' Indent'
            }</label>
          }</div>
        }</form>,
        (: the line is reserved: the client writes to it as well :)
        <div id='note' class='note{ ' warn'[$document?note] }'>{ $document?note }</div>,
        (: the query runs while it is typed; a slow one can be stopped :)
        if ($document?xml) {
          <div class='query-row'>{
            <input type='text' class='query' name='input' id='input'
                   placeholder='XQuery on this document, e.g. //title'
                   title='Runs while you type; the result is shown instead of the document'
                   oninput='queryResource(false)'/>,
            <button type='button' id='stop' onclick='stopQuery()' disabled=''
                    title='Stop the running query'>Stop</button>
          }</div>
        },

        (: the new path of the resource is asked for and submitted :)
        form:prompt('rename-target', 'target', 'databases/resource-rename', $hidden),
        form:upload('databases/replace', 'replace-file', false(), $hidden)
      )
    )
    (: while a database is shown, the panel keeps its place, whatever is selected :)
    case boolean($name) return (
      <h2>Resource</h2>,
      <div class='note'>Choose a resource of the database.</div>
    )
    default return ()
  }
};

(:~
 : Returns the document that is shown in the editor, and what can be done with it.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @return properties: whether the resource exists, is XML and can be edited, the reason why
 :         it cannot be edited, and its text
 :)
declare function panels:document(
  $name      as xs:string?,
  $resource  as xs:string?
) as utils:editor {
  if (not($name and $resource and db:exists($name, $resource))) then (
    { 'exists': false(), 'text': '', 'editable': false() }
  ) else (
    (: the value is serialized with a bounded limit: db:list-details/@size counts nodes, not
       characters, so the length of the shown text is only known once the text is there :)
    let $xml := db:type($name, $resource) = 'xml'
    let $max := config:get($config:MAXCHARS)
    let $value := panels:resource-value($name, $resource)
    let $text := serialize($value, {
      'method': if ($xml) then 'xml' else 'basex',
      'limit': $max * 2 + 1
    })
    let $truncated := string-length($text) > $max
    (: what speaks against editing; the note the client extends for query results is the one
       that utils:editable composes from it :)
    return utils:editable(
      substring($text, 1, $max),
      (
        'no XML document'[not($xml)],
        'truncated, download for full text'[$truncated]
      )
    ) but with { 'xml': $xml }
  )
};

(:~
 : Returns the value of a resource, whatever it is stored as.
 : @param  $name      database
 : @param  $resource  resource
 : @return value
 :)
declare function panels:resource-value(
  $name      as xs:string,
  $resource  as xs:string
) as item()* {
  let $type := db:type($name, $resource)
  return if ($type = 'xml') then (
    db:get($name, $resource)
  ) else if ($type = 'binary') then (
    db:get-binary($name, $resource)
  ) else (
    db:get-value($name, $resource)
  )
};

(:~
 : Creates a link that enters a directory of a database.
 : @param  $label  link label
 : @param  $name   database
 : @param  $dir    directory the link refers to
 : @return link
 :)
declare %private function panels:enter(
  $label  as xs:string,
  $name   as xs:string,
  $dir    as xs:string
) as element(a) {
  (: the reference is a deep link naming the directory in full; following it in place leaves
     the other panels alone :)
  html:action($label, 'enterDbDir', { 'dir': $dir },
    { 'href': web:create-url($panels:CAT, { 'name': $name, 'dir': $dir }) })
};

(:~
 : Creates a link that selects a database.
 : @param  $name      database
 : @param  $selected  whether the link refers to what is shown
 : @return function creating the link
 :)
declare %private function panels:select(
  $name      as xs:string,
  $selected  as xs:boolean
) as fn() as element(a) {
  html:select($name, $panels:CAT, { 'name': $name }, $selected, 'name', 'selectDatabase')
};

(:~
 : Creates the backups of a database: a facet of it, not a sibling of its resources.
 : @param  $name  database; empty string for the backups of the general data
 : @param  $sort  sort key of the list; empty string for the newest backup first
 : @param  $page  number of shown pages of the list
 : @return the forms that create, upload and list the backups
 :)
declare %private function panels:backup-section(
  $name  as xs:string,
  $sort  as xs:string,
  $page  as xs:integer
) as element()+ {
  (: the backups of a database and the general ones are tabs of two dialogs on the same page: the
     backup prompt and the file chooser are named after what they belong to. One form for the
     whole section, so that its actions share a single row of buttons. Every button carries its
     own 'formaction' :)
  let $id := if ($name) then 'backup' else 'general-backup'
  (: the backups are listed with the newest one first, which is the order of their names :)
  let $presort := 'backup'
  let $sort := $sort[.] otherwise $presort
  return (<form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
    <input type='hidden' name='name' value='{ $name }'/>
    {
        let $headers := (
          (: a backup is named after its timestamp and a size never grows beyond four digits
             and a unit: both are of a known length and take no more than they need. The
             comment is free text, and is given whatever is left. The name shares its column
             with the checkbox :)
          { 'key': 'backup', 'label': 'Name', 'order': 'desc', 'width': 'calc(21ch + 0.5rem)' },
          { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc',
            'width': 'calc(9ch + 0.5rem)' },
          { 'key': 'comment', 'label': 'Comment' }
        )
        let $entries :=
          for $backup in db:backups($name)
          return {
            (: the name is the download: a column that repeats it as a link adds nothing :)
            'backup': fn() {
              html:link(substring-after($backup, $name || '-'),
                `backup/{ encode-for-uri($backup) }.zip`)
            },
            'size': $backup/@size,
            'comment': $backup/@comment
          }
        (: an empty name addresses the data that belongs to no database :)
        let $what := if ($name) then 'the database' else 'the users, services, and stores'
        let $buttons := (
          <button type='button' onclick='showDialog("{ $id }")'
                  title='Create a backup of { $what }'>{
            (: there is nothing to back up if the name is only known from a backup :)
            attribute disabled { }[$name][not(db:exists($name))],
            'Back up…'
          }</button>,
          <button type='button' onclick='chooseUpload("upload-{ $id }s")'
                  title='Upload backup files'>Upload…</button>,
          form:button('databases/backup-restore', 'Restore', ('CHECK', 'CONFIRM'),
            title := 'Replace ' || $what || ' with the selected backup'),
          form:button('databases/backup-drop', 'Drop', ('CHECK', 'CONFIRM'),
            title := 'Delete the selected backups')
        )
        return table:create($headers, $entries, $buttons, { 'name': $name },
          { 'sort': $sort, 'presort': $presort, 'page': $page, 'noun': 'backup',
            'empty': 'There are no backups yet. Create one with Back up…' })
      }
  </form>,

  form:dialog($id, 'Back Up', 'databases/backup-create', false(), (
    <input type='hidden' name='name' value='{ $name }'/>,
    form:field('Comment:', <input type='text' name='comment' placeholder='optional' autofocus=''/>),
    form:checkbox('compress', 'true', true(), 'Compress',
      title := 'Compress the backup; without compression, it is written faster')
  )),

  (: the file chooser is opened by the Upload button and submits what it collects. An upload
     lands in the database directory, so the server checks that it belongs where it is put :)
  form:upload('databases/backup-upload', `upload-{ $id }s`, true(),
    <input type='hidden' name='name' value='{ $name }'/>)
  )
};
