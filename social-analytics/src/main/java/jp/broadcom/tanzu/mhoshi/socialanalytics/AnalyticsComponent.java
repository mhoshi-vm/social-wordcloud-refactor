package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadAsString;
import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadSqlAsString;

@Component
class AnalyticsComponent {

    SqlScripts sqlScripts;
    JdbcClient jdbcClient;
    SqlGenerator sqlGenerator;
    AnalyticsConfigProperties analyticsConfigProperties;

    AnalyticsComponent(JdbcClient jdbcClient, SqlGenerator sqlGenerator, AnalyticsConfigProperties analyticsConfigProperties) {
        this.sqlGenerator = sqlGenerator;
        this.jdbcClient = jdbcClient;
        this.analyticsConfigProperties = analyticsConfigProperties;
        this.sqlScripts = createScriptPaths(analyticsConfigProperties.database());
        enableExtensions();
        createVaderSentimentFunction();
    }

    private static SqlScripts createScriptPaths(String db) {
        String base = "db/" + db + "/";
        return new SqlScripts(
                base + "enableExtensions.sql",
                base + "vaderSentimentFunction.sql",
                base + "vaderSentimentFunction.py",
                base + "termFrequencyRanking.sql",
                base + "updateTsvector.sql",
                base + "updateVaderSentiment.sql"
        );
    }

    void enableExtensions() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.enableExtensions), params.getValues());
        this.jdbcClient
                .sql(sql)
                .update();
    }

    void createVaderSentimentFunction() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String plpythonscript = "'" + loadAsString(sqlScripts.createVaderScript()) + "'";
        params.addValue("plpythonscript", plpythonscript);
        final String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.createVaderSql), params.getValues());
        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.term-frequency-interval}")
    void termFrequencyRanking() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.termFrequency), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-tsvector-interval}")
    void updateTsvector() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateTsVector), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-vader-sentiment-interval}")
    void updateVaderSentiment() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateVader), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    private record SqlScripts(
            String enableExtensions,
            String createVaderSql,
            String createVaderScript,
            String termFrequency,
            String updateTsVector,
            String updateVader
    ) {
    }

}
