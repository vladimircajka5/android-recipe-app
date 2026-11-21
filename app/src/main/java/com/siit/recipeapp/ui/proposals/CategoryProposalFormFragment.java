package com.siit.recipeapp.ui.proposals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.remote.UserDataSession;

import java.util.UUID;

public class CategoryProposalFormFragment extends Fragment {

    private String ownerId;
    private String draftId;
    private boolean submitted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ownerId = savedInstanceState == null
                ? UserDataSession.currentOwnerId(requireContext())
                : savedInstanceState.getString("owner_id");

        draftId = savedInstanceState == null
                ? UUID.randomUUID().toString()
                : savedInstanceState.getString("draft_id");

        submitted = savedInstanceState != null
                && savedInstanceState.getBoolean("submitted", false);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_category_proposals_form, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            ((TextView) view.findViewById(R.id.et_proposal_element))
                    .setText(savedInstanceState.getString("element", ""));

            ((TextView) view.findViewById(R.id.et_proposal_name))
                    .setText(savedInstanceState.getString("name", ""));

            ((TextView) view.findViewById(R.id.et_proposal_explanation))
                    .setText(savedInstanceState.getString("explanation", ""));
        }

        view.findViewById(R.id.btn_proposal_preview_submit)
                .setEnabled(activeUser() && !submitted);

        if (!activeUser()) {
            ((TextView) view.findViewById(R.id.tv_proposal_form_status))
                    .setText(R.string.proposal_sign_in);
        } else if (submitted) {
            ((TextView) view.findViewById(R.id.tv_proposal_form_status))
                    .setText(R.string.proposal_preview_added);
        }

        view.findViewById(R.id.btn_proposal_preview_submit)
                .setOnClickListener(v -> submitPreview(view));
    }

    private boolean activeUser() {
        return isAdded()
                && ownerId != null
                && ownerId.equals(
                UserDataSession.currentOwnerId(requireContext()));
    }

    private void submitPreview(View view) {
        if (!activeUser() || submitted) {
            return;
        }

        TextInputLayout elementLayout =
                view.findViewById(R.id.til_proposal_element);

        TextInputLayout nameLayout =
                view.findViewById(R.id.til_proposal_name);

        String element = text(view.findViewById(R.id.et_proposal_element));
        String name = text(view.findViewById(R.id.et_proposal_name));
        String explanation = text(
                view.findViewById(R.id.et_proposal_explanation));

        elementLayout.setError(null);
        nameLayout.setError(null);

        boolean valid = true;

        if (element.isEmpty()) {
            elementLayout.setError(getString(
                    R.string.proposal_element_required));
            valid = false;
        }

        if (name.isEmpty()) {
            nameLayout.setError(getString(
                    R.string.proposal_name_required));
            valid = false;
        }

        if (!valid) {
            return;
        }

        CategoryProposalPreviewViewModel.obtain(requireActivity(), ownerId)
                .addPreview(draftId, element, name, explanation);

        submitted = true;
        view.findViewById(R.id.btn_proposal_preview_submit)
                .setEnabled(false);

        Toast.makeText(
                requireContext(),
                R.string.proposal_preview_added,
                Toast.LENGTH_LONG
        ).show();

        if (!getParentFragmentManager().isStateSaved()) {
            getParentFragmentManager().popBackStack();
        }
    }

    private String text(TextView field) {
        return field.getText() == null
                ? "" : field.getText().toString().trim();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("owner_id", ownerId);
        outState.putString("draft_id", draftId);
        outState.putBoolean("submitted", submitted);

        View view = getView();

        if (view != null) {
            outState.putString("element", text(
                    view.findViewById(R.id.et_proposal_element)));

            outState.putString("name", text(
                    view.findViewById(R.id.et_proposal_name)));

            outState.putString("explanation", text(
                    view.findViewById(R.id.et_proposal_explanation)));
        }
    }
}