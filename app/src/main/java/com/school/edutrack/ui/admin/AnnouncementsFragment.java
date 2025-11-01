package com.school.edutrack.ui.admin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.school.edutrack.R;
import com.school.edutrack.model.Announcement;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.network.ApiService;
import com.school.edutrack.network.RetrofitClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsFragment extends Fragment {

    private static final String TAG = "AnnouncementsFragment";
    private static final String BASE_URL = "http://10.0.2.2/edutrack-backend/";

    private RecyclerView recyclerView;
    private EditText announcementTitleInput;
    private EditText announcementDescriptionInput;
    private Spinner roleSpinner;
    private Button addAnnouncementButton;
    private Button uploadBannerButton;
    private Button uploadFileButton;
    private List<Announcement> announcements;
    private AnnouncementAdapter adapter;
    private ApiService apiService;
    private Uri bannerUri;
    private Uri fileUri;
    private File bannerFile;
    private File fileAttachment;

    private final ActivityResultLauncher<Intent> bannerPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    bannerUri = result.getData().getData();
                    uploadBannerButton.setText("Banner Selected");
                    Log.d(TAG, "bannerPickerLauncher: Banner selected, URI = " + bannerUri);
                } else {
                    Log.w(TAG, "bannerPickerLauncher: Banner selection failed or canceled");
                }
            });

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    fileUri = result.getData().getData();
                    uploadFileButton.setText("File Selected");
                    Log.d(TAG, "filePickerLauncher: File selected, URI = " + fileUri);
                } else {
                    Log.w(TAG, "filePickerLauncher: File selection failed or canceled");
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "permissionLauncher: Storage permission granted");
                } else {
                    Log.w(TAG, "permissionLauncher: Storage permission denied");
                    showToast("Storage permission is required to upload files");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating fragment_announcements layout");
        return inflater.inflate(R.layout.fragment_announcements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Initializing views");

        recyclerView = view.findViewById(R.id.announcements_recycler_view);
        announcementTitleInput = view.findViewById(R.id.announcement_title_input);
        announcementDescriptionInput = view.findViewById(R.id.announcement_description_input);
        roleSpinner = view.findViewById(R.id.role_spinner);
        addAnnouncementButton = view.findViewById(R.id.add_announcement_button);
        uploadBannerButton = view.findViewById(R.id.upload_banner_button);
        uploadFileButton = view.findViewById(R.id.upload_file_button);

        apiService = RetrofitClient.getApiService();
        Log.d(TAG, "onViewCreated: Retrofit API service initialized");

        if (roleSpinner != null) {
            Log.d(TAG, "onViewCreated: Setting up roleSpinner");
            ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.role_options,
                    android.R.layout.simple_spinner_item
            );
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roleSpinner.setAdapter(roleAdapter);
        } else {
            Log.e(TAG, "onViewCreated: roleSpinner is null");
            Toast.makeText(requireContext(), "Role spinner not found in layout", Toast.LENGTH_SHORT).show();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        announcements = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcements);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onViewCreated: RecyclerView and adapter initialized");

        Log.d(TAG, "onViewCreated: Initiating fetchAnnouncements in background thread");
        new Thread(this::fetchAnnouncements).start();

        uploadBannerButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                bannerPickerLauncher.launch(intent);
                Log.d(TAG, "uploadBannerButton: Launching banner picker");
            } else {
                requestStoragePermission();
            }
        });

        uploadFileButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                filePickerLauncher.launch(intent);
                Log.d(TAG, "uploadFileButton: Launching file picker");
            } else {
                requestStoragePermission();
            }
        });

        addAnnouncementButton.setOnClickListener(v -> {
            String title = announcementTitleInput.getText().toString().trim();
            String description = announcementDescriptionInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString().toLowerCase() : "all";

            if (title.isEmpty() || description.isEmpty()) {
                Log.w(TAG, "addAnnouncementButton: Validation failed - title or description empty");
                Toast.makeText(requireContext(), "Please enter title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "addAnnouncementButton: Adding announcement - Title: " + title + ", Role: " + role);
            new Thread(() -> addAnnouncement(title, description, role)).start();
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        int permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean granted = permission == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkStoragePermission: READ_EXTERNAL_STORAGE granted = " + granted);
        return granted;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        Log.d(TAG, "requestStoragePermission: Requesting READ_EXTERNAL_STORAGE permission");
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void fetchAnnouncements() {
        Log.d(TAG, "fetchAnnouncements: Starting fetch operation");
        Call<List<Announcement>> call = apiService.getAnnouncements();
        Log.d(TAG, "fetchAnnouncements: Making API call to getAnnouncements, URL = " + call.request().url());

        call.enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    announcements.clear();
                    announcements.addAll(response.body());
                    Log.d(TAG, "fetchAnnouncements: Successfully fetched " + announcements.size() + " announcements");
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    String errorMsg = "Failed to fetch announcements: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", Error Body: " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "fetchAnnouncements: Error reading errorBody", e);
                        }
                    }
                    Log.e(TAG, "fetchAnnouncements: " + errorMsg);
                    showToast(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                Log.e(TAG, "fetchAnnouncements: Failed with exception", t);
                showToast("Error fetching announcements: " + t.getMessage());
            }
        });
    }

    private void addAnnouncement(String title, String description, String role) {
        Log.d(TAG, "addAnnouncement: Preparing to add announcement");

        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody roleBody = RequestBody.create(MediaType.parse("text/plain"), role);
        Log.d(TAG, "addAnnouncement: Text fields prepared - Title: " + title + ", Description: " + description + ", Role: " + role);

        MultipartBody.Part bannerPart = null;
        bannerFile = null;
        if (bannerUri != null) {
            bannerFile = uriToFile(bannerUri, "banner");
            if (bannerFile != null && bannerFile.exists()) {
                bannerPart = MultipartBody.Part.createFormData("banner", bannerFile.getName(),
                        RequestBody.create(MediaType.parse("image/*"), bannerFile));
                Log.d(TAG, "addAnnouncement: Banner file prepared - Path: " + bannerFile.getAbsolutePath() + ", Size: " + bannerFile.length() + " bytes");
            } else {
                Log.w(TAG, "addAnnouncement: Failed to convert banner URI to file or file does not exist");
                showToast("Failed to prepare banner file");
                return;
            }
        } else {
            Log.d(TAG, "addAnnouncement: No banner selected");
        }

        MultipartBody.Part filePart = null;
        fileAttachment = null;
        if (fileUri != null) {
            fileAttachment = uriToFile(fileUri, "file");
            if (fileAttachment != null && fileAttachment.exists()) {
                filePart = MultipartBody.Part.createFormData("file", fileAttachment.getName(),
                        RequestBody.create(MediaType.parse("*/*"), fileAttachment));
                Log.d(TAG, "addAnnouncement: File prepared - Path: " + fileAttachment.getAbsolutePath() + ", Size: " + fileAttachment.length() + " bytes");
            } else {
                Log.w(TAG, "addAnnouncement: Failed to convert file URI to file or file does not exist");
                showToast("Failed to prepare attachment file");
                return;
            }
        } else {
            Log.d(TAG, "addAnnouncement: No file selected");
        }

        Call<ApiResponse> call = apiService.addAnnouncement(titleBody, descriptionBody, roleBody, bannerPart, filePart);
        Log.d(TAG, "addAnnouncement: Making API call to addAnnouncement, URL = " + call.request().url());

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Log raw response for debugging
                try {
                    String rawResponse = response.raw().body() != null ? response.raw().body().string() : "No response body";
                    Log.d(TAG, "addAnnouncement: Raw response: " + rawResponse);
                } catch (Exception e) {
                    Log.e(TAG, "addAnnouncement: Failed to read raw response", e);
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    Log.d(TAG, "addAnnouncement: Response: Status = " + apiResponse.getStatus() + ", Message = " + apiResponse.getMessage());
                    showToast(apiResponse.getMessage());
                    requireActivity().runOnUiThread(() -> {
                        announcementTitleInput.setText("");
                        announcementDescriptionInput.setText("");
                        uploadBannerButton.setText("Upload Banner");
                        uploadFileButton.setText("Upload File");
                        if (roleSpinner != null) {
                            roleSpinner.setSelection(0);
                        }
                        bannerUri = null;
                        fileUri = null;
                        Log.d(TAG, "addAnnouncement: Form reset");
                    });
                    new Thread(() -> fetchAnnouncements()).start();
                } else {
                    String errorMsg = "Failed to add announcement: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            errorMsg += ", Error Body: " + errorBody;
                            Log.e(TAG, "addAnnouncement: Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "addAnnouncement: Error reading errorBody", e);
                            errorMsg += ", Unable to read error body";
                        }
                    }
                    Log.e(TAG, "addAnnouncement: " + errorMsg);
                    showToast(errorMsg);
                }
                cleanupTempFiles();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "addAnnouncement: Failed with exception", t);
                showToast("Error adding announcement: " + t.getMessage());
                cleanupTempFiles();
            }
        });
    }

    private void cleanupTempFiles() {
        if (bannerFile != null && bannerFile.exists()) {
            bannerFile.delete();
            Log.d(TAG, "cleanupTempFiles: Deleted temporary banner file: " + bannerFile.getAbsolutePath());
            bannerFile = null;
        }
        if (fileAttachment != null && fileAttachment.exists()) {
            fileAttachment.delete();
            Log.d(TAG, "cleanupTempFiles: Deleted temporary file: " + fileAttachment.getAbsolutePath());
            fileAttachment = null;
        }
    }

    private void deleteAnnouncement(int id) {
        Log.d(TAG, "deleteAnnouncement: Deleting announcement with ID = " + id);
        Call<ApiResponse> call = apiService.deleteAnnouncement(id);
        Log.d(TAG, "deleteAnnouncement: Making API call to deleteAnnouncement, URL = " + call.request().url());

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    Log.d(TAG, "deleteAnnouncement: Response: Status = " + apiResponse.getStatus() + ", Message = " + apiResponse.getMessage());
                    showToast(apiResponse.getMessage());
                    new Thread(() -> fetchAnnouncements()).start();
                } else {
                    String errorMsg = "Failed to delete announcement: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", Error Body: " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "deleteAnnouncement: Error reading errorBody", e);
                        }
                    }
                    Log.e(TAG, "deleteAnnouncement: " + errorMsg);
                    showToast(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "deleteAnnouncement: Failed with exception", t);
                showToast("Error deleting announcement: " + t.getMessage());
            }
        });
    }

    private File uriToFile(Uri uri, String prefix) {
        if (uri == null || requireContext().getContentResolver() == null) {
            Log.e(TAG, "uriToFile: Invalid URI or ContentResolver");
            showToast("Invalid file URI");
            return null;
        }

        Log.d(TAG, "uriToFile: Converting URI to file - URI: " + uri + ", Prefix: " + prefix);
        File file = new File(requireContext().getCacheDir(), prefix + "_temp_" + System.currentTimeMillis());
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            if (inputStream == null) {
                Log.e(TAG, "uriToFile: Failed to open input stream for URI: " + uri);
                showToast("Failed to open file stream");
                return null;
            }
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();
            Log.d(TAG, "uriToFile: Successfully wrote " + totalBytes + " bytes to file: " + file.getAbsolutePath());

            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "uriToFile: File verified - Path: " + file.getAbsolutePath() + ", Size: " + file.length() + " bytes");
                return file;
            } else {
                Log.e(TAG, "uriToFile: File is empty or does not exist after writing - Path: " + file.getAbsolutePath());
                showToast("Failed to create file: Empty or missing");
                return null;
            }
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "uriToFile: Failed to process file for URI: " + uri, e);
            showToast("Failed to process file: " + e.getMessage());
            return null;
        }
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "showToast: Displaying toast: " + message);
        });
    }

    private class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
        private final List<Announcement> announcements;

        public AnnouncementAdapter(List<Announcement> announcements) {
            this.announcements = announcements;
            Log.d(TAG, "AnnouncementAdapter: Initialized with " + announcements.size() + " announcements");
        }

        @NonNull
        @Override
        public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
            Log.d(TAG, "AnnouncementAdapter: Creating view holder");
            return new AnnouncementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
            Announcement announcement = announcements.get(position);
            Log.d(TAG, "AnnouncementAdapter: Binding announcement at position " + position + ": ID = " + announcement.getId());

            holder.title.setText(announcement.getTitle() != null ? announcement.getTitle() : "No Title");
            holder.description.setText(announcement.getDescription() != null ? announcement.getDescription() : "No Description");
            holder.role.setText(announcement.getRole() != null ? "Role: " + announcement.getRole().toUpperCase() : "Role: N/A");
            holder.date.setText(announcement.getTimestamp() != null ? "Date: " + announcement.getTimestamp() : "Date: N/A");

            if (announcement.getBannerImage() != null && !announcement.getBannerImage().isEmpty()) {
                String bannerUrl = BASE_URL + announcement.getBannerImage();
                Log.d(TAG, "AnnouncementAdapter: Loading banner from URL: " + bannerUrl);
                if (isImageFile(bannerUrl)) {
                    holder.bannerImage.setVisibility(View.VISIBLE);
                    holder.bannerText.setVisibility(View.GONE);
                    Glide.with(holder.itemView.getContext())
                            .load(bannerUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(holder.bannerImage);
                } else {
                    holder.bannerImage.setVisibility(View.GONE);
                    holder.bannerText.setVisibility(View.VISIBLE);
                    holder.bannerText.setText("Banner: " + announcement.getBannerImage());
                    holder.bannerText.setOnClickListener(v -> openFile(bannerUrl));
                }
            } else {
                holder.bannerImage.setVisibility(View.GONE);
                holder.bannerText.setVisibility(View.GONE);
            }

            if (announcement.getFileAttachment() != null && !announcement.getFileAttachment().isEmpty()) {
                String fileUrl = BASE_URL + announcement.getFileAttachment();
                Log.d(TAG, "AnnouncementAdapter: File attachment URL: " + fileUrl);
                holder.file.setVisibility(View.VISIBLE);
                holder.file.setText("File: " + announcement.getFileAttachment());
                holder.file.setOnClickListener(v -> openFile(fileUrl));
            } else {
                holder.file.setVisibility(View.GONE);
            }

            holder.deleteButton.setOnClickListener(v -> {
                Log.d(TAG, "AnnouncementAdapter: Delete button clicked for announcement ID: " + announcement.getId());
                new Thread(() -> deleteAnnouncement(announcement.getId())).start();
            });
        }

        private boolean isImageFile(String url) {
            return url != null && (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif"));
        }

        private void openFile(String url) {
            Log.d(TAG, "openFile: Attempting to open file: " + url);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                requireContext().startActivity(intent);
                Log.d(TAG, "openFile: Launched intent to open file");
            } catch (Exception e) {
                Log.e(TAG, "openFile: Failed to open file", e);
                showToast("Unable to open file: " + e.getMessage());
            }
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "AnnouncementAdapter: getItemCount = " + announcements.size());
            return announcements.size();
        }

        class AnnouncementViewHolder extends RecyclerView.ViewHolder {
            TextView title, description, role, date, bannerText, file;
            ImageView bannerImage;
            Button deleteButton;

            public AnnouncementViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.announcement_title);
                description = itemView.findViewById(R.id.announcement_description);
                role = itemView.findViewById(R.id.announcement_role);
                date = itemView.findViewById(R.id.announcement_date);
                bannerText = itemView.findViewById(R.id.announcement_banner);
                bannerImage = itemView.findViewById(R.id.announcement_banner_image);
                file = itemView.findViewById(R.id.announcement_file);
                deleteButton = itemView.findViewById(R.id.delete_button);
                Log.d(TAG, "AnnouncementViewHolder: Initialized view holder");
            }
        }
    }
}