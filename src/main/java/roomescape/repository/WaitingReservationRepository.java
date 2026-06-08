package roomescape.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingReservation;
import roomescape.domain.WaitingReservationStatus;
import roomescape.global.exception.waitingreservation.WaitingReservationNotFoundException;
import roomescape.repository.dto.WaitingReservationWithRank;

@Repository
public class WaitingReservationRepository {

    private static final String SELECT_COLUMNS = """
            wr.id,
            wr.store_id,
            wr.member_id,
            wr.name,
            wr.date,
            wr.status,
            wr.created_at,
            wr.promoted_reservation_id,
            rt.id AS time_id,
            rt.start_at AS time_start_at,
            t.id AS theme_id,
            t.name AS theme_name,
            t.description,
            t.image_path
            """;

    private static final String JOINS = """
            FROM waiting_reservation wr
            INNER JOIN reservation_time rt ON wr.time_id = rt.id
            INNER JOIN theme t ON wr.theme_id = t.id
            """;

    private static final RowMapper<WaitingReservation> waitingRowMapper = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.from(
                rs.getLong("time_id"),
                rs.getObject("time_start_at", LocalTime.class)
        );
        Theme theme = Theme.from(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("image_path")
        );
        ReservationSlot slot = ReservationSlot.of(
                rs.getLong("store_id"),
                rs.getObject("date", LocalDate.class),
                time,
                theme
        );
        return WaitingReservation.from(
                rs.getLong("id"),
                rs.getLong("member_id"),
                rs.getString("name"),
                slot,
                WaitingReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("promoted_reservation_id", Long.class)
        );
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("waiting_reservation")
                .usingColumns(
                        "store_id", "member_id", "name", "date", "time_id", "theme_id",
                        "status", "created_at", "promoted_reservation_id"
                )
                .usingGeneratedKeyColumns("id");
    }

    public WaitingReservation save(WaitingReservation waiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("store_id", waiting.getStoreId())
                .addValue("member_id", waiting.getMemberId())
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTime().getId())
                .addValue("theme_id", waiting.getTheme().getId())
                .addValue("status", waiting.getStatus().name())
                .addValue("created_at", Timestamp.valueOf(waiting.getCreatedAt()))
                .addValue("promoted_reservation_id", waiting.getPromotedReservationId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return WaitingReservation.from(
                id,
                waiting.getMemberId(),
                waiting.getName(),
                waiting.getSlot(),
                waiting.getStatus(),
                waiting.getCreatedAt(),
                waiting.getPromotedReservationId()
        );
    }

    public Optional<WaitingReservation> findById(Long id) {
        String sql = "SELECT " + SELECT_COLUMNS + JOINS + " WHERE wr.id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, parameters, waitingRowMapper).stream().findFirst();
    }

    public Optional<WaitingReservation> findByIdForUpdate(Long id) {
        String sql = "SELECT " + SELECT_COLUMNS + JOINS + " WHERE wr.id = :id FOR UPDATE";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, parameters, waitingRowMapper).stream().findFirst();
    }

    public Optional<WaitingReservation> findOldestActiveBySlotForUpdate(ReservationSlot slot) {
        String sql = "SELECT " + SELECT_COLUMNS + JOINS + """
                 WHERE wr.store_id = :storeId
                   AND wr.date = :date
                   AND wr.time_id = :timeId
                   AND wr.theme_id = :themeId
                   AND wr.status = 'WAITING'
                 ORDER BY wr.created_at, wr.id
                 LIMIT 1
                 FOR UPDATE
                """;
        return jdbcTemplate.query(sql, slotParameters(slot), waitingRowMapper).stream().findFirst();
    }

    public List<WaitingReservationWithRank> findActiveByMemberId(Long memberId, int size, int offset) {
        String sql = "SELECT " + SELECT_COLUMNS + """
                ,
                (
                    SELECT COUNT(1) + 1
                    FROM waiting_reservation earlier
                    WHERE earlier.store_id = wr.store_id
                      AND earlier.date = wr.date
                      AND earlier.time_id = wr.time_id
                      AND earlier.theme_id = wr.theme_id
                      AND earlier.status = 'WAITING'
                      AND (
                          earlier.created_at < wr.created_at
                          OR (earlier.created_at = wr.created_at AND earlier.id < wr.id)
                      )
                ) AS waiting_rank
                """ + JOINS + """
                 WHERE wr.member_id = :memberId
                   AND wr.status = 'WAITING'
                 ORDER BY wr.created_at, wr.id
                 LIMIT :size OFFSET :offset
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) ->
                new WaitingReservationWithRank(waitingRowMapper.mapRow(rs, rowNum), rs.getInt("waiting_rank"))
        );
    }

    public List<WaitingReservation> findAllByStoreIds(List<Long> storeIds, int size, int offset) {
        if (storeIds.isEmpty()) {
            return List.of();
        }
        String sql = "SELECT " + SELECT_COLUMNS + JOINS + """
                 WHERE wr.store_id IN (:storeIds)
                 ORDER BY wr.created_at, wr.id
                 LIMIT :size OFFSET :offset
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("storeIds", storeIds)
                .addValue("size", size)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, parameters, waitingRowMapper);
    }

    public boolean existsActiveByMemberAndSlot(Long memberId, ReservationSlot slot) {
        String sql = """
                SELECT COUNT(1)
                FROM waiting_reservation
                WHERE member_id = :memberId
                  AND store_id = :storeId
                  AND date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'WAITING'
                """;
        MapSqlParameterSource parameters = (MapSqlParameterSource) slotParameters(slot);
        parameters.addValue("memberId", memberId);
        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsActiveBySlot(ReservationSlot slot) {
        String sql = """
                SELECT COUNT(1)
                FROM waiting_reservation
                WHERE store_id = :storeId
                  AND date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'WAITING'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, slotParameters(slot), Integer.class);
        return count != null && count > 0;
    }

    public int updateStatus(
            WaitingReservation waiting,
            WaitingReservationStatus expectedStatus
    ) {
        String sql = """
                UPDATE waiting_reservation
                SET status = :status,
                    promoted_reservation_id = :promotedReservationId
                WHERE id = :id
                  AND status = :expectedStatus
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", waiting.getId())
                .addValue("status", waiting.getStatus().name())
                .addValue("promotedReservationId", waiting.getPromotedReservationId())
                .addValue("expectedStatus", expectedStatus.name());
        return jdbcTemplate.update(sql, parameters);
    }

    public void deleteById(Long id) {
        int deletedCount = jdbcTemplate.update(
                "DELETE FROM waiting_reservation WHERE id = :id",
                new MapSqlParameterSource("id", id)
        );
        if (deletedCount == 0) {
            throw new WaitingReservationNotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }
    }

    private MapSqlParameterSource slotParameters(ReservationSlot slot) {
        return new MapSqlParameterSource()
                .addValue("storeId", slot.getStoreId())
                .addValue("date", slot.getDate())
                .addValue("timeId", slot.getTime().getId())
                .addValue("themeId", slot.getTheme().getId());
    }
}
