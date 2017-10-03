package de.maxhenkel.reap.config;

public interface Selector<T> {

	public boolean isValid(T element);
	
}
