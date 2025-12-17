package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadAsString;
import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadSqlAsString;

class AnalyticsPostgres {

    private static final String ENABLE_EXTENSIONS = "sqls/analytics/enableExtensions.sql";
    private static final String CREATE_VADER_SENTIMENT_SQL = "sqls/analytics/vaderSentimentFunction.sql";
    private static final String CREATE_VADER_SENTIMENT_SCRIPT = "sqls/analytics/vaderSentimentFunction.py";
    private static final String TERM_FREQUENCY_RANKING_SQL = "sqls/analytics/termFrequencyRanking.sql";
    private static final String UPDATE_TS_VECTOR_SQL = "sqls/analytics/updateTsvector.sql";
    private static final String UPDATE_VADER_SENTIMENT_SQL = "sqls/analytics/updateVaderSentiment.sql";

    JdbcClient jdbcClient;

    SqlGenerator sqlGenerator;

    AnalyticsConfigProperties analyticsConfigProperties;

    AnalyticsPostgres(JdbcClient jdbcClient, SqlGenerator sqlGenerator, AnalyticsConfigProperties analyticsConfigProperties) {
        this.sqlGenerator = sqlGenerator;
        this.jdbcClient = jdbcClient;
        this.analyticsConfigProperties = analyticsConfigProperties;
        enableExtensions();
        createVaderSentimentFunction();
    }

    void enableExtensions() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final String sql = this.sqlGenerator.generate(loadSqlAsString(ENABLE_EXTENSIONS), params.getValues());
        this.jdbcClient
                .sql(sql)
                .update();
    }

    void createVaderSentimentFunction() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String plpythonscript = "'"+ loadAsString(CREATE_VADER_SENTIMENT_SCRIPT)+"'";
        params.addValue("plpythonscript", plpythonscript);
        final String sql = this.sqlGenerator.generate(loadSqlAsString(CREATE_VADER_SENTIMENT_SQL), params.getValues());
        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "#{@'jp.broadcom.tanzu.mhoshi.socialanalytics.AnalyticsConfigProperties'.termFrequencyInterval}")
    void termFrequencyRanking() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(TERM_FREQUENCY_RANKING_SQL), params);

        List<TermFrequency> termFrequencies = this.jdbcClient.sql(sql)
                .query(TermFrequency.class)
                .list();
    }

    @Scheduled(fixedRateString = "#{@'jp.broadcom.tanzu.mhoshi.socialanalytics.AnalyticsConfigProperties'.updateTsvectorInterval}")
    void updateTsvector() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(UPDATE_TS_VECTOR_SQL), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "#{@'jp.broadcom.tanzu.mhoshi.socialanalytics.AnalyticsConfigProperties'.updateVaderSentimentInterval}")
    void updateVaderSentiment() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(UPDATE_VADER_SENTIMENT_SQL), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

}
