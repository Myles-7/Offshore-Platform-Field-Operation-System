package com.offshore.platform.service;

import com.offshore.platform.dto.sync.ConflictResolveRequest;
import com.offshore.platform.dto.sync.DeviceRegisterRequest;
import com.offshore.platform.dto.sync.SyncAckRequest;
import com.offshore.platform.dto.sync.SyncPullRequest;
import com.offshore.platform.dto.sync.SyncPushRequest;
import com.offshore.platform.vo.sync.DeviceVO;
import com.offshore.platform.vo.sync.SyncConflictVO;
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

    SyncConflictVO conflict(Long id);

    SyncConflictVO resolveConflict(Long id, ConflictResolveRequest request);
}
