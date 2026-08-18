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
 : Databases: the databases, the resources of the selected one, and the selected document. The
 : selection is part of the address, so a link reproduces what the panels show; changing it
 : refreshes the panels over the connection the view opens for its queries.
 : @param  $name      selected database
 : @param  $resource  selected resource
 : @param  $info      info string
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/databases')
  %rest:query-param('name',     '{$name}', '')
  %rest:query-param('resource', '{$resource}', '')
  %rest:query-param('info',     '{$info}')
  %rest:query-param('error',    '{$error}')
  %output:method('html')
function dba:databases(
  $name      as xs:string,
  $resource  as xs:string,
  $info      as xs:string?,
  $error     as xs:string?
) as element(html) {
  (: the document is needed twice: its text fills the editor, its properties the panel :)
  let $document := panels:document($name, $resource)
  (: a panel is labelled in the markup, not by its heading: it keeps its name while it is
     folded away, and while it has nothing to show and is hidden :)
  let $database := panels:database($name, '', 1, $resource)
  let $information := panels:information($name)
  (: the panels follow the selection, and are not remembered: while a document is shown, what
     it was chosen from steps back to a strip; without one, the lists are what there is to see :)
  let $fold := ' collapsed'[$document?exists]
  return (
    <div class='panel no-divider{ $fold }' data-label='Databases'>
      <div id='databases-panel' class='pane'>{ panels:databases('', 1, $name) }</div>
    </div>,
    <div class='panel no-divider{ ' hidden'[empty($database)] }' data-label='Database'>
      <div id='database-panel' class='pane'>{ $database }</div>
    </div>,
    (: the editor is created once and outlives the panel above it, which is redrawn :)
    <div class='panel no-divider{ ' hidden'[not($document?exists)] }' data-label='Resource'>
      <div id='resource-panel'>{ panels:resource($name, $resource, $document) }</div>
      <textarea id='editor' spellcheck='false'>{ $document?text }</textarea>
    </div>,
    (: both sit at the right edge, so both fold that way; only the last one does so by default :)
    <div class='panel no-divider{ $fold }' data-label='Backups' data-fold='right'>
      <div id='backups-panel' class='pane'>{ panels:backups($name) }</div>
    </div>,
    (: a report, not a step of the work: it is opened when it is asked for :)
    <div class='panel no-divider collapsed{ ' hidden'[empty($information)] }'
         data-label='Information'>
      <div id='information-panel' class='pane'>{ $information }</div>
    </div>
  ) => html:wrap({
    'header' : $dba:CAT,
    'columns': ('20fr', '25fr', '35fr', '20fr', '20fr'),
    'rows'   : '1fr',
    'panels' : 'auto',
    'scripts': ('cm6', 'editor', 'databases'),
    'init'   : 'initDatabases(' || ($document?editable = true()) || ');',
    'info'   : $info,
    'error'  : $error
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
    web:response-header(
      { 'media-type': db:content-type($name, $resource) },
      utils:disposition($resource)
    ),
    let $type := db:type($name, $resource)
    return if ($type = 'xml') {
      db:get($name, $resource)
    } else if ($type = 'binary') {
      db:get-binary($name, $resource)
    } else {
      db:get-value($name, $resource)
    }
  } catch * {
    web:error(404, $err:description)
  }
};

(:~
 : Downloads a backup.
 : @param  $backup  name of backup file (ignored by the server)
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path('/dba/backup/{$backup}')
function dba:backup-download(
  $backup  as xs:string
) as item()+ {
  let $path := db:option('dbpath') || '/' || $backup
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
  db:put($name, parse-xml($content, { 'strip-space': $indent = 'true' }), $resource),
  update:output('')
};

(:~
 : Runs a database action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/databases/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'create': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'database', 'created'),
      'run'   : %updating fn() {
        if (db:exists($args?name)) {
          error((), 'Database already exists.')
        } else {
          db:create($args?name, (), (), form:index-map($args?opts, $args?lang, true()))
        }
      }
    } },
    'drop': fn($args) { {
      'info': utils:info($args?name, 'database', 'dropped'),
      'run' : %updating fn() { $args?name ! db:drop(.) }
    } },
    'optimize': fn($args) { {
      'info': utils:info($args?name, 'database', 'optimized'),
      'run' : %updating fn() { $args?name ! db:optimize(.) }
    } },
    'optimize-db': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'database', 'optimized'),
      'run'   : %updating fn() {
        db:optimize($args?name, boolean($args?all),
          form:index-map($args?opts, $args?lang, false()))
      }
    } },
    'rename': fn($args) {
      dba:rename-database($args, 'renamed', %updating fn($from, $to) { db:alter($from, $to) })
    },
    'copy': fn($args) {
      dba:rename-database($args, 'copied', %updating fn($from, $to) { db:copy($from, $to) })
    },
    'backups-create': fn($args) { {
      'info': utils:info($args?name, 'database', 'backed up'),
      'run' : %updating fn() { $args?name ! db:create-backup(.) }
    } },
    'backups-restore': fn($args) { {
      'info': utils:info($args?name, 'backup', 'restored'),
      'run' : %updating fn() { $args?name ! db:restore(.) }
    } },
    'backup-create': fn($args) {
      let $name := string($args?name)
      return {
        'params': { 'name': $name },
        'info'  : utils:info($name, 'database', 'backed up'),
        'run'   : %updating fn() {
          db:create-backup($name, {
            'comment': $args?comment, 'compress': boolean($args?compress)
          })
        }
      }
    },
    'backup-drop': fn($args) {
      let $name := string($args?name)
      return {
        'params': { 'name': $name },
        'info'  : utils:info($args?backup, 'backup', 'dropped'),
        'run'   : %updating fn() { $args?backup ! db:drop-backup($name || '-' || .) }
      }
    },
    'backup-restore': fn($args) {
      let $name := string($args?name)
      (: only the first backup will be restored :)
      let $backup := head($args?backup)
      return {
        'params': { 'name': $name },
        'info'  : utils:info($backup, 'backup', 'restored'),
        'run'   : %updating fn() { db:restore($name || '-' || $backup) }
      }
    },
    'put': fn($args) {
      let $files := $args?files[. instance of map(*)] otherwise {}
      return {
        'params': { 'name': $args?name },
        'info'  : utils:info(map:keys($files), 'resource', 'added'),
        'run'   : %updating fn() {
          if (map:size($files) = 0) {
            error((), 'No input specified.')
          } else if ($args?binary) {
            for key $path value $content in $files
            return db:put-binary($args?name, $content, $path)
          } else {
            (: the input is parsed here, so that a broken document is reported as an error
               instead of failing when the pending updates are applied :)
            let $options := map:merge(
              ('intparse', 'dtd', 'stripns', 'stripws', 'xinclude') !
                map:entry(., $args?opts = .))
            for key $path value $content in $files
            return db:put($args?name, fetch:binary-doc($content), $path, $options)
          }
        }
      }
    },
    'replace': fn($args) {
      let $files := $args?files[. instance of map(*)] otherwise {}
      let $content := head(map:items($files))
      return {
        'params': { 'name': $args?name, 'resource': $args?resource },
        'info'  : utils:info($args?resource, 'resource', 'replaced'),
        'run'   : %updating fn() {
          if (empty($content)) {
            error((), 'No input specified.')
          } else if (db:type($args?name, $args?resource) = 'xml') {
            db:put($args?name, fetch:binary-doc($content), $args?resource)
          } else {
            db:put-binary($args?name, $content, $args?resource)
          }
        }
      }
    },
    'resource-delete': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?resource, 'resource', 'deleted'),
      'run'   : %updating fn() { $args?resource ! db:delete($args?name, .) }
    } },
    'resource-rename': fn($args) {
      let $exists := db:exists($args?name, $args?target)
      return {
        (: a rename that fails leaves the resource where it is, and selected :)
        'params': {
          'name': $args?name,
          'resource': if ($exists) then $args?resource else $args?target
        },
        'info'  : utils:info($args?resource, 'resource', 'renamed'),
        'run'   : %updating fn() {
          if ($exists) {
            error((), 'Resource already exists.')
          } else {
            db:rename($args?name, $args?resource, $args?target)
          }
        }
      }
    }
  })
};

(:~
 : Returns the action that renames or copies a database. Both take a new name and reject one
 : that is assigned already.
 : @param  $args    request parameters
 : @param  $action  action label (past tense)
 : @param  $update  database operation
 : @return action
 :)
declare %private function dba:rename-database(
  $args    as map(*),
  $action  as xs:string,
  $update  as %updating fn(*)
) as map(*) {
  (: the name that was offered for editing is the current one: keeping it is no conflict :)
  let $exists := $args?newname != $args?name and db:exists($args?newname)
  return {
    (: an operation that fails leaves the database it was started from selected :)
    'params': { 'name': if ($exists) then $args?name else $args?newname },
    'info'  : utils:info($args?name, 'database', $action),
    'run'   : %updating fn() {
      if ($exists) {
        error((), 'Database already exists.')
      } else if ($args?name != $args?newname) {
        updating $update($args?name, $args?newname)
      }
    }
  }
};
