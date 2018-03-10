(:~
 : Main page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~ Regular expression for backups. :)
declare variable $dba:BACKUP-REGEX := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$';

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
  %rest:path("/dba/databases")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("page",  "{$page}", 1)
  %rest:query-param("info",  "{$info}")
  %rest:query-param("error", "{$error}")
  %output:method("html")
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
    where matches($backup, $dba:BACKUP-REGEX)
    group by $name := replace($backup, $dba:BACKUP-REGEX, '$1')
    where not($names($name))
    let $date := replace(sort($backup)[last()], $dba:BACKUP-REGEX, '$2T$3:$4:$5Z')
    return map {
      'name': $name,
      'size': '(backup)',
      'date': $date
    }

  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $dba:CAT }" method="post" class="update">
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
              html:button('db-drop', 'Drop', true())
            )
            let $link := function($value) { 'database' }
            let $count := map:size($names) + count($backups)
            let $options := map { 'sort': $sort, 'link': $link, 'page': $page, 'count': $count }
            return html:table($headers, $entries, $buttons, map { }, $options)
          }
        </form>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of selected databases
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/databases")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
function dba:databases-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action,
    if($action = 'create-db') then map { }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
