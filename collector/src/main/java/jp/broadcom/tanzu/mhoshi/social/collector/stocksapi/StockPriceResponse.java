package jp.broadcom.tanzu.mhoshi.social.collector.stocksapi;

import java.time.Instant;

record StockPriceResponse(
// @formatter:off
		String ticker,
		Float price,
		Instant updated,
		Integer volume
		// @formatter:on
) {
}
