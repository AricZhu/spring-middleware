package com.aric.middleware;

public class CustomBean {
    private String name;
    private Integer age;

    public CustomBean(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public void sayHello() {
        System.out.println("hello world: " + name + ":" + age);
    }
}
