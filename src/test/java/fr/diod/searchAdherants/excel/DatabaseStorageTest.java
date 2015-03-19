package fr.diod.searchAdherants.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by broca on 11/03/14.
 */
public class DatabaseStorageTest {


	private static final String MAIDEN_NAME = "Grange-Cabane";
	private static final String NAME = "Broca";
	private static final String BIRTH_DATE = "05/02/1976";
	private static final String FIRST_NAME = "Aurore";
	private Adherant adherant;
	private DatabaseStorage dataStorage;

	@Before
	public void initTest() {
		dataStorage = new DatabaseStorage();

		adherant = new Adherant().setName(NAME).setMaidenName(MAIDEN_NAME).setFirstName(FIRST_NAME).setBirthDate(BIRTH_DATE);

		List<Adherant> adherantsList = new ArrayList<Adherant>();
		adherantsList.add(adherant);

		dataStorage.populate(adherantsList);
	}

	@Test
	public void getAdherantsTest() {
		List<Adherant> adherants = dataStorage.getAdherants();
		assertThat(adherants, hasSize(1));
		assertThat(adherants, hasItem(adherant));
	}

	@Test
	public void searchAdherant_SAME() {
		assertThat(dataStorage.searchAdherant(new String[]{NAME, FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_LOWERCASE() {
		assertThat(dataStorage.searchAdherant(new String[]{"broca", FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_VARIOUS_CASES() {
		assertThat(dataStorage.searchAdherant(new String[]{"BrocA", FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_END_S() {
		assertThat(dataStorage.searchAdherant(new String[]{"Brocas", FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_MAIDEN_WITHOUT_SIGN() {
		assertThat(dataStorage.searchAdherant(new String[]{"Grangecabane", FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_MAIDEN_SAME() {
		assertThat(dataStorage.searchAdherant(new String[]{MAIDEN_NAME, FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_MAIDEN_SPACE() {
		assertThat(dataStorage.searchAdherant(new String[]{"Grange Cabane", FIRST_NAME, BIRTH_DATE}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_FIRST_NAME_DIFF() {
		//		assertThat(dataStorage.searchAdherant(new String[]{NAME, "Didier", BIRTH_DATE}).isPresent(), equalTo(false));
	}

	@Test
	public void searchAdherant_MAIDEN_FIRST_NAME_DIFF() {
		//		assertThat(dataStorage.searchAdherant(new String[]{MAIDEN_NAME, "Didier", BIRTH_DATE}).isPresent(), equalTo(false));
	}

	@Test
	public void searchAdherant_BIRTH_DATE_ABSENT() {
		assertThat(dataStorage.searchAdherant(new String[]{NAME, FIRST_NAME, ""}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherant));
	}

	@Test
	public void searchAdherant_DatabaseWithNull_BirthDate() {
		DatabaseStorage dataStorageWithNull = new DatabaseStorage();

		Adherant adherantWithNull = new Adherant().setName(NAME).setMaidenName(MAIDEN_NAME).setFirstName(FIRST_NAME);

		List<Adherant> adherantsList = new ArrayList<Adherant>();
		adherantsList.add(adherantWithNull);

		dataStorageWithNull.populate(adherantsList);
		
		assertThat(dataStorageWithNull.searchAdherant(new String[]{NAME, FIRST_NAME, ""}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherantWithNull));
	}
	
	@Test
	public void searchAdherant_DatabaseWithNull_FirstName() {
		DatabaseStorage dataStorageWithNull = new DatabaseStorage();

		Adherant adherantWithNull = new Adherant().setName(NAME).setMaidenName(MAIDEN_NAME).setBirthDate(BIRTH_DATE);

		List<Adherant> adherantsList = new ArrayList<Adherant>();
		adherantsList.add(adherantWithNull);

		dataStorageWithNull.populate(adherantsList);
		
		assertThat(dataStorageWithNull.searchAdherant(new String[]{NAME, FIRST_NAME, ""}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherantWithNull));
	}
	
	@Test
	public void searchAdherant_DatabaseWithNull_MaidenName() {
		DatabaseStorage dataStorageWithNull = new DatabaseStorage();

		Adherant adherantWithNull = new Adherant().setName(NAME).setFirstName(FIRST_NAME).setBirthDate(BIRTH_DATE);

		List<Adherant> adherantsList = new ArrayList<Adherant>();
		adherantsList.add(adherantWithNull);

		dataStorageWithNull.populate(adherantsList);
		
		assertThat(dataStorageWithNull.searchAdherant(new String[]{NAME, FIRST_NAME, ""}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherantWithNull));
	}
	
	@Test
	public void searchAdherant_DatabaseWithNull_Name() {
		DatabaseStorage dataStorageWithNull = new DatabaseStorage();

		Adherant adherantWithNull = new Adherant().setMaidenName(NAME).setFirstName(FIRST_NAME).setBirthDate(BIRTH_DATE);

		List<Adherant> adherantsList = new ArrayList<Adherant>();
		adherantsList.add(adherantWithNull);

		dataStorageWithNull.populate(adherantsList);
		
		assertThat(dataStorageWithNull.searchAdherant(new String[]{NAME, FIRST_NAME, ""}).or(new AdherantScore(new Adherant(), 0)).adherant, equalTo(adherantWithNull));
	}
}
