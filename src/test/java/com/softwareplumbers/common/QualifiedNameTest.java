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
	public void testIndexFromEnd() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals(2,ABC.indexFromEnd(e -> e.equals("a")));
		assertEquals(1,ABC.indexFromEnd(e -> e.equals("b")));
		assertEquals(0,ABC.indexFromEnd(e -> e.equals("c")));
		assertEquals(-1,ABC.indexFromEnd(e -> e.equals("d")));
	}
	
	@Test
	public void testIndexOf() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals(0,ABC.indexOf(e -> e.equals("a")));
		assertEquals(1,ABC.indexOf(e -> e.equals("b")));
		assertEquals(2,ABC.indexOf(e -> e.equals("c")));
		assertEquals(-1,ABC.indexOf(e -> e.equals("d")));
	}

	@Test
	public void testGet() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals("a", ABC.get(0));
		assertEquals("b", ABC.get(1));
		assertEquals("c", ABC.get(2));
	}
	
	@Test
	public void testGetFromEnd() {
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals("a", ABC.getFromEnd(2));
		assertEquals("b", ABC.getFromEnd(1));
		assertEquals("c", ABC.getFromEnd(0));
	}
	
	@Test
	public void testUpTo() {
		QualifiedName TEST1 = QualifiedName.of("www","softwareplumbers","com");
		QualifiedName result = TEST1.upTo(elem -> elem.contains("ware"));
		assertEquals(QualifiedName.of("www","softwareplumbers"), result);
	}
	
	@Test
	public void testFrom() {
		QualifiedName TEST1 = QualifiedName.of("www","softwareplumbers","com");
		QualifiedName result = TEST1.fromEnd(elem -> elem.contains("ware"));
		assertEquals(QualifiedName.of("com"), result);
	}
	
	@Test
	public void testRight() {
		QualifiedName ABCDEF = QualifiedName.of("a","b","c","d","e","f");
		QualifiedName DEF = QualifiedName.of("d","e","f");
		assertEquals(DEF, ABCDEF.right(3));
		assertEquals(QualifiedName.ROOT, ABCDEF.right(0));
		assertEquals(ABCDEF, ABCDEF.right(20));
	}
	
	@Test
	public void testLeftFromEnd() {
		QualifiedName ABCDEF = QualifiedName.of("a","b","c","d","e","f");
		QualifiedName ABC = QualifiedName.of("a","b","c");
		assertEquals(ABC, ABCDEF.leftFromEnd(3));
		assertEquals(QualifiedName.ROOT, ABCDEF.leftFromEnd(20));
		assertEquals(ABCDEF, ABCDEF.leftFromEnd(0));
	}
	
	@Test
	public void testLeft() {
		QualifiedName ABCDEF = QualifiedName.of("a","b","c","d","e","f");
		QualifiedName ABC = QualifiedName.of("a","b","c");
		QualifiedName ABCD = QualifiedName.of("a","b","c","d");
		assertEquals(ABC, ABCDEF.left(3));
		assertEquals(ABCD, ABCDEF.left(4));
		assertEquals(QualifiedName.ROOT, ABCDEF.left(0));
		assertEquals(ABCDEF, ABCDEF.left(20));
	}

	@Test
	public void testRightFromStart() {
		QualifiedName ABCDEF = QualifiedName.of("a","b","c","d","e","f");
		QualifiedName DEF = QualifiedName.of("d","e","f");
		QualifiedName EF = QualifiedName.of("e","f");
		assertEquals(DEF, ABCDEF.rightFromStart(3));
		assertEquals(EF, ABCDEF.rightFromStart(4));
		assertEquals(ABCDEF, ABCDEF.rightFromStart(0));
		assertEquals(QualifiedName.ROOT, ABCDEF.rightFromStart(20));
	}
	
	@Test
	public void testPatternMatch() {
		QualifiedName shouldMatch1 = QualifiedName.of("peter","piper","picked");
		QualifiedName shouldMatch2 = QualifiedName.of("peter","poper","jumped");
		QualifiedName shouldntMatch = QualifiedName.of("david","piper","picked");
		QualifiedName pattern = QualifiedName.of("p.*","p.per",".*d");
		
		assertTrue(shouldMatch1.matches(pattern, true));
		assertTrue(shouldMatch2.matches(pattern, true));
		assertFalse(shouldntMatch.matches(pattern, true));
	}
}
