package com.leadplatform.kfarmers.util;

import android.support.v4.os.AsyncTaskCompat;

import com.leadplatform.kfarmers.view.common.ImageSelectorFragment;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class AsyncTaskDispatcher {

    static String CNAME = AsyncTaskDispatcher.class.getSimpleName();
    public static AtomicInteger runningTask = new AtomicInteger(0);

    static int MAX_SIZE = 72;
    static int HALF_MAX_SIZE = 36;

    static LinkedBlockingDeque<AsyncTaskVO> queue = new LinkedBlockingDeque<AsyncTaskVO>(MAX_SIZE);
    static ArrayList<Thread> consumers = new ArrayList<Thread>(3);

    static {
        consumers.add(new Thread(new Consumer("1")));
        consumers.add(new Thread(new Consumer("2")));
        consumers.add(new Thread(new Consumer("3")));

        consumers.get(0).start();
        consumers.get(1).start();
        consumers.get(2).start();
    }


    public static void put(AsyncTaskVO e) {

        if (e == null)
            return;

        try {
            queue.addFirst(e);
            if (queue.size() > HALF_MAX_SIZE) {
                AsyncTaskVO taskVo = queue.takeLast();
                taskVo = null;
                return;
            }
        } catch (InterruptedException e1) {
        }
    }

    public static class Consumer implements Runnable {

        private String name ="";
        final Object lock = new Object();

        public Consumer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    try {// 실행중인 Task가 20개가 넘으면 wait을 한다.
                        if (runningTask.intValue() > 20)
                            lock.wait(200);

                        AsyncTaskVO taskVo = queue.takeFirst();
                        if (taskVo == null || taskVo.c == null || taskVo.task == null) {
                            return;
                        }

                        /*if (!NetworkUtil.isAvailable(taskVo.c)) {
                            queue.clear();
                            return;
                        }*/

                        //taskVo.task.executeOnExecutor(EXECUTOR, "");
                        AsyncTaskCompat.executeParallel(taskVo.task, new ImageSelectorFragment.PhotoData[]{taskVo.photoData});

                    } catch (Exception e) {
                    }

                }

            }

        }

    }

}
