package com.hammi.playground.modules.global;

import com.hammi.playground.modules.merchants.Provider;
import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")

public class AppControllers {
    private final AppService appService;
private final JdbcTemplate jdbcTemplate;
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<Provider>>> getFiledEvents() {
        return ResponseEntity.ok().body(new ApiResponse<>(appService.getMerchantProviders()));
    }

    @GetMapping("/debug/field-check/{fieldId}")
    public Map<String, Object> debugFieldCheck(@PathVariable Short fieldId) {
        Map<String, Object> result = new HashMap<>();

        // 1. Connection-ka Spring-ku isticmaalayo — ma isla DB-ga psql?
        result.put("database", jdbcTemplate.queryForObject("SELECT current_database()", String.class));
        result.put("user", jdbcTemplate.queryForObject("SELECT current_user", String.class));
        result.put("host_port", jdbcTemplate.queryForObject(
                "SELECT inet_server_addr() || ':' || inet_server_port()", String.class));

        // 2. Session replication role — haddii 'replica' yahay, triggers waa la iska dhaafaa
        result.put("session_replication_role", jdbcTemplate.queryForObject(
                "SHOW session_replication_role", String.class));

        // 3. Trigger-ku ma jiraa, ma enabled baa? (O = fires normally, D = disabled)
        List<Map<String, Object>> triggers = jdbcTemplate.queryForList(
                "SELECT tgname, tgenabled FROM pg_trigger WHERE tgrelid = 'fields'::regclass AND NOT tgisinternal");
        result.put("triggers", triggers);

        // 4. Field-kan dhab ahaan ma haystaa booking mustaqbal ah HADDA?
        List<Map<String, Object>> futureBookings = jdbcTemplate.queryForList(
                "SELECT id, event_start, NOW() as db_now FROM event_bookings WHERE field_id = ? AND event_start >= NOW()",
                fieldId);
        result.put("future_bookings_for_field", futureBookings);

        return result;
    }
}
