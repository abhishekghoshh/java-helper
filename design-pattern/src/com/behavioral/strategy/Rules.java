package com.behavioral.strategy;

import java.util.Random;

public final class Rules {
	private Rules() {
	}

	private final static Object object = new Object();

	public static final Rule RULE_1 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_1");
	};
	public static final Rule RULE_2 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_2");
	};
	public static final Rule RULE_3 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_3");
	};
	public static final Rule RULE_4 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_4");
	};
	public static final Rule RULE_5 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_5");
	};
	public static final Rule RULE_6 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_6");
	};
	public static final Rule RULE_7 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_7");
	};
	public static final Rule RULE_8 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_8");
	};
	public static final Rule RULE_9 = domainModel -> {
		synchronizedProcess(domainModel, "RULE_9");
	};
	public static final Rule RULE_10 = domainModel -> {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronizedProcess(domainModel, "RULE_10");
	};

	private static synchronized void synchronizedProcess(DomainModel domainModel, String ruleName) {
		synchronized (object) {
			domainModel.setData(ruleName);
			domainModel.printThread("Executing rule no");
			try {
				Thread.sleep(new Random().nextInt(100, 200));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
