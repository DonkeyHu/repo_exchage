package com.mashibing.juc.c_020;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TokenTest {

    private volatile String token = "";

//    static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
//    static Lock readLock = readWriteLock.readLock();
//    static Lock writeLock = readWriteLock.writeLock();

    Lock lock = new ReentrantLock();

    public String getToken() {
//        readLock.lock();
        try {
            lock.lock();
            return token;
        }catch (Exception e){
            e.printStackTrace();
            return token;
        }finally {
            lock.unlock();
        }
    }

    public void setToken() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            public void run() {
//                writeLock.lock();
                lock.lock();
                token = UUID.randomUUID().toString();
//                writeLock.unlock();
                lock.unlock();
            }
        };
        service.scheduleAtFixedRate(runnable, 0, 2, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        TokenTest test = new TokenTest();
        test.setToken();

        for (int i = 0; i < 20; i++) {
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            new Thread(() -> {
                System.out.println("service---->" + test.getToken());
            }).start();
        }
    }
}
