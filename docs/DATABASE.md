# RecipeApp - Database Infrastructure

## 1. Scope of feature/database-setup

This branch provides database infrastructure and connects the data flows
currently implemented in the application.

Its scope includes local persistence, ownership isolation, migrations,
basic Firestore synchronization and integration guidance.

It does not implement all repositories, screens or business rules for
features assigned to other students.

## 2. Storage responsibilities

### Firebase Authentication

Firebase Authentication manages credentials, sign-in and email verification.
Its UID identifies the owner of personal application data.

Passwords and password hashes are not stored in Room or Firestore.

### Room / SQLite

Room provides relational local storage.

Currently implemented tables:

| Table | Purpose |
| --- | --- |
| ingredients | Locally available ingredient reference data |
| products | Locally available product and barcode data |
| pantry_items | Personal pantry entries |
| shopping_lists | Personal shopping lists |
| shopping_list_items | Items belonging to shopping lists |
| sync_tombstones | Pending deletions |
| user_profiles | Cached own profile and pending profile changes |

Ingredient and product references are currently maintained by application
logic. These relationships do not have SQL foreign-key constraints.

### Cloud Firestore

Firestore stores online application data and remote copies of the
personal data currently connected to synchronization.

Current paths:

- users/{uid}
- users/{uid}/pantry/{itemId}
- users/{uid}/shoppingLists/{listId}
- users/{uid}/shoppingItems/{itemId}
- usernames/{usernameKey}

The username registry is used by the existing authentication implementation.

The current rules file covers these existing paths. Rules for future public
collections and administrator operations must be added with those features.

## 3. Agreed offline scope

Personal data is intended to be available offline:

- pantry;
- shopping lists;
- own profile;
- own recipes;
- favorite recipe membership and cached recipe details;
- viewing and cooking history;
- notifications.

This branch currently connects pantry, shopping data and own-profile storage.

Own recipes, favorites, history and notifications are planned integrations.
Their tables and repositories are added when the corresponding features
are implemented.

Shared catalogs, the complete public recipe collection, comments and
ratings do not need to be mirrored entirely into Room.

Offline recipe copies must include the reference data required to display
their contents.

## 4. Ownership and repository access

- Personal records use Firebase UID as ownerId.
- Guests cannot access pantry and shopping screens through main navigation.
- Personal writes require a signed-in user with verified email.
- RoomPantryRepository, RoomShoppingRepository and RoomProfileRepository
  capture their owner when constructed.
- They check that the same owner is active before writing.
- Personal reads use owner-scoped DAO queries.
- Pantry and shopping ViewModels use owner-specific keys.
- A repository instance from the previous account must not be reused as
  the new account's repository.

Ingredient and product tables are reference-data caches and currently
do not have per-user ownership.

## 5. Existing repository implementations

| Interface | Implementation |
| --- | --- |
| AuthRepository | FirebaseAuthRepository |
| IngredientRepository | RoomIngredientRepository |
| ProductRepository | RoomProductRepository |
| PantryRepository | RoomPantryRepository |
| ShoppingRepository | RoomShoppingRepository |
| ProfileRepository | RoomProfileRepository |
| CatalogRepository | Reserved for catalog-management implementation |

A repository does not automatically require both a Room and a Firebase
implementation. Storage responsibilities determine the implementation.

RoomProfileRepository currently supports only the signed-in user's profile.
It preserves the existing username-based interface while using UID for
local ownership.

Its integration with the profile editing screen remains a feature task.

## 6. Local writes and synchronization

Personal pantry, shopping and profile changes are saved locally first.

- syncState is PENDING while a change awaits upload.
- updatedAt stores the modification time in milliseconds.
- localRevision identifies a concrete local version.
- Successful uploads mark only the matching local revision as SYNCED.
- Failed uploads leave changes pending.
- Local deletion creates a sync_tombstones record in the same transaction.
- Firestore represents synchronized deletions with deleted = true.
- A deletion acknowledgement removes only the matching local tombstone.

RecipeApplication coordinates synchronization requests.

Synchronization is requested:

- after connected repository writes;
- when network connectivity becomes available;
- on authentication-state changes;
- when the existing pantry/shopping factories request it.

Only one application-coordinated synchronization runs at a time.
A request received during a run schedules another pass.

Synchronization database work runs on a background executor.

This is in-process synchronization. It is not a WorkManager job and does
not guarantee execution while the application process is stopped.

## 7. Applying remote data

A synchronization uploads pending local changes, then reads remote data.

Remote data is applied in a Room transaction.

- Pending local records are preserved during remote merge.
- Pending local deletions prevent records from being restored.
- Older remote timestamps do not replace newer local records.
- Shopping lists are processed before their items.
- Incoming items require a locally available list owned by the same user.
- Remote list deletion is deferred if its local items have pending changes.
- Applying data stops if the active account changes.
- Local ID collisions with another owner are reported as errors.

Pantry documents include ingredient and optional product snapshots.
Shopping-item documents include ingredient snapshots.

These snapshots restore missing local references on another device.
They do not implement shared-catalog administration.

Legacy documents containing only reference IDs may require resaving on the
source device before they can be restored on a clean device.

### Conflict limitations

The current mechanism protects pending local changes and upload
acknowledgements.

Uploads replace remote document values without a remote-version comparison.
Simultaneous edits on multiple devices can therefore overwrite each other.

updatedAt comparisons depend on device clocks. Advanced conflict resolution
is outside this infrastructure checkpoint.

## 8. Own profile

The signed-in user's Firestore document is cached in user_profiles.

RoomProfileRepository reads the cached biography and location and can save
validated changes locally before synchronization.

Profile uploads update only biography, location fields and updatedAt.
They do not replace the complete user document.

The profile must be downloaded at least once before offline editing.

localImageUri remains device-local and is preserved during profile download.
Profile screens still require explicit connection to the repository.

## 9. Media

Firebase Storage is not used.

- Selected images are copied into app-internal storage.
- Local image URIs are valid only on the device containing the files.
- Internal image paths must not be published as shared image URLs.
- Recipe videos use validated YouTube links.

Sharing uploaded images between devices remains an unresolved requirement
for a later checkpoint.

## 10. Database version and migrations

Current Room schema version: 4.

- 1 -> 2: adds sync_tombstones.
- 2 -> 3: adds localRevision to synchronized personal tables and tombstones.
- 3 -> 4: adds user_profiles.

All migrations are registered in RecipeDatabase.

Destructive migration fallback is not enabled.
Migration checks must install an upgrade over an existing database without
clearing application data.

Schema export is currently disabled.

## 11. Seed data and current limitations

LocalDataSeeder initializes predefined ingredients and preview pantry and
shopping records when the corresponding tables are empty.

Preview records keep preview owner IDs. They are not automatically assigned
to a real account and are excluded from owner-scoped synchronization.

Measurement units and some category choices still use existing application
fixtures/resources. Full shared-catalog management is a separate feature.

allowMainThreadQueries remains enabled for compatibility with current
synchronous UI calls. Future integrations should use background operations
and observable data before removing this option.

Shopping screens currently use synchronous reads. They may require refresh
or reopening to show changes received after the initial render.

AuthManager remains used by the existing change-password flow.
Authentication cleanup and missing authentication features belong to the
authentication task.

## 12. Guidelines for further feature work

When adding a feature:

1. Agree on shared models, identifiers and Firestore paths.
2. Decide whether its data is personal offline data or shared online data.
3. Add Room entities and a data-preserving migration when local storage
   is required.
4. Scope personal reads and writes by Firebase UID.
5. Implement the feature's repository using shared models.
6. Connect its pending writes, acknowledgements and remote merge if manual
   Room/Firestore synchronization is required.
7. Add Firestore rules for the new paths and allowed operations.
8. Verify offline persistence, account changes and migration behavior.

Local cached copies of another author's recipes must not be uploaded as
edits owned by the current user.

Administrator privileges must be enforced by backend rules, not by UI
visibility or a role stored only on the device.

## 13. Verification checklist

Record actual results before merging this branch:

- [ ] Debug build succeeds.
- [ ] Existing pantry and shopping data survives the schema upgrade.
- [ ] Guest navigation requests sign-in for personal-data screens.
- [ ] Accounts A and B see separate personal data.
- [ ] Offline writes survive an application restart.
- [ ] Pending changes upload after connectivity returns.
- [ ] An old upload acknowledgement cannot clear a newer local revision.
- [ ] Deleted test records do not reappear after synchronization.
- [ ] A scanned product and its ingredient restore on a second device.
- [ ] Own profile is cached and pending profile changes upload.
- [ ] Deployed Firestore rules deny access to another user's private paths.