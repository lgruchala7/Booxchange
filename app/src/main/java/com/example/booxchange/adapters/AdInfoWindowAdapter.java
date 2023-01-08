package com.example.booxchange.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.booxchange.R;
import com.example.booxchange.activities.ChatActivity;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class AdInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View customView;
    private Context context;
    private List<Ad> adList;

    public AdInfoWindowAdapter(Context context, List<Ad> adList) {
        this.context = context;
        this.adList = adList;
        this.customView = LayoutInflater.from(context).inflate(R.layout.custom_ad_info_window, null);
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        int adIndex = Integer.parseInt(marker.getSnippet());
        Ad ad = adList.get(adIndex);

        TextView title = (TextView) customView.findViewById(R.id.text_title);
        title.setText("\"" + ad.title + "\"");

        TextView author = (TextView) customView.findViewById(R.id.text_author);
        author.setText(ad.author);

        TextView genre = (TextView) customView.findViewById(R.id.text_genre);
        genre.setText(ad.genre);

        RatingBar condition = (RatingBar) customView.findViewById(R.id.rating_bar_condition);
        condition.setRating(Float.parseFloat(ad.condition));

        TextView description = (TextView) customView.findViewById(R.id.text_description);
        description.setText(ad.description);

        TextView city = (TextView) customView.findViewById(R.id.text_city);
        city.setText(ad.city);

        TextView address = (TextView) customView.findViewById(R.id.text_address);
        address.setText(ad.address);

        AppCompatImageView adImage = (AppCompatImageView) customView.findViewById(R.id.image_ad);
        adImage.setImageBitmap(getBitmapFromEncodedString(ad.images.get(0)));
        adImage.setBackground(null);

        return customView;
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }


}
