package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class TestTestComponent {
    private final TestComponent testComponent;

    @Autowired
    TestTestComponent(TestComponent testComponent){
        this.testComponent=testComponent;
    }

    public void test() throws SQLException {
        testComponent.test();
    }
}
