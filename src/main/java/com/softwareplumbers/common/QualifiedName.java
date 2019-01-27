package com.softwareplumbers.common;

import java.util.function.BiFunction;

/** Very simple qualfied name class.
 * 
 * Got tired of other implementations (e.g. Path) that have too many special-purpose 
 * methods in them.
 * 
 * Create a new Qualified name with QualifiedName.of("abc"). 
 * Add new parts to name with 'add'. QualifiedName.of("abc").add("def") etc.
 * 
 * @author SWPNET\jonessex
 *
 */
public class QualifiedName implements Comparable<QualifiedName> {
	
	/** Parent part of name - the name of the enclosing scope.
	 * 
	 */
	public final QualifiedName parent;

	/** The name of this thing
	 *  
	 */
	public final String part;
	
	private QualifiedName(QualifiedName parent, String part) {
		this.parent = parent;
		this.part = part;
	}
	
	/** Preferred way to construct a new qualified name 
	 * 
	 * Equivalent to ROOT.add(part)
	 * 
	 * @param part base of new qualified name
	 */
	public static QualifiedName of(String part) {
		return ROOT.add(part);
	}
	
	/** Add a new part to a qualified name
	 * 
	 * The new name (in dotted notation) will be <I>thisname</I>.part
	 * 
	 * @param part New part to add to a qualified name
	 * @return A new qualified name (this name does not change)
	 */
	public final QualifiedName add(String part) {
		if (part == null) throw new IllegalArgumentException("Cannot add a null part to a qualified name");
		return new QualifiedName(this, part);
	}

	/** Representation of an empty qualified name.
	 * 
	 *  ROOT.add(part) == part
	 * 
	 */
	public static final QualifiedName ROOT = new QualifiedName(null, null) {
		public String toString() { return "{}"; }
		public int hashCode() { return 77; }
		public int compareTo(QualifiedName other) { return (other == ROOT) ? 0 : -1; }
		public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator) { return applyTo; }
	};
	
	@Override
	public int hashCode() {
		return parent.hashCode() ^ part.hashCode();
	}

	@Override
	public int compareTo(QualifiedName other) {
		if (other == ROOT) return 1;
		int parentComparison = this.parent.compareTo(other.parent);
		if (parentComparison != 0) return parentComparison;
		return this.part.compareTo(other.part);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		return other instanceof QualifiedName ? 0 == compareTo((QualifiedName)other) : false;
	}
	
	/** Apply accumulator function in depth-first order */
	public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator) {
		return accumulator.apply(parent.apply(applyTo, accumulator), part);
	}

	/** Join elements of the qualified name with the given separator */
	public String join(final String separator) {
		final BiFunction<String,String,String> joiner = (left,right)-> left==null?right:left+separator+right;
		return apply(null, joiner);
	}
	
	/** Default string representation
	 * 
	 * Equivalent to join(".")
	 */
	public String toString() {
		return join(".");
	}
}