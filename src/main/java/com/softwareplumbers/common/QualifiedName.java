package com.softwareplumbers.common;

import java.util.Arrays;
import java.util.List;
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
	 * @return a new qualified name
	 */
	public static QualifiedName of(String part) {
		return ROOT.add(part);
	}
		
	/** Preferred way to construct a new qualified name 
	 * 
	 * Equivalent to ROOT.add(parts)
	 * 
	 * @param parts base of new qualified name
	 * @return a new qualified name
	 */
	public static QualifiedName of(String... parts) {
		return ROOT.add(parts);
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
	
	/** Apply accumulator function in depth-first order
	 * 
	 * @param <T> value type of accumulator
	 * @param applyTo Initial accumulator value
	 * @param accumulator Accumulation function
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator) {
		return accumulator.apply(parent.apply(applyTo, accumulator), part);
	}

	/** Join elements of the qualified name with the given separator
	 * 
	 * @param separator string to place between elements of path
	 * @return concatenate elements of path with separator between them.
	 */
	public String join(final String separator) {
		final BiFunction<String,String,String> joiner = (left,right)-> left==null?right:left+separator+right;
		return apply(null, joiner);
	}
	
	/** Add several elements in order.
	 * 
	 * Equivalent to name.add(part[0]).add(part[1])... etc
	 * 
	 * @param parts to add
	 * @return new qualified name including additional parts
	 */
	public QualifiedName add(String... parts) {
		return add(Arrays.asList(parts));
	}
	
	/** Add several elements in order 
	 * 
	 * Equivalent to name.add(part.get(0)).add(part.get(1))... etc
	 * 
	 * @param parts to add
	 * @return new qualified name including additional parts
	 */
	public QualifiedName add(List<String> parts) {
		QualifiedName result = this;
		for (String part : parts) result = result.add(part);
		return result;
	}
	
	/** Add several elements as parsed from a string.
	 *
	 * Consecutive separators are suppressed.
	 * 
	 * @param toParse string to parse
	 * @param separator separator to break up name parts
	 * @return qualified name with the leftmost element of string as root
	 */
	public QualifiedName parse(String toParse, String separator) {
		QualifiedName result = this;
		for (String element : toParse.split(separator)) {
			if (!element.isEmpty()) result = result.add(element);
		};
		return result;
	}
	
	/** Default string representation
	 * 
	 * Equivalent to join(".")
	 * 
	 * @return join(".")
	 */
	public String toString() {
		return join(".");
	}
}