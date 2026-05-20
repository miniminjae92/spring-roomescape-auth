package roomescape.repository;

import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;

@Repository
public class MemberRepository {

    private static final RowMapper<Member> memberRowMapper = (rs, rowNum) -> Member.from(
            rs.getLong("id"),
            rs.getString("login_id"),
            rs.getString("password"),
            rs.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public MemberRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("member")
                .usingColumns("login_id", "password", "name")
                .usingGeneratedKeyColumns("id");
    }

    public Member save(Member member) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("login_id", member.getLoginId())
                .addValue("password", member.getPassword())
                .addValue("name", member.getName());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Member.from(id, member.getLoginId(), member.getPassword(), member.getName());
    }

    public Optional<Member> findByLoginId(String loginId) {
        String sql = """
                SELECT id, login_id, password, name
                FROM member
                WHERE login_id = :loginId
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("loginId", loginId);
        return jdbcTemplate.query(sql, parameters, memberRowMapper)
                .stream()
                .findFirst();
    }

    public Optional<Member> findById(Long id) {
        String sql = """
                SELECT id, login_id, password, name
                FROM member
                WHERE id = :id
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.query(sql, parameters, memberRowMapper)
                .stream()
                .findFirst();
    }

}
