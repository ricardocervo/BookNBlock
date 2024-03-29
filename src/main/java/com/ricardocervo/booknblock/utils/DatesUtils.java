package com.ricardocervo.booknblock.utils;

import java.time.LocalDate;

public class DatesUtils {

    public static boolean isOverlappingDates(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
        return !startDate1.isAfter(endDate2) && !endDate1.isBefore(startDate2);
    }

}
