package com.school.edutrack.ui.student;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.FeePayment;
import com.school.edutrack.model.FeePaymentResponse;
import com.school.edutrack.model.FeePaymentRequest;
import com.school.edutrack.model.StudentQuizModels;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentPaymentsFragment extends Fragment {

    private static final String TAG = "StudentPaymentsFragment";
    private String studentId, studentClass;
    private TextView studentIdDisplay, classDisplay, noPaymentsMessage;
    private RecyclerView paymentsRecyclerView;
    private Button retryButton;
    private FeePaymentAdapter adapter;
    private List<FeePayment> feePayments = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 03:34 PM IST, Saturday, June 28, 2025: " + studentId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_payments, container, false);

        // Initialize views
        studentIdDisplay = root.findViewById(R.id.payments_student_id);
        classDisplay = root.findViewById(R.id.class_display);
        paymentsRecyclerView = root.findViewById(R.id.payments_recycler_view);
        noPaymentsMessage = root.findViewById(R.id.no_payments_message);
        retryButton = root.findViewById(R.id.retry_button);

        // Set student ID
        if (studentId != null && !studentId.isEmpty()) {
            studentIdDisplay.setText("Student ID: " + studentId);
        } else {
            Log.e(TAG, "Student ID is null or empty at 03:34 PM IST, Saturday, June 28, 2025");
            studentIdDisplay.setText("Student ID: Not Set");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Student ID not found", () -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
            return root;
        }

        // Setup RecyclerView
        adapter = new FeePaymentAdapter(feePayments);
        paymentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        paymentsRecyclerView.setAdapter(adapter);
        paymentsRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator() {
            @Override
            public void onAnimationStarted(RecyclerView.ViewHolder viewHolder) {
                viewHolder.itemView.setAlpha(0f);
                viewHolder.itemView.setScaleY(0.8f);
                Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
                fadeIn.setDuration(300);
                viewHolder.itemView.startAnimation(fadeIn);
            }
        });

        // Setup retry button
        retryButton.setOnClickListener(v -> fetchStudentDetails());

        // Fetch student details and fee structures
        fetchStudentDetails();

        return root;
    }

    private void fetchStudentDetails() {
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "Student ID is null or empty");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Cannot fetch student details: Invalid student ID", null);
            showRetryButton(true);
            return;
        }

        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getStudentApiService().getStudentQuizDetails("get_student_details", studentId).enqueue(new Callback<StudentQuizModels.StudentQuizDetailsResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Response<StudentQuizModels.StudentQuizDetailsResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    StudentQuizModels.StudentQuizDetailsResponse detailsResponse = response.body();
                    studentClass = detailsResponse.getStudent().getClassName();
                    classDisplay.setText(String.format(Locale.US, "Class: %s", studentClass));
                    Log.d(TAG, "Fetched class: " + studentClass);
                    fetchFeePayments();
                } else {
                    Log.e(TAG, "Failed to fetch student details: " + response.code());
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch student details: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                    showRetryButton(true);
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error fetching student details: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch student details: " + t.getMessage(), null);
                showRetryButton(true);
            }
        });
    }

    private void fetchFeePayments() {
        if (studentClass == null) {
            Log.e(TAG, "Class is null, cannot fetch fee payments");
            showRetryButton(true);
            return;
        }

        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getStudentApiService().getFeePayments(studentId).enqueue(new Callback<FeePaymentResponse>() {
            @Override
            public void onResponse(Call<FeePaymentResponse> call, Response<FeePaymentResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    feePayments.clear();
                    feePayments.addAll(response.body().getData());
                    Log.d(TAG, "Fetched " + feePayments.size() + " fee payments");
                    adapter.notifyDataSetChanged();
                    updatePaymentsVisibility();
                    showRetryButton(false);
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to fetch fee payments: " + error);
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to fetch fee payments: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                    showRetryButton(true);
                }
            }

            @Override
            public void onFailure(Call<FeePaymentResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to fetch fee payments: " + t.getMessage(), null);
                showRetryButton(true);
            }
        });
    }

    private void updatePaymentsVisibility() {
        if (feePayments.isEmpty()) {
            paymentsRecyclerView.setVisibility(View.GONE);
            noPaymentsMessage.setVisibility(View.VISIBLE);
        } else {
            paymentsRecyclerView.setVisibility(View.VISIBLE);
            noPaymentsMessage.setVisibility(View.GONE);
            showRetryButton(false);
        }
    }

    private void showRetryButton(boolean show) {
        retryButton.setVisibility(show ? View.VISIBLE : View.GONE);
        noPaymentsMessage.setVisibility(show || feePayments.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showUpiPaymentDialog(FeePayment payment, String transactionId) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_upi_payment);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView upiImage = dialog.findViewById(R.id.upi_image);
        EditText pinInput = dialog.findViewById(R.id.upi_pin_input);
        Button submitPinButton = dialog.findViewById(R.id.submit_pin_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);

        // Use existing icons for UPI
        upiImage.setImageResource(R.drawable.ic_upi_qr);
        upiImage.setOnClickListener(v -> {
            upiImage.setImageResource(upiImage.getDrawable().getConstantState().equals(
                    getResources().getDrawable(R.drawable.ic_upi_qr).getConstantState())
                    ? R.drawable.ic_upi_apps : R.drawable.ic_upi_qr);
        });

        submitPinButton.setOnClickListener(v -> {
            String pin = pinInput.getText().toString().trim();
            if (pin.length() >= 4 && pin.length() <= 6 && pin.matches("\\d+")) {
                dialog.dismiss();
                submitPayment(payment, transactionId);
            } else {
                DialogUtils.showAlertDialog(requireContext(), "Invalid PIN", "Please enter a 4-6 digit PIN", null);
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void submitPayment(FeePayment payment, String transactionId) {
        DialogUtils.showLoadingDialog(requireContext());
        FeePaymentRequest request = new FeePaymentRequest(studentId, payment.getId(), "Paid", transactionId);
        RetrofitClient.getStudentApiService().addFeePayment(request).enqueue(new Callback<FeePaymentResponse.ApiResponse>() {
            @Override
            public void onResponse(Call<FeePaymentResponse.ApiResponse> call, Response<FeePaymentResponse.ApiResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("success")) {
                    DialogUtils.showSuccessDialog(requireContext(), "Payment Successful", "Payment recorded successfully", () -> fetchFeePayments());
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Failed to record payment: " + error, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<FeePaymentResponse.ApiResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                Log.e(TAG, "Network error: " + t.getMessage());
                DialogUtils.showFailureDialog(requireContext(), "Network Error", "Failed to record payment: " + t.getMessage(), null);
            }
        });
    }

    private class FeePaymentAdapter extends RecyclerView.Adapter<FeePaymentAdapter.ViewHolder> {
        private List<FeePayment> payments;
        private List<Integer> expandedPositions = new ArrayList<>();
        private List<String> paymentMethods = Arrays.asList("UPI", "Net Banking", "Offline");

        public FeePaymentAdapter(List<FeePayment> payments) {
            this.payments = payments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee_payment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeePayment payment = payments.get(position);
            holder.feeTypeBanner.setText(payment.getFeeType());
            holder.classText.setText("Class: " + payment.getClassName());
            holder.amount.setText(String.format(Locale.US, "₹%s", payment.getAmount()));
            holder.deadline.setText("Due: " + payment.getDeadline());
            String status = payment.getStatus() != null ? payment.getStatus() : "Pending";
            holder.statusIcon.setImageResource(status.equals("Paid") ? R.drawable.ic_paid : R.drawable.ic_pending);
            holder.statusText.setText(status);
            if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
                holder.transactionId.setText("Trans. ID: " + payment.getTransactionId());
                holder.transactionId.setVisibility(View.VISIBLE);
            } else {
                holder.transactionId.setVisibility(View.GONE);
            }

            boolean isExpanded = expandedPositions.contains(position);
            holder.paymentDetailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Setup payment method spinner
            ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, paymentMethods);
            methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.paymentMethodSpinner.setAdapter(methodAdapter);
            holder.paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String method = paymentMethods.get(pos);
                    holder.transactionIdInput.setEnabled("Offline".equals(method));
                    if (!"Offline".equals(method)) {
                        holder.transactionIdInput.setText(UUID.randomUUID().toString().substring(0, 12));
                    } else {
                        holder.transactionIdInput.setText("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    holder.transactionIdInput.setEnabled(true);
                    holder.transactionIdInput.setText("");
                }
            });

            // Pay button
            boolean isPaid = status.equals("Paid");
            holder.payButton.setEnabled(!isPaid);
            holder.paymentMethodSpinner.setEnabled(!isPaid);
            holder.transactionIdInput.setEnabled(!isPaid && "Offline".equals(holder.paymentMethodSpinner.getSelectedItem()));
            holder.payButton.setOnClickListener(v -> {
                String method = holder.paymentMethodSpinner.getSelectedItem().toString();
                String transactionId = holder.transactionIdInput.getText().toString().trim();
                if ("Offline".equals(method) && transactionId.isEmpty()) {
                    DialogUtils.showAlertDialog(requireContext(), "Validation Error", "Please enter a transaction ID for offline payment", null);
                    return;
                }
                if ("UPI".equals(method)) {
                    showUpiPaymentDialog(payment, transactionId);
                } else {
                    submitPayment(payment, transactionId);
                }
            });

            // Toggle payment details
            holder.cardView.setOnClickListener(v -> {
                if (isExpanded) {
                    expandedPositions.remove((Integer) position);
                    holder.paymentDetailsContainer.setVisibility(View.GONE);
                    Animation collapse = AnimationUtils.loadAnimation(requireContext(), R.anim.collapse);
                    holder.paymentDetailsContainer.startAnimation(collapse);
                } else {
                    expandedPositions.add(position);
                    holder.paymentDetailsContainer.setVisibility(View.VISIBLE);
                    Animation expand = AnimationUtils.loadAnimation(requireContext(), R.anim.expand);
                    holder.paymentDetailsContainer.startAnimation(expand);
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return payments.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView feeTypeBanner, classText, amount, deadline, statusText, transactionId;
            ImageView statusIcon;
            LinearLayout paymentDetailsContainer;
            Spinner paymentMethodSpinner;
            EditText transactionIdInput;
            Button payButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                feeTypeBanner = itemView.findViewById(R.id.fee_type_banner);
                classText = itemView.findViewById(R.id.class_text);
                amount = itemView.findViewById(R.id.amount_text);
                deadline = itemView.findViewById(R.id.deadline_text);
                statusIcon = itemView.findViewById(R.id.status_icon);
                statusText = itemView.findViewById(R.id.status_text);
                transactionId = itemView.findViewById(R.id.transaction_id_text);
                paymentDetailsContainer = itemView.findViewById(R.id.payment_details_container);
                paymentMethodSpinner = itemView.findViewById(R.id.payment_method_spinner);
                transactionIdInput = itemView.findViewById(R.id.transaction_id_input);
                payButton = itemView.findViewById(R.id.pay_button);
            }
        }
    }
}