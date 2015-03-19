package fr.diod.searchAdherants.excel;

import org.junit.Assert;
import org.junit.Test;

public class TestTransform {

	@Test
	public void testTransform() {
		String string1 = "abcdef";
		Assert.assertEquals(string1, ExcelSearch.transform(string1));
		
		String string2 = "abédef";
		String result2 = "abedef";
		Assert.assertEquals(result2, ExcelSearch.transform(string2));
		
		String string3 = "ab&def";
		String result3 = "abdef";
		Assert.assertEquals(result3, ExcelSearch.transform(string3));
	}
}
