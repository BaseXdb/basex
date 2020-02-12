(:~
 : Users page.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

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
  %rest:path('/dba/users')
  %rest:query-param('sort',  '{$sort}', 'name')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:users(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  html:wrap(
    map {
      'header': $dba:CAT, 'info': $info, 'error': $error,
      'css': 'codemirror/lib/codemirror.css',
      'scripts': ('codemirror/lib/codemirror.js', 'codemirror/mode/xml/xml.js')
    },
    <tr>
      <td>
        <form action='{ $dba:CAT }' method='post' class='update'>
        <h2>Users</h2>
        {
          let $headers := (
            map { 'key': 'name', 'label': 'Name' },
            map { 'key': 'permission', 'label': 'Permission' },
            map { 'key': 'you', 'label': 'You' }
          )
          let $entries := (
            let $current := session:get($config:SESSION-KEY)
            for $user in user:list-details()
            let $name := string($user/@name)
            return map {
              'name': $name,
              'permission': $user/@permission,
              'you': if($current = $name) then '✓' else '–'
            }
          )
          let $buttons := (
            html:button('user-create', 'Create…'),
            html:button('user-drop', 'Drop', true())
          )
          let $options := map { 'link': 'user', 'sort': $sort }
          return html:table($headers, $entries, $buttons, map { }, $options)
        }
        </form>
        <div>&#xa0;</div>
      </td>
      <td class='vertical'/>
      <td>
        <form action='users-info' method='post'>{
          <h3>Extra Information</h3>,
          html:button('save', 'Save'),
          <div class='small'/>,
          <textarea name='info' id='editor' spellcheck='false'>{
            serialize(user:info())
          }</textarea>,
          html:js('loadCodeMirror("xml", true);')
        }</form>
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
  %rest:path('/dba/users')
  %rest:query-param('action', '{$action}')
  %rest:query-param('name',   '{$names}')
  %rest:query-param('id',     '{$ids}')
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
