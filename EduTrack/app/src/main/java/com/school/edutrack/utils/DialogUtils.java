package com.school.edutrack.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.school.edutrack.R;

public class DialogUtils {

    private static Dialog loadingDialog;

    // Show Full-Screen Loading Dialog
    public static void showLoadingDialog(@NonNull Context context) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return; // Prevent multiple dialogs
        }

        loadingDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        // Ensure the dialog covers the entire screen, including status bar
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            loadingDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        loadingDialog.show();
    }

    // Hide Full-Screen Loading Dialog
    public static void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    // Success Dialog
    public static void showSuccessDialog(@NonNull Context context, String title, String message, Runnable onDismissCallback) {
        Dialog successDialog = new Dialog(context);
        successDialog.setContentView(R.layout.dialog_success_message);
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView successTitle = successDialog.findViewById(R.id.success_title);
        TextView successMessage = successDialog.findViewById(R.id.success_message);
        Button successOkButton = successDialog.findViewById(R.id.success_ok_button);

        successTitle.setText(title != null ? title : "Success");
        successMessage.setText(message != null ? message : "Operation completed successfully");

        successOkButton.setOnClickListener(v -> {
            successDialog.dismiss();
            if (onDismissCallback != null) {
                onDismissCallback.run();
            }
        });

        successDialog.show();
    }

    // Failure Dialog
    public static void showFailureDialog(@NonNull Context context, String title, String message, Runnable onDismissCallback) {
        Dialog failureDialog = new Dialog(context);
        failureDialog.setContentView(R.layout.dialog_failure_message);
        failureDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        failureDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView failureTitle = failureDialog.findViewById(R.id.failure_title);
        TextView failureMessage = failureDialog.findViewById(R.id.failure_message);
        Button failureOkButton = failureDialog.findViewById(R.id.failure_ok_button);

        failureTitle.setText(title != null ? title : "Error");
        failureMessage.setText(message != null ? message : "An error occurred. Please try again.");

        failureOkButton.setOnClickListener(v -> {
            failureDialog.dismiss();
            if (onDismissCallback != null) {
                onDismissCallback.run();
            }
        });

        failureDialog.show();
    }

    // Alert Dialog
    public static void showAlertDialog(@NonNull Context context, String title, String message, Runnable onDismissCallback) {
        Dialog alertDialog = new Dialog(context);
        alertDialog.setContentView(R.layout.dialog_alert_message);
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView alertTitle = alertDialog.findViewById(R.id.alert_title);
        TextView alertMessage = alertDialog.findViewById(R.id.alert_message);
        Button alertOkButton = alertDialog.findViewById(R.id.alert_ok_button);

        alertTitle.setText(title != null ? title : "Alert");
        alertMessage.setText(message != null ? message : "Please take note of this information.");

        alertOkButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (onDismissCallback != null) {
                onDismissCallback.run();
            }
        });

        alertDialog.show();
    }
}