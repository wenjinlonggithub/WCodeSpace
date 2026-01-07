package com.architecture.designpattern.singleton.demo;

public class Sun {
    private static final Sun sun = new Sun(); //自有永有的单例
    private Sun(){ //构造方法私有化

    }

    public static Sun getInstance(){ //阳光普照，方法公开化
        return sun;
    }

    public static void main(String[] args) {
        Sun demo = new Sun();
        System.out.println(demo);
    }
}
