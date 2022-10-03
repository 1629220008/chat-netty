package com.example.springcloudchat.leecode.thread;

import java.util.concurrent.CountDownLatch;

public class First {

    private void test1() {
        Foo foo = new Foo();
    }

    static class Foo {

        private final CountDownLatch secondDown;
        private final CountDownLatch thirdDown;

        public Foo() {
            secondDown = new CountDownLatch(1);
            thirdDown = new CountDownLatch(1);
        }

        public void first(Runnable printFirst) throws InterruptedException {
            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            secondDown.countDown();
        }

        public void second(Runnable printSecond) throws InterruptedException {
            secondDown.await();
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            thirdDown.countDown();
        }

        public void third(Runnable printThird) throws InterruptedException {
            thirdDown.await();
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        }
    }
}
