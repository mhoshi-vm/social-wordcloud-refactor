package jp.broadcom.tanzu.mhoshi.social.collector.shared;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for masking sensitive data in logs.
 */
public final class SensitiveDataMasker {

	private static final String MASK = "****";

	private static final List<String> SENSITIVE_HEADER_NAMES = List.of("authorization", "x-api-key", "api-key",
			"apikey");

	private static final Pattern API_KEY_PATTERN = Pattern.compile("(apiKey|api_key|key)=([^&]+)",
			Pattern.CASE_INSENSITIVE);

	private SensitiveDataMasker() {
		// Utility class
	}

	/**
	 * Masks sensitive headers in HTTP headers.
	 * @param headers the original headers
	 * @return a string representation with sensitive values masked
	 */
	public static String maskHeaders(HttpHeaders headers) {
		if (headers == null || headers.isEmpty()) {
			return "[]";
		}

		StringBuilder masked = new StringBuilder("[");
		headers.forEach((name, values) -> {
			if (isSensitiveHeader(name)) {
				masked.append(name).append(":").append(MASK).append(", ");
			}
			else {
				masked.append(name).append(":").append(values).append(", ");
			}
		});

		// Remove trailing comma and space
		if (masked.length() > 1) {
			masked.setLength(masked.length() - 2);
		}

		masked.append("]");
		return masked.toString();
	}

	/**
	 * Masks sensitive query parameters in URI.
	 * @param uri the original URI
	 * @return URI string with sensitive query parameters masked
	 */
	public static String maskUri(URI uri) {
		if (uri == null) {
			return "";
		}

		String uriString = uri.toString();
		String query = uri.getQuery();

		if (query == null || query.isEmpty()) {
			return uriString;
		}

		// Mask API keys in query parameters
		String maskedQuery = API_KEY_PATTERN.matcher(query).replaceAll("$1=" + MASK);

		return uriString.replace(query, maskedQuery);
	}

	/**
	 * Masks sensitive data in an HTTP request (both URI and headers).
	 * @param request the HTTP request
	 * @return formatted string with masked sensitive data
	 */
	public static String maskRequest(HttpRequest request) {
		return "URI: " + maskUri(request.getURI()) + ", Headers: " + maskHeaders(request.getHeaders());
	}

	private static boolean isSensitiveHeader(String headerName) {
		if (headerName == null) {
			return false;
		}
		String lowerName = headerName.toLowerCase();
		return SENSITIVE_HEADER_NAMES.stream().anyMatch(lowerName::contains);
	}

}
