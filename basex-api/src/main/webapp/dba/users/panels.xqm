(:~
 : Panels of the users view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/user-panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';

(:~ Page the links of the panels refer to. :)
declare %private variable $panels:CAT := 'users';

(:~
 : Creates the contents of the users panel: the users to choose from.
 : @param  $sort  sort key of the user list
 : @param  $name  selected user
 : @return panel contents
 :)
declare function panels:users(
  $sort  as xs:string,
  $name  as xs:string?
) as element()+ {
  <form method='post' autocomplete='off'>{
    let $headers := (
      { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '45%' },
      { 'key': 'permission', 'label': 'Permission', 'width': '35%' },
      { 'key': 'you', 'label': 'You', 'width': '20%' }
    )
    let $entries := (
      let $current := session:get($config:SESSION-KEY)
      for $user in user:list-details()
      let $user-name := string($user/@name)
      return {
        (: the link names the whole selection, so it can be followed and bookmarked :)
        'name': fn() {
          <a href='{ web:create-url($panels:CAT, { 'name': $user-name }) }'>{
            attribute class { 'selected' }[$user-name = $name],
            $user-name
          }</a>
        },
        'permission': $user/@permission,
        'you': if ($current = $user-name) then '✓' else '–'
      }
    )
    let $buttons := (
      <button type='button' onclick='showDialog("create")'>New…</button>,
      form:button('users/drop', 'Drop', ('CHECK', 'CONFIRM'))
    )
    return table:create($headers, $entries, $buttons, {},
      { 'sort': $sort, 'presort': 'name', 'sticky': <h2>Users</h2> })
  }</form>,

  form:dialog('create', 'Create User', 'users/create', false(), (
    form:field('Name:', <input type='text' name='name' autofocus='' required=''/>),
    form:field('Password:', <input type='password' name='pw' autocomplete='new-password'/>),
    form:field('Permission:', panels:permission-select('perm', 'none', 5))
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
  if (not($name) or not(user:exists($name))) {
    (: nothing is selected: the panel is not shown, so it needs no placeholder :)
  } else {
    let $user := user:list-details($name)
    (: the admin is the one user whose name and permission are not up for discussion :)
    let $admin := $name eq 'admin'
    return (
      <form method='post' action='users/update' autocomplete='off' class='pane column'>
        <h2>{ 'User: ' || $name }</h2>
        <div class='buttons'><button>Update</button></div>
        <input type='hidden' name='name' value='{ $name }'/>
        {
          if ($admin) {
            <input type='hidden' name='newname' value='admin'/>,
            <input type='hidden' name='perm' value='admin'/>
          } else {
            form:field('Name:',
              <input type='text' name='newname' value='{ $newname otherwise $name }'/>)
          },
          form:field('Password:', (
            <input type='password' name='pw' autocomplete='new-password'/>,
            <div class='note'>…only changed if a new one is entered</div>
          )),
          if (not($admin)) {
            form:field('Permission:',
              panels:permission-select('perm', ($perm otherwise $user/@permission), 5))
          },
          (: the editor is named apart from the user, and takes the height that is left :)
          <h3>User Data</h3>,
          <div class='note'>
            Custom XML data for this user, with an &lt;info&gt; root element.
          </div>,
          <textarea name='info' id='editor' spellcheck='false'>{
            serialize(user:info($name), { 'indent': true() })
          }</textarea>
        }
      </form>
    )
  }
};

(:~
 : Creates the contents of the permissions panel: the databases on which the selected user is
 : granted a permission of its own.
 : @param  $name  selected user
 : @return panel contents; empty if no user is selected, or if it is the admin
 :)
declare function panels:local-permissions(
  $name  as xs:string?
) as element()* {
  (: the admin may do everything everywhere: there is nothing to overwrite :)
  if (not($name) or not(user:exists($name)) or $name eq 'admin') {
    (: the panel is not shown, so it needs no placeholder :)
  } else {
    <form method='post' autocomplete='off'>
      <input type='hidden' name='name' value='{ $name }'/>
      {
        let $headers := (
          { 'key': 'pattern', 'label': 'Pattern' },
          { 'key': 'permission', 'label': 'Permission' }
        )
        let $entries := user:list-details($name)/database ! {
          'pattern': @pattern,
          'permission': @permission
        }
        let $buttons := (
          <button type='button' onclick='showDialog("pattern")'>Add…</button>,
          form:button('users/pattern-drop', 'Drop', ('CHECK', 'CONFIRM'))
        )
        return table:create($headers, $entries, $buttons, { 'name': $name },
          { 'sticky': <h2>Local Permissions</h2> })
      }
    </form>,
    <div class='note'>
      A local permission overrides the global one for databases whose name matches its pattern
      (<a target='_blank'
        href='https://docs.basex.org/main/Commands#glob_syntax'>glob syntax</a>).
    </div>,
    form:dialog('pattern', 'Add Pattern', 'users/pattern-add', false(), (
      <input type='hidden' name='name' value='{ $name }'/>,
      form:field('Pattern:', <input type='text' name='pattern' autofocus='' required=''/>),
      (: a local permission cannot grant more than access to the data :)
      form:field('Permission:', panels:permission-select('perm', 'write', 3))
    ))
  }
};

(:~
 : Creates the contents of the information panel: the information that is attached to no user
 : in particular.
 : @return panel contents
 :)
declare function panels:information() as element()+ {
  <form method='post' action='users/info' autocomplete='off' class='pane column'>
    <h2>General User Data</h2>
    <div class='buttons'><button>Update</button></div>
    <div class='note'>
      Custom XML data that belongs to no user in particular, with an &lt;info&gt; root element.
    </div>
    <textarea name='info' id='user-info' spellcheck='false'>{
      serialize(user:info(), { 'indent': true() })
    }</textarea>
  </form>
};

(:~
 : Creates a chooser for a permission.
 : @param  $name      name of the field
 : @param  $selected  selected permission
 : @param  $count     number of permissions to offer, counted from the least privileged
 : @return chooser
 :)
declare %private function panels:permission-select(
  $name      as xs:string,
  $selected  as xs:anyAtomicType?,
  $count     as xs:integer
) as element(select) {
  <select name='{ $name }'>{
    for $permission in $config:PERMISSIONS[position() <= $count]
    return element option {
      attribute selected { }[$permission = $selected],
      $permission
    }
  }</select>
};
