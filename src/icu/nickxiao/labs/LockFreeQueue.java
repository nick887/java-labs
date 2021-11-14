package icu.nickxiao.labs;

/**
 * @author nick
 * @version 1.0, 2021/11/14
 * @since 1.0.0
 */

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;


/**
 * 用数组实现无锁有界队列
 */

public class LockFreeQueue {

    private AtomicReferenceArray atomicReferenceArray;
    //代表为空，没有元素
    private static final Integer EMPTY = null;
    //头指针,尾指针
    AtomicInteger head, tail;


    public LockFreeQueue(int size) {
        atomicReferenceArray = new AtomicReferenceArray(new Integer[size + 1]);
        head = new AtomicInteger(0);
        tail = new AtomicInteger(0);
    }

    /**
     * 入队
     *
     * @param element
     * @return
     */
    public boolean add(Integer element) {
        int index = 0;
        do {
            index = (tail.get() + 1) % atomicReferenceArray.length();
            if (index == head.get() % atomicReferenceArray.length()) {
                return false;
            }
        }while (!atomicReferenceArray.compareAndSet(index, EMPTY, element));

        tail.incrementAndGet(); //移动尾指针
        return true;
    }

    /**
     * 出队
     *
     * @return
     */
    public Integer poll() {
        if (head.get() == tail.get()) {
            // System.out.println("当前队列为空");
            return null;
        }
        int index = 0;
        Integer ele = null;
        do {
            index = (head.get() + 1) % atomicReferenceArray.length();
            ele = (Integer) atomicReferenceArray.get(index);
        }while (!atomicReferenceArray.compareAndSet(index, ele, EMPTY));
        head.incrementAndGet();
        // System.out.println("出队成功!" + ele);
        return ele;
    }

    public void print() {
        StringBuffer buffer = new StringBuffer("[");
        for (int i = 0; i < atomicReferenceArray.length(); i++) {
            if (i == head.get() || atomicReferenceArray.get(i) == null) {
                continue;
            }
            buffer.append(atomicReferenceArray.get(i) + ",");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append("]");
        // System.out.println("队列内容:"    +buffer.toString());

    }


    public static void main(String[] args) throws InterruptedException {
        LockFreeQueue queue = new LockFreeQueue(10000000);
        List<Thread> list = new LinkedList<>();
        long a = System.currentTimeMillis();
        Thread t1=new Thread(()->{
            for (int i = 0; i < 100; i++) {
                queue.add(i);
                queue.print();
            }
        });
        list.add(t1);
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(()->{
                for (int j = 0; j < 10; j++) {
                    while (queue.poll()==null){

                    }
                    queue.print();
                }
            });
            list.add(t);
        }

        for (int i = 0; i < list.size(); i++) {
            list.get(i).start();
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).join();
        }
        long a1 = System.currentTimeMillis();
        System.out.println(a1 - a);
    }
}