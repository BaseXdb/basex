(:~
 : Databases: database chooser, resource browser and document editor in a single view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace panels = 'dba/lib/db-panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'databases';

(:~
 : Databases: the databases, the resources of the selected one, and the selected document.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @param  $dir       directory that is listed
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/databases')
  %rest:query-param('name',     '{$name}', '')
  %rest:query-param('resource', '{$resource}', '')
  %rest:query-param('dir',      '{$dir}', '')
  %output:method('html')
function dba:databases(
  $name      as xs:string,
  $resource  as xs:string,
  $dir       as xs:string
) as element(html) {
  (: the selection is part of the address, so a link reproduces what the panels show; changing
     it refreshes the panels over the connection the view opens for its queries :)
  (: the document is needed twice: its text fills the editor, its properties the panel :)
  let $document := panels:document($name, $resource)
  (: a link that names a resource alone opens the level that holds it; the client derives the
     directory in the same way :)
  let $dir := $dir[.] otherwise replace($resource, '[^/]+$', '')
  (: a panel is labelled in the markup, not by its heading: it keeps its name while it is
     folded away, and while it has nothing to show and is hidden :)
  let $database := panels:database($name, '', 1, $resource, $dir, '')
  let $information := panels:information($name)
  let $index := panels:index($name, 'element-name', '', '', 1)
  (: the panels follow the selection: while a document is shown, what it was chosen from steps
     back to a strip; without one, the lists are what there is to see. A panel that is folded by
     hand keeps the state it was given :)
  let $fold := $document?exists
  return (
    html:panel(panels:databases('', 1, $name),
      { 'id': 'databases-panel', 'label': 'Databases', 'collapsed': $fold }),
    html:panel($database,
      { 'id': 'database-panel', 'label': 'Database', 'close': 'selectDatabase("")' }),
    (: the editor is created once and outlives the panel above it, which is redrawn :)
    html:panel(panels:resource($name, $resource, $document), {
      'id'   : 'resource-panel',
      'label': 'Resource',
      'close': 'selectResource("")',
      'pane' : false(),
      'extra': <textarea id='editor' spellcheck='false'>{ $document?text }</textarea>
    }),
    (: the last three sit at the right edge, so all fold that way; only the last one does so by
       default. The backups are shown on the top level, and step back once a database is chosen :)
    html:panel(panels:backups($name), {
      'id': 'backups-panel', 'label': 'Backups', 'collapsed': boolean($name), 'fold': 'right',
      'description': 'the backups of the database, or of the users, services and stores'
    }),
    (: reports, not steps of the work: they are opened when they are asked for :)
    html:panel($index,
      { 'id': 'index-panel', 'label': 'Indexes', 'collapsed': true(), 'fold': 'right',
        'description': 'the entries of the indexes of the database' }),
    html:panel($information,
      { 'id': 'information-panel', 'label': 'Information', 'collapsed': true(),
        'description': 'the properties and options of the database' }),
    (: what the language fields of the dialogs suggest :)
    form:languages()
  ) => html:wrap({
    'header' : $dba:CAT,
    'columns': ('20fr', '25fr', '35fr', '20fr', '20fr', '20fr'),
    'rows'   : '1fr',
    (: a view of its own per level: what is folded away while a document is shown is not what
       is folded away while a database or the list of databases is; see foldResourcePanels :)
    'panels' : if ($document?exists) then 'resource' else if ($name) then 'database' else (),
    'scripts': ('cm6', 'editor', 'databases'),
    'init'   : `initDatabases({ $document?editable });`
  })
};

(:~
 : Downloads a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @return rest response and file content
 :)
declare
  %rest:POST
  %rest:path('/dba/db-download')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resource}')
function dba:db-download(
  $name      as xs:string,
  $resource  as xs:string
) as item()+ {
  try {
    utils:attachment($resource, panels:resource-value($name, $resource),
      db:content-type($name, $resource))
  } catch * {
    web:error(404, $err:description)
  }
};

(:~
 : Downloads the checked resources; several of them are packed into an archive.
 : @param  $name       database
 : @param  $resources  resources; a directory stands for everything below it
 : @return rest response and file content, or redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/resources-download')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resources}')
function dba:resources-download(
  $name       as xs:string,
  $resources  as xs:string*
) as item()+ {
  (: a level is listed with its directories, and db:list resolves either of them to the
     resources it covers; the same resource may be reached by two of them :)
  let $paths := distinct-values($resources ! db:list($name, .))
  return try {
    if (empty($paths)) then (
      utils:outcome($dba:CAT, { 'name': $name }, { 'error': 'No resource was selected.' })
    ) else (
      (: the archive is named after the database, as the one of the file panel is named
         after its directory :)
      utils:archive($paths, $paths ! dba:content($name, .), $name)
    )
  } catch * {
    utils:outcome($dba:CAT, { 'name': $name },
      { 'error': 'Download failed: ' || $err:description })
  }
};

(:~
 : Returns the content of a resource, in the form an archive holds it.
 : @param  $name      database
 : @param  $resource  resource
 : @return content
 :)
declare %private function dba:content(
  $name      as xs:string,
  $resource  as xs:string
) as item() {
  let $value := panels:resource-value($name, $resource)
  return if ($value instance of xs:base64Binary) then (
    $value
  ) else (
    let $method := if (db:type($name, $resource) = 'xml') then 'xml' else 'basex'
    return serialize($value, { 'method': $method })
  )
};

(:~
 : Downloads a backup.
 : @param  $backup  name of backup file
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path('/dba/backup/{$backup}')
function dba:backup-download(
  $backup  as xs:string
) as item()+ {
  let $path := utils:safe-path(db:option('dbpath') || '/', $backup)
  return (
    web:response-header(
      { 'media-type': 'application/octet-stream' },
      { 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  )
};

(:~
 : Saves the edited content of an XML resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $content   new content
 : @param  $indent    indicates if the content is indented
 : @return empty output
 :)
declare
  %updating
  %rest:POST('{$content}')
  %rest:path('/dba/db-save')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resource}')
  %rest:query-param('indent',   '{$indent}')
  %output:method('text')
function dba:db-save(
  $name      as xs:string,
  $resource  as xs:string,
  $content   as xs:string?,
  $indent    as xs:string?
) {
  (: indentation is only added for display :)
  db:put($name, parse-xml($content,
    { 'strip-space': if ($indent = 'true') then 'all' else 'none' }), $resource),
  update:output('')
};

(:~
 : Creates a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/create')
function dba:create() {
  utils:dispatch($dba:CAT, fn($args) {
    let $files := utils:files($args?files)
    let $input := $args?input[.]
    return {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'database', 'created'),
      'run'   : %updating fn() {
        if (db:exists($args?name)) then (
          error((), 'Database already exists.')
        ) else (
          (: without an input, an empty database is created. Uploaded files are stored under
             their names, as binaries if requested; an input on the server is expanded below
             the root :)
          db:create($args?name,
            (
              for value $content in $files
              return if ($args?binary) then $content else fetch:binary-doc($content),
              $input
            ),
            (map:keys($files), $input ! ''), {
              form:index-map($args?opts, $args?lang, $args?ftinclude, true()),
              form:parsing-map($args?opts, $args?filter, $args?parser)
            })
        )
      }
    }
  })
};

(:~
 : Drops databases.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/drop')
function dba:drop() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'database', 'dropped'),
    'run' : %updating fn() { $args?name ! db:drop(.) }
  } })
};

(:~
 : Optimizes databases.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/optimize')
function dba:optimize() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'database', 'optimized'),
    'run' : %updating fn() { $args?name ! db:optimize(.) }
  } })
};

(:~
 : Optimizes a database with new options.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/optimize-db')
function dba:optimize-db() {
  utils:dispatch($dba:CAT, fn($args) { {
    'params': { 'name': $args?name },
    'info'  : utils:info($args?name, 'database', 'optimized'),
    'run'   : %updating fn() {
      db:optimize($args?name, boolean($args?all),
        form:index-map($args?opts, $args?lang, $args?ftinclude, false()))
    }
  } })
};

(:~
 : Renames a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/rename')
function dba:rename() {
  utils:dispatch($dba:CAT, dba:rename-database(?, 'renamed', db:alter#2, true()))
};

(:~
 : Copies a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/copy')
function dba:copy() {
  utils:dispatch($dba:CAT, dba:rename-database(?, 'copied', db:copy#2, false()))
};

(:~
 : Creates backups of databases.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backups-create')
function dba:backups-create() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'database', 'backed up'),
    'run' : %updating fn() { $args?name ! db:create-backup(.) }
  } })
};

(:~
 : Restores the latest backups of databases.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backups-restore')
function dba:backups-restore() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'backup', 'restored'),
    'run' : %updating fn() { $args?name ! db:restore(.) }
  } })
};

(:~
 : Creates a backup of a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backup-create')
function dba:backup-create() {
  utils:dispatch($dba:CAT, fn($args) {
    let $name := string($args?name)
    return {
      'params': { 'name': $name },
      (: an empty name addresses the data that belongs to no database :)
      'info'  : if ($name) then utils:info($name, 'database', 'backed up')
                else 'Users, services, and stores were backed up.',
      'run'   : %updating fn() {
        db:create-backup($name, {
          'comment': string($args?comment), 'compress': boolean($args?compress)
        })
      }
    }
  })
};

(:~
 : Drops backups of a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backup-drop')
function dba:backup-drop() {
  utils:dispatch($dba:CAT, fn($args) {
    let $name := string($args?name)
    return {
      'params': { 'name': $name },
      'info'  : utils:info($args?backup, 'backup', 'dropped'),
      'run'   : %updating fn() { $args?backup ! db:drop-backup(`{ $name }-{ . }`) }
    }
  })
};

(:~
 : Restores a backup of a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backup-restore')
function dba:backup-restore() {
  utils:dispatch($dba:CAT, fn($args) {
    let $name := string($args?name)
    (: only the first backup will be restored :)
    let $backup := head($args?backup)
    return {
      'params': { 'name': $name },
      'info'  : utils:info($backup, 'backup', 'restored'),
      'run'   : %updating fn() { db:restore(`{ $name }-{ $backup }`) }
    }
  })
};

(:~
 : Uploads backups.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/backup-upload')
function dba:backup-upload() {
  utils:dispatch($dba:CAT, fn($args) {
    (: the panel the upload was started from decides where the backups belong; an empty name
       stands for the general data :)
    let $name := string($args?name)
    let $files := utils:files($args?files)
    return {
      'params': { 'name': $name },
      'info'  : utils:info(map:keys($files), 'backup', 'uploaded'),
      'run'   : %updating fn() {
        for key $file value $content in $files
        let $db := replace($file, $utils:BACKUP-ZIP-REGEX, '$1')
        let $entries := archive:entries($content) ! data()
        (: reject backups with invalid content :)
        let $valid := if ($db) then (
          $entries = $db || '/inf.basex' and
          (every $entry in $entries satisfies starts-with($entry, $db || '/'))
        ) else (
          every $entry in $entries satisfies matches($entry, '\.(xml|basex)')
        )
        return if (not($valid)) then (
          error((), 'Invalid backup file: ' || $file)
        ) else if ($name and $db != $name) {
          (: reject the backup of another database: it would be invisible in the panel it was
             uploaded from. Without a selected database there is nothing to contradict, and a
             backup of a database that no longer exists is what a recovery starts from :)
          error((), `Backup "{ $file }" does not belong to database "{ $name }".`)
        },
        let $dir := db:option('dbpath') || '/'
        for key $file value $content in $files
        return file:write-binary(utils:safe-path($dir, $file), $content)
      }
    }
  })
};

(:~
 : Adds resources to a database.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/put')
function dba:put() {
  utils:dispatch($dba:CAT, fn($args) {
    let $files := utils:files($args?files)
    let $input := $args?input[.]
    let $target := $args?target[.]
    return {
      'params': { 'name': $args?name },
      (: an input may stand for a single file or for the contents of a directory :)
      'info'  : utils:info((map:keys($files), $input), 'resource', 'added'),
      'run'   : %updating fn() {
        if (map:size($files) = 0 and empty($input)) then (
          error((), 'No input specified.')
        ) else if ($input and empty($target)) then (
          (: an empty target addresses the database as a whole: what the input does not
             supply would be deleted :)
          error((), 'Target path is required.')
        ) else (
          let $options := form:parsing-map($args?opts, $args?filter, $args?parser)
          return (
            if ($args?binary) then (
              for key $path value $content in $files
              return db:put-binary($args?name, $content, $path)
            ) else (
              (: the input is parsed here, so that a broken document is reported as an error
                 instead of failing when the pending updates are applied :)
              for key $path value $content in $files
              return db:put($args?name, fetch:binary-doc($content), $path, $options)
            ),
            (: a directory or an archive is expanded, and the paths it contains are kept
               below the target; what is stored there already is replaced :)
            $input ! db:put($args?name, ., $target, $options)
          )
        )
      }
    }
  })
};

(:~
 : Replaces a resource.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/replace')
function dba:replace() {
  utils:dispatch($dba:CAT, fn($args) {
    let $files := utils:files($args?files)
    let $content := head(map:items($files))
    return {
      'params': { 'name': $args?name, 'resource': $args?resource },
      'info'  : utils:info($args?resource, 'resource', 'replaced'),
      'run'   : %updating fn() {
        if (empty($content)) then (
          error((), 'No input specified.')
        ) else if (db:type($args?name, $args?resource) = 'xml') then (
          db:put($args?name, fetch:binary-doc($content), $args?resource)
        ) else (
          db:put-binary($args?name, $content, $args?resource)
        )
      }
    }
  })
};

(:~
 : Deletes resources.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/resource-delete')
function dba:resource-delete() {
  utils:dispatch($dba:CAT, fn($args) { {
    'params': { 'name': $args?name },
    'info'  : utils:info($args?resource, 'resource', 'deleted'),
    'run'   : %updating fn() { $args?resource ! db:delete($args?name, .) }
  } })
};

(:~
 : Renames a resource.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/resource-rename')
function dba:resource-rename() {
  utils:dispatch($dba:CAT, fn($args) {
    let $exists := db:exists($args?name, $args?target)
    return {
      (: a rename that fails leaves the resource where it is, and selected :)
      'params': {
        'name': $args?name,
        'resource': if ($exists) then $args?resource else $args?target
      },
      'info'  : utils:info($args?resource, 'resource', 'renamed'),
      'run'   : %updating fn() {
        if ($exists) then (
          error((), 'Resource already exists.')
        ) else (
          db:rename($args?name, $args?resource, $args?target)
        )
      }
    }
  })
};

(:~
 : Creates an index.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/index-create')
function dba:index-create() {
  utils:dispatch($dba:CAT, dba:index(?, true()))
};

(:~
 : Drops an index.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/index-drop')
function dba:index-drop() {
  utils:dispatch($dba:CAT, dba:index(?, false()))
};

(:~
 : Returns the action that renames or copies a database.
 : @param  $args    request parameters
 : @param  $action  action label (past tense)
 : @param  $update  database operation
 : @param  $select  select the new database afterwards
 : @return action
 :)
declare %private function dba:rename-database(
  $args    as map(*),
  $action  as xs:string,
  $update  as %updating fn(*),
  $select  as xs:boolean
) as utils:action {
  (: both take a new name and reject one that is assigned already :)
  (: the name that was offered for editing is the current one: keeping it is no conflict :)
  let $exists := $args?newname != $args?name and db:exists($args?newname)
  return {
    (: an operation that fails leaves the database it was started from selected :)
    'params': { 'name': if ($select and not($exists)) then $args?newname else $args?name },
    'info'  : `Database "{ $args?name }" was { $action } to "{ $args?newname }".`,
    'run'   : %updating fn() {
      if ($exists) then (
        error((), 'Database already exists.')
      ) else if ($args?name != $args?newname) {
        updating $update($args?name, $args?newname)
      }
    }
  }
};

(:~
 : Returns the action that builds or discards a single index of a database.
 : @param  $args    request parameters
 : @param  $create  create or drop the index
 : @return action
 :)
declare %private function dba:index(
  $args    as map(*),
  $create  as xs:boolean
) as utils:action {
  (: an optimization that is limited to one index option leaves the other options of the
     database as they are :)
  let $index := panels:index-type($args?index)
  return {
    'params': { 'name': $args?name },
    'info'  : utils:info($index?label, 'index', if ($create) then 'created' else 'dropped'),
    'run'   : %updating fn() {
      if (empty($index?option)) then (
        error((), 'Index cannot be created or dropped.')
      ) else (
        db:optimize($args?name, false(), { $index?option: $create })
      )
    }
  }
};
