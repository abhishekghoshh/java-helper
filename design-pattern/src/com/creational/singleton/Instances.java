package com.creational.singleton;

import java.util.Arrays;

public final class Instances {
	public static enum DaysOfWeek implements Instance {
		WEEKDAY("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"), WEEKEND("Saturday", "Sunday");

		private String[] days;

		DaysOfWeek(String... days) {
			System.out.println("Initializing DaysOfWeek with " + Arrays.toString(days));
			this.days = days;
		}

		public String[] getDays() {
			return this.days;
		}

		public String day(int i) {
			return null != days && i < days.length ? days[i] : null;
		}

		public int size() {
			return null != days ? days.length : -1;
		}

		@Override
		public String toString() {
			return "{" + "days=" + Arrays.toString(days) + '}';
		}
	}

	public static class EagerSingleton implements Instance {

		/** private constructor to prevent others from instantiating this class */
		private EagerSingleton() {
		}

		/** Create an instance of the class at the time of class loading */
		private static final EagerSingleton instance = new EagerSingleton();

		/** Provide a global point of access to the instance */
		public static EagerSingleton getInstance() {
			return instance;
		}
	}

	public static class EagerStaticBlockSingleton implements Instance {

		private static final EagerStaticBlockSingleton instance;

		/** Don't let anyone else instantiate this class */
		private EagerStaticBlockSingleton() {
		}

		/** Create the one-and-only instance in a static block */
		static {
			try {
				instance = new EagerStaticBlockSingleton();
			} catch (Exception ex) {
				throw ex;
			}
		}

		/** Provide a public method to get the instance that we created */
		public static EagerStaticBlockSingleton getInstance() {
			return instance;
		}
	}

	public static class LazyInnerClassSingleton implements Instance {

		private LazyInnerClassSingleton() {
		}

		/**
		 * This inner class is loaded only after getInstance() is called for the first
		 * time.
		 */
		private static class SingletonHelper {
			private static final LazyInnerClassSingleton INSTANCE = new LazyInnerClassSingleton();
		}

		public static LazyInnerClassSingleton getInstance() {
			return SingletonHelper.INSTANCE;
		}
	}

	public static class LazySingleton implements Instance {

		private static LazySingleton instance = null;

		/** Don't let anyone else instantiate this class */
		private LazySingleton() {
		}

		/**
		 * only when someone tries to access the getInstance method that's when the
		 * instance is created
		 **/
		public static synchronized LazySingleton getInstance() {
			if (instance == null) {
				instance = new LazySingleton();
			}
			return instance;
		}
	}

	public static class LazyDoubleCheckedLockingSingleton implements Instance {

		private static volatile LazyDoubleCheckedLockingSingleton instance;

		/** private constructor to prevent others from instantiating this class */
		private LazyDoubleCheckedLockingSingleton() {
		}

		/** Lazily initialize the singleton in a synchronized block */
		public static LazyDoubleCheckedLockingSingleton getInstance() {
			if (instance == null) {
				synchronized (LazyDoubleCheckedLockingSingleton.class) {
					if (instance == null) {
						instance = new LazyDoubleCheckedLockingSingleton();
					}
				}
			}
			return instance;
		}
	}

}
