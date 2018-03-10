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
            map { 'key': 'name', 'label': 'Name' },
            map { 'key': 'permission', 'label': 'Permission' },
            map { 'key': 'you', 'label': 'You' }
          )
          let $entries := (
            for $user in user:list-details()
            let $name := string($user/@name)
            return map {
              'name': $name,
              'permission': $user/@permission,
              'you': if($session:VALUE = $name) then '✓' else '–'
            }
          )
          let $buttons := (
            html:button('user-create', 'Create…'),
            html:button('user-drop', 'Drop', true())
          )
          let $options := map { 'link': function($value) { 'user' }, 'sort': $sort }
          return html:table($headers, $entries, $buttons, map { }, $options)
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
