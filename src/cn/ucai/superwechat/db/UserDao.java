package cn.ucai.superwechat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.bean.UserBean;


public class UserDao extends SQLiteOpenHelper {
    public static final String Id = "_id";
    public static final String TABLE_NAME = "user";

    public UserDao(Context context) {
        super(context, "user.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "+ TABLE_NAME +"( " +
                I.User.USER_NAME +" varchar primary key unique not null, " +
                I.User.NICK +" varchar, " +
                I.User.PASSWORD +" varchar " +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public boolean addUser(UserBean user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_NAME,user.getName());
        values.put(I.User.NICK,user.getNick());
        values.put(I.User.PASSWORD,user.getPassword());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.insert(TABLE_NAME, null, values);
        return insert>0;
    }

    public UserBean findUserByUserName(String userName){
        if(userName==null||userName.isEmpty())return null;
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from "+ TABLE_NAME + " where " + I.User.USER_NAME  + "=?";
        Cursor c = db.rawQuery(sql,new String []{userName});
        if(c.moveToNext()){
            String nick = c.getString(c.getColumnIndex(I.User.NICK));
            String password = c.getString(c.getColumnIndex(I.User.PASSWORD));
            return new UserBean(userName,password,nick);
        }
        c.close();
        return null;
    }

    public boolean updateUser(UserBean user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_NAME,user.getName());
        values.put(I.User.NICK,user.getNick());
        values.put(I.User.PASSWORD,user.getPassword());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.update(TABLE_NAME, values,I.User.USER_NAME+"=?",new String[]{user.getName()});
        return insert>0;
    }
}
