package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.ai.embedding.Embedding;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadAsString;
import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadSqlAsString;

@Component
class AnalyticsComponent {

    SqlScripts sqlScripts;
    JdbcClient jdbcClient;
    JdbcTemplate jdbcTemplate;
    SqlGenerator sqlGenerator;
    AnalyticsAiService analyticsAiService;

    AnalyticsConfigProperties analyticsConfigProperties;

    AnalyticsComponent(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator, AnalyticsAiService analyticsAiService, AnalyticsConfigProperties analyticsConfigProperties) {
        this.sqlGenerator = sqlGenerator;
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcClient = jdbcClient;
        this.analyticsConfigProperties = analyticsConfigProperties;
        this.analyticsAiService = analyticsAiService;
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
                base + "updateVaderSentiment.sql",
                base + "updateEmbeddings_1.sql",
                base + "updateEmbeddings_2.sql",
                base + "updateGisInfo_1.sql",
                base + "updateGisInfo_2.sql"
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

    @Scheduled(fixedDelayString = "${analytics.term-frequency-interval}")
    void termFrequencyRanking() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.termFrequency), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }


    @Scheduled(fixedDelayString = "${analytics.update-tsvector-interval}")
    void updateTsvector() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateTsVector), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedDelayString = "${analytics.update-vader-sentiment-interval}")
    void updateVaderSentiment() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateVader), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedDelayString = "${analytics.update-embeddings-interval}")
    void updateEmbeddings() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateEmbeddings_1), params);
        final List<SocialMessage> messages = this.jdbcClient.sql(sql).query(SocialMessage.class).list();
        if (!messages.isEmpty()) {
            List<Embedding> embeddings = analyticsAiService.getEmbeddingResponse(messages.stream().map(SocialMessage::text).toList()).getResults();

            if (messages.size() == embeddings.size()) {
                this.jdbcTemplate.batchUpdate(loadAsString(sqlScripts.updateEmbeddings_2), new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        SocialMessage document = messages.get(i);
                        Embedding embedding = embeddings.get(i);
                        ObjectMapper mapper = new ObjectMapper();

                        Connection conn = ps.getConnection();
                        Float[] embeddingArray = IntStream.range(0, embedding.getOutput().length)
                                .mapToObj(j -> embedding.getOutput()[j])
                                .toArray(Float[]::new);

                        ps.setString(1, document.id());
                        ps.setString(2, document.text());
                        ps.setString(3, mapper.writeValueAsString(embedding.getMetadata()));
                        ps.setArray(4, conn.createArrayOf("FLOAT", embeddingArray));

                    }

                    @Override
                    public int getBatchSize() {
                        return messages.size();
                    }
                });
            }
        }
    }

    @Scheduled(fixedDelayString = "${analytics.update-guess-gis-info}")
    void updateGuessGisInfo() {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateGisInfo_1), params);
        final List<SocialMessage> messages = this.jdbcClient.sql(sql).query(SocialMessage.class).list();
        List<GisInfo> gisInfos = analyticsAiService.getGisInfo(messages.stream().map(Record::toString).toList());

        if (!gisInfos.isEmpty()) {
            this.jdbcTemplate.batchUpdate(loadAsString(sqlScripts.updateGisInfo_2), new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    GisInfo document = gisInfos.get(i);

                    ps.setString(1, document.messageId());
                    ps.setInt(2, document.srid());
                    ps.setString(3, document.gis());
                    ps.setString(4, document.reason());

                }

                @Override
                public int getBatchSize() {
                    return gisInfos.size();
                }
            });
        }
    }

    private record SqlScripts(
            String enableExtensions,
            String createVaderSql,
            String createVaderScript,
            String termFrequency,
            String updateTsVector,
            String updateVader,
            String updateEmbeddings_1,
            String updateEmbeddings_2,
            String updateGisInfo_1,
            String updateGisInfo_2
    ) {
    }

}
