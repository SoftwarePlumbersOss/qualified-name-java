package com.softwareplumbers.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QualifiedNameTest {
	
	@Test
	public void testDottedFormat() {
		assertEquals("a.b.c", QualifiedName.of("a").add("b").add("c").toString());
	}

	@Test
	public void testRootFormat() {
		assertEquals("{}", QualifiedName.ROOT.toString());
	}

	@Test
	public void testEquals() {
		QualifiedName a = QualifiedName.of("a");
		QualifiedName b = QualifiedName.of("b");
		QualifiedName a2 = QualifiedName.of("a");
		QualifiedName b2 = QualifiedName.of("b");
		QualifiedName ab = QualifiedName.of("a").add("b");
		QualifiedName ba = QualifiedName.of("b").add("a");
		
		assertTrue(QualifiedName.ROOT.equals(QualifiedName.ROOT));
		assertTrue(a.equals(a2));
		assertTrue(b.equals(b2));
		assertFalse(QualifiedName.ROOT.equals(a));
		assertFalse(a.equals(QualifiedName.ROOT));
		assertFalse(a.equals(b));
		assertFalse(a.equals(ab));
		assertFalse(a.equals(ba));
		assertFalse(ab.equals(ba));
		assertFalse(ab.equals(QualifiedName.ROOT));
	}
	
	@Test
	public void testComparison() {
		QualifiedName a = QualifiedName.of("a");
		QualifiedName b = QualifiedName.of("b");
		QualifiedName a2 = QualifiedName.of("a");
		QualifiedName b2 = QualifiedName.of("b");
		QualifiedName ab = QualifiedName.of("a").add("b");
		QualifiedName ba = QualifiedName.of("b").add("a");
		
		assertEquals(0,QualifiedName.ROOT.compareTo(QualifiedName.ROOT));
		assertEquals(0,a.compareTo(a2));
		assertEquals(0,b.compareTo(b2));
		assertEquals(-1,QualifiedName.ROOT.compareTo(a));
		assertEquals(1, a.compareTo(QualifiedName.ROOT));
		assertEquals(-1,a.compareTo(b));
		assertEquals(1,b.compareTo(a));
		assertEquals(-1,a.compareTo(ab));
		assertEquals(-1,a.compareTo(ba));
		assertEquals(-1,ab.compareTo(ba));
		assertEquals(1,ab.compareTo(QualifiedName.ROOT));
	}
	
	@Test
	public void testParse() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals(ABC, QualifiedName.parse("a/b/c","\\/"));
		assertEquals(ABC, QualifiedName.parse("/a/b/c","\\/"));
		assertEquals(ABC, QualifiedName.parse("/a/b/c/","\\/"));
		assertEquals(ABC, QualifiedName.parse("a//b/c","\\/"));
	}
	
	@Test
	public void testReverse() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals(QualifiedName.of("c","b","a"), ABC.reverse());				
	}
	
	@Test
	public void testStartsWith() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertTrue(ABC.startsWith(QualifiedName.of("a","b","c")));
		assertTrue(ABC.startsWith(QualifiedName.of("a","b")));
		assertTrue(ABC.startsWith(QualifiedName.of("a")));
		assertFalse(ABC.startsWith(QualifiedName.of("c")));
		assertFalse(ABC.startsWith(QualifiedName.of("a","b","c","d")));
	}

	@Test
	public void testEndsWith() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertTrue(ABC.endsWith(QualifiedName.of("a","b","c")));
		assertTrue(ABC.endsWith(QualifiedName.of("b","c")));
		assertTrue(ABC.endsWith(QualifiedName.of("c")));
		assertFalse(ABC.endsWith(QualifiedName.of("a")));
		assertFalse(ABC.endsWith(QualifiedName.of("a","b","c","d")));
	}

	@Test
	public void testContains() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertTrue(ABC.contains(e -> e.equals("a")));
		assertTrue(ABC.contains(e -> e.equals("b")));
		assertTrue(ABC.contains(e -> e.equals("c")));
		assertFalse(ABC.contains(e -> e.equals("d")));
	}
	
	@Test
	public void testGet() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals("a", ABC.get(0));
		assertEquals("b", ABC.get(1));
		assertEquals("c", ABC.get(2));
	}
}
