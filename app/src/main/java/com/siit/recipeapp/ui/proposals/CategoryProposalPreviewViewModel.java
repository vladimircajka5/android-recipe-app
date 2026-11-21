package com.siit.recipeapp.ui.proposals;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public final class CategoryProposalPreviewViewModel extends ViewModel {

    public static final int PENDING = 0;
    public static final int ACCEPTED = 1;
    public static final int REJECTED = 2;
    public static final int ALTERNATIVE = 3;

    public static final class Proposal {
        public final String id;
        public final String element;
        public final String name;
        public final String explanation;
        public final boolean own;
        public final long createdAt;

        public int status = PENDING;
        public String response = "";
        public String alternative = "";

        Proposal(
                String id,
                String element,
                String name,
                String explanation,
                boolean own) {

            this.id = id;
            this.element = element;
            this.name = name;
            this.explanation = explanation;
            this.own = own;
            this.createdAt = System.currentTimeMillis();
        }
    }

    private final List<Proposal> entries = new ArrayList<>();
    private final MutableLiveData<List<Proposal>> observable =
            new MutableLiveData<>();

    public CategoryProposalPreviewViewModel() {
        entries.add(sample(
                "sample-1", "Oat drink", "Plant-based drinks",
                true, PENDING, "", ""));

        entries.add(sample(
                "sample-2", "Tofu", "Soy products",
                true, ACCEPTED, "Category accepted.", ""));

        entries.add(sample(
                "sample-3", "Buckwheat", "Pseudocereals",
                false, PENDING, "", ""));

        entries.add(sample(
                "sample-4", "Salt", "Other",
                false, REJECTED, "Use a more specific category.", ""));

        entries.add(sample(
                "sample-5", "Lentils", "Plant protein",
                false, ALTERNATIVE,
                "Use an existing category.", "Legumes"));

        publish();
    }

    public static CategoryProposalPreviewViewModel obtain(
            FragmentActivity activity, String ownerId) {

        return new ViewModelProvider(activity).get(
                "category-proposals-preview:" + ownerId,
                CategoryProposalPreviewViewModel.class
        );
    }

    public LiveData<List<Proposal>> observe() {
        return observable;
    }

    public void addPreview(
            String id, String element, String name, String explanation) {

        for (Proposal entry : entries) {
            if (entry.id.equals(id)) {
                return;
            }
        }

        entries.add(0, new Proposal(id, element, name, explanation, true));
        publish();
    }

    public boolean applyPreviewDecision(
            String id, int status, String response, String alternative) {

        if (status < ACCEPTED || status > ALTERNATIVE) {
            return false;
        }

        if (status == ALTERNATIVE && alternative.trim().isEmpty()) {
            return false;
        }

        for (Proposal entry : entries) {
            if (entry.id.equals(id) && entry.status == PENDING) {
                entry.status = status;
                entry.response = response;
                entry.alternative = status == ALTERNATIVE ? alternative : "";
                publish();
                return true;
            }
        }

        return false;
    }

    private Proposal sample(
            String id,
            String element,
            String name,
            boolean own,
            int status,
            String response,
            String alternative) {

        Proposal entry = new Proposal(
                id, element, name,
                "Example proposal for the screen preview.", own);

        entry.status = status;
        entry.response = response;
        entry.alternative = alternative;

        return entry;
    }

    private void publish() {
        observable.setValue(new ArrayList<>(entries));
    }
}