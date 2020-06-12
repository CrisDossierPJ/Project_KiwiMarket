package registerTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import stev.kwikemart.*;
import stev.kwikemart.AmountException.AmountTooLargeException;
import stev.kwikemart.InvalidQuantityException.InvalidQuantityForCategoryException;
import stev.kwikemart.InvalidUpcException.UpcTooLongException;
import stev.kwikemart.InvalidUpcException.UpcTooShortException;
import stev.kwikemart.PaperRollException.OutOfPaperException;
import stev.kwikemart.Register.DuplicateItemException;
import stev.kwikemart.RegisterException.EmptyGroceryListException;
import stev.kwikemart.RegisterException.TooManyItemsException;

class RegisterTest {
	Register register;
	List<Item> grocery;

	@BeforeEach
	void setUp() throws Exception {
		register = Register.getRegister();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	public void GenerateItems(int maxItem, int minPrice, int maxPrice) {
		Register register = Register.getRegister();
		grocery = new ArrayList<Item>();

		Random randomNum = new Random();

		int nbItems = 0;
		int price = 0;
		String aze = "";
		for (int j = 0; j < maxItem; j++) {

			for (int i = 0; i < 11; i++) {
				nbItems = randomNum.nextInt(9 - 0) + 0;
				price = randomNum.nextInt(maxPrice - minPrice) + minPrice;
				if (nbItems == 5 || nbItems == 2) {
					while (nbItems == 5 || nbItems == 2) {
						nbItems = randomNum.nextInt(9 - 0) + 0;
					}
				}

				aze = aze + nbItems;
			}

			grocery.add(new Item(Upc.generateCode(aze), "Item " + j, nbItems, price));
			aze = "";

		}
		System.out.println(register.print(grocery));
	}

	/**
	 * VALIDES
	 */
	/**
	 * Cas de test valide avec ue liste dont la taille est supérieure à 0 Des Items
	 * dont le CUP est égal à 12
	 * 
	 * Dont le prix est inférieur à 35
	 */
	@Test
	void testValidList() {
		try {
			register.changePaper(PaperRoll.SMALL_ROLL);
			GenerateItems(1, 1, 12);
			System.out.println(register.print(grocery));
		} catch (OutOfPaperException e) {
			register.changePaper(PaperRoll.LARGE_ROLL);
			System.out.println(register.print(grocery));
		}
	}

	/**
	 * Ajout d'un item fractionnaire à la liste
	 */
	@Test
	void testItemFrac() {
		try {
			register.changePaper(PaperRoll.SMALL_ROLL);
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("25689784569"), "Beef ", 0.5, 2));
			System.out.println(register.print(grocery));
		} catch (OutOfPaperException e) {
			register.changePaper(PaperRoll.LARGE_ROLL);
			System.out.println(register.print(grocery));
		}
	}

	/**
	 * On retire un item de la liste
	 */
	@Test
	void testItemQT() {
		try {
			register.changePaper(PaperRoll.SMALL_ROLL);
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("72689784569"), "Arbre ", 1, 2));
			grocery.add(new Item(Upc.generateCode("72689784569"), "Arbre ", -1, 2));
			grocery.add(new Item(Upc.generateCode("82689784569"), "Dortis ", 1, 2));
			System.out.println(register.print(grocery));
		} catch (OutOfPaperException e) {
			register.changePaper(PaperRoll.LARGE_ROLL);
			System.out.println(register.print(grocery));
		}
	}
	
	/**
	 * Coupon 5 items Rabais
	 */
	@Test
	void testFiveItemCoupon() {
		try {
			register.changePaper(PaperRoll.SMALL_ROLL);
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("72689784569"), "Arbre ", 1, 2));
			grocery.add(new Item(Upc.generateCode("12689784569"), "Beef ", 1, 2));
			grocery.add(new Item(Upc.generateCode("82689784569"), "Dortis ", 1, 2));
			grocery.add(new Item(Upc.generateCode("22804918500"), "Beef", 0.5, 5.75));
			grocery.add(new Item(Upc.generateCode("64748119599"), "Chewing gum", 2, 0.99));
			grocery.add(new Item(Upc.generateCode("44348225996"), "Gobstoppers", 1, 0.99));

			System.out.println(register.print(grocery));
		} catch (OutOfPaperException e) {
			register.changePaper(PaperRoll.LARGE_ROLL);
			System.out.println(register.print(grocery));
		}
	}
	/**
	 * Coupon 
	 */
	@Test
	void testitemCoupon() {
		try {
			register.changePaper(PaperRoll.SMALL_ROLL);
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("72689784569"), "Arbre ", 1, 2));
			grocery.add(new Item(Upc.generateCode("12689784569"), "Beef ", 1, 2));
			grocery.add(new Item(Upc.generateCode("82689784569"), "Dortis ", 1, 2));
			grocery.add(new Item(Upc.generateCode("22804918500"), "Beef", 0.5, 5.75));
			grocery.add(new Item(Upc.generateCode("64748119599"), "Chewing gum", 2, 0.99));
			grocery.add(new Item(Upc.generateCode("44348225996"), "Gobstoppers", 1, 0.99));
			grocery.add(new Item(Upc.generateCode("54348225996"), "Coupon",1 , 1));
			System.out.println(register.print(grocery));
			
		} catch (OutOfPaperException e) {
			register.changePaper(PaperRoll.LARGE_ROLL);
			System.out.println(register.print(grocery));
			
		}
	}

	/**
	 * INVALIDES
	 */
	/**
	 * Liste avec 0 'item
	 */
	@Test
	void testEmptyList() {
		assertThrows(EmptyGroceryListException.class, () -> {
			try {
				register.changePaper(PaperRoll.SMALL_ROLL);
				GenerateItems(0, 1, 12);
				System.out.println(register.print(grocery));
			} catch (OutOfPaperException e) {
				register.changePaper(PaperRoll.LARGE_ROLL);
				System.out.println(register.print(grocery));
			}
		});
	}

	/**
	 * Liste avec trop d'item
	 */
	@Test
	void testTooMuchItem() {
		assertThrows(TooManyItemsException.class, () -> {
			try {
				register.changePaper(PaperRoll.SMALL_ROLL);
				GenerateItems(12, 1, 12);
				System.out.println(register.print(grocery));
			} catch (OutOfPaperException e) {
				register.changePaper(PaperRoll.LARGE_ROLL);
				System.out.println(register.print(grocery));
			}
		});
	}

	/**
	 * Liste avec item qui possède un CUP trop petit
	 */
	@Test
	void testInvalidCupTooSmall() {
		assertThrows(UpcTooShortException.class, () -> {
			try {
				register.changePaper(PaperRoll.SMALL_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode(""), "Arbre ", 1, 2));
				System.out.println(register.print(grocery));
			} catch (OutOfPaperException e) {
				register.changePaper(PaperRoll.LARGE_ROLL);
				System.out.println(register.print(grocery));
			}
		});
	}

	/**
	 * Liste avec item qui possède un CUP trop grand
	 */
	@Test
	void testInvalidCupTooBig() {
		assertThrows(UpcTooLongException.class, () -> {
			try {
				register.changePaper(PaperRoll.SMALL_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("722269784569"), "Arbre ", 1, 2));
				System.out.println(register.print(grocery));
			} catch (OutOfPaperException e) {
				register.changePaper(PaperRoll.LARGE_ROLL);
				System.out.println(register.print(grocery));
			}
		});
	}

	/**
	 * Item avec un prix négatif
	 */
	@Test
	void testAmountNegative() {
		assertThrows(AmountException.class, () -> {
			register.changePaper(PaperRoll.SMALL_ROLL);
			/* Create a list of items */
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("12345678901"), "Bananas", 1, 1.5));
			grocery.add(new Item(Upc.generateCode("22804918500"), "Beef", 0.5, 5.75));
			grocery.add(new Item(Upc.generateCode("12345678901"), "Bananas", -1, 1.5));
			grocery.add(new Item(Upc.generateCode("64748119599"), "Chewing gum", 2, -3.99));
			System.out.println(register.print(grocery));
		});
	}

	/**
	 * Item trop cher
	 */
	@Test
	void testAmountToExpensive() {
		assertThrows(AmountException.class, () -> {
			register.changePaper(PaperRoll.SMALL_ROLL);
			/* Create a list of items */
			grocery = new ArrayList<Item>();
			grocery.add(new Item(Upc.generateCode("12345678901"), "Bananas", 1, 1.5));
			grocery.add(new Item(Upc.generateCode("22804918500"), "Beef", 0.5, 5.75));
			grocery.add(new Item(Upc.generateCode("12345678901"), "Bananas", -1, 1.5));
			grocery.add(new Item(Upc.generateCode("64748119599"), "Chewing gum", 2, 48.99));
			System.out.println(register.print(grocery));
		});
	}

	/**
	 * item fractionnaire avec mauvais CUP
	 */
	@Test
	void testWrongCupFracItem() {
		assertThrows(InvalidQuantityForCategoryException.class, () -> {
			try {
				register.changePaper(PaperRoll.SMALL_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("72269784569"), "Arbre ", 0.5, 2));
				System.out.println(register.print(grocery));
			} catch (OutOfPaperException e) {
				register.changePaper(PaperRoll.LARGE_ROLL);
				System.out.println(register.print(grocery));
			}
		});
	}


}
