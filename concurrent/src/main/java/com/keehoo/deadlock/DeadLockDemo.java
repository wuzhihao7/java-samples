package com.keehoo.deadlock;

/**
 * @author wuzhihao
 * @version V1.0
 * @since 2019/4/13
 */
public class DeadLockDemo {
    private static String resource_a = "A";
    private static String resource_b = "B";

    public static void main(String[] args) {
        deadLock();
    }

    private static void deadLock(){
        Thread threadA = new Thread(){
            @Override
            public void run() {
                synchronized (resource_a){
                    System.out.println("get resource a");
                    try {
                        Thread.sleep(3000);
                        synchronized (resource_b){
                            System.out.println("get resource b");
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread threadB = new Thread(){
            @Override
            public void run() {
                synchronized (resource_b){
                    System.out.println("get resource b");
                    synchronized (resource_a){
                        System.out.println("get resource a");
                    }
                }
            }
        };

        threadA.start();
        threadB.start();
    }
}
