(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'users';

(:~
 : Users page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/users")
  %rest:query-param("sort", "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function _:users(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  (: request data in a single step :)
  let $data := try {
    util:eval('element result {
      element users { user:list-details() },
      element sessions { admin:sessions() }
    }')
  } catch * {
    element error { $cons:DATA-ERROR || ': ' || $err:description }
  }
  let $error := ($data/self::error/string(), $error)[1]

  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td width='49%'>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Users</h2>
        {
          let $entries := $data/users/user/<e name='{ @name }' perm='{ @permission }'/>
          let $headers := (
            <name>{ html:label($entries, ('User', 'Users')) }</name>,
            <perm>Permission</perm>
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
      <td class='vertical'/>
      <td width='49%'>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Browser Sessions</h2>
        {
          let $entries :=
            for $id in Sessions:ids()
            for $session in Sessions:get($id, $cons:SESSION-KEY)
            let $name := $session/name || ' (you)'[Session:id() = $id]
            let $addr := (string-join($session/(host, port), ":")[.], 'local')[1]
            let $access := format-dateTime(Sessions:accessed($id), '[Y]-[M2]-[D2], [H]:[m]:[s]')
            return <e id='{ $id }' user='{ $name }' addr='{ $addr }' access='{ $access }'/>
          let $headers := (
            <id type='id'/>,
            <user>User</user>,
            <addr>Address</addr>,
            <access>Last Access</access>
          )
          let $buttons := (
            html:button('kill-dba', 'Kill', true())
          )
          return html:table($entries, $headers, $buttons)
        }
        </form>
        <h2>Database Client Sessions</h2>
        {
          let $entries := $data/sessions/session/<e addr='{ @address }' user='{ @user }'/>
          let $headers := (
            <addr>{ html:label($entries, ('Session', 'Sessions')) }</addr>,
            <user>Address</user>
          )
          return html:table($entries, $headers, ())
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
function _:action(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) {
  web:redirect($action,
    if($action = 'create-user') then map { }
    else if($action = 'kill-dba') then map { 'id': $ids }
    else map { 'name': $names, 'redirect': $_:CAT }
  )
};
