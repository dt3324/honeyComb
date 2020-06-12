package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.service.user.BbeeFetchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BbeeFetchServiceImplTest {

    @Autowired
    private BbeeFetchService service;
    @Test
    public void findAll() {
        ArrayList<Integer> type = new ArrayList<>();
        type.add(1);
        type.add(2);
        Map<String, Object> all = service.findAll(1, type, "888888", "", "51000000000", null, 50);
        System.out.println(all);
    }
}