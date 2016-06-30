(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/jobs-users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'jobs-users';

(:~
 : Jobs and users page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/jobs-users")
  %rest:query-param("sort", "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function _:jobs-users(
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
            let $you := if($id = $curr) then '✓' else '–'
            order by $ms descending
            return <e id='{ $id }' type='{ $job/@type }'
              sec='{ $ms div 1000 }' state='{ $job/@state }' user='{ $job/@user }' you='{ $you }'/>
            
          let $headers := (
            element id { html:label($entries, ('ID', 'IDs')) },
            element type { 'Type' },
            element sec { 'Seconds' },
            element state { 'State' },
            element user { 'User' },
            element you { 'Yourself' }
          )
          let $buttons := (
            html:button('stop-job', 'Stop', true())
          )
          let $link := function($value) { 'job' }
          return html:table($entries, $headers, $buttons)
        }
        </form>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Server Sessions</h2>
        {
          let $entries :=
            for $id in Sessions:ids()
            for $name in Sessions:names($id)
            for $session in Sessions:get($id, $name)
            let $access := html:date(Sessions:accessed($id))
            let $you := if(Session:id() = $id) then '✓' else '–'
            let $value :=
              if ($session instance of element()) then $session/name
              else if($session instance of xs:string) then $session else ()
            return <e id='{ $id || '|' || $name }' name='{ $name }' value='{ $value }'
             access='{ $access }' you='{ $you }'/>
          let $headers := (
            element id { attribute type { 'id' }, html:label($entries, ('ID', 'IDs')) },
            element name { 'Name' },
            element value { 'Value' },
            element access { 'Last Access' },
            element you { 'Yourself' }
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
          let $entries :=
            for $entry in $data/users/user
            let $you := if($cons:SESSION/name = $entry/@name) then '✓' else '–'
            return <e name='{ $entry/@name }' perm='{ $entry/@permission }' you='{ $you }'/>
          let $headers := (
            element name { html:label($entries, ('User', 'Users')) },
            element perm { 'Permission' },
            element you { 'Yourself' }
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
  %rest:path("/dba/jobs-users")
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
