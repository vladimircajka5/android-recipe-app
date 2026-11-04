# RecipeApp - Application Architecture

## 1. Technology

- Language: Java
- UI: XML layouts with Material 3
- Minimum Android version: API 34
- Structure: Single Android app module

## 2. Package Organization

Base package: com.siit.recipeapp

- ui.auth - Login and registration
- ui.home - Home screen
- ui.profile - User profiles
- ui.recipe - Recipe management
- ui.cooking - Cooking mode
- ui.search - Recipe search
- ui.pantry - Ingredient inventory
- ui.shopping - Shopping lists
- ui.map - Map-related screens
- ui.admin - Administration
- data.model - Shared data models
- data.repository - Data access interfaces and implementations
- data.local - Local persistence
- data.remote - Remote data access
- domain - Optional reusable business logic

Packages are created as features are implemented.

## 3. Communication

- MainActivity contains the fragment container.
- FragmentManager handles navigation between screens.
- Shared data will be accessed through repositories.
- Features should reuse shared models instead of defining duplicate entities.
- Cross-feature dependencies should be kept minimal.