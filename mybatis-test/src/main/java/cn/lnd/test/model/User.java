package cn.lnd.test.model;


/**
 * @Author lnd
 * @Description
 * @Date 2024/9/26 23:45
 */
public class User {
    private int id;
    private String username;
    private String hashedPassword;
    private int age;

    public User(int id, String username, int age) {
        this.id = id;
        this.username = username;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
