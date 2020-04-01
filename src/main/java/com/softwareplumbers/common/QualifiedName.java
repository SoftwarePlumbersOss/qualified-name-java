package com.softwareplumbers.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
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
public class QualifiedName implements Comparable<QualifiedName>, Iterable<String> {
    
    public static final String DEFAULT_ESCAPE="\\";
    
    @FunctionalInterface
    public interface Transformer<E extends Exception> {
        String transform(String part) throws E;
    }
	
	/** Parent part of name - the name of the enclosing scope.
	 * 
	 */
	public final QualifiedName parent;

	/** The name of something, within an enclosing scope.
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
		return ROOT.addAll(parts);
	}
	
    /** Parse a string into a QualifiedName using the given separator.
     * 
     * @param name String to parse
     * @param separator Separator string 
     * @return A qualified name consisting of elements of the given string, split by the given separator
     */
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
        @Override
		public String toString() { return "{}"; }
        @Override
		public int hashCode() { return 77; }
        @Override
		public int compareTo(QualifiedName other) { return (other == ROOT) ? 0 : -1; }
        @Override
		public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator, Predicate<T> whiletrue) { return applyTo; }
        @Override
		public <T> T applyReverse(T applyTo, BiFunction<T,String,T> accumulator, BiPredicate<T,String> whiletrue) { return applyTo; }
        @Override
		public int indexFromEnd(Predicate<String> predicate) { return -1; }
        @Override
		public QualifiedName right(int index) { return this; }
        @Override
		public QualifiedName leftFromEnd(int index) { return this; }
        @Override
		public boolean matches(QualifiedName name, BiPredicate<String,String> predicate, boolean match_all) { return name == ROOT || !match_all; }
        @Override
		public String getFromEnd(int index) { return null; }
        @Override
		public int size() { return 0; }
        @Override
		public boolean isEmpty() { return true; }
        @Override
        public <E extends Exception> QualifiedName transform(Transformer<E> transformer) throws E { return this; } 
        @Override
        public boolean equals(Object obj) { return obj == ROOT; }
	};
	
    /** Generate a hash code for a Qualified Name.
     * 
     * @return hash code
     */
	@Override
	public int hashCode() {
		return (parent.hashCode() * 17) ^ part.hashCode();
	}
 
    /** Compare this qualified name with another.
     * 
     * If parent scopes are equal, return the result of comparing parts. Otherwise
     * the result of comparing parents. ROOT is deemed equal to itself and less than
     * any other value.
     * 
     * @param other Other qualified name to compare
     * @return -1 if this name less than other, 0 if equal, 1 if greater.
     */
	@Override
	public int compareTo(QualifiedName other) {
		if (other == ROOT) return 1;
		int parentComparison = this.parent.compareTo(other.parent);
		if (parentComparison != 0) return parentComparison;
		return this.part.compareTo(other.part);
	}
	
    /** Compare a qualified name with another object
     * 
     * @param other Other qualified name to compare
     * @return true of other is a QualifiedName which is equal according to the compareTo algorithm.
     */
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
	 * @param whiletrue Stop and return what we have when false
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator, Predicate<T> whiletrue) {

		T result = parent.apply(applyTo, accumulator, whiletrue);
		if (whiletrue.test(result))
			return accumulator.apply(result, part);
		else
			return result;
	}
	
	/** Apply accumulator function in depth-first order
	 * 
	 * @param <T> value type of accumulator
	 * @param applyTo Initial accumulator value
	 * @param accumulator Accumulation function
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T apply(T applyTo, BiFunction<T,String,T> accumulator) {
		return apply(applyTo, accumulator, (t)->true);
	}
    
    /** Transform each element of a QualifiedName
     * 
     * @param <E> Exception type thrown by transformer function
     * @param transformer function to transform each part of this name
     * @return a qualified name with each element of this qualified name transformed by the transformer 
     * @throws E Exception propagated from transformer
     */
    public <E extends Exception> QualifiedName transform(Transformer<E> transformer) throws E {
        return parent.transform(transformer).add(transformer.transform(part));        
    }
	
	/** Find if any part satisfies a predicate
	 * 
	 * @param predicate test to satisfy
	 * @return smallest index (from end) of part matching predicate
	 */
	public int indexFromEnd(Predicate<String> predicate) {
		if (predicate.test(part)) return 0;
		int result = parent.indexFromEnd(predicate);
		return (result < 0) ? result : 1 + result;
	}
	
	/** Find if any part satisfies a predicate
	 * 
	 * @param predicate test to satisfy
	 * @return smallest index (from start) of part matching predicate
	 */
	public int indexOf(Predicate<String> predicate) {
		return reverse().indexFromEnd(predicate);
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
	 * @param whiletrue Termination function - stop applying when false
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T applyReverse(T applyTo, BiFunction<T,String,T> accumulator, BiPredicate<T,String> whiletrue) {
		if (whiletrue.test(applyTo, part)) 
			return parent.applyReverse(accumulator.apply(applyTo, part), accumulator, whiletrue);
		else
			return applyTo;
	}
	
	/** Apply accumulator function in reverse order
	 * 
	 * @param <T> value type of accumulator
	 * @param applyTo Initial accumulator value
	 * @param accumulator Accumulation function
	 * @return The result of applying the function to the accumulator value and each part.
	 */
	public <T> T applyReverse(T applyTo, BiFunction<T,String,T> accumulator) {
		return applyReverse(applyTo, accumulator, (x,y)->true);
	}
    
    private static String escape(final String toEscape, final String separator, final String escape) {
        return toEscape.replace(escape,escape+escape).replace(separator, escape + separator);
    }
	
	/** Join elements of the qualified name with the given separator.
     * 
     * if the given separator exists with any part of the name, it will be escaped
     * by doubling the separator character.
	 * 
	 * @param separator string to place between elements of path
     * @param escape string to prefix separator with if found in the parts of this name
	 * @return concatenate elements of path with separator between them.
	 */
	public String join(final String separator, final String escape) {
		final BiFunction<String,String,String> joiner = (left,right)-> {
            return left.isEmpty()
                ? escape(right, separator, escape)
                : left + separator + escape(right, separator, escape);
        };
		return apply("", joiner);
	}
    
    /** Join elements of the qualified name with the given separator.
     * 
     * if the given separator exists with any part of the name, it will be escaped
     * with DEFAULT_ESCAPE.
	 * 
	 * @param separator string to place between elements of path
	 * @return concatenate elements of path with separator between them.
	 */
    public String join(final String separator) {
        return join(separator, DEFAULT_ESCAPE);
    }
	
	/** Add several elements in order.
	 * 
	 * Equivalent to name.add(part[0]).add(part[1])... etc
	 * 
	 * @param parts to add
	 * @return new qualified name including additional parts
	 */
	public QualifiedName addAll(String... parts) {
		return addAll(Arrays.asList(parts));
	}
	
	/** Add several elements in order 
	 * 
	 * Equivalent to name.add(part.get(0)).add(part.get(1))... etc
	 * 
	 * @param parts to add
	 * @return new qualified name including additional parts
	 */
	public QualifiedName addAll(Iterable<String> parts) {
		QualifiedName result = this;
		for (String p : parts) result = result.add(p);
		return result;
	}
	
    private enum ParseState { BEGIN, SEPARATOR_AT_START, JOIN, NEXT }
    
    private static final String unescape(String escaped, String escape) {
        String regexEscape = escape.replace("\\", "\\\\");
        return escaped
            .replaceAll("(?<!"+ regexEscape +")" + regexEscape, "")
            .replaceAll(regexEscape + regexEscape, escape);
    }
    
	/** Add several elements as parsed from a string.
	 * 
	 * @param toParse string to parse
	 * @param separator separator to break up name parts
     * @param escape escape character, which is used as prefix for separator
	 * @return qualified name with the leftmost element of string as root
	 */
	public QualifiedName addParsed(String toParse, String separator, String escape) {
        String regexEscape = escape.replace("\\", "\\\\");
        String[] elements = toParse.split("(?<!"+ regexEscape +")" + separator);
        QualifiedName result = this;
        for (String element : elements) {
            if (!element.isEmpty()) result = result.add(unescape(element, escape));
        }
        return result;
	}
    
    /** Add several elements as parsed from a string
     * 
     * Equivalent to addParsed(toParse, separate, DEFAULT_ESCAPE)
     * 
     * @param toParse string to parse
     * @param separator separator used to identify elements within the string
     * @return The qualified name parsed from then given string
     */
    public QualifiedName addParsed(String toParse, String separator) {
        return addParsed(toParse, separator, DEFAULT_ESCAPE);
    }
	
	/** Default string representation
	 * 
	 * Equivalent to join(".")
	 * 
	 * @return join(".")
	 */
    @Override
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
	 * @param name name to check
	 * @return true if the last elements of this qualified name match the given name
	 */
	public boolean endsWith(QualifiedName name) {
		return name.matches(this, (a,b) -> a.equals(b), false);
	}
	
	/** Check to see if a qualified name starts with a given name
	 * 
	 * @param name name to check
	 * @return true if the first elements of this qualified name match the given name
	 */
	public boolean startsWith(QualifiedName name) {
		return reverse().endsWith(name.reverse());
	}
	
	/** Return elements in a qualified name up to the one matching the predicate 
	 * 
	 * @param matching test to match
	 * @return A qualified name including elements up to the matching part
	 */
	public QualifiedName upTo(Predicate<String> matching) {
		return apply(ROOT, (result,elem)->result.add(elem), (result)->result==ROOT || !matching.test(result.part)); 
	}
	
	/** Return elements in a qualified name up to the given index, counting from start
     * 
     * @param index index of first dropped part
     * @return the leftmost parts of the qualified name, up to index
     */
	public QualifiedName left(int index) {	
		return leftFromEnd(size()-index);
	}

	/** Return elements in a qualified name from the given index, counting from start
     * 
     * @param index index of first retained part
     * @return the rightmost parts of the qualified name, starting from index
     */
	public QualifiedName rightFromStart(int index) {
		return right(size()-index);
	}	

	/** Return elements in a qualified name from the last one matching the predicate 
	 * 
	 * @param matching Predicate to match an element in the name
	 * @return A qualified name including elements from the last one matching the predicate
	 */
	public QualifiedName fromEnd(Predicate<String> matching) {
		return applyReverse(ROOT, (result,elem)->result.add(elem), (result,elem) -> !matching.test(elem)).reverse();
	}
	
    /** Return the n rightmost elements of the name.
     * 
     * @param n count of elements retained
     * @return the n rightmost elements of the name.
     */
	public QualifiedName right(int n) {
		if (n <= 0) return ROOT;
		if (n == 1) return QualifiedName.of(part);
		return parent.right(n-1).add(part);
	}
	
    /** Return what is left of the name after the rightmost n elements have been removed.
     * 
     * @param n count of elements removed
     * @return the name with the n rightmost elements removed.
     */
	public QualifiedName leftFromEnd(int n) {
		if (n <= 0) return this;
		return parent.leftFromEnd(n-1);
	}

	
	/** Match against a sequence of regular expressions
	 * 
	 * @param pattern A qualified name formed of regular expressions
	 * @param match_all matching flag
	 * @return true if regex parts from pattern match parts of this name 
	 */
	public boolean matches(QualifiedName pattern, boolean match_all) {
		return pattern.matches(this, (regex, myPart) -> Pattern.matches(regex, myPart), match_all);
	}
	
	/** Get the part that is nth from then end
	 * 
	 * @param index index of part to fetch
	 * @return A part
	 */
	public String getFromEnd(int index) {
		return (index == 0) ? part : parent.getFromEnd(index-1);
	}
	
	/** Get the part that is nth from the start
	 * 
	 * @param index index of part to fetch
	 * @return A part
	 */
	public String get(int index) {
		return reverse().getFromEnd(index);
	}
	
	/** Get number of parts in name
	 * 
	 * @return number of parts in this name
	 */
	public int size() {
		return parent.size()+1;
	}
	
	/** Check if name is empty
	 * 
	 * @return true if name has no parts
	 */
	public boolean isEmpty() {
		return false;
	}

	/** Iterator over parts
	 * 
	 * @author SWPNET\jonessex
	 *
	 */
	private static class MyIterator implements Iterator<String> {
		
		QualifiedName current;
		
		public MyIterator(QualifiedName current) { this.current = current; }

		@Override
		public boolean hasNext() {
			return current != ROOT;
		}

		@Override
		public String next() {
			String next = current.part;
			current = current.parent;
			return next;
		}
	}
	
	/** Iterate over parts from first to last
	 * 
	 */
	@Override
	public Iterator<String> iterator() {
		return reverse().reverseIterator();
	}
	
	/** Iterate over parts from last to first
     * @return an iterator over parts of this qualified name 
	 */
	public Iterator<String> reverseIterator() {
		return new MyIterator(this);
	}
	
	/** Apply a qualified name to a map-of-maps (such as JsonObject)
     * @param <T> value type of map
     * @param map map of strings to T
     * @return the result of looking up successive elements of this name in map and returned maps. 
     */
	public <T> T apply(Map<String,T> map) {
		if (!parent.isEmpty()) {
			try { 
				map = (Map<String,T>)parent.apply(map);
			} catch (ClassCastException e) {
				map = null;
			}
		}
		return map == null ? null : map.get(part);
	}

}