(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Jobs and users page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/users")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:users(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Users</h2>
        {
          let $rows :=
            for $user in user:list-details()
            let $name := string($user/@name)
            let $you := if($cons:SESSION-VALUE = $name) then '&#x2713;' else '–'
            return <row name='{ $name }' perm='{ $user/@permission }' you='{ $you }'/>
          let $headers := (
            <name>Name</name>,
            <perm>Permission</perm>,
            <you>You</you>
          )
          let $buttons := (
            html:button('create-user', 'Create…'),
            html:button('drop-user', 'Drop', true())
          )
          let $link := function($value) { 'user' }
          return html:table($headers, $rows, $buttons, map {}, map { 'link': $link })
        }
        </form>
        <div>&#xa0;</div>
      </td>
      <td class='vertical'/>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Web Sessions</h2>
        {
          let $rows :=
            for $id in Sessions:ids()
            let $access := Sessions:accessed($id)
            let $you := if(Session:id() = $id) then '&#x2713;' else '–'
            (: supported session ids (application-specific, can be extended :)
            for $name in Sessions:names($id)[. = ('dba', 'id')]
            let $value := try {
              Sessions:get($id, $name)
            } catch bxerr:BXSE0002 {
              (: non-XQuery session value :)
            }
            let $string := util:chop(serialize($value, map { 'method': 'basex' }), 20)
            order by $access descending
            return <row id='{ $id || '|' || $name }' name='{ $name }' value='{ $string }'
                        access='{ $access }' you='{ $you }'/>
          let $headers := (
            <id type='id'>ID</id>,
            <name>Name</name>,
            <value>Value</value>,
            <access type='dateTime' order='desc'>Last Access</access>,
            <you>You</you>
          )
          let $buttons := (
            html:button('kill-session', 'Kill', true())
          )
          return html:table($headers, $rows, $buttons, map {}, map {})
        }
        </form>
        <h2>Database Sessions</h2>
        {
          let $rows :=
            for $session in admin:sessions()
            return <row address='{ $session/@address }' user='{ $session/@user }'/>
          let $headers := (
            <address>Address</address>,
            <user>User</user>
          )
          return html:table($headers, $rows, (), map {}, map {})
        }
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
  %rest:path("/dba/users")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %rest:query-param("id",     "{$ids}")
  %output:method("html")
function dba:users-redirect(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action,
    if($action = 'create-user') then map { }
    else if($action = 'kill-session') then map { 'id': $ids }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
