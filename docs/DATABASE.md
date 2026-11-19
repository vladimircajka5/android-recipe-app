# RecipeApp - Database Design

## 1. Storage responsibilities

- Firebase Authentication manages credentials, sign-in and email verification.
- Cloud Firestore stores shared application data and remote copies of
  personal pantry and shopping data.
- Room stores personal pantry and shopping data locally so these features
  remain available without an internet connection.
- Shared entities use stable identifiers. References to authenticated
  users use their Firebase Authentication UID.
- Passwords and password hashes are not stored in Room or Firestore.

## 2. Media for the next checkpoint

- Firebase Storage is not used.
- User-selected images are copied into app-internal storage.
- Local image references are valid only on the device that stores the files.
- Local image references must not be treated as publicly accessible URLs.
- Recipe videos are represented by validated YouTube links.
- Sharing uploaded images across devices remains an unresolved requirement.

## 3. Personal data and synchronization

- Pantry and shopping data must be isolated by user.
- Local changes must survive application restarts and network loss.
- Failed synchronization must preserve changes for a later retry.
- Synchronization must support creation, updates and deletion.
- Database schema changes must preserve existing data through migrations.