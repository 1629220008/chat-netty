package com.example.springcloudchat.leecode.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Second {


    class FooBar {
        private AtomicInteger sam = new AtomicInteger(0);
        private int n;

        public FooBar(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while ((sam.get() & 1) != 0) {
                }
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                sam.decrementAndGet();
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {

                while ((sam.get() & 1) == 0) {
                }
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                sam.decrementAndGet();
            }
        }
    }
}
