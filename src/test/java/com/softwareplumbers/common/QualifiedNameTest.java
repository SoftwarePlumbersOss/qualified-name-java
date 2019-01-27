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
}
