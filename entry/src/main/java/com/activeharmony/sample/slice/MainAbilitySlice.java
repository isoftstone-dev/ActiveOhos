package com.activeharmony.sample.slice;

import com.activeharmony.sample.ResourceTable;
import com.activeharmony.sample.model.User;
import com.activeharmony.sample.provider.ListProvider;
import com.activeharmony.sample.utils.DbManager;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import ohos.data.rdb.RdbPredicates;
import ohos.data.resultset.ResultSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice implements Component.ClickedListener {

    private DbManager dbManager;
    private ListProvider listProvider;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        Button btnInsert = (Button) findComponentById(ResourceTable.Id_btn_insert);
        Button btnQuery = (Button) findComponentById(ResourceTable.Id_btn_query);
        Button btnDelete = (Button) findComponentById(ResourceTable.Id_btn_delete);
        Button btnUpdate = (Button) findComponentById(ResourceTable.Id_btn_update);
        btnInsert.setClickedListener(this);
        btnQuery.setClickedListener(this);
        btnDelete.setClickedListener(this);
        btnUpdate.setClickedListener(this);

        dbManager = new DbManager();

        ListContainer listText = (ListContainer) findComponentById(ResourceTable.Id_listText);
        // 拿到ListProvider的对象
        listProvider = new ListProvider(this, users);
        // 将ListProvider的对象设置给ListContainer控件,展示数据
        listText.setItemProvider(listProvider);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private List<User> users = new LinkedList<>();

    @Override
    public void onClick(Component component) {
        switch (component.getId()) {
            case ResourceTable.Id_btn_insert: //插入数据
                //第一次使用user对应的表的时候，如果有这张表就直接使用，没有就创建表
                //user表对应的实体类继承于Model类，此类中提供了一些操作数据库的工具类
                User user = new User();
                user.setAddr("信阳");
                user.setAge(28);
                user.setUserId(1);
                user.setUserName("张飞");

//                dbManager.insertUser(user);



                query();
                break;
            case ResourceTable.Id_btn_query: //条件查询
                users = dbManager.query("张飞");
                listProvider.setUsers(users);  //给适配器重新设置数据
                break;
            case ResourceTable.Id_btn_delete: //条件删除
                dbManager.delete("张飞");
                query();
                break;
            case ResourceTable.Id_btn_update: //条件更新
                dbManager.update("张飞");
                query();
                break;
        }
    }

    //查询所有
    public void query() {
        users = dbManager.queryUser();
        listProvider.setUsers(users); //给适配器重新设置数据
    }

//    public List<User> query() {
//        String[] columns = new String[]{"id", "name", "age", "salary"};
////        RdbPredicates rdbPredicates = new RdbPredicates("test").equalTo("age", 18).orderByAsc("salary");
//        RdbPredicates rdbPredicates = new RdbPredicates("test");
//        ResultSet resultSet = store.query(rdbPredicates, columns);
//        List<Student> students = new ArrayList<>();
//        if (resultSet.goToNextRow()) {
//            long id = Long.valueOf(resultSet.getString(0));
//            String name = resultSet.getString(1);
//            int age = Integer.valueOf(resultSet.getString(2));
//            double salary = Double.valueOf(resultSet.getString(3));
//            students.add(new Student(id, name, age, salary));
//        }
//        return students;
//    }

}
