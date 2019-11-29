package net.runelite.client.plugins.fred.api.other;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

public abstract class Tuples
{

	public static <A, B> T2<A, B> of(A a, B b)
	{
		return new T2<>(a, b);
	}

	public static <A, B, C> T3<A, B, C> of(A a, B b, C c)
	{
		return new T3<>(a, b, c);
	}

	public static <A, B, C, D> T4<A, B, C, D> of(A a, B b, C c, D d)
	{
		return new T4<>(a, b, c, d);
	}

	public static <A, B, C, D, E> T5<A, B, C, D, E> of(A a, B b, C c, D d, E e)
	{
		return new T5<>(a, b, c, d, e);
	}

	public static <A, B, C, D, E, F> T6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f)
	{
		return new T6<>(a, b, c, d, e, f);
	}

	static abstract class Tuple<IDENT>
	{

	}

	@Getter
	@ToString
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	public static class T2<T, U> extends Tuple<T2>
	{
		T _1;
		U _2;
	}

	@Getter
	@ToString
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	public static class T3<T, U, V> extends Tuple<T3>
	{
		T _1;
		U _2;
		V _3;
	}

	@Getter
	@ToString
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	public static class T4<T, U, V, W> extends Tuple<T4>
	{
		T _1;
		U _2;
		V _3;
		W _4;
	}

	@Getter
	@ToString
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	public static class T5<T, U, V, W, X> extends Tuple<T5>
	{
		T _1;
		U _2;
		V _3;
		W _4;
		X _5;
	}

	@Getter
	@ToString
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	public static class T6<T, U, V, W, X, Y> extends Tuple<T6>
	{
		T _1;
		U _2;
		V _3;
		W _4;
		X _5;
		Y _6;
	}
}
