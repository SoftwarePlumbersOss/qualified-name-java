package com.softwareplumbers.common;

import java.util.function.BiFunction;

/** Very simple qualfied name class.
 * 
 * Got tired of other implementations (e.g. Path) that have too many special-purpose 
 * methods in them.
 * 
 * Create a Qualified name with QualifiedName.of("abc"). 
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
	
	public static QualifiedName of(String part) {
		return ROOT.add(part);
	}
	
	public final QualifiedName add(String part) {
		if (part == null) throw new IllegalArgumentException("Cannot add a null part to a qualified name");
		return new QualifiedName(this, part);
	}

	public static QualifiedName ROOT = new QualifiedName(null, null);
	
	public int hashCode() {
		if (parent == ROOT) return part.hashCode();
		else return parent.hashCode() ^ part.hashCode();
	}

	@Override
	public int compareTo(QualifiedName other) {
		if (this.parent == null && other.parent == null) return 0;
		if (this.parent == null) return -1;
		if (other.parent == null) return 1;
		int parentComparison = this.parent.compareTo(other.parent);
		if (parentComparison != 0) return parentComparison;
		return this.part.compareTo(other.part);
	}
	
	/** Apply accumualtor junction depth-first order */
	public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator) {
		if (parent == ROOT) 
			return accumulator.apply(applyTo, part);
		else 
			return accumulator.apply(parent.apply(applyTo, accumulator), part);
	}

	/** Join elements of the qualified name with the given separator */
	public String join(final String separator) {
		final BiFunction<String,String,String> joiner = (left,right)-> left==null?right:left+separator+right;
		return apply(null, joiner);
	}
	
	public String toString() {
		return join(".");
	}
}