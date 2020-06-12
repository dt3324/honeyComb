package com.hnf.honeycomb.serviceimpl.user;

import java.util.ArrayList;
import java.util.List;

public class MyQueue {
    private List<String> list;

    public static void main(String[] args) {
        MyQueue myQueue = new MyQueue();
        myQueue.push("1");
        myQueue.push("2");
        myQueue.push("3");
        myQueue.push("4");

        String pull = myQueue.pull();
        while (pull != null){
            System.out.println(pull);
            pull = myQueue.pull();
        }
    }

    /**存数据*/
    public void push(String s){
        if(list == null){
            list = new ArrayList<>();
        }
        list.add(s);
    }
    /**取数据*/
    public String pull(){
        if(list == null || list.size() == 0){
            return null;
        }
        String s = list.get(0);
        list.remove(0);
        return s;
    }


}
