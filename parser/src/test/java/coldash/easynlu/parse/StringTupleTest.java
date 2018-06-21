package coldash.easynlu.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

class StringTupleTest {

	@Test
	void testModify() {
		StringTuple s = new StringTuple("1 2 3");
		StringTuple changed = s.modify(1, (item) -> "4");
		assertEquals(changed, new StringTuple("1 4 3"));
		assertNotEquals(changed, s);
	}

	@Test
	void testRemove() {
		StringTuple s = new StringTuple("1 2 3");
		StringTuple changed = s.remove(0);
		assertEquals(changed, new StringTuple("2 3"));
		assertNotEquals(changed, s);
	}
	
	@Test
	void testInsert() {
		StringTuple s = new StringTuple("2 3");
		StringTuple changed = s.insert(0, "X");
		assertEquals(changed, new StringTuple("X 2 3"));
		assertNotEquals(changed, s);
	}
	
	@Test
	void testHashing() {
		HashMap<StringTuple, String> map = new HashMap<>();
		map.put(new StringTuple("A B C"), "Test");
		assertTrue(map.containsKey(new StringTuple("A B C")));
	}
	
	@Test
	void testHashingSingle() {
		HashMap<StringTuple, String> map = new HashMap<>();
		map.put(StringTuple.fromParts("A"), "Test");
		assertTrue(map.containsKey(StringTuple.fromParts("A")));
	}

	@Test
	void joinList(){
		List<String> items = Arrays.asList("a", "bb", "ccc", "12");
		assertEquals("a bb ccc 12", StringTuple.joinList(" ", items));
	}
}
