package com.wix.specialble.kays;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PublicKey {

    public PublicKey(int id, String publicKey){
        this.id = id;
        this.publicKey = publicKey;
    }

    @NonNull
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "public_key")
    private String publicKey = "";

    public int getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setId(int id) {
        this.id = id;
    }
}
