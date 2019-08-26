package com.softwareplumbers.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
    
    @FunctionalInterface
    public interface Transformer<E extends Exception> {
        String transform(String part) throws E;
    }
	
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
		return ROOT.addAll(parts);
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
	 * @param predicate
	 * @return smallest index (from end) of part matching predicate
	 */
	public int indexFromEnd(Predicate<String> predicate) {
		if (predicate.test(part)) return 0;
		int result = parent.indexFromEnd(predicate);
		return (result < 0) ? result : 1 + result;
	}
	
	/** Find if any part satisfies a predicate
	 * 
	 * @param predicate
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
	
	/** Join elements of the qualified name with the given separator
	 * 
	 * @param separator string to place between elements of path
	 * @return concatenate elements of path with separator between them.
	 */
	public String join(final String separator) {
		final BiFunction<String,String,String> joiner = (left,right)-> left.isEmpty()?right:left+separator+right;
		return apply("", joiner);
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
		}
		return result;
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
	
	/** Return elements in a qualified name up to the one matching the predicate 
	 * 
	 * @param matching
	 * @return
	 */
	public QualifiedName upTo(Predicate<String> matching) {
		return apply(ROOT, (result,elem)->result.add(elem), (result)->result==ROOT || !matching.test(result.part)); 
	}
	
	/** Return elements in a qualified name up to the given index, counting from start
     * 
     * @param index
     * @return the leftmost parts of the qualified name, up to index
     */
	public QualifiedName left(int index) {	
		return leftFromEnd(size()-index);
	}

	/** Return elements in a qualified name from the given index, counting from start
     * 
     * @param index
     * @return the rightmost parts of the qualified name, starting from index
     */
	public QualifiedName rightFromStart(int index) {
		return right(size()-index);
	}	

	/** Return elements in a qualified name from the last one matching the predicate 
	 * 
	 * @param matching
	 * @return
	 */
	public QualifiedName fromEnd(Predicate<String> matching) {
		return applyReverse(ROOT, (result,elem)->result.add(elem), (result,elem) -> !matching.test(elem)).reverse();
	}
	
	public QualifiedName right(int index) {
		if (index <= 0) return ROOT;
		if (index == 1) return QualifiedName.of(part);
		return parent.right(index-1).add(part);
	}
	
	public QualifiedName leftFromEnd(int index) {
		if (index <= 0) return this;
		return parent.leftFromEnd(index-1);
	}

	
	/** Match against a sequence of regular expressions
	 * 
	 * @param pattern A qualified name formed of regular expressions
	 * @param match_all
	 * @return true if regex parts from pattern match parts of this name 
	 */
	public boolean matches(QualifiedName pattern, boolean match_all) {
		return pattern.matches(this, (regex, myPart) -> Pattern.matches(regex, myPart), match_all);
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