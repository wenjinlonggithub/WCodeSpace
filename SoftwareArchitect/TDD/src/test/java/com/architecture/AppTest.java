package com.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for simple App.
 * Updated to use JUnit 5
 */
@DisplayName("App Tests")
class AppTest {

    @Test
    @DisplayName("Should pass basic test")
    void shouldPassBasicTest() {
        assertThat(true).isTrue();
    }
}
