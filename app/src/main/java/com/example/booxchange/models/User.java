package com.example.booxchange.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;

    public User(){};
    public  User(String id, String name, String image){
        this.id = id;
        this.name = name;
        this.image = image;
    };

}
