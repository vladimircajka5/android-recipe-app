RECIPEAPP - GIT PRAVILA

===== GRANE =====

main - stabilna verzija aplikacije.
develop - zajednička grana za razvoj.

feature/auth-ui, feature/recipe-create
fix/login-validation
docs/git-rules
chore/gradle-config.


===== COMMIT PORUKE =====

type(scope): kratak opis na engleskom

feat - nova funkcionalnost
fix - ispravka greške
refactor - preuređivanje koda
docs - dokumentacija
test - testovi
chore - podešavanja projekta

Scope: auth, profile, recipe, cooking, ui, git, gradle...

feat(auth): add login screen
fix(recipe): correct ingredient scaling
docs(git): add team rules
chore(gradle): update configuration

===== POČETAK NOVOG ZADATKA (u terminalu, iz korena projekta) =====
git status
git switch develop
git pull origin develop
git switch -c feature/naziv-zadatka

===== ČUVANJE I SLANJE IZMENA =====
git status
git add .
git commit -m "feat(auth): add login screen"
git push -u origin feature/naziv-zadatka

Za naredne commit-ove na istoj grani ponovi git add . i git commit -m "...", a za slanje je dovoljno git push.

===== PULL REQUEST =====
Na GitHub-u napravi Pull Request: base = develop, compare = tvoja grana.
Posle spajanja, novi zadatak ponovo započni iz ažuriranog develop.


