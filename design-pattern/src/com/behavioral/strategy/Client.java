package com.behavioral.strategy;

public class Client {

	public static void main(String[] args) {
		executeForBankUsers();
		executeForCardUsers();
	}

	private static void executeForCardUsers() {
		DomainModel cardModel = new DomainModel() {
			@Override
			public String getPartnerId() {
				return Partner.CARD;
			}
		};
		StrategyBuilder.getStrategy(cardModel).build().executeStrategy(cardModel);
	}

	private static void executeForBankUsers() {
		DomainModel bankModel = new DomainModel() {
			@Override
			public String getPartnerId() {
				return Partner.BANK;
			}
		};
		StrategyBuilder.getStrategy(bankModel).build().executeStrategy(bankModel);
	}

}
