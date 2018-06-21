package coldash.easynlu.parse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringTuple implements Iterable<String>{
	public interface ModifierFn {
		String modify(String input);
	}
	
	public interface ConditionFn {
		boolean condition(String input);
	}
	
	List<String> items;
	String repr;
	
	public StringTuple(String s){
		this(s, stringToItems(s));
	}
	
	public static StringTuple fromParts(String ...parts) {
		return fromList(Arrays.asList(parts));
	}
	
	public static StringTuple fromList(List<String> list) {
		List<String> items = new LinkedList<String>(list);
		String repr = joinList(" ", items);
		
		return new StringTuple(repr, items);
	}
	
	private StringTuple(String s, List<String> items) {
		repr = s;
		this.items = items;
	}
	
	public int size() {
		return items.size();
	}

	
	public String get(int index) throws IndexOutOfBoundsException {
		if( index < 0 || index >= items.size())
			throw new IndexOutOfBoundsException();
		
		return items.get(index);
	}
	
	public int find(ConditionFn fn){
		for(int i = 0; i < items.size(); i++) {
			if(fn.condition(items.get(i)))
				return i;
		}
		
		return -1;
	}
	
	public StringTuple modify(int index, ModifierFn fn) throws IndexOutOfBoundsException{
		if( index < 0 || index >= items.size())
			throw new IndexOutOfBoundsException();
		List<String> copy = stringToItems(repr);
		
		copy.set(index, fn.modify(items.get(index)));
		
		String modifiedRepr = joinList(" ", copy);
		
		return new StringTuple(modifiedRepr, copy);
	}
	
	public StringTuple remove(int index) throws IndexOutOfBoundsException{
		if( index < 0 || index >= items.size())
			throw new IndexOutOfBoundsException();
		
		List<String> copy = stringToItems(repr);
		
		copy.remove(index);
		
		String modifiedRepr = joinList(" ", copy);
		
		return new StringTuple(modifiedRepr, copy);
	}
	
	public StringTuple insert(int index, String value) throws IndexOutOfBoundsException{
		if( index < 0 || index >= items.size())
			throw new IndexOutOfBoundsException();
		
		List<String> copy = stringToItems(repr);
		
		copy.add(index, value);
		
		String modifiedRepr = joinList(" ", copy);
		
		return new StringTuple(modifiedRepr, copy);
	}

	@Override
	public Iterator<String> iterator() {
		return items.iterator();
	}
	
	@Override
	public int hashCode() {
		return repr.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof StringTuple) {
			StringTuple t = (StringTuple)other;
			return repr.equals(t.repr); 
		}else {
			return false;
		}
	}

	@Override
	public String toString() {
		return repr;
	}
	
	private static List<String> stringToItems(String s){
		return new LinkedList<String>(Arrays.asList(s.split(" ")));
	}

	public static <S> String joinList(String separator, List<S> list){
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < list.size(); i++) {
	        if(i > 0)
	            sb.append(separator);
            sb.append(list.get(i).toString());
        }

        return sb.toString();
    }
}
