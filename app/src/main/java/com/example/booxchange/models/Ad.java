package com.example.booxchange.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Ad implements Serializable {
    public String title, author, genre, condition, description, city, address, userName, userId, userEmail, dateTime, userImage;
    public ArrayList<String> images;
    public Date dateObject;

    public Ad() {}
}
