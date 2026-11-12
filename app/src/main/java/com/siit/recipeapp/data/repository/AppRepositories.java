package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.repository.inmemory.InMemoryCatalogRepository;
import com.siit.recipeapp.domain.service.CatalogService;

import java.util.Collections;

/** Composition root. Replace in-memory implementations with persistent repositories when backend is integrated. */
public final class AppRepositories {
    public static final InMemoryCatalogRepository CATALOG = new InMemoryCatalogRepository();
    public static final CatalogService CATALOG_SERVICE = new CatalogService(CATALOG);
    private AppRepositories() { }
}
