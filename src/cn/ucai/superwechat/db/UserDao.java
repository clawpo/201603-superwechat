package cn.ucai.superwechat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.bean.UserAvatar;


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

    public boolean addUser(UserAvatar user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_NAME,user.getMUserName());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.PASSWORD,user.getMUserPassword());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.insert(TABLE_NAME, null, values);
        return insert>0;
    }

    public UserAvatar findUserByUserName(String userName){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from "+ TABLE_NAME + " where " + I.User.USER_NAME  + "=?";
        Cursor c = db.rawQuery(sql,new String []{userName});
        if(c.moveToNext()){
            String nick = c.getString(c.getColumnIndex(I.User.NICK));
            String password = c.getString(c.getColumnIndex(I.User.PASSWORD));
            return new UserAvatar(userName,password,nick);
        }
        c.close();
        return null;
    }

    public boolean updateUser(UserAvatar user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_NAME,user.getMUserName());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.PASSWORD,user.getMUserPassword());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.update(TABLE_NAME, values,I.User.USER_NAME+"=?",new String[]{user.getMUserName()});
        return insert>0;
    }
}
