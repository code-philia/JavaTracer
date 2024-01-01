package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestClassTest {

    @Test
    void add() {
        TestClass testClass = new TestClass();
        assertEquals(2, testClass.add(1, 1));
    }
}