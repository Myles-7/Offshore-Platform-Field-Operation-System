package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.sync.ConflictResolveRequest;
import com.offshore.platform.dto.sync.DeviceRegisterRequest;
import com.offshore.platform.dto.sync.SyncConflictBatchResolveRequest;
import com.offshore.platform.dto.sync.SyncConflictQueryRequest;
import com.offshore.platform.dto.sync.SyncAckRequest;
import com.offshore.platform.dto.sync.SyncPullRequest;
import com.offshore.platform.dto.sync.SyncPushRequest;
import com.offshore.platform.service.SyncService;
import com.offshore.platform.vo.sync.DeviceVO;
import com.offshore.platform.vo.sync.SyncConflictVO;
import com.offshore.platform.vo.sync.SyncConflictCompareVO;
import com.offshore.platform.vo.sync.SyncConflictDetailVO;
import com.offshore.platform.vo.sync.SyncPullVO;
import com.offshore.platform.vo.sync.SyncPushResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "sync", description = "Offline sync")
@RestController
public class SyncController {
    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/api/sync/device/register")
    public ApiResponse<DeviceVO> register(@Valid @RequestBody DeviceRegisterRequest request) {
        return ApiResponse.success(syncService.registerDevice(request));
    }

    @PostMapping("/api/sync/device/heartbeat")
    public ApiResponse<DeviceVO> heartbeat(@Valid @RequestBody DeviceRegisterRequest request) {
        return ApiResponse.success(syncService.heartbeat(request));
    }

    @PostMapping("/api/sync/pull")
    public ApiResponse<SyncPullVO> pull(@RequestBody SyncPullRequest request) {
        return ApiResponse.success(syncService.pull(request));
    }

    @PostMapping("/api/sync/push")
    public ApiResponse<SyncPushResultVO> push(@Valid @RequestBody SyncPushRequest request) {
        return ApiResponse.success(syncService.push(request));
    }

    @PostMapping("/api/sync/ack")
    public ApiResponse<Void> ack(@RequestBody SyncAckRequest request) {
        syncService.ack(request);
        return ApiResponse.success();
    }

    @GetMapping("/api/sync/tasks")
    public ApiResponse<List<Object>> tasks() {
        return ApiResponse.success(syncService.myTasks());
    }

    @GetMapping("/api/sync/logs")
    public ApiResponse<List<Object>> logs() {
        return ApiResponse.success(syncService.myLogs());
    }

    @GetMapping("/api/admin/sync/conflicts")
    public ApiResponse<List<SyncConflictVO>> conflicts(SyncConflictQueryRequest request) {
        return ApiResponse.success(syncService.conflicts(request));
    }

    @GetMapping("/api/admin/sync/conflicts/{id}")
    public ApiResponse<SyncConflictDetailVO> conflict(@PathVariable Long id) {
        return ApiResponse.success(syncService.conflictDetail(id));
    }

    @PostMapping("/api/admin/sync/conflicts/{id}/resolve")
    @OperationLog(module = "SYNC", operation = "RESOLVE_SYNC_CONFLICT", businessType = "SYNC_CONFLICT")
    public ApiResponse<SyncConflictVO> resolve(@PathVariable Long id, @Valid @RequestBody ConflictResolveRequest request) {
        return ApiResponse.success(syncService.resolveConflict(id, request));
    }

    @PostMapping("/api/admin/sync/conflicts/batch-resolve")
    @OperationLog(module = "SYNC", operation = "BATCH_RESOLVE_SYNC_CONFLICT", businessType = "SYNC_CONFLICT")
    public ApiResponse<List<SyncConflictVO>> batchResolve(@Valid @RequestBody SyncConflictBatchResolveRequest request) {
        return ApiResponse.success(syncService.batchResolveConflicts(request));
    }

    @GetMapping("/api/admin/sync/conflicts/{id}/compare")
    public ApiResponse<SyncConflictCompareVO> compare(@PathVariable Long id) {
        return ApiResponse.success(syncService.compareConflict(id));
    }

    @GetMapping("/api/admin/work-orders/{workOrderId}/conflicts")
    public ApiResponse<List<SyncConflictVO>> workOrderConflicts(@PathVariable Long workOrderId) {
        return ApiResponse.success(syncService.workOrderConflicts(workOrderId));
    }
}
