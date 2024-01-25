package com.ricardocervo.booknblock;

import com.ricardocervo.booknblock.utils.DatesUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BooknblockApplicationTests {

	@Test
	void contextLoads() {
	}

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
}
