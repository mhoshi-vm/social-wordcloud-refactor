package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadAsString;
import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadSqlAsString;

@Component
class AnalyticsComponent {

    String ENABLE_EXTENSIONS;
    String CREATE_VADER_SENTIMENT_SQL;
    String CREATE_VADER_SENTIMENT_SCRIPT;
    String TERM_FREQUENCY_RANKING_SQL;
    String UPDATE_TS_VECTOR_SQL;
    String UPDATE_VADER_SENTIMENT_SQL;

    JdbcClient jdbcClient;

    SqlGenerator sqlGenerator;

    AnalyticsConfigProperties analyticsConfigProperties;

    AnalyticsComponent(JdbcClient jdbcClient, SqlGenerator sqlGenerator, AnalyticsConfigProperties analyticsConfigProperties) {
        this.sqlGenerator = sqlGenerator;
        this.jdbcClient = jdbcClient;
        this.analyticsConfigProperties = analyticsConfigProperties;
        updateSqlFiles(analyticsConfigProperties.database());
        enableExtensions();
        createVaderSentimentFunction();
    }

    void updateSqlFiles(String db){
        ENABLE_EXTENSIONS = String.format("db/%s/enableExtensions.sql",db);
        CREATE_VADER_SENTIMENT_SQL = String.format("db/%s/vaderSentimentFunction.sql",db);
        CREATE_VADER_SENTIMENT_SCRIPT = String.format("db/%s/vaderSentimentFunction.py",db);
        TERM_FREQUENCY_RANKING_SQL = String.format("db/%s/termFrequencyRanking.sql",db);
        UPDATE_TS_VECTOR_SQL = String.format("db/%s/updateTsvector.sql",db);
        UPDATE_VADER_SENTIMENT_SQL = String.format("db/%s/updateVaderSentiment.sql",db);
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

    @Scheduled(fixedRateString = "${analytics.term-frequency-interval}")
    void termFrequencyRanking() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(TERM_FREQUENCY_RANKING_SQL), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-tsvector-interval}")
    void updateTsvector() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(UPDATE_TS_VECTOR_SQL), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-vader-sentiment-interval}")
    void updateVaderSentiment() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(UPDATE_VADER_SENTIMENT_SQL), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

}
