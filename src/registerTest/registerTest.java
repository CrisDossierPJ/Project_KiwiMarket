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
import stev.kwikemart.Register.NoSuchItemException;
import stev.kwikemart.RegisterException.EmptyGroceryListException;
import stev.kwikemart.RegisterException.TooManyItemsException;

class RegisterTest {
	/**
	 * Classe d'équivalence : Entrées/Sorties :
	 * 
	 * CUP V1 : 1,3,4,6,7,8,9,0
	 *  V2 : Au départ 2 
	 *  V3 : Au départ 5 
	 *  V4 : 12 Caractères
	 * 
	 * I1: nbCaractères>=13 
	 * I2 : nbCaractères <=11
	 * 
	 * Liste :
	 *  V5 : NbItems entre 1 et 10
	 * 
	 * I3 Nombre items <=0 
	 * I4 Nombre item >= 11
	 * 
	 * Quantité :
	 *  V6 : >0 
	 *  V7 : <0 
	 *  V8 : Qt Fractionnaire 
	 *  V9 : qt Entier
	 *
	 * I5 : Qt < 0 Première entrée 
	 * I6 : Qt > 0 article déjà présent 
	 * I7 : QT fractionnaire pour item normal
	 * 
	 * Prix : 
	 * V10 : Prix entre 0 et 35
	 * 
	 * I8 : Prix <= 0 
	 * I9 Prix >=36
	 *
	 * Rabais : 
	 * V11 : 5 produits
	 *
	 */
	Register register;
	List<Item> grocery;
	Item myItem;
	Item myItem2;

	@BeforeEach
	void setUp() throws Exception {
		register = Register.getRegister();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Génère des items aléatoires
	 * 
	 * @param maxItem
	 * @param minPrice
	 * @param maxPrice
	 */
	public void GenerateItems(int maxItem, int minPrice, int maxPrice) {
		Register register = Register.getRegister();
		grocery = new ArrayList<Item>();

		Random randomNum = new Random();

		int Quantity = 0;
		double dQT = 0;
		int price = 0;
		int FirstChar = 0;
		String CUP = "";
		for (int j = 0; j < maxItem; j++) {

			for (int i = 0; i < 10; i++) {

				Quantity = randomNum.nextInt(9 - 0) + 0;
				FirstChar = randomNum.nextInt(9 - 0) + 0;
				if (FirstChar == 5) {
					FirstChar++;
				}
				if (FirstChar == 2) {
					dQT = 0 + (9 - 0) * randomNum.nextDouble();
				}
				price = randomNum.nextInt(maxPrice - minPrice) + minPrice;

				CUP = FirstChar + CUP + Quantity;
			}
			if (FirstChar == 2) {
				myItem = new Item(Upc.generateCode(CUP), "Item " + j, Quantity, price);
			} else {
				myItem = new Item(Upc.generateCode(CUP), "Item " + j, dQT, price);
			}

			if (grocery.contains(myItem)) {
				j--;
			} else {
				grocery.add(myItem);
			}

			CUP = "";

		}
		System.out.println(register.print(grocery));
	}

	/**
	 * Test des combinaisons suivantes : 
	 * (V4 V1 V5 V6 V9 V10) Item 'Normal' dans la liste 
	 * (V4 V2 V5 V6 V8 V10)  Item Fractionnaire dans la liste 
	 * (V4 (V1 OU V2) V5 V7 V9 V10 ) Retrait article ( Quantité négatif ) 
	 * (V4 V3 V5 V6 V9 V10 ) Ajout d'un coupon 
	 * (V4 V1 V5 V6 V9 V10 V11) Item 'Normal' dans la liste pour avoir le rabais 
	 * (V4 V3 V5 V6 V9 V10 ) Ajout d'un deuxieme coupon
	 */

	@Test
	public void testArticles() {
		register.changePaper(PaperRoll.SMALL_ROLL);
		grocery = new ArrayList<Item>();
		grocery.add(new Item(Upc.generateCode("12345678901"), "Bananas", 1, 12));
		grocery.add(new Item(Upc.generateCode("22345678901"), "Beef", 1.5, 13));
		grocery.add(new Item(Upc.generateCode("82345678901"), "Mozzarella", 1, 12));
		grocery.add(new Item(Upc.generateCode("82345678901"), "Mozzarella", -1, 12));
		grocery.add(new Item(Upc.generateCode("82345678901"), "Mozzarella", 1, 12));
		grocery.add(new Item(Upc.generateCode("52345678901"), "Coupon", 1, 1));
		grocery.add(new Item(Upc.generateCode("32345678901"), "Table", 1, 20));
		grocery.add(new Item(Upc.generateCode("52345668901"), "Coupon", 1, 1));
		System.out.println(register.print(grocery));
	}

	/**
	 * Test de la combinaison suivante : 
	 * liste vide 
	 * I3
	 */
	@Test
	void testEmptyList() {
		assertThrows(EmptyGroceryListException.class, () -> {
			register.changePaper(PaperRoll.LARGE_ROLL);
			grocery = new ArrayList<Item>();
			System.out.println(register.print(grocery));

		});
	}

	/**
	 * Test de la combinaison suivante : 
	 * V4 (V1 Ou V2) V6 (SI V1 -> V9 OU SI V2 ->V8)I4 
	 * Liste avec trop d'items
	 * 
	 */
	@Test
	void testTooMuchItem() {
		assertThrows(TooManyItemsException.class, () -> {
			register.changePaper(PaperRoll.LARGE_ROLL);
			GenerateItems(12, 1, 12);
			System.out.println(register.print(grocery));
		});
	}

	/**
	 * Liste avec item qui possède un CUP trop petit
	 * I2 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8)
	 */
	@Test
	void testInvalidCupTooSmall() {
		assertThrows(UpcTooShortException.class, () -> {
				register.changePaper(PaperRoll.LARGE_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("123"), "Chaise ", 1, 2));
				System.out.println(register.print(grocery));
		});
	}

	/**
	 * Liste avec item qui possède un CUP trop grand
	 * I1 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8)
	 */
	@Test
	void testInvalidCupTooBig() {
		assertThrows(UpcTooLongException.class, () -> {
			
				register.changePaper(PaperRoll.LARGE_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("722269784569"), "Cacahuete ", 1, 2));
				System.out.println(register.print(grocery));
		
		});
	}

	/**
	 * Item avec un prix négatif
	 * I8 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8)
	 */
	@Test
	void testAmountNegative() {
		assertThrows(AmountException.class, () -> {
			register.changePaper(PaperRoll.LARGE_ROLL);
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
	 *  I9 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8) V4 
	 *  
	 */
	@Test
	void testAmountToExpensive() {
		assertThrows(AmountException.class, () -> {
			register.changePaper(PaperRoll.LARGE_ROLL);
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
	 * I7 V2 V4 V6 V8
	 */
	@Test
	void testWrongCupFracItem() {
		assertThrows(InvalidQuantityForCategoryException.class, () -> {
			
				register.changePaper(PaperRoll.LARGE_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("72269784569"), "Noix ", 0.5, 2));
				System.out.println(register.print(grocery));
			
		});
	}
	/**
	 * Premier Item avec quantité négative
	 * I5 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8) V4
	 */
	@Test
	void testQtNegFirstItem() {
		assertThrows(NoSuchItemException.class, () -> {
			
				register.changePaper(PaperRoll.LARGE_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("72269784569"), "Noix ", -5, 2));
				System.out.println(register.print(grocery));
			
		});
	}
	/**
	 * Dupliquer item dans la liste
	 * I6 (V1 OU V2) V6 (SI V1 -> V9 OU SI V2 ->V8) V4
	 */
	@Test
	void testQtItem() {
		assertThrows(DuplicateItemException.class, () -> {
			
				register.changePaper(PaperRoll.LARGE_ROLL);
				grocery = new ArrayList<Item>();
				grocery.add(new Item(Upc.generateCode("72269784569"), "Noix ", 5, 2));
				grocery.add(new Item(Upc.generateCode("72269784569"), "Noix ", 5, 2));
				
				System.out.println(register.print(grocery));
			
		});
	}

}
