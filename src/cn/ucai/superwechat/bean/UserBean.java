package cn.ucai.superwechat.bean;

/**
 * Created by clawpo on 16/6/11.
 */
public class UserBean {
    private String name;
    private String password;
    private String nick;

    public UserBean(String name, String password, String nick) {
        this.name = name;
        this.password = password;
        this.nick = nick;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", nick='" + nick + '\'' +
                '}';
    }
}
