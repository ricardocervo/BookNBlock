package com.ricardocervo.booknblock.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatesUtilsTest {
    @Test
    public void testOverlappingDates1() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 1);
        LocalDate endDate1 = LocalDate.of(2021, 1, 10);
        LocalDate startDate2 = LocalDate.of(2021, 1, 5);
        LocalDate endDate2 = LocalDate.of(2021, 1, 15);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 1);
        LocalDate endDate1 = LocalDate.of(2021, 1, 2);
        LocalDate startDate2 = LocalDate.of(2021, 1, 1);
        LocalDate endDate2 = LocalDate.of(2021, 1, 2);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates3() {
        LocalDate startDate1 = LocalDate.of(2024, 2, 6);
        LocalDate endDate1 = LocalDate.of(2024, 2, 8);
        LocalDate startDate2 = LocalDate.of(2024, 2, 6);
        LocalDate endDate2 = LocalDate.of(2024, 2, 8);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates4() {
        LocalDate startDate1 = LocalDate.of(2024, 1, 25);
        LocalDate endDate1 = LocalDate.of(2024, 1, 25);
        LocalDate startDate2 = LocalDate.of(2024, 1, 25);
        LocalDate endDate2 = LocalDate.of(2024, 1, 25);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testNotOverlappingDates1() {
        LocalDate startDate1 = LocalDate.of(2024, 1, 25);
        LocalDate endDate1 = LocalDate.of(2024, 1, 25);
        LocalDate startDate2 = LocalDate.of(2024, 1, 26);
        LocalDate endDate2 = LocalDate.of(2024, 1, 26);

        assertFalse(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates_EndDate1EqualsStartDate2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 1);
        LocalDate endDate1 = LocalDate.of(2021, 1, 5);
        LocalDate startDate2 = LocalDate.of(2021, 1, 5);
        LocalDate endDate2 = LocalDate.of(2021, 1, 10);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates_StartDate1EqualsEndDate2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 5);
        LocalDate endDate1 = LocalDate.of(2021, 1, 10);
        LocalDate startDate2 = LocalDate.of(2021, 1, 1);
        LocalDate endDate2 = LocalDate.of(2021, 1, 5);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testNotOverlappingDates_EndDate1BeforeStartDate2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 1);
        LocalDate endDate1 = LocalDate.of(2021, 1, 4);
        LocalDate startDate2 = LocalDate.of(2021, 1, 5);
        LocalDate endDate2 = LocalDate.of(2021, 1, 10);

        assertFalse(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testNotOverlappingDates_StartDate1AfterEndDate2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 6);
        LocalDate endDate1 = LocalDate.of(2021, 1, 10);
        LocalDate startDate2 = LocalDate.of(2021, 1, 1);
        LocalDate endDate2 = LocalDate.of(2021, 1, 5);

        assertFalse(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates_DateRange1WithinDateRange2() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 6);
        LocalDate endDate1 = LocalDate.of(2021, 1, 8);
        LocalDate startDate2 = LocalDate.of(2021, 1, 5);
        LocalDate endDate2 = LocalDate.of(2021, 1, 10);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }

    @Test
    public void testOverlappingDates_DateRange2WithinDateRange1() {
        LocalDate startDate1 = LocalDate.of(2021, 1, 5);
        LocalDate endDate1 = LocalDate.of(2021, 1, 10);
        LocalDate startDate2 = LocalDate.of(2021, 1, 6);
        LocalDate endDate2 = LocalDate.of(2021, 1, 8);

        assertTrue(DatesUtils.isOverlappingDates(startDate1, endDate1, startDate2, endDate2));
    }
}
