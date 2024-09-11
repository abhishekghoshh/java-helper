package com.behavioral.strategy;

public class StrategyBuilder {

	public static Strategy getStrategy(Partner partner) {
		switch (partner.getPartnerId()) {
		case Partner.BANK:
			return new BankExecutionStrategy();
		case Partner.CARD:
			return new CardExecutionStrategy();
		default:
			throw new RuntimeException("unknown partner id");
		}
	}
}
