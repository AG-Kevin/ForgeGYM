package com.myidea.gym.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordUtilTest {

    @Test
    void hash_shouldUseUsernameAsSalt() {
        String h1 = PasswordUtil.hash("u1", "p");
        String h2 = PasswordUtil.hash("u2", "p");
        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void hash_shouldBeDeterministic() {
        String h1 = PasswordUtil.hash("user", "pass");
        String h2 = PasswordUtil.hash("user", "pass");
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void hash_shouldRejectNull() {
        assertThatThrownBy(() -> PasswordUtil.hash(null, "p"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PasswordUtil.hash("u", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

