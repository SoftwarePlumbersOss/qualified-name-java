package com.softwareplumbers.common;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
	
	public static QualifiedName parse(String name, String separator) {
		return ROOT.addParsed(name, separator);
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
		public <T> T applyReverse(T applyTo, BiFunction<T,String,T> accumulator) { return applyTo; }
		public boolean contains(Predicate<String> predicate) { return false; }
		public boolean matches(QualifiedName name, BiPredicate<String,String> predicate, boolean match_all) { return name == ROOT || !match_all; }
		public String getFromEnd(int index) { return null; }
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
	
	/** Find if any part satisfies a predicate
	 * 
	 * @param predicate
	 * @return true if predicate returns true for any part
	 */
	public boolean contains(Predicate<String> predicate) {
		if (predicate.test(part)) return true;
		return parent.contains(predicate);
	}
	
	/** Match this name against another using a predicate
	 * 
	 * @param name Name to match
	 * @param matcher Predicate to determine whether parts match
	 * @param match_all if true, all parts in given name must match all parts in this name
	 * @return true if matcher is satisfied for each corresponding part of this and the given name
	 */
	public boolean matches(QualifiedName name, BiPredicate<String,String> matcher, boolean match_all) {
		if (name == ROOT) return false;
		return matcher.test(part, name.part) && parent.matches(name.parent, matcher, match_all);
		
	}

	/** Apply accumulator function in reverse order
	 * 
	 * @param <T> value type of accumulator
	 * @param applyTo Initial accumulator value
	 * @param accumulator Accumulation function
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T applyReverse(T applyTo, BiFunction<T,String,T> accumulator) {
		return parent.applyReverse(accumulator.apply(applyTo, part), accumulator);
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
	public QualifiedName addParsed(String toParse, String separator) {
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
	
	/** Reverse the order of the elements
	 * 
	 * @return A qualified name with parts in reverse order to this one
	 */
	public QualifiedName reverse() {
		return applyReverse(ROOT, (a,e)->a.add(e));
	}
	
	/** Check to see if a qualified name ends with a given name
	 * 
	 * @param name
	 * @return true if the last elements of this qualified name match the given name
	 */
	public boolean endsWith(QualifiedName name) {
		return name.matches(this, (a,b) -> a.equals(b), false);
	}
	
	/** Check to see if a qualified name starts with a given name
	 * 
	 * @param name
	 * @return true if the first elements of this qualified name match the given name
	 */
	public boolean startsWith(QualifiedName name) {
		return reverse().endsWith(name.reverse());
	}
	
	/** Match against a sequence of regular expressions
	 * 
	 * @param pattern A qualified name formed of regular expressions
	 * @param match_all
	 * @return true if regex parts from pattern match parts of this name 
	 */
	public boolean matches(QualifiedName pattern, boolean match_all) {
		return pattern.matches(this, (regex, part) -> Pattern.matches(regex, part), match_all);
	}
	
	/** Get the part that is nth from then end
	 * 
	 * @param index
	 * @return A part
	 */
	public String getFromEnd(int index) {
		return (index == 0) ? part : parent.getFromEnd(index-1);
	}
	
	/** Get the part that is nth from the start
	 * 
	 * @param index
	 * @return A part
	 */
	public String get(int index) {
		return reverse().getFromEnd(index);
	}

}