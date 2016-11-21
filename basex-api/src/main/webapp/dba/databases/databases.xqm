(:~
 : Main page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
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
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %rest:query-param("page",  "{$page}", 1)
  %output:method("html")
function dba:databases(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:integer?
) as element(html) {
  cons:check(),

  (: request data in a single step :)
  let $data := try {
    util:eval('element result {
      element databases { db:list-details() },
      element backups { db:backups() },
      db:system()
    }')
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $error := ($data/self::error/string(), $error)[1]

  return tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $dba:CAT }" method="post" class="update">
          <h2>Databases</h2>
          {
            let $entries :=
              for $db in $data/databases/database
              return <row name='{ $db }' resources='{ $db/@resources }' size='{ $db/@size }'
                          date='{ $db/@modified-date }'/>
            (: integrate backups in list :)
            let $rows := (
              $entries,
              for $backup in $data/backups/backup
              let $file := $backup/text()
              order by $file descending
              group by $name := replace($file, '-\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}$', '')
              where not($entries/@name = $name)
              let $date := replace($file[1], '^.*(\d{4}-\d{2}-\d{2})-(\d{2})-(\d{2})-(\d{2})$',
                '$1T$2:$3:$4Z')
              return <row name='{ $name }' resources='' size='(backup)' date='{ $date }'/>
            )
            let $headers := (
              <name>Name</name>,
              <resources type='number' order='desc'>Resources</resources>,
              <size type='bytes' order='desc'>Size</size>,
              <date type='dateTime' order='desc'>Modification Date</date>
            )
            let $buttons := (
              html:button('create-db', 'Create…'),
              html:button('optimize-all', 'Optimize'),
              html:button('drop-db', 'Drop', true())
            )
            let $link := function($value) { 'database' }
            return html:table($headers, $rows, $buttons, map { }, $sort, $link, $page)
          }
        </form>
      </td>
      <td class='vertical'/>
      <td width='49%'>
        <table>
          <tr>{
            for $table in $data/system/html:properties(.)
            let $th := $table/tr[th][3]
            return (
              <td>
                <h2>System</h2>
                <table>{ $th/preceding-sibling::*/. }</table>
              </td>,
              <td>
                <h2> </h2>
                <table>{ $th, $th/following-sibling::* }</table>
              </td>
            )
          }</tr>
        </table>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of selected databases
 :)
declare
  %rest:POST
  %rest:path("/dba/databases")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %output:method("html")
function dba:action(
  $action  as xs:string,
  $names   as xs:string*
) {
  web:redirect($action,
    if($action = 'create-db') then map { }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
