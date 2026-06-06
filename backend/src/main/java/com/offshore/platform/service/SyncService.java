package com.offshore.platform.service;

import com.offshore.platform.dto.sync.ConflictResolveRequest;
import com.offshore.platform.dto.sync.DeviceRegisterRequest;
import com.offshore.platform.dto.sync.SyncConflictBatchResolveRequest;
import com.offshore.platform.dto.sync.SyncConflictQueryRequest;
import com.offshore.platform.dto.sync.SyncAckRequest;
import com.offshore.platform.dto.sync.SyncPullRequest;
import com.offshore.platform.dto.sync.SyncPushRequest;
import com.offshore.platform.vo.sync.DeviceVO;
import com.offshore.platform.vo.sync.SyncConflictVO;
import com.offshore.platform.vo.sync.SyncConflictCompareVO;
import com.offshore.platform.vo.sync.SyncConflictDetailVO;
import com.offshore.platform.vo.sync.SyncPullVO;
import com.offshore.platform.vo.sync.SyncPushResultVO;
import java.util.List;

public interface SyncService {
    DeviceVO registerDevice(DeviceRegisterRequest request);

    DeviceVO heartbeat(DeviceRegisterRequest request);

    SyncPullVO pull(SyncPullRequest request);

    SyncPushResultVO push(SyncPushRequest request);

    void ack(SyncAckRequest request);

    List<Object> myTasks();

    List<Object> myLogs();

    List<SyncConflictVO> conflicts();
    List<SyncConflictVO> conflicts(SyncConflictQueryRequest request);

    SyncConflictVO conflict(Long id);
    SyncConflictDetailVO conflictDetail(Long id);

    SyncConflictVO resolveConflict(Long id, ConflictResolveRequest request);

    List<SyncConflictVO> batchResolveConflicts(SyncConflictBatchResolveRequest request);

    SyncConflictCompareVO compareConflict(Long id);

    List<SyncConflictVO> workOrderConflicts(Long workOrderId);
}
