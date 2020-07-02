package com.hnf.honeycomb.lq;

import java.util.Stack;

class CQueue {
    private Stack<Integer> left;
    private Stack<Integer> right;

    public CQueue() {
        left = new Stack<>();
        right = new Stack<>();
    }

    public void appendTail(int value) {
        while (!right.empty()){
            left.push(right.pop());
        }
        left.push(value);
        while (!left.empty()){
            right.push(left.pop());
        }
    }

    public int deleteHead() {
        if (right.empty()) return -1;
        return right.pop();
    }

    public static void main(String[] args) {
        CQueue cQueue = new CQueue();
        System.out.println(cQueue.deleteHead());
        cQueue.appendTail(5);
        cQueue.appendTail(2);
        System.out.println(cQueue.deleteHead());
        System.out.println(cQueue.deleteHead());
    }
}
