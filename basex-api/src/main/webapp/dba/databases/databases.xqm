(:~
 : Main page.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Main page.
 : @param  $sort   table sort key
 : @param  $error  error string
 : @param  $info   info string
 : @param  $page   current page
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
  let $names := map:merge(db:list() ! map:entry(., true()))
  let $databases :=
    let $start := util:start($page, $sort)
    let $end := util:end($page, $sort)
    for $db in db:list-details()[position() = $start to $end]
    return map {
      'name': $db,
      'resources': $db/@resources,
      'size': $db/@size,
      'date': $db/@modified-date
    }
  let $backups :=
    for $backup in db:backups()
    where matches($backup, $util:BACKUP-REGEX)
    group by $name := replace($backup, $util:BACKUP-REGEX, '$1')
    where $name and not($names($name))
    let $date := replace(sort($backup)[last()], $util:BACKUP-REGEX, '$2T$3:$4:$5Z')
    return map {
      'name': $name,
      'size': (),
      'date': $date
    }

  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action='{ $dba:CAT }' method='post' class='update'>
          <h2>Databases</h2>
          {
            let $headers := (
              map { 'key': 'name', 'label': 'Name' },
              map { 'key': 'resources', 'label': 'Count', 'type': 'number', 'order': 'desc' },
              map { 'key': 'size', 'label': 'Bytes', 'type': 'bytes', 'order': 'desc' },
              map { 'key': 'date', 'label': 'Last Modified', 'type': 'dateTime', 'order': 'desc' }
            )
            let $entries := ($databases, $backups)
            let $buttons := (
              html:button('db-create', 'Create…'),
              html:button('db-optimize-all', 'Optimize'),
              html:button('db-drop', 'Drop', true()),
              html:button('backup-create-all', 'Back up'),
              html:button('backup-restore-all', 'Restore', true())
            )
            let $count := map:size($names) + count($backups)
            let $options := map {
              'sort': $sort,
              'link': 'database',
              'page': $page,
              'count': $count
            }
            return html:table($headers, $entries, $buttons, map { }, $options)
          }
        </form>
      </td>
      <td class='vertical'/>
      <td>
        <h2>Upload Backups</h2>
        <form action='backup-upload' method='post' enctype='multipart/form-data'>
          <input type='file' name='files' multiple='multiple'/>
          <input type='submit' value='Send'/>
        </form>
        <div class='note'>
          Ensure that your server has enough RAM assigned to upload large backups.
        </div>
        <div class='small'/>
        <form action='{ $dba:CAT }' method='post' class='update'>
          <input type='hidden' name='name' value=''/>
          <h2>General Backups</h2>
          <div class='note'>
            Comprising:
            <a target='_blank' href='https://docs.basex.org/wiki/User_Management'>registered users</a>;
            <a target='_blank' href='https://docs.basex.org/wiki/Jobs_Module#Services'>scheduled services</a>;
            <a target='_blank' href='https://docs.basex.org/wiki/Store_Module'>value stores</a>.
          </div>
          <div class='small'/>
          {
            let $headers := (
              map { 'key': 'backup', 'label': 'Name', 'order': 'desc' },
              map { 'key': 'size', 'label': 'Size', 'type': 'bytes' },
              map { 'key': 'comment', 'label': 'Comment' },
              map { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
            )
            let $entries :=
              for $backup in db:backups('')
              order by $backup descending
              return map {
                'backup': substring-after($backup, '-'),
                'size': $backup/@size,
                'comment': $backup/@comment,
                'action': function() {
                  html:link('Download', 'backup/' || encode-for-uri($backup) || '.zip')
                }
              }
            let $buttons := (
              html:button('backup-create', 'Create…', false(), map { 'class': 'global' }),
              html:button('backup-restore', 'Restore', true()),
              html:button('backup-drop', 'Drop', true())
            )
            let $params := map { 'name': '' }
            return html:table($headers, $entries, $buttons, $params, map { })
          }
        </form>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action   action to perform
 : @param  $names    names of selected databases
 : @param  $backups  backups
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/databases')
  %rest:query-param('action', '{$action}')
  %rest:query-param('name',   '{$names}')
  %rest:query-param('backup',  '{$backups}')
function dba:databases-redirect(
  $action   as xs:string,
  $names    as xs:string*,
  $backups  as xs:string*
) as element(rest:response) {
  web:redirect($action,
    if($action = ('db-create')) then (
      map { }
    ) else if($action = ('backup-create', 'backup-drop', 'backup-restore')) then (
      map { 'redirect': $dba:CAT, 'backup': $backups }
    ) else (
      map { 'name': $names }
    )
  )
};
