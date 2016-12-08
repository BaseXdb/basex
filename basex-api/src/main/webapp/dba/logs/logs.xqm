(:~
 : Logging page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/logs';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Logging page.
 : @param  $sort     table sort key
 : @param  $name     name (date) of log file
 : @param  $loglist  search term for log list
 : @param  $logs     search term for log entries
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
  let $logs := head(($loglist, $logs)[.])
  let $error := if($loglists) then $error else "No log files found."
  return tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='230'>
        <form action="javascript:void(0);">
          <h2>{
            if($name) then html:link('Logs:', $dba:CAT) else 'Logs:', ' ',
            <input size="14" name="loglist" id="loglist" value="{ $loglist }"
                   placeholder="regular expression" onkeyup="logList(false);"/>
          }</h2>
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
          <form action="download-logs" method="post" id="resources">
            <h3>
              { $name }:&#xa0;
              <input type="hidden" name="name" value="{ $name }"/>
              <input type="hidden" name="loglist" value="{ $loglist }"/>
              <input size="40" id="logs" name="logs" value="{ $logs }"
                placeholder="regular expression"
                onkeyup="javascript:void(0);logEntries(false);"/>
              { html:button('download', 'Download') }
            </h3>
          </form>,
          <div id='output'/>,
          <script type="text/javascript">(function(){{ logEntries(true); }})();</script>
        ) else (),
        html:focus(if($name) then 'logs' else 'loglist')
      }</td>
    </tr>
  )
};

(:~
 : Returns entries of a specific log file.
 : @param  $query    search term
 : @param  $names    name of selected log files
 : @param  $sort     table sort key
 : @param  $loglist  search term for log list
 : @param  $page     current page
 : @return html elements
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/log")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("loglist", "{$loglist}")
  %rest:query-param("sort",    "{$sort}", "")
  %rest:query-param("page",    "{$page}", 1)
  %output:method("html")
  %output:indent("no")
  %rest:single
function dba:log(
  $name     as xs:string,
  $query    as xs:string?,
  $loglist  as xs:string?,
  $sort     as xs:string,
  $page     as xs:integer
) as element()* {
  cons:check(),

  let $data := try {
    admin:logs($name, true())[matches(., $query, 'i')]
  } catch * { (: ignored :) }

  let $rows :=
    for $log in $data
    return <row time='{ $log/@time }' address='{ $log/@address }' user='{ $log/@user}'
                type='{ $log/@type }' ms='{ $log/@ms }' message='{ $log }'/>
  let $headers := (
    <time type='time' order='desc'>Time</time>,
    <address>Address</address>,
    <user>User</user>,
    <type>Type</type>,
    <ms type='decimal' order='desc'>ms</ms>,
    <message>Message</message>
  )
  return html:table($headers, $rows, (),
    map { 'name': $name, 'loglist': $loglist, 'logs': $query },
    map { 'sort': head(($sort[.], 'time')), 'page': $page }
  )
};

(:~
 : Returns log files matching the specified query.
 : @param  $sort   table sort key
 : @param  $query  search term
 : @return html elements
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/loglist")
  %rest:query-param("sort", "{$sort}")
  %output:method("html")
  %rest:single
function dba:loglist(
  $sort   as xs:string?,
  $query  as xs:string?
) as element()* {
  cons:check(),

  let $data := try {
    for $log in admin:logs()
    let $date := string($log)
    where not($query) or (
      some $entry in admin:logs($date) satisfies matches($entry, $query, 'i')
    )
    order by $date descending
    return $log
  } catch * { (: ignored :) }
  let $rows :=
    for $a in $data
    order by $a descending
    return <row name='{ $a }' size='{ $a/@size }'/>
  let $headers := (
    <name>Name</name>,
    <size type='bytes'>Size</size>
  )
  let $buttons := html:button('delete-logs', 'Delete', true())
  let $link := function($value) { $dba:CAT }
  return html:table($headers, $rows, $buttons, map { 'sort': $sort, 'loglist': $query },
    map { 'link': $link })
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of selected log files
 :)
declare
  %rest:POST
  %rest:path("/dba/logs")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %output:method("html")
function dba:logs-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $names[.], 'redirect': $dba:CAT })
};
