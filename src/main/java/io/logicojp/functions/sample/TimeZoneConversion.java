package io.logicojp.functions.sample;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class TimeZoneConversion {

    static Map<String, String> toShortZoneIdMap;
    static Map<String, String> toLongZoneIdMap;

    public TimeZoneConversion() {
        toLongZoneIdMap = getLongZoneIdMap();
        toShortZoneIdMap = getShortZoneIdMap();
    }

    private HashMap<String, String> getShortZoneIdMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            map.put(ZoneId.of(zoneId).getDisplayName(TextStyle.FULL, Locale.ENGLISH), zoneId);
        }
        return map;
    }

    private HashMap<String, String> getLongZoneIdMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            map.put(zoneId, ZoneId.of(zoneId).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        return map;
    }

    HttpResponseMessage createResponse(HttpRequestMessage<Optional<String>>_req, TZ _tz, HttpStatus _status) {
        return _req.createResponseBuilder(_status)
            .header("Content-Type", "application/json")
            .body(_tz)
            .build();
    }

    @FunctionName("timezone-conversion")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        // Create instances
        context.getLogger().info("Java HTTP trigger processed a request.");
        TZ tz = new TZ();

        // 両方設定されていれば、エラーにして返す
        String shortZone = request.getQueryParameters().get("short");
        String longZone = request.getQueryParameters().get("long");
        if( Optional.ofNullable(shortZone).isEmpty() && Optional.ofNullable(longZone).isEmpty() ) {
            // 両方存在しない("")ならば、403
            tz.setShortId("");
            tz.setDisplayName("");
            tz.setDescription("No query parameter (Short Id or Timezone display name) is specified.");
            context.getLogger().info(tz.toString());
            return createResponse(request, tz, HttpStatus.FORBIDDEN);
        }
        if(Optional.ofNullable(shortZone).isPresent() && Optional.ofNullable(longZone).isPresent()) {
            //両方とも設定されている場合も、403
            tz.setShortId(shortZone);
            tz.setDisplayName(longZone);
            tz.setDescription("Both query parameters (Short Id and Timezone display name) are specified.");
            context.getLogger().info(tz.toString());
            return createResponse(request, tz, HttpStatus.FORBIDDEN);
        }

        // 以下はどちらか一方に設定がある場合
        // ianaが指定されている場合
        if(Optional.ofNullable(shortZone).isPresent()) {
            if(shortZone.length()<1) {
                // Query Parameterがあるけど中身がない
                tz.setShortId("");
                tz.setDisplayName("");
                tz.setDescription("No query parameter for Short Id is specified.");
                context.getLogger().info(tz.toString());
                return createResponse(request, tz, HttpStatus.NOT_FOUND);
            }
            // Query Parameterの中身がある
            // IANA -> Win
            if(toLongZoneIdMap.isEmpty()) {
                toLongZoneIdMap = getShortZoneIdMap();
            }
            tz.setShortId(shortZone);
            longZone = toLongZoneIdMap.get(shortZone);
            if(Optional.ofNullable(longZone).isEmpty()) {
                tz.setDisplayName("");
                tz.setDescription(String.format("Timezone display name mapped to Short Id %s is not found.", shortZone));
                context.getLogger().info(tz.toString());
                return createResponse(request, tz, HttpStatus.NOT_FOUND);
            }

            tz.setDisplayName(longZone);
            tz.setDescription(String.format("Timezone display name mapped to Short Id %s is %s.", shortZone, longZone));
        }
        // winが指定されている場合
        else {
            if(longZone.length()<1) {
                // Query Parameterがあるけど中身がない
                tz.setShortId("");
                tz.setDisplayName("");
                tz.setDescription("No query parameter for Timezone display name is specified.");
                context.getLogger().info(tz.toString());
                return createResponse(request, tz, HttpStatus.NOT_FOUND);
            }
            // Query Parameterの中身がある
            // Windows -> IANA
            if(toShortZoneIdMap.isEmpty()) {
                toShortZoneIdMap = getLongZoneIdMap();
            }

            tz.setDisplayName(longZone);
            shortZone = toShortZoneIdMap.get(longZone);
            if(Optional.ofNullable(shortZone).isEmpty()) {
                tz.setShortId("");
                tz.setDescription(String.format("Short timezone name mapped to Timezone display name %s is not found.", longZone));
                context.getLogger().info(tz.toString());
                return createResponse(request, tz, HttpStatus.NOT_FOUND);
            }

            tz.setShortId(shortZone);
            tz.setDescription(String.format("Short timezone name mapped to Timezone display name %s is %s.", longZone, shortZone));
        }

        // ここまで来るときは、何らかの結果が出ている
        context.getLogger().info(tz.toString());
        return createResponse(request, tz, HttpStatus.OK);
    }
}
