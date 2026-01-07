package com.architecture.designpattern.singleton.demo;

public class Sun {
    private volatile static Sun sun; //自有永有的单例

    private Sun() { //构造方法私有化

    }

    public static synchronized Sun getInstance() { //阳光普照，方法公开化
        if (sun == null) { //无日才造日
            synchronized (Sun.class) {
                if (sun == null) {
                    sun = new Sun();
                }
            }
        }
        return sun;
    }

    public static void main(String[] args) {
        Sun demo = Sun.getInstance();
        System.out.println(demo);
    }
}
