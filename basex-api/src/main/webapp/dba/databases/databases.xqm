(:~
 : Main page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

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
  cons:check(),

  let $names := map:merge(db:list() ! map:entry(., true()))
  let $databases :=
    let $start := util:start($page, $sort)
    let $end := util:end($page, $sort)
    for $db in db:list-details()[position() = $start to $end]
    return <row name='{ $db }' resources='{ $db/@resources }' size='{ $db/@size }'
                date='{ $db/@modified-date }'/>
  let $backups :=
    let $regex := '^(.*)-(\d{4}-\d\d-\d\d)-(\d\d)-(\d\d)-(\d\d)$'
    for $backup in db:backups()
    where matches($backup, $regex)
    group by $name := replace($backup, $regex, '$1')
    where not($names($name))
    let $date := replace(sort($backup)[last()], $regex, '$2T$3:$4:$5Z')
    return <row name='{ $name }' resources='' size='(backup)' date='{ $date }'/>

  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $dba:CAT }" method="post" class="update">
          <h2>Databases</h2>
          {
            let $headers := (
              <name>Name</name>,
              <resources type='number' order='desc'>Count</resources>,
              <size type='bytes' order='desc'>Bytes</size>,
              <date type='dateTime' order='desc'>Last Modified</date>
            )
            let $rows := ($databases, $backups)
            let $buttons := (
              html:button('db-create', 'Create…'),
              html:button('db-optimize-all', 'Optimize'),
              html:button('db-drop', 'Drop', true())
            )
            let $link := function($value) { 'database' }
            let $count := map:size($names) + count($backups)
            return html:table($headers, $rows, $buttons, map { },
              map { 'sort': $sort, 'link': $link, 'page': $page, 'count': $count })
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
  %output:method("html")
function dba:databases-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action,
    if($action = 'create-db') then map { }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
