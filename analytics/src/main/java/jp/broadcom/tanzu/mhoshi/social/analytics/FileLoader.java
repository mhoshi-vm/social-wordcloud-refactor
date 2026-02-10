package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class FileLoader {

	private static final Map<String, String> cache = new ConcurrentHashMap<>();

	static String loadSqlAsString(String file) {
		return "/* %s */ %s".formatted(file, loadAsString(file));
	}

	static String loadAsString(String file) {
		return cache.computeIfAbsent(file, f -> {
			try (final InputStream stream = new ClassPathResource(file).getInputStream()) {
				return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

}
