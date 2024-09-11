package com.creational.singleton;

public class Client {

	public static void main(String[] args) {

		// Enums are singleton by default
		Instances.DaysOfWeek.WEEKEND.showHashCode();
		Instances.DaysOfWeek.WEEKEND.showHashCode();

		Instances.EagerSingleton.getInstance().showHashCode();
		Instances.EagerSingleton.getInstance().showHashCode();

		Instances.EagerStaticBlockSingleton.getInstance().showHashCode();
		Instances.EagerStaticBlockSingleton.getInstance().showHashCode();

		Instances.LazySingleton.getInstance().showHashCode();
		Instances.LazySingleton.getInstance().showHashCode();

		Instances.LazyInnerClassSingleton.getInstance().showHashCode();
		Instances.LazyInnerClassSingleton.getInstance().showHashCode();

		Instances.LazyDoubleCheckedLockingSingleton.getInstance().showHashCode();
		Instances.LazyDoubleCheckedLockingSingleton.getInstance().showHashCode();
	}
}
