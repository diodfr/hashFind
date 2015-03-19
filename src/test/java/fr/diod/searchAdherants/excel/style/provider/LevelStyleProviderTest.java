package fr.diod.searchAdherants.excel.style.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.Test;
import org.mockito.Mockito;

public class LevelStyleProviderTest {

	@Test
	public final void testGet10() {
		LevelStyleProvider styleProvider = Mockito.spy(new LevelStyleProvider());
		CellStyle cellStyle0 = Mockito.mock(CellStyle.class);
		short color0 = HSSFColor.BLUE.index;
		Mockito.doReturn(cellStyle0).when(styleProvider).createStyle(color0);
		styleProvider.addLevel(0, color0);

		CellStyle cellStyle1 = Mockito.mock(CellStyle.class);
		short color1 = HSSFColor.YELLOW.index;
		Mockito.doReturn(cellStyle1).when(styleProvider).createStyle(color1);
		styleProvider.addLevel(50, color1);
		
		assertThat(styleProvider.get(10), equalTo(cellStyle0));
		assertThat(styleProvider.get(50), equalTo(cellStyle1));
		assertThat(styleProvider.get(100), equalTo(cellStyle1));
	}
	
}
