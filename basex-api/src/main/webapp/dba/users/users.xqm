(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/users';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace session = 'dba/session' at '../modules/session.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Returns the users page.
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
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Users</h2>
        {
          let $headers := (
            <name>Name</name>,
            <perm>Permission</perm>,
            <you>You</you>
          )
          let $rows :=
            for $user in user:list-details()
            let $name := string($user/@name)
            let $you := if($session:VALUE = $name) then '✓' else '–'
            return <row name='{ $name }' perm='{ $user/@permission }' you='{ $you }'/>
          let $buttons := (
            html:button('user-create', 'Create…'),
            html:button('user-drop', 'Drop', true())
          )
          let $link := function($value) { 'user' }
          return html:table($headers, $rows, $buttons, map { }, map { 'link': $link })
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
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/users")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %rest:query-param("id",     "{$ids}")
function dba:users-redirect(
  $action  as xs:string,
  $names   as xs:string*,
  $ids     as xs:string*
) as element(rest:response) {
  web:redirect($action,
    if($action = 'user-create') then map { }
    else map { 'name': $names, 'redirect': $dba:CAT }
  )
};
