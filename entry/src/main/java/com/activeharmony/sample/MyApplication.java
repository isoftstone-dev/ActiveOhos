package com.activeharmony.sample;

import com.activeharmony.ActiveAndroid;
import com.activeharmony.Configuration;
import com.activeharmony.app.Application;
import com.activeharmony.sample.model.User;

public class MyApplication extends Application {

    @Override
    public void onInitialize() {
        super.onInitialize();
        Configuration.Builder builder = new Configuration.Builder(this);
        // 手动的添加模型类
        builder.addModelClasses(User.class);
        ActiveAndroid.initialize(builder.create());
    }

}
