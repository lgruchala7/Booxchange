package com.example.booxchange.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booxchange.activities.NewAdActivity;
import com.example.booxchange.databinding.ItemContainerAdImageBinding;
import com.example.booxchange.databinding.ItemContainerRecentConversationBinding;
import com.example.booxchange.listeners.AddPhotoListener;
import com.example.booxchange.models.ChatMessage;
import com.example.booxchange.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdImagesAdapter extends RecyclerView.Adapter<AdImagesAdapter.ViewHolder> {

    private ArrayList<String> encodedImages;
    private AddPhotoListener addPhotoListener;

    public AdImagesAdapter(ArrayList<String> encodedImages, AddPhotoListener addPhotoListener) {
        this.encodedImages = encodedImages;
        this.addPhotoListener = addPhotoListener;
    }

    @NonNull
    @Override
    public AdImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdImagesAdapter.ViewHolder(
                ItemContainerAdImageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AdImagesAdapter.ViewHolder holder, int position) {
        holder.setData(encodedImages.get(position));
    }

    @Override
    public int getItemCount() { return encodedImages.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAdImageBinding binding;

        public ViewHolder(ItemContainerAdImageBinding itemContainerAdImageBinding) {
            super(itemContainerAdImageBinding.getRoot());
            binding = itemContainerAdImageBinding;
        }

        void setData(String encodedImage) {
            binding.adImage.setImageBitmap(getAdImage(encodedImage));
            if (getItemCount() == 1) {
                binding.getRoot().setOnClickListener(v -> {
                    addPhotoListener.onAddPhotoClicked();
                });
            }
            else {

            }
        }
    }

    private Bitmap getAdImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
