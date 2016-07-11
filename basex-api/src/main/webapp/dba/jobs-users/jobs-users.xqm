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
      <td width='69%'>
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
            return <e id='{ $id }' type='{ $job/@type }' state='{ $job/@state }'
              user='{ $job/@user }' sec='{ $ms div 1000 }' start='{ $job/@start}'
              end='{ $job/@end }' you='{ $you }'/>
            
          let $headers := (
            <id>{ html:label($entries, ('ID', 'IDs')) }</id>,
            <type>Type</type>,
            <state>State</state>,
            <user>User</user>,
            <sec type='number' order='desc'>Seconds</sec>,
            <start type='dateTime' order='desc'>Start</start>,
            <end type='dateTime' order='desc'>End</end>,
            <you>You</you>
          )
          let $buttons := (
            html:button('stop-job', 'Stop', true())
          )
          return html:table($entries, $headers, $buttons, map {}, $sort)
        }
        </form>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Server Sessions</h2>
        {
          let $entries :=
            for $id in Sessions:ids()
            let $access := Sessions:accessed($id)
            let $you := if(Session:id() = $id) then '✓' else '–'
            for $name in Sessions:names($id)
            for $value in try {
              let $attr := Sessions:get($id, $name)
              return if($attr instance of element()) then (
                $attr/name
              ) else if($attr instance of xs:string) then (
                $attr
              ) else (
                '...'
              )
            } catch bxerr:BXSE0002 {
              (: ignore non-XQuery session values :)
            }
            return <e id='{ $id || '|' || $name }' name='{ $name }' value='{ $value }'
             access='{ $access }' you='{ $you }'/>
          let $headers := (
            <id type='id'>{  html:label($entries, ('ID', 'IDs')) }</id>,
            <name>Name</name>,
            <value>Value</value>,
            <access type='dateTime' order='desc'>Last Access</access>,
            <you>You</you>
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
            <addr>{ html:label($entries, ('Session', 'Sessions')) }</addr>,
            <user>Address</user>
          )
          return html:table($entries, $headers, ())
        }
      </td>
      <td class='vertical'/>
      <td width='29%'>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Users</h2>
        {
          let $entries :=
            for $entry in $data/users/user
            let $you := if($cons:SESSION/name = $entry/@name) then '✓' else '–'
            return <e name='{ $entry/@name }' perm='{ $entry/@permission }' you='{ $you }'/>
          let $headers := (
            <name>{ html:label($entries, ('User', 'Users')) }</name>,
            <perm>Permission</perm>,
            <you>You</you>
          )
          let $buttons := (
            html:button('create-user', 'Create…'),
            html:button('drop-user', 'Drop', true())
          )
          let $link := function($value) { 'user' }
          return html:table($entries, $headers, $buttons, map {}, (), $link)
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
