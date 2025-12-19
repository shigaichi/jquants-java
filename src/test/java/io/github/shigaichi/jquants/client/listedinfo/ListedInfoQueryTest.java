package io.github.shigaichi.jquants.client.listedinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListedInfoQueryTest {
    @Test
    @DisplayName("code に与えた文字列は trim される")
    void codeIsTrimmed() {
        ListedInfoQuery query = ListedInfoQuery.builder().code(" 86970 ").build();

        assertTrue(query.getCode().isPresent());
        assertEquals("86970", query.getCode().get());
    }

    @Test
    @DisplayName("code に空文字列や空白文字列を与えた場合は Optional.empty() になる")
    void codeBlankOrEmptyBecomesEmptyOptional() {
        ListedInfoQuery queryBlank = ListedInfoQuery.builder().code("   ").build();
        ListedInfoQuery queryEmpty = ListedInfoQuery.builder().code("").build();

        assertTrue(queryBlank.getCode().isEmpty());
        assertTrue(queryEmpty.getCode().isEmpty());
    }

    @Test
    @DisplayName("code に null を与えた場合は Optional.empty() になる")
    void codeNullBecomesEmptyOptional() {
        ListedInfoQuery query = ListedInfoQuery.builder().code(null).build();

        assertTrue(query.getCode().isEmpty());
    }

    @Test
    @DisplayName("date に与えた文字列は trim される")
    void dateIsTrimmed() {
        ListedInfoQuery query = ListedInfoQuery.builder().date(" 2024-02-09 ").build();

        assertTrue(query.getDate().isPresent());
        assertEquals("2024-02-09", query.getDate().get());
    }

    @Test
    @DisplayName("date に空文字列や空白文字列/null を与えた場合は Optional.empty() になる")
    void dateBlankOrNullBecomesEmptyOptional() {
        ListedInfoQuery queryBlank = ListedInfoQuery.builder().date("   ").build();
        ListedInfoQuery queryEmpty = ListedInfoQuery.builder().date("").build();
        ListedInfoQuery queryNull = ListedInfoQuery.builder().date(null).build();

        assertTrue(queryBlank.getDate().isEmpty());
        assertTrue(queryEmpty.getDate().isEmpty());
        assertTrue(queryNull.getDate().isEmpty());
    }

    @Test
    @DisplayName("paginationKey に与えた文字列は trim される")
    void paginationKeyIsTrimmed() {
        ListedInfoQuery query = ListedInfoQuery.builder().paginationKey(" key-001 ").build();

        assertTrue(query.getPaginationKey().isPresent());
        assertEquals("key-001", query.getPaginationKey().get());
    }

    @Test
    @DisplayName("paginationKey に空文字列や空白文字列/null を与えた場合は Optional.empty() になる")
    void paginationKeyBlankOrNullBecomesEmptyOptional() {
        ListedInfoQuery queryBlank = ListedInfoQuery.builder().paginationKey("   ").build();
        ListedInfoQuery queryEmpty = ListedInfoQuery.builder().paginationKey("").build();
        ListedInfoQuery queryNull = ListedInfoQuery.builder().paginationKey(null).build();

        assertTrue(queryBlank.getPaginationKey().isEmpty());
        assertTrue(queryEmpty.getPaginationKey().isEmpty());
        assertTrue(queryNull.getPaginationKey().isEmpty());
    }
}
