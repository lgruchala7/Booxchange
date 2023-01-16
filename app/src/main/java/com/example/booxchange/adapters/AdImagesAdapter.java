package com.example.booxchange.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booxchange.databinding.ItemContainerAdImageBinding;
import com.example.booxchange.listeners.AdImageListaner;

import java.util.ArrayList;

public class AdImagesAdapter extends RecyclerView.Adapter<AdImagesAdapter.ViewHolder> {

    private ArrayList<String> encodedImages;
    private AdImageListaner adImageListaner;
    private boolean isModifiable;

    public AdImagesAdapter(ArrayList<String> encodedImages, AdImageListaner adImageListaner, boolean isModifiable) {
        this.encodedImages = encodedImages;
        this.adImageListaner = adImageListaner;
        this.isModifiable = isModifiable;
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
            if (!isModifiable) {
                binding.imageDelete.setVisibility(View.GONE);
            }
            else if (isModifiable && getItemCount() == 1) {
                binding.adImage.setTag("image_add_photo");
                binding.imageDelete.setVisibility(View.GONE);
            }
            else {
                binding.imageDelete.setOnClickListener( v -> adImageListaner.onDeleteIconClicked(v));
                binding.imageDelete.setTag(getItemCount() - 1);
            }
            binding.adImage.setOnClickListener( v -> adImageListaner.onAdImageClicked(v) );
        }
    }

    private Bitmap getAdImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
