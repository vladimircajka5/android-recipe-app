package com.siit.recipeapp.ui.pantry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.domain.service.OpenFoodFactsLookupService;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BarcodeScannerFragment extends Fragment {
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService lookupExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean handled = new AtomicBoolean(false);
    private final BarcodeScanner scanner = BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E).build());
    private PantryViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private TextView status;

    private final androidx.activity.result.ActivityResultLauncher<String> permissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera(); else showManualOnly();
            });

    public BarcodeScannerFragment() { super(R.layout.fragment_barcode_scanner); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new PantryViewModelFactory()).get(PantryViewModel.class);
        previewView = root.findViewById(R.id.barcode_preview);
        status = root.findViewById(R.id.barcode_status);
        root.findViewById(R.id.barcode_cancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.barcode_manual_continue).setOnClickListener(v -> {
            String code = ((EditText) root.findViewById(R.id.barcode_manual_input)).getText().toString().trim();
            if (code.isEmpty()) { ((EditText) root.findViewById(R.id.barcode_manual_input)).setError(getString(R.string.barcode_required)); return; }
            handleBarcode(code);
        });
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera();
        else permissionRequest.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        if (!isAdded() || previewView == null) return;
        status.setText(R.string.barcode_camera_starting);
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            if (!isAdded() || previewView == null) return;
            try {
                cameraProvider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(cameraExecutor, this::analyze);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
                status.setText(R.string.barcode_camera_ready);
            } catch (Exception error) { showManualOnly(); }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void analyze(@NonNull ImageProxy proxy) {
        android.media.Image media = proxy.getImage();
        if (media == null || handled.get()) { proxy.close(); return; }
        scanner.process(InputImage.fromMediaImage(media, proxy.getImageInfo().getRotationDegrees()))
                .addOnSuccessListener(codes -> {
                    if (!codes.isEmpty() && handled.compareAndSet(false, true)) {
                        String code = codes.get(0).getRawValue();
                        if (code != null) requireActivity().runOnUiThread(() -> handleBarcode(code)); else handled.set(false);
                    }
                }).addOnCompleteListener(task -> proxy.close());
    }

    private void handleBarcode(String barcode) {
        if (cameraProvider != null) cameraProvider.unbindAll();
        handled.set(true);
        if (status != null) status.setText(R.string.barcode_lookup_loading);
        Product existing = viewModel.findProductByBarcode(barcode);
        if (existing != null) {
            PantryItem item = viewModel.findItemByProductId(existing.getId());
            if (item != null) {
                new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.barcode_duplicate_title)
                        .setMessage(R.string.barcode_duplicate_message)
                        .setNegativeButton(R.string.pantry_cancel, (dialog, which) -> { handled.set(false); startCamera(); })
                        .setPositiveButton(R.string.barcode_add_package, (dialog, which) -> {
                            double count = item.getQuantity() == null ? 1.0 : item.getQuantity() + 1.0;
                            viewModel.saveItem(new PantryItem(item.getId(), item.getOwnerId(), item.getIngredientId(), item.getProductId(), item.getBrand(), count, "piece", item.getExpirationDate()));
                            Toast.makeText(requireContext(), R.string.barcode_package_added, Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        }).show();
            } else {
                viewModel.saveItem(new PantryItem(UUID.randomUUID().toString(), "pantry-preview", existing.getIngredientId(), existing.getId(), existing.getBrandName(), 1.0, "piece", null));
                Toast.makeText(requireContext(), R.string.barcode_product_added, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
            return;
        }
        lookupExecutor.execute(() -> {
            OpenFoodFactsLookupService.Result result = new OpenFoodFactsLookupService().lookup(barcode);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                Bundle args = new Bundle();
                args.putString(PantryProductReviewFragment.ARG_BARCODE, barcode);
                if (result != null) {
                    args.putString(PantryProductReviewFragment.ARG_NAME, result.name);
                    args.putString(PantryProductReviewFragment.ARG_BRAND, result.brand);
                    if (result.packageQuantity != null) args.putDouble(PantryProductReviewFragment.ARG_QUANTITY, result.packageQuantity);
                    args.putString(PantryProductReviewFragment.ARG_UNIT, result.unitId);
                }
                PantryProductReviewFragment review = new PantryProductReviewFragment();
                review.setArguments(args);
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, review)
                        .addToBackStack("pantry-product-review").commit();
            });
        });
    }

    private void showManualOnly() { if (status != null) status.setText(R.string.barcode_camera_unavailable); }
    @Override public void onDestroyView() { if (cameraProvider != null) cameraProvider.unbindAll(); previewView = null; status = null; super.onDestroyView(); }
    @Override public void onDestroy() { scanner.close(); cameraExecutor.shutdown(); lookupExecutor.shutdown(); super.onDestroy(); }
}
