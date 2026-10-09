(:~
 : Panels of the users view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/user-panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';

(:~ Page the links of the panels refer to. :)
declare %private variable $panels:CAT := 'users';

(:~
 : Creates the contents of the users panel: the users to choose from.
 : @param  $sort  sort key of the user list
 : @param  $page  number of shown pages of the user list
 : @param  $name  selected user
 : @return panel contents
 :)
declare function panels:users(
  $sort  as xs:string,
  $page  as xs:integer,
  $name  as xs:string?
) as element()+ {
  <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>{
    let $headers := (
      (: the values of a known format get the width they need; the name takes the rest :)
      { 'key': 'name', 'label': 'Name' },
      { 'key': 'permission', 'label': 'Permission', 'width': '6.5rem' },
      { 'key': 'you', 'label': 'You', 'width': '3rem' }
    )
    let $entries := (
      let $current := session:get($config:SESSION-KEY)
      for $user in user:list-details()
      let $user-name := string($user/@name)
      return {
        (: the link names the whole selection, so it can be followed and bookmarked; the
           view follows it in place, over the connection it opened for its panels :)
        'name': html:select($user-name, $panels:CAT, { 'name': $user-name },
          $user-name = $name, 'name', 'selectUser'),
        (: local permissions are counted; the patterns are named by the tooltip :)
        'permission': fn() {
          let $local := $user/database
          return if ($local) then (
            <span title='{ string-join($local ! (@pattern || ' → ' || @permission), ', ') }'>{
              $user/@permission || ' +' || count($local)
            }</span>
          ) else string($user/@permission)
        },
        'you': fn() {
          if ($current = $user-name) then html:symbol('✓', 'yes') else html:symbol('–', 'no')
        }
      }
    )
    let $buttons := (
      <button type='button' onclick='showDialog("create")'
              title='Create a new user'>New…</button>,
      form:button('users/drop', 'Drop', ('CHECK', 'CONFIRM'),
        title := 'Delete the selected users')
    )
    return table:create($headers, $entries, $buttons, {},
      { 'sort': $sort, 'page': $page, 'presort': 'name', 'sticky': <h2>Users</h2>,
        'noun': 'user' })
  }</form>,

  form:dialog('create', 'New User', 'users/create', false(), (
    form:field('Name:', <input type='text' name='name' autofocus='' required=''/>),
    form:field('Password:', <input type='password' name='pw' autocomplete='new-password'/>),
    form:field('Permission:', form:select('perm', $config:PERMISSIONS[position() <= 5], 'none'))
  ))
};

(:~
 : Creates the contents of the user panel: what the selected user is.
 : @param  $name     selected user
 : @param  $newname  name that was entered but could not be assigned
 : @param  $perm     permission that was entered but could not be assigned
 : @return panel contents; empty if no existing user is selected
 :)
declare function panels:user(
  $name     as xs:string?,
  $newname  as xs:string?,
  $perm     as xs:string?
) as element()* {
  (: the form that submits them is the panel itself and outlives them; see users.xqm :)
  (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  if ($name and user:exists($name)) {
    let $user := user:list-details($name)
    (: the admin is the one user whose name and permission are not up for discussion :)
    let $admin := $name eq 'admin'
    return (
      <h2>{ 'User: ' || $name }</h2>,
      <div class='buttons'><button title='Save the changes to this user'>Save</button></div>,
      <input type='hidden' name='name' value='{ $name }'/>,
      if ($admin) {
        <input type='hidden' name='newname' value='admin'/>,
        <input type='hidden' name='perm' value='admin'/>
      },
      (: who the user is on the left, what it may do on the right :)
      <div class='field-columns'>{
        <div>{
          if (not($admin)) {
            form:field('Name:',
              <input type='text' name='newname' value='{ $newname otherwise $name }'/>)
          },
          form:field('Password:', (
            <input type='password' name='pw' autocomplete='new-password'/>,
            <div class='note'>…only changed if a new one is entered</div>
          ))
        }</div>,
        if (not($admin)) {
          <div>{
            form:field('Permission:', form:select('perm', $config:PERMISSIONS[position() <= 5],
              $perm otherwise $user/@permission)),
            panels:local-permissions($user)
          }</div>
        }
      }</div>,
      (: the editor is named apart from the user, and takes the height that is left :)
      <h3>User Data</h3>,
      panels:info-editor('editor', 'Custom XML data for this user', $name)
    )
  }
};

(:~
 : Creates the field with the local permissions of a user: the databases on which it is granted
 : a permission of its own.
 : @param  $user  user details
 : @return field
 :)
declare %private function panels:local-permissions(
  $user  as element(user)
) as element() {
  (: the field is part of the user form: a pattern is dropped by a button that submits it to an
     action of its own, and added in a dialog outside the form :)
  form:field('Per database:', (
    for $db in $user/database
    return <div>{
      <code>{ string($db/@pattern) }</code>, ' → ', string($db/@permission), ' ',
      <button class='link' formaction='users/pattern-drop' name='pattern'
              value='{ $db/@pattern }' title='Delete the database permission'
              onclick='return confirmAction(this, "Drop")'>✕</button>
    }</div>,
    <a href='#' onclick='addPattern(); return false;'
       title='Add a permission for databases whose names match a pattern'>Add…</a>
  ), title := 'Permissions that override the global one for databases whose names match a pattern')
};

(:~
 : Creates the dialog that adds a local permission to the selected user.
 : @return dialog
 :)
declare function panels:pattern-dialog() as element(dialog) {
  (: the user is filled in when the dialog is opened; see addPattern :)
  form:dialog('pattern', 'Add Database Permission', 'users/pattern-add', false(), (
    <input type='hidden' name='name' id='pattern-user'/>,
    form:field('Pattern:', <input type='text' name='pattern' autofocus='' required=''/>,
      title := 'Database names, with * and ? as wildcards'),
    (: a local permission cannot grant more than access to the data :)
    form:field('Permission:', form:select('perm', $config:PERMISSIONS[position() <= 3], 'write')),
    <div class='note'>
      A local permission overrides the global one for databases whose name matches its pattern
      (<a target='_blank'
        href='https://docs.basex.org/main/Commands#glob_syntax'>glob syntax</a>).
      The first matching pattern applies.
    </div>
  ))
};

(:~
 : Creates the contents of the information panel: the information that is attached to no user
 : in particular.
 : @return panel contents
 :)
declare function panels:information() as element()+ {
  <form method='post' action='users/info' autocomplete='off' class='pane column'>
    <h2>General User Data</h2>
    <div class='buttons'><button title='Save the general user data'>Save</button></div>
    {
      panels:info-editor('user-info',
        'Custom XML data that belongs to no user in particular', ())
    }
  </form>
};

(:~
 : Creates the editor for custom user data, and the note that says what the data belongs to.
 : @param  $id    id of the editor
 : @param  $note  what the data belongs to
 : @param  $name  user; empty sequence for the data that belongs to no user in particular
 : @return note and editor
 :)
declare %private function panels:info-editor(
  $id    as xs:string,
  $note  as xs:string,
  $name  as xs:string?
) as element()+ {
  <div class='note'>{ $note }, with an &lt;info&gt; root element.</div>,
  <textarea name='info' id='{ $id }' spellcheck='false'>{
    serialize(user:info($name), { 'indent': true() })
  }</textarea>
};
