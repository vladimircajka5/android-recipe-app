package com.siit.recipeapp.data.model.shopping;

import java.time.LocalDateTime;

/** Shared list model. Archive policy and reminders are deferred. */
public final class ShoppingList {
    private final String id;
    private final String ownerId;
    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDateTime plannedPurchaseAt;
    private final boolean permanent;
    private final ShoppingListStatus status;

    public ShoppingList(String id, String ownerId, String name, LocalDateTime createdAt,
                        LocalDateTime plannedPurchaseAt, boolean permanent, ShoppingListStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = createdAt;
        this.plannedPurchaseAt = plannedPurchaseAt;
        this.permanent = permanent;
        this.status = status;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPlannedPurchaseAt() { return plannedPurchaseAt; }
    public boolean isPermanent() { return permanent; }
    public ShoppingListStatus getStatus() { return status; }
}
