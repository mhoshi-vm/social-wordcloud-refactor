package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.Embedding;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.IntStream;

import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadAsString;
import static jp.broadcom.tanzu.mhoshi.socialanalytics.FileLoader.loadSqlAsString;

@Component
@Async
class AnalyticsComponent {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsComponent.class);
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
        createVaderSentimentFunction();
    }

    private static SqlScripts createScriptPaths(String db) {
        String base = "db/" + db + "/";
        return new SqlScripts(
                base + "vaderSentimentFunction.sql",
                base + "vaderSentimentFunction.py",
                base + "updateTsvector.sql",
                base + "refreshTsvector.sql",
                base + "updateVaderSentiment.sql",
                base + "updateEmbeddings_1.sql",
                base + "updateEmbeddings_2.sql",
                base + "updateGisInfo_1.sql",
                base + "updateGisInfo_2.sql",
                base + "insertSocialMessage.sql",
                base + "deleteSocialMessage.sql",
                base + "maintenance.sql"
        );
    }

    void createVaderSentimentFunction() {
        logger.debug("createVaderSentimentFunction");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String plpythonscript = loadAsString(sqlScripts.createVaderScript());
        params.addValue("plpythonscript", plpythonscript);
        final String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.createVaderSql), params.getValues());
        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-tsvector-interval}")
    void updateTsvector() {
        logger.debug("updateTsvector");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateTsVector), params);

        int rowsInserted = this.jdbcClient.sql(sql).update();

        if (rowsInserted > 0) {
            logger.info("Tsvector updated, refresh");
            String refreshSql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.refreshTsVector), params);
            jdbcClient.sql(refreshSql).update();
        }
    }

    @Scheduled(fixedRateString = "${analytics.update-vader-sentiment-interval}")
    void updateVaderSentiment() {
        logger.debug("updateVaderSentiment");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateVader), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    @Scheduled(fixedRateString = "${analytics.update-embeddings-interval}")
    void updateEmbeddings() {
        logger.debug("updateEmbeddings");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.updateEmbeddings_1), params);
        final List<SocialMessage> messages = this.jdbcClient.sql(sql).query(SocialMessage.class).list();
        if (!messages.isEmpty()) {
            List<Embedding> embeddings = analyticsAiService.getEmbeddingResponse(messages.stream().map(SocialMessage::text).toList()).getResults();

            if (messages.size() == embeddings.size()) {
                this.jdbcTemplate.batchUpdate(loadAsString(sqlScripts.updateEmbeddings_2), new BatchPreparedStatementSetter() {

                    private static final ObjectMapper mapper = new ObjectMapper();

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        SocialMessage document = messages.get(i);
                        Embedding embedding = embeddings.get(i);

                        Connection conn = ps.getConnection();
                        Float[] embeddingArray = IntStream.range(0, embedding.getOutput().length)
                                .mapToObj(j -> embedding.getOutput()[j])
                                .toArray(Float[]::new);

                        ps.setString(1, document.id());
                        ps.setTimestamp(2, Timestamp.valueOf(document.createDateTime()));
                        try {
                            ps.setObject(3, mapper.writeValueAsString(embedding.getMetadata()), java.sql.Types.OTHER);
                        } catch (Exception e) {
                            throw new SQLException("Failed to serialize metadata", e);
                        }
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

    @Scheduled(fixedRateString = "${analytics.update-guess-gis-info}")
    void updateGuessGisInfo() {
        logger.debug("updateGuessGisInfo");
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
                    ps.setTimestamp(2, Timestamp.valueOf(document.createDateTime()));
                    ps.setInt(3, document.srid());
                    ps.setString(4, document.gis());
                    ps.setString(5, document.reason());

                }

                @Override
                public int getBatchSize() {
                    return gisInfos.size();
                }
            });
        }
    }


    @Scheduled(cron = "${analytics.maintenance-cron}")
    void dbMaintenance() {
        logger.debug("dbMaintenance");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.maintenance), params);

        this.jdbcClient
                .sql(sql)
                .update();
    }

    void insertSocialMessages(List<SocialMessage> socialMessages) {
        logger.debug("insertSocialMessages");

        this.jdbcTemplate.batchUpdate(loadAsString(sqlScripts.insertSocialMessages), new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SocialMessage document = socialMessages.get(i);

                ps.setString(1, document.id());
                ps.setString(2, document.origin());
                ps.setString(3, document.text());
                ps.setString(4, document.lang());
                ps.setString(5, document.name());
                ps.setString(6, document.url());
                ps.setTimestamp(7, Timestamp.valueOf(document.createDateTime()));

            }

            @Override
            public int getBatchSize() {
                return socialMessages.size();
            }
        });

    }

    void deleteSocialMessages(List<String> socialMessagesIds) {
        logger.debug("deleteSocialMessages");
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = this.sqlGenerator.generate(loadSqlAsString(sqlScripts.deleteSocialMessages), params);
        this.jdbcClient
                .sql(sql)
                .update();
    }

    private record SqlScripts(
            String createVaderSql,
            String createVaderScript,
            String updateTsVector,
            String refreshTsVector,
            String updateVader,
            String updateEmbeddings_1,
            String updateEmbeddings_2,
            String updateGisInfo_1,
            String updateGisInfo_2,
            String insertSocialMessages,
            String deleteSocialMessages,
            String maintenance
    ) {
    }

}
