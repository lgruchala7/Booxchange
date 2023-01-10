package com.example.booxchange.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booxchange.databinding.ItemContainerAdBinding;
import com.example.booxchange.listeners.AdListener;
import com.example.booxchange.listeners.AdListener;
import com.example.booxchange.models.Ad;

import java.util.List;

public class FavoriteItemsAdapter extends RecyclerView.Adapter<FavoriteItemsAdapter.ConversionViewHolder>{

    private final List<Ad> adList;
    private final AdListener adListener;

    public FavoriteItemsAdapter(List<Ad> adList, AdListener adListener) {
        this.adList = adList;
        this.adListener = adListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerAdBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(adList.get(position));
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAdBinding binding;

        ConversionViewHolder(ItemContainerAdBinding itemContainerAdBinding) {
            super(itemContainerAdBinding.getRoot());
            binding = itemContainerAdBinding;
        }

        void setData(Ad ad) {
            binding.imageProfile.setImageBitmap(getConversionImage(ad.images.get(0)));
            binding.textTitle.setText(ad.title);
            binding.textAuthor.setText(ad.author);
            binding.textUserName.setText("@" + ad.userName);
            binding.getRoot().setOnClickListener(v -> {
                adListener.onAdClicked(ad);
            });
        }
    }

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
