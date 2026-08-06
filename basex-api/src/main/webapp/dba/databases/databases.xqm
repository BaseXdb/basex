(:~
 : List of databases.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : List of databases.
 : @param  $sort   table sort key
 : @param  $page   current page
 : @param  $info   info string
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/databases')
  %rest:query-param('sort',  '{$sort}', '')
  %rest:query-param('page',  '{$page}', 1)
  %rest:query-param('info',  '{$info}')
  %rest:query-param('error', '{$error}')
  %output:method('html')
function dba:databases(
  $sort   as xs:string,
  $page   as xs:integer,
  $info   as xs:string?,
  $error  as xs:string?
) as element(html) {
  let $db-names := db:list()
  let $databases :=
    for $db in utils:slice(db:list-details(), $page, $sort)
    return {
      'name': $db,
      'resources': $db/@resources,
      'size': $db/@size,
      'date': $db/@modified-date
    }
  let $backups := (
    for $backup in db:backups()
    where matches($backup, $utils:BACKUP-REGEX)
    group by $name := replace($backup, $utils:BACKUP-REGEX, '$1')
    where $name and not($db-names = $name)
    let $date := replace(sort($backup)[last()], $utils:BACKUP-REGEX, '$2T$3:$4:$5Z')
    return {
      'name': $name,
      'size': (),
      'date': $date
    }
  )
  return (
    <div class='panel'>
      <form method='post' autocomplete='off'>
        <h2>Databases</h2>
        {
          let $headers := (
            { 'key': 'name', 'label': 'Name' },
            { 'key': 'resources', 'label': 'Count', 'type': 'number', 'order': 'desc' },
            { 'key': 'size', 'label': 'Bytes', 'type': 'bytes', 'order': 'desc' },
            { 'key': 'date', 'label': 'Last Modified', 'type': 'dateTime', 'order': 'desc' }
          )
          let $entries := ($databases, $backups)
          let $buttons := (
            html:button('db-create', 'Create…'),
            html:button('databases/optimize', 'Optimize', 'CHECK'),
            html:button('databases/drop', 'Drop', ('CHECK', 'CONFIRM')),
            html:button('databases/backups-create', 'Back up', 'CHECK'),
            html:button('databases/backups-restore', 'Restore', ('CHECK', 'CONFIRM'))
          )
          let $count := count($db-names) + count($backups)
          let $options := {
            'sort': $sort,
            'link': 'database',
            'page': $page,
            'count': $count
          }
          return html:table($headers, $entries, $buttons, {}, $options)
        }
      </form>
    </div>,
    <div class='panel'>
      <h2>Upload Backups</h2>
      <form method='post' enctype='multipart/form-data' autocomplete='off'
            onsubmit='uploading(this);'>{
        <input type='file' name='files' multiple='multiple'/>,
        html:button('backup-upload', 'Upload')
      }</form>
      <div class='note'>
        Ensure that your server has enough RAM assigned to upload large backups.
      </div>
      <form method='post' autocomplete='off'>
        <input type='hidden' name='name' value=''/>
        <h2>General Backups</h2>
        <div class='note'>
          Comprising:
          <a target='_blank'
             href='https://docs.basex.org/main/User_Management'>registered users</a>;
          <a target='_blank'
             href='https://docs.basex.org/main/Jobs_Module#Services'>scheduled services</a>;
          <a target='_blank'
             href='https://docs.basex.org/main/Store_Module'>value stores</a>.
        </div>
        {
          let $headers := (
            { 'key': 'backup', 'label': 'Name', 'order': 'desc' },
            { 'key': 'size', 'label': 'Size', 'type': 'bytes' },
            { 'key': 'comment', 'label': 'Comment' },
            { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
          )
          let $entries :=
            for $backup in db:backups('')
            order by $backup descending
            return {
              'backup': substring-after($backup, '-'),
              'size': $backup/@size,
              'comment': $backup/@comment,
              'action': fn() {
                html:link('Download', 'backup/' || encode-for-uri($backup) || '.zip')
              }
            }
          let $buttons := (
            html:button('backup-create', 'Create…'),
            html:button('databases/backup-restore', 'Restore', ('CHECK', 'CONFIRM')),
            html:button('databases/backup-drop', 'Drop', ('CHECK', 'CONFIRM'))
          )
          let $params := { 'name': '' }
          return html:table($headers, $entries, $buttons, $params)
        }
      </form>
    </div>
  ) => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
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
  utils:dispatch($action, {
    'drop': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'database', 'dropped'),
      'run' : %updating fn() { $args?name ! db:drop(.) }
    } },
    'optimize': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'database', 'optimized'),
      'run' : %updating fn() { $args?name ! db:optimize(.) }
    } },
    'backups-create': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'database', 'backed up'),
      'run' : %updating fn() { $args?name ! db:create-backup(.) }
    } },
    'backups-restore': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'backup', 'restored'),
      'run' : %updating fn() { $args?name ! db:restore(.) }
    } },
    'backup-drop': fn($args) {
      let $name := string($args?name)
      return {
        'page'  : if ($name) then $dba:SUB else $dba:CAT,
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
        'page'  : if ($name) then $dba:SUB else $dba:CAT,
        'params': { 'name': $name },
        'info'  : utils:info($backup, 'backup', 'restored'),
        'run'   : %updating fn() { db:restore($name || '-' || $backup) }
      }
    },
    'resource-delete': fn($args) { {
      'page'  : $dba:SUB,
      'params': { 'name': $args?name },
      'info'  : utils:info($args?resource, 'resource', 'deleted'),
      'run'   : %updating fn() { $args?resource ! db:delete($args?name, .) }
    } }
  })
};
