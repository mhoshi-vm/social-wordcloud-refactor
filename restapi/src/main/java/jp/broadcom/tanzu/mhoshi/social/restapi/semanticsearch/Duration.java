package jp.broadcom.tanzu.mhoshi.social.restapi.semanticsearch;

enum Duration {

	DAY("1 day"), WEEK("7 days"), MONTH("1 month");

	private final String value;

	Duration(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
