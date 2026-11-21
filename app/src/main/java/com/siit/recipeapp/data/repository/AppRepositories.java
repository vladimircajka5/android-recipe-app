package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.repository.firebase.FirebaseCatalogRepository;
import com.siit.recipeapp.domain.service.CatalogService;

public final class AppRepositories {

    private AppRepositories() {
    }

    public static CatalogRepository createCatalogRepository(){
        return new FirebaseCatalogRepository();
    }
    public static CatalogService createCatalogService(
            CatalogRepository repository) {

        return new CatalogService(repository);
    }
}