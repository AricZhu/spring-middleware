package com.aric.middleware;

public class CustomBean {
    private String name;
    private Integer age;

    public CustomBean() {
    }

    public CustomBean(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void sayHello() {
        System.out.println("hello world: " + name + ":" + age);
    }
}
