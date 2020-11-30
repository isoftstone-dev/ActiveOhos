package com.activeharmony.sample;

import com.activeharmony.ActiveAndroid;
import com.activeharmony.Configuration;
import com.activeharmony.sample.model.User;
import com.activeharmony.sample.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        Configuration.Builder builder = new Configuration.Builder(this);
        // 手动的添加模型类
        builder.addModelClasses(User.class);
        ActiveAndroid.initialize(builder.create());
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
