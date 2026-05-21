package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class StoreManagerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StoreManagerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long storeId, Long memberId) {
        String sql = """
                INSERT INTO store_manager (store_id, member_id)
                VALUES (:storeId, :memberId)
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("storeId", storeId)
                .addValue("memberId", memberId);
        jdbcTemplate.update(sql, parameters);
    }

    public List<Long> findStoreIdsByMemberId(Long memberId) {
        String sql = """
                SELECT store_id
                FROM store_manager
                WHERE member_id = :memberId
                ORDER BY store_id
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("memberId", memberId);
        return jdbcTemplate.queryForList(sql, parameters, Long.class);
    }

    public boolean existsByStoreIdAndMemberId(Long storeId, Long memberId) {
        String sql = """
                SELECT COUNT(1)
                FROM store_manager
                WHERE store_id = :storeId
                  AND member_id = :memberId
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("storeId", storeId)
                .addValue("memberId", memberId);
        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return count != null && count > 0;
    }
}
