package com.raymondpang365.CustomBlockingQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class BasicQueueWithCondition<T> {

    private final List<T> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();


    public BasicQueueWithCondition(int capacity){
        this.capacity = capacity;
    }

    public int getSize(){

        lock.lock();
        int size = queue.size();
        lock.unlock();
        return size;
    }


    public T dequeue(){
        try {
            lock.lock();
            while (queue.size() == 0) {
                if(!notEmpty.await(10, TimeUnit.MILLISECONDS)){
                    throw new TimeoutException("time out");
                }
            }
            final T data = queue.remove(0);
            notFull.signalAll();
            return data;
        }
        catch(final InterruptedException | TimeoutException e){
            e.printStackTrace();
        }
        finally{
            lock.unlock();
        }
        return null;
    }

    public void enqueue(T data) {
        try {
            lock.lock();

            while (queue.size() == capacity) {
                if (!notFull.await(10, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("time out");
                }
            }
            queue.add(data);
            notEmpty.signalAll();
        } catch (final InterruptedException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
