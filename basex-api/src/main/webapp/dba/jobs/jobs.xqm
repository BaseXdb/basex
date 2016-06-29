(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/jobs';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'jobs';

(:~
 : Jobs and users page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/jobs")
  %rest:query-param("sort", "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function _:jobs(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  (: request data in a single step :)
  let $data := try {
    util:eval('element result {
      element users { user:list-details() },
      element sessions { admin:sessions() },
      element jobs { jobs:list-details() },
      element current-job { jobs:current() }
    }')
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $error := ($data/self::error/string(), $error)[1]

  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Jobs</h2>
        {
          let $entries :=
            let $curr := $data/current-job
            for $job in $data/jobs/job
            let $id := $job/@id
            let $ms := xs:dayTimeDuration($job/@duration) div xs:dayTimeDuration('PT0.001S')
            order by $ms descending
            return <e id='{ $id || (" (you)"[$id = $curr]) }' type='{ $job/@type }'
              sec='{ $ms div 1000 }' state='{ $job/@state }' user='{ $job/@user }'/>
            
          let $headers := (
            element id { html:label($entries, ('ID', 'IDs')) },
            element type { 'Type' },
            element sec { 'Seconds' },
            element state { 'State' },
            element user { 'User' }
          )
          let $buttons := (
            html:button('stop-job', 'Stop', true())
          )
          let $link := function($value) { 'job' }
          return html:table($entries, $headers, $buttons)
        }
        </form>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Browser Sessions</h2>
        {
          let $entries :=
            for $id in Sessions:ids()
            for $name in Sessions:names($id)
            for $session in Sessions:get($id, $name)
            let $access := format-dateTime(Sessions:accessed($id), '[Y]-[M2]-[D2], [H]:[m]:[s]')
            let $dba := $session instance of element(dba-session)
            let $key := $id || '|' || $name
            return if($dba) then (
              let $user := $session/name || ' (you)'[Session:id() = $id]
              let $addr := (string-join($session/(host, port), ":")[.], 'local')[1]
              return <e id='{ $key }' user='{ $user }' addr='{ $addr }' access='{ $access }'/>
            ) else (
              let $user := 'Application' || ' (you)'[Session:id() = $id]
              return <e id='{ $key }' user='{ $user }' addr='–' access='{ $access }'/>
            )
          let $headers := (
            element id { attribute type { 'id' } },
            element user { 'User' },
            element addr { 'Address' },
            element access { 'Last Access' }
          )
          let $buttons := (
            html:button('kill-session', 'Kill', true())
          )
          return html:table($entries, $headers, $buttons)
        }
        </form>
        <h2>Database Clients</h2>
        {
          let $entries := $data/sessions/session/<e addr='{ @address }' user='{ @user }'/>
          let $headers := (
            element addr { html:label($entries, ('Session', 'Sessions')) },
            element user { 'Address' }
          )
          return html:table($entries, $headers, ())
        }
      </td>
      <td class='vertical'/>
      <td width='49%'>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Users</h2>
        {
          let $entries := $data/users/user/<e name='{ @name }' perm='{ @permission }'/>
          let $headers := (
            element name { html:label($entries, ('User', 'Users')) },
            element perm { 'Permission' }
          )
          let $buttons := (
            html:button('create-user', 'Create…'),
            html:button('drop-user', 'Drop', true())
          )
          let $link := function($value) { 'user' }
          return html:table($entries, $headers, $buttons, map {}, $sort, $link)
        }
        </form>
        <div>&#xa0;</div>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of users
 : @param  $ids     ids
 :)
declare
  %rest:POST
  %rest:path("/dba/jobs")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %rest:query-param("id",     "{$ids}")
  %output:method("html")
function _:action(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) {
  web:redirect($action,
    if($action = 'create-user') then map { }
    else if($action = 'kill-session') then map { 'id': $ids }
    else if($action = 'stop-job') then map { 'id': $ids }
    else map { 'name': $names, 'redirect': $_:CAT }
  )
};
