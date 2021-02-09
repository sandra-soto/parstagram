package com.example.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // register your parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("l8C9ASDXRO1PxMJEuxmn7NPuU4pRXGesZKNCY8eT")
                .clientKey("Tmwhn7DOyQguZhbtXdrhupneUhQq9B78e8yow5vG")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
