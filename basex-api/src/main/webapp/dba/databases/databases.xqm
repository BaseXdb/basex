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
  %output:html-version('5')
function dba:databases(
  $sort   as xs:string,
  $page   as xs:integer,
  $info   as xs:string?,
  $error  as xs:string?
) as element(html) {
  let $db-names := db:list()
  let $databases :=
    let $start := utils:start($page, $sort)
    let $end := utils:end($page, $sort)
    for $db in db:list-details()[position() = $start to $end]
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
    <tr>
      <td>
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
              html:button('dbs-optimize', 'Optimize', 'CHECK'),
              html:button('dbs-drop', 'Drop', ('CHECK', 'CONFIRM')),
              html:button('backups-create', 'Back up', 'CHECK'),
              html:button('backups-restore', 'Restore', ('CHECK', 'CONFIRM'))
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
      </td>
      <td class='vertical'/>
      <td>
        <h2>Upload Backups</h2>
        <form method='post' enctype='multipart/form-data' autocomplete='off'>{
          <input type='file' name='files' multiple='multiple'/>,
          html:button('backup-upload', 'Upload')
        }</form>
        <div class='note'>
          Ensure that your server has enough RAM assigned to upload large backups.
        </div>
        <div class='small'/>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='name' value=''/>
          <h2>General Backups</h2>
          <div class='note'>
            Comprising:
            <a target='_blank'
               href='https://docs.basex.org/wiki/User_Management'>registered users</a>;
            <a target='_blank'
               href='https://docs.basex.org/wiki/Jobs_Module#Services'>scheduled services</a>;
            <a target='_blank'
               href='https://docs.basex.org/wiki/Store_Module'>value stores</a>.
          </div>
          <div class='small'/>
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
              html:button('backup-restore', 'Restore', ('CHECK', 'CONFIRM')),
              html:button('backup-drop', 'Drop', ('CHECK', 'CONFIRM'))
            )
            let $params := { 'name': '' }
            return html:table($headers, $entries, $buttons, $params)
          }
        </form>
      </td>
    </tr>
    => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
  )
};
