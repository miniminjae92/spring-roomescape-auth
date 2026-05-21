package roomescape.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Store;

@Repository
public class StoreRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public StoreRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("store")
                .usingColumns("name")
                .usingGeneratedKeyColumns("id");
    }

    public Store save(Store store) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", store.getName());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Store.from(id, store.getName());
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(1) FROM store WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return count != null && count > 0;
    }
}
