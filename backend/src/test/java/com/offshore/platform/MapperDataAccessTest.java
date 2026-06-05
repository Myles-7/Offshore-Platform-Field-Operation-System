package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.SyncLog;
import com.offshore.platform.entity.SyncTask;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql")
@Transactional
class MapperDataAccessTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderRecordMapper workOrderRecordMapper;

    @Autowired
    private FileStorageMapper fileStorageMapper;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private SyncTaskMapper syncTaskMapper;

    @Autowired
    private SyncLogMapper syncLogMapper;

    @Autowired
    private SyncConflictMapper syncConflictMapper;

    @Test
    void sysUserMapperSupportsBasicCrudAndSoftDelete() {
        SysUser user = newSysUser("mapper_user", "Mapper用户", "13900000001");

        assertThat(sysUserMapper.insert(user)).isEqualTo(1);
        assertThat(user.getId()).isNotNull();

        SysUser loaded = sysUserMapper.selectById(user.getId());
        assertThat(loaded.getUsername()).isEqualTo("mapper_user");

        loaded.setRealName("Mapper用户-更新");
        assertThat(sysUserMapper.updateById(loaded)).isEqualTo(1);
        assertThat(sysUserMapper.selectById(user.getId()).getRealName()).isEqualTo("Mapper用户-更新");

        assertThat(sysUserMapper.softDeleteById(user.getId())).isEqualTo(1);
        assertThat(sysUserMapper.selectById(user.getId())).isNull();
        assertThat(sysUserMapper.selectAll()).noneMatch(item -> user.getId().equals(item.getId()));
    }

    @Test
    void workOrderMapperSupportsBasicCrudAndSoftDelete() {
        SysUser user = insertUser("wo_user", "工单用户", "13900000002");
        ProjectInfo project = insertProject(user.getId(), "PRJ-MAPPER-WO");
        WorkOrder order = newWorkOrder(project.getId(), user.getId(), "WO-MAPPER-001");

        assertThat(workOrderMapper.insert(order)).isEqualTo(1);
        assertThat(order.getId()).isNotNull();

        WorkOrder loaded = workOrderMapper.selectById(order.getId());
        assertThat(loaded.getWorkOrderNo()).isEqualTo("WO-MAPPER-001");

        loaded.setStatus("IN_PROGRESS");
        assertThat(workOrderMapper.updateById(loaded)).isEqualTo(1);
        assertThat(workOrderMapper.selectById(order.getId()).getStatus()).isEqualTo("IN_PROGRESS");

        assertThat(workOrderMapper.softDeleteById(order.getId())).isEqualTo(1);
        assertThat(workOrderMapper.selectById(order.getId())).isNull();
        assertThat(workOrderMapper.selectAll()).noneMatch(item -> order.getId().equals(item.getId()));
    }

    @Test
    void workOrderRecordMapperSupportsBasicCrudAndSoftDelete() {
        SysUser user = insertUser("record_user", "施工记录用户", "13900000003");
        ProjectInfo project = insertProject(user.getId(), "PRJ-MAPPER-REC");
        WorkOrder order = insertWorkOrder(project.getId(), user.getId(), "WO-MAPPER-REC");
        WorkOrderRecord record = newWorkOrderRecord(order.getId(), project.getId(), user.getId(), "REC-MAPPER-001");

        assertThat(workOrderRecordMapper.insert(record)).isEqualTo(1);
        assertThat(record.getId()).isNotNull();

        WorkOrderRecord loaded = workOrderRecordMapper.selectById(record.getId());
        assertThat(loaded.getRecordNo()).isEqualTo("REC-MAPPER-001");

        loaded.setRecordStatus("CONFIRMED");
        assertThat(workOrderRecordMapper.updateById(loaded)).isEqualTo(1);
        assertThat(workOrderRecordMapper.selectById(record.getId()).getRecordStatus()).isEqualTo("CONFIRMED");

        assertThat(workOrderRecordMapper.softDeleteById(record.getId())).isEqualTo(1);
        assertThat(workOrderRecordMapper.selectById(record.getId())).isNull();
    }

    @Test
    void fileStorageMapperSupportsBasicCrudAndSoftDelete() {
        SysUser user = insertUser("file_user", "文件用户", "13900000004");
        ProjectInfo project = insertProject(user.getId(), "PRJ-MAPPER-FILE");
        WorkOrder order = insertWorkOrder(project.getId(), user.getId(), "WO-MAPPER-FILE");
        WorkOrderRecord record = insertWorkOrderRecord(order.getId(), project.getId(), user.getId(), "REC-MAPPER-FILE");
        FileStorage file = newFileStorage(user.getId(), order.getId(), record.getId(), "FILE-MAPPER-001");

        assertThat(fileStorageMapper.insert(file)).isEqualTo(1);
        assertThat(file.getId()).isNotNull();

        FileStorage loaded = fileStorageMapper.selectById(file.getId());
        assertThat(loaded.getFileId()).isEqualTo("FILE-MAPPER-001");

        loaded.setUploadStatus("FAILED");
        assertThat(fileStorageMapper.updateById(loaded)).isEqualTo(1);
        assertThat(fileStorageMapper.selectById(file.getId()).getUploadStatus()).isEqualTo("FAILED");

        assertThat(fileStorageMapper.softDeleteById(file.getId())).isEqualTo(1);
        assertThat(fileStorageMapper.selectById(file.getId())).isNull();
    }

    @Test
    void syncConflictMapperSupportsBasicCrudAndSoftDelete() {
        SysUser user = insertUser("sync_user", "同步用户", "13900000005");
        ProjectInfo project = insertProject(user.getId(), "PRJ-MAPPER-SYNC");
        WorkOrder order = insertWorkOrder(project.getId(), user.getId(), "WO-MAPPER-SYNC");
        insertDevice(user.getId(), "android-mapper-001");
        SyncTask task = insertSyncTask(user.getId(), "android-mapper-001", "BATCH-MAPPER-001");
        SyncLog log = insertSyncLog(task.getId(), user.getId(), order.getId(), "android-mapper-001");
        SyncConflict conflict = newSyncConflict(task.getId(), log.getId(), user.getId(), order.getId(), "WO-MAPPER-SYNC");

        assertThat(syncConflictMapper.insert(conflict)).isEqualTo(1);
        assertThat(conflict.getId()).isNotNull();

        SyncConflict loaded = syncConflictMapper.selectById(conflict.getId());
        assertThat(loaded.getConflictNo()).isEqualTo("CONF-MAPPER-001");

        loaded.setResolveStatus("RESOLVED");
        assertThat(syncConflictMapper.updateById(loaded)).isEqualTo(1);
        assertThat(syncConflictMapper.selectById(conflict.getId()).getResolveStatus()).isEqualTo("RESOLVED");

        assertThat(syncConflictMapper.softDeleteById(conflict.getId())).isEqualTo(1);
        assertThat(syncConflictMapper.selectById(conflict.getId())).isNull();
    }

    private SysUser insertUser(String username, String realName, String phone) {
        SysUser user = newSysUser(username, realName, phone);
        assertThat(sysUserMapper.insert(user)).isEqualTo(1);
        return user;
    }

    private SysUser newSysUser(String username, String realName, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}test");
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(1);
        user.setMobileEnabled(1);
        user.setDeletedFlag(0);
        return user;
    }

    private ProjectInfo insertProject(Long managerId, String projectCode) {
        ProjectInfo project = new ProjectInfo();
        project.setProjectCode(projectCode);
        project.setProjectName(projectCode + "项目");
        project.setProjectManagerId(managerId);
        project.setProjectStatus("ACTIVE");
        project.setDeletedFlag(0);
        assertThat(projectInfoMapper.insert(project)).isEqualTo(1);
        return project;
    }

    private WorkOrder insertWorkOrder(Long projectId, Long userId, String workOrderNo) {
        WorkOrder order = newWorkOrder(projectId, userId, workOrderNo);
        assertThat(workOrderMapper.insert(order)).isEqualTo(1);
        return order;
    }

    private WorkOrder newWorkOrder(Long projectId, Long userId, String workOrderNo) {
        WorkOrder order = new WorkOrder();
        order.setWorkOrderNo(workOrderNo);
        order.setProjectId(projectId);
        order.setWorkTitle(workOrderNo + "标题");
        order.setWorkLocation("A平台甲板区");
        order.setWorkContent("防腐层修复施工");
        order.setRequiredMaterialDesc("防腐涂料");
        order.setLeaderId(userId);
        order.setMaintainerId(userId);
        order.setPlannedStartTime(LocalDateTime.now());
        order.setPlannedEndTime(LocalDateTime.now().plusHours(8));
        order.setStatus("ASSIGNED");
        order.setPriority("NORMAL");
        order.setLocalId("local-" + workOrderNo);
        order.setVersion(1);
        order.setSyncStatus("SYNCED");
        order.setDeviceId("android-mapper-001");
        order.setOperatorId(userId);
        order.setDeletedFlag(0);
        return order;
    }

    private WorkOrderRecord insertWorkOrderRecord(Long workOrderId, Long projectId, Long userId, String recordNo) {
        WorkOrderRecord record = newWorkOrderRecord(workOrderId, projectId, userId, recordNo);
        assertThat(workOrderRecordMapper.insert(record)).isEqualTo(1);
        return record;
    }

    private WorkOrderRecord newWorkOrderRecord(Long workOrderId, Long projectId, Long userId, String recordNo) {
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(workOrderId);
        record.setProjectId(projectId);
        record.setRecordNo(recordNo);
        record.setRecordType("CONSTRUCTION");
        record.setConstructionTime(LocalDateTime.now());
        record.setConstructionUserId(userId);
        record.setConstructionUserName("施工人员");
        record.setConstructionDesc("完成现场施工记录");
        record.setSiteCondition("现场正常");
        record.setAbnormalFlag(0);
        record.setRecordStatus("SUBMITTED");
        record.setLocalId("local-" + recordNo);
        record.setVersion(1);
        record.setSyncStatus("PENDING");
        record.setDeviceId("android-mapper-001");
        record.setOperatorId(userId);
        record.setDeletedFlag(0);
        return record;
    }

    private FileStorage newFileStorage(Long userId, Long workOrderId, Long recordId, String fileId) {
        FileStorage file = new FileStorage();
        file.setFileId(fileId);
        file.setOriginalName("mapper-photo.jpg");
        file.setStoredName(fileId + ".jpg");
        file.setFileType("PHOTO");
        file.setMimeType("image/jpeg");
        file.setFileSize(2048L);
        file.setFilePath("/test/mapper-photo.jpg");
        file.setUploadUserId(userId);
        file.setUploadStatus("UPLOADED");
        file.setWorkOrderId(workOrderId);
        file.setRecordId(recordId);
        file.setPreviewEnabled(1);
        file.setDownloadEnabled(1);
        file.setCacheEnabled(1);
        file.setLocalId("local-" + fileId);
        file.setVersion(1);
        file.setSyncStatus("SYNCED");
        file.setDeviceId("android-mapper-001");
        file.setOperatorId(userId);
        file.setDeletedFlag(0);
        return file;
    }

    private void insertDevice(Long userId, String deviceId) {
        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(deviceId);
        device.setUserId(userId);
        device.setDeviceName("Mapper Android");
        device.setAppVersion("1.0.0");
        device.setOnlineStatus("ONLINE");
        device.setDeviceStatus("ACTIVE");
        device.setSyncEnabled(1);
        device.setDeletedFlag(0);
        assertThat(deviceInfoMapper.insert(device)).isEqualTo(1);
    }

    private SyncTask insertSyncTask(Long userId, String deviceId, String batchId) {
        SyncTask task = new SyncTask();
        task.setSyncTaskNo("SYNC-MAPPER-001");
        task.setBatchId(batchId);
        task.setDeviceId(deviceId);
        task.setOperatorId(userId);
        task.setSyncDirection("PUSH");
        task.setSyncType("INCREMENTAL");
        task.setTaskStatus("CONFLICT");
        task.setTotalCount(1);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setConflictCount(1);
        task.setIdempotencyKey("idem-" + batchId);
        task.setDeletedFlag(0);
        assertThat(syncTaskMapper.insert(task)).isEqualTo(1);
        return task;
    }

    private SyncLog insertSyncLog(Long taskId, Long userId, Long workOrderId, String deviceId) {
        SyncLog log = new SyncLog();
        log.setSyncTaskId(taskId);
        log.setBatchId("BATCH-MAPPER-001");
        log.setDeviceId(deviceId);
        log.setOperatorId(userId);
        log.setModuleType("WORK_ORDER");
        log.setEntityType("work_order");
        log.setActionType("UPDATE");
        log.setLocalId("local-sync-conflict");
        log.setServerId(workOrderId);
        log.setEntityId(workOrderId);
        log.setWorkOrderId(workOrderId);
        log.setBusinessNo("WO-MAPPER-SYNC");
        log.setClientVersion(1);
        log.setServerVersion(2);
        log.setSyncStatus("CONFLICT");
        log.setDeletedFlag(0);
        assertThat(syncLogMapper.insert(log)).isEqualTo(1);
        return log;
    }

    private SyncConflict newSyncConflict(Long taskId, Long logId, Long userId, Long workOrderId, String businessNo) {
        SyncConflict conflict = new SyncConflict();
        conflict.setConflictNo("CONF-MAPPER-001");
        conflict.setSyncTaskId(taskId);
        conflict.setSyncLogId(logId);
        conflict.setDeviceId("android-mapper-001");
        conflict.setOperatorId(userId);
        conflict.setModuleType("WORK_ORDER");
        conflict.setEntityType("work_order");
        conflict.setEntityId(workOrderId);
        conflict.setLocalId("local-sync-conflict");
        conflict.setServerId(workOrderId);
        conflict.setWorkOrderId(workOrderId);
        conflict.setBusinessNo(businessNo);
        conflict.setBaseVersion(1);
        conflict.setClientVersion(1);
        conflict.setServerVersion(2);
        conflict.setConflictType("VERSION_CONFLICT");
        conflict.setConflictFields("[\"status\"]");
        conflict.setOldPayload("{}");
        conflict.setClientPayload("{}");
        conflict.setServerPayload("{}");
        conflict.setResolveStatus("PENDING_REVIEW");
        conflict.setDefaultStrategy("LAST_WRITE_WINS");
        conflict.setDeletedFlag(0);
        return conflict;
    }
}
