(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/logs';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Logging page.
 : @param  $sort     table sort key
 : @param  $name     name (date) of log file
 : @param  $loglist  search term for log list
 : @param  $logs     search term for logs
 : @param  $error    error string
 : @param  $info     info string
 : @param  $page     current page
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/logs")
  %rest:query-param("sort",    "{$sort}", "")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("loglist", "{$loglist}")
  %rest:query-param("logs",    "{$logs}")
  %rest:query-param("error",   "{$error}")
  %rest:query-param("info",    "{$info}")
  %rest:query-param("page",    "{$page}", 1)
  %output:method("html")
function dba:logs(
  $sort     as xs:string,
  $name     as xs:string?,
  $loglist  as xs:string?,
  $logs     as xs:string?,
  $error    as xs:string?,
  $info     as xs:string?,
  $page     as xs:integer
) as element(html) {
  cons:check(),

  let $loglists := dba:loglist($sort, $loglist)
  let $error := if($loglists) then $error else $cons:DATA-ERROR
  return tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='230'>
        <form action="javascript:void(0);">
          <h2>{ if($name) then <a href="{ $dba:CAT }">Logs</a> else 'Logs' }:
            <input size="14" name="loglist" id="loglist" value="{ $loglist }"
                   placeholder="regular expression"
                   onkeyup="logList();"/>
          </h2>
        </form>
        <form action="{ $dba:CAT }" method="post" class="update" autocomplete="off">
          <input type='hidden' name='name' id='name' value='{ $name }'/>
          <input type='hidden' name='sort' id='sort' value='{ $sort }'/>
          <input type='hidden' name='page' id='page' value='{ $page }'/>
          <div id='list'>{ $loglists }</div>
        </form>
      </td>
      <td class='vertical'/>
      <td>{
        if($name) then (
          <h3>
            { $name }:
            <input size="40" id="logs" value="{ ($loglist, $logs)[1] }"
              placeholder="regular expression"
              onkeyup="logEntries();"/>
          </h3>,
          <div id='output'/>,
          <script type="text/javascript">(function(){{ logEntries(); }})();</script>
        ) else (),
        html:focus(if($name) then 'logs' else 'loglist')
      }</td>
    </tr>
  )
};

(:~
 : Returns log entries of a specific log file.
 : @param  $query    query
 : @param  $names    name of selected log files
 : @param  $sort     table sort key
 : @param  $loglist  loglist
 : @param  $page     current page
 : @return html elements
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/log")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("sort",    "{$sort}")
  %rest:query-param("loglist", "{$loglist}")
  %rest:query-param("page",    "{$page}", 1)
  %output:method("html")
  %output:indent("no")
  %rest:single
function dba:log(
  $name     as xs:string,
  $sort     as xs:string?,
  $query    as xs:string?,
  $loglist  as xs:string?,
  $page     as xs:integer
) as element()* {
  cons:check(),

  let $logs := try {
    util:eval("admin:logs($n, true())[matches(., $q, 'i')]",
      map { 'n': $name, 'q': $query }
    )
  } catch * { () }

  where $logs
  let $rows :=
    for $log in $logs
    return <row time='{ $log/@time }' address='{ $log/@address }' user='{ $log/@user}'
                type='{ $log/@type }' ms='{ $log/@ms }' value='{ $log }'/>
  let $headers := (
    <time type='time' order='desc'>Time</time>,
    <address>Address</address>,
    <user>User</user>,
    <type>Type</type>,
    <ms type='decimal' order='desc'>ms</ms>,
    <value>Value</value>
  )
  return html:table($headers, $rows, (), map { 'name': $name, 'loglist': $loglist, 'logs': $query },
    $sort, (), $page)
};

(:~
 : Returns log data.
 : @param  $sort   table sort key
 : @param  $query  query
 : @return html elements
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/loglist")
  %rest:query-param("sort",  "{$sort}")
  %output:method("html")
  %rest:single
function dba:loglist(
  $sort   as xs:string?,
  $query  as xs:string?
) as element()* {
  cons:check(),

  let $logs := try {
    util:eval("for $a in admin:logs()
      let $n := $a/(@date,text())/string()
      where not($query) or (some $a in admin:logs($n) satisfies matches($a, $query, 'i'))
      order by $n descending
      return $a", map { 'query': $query }
    )
  } catch * { () }
  where $logs
  let $rows :=
    (: legacy (7.8.1): $a/@date :)
    for $a in $logs
    order by $a descending
    (: legacy (7.8.1): $a/@date :)
    return <row name='{ ($a/(@date,text())) }' size='{ $a/@size }'/>
  let $headers := (
    <name>Name</name>,
    <size type='bytes'>Size</size>
  )
  let $buttons := html:button('delete-logs', 'Delete', true())
  let $link := function($value) { $dba:CAT }
  return html:table($headers, $rows, $buttons,
    map { 'sort': $sort, 'loglist': $query }, (), $link, ())
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of selected databases
 :)
declare
  %rest:POST
  %rest:path("/dba/logs")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %output:method("html")
function dba:action(
  $action  as xs:string,
  $names   as xs:string*
) {
  web:redirect($action, map { 'name': $names[.], 'redirect': $dba:CAT })
};
