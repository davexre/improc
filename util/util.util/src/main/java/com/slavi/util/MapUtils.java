package com.slavi.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapUtils {

	/**
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     2222222222222222
	 *     11111111111111         22222222222222     11222222222222
	 *     111111111111             222222222222     111122222222
	 *     1111111111                 2222222222     1111112222
	 *     11111111                     22222222     11111111
	 *     111111                         222222     111111
	 *     1111                             2222     1111
	 *     11                                 22     11
	 * </pre>
	 */
	public static void mapUpdateExisting(Map updateInto, Map second) {
		Set<Map.Entry> set = updateInto.entrySet();
		for (Map.Entry i : set) {
			Object newValue = second.get(i.getKey());
			if (newValue != null)
				i.setValue(newValue);
		}
	}

	/**
	 * @see java.awt.geom.Area.exclusiveOr()
	 * <pre>
	 *   updateInto(before) xor second            =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222
	 *     11111111111111         22222222222222     11            22
	 *     111111111111             222222222222     1111        2222
	 *     1111111111                 2222222222     111111    222222
	 *     11111111                     22222222     1111111122222222
	 *     111111                         222222     111111    222222
	 *     1111                             2222     1111        2222
	 *     11                                 22     11            22
	 * </pre>
	 */
	public static void mapXor(Map updateInto, Map second) {
		Set<Map.Entry> set = second.entrySet();
		for (Map.Entry i : set) {
			Object key = i.getKey();
			if (updateInto.containsKey(key))
				updateInto.remove(key);
			else
				updateInto.put(key, i.getValue());
		}
	}

	/**
	 * @see java.awt.geom.Area.add()
	 * <pre>
	 *   updateInto(before) add second            =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     1111111111111111
	 *     11111111111111         22222222222222     1111111111111122
	 *     111111111111             222222222222     1111111111112222
	 *     1111111111                 2222222222     1111111111222222
	 *     11111111                     22222222     1111111122222222
	 *     111111                         222222     111111    222222
	 *     1111                             2222     1111        2222
	 *     11                                 22     11            22
	 * </pre>
	 */
	public static void mapAdd(Map updateInto, Map second) {
		Set<Map.Entry> set = second.entrySet();
		for (Map.Entry i : set) {
			Object key = i.getKey();
			if (!updateInto.containsKey(key))
				updateInto.put(key, i.getValue());
		}
	}

	/**
	 * @see java.awt.geom.Area.add()
	 * <pre>
	 *   updateInto(before) add second            =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     2222222222222222
	 *     11111111111111         22222222222222     1122222222222222
	 *     111111111111             222222222222     1111222222222222
	 *     1111111111                 2222222222     1111112222222222
	 *     11111111                     22222222     1111111122222222
	 *     111111                         222222     111111    222222
	 *     1111                             2222     1111        2222
	 *     11                                 22     11            22
	 * </pre>
	 */
	public static void mapAddAndUpdate(Map updateInto, Map second) {
		updateInto.putAll(second);
	}

	/**
	 * @see java.awt.geom.Area.intersect()
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     1111111111111111
	 *     11111111111111         22222222222222       111111111111
	 *     111111111111             222222222222         11111111
	 *     1111111111                 2222222222           1111
	 *     11111111                     22222222
	 *     111111                         222222
	 *     1111                             2222
	 *     11                                 22
	 * </pre>
	 */
	public static void mapIntersect(Map updateInto, Map second) {
		Object keys[] = updateInto.keySet().toArray();
		for (Object key : keys) {
			if (!second.containsKey(key))
				updateInto.remove(key);
		}
	}

	/**
	 * @see java.awt.geom.Area.intersect()
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     2222222222222222
	 *     11111111111111         22222222222222       222222222222
	 *     111111111111             222222222222         22222222
	 *     1111111111                 2222222222           2222
	 *     11111111                     22222222
	 *     111111                         222222
	 *     1111                             2222
	 *     11                                 22
	 * </pre>
	 */
	public static void mapIntersectAndUpdate(Map updateInto, Map second) {
		Object keys[] = updateInto.keySet().toArray();
		for (Object key : keys) {
			Object newValue = second.get(key);
			if (newValue != null)
				updateInto.put(key, newValue);
			else
				updateInto.remove(key);
		}
	}
	
	/**
	 * @see java.awt.geom.Area.subtract()
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222
	 *     11111111111111         22222222222222     11
	 *     111111111111             222222222222     1111
	 *     1111111111                 2222222222     111111
	 *     11111111                     22222222     11111111
	 *     111111                         222222     111111
	 *     1111                             2222     1111
	 *     11                                 22     11
	 * </pre>
	 */
	public static void mapSubtract(Map updateInto, Map second) {
		Set<Map.Entry> set = second.entrySet();
		for (Map.Entry i : set) {
			Object key = i.getKey();
			if (updateInto.containsKey(key))
				updateInto.remove(key);
		}
	}

	/**
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     2222222222222222
	 *     11111111111111         22222222222222       22222222222222
	 *     111111111111             222222222222         222222222222
	 *     1111111111                 2222222222           2222222222
	 *     11111111                     22222222             22222222
	 *     111111                         222222               222222
	 *     1111                             2222                 2222
	 *     11                                 22                   22
	 * </pre>
	 */
	public static void mapCopyInto(Map updateInto, Map second) {
		updateInto.clear();
		updateInto.putAll(second);
	}
	
	//////////////////////////////////////////////////////////////////
	
	/**
	 * @see java.awt.geom.Area.exclusiveOr()
	 * <pre>
	 *   updateInto(before) xor second            =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222
	 *     11111111111111         22222222222222     11            22
	 *     111111111111             222222222222     1111        2222
	 *     1111111111                 2222222222     111111    222222
	 *     11111111                     22222222     1111111122222222
	 *     111111                         222222     111111    222222
	 *     1111                             2222     1111        2222
	 *     11                                 22     11            22
	 * </pre>
	 */
	public static void collectionXor(Collection updateInto, Collection second) {
		for (Object i : second) {
			if (updateInto.contains(i))
				updateInto.remove(i);
			else
				updateInto.add(i);
		}
	}

	/**
	 * @see java.awt.geom.Area.add()
	 * <pre>
	 *   updateInto(before) add second            =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     1111111111111111
	 *     11111111111111         22222222222222     1111111111111122
	 *     111111111111             222222222222     1111111111112222
	 *     1111111111                 2222222222     1111111111222222
	 *     11111111                     22222222     1111111122222222
	 *     111111                         222222     111111    222222
	 *     1111                             2222     1111        2222
	 *     11                                 22     11            22
	 * </pre>
	 */
	public static void collectionAdd(Collection updateInto, Collection second) {
		updateInto.addAll(second);
	}

	/**
	 * @see java.awt.geom.Area.intersect()
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     1111111111111111
	 *     11111111111111         22222222222222       111111111111
	 *     111111111111             222222222222         11111111
	 *     1111111111                 2222222222           1111
	 *     11111111                     22222222
	 *     111111                         222222
	 *     1111                             2222
	 *     11                                 22
	 * </pre>
	 */
	public static void collectionIntersect(Collection updateInto, Collection second) {
		updateInto.retainAll(second);
	}

	/**
	 * @see java.awt.geom.Area.subtract()
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222
	 *     11111111111111         22222222222222     11
	 *     111111111111             222222222222     1111
	 *     1111111111                 2222222222     111111
	 *     11111111                     22222222     11111111
	 *     111111                         222222     111111
	 *     1111                             2222     1111
	 *     11                                 22     11
	 * </pre>
	 */
	public static void collectionSubtract(Collection updateInto, Collection second) {
		updateInto.removeAll(second);
	}

	/**
	 * <pre>
	 *   updateInto(before) intersect second      =  updateInto(after)
	 *
	 *     1111111111111111     2222222222222222     2222222222222222
	 *     11111111111111         22222222222222       22222222222222
	 *     111111111111             222222222222         222222222222
	 *     1111111111                 2222222222           2222222222
	 *     11111111                     22222222             22222222
	 *     111111                         222222               222222
	 *     1111                             2222                 2222
	 *     11                                 22                   22
	 * </pre>
	 */
	public static void collectionCopyInto(Collection updateInto, Collection second) {
		updateInto.retainAll(second);
		updateInto.addAll(second);
	}
}
