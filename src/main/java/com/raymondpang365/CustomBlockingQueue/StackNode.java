package com.raymondpang365.CustomBlockingQueue;

public class StackNode<T> {

    private T data;
    private StackNode<T> next;

    public StackNode(final T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public StackNode<T> getNext() {
        return next;
    }

    public void setNext(final StackNode<T> next) {
        this.next = next;
    }
}
