package entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.util.Date;

@Entity(name = "users")
public class User {
    @Id
    private Integer id;
    @Column(name = "username")
    private String username;
    @Column(name = "age")
    private Integer age;
    @Column(name = "registration_date")
    private Date registrationDate;

    public User(Integer id,String username, Integer age, Date registrationDate){
        setId(id);
        setUsername(username);
        setAge(age);
        setRegistrationDate(registrationDate);
    }

    public User() {
        
    }

    public int getId() {
        return id;
    }

    private void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }




}
