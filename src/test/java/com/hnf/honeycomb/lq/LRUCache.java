package com.hnf.honeycomb.lq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class LRUCache {

    private final ConcurrentHashMap<Integer,Integer> map;
    private final LinkedList<String> time;
    private final int size;

    public static void main(String[] args) {
        ArrayList<Map<String, String>> maps = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("1", "2");
            if(!maps.contains(map)){
                maps.add(map);
            }
        }
        System.out.println(maps.size());
    }

    public LRUCache(int capacity) {
        map = new ConcurrentHashMap<>(capacity);
        size = capacity;
        time = new LinkedList<>();
    }
    //每次获取的时候先从list中删除 再从后面插入
    public int get(int key) {
        Integer integer = map.get(key);
        if(integer == null){
            return -1;
        }else {
            String s = String.valueOf(key);
            time.remove(s);
            time.offerLast(s);
            return map.get(key);
        }
    }

    public void put(int key, int value) {
        map.put(key, value);
        //如果不满的话就添加
        if(time.size() <= size){
            String s = String.valueOf(key);
            //判断存不存在
            if(map.get(key) == null){
                //不存在就添加到list中
                time.offerLast(s);
            }else {
                //存在就删除再添加
                time.remove(s);
                time.offerLast(s);
            }
        }else {
            String first = time.getFirst();
            time.remove(first);
            time.offerLast(first);
            if(map.get(key) == null){
                map.remove(Integer.valueOf(first));
            }
        }
    }
}
