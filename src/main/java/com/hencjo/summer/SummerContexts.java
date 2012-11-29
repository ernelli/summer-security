package com.hencjo.summer;

public final class SummerContexts {
	private static SummerContext instance = new SummerContext() {
		@Override
		public String getAuthenticatedAs() {
			return "admin";
		}
	};

	public static SummerContext getContext() {
		return instance;
	}
}
