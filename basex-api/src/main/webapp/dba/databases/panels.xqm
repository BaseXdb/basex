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
          (: the date is the longest value of a known length, and the count and the size have
             room to spare; the name keeps its share, as it is the one that identifies a row :)
          { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '40%' },
          { 'key': 'resources', 'label': 'Count', 'type': 'number', 'order': 'desc',
            'width': '13%' },
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
      form:index-options(('textindex', 'attrindex'), 'en', true())
    ))
  )
};

(:~
 : Creates the contents of the database panel: the resources of the selected database, its
 : backups, and the index configuration that its next optimization applies.
 : @param  $name      selected database
 : @param  $sort      sort key of the resource list
 : @param  $page      current page of the resource list
 : @param  $resource  selected resource
 : @return panel contents; empty if no database is selected
 :)
declare function panels:database(
  $name      as xs:string?,
  $sort      as xs:string,
  $page      as xs:integer,
  $resource  as xs:string?
) as element()* {
  if (not($name)) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else if (not(db:exists($name))) {
    (: the name is only known from the backups of the dropped database :)
    <h2>{ 'Database: ' || $name }</h2>,
    <div class='note'>The database does not exist; one of its backups can be restored.</div>
  } else {
    <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
      <input type='hidden' name='name' value='{ $name }'/>
      {
        let $headers := (
          { 'key': 'resource', 'label': 'Name', 'type': 'dynamic', 'width': '45%' },
          { 'key': 'type', 'label': 'Type', 'width': '13%' },
          { 'key': 'size', 'label': 'Size', 'type': 'number', 'order': 'desc', 'width': '14%' },
          { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
            'width': '28%' }
        )
        let $entries :=
          for $res in utils:slice(db:list-details($name), $page, $sort)
          let $path := string($res)
          return {
            'resource': panels:select($path, { 'name': $name, 'resource': $path },
              $path = $resource),
            'type': $res/@type,
            'size': $res/@size,
            'date': $res/@modified-date
          }
        (: what applies to the database comes first; the row wraps only if it has to :)
        let $buttons := (
          <button type='button' onclick='renameDatabase()'>Rename…</button>,
          <button type='button' onclick='copyDatabase()'>Copy…</button>,
          <button type='button' onclick='showDialog("optimize")'>Optimize…</button>,
          <button type='button' onclick='showDialog("add")'>Add…</button>,
          form:button('databases/resource-delete', 'Delete', ('CHECK', 'CONFIRM'))
        )
        let $options := {
          'sort': $sort,
          'page': $page,
          (: the total is only read while the entries are not re-sorted; enumerating a database
             of six-figure size for a number that is then discarded is not free :)
          'count': if ($sort) { () } else { count(db:list($name)) },
          (: the database and what can be done with it stay in view while its resources scroll :)
          'sticky': <h2>{
            'Database: ',
            (: the link clears the selected resource, and with it the document that is shown :)
            <a href='{ web:create-url($panels:CAT, { 'name': $name }) }'
               onclick='selectResource(""); return false;'>{ $name }</a>
          }</h2>
        }
        return table:create($headers, $entries, $buttons, { 'name': $name }, $options)
      }
    </form>,

    (: the resources are stored under their own names; how they are parsed is chosen here :)
    form:dialog('add', 'Add Resources', 'databases/put', true(), (
      <input type='hidden' name='name' value='{ $name }'/>,
      form:field('Input:', <input type='file' name='files' multiple='multiple' required=''/>),
      form:field('Binary Storage:', form:checkbox('binary', 'true', false(), '')),
      <h3>Parsing Options</h3>,
      form:option('intparse', 'Use internal XML parser', ()),
      form:option('dtd', 'Parse DTDs and entities', ()),
      form:option('stripns', 'Strip namespaces', ()),
      form:option('stripws', 'Strip whitespace', ()),
      form:option('xinclude', 'Use XInclude', ())
    )),
    (: the index configuration is not a report: it is what the next optimization applies :)
    (: one read of the database properties supplies both the index flags and the language :)
    let $info := db:info($name)
    return form:dialog('optimize', 'Optimize Database', 'databases/optimize-db', false(), (
      <input type='hidden' name='name' value='{ $name }'/>,
      form:checkbox('all', 'true', false(), 'Full optimization'),
      form:index-options($info//*[text() = 'true']/name(), $info//language, false())
    )),
    (: the new name of the database is asked for; the chosen action decides what is done with it :)
    <form method='post' autocomplete='off' id='database-form'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='newname' id='database-newname'/>
    </form>
  }
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
    <div id='note' class='note{ ' strong'[$document?truncated] }'>{ $document?note }</div>,
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
  (: one section is shown at a time, so its fields need no names of their own :)
  <div class='note'>
    Ensure that your server has enough RAM assigned to upload large backups.
  </div>,
  (: one form for the whole section, so that its actions share a single row of buttons.
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
          <button type='button' onclick='chooseBackups()'>Upload</button>,
          form:button('databases/backup-restore', 'Restore', ('CHECK', 'CONFIRM')),
          form:button('databases/backup-drop', 'Drop', ('CHECK', 'CONFIRM'))
        )
        return table:create($headers, $entries, $buttons, { 'name': $name },
          { 'compact': true() })
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
