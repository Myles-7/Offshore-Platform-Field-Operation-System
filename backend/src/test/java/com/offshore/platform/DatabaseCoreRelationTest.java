package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql")
@Transactional
class DatabaseCoreRelationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long managerId;
    private Long maintainerId;
    private Long acceptorId;
    private Long projectId;
    private Long materialId;
    private Long qrcodeId;

    @BeforeEach
    void setUpBaseData() {
        managerId = insertUser("pm_test", "项目经理", "PM001");
        maintainerId = insertUser("worker_test", "维修工", "WK001");
        acceptorId = insertUser("acceptor_test", "验收员", "AC001");
        projectId = insertProject("PRJ-TEST", "测试海上平台项目", managerId);
        jdbcTemplate.update("update sys_user set primary_project_id = ? where id in (?, ?, ?)",
                projectId, managerId, maintainerId, acceptorId);
        materialId = insertMaterial("MAT-ANTICORROSION", "防腐涂料");
        qrcodeId = insertMaterialQrcode(materialId, "MAT-ANTICORROSION", "QR-MAT-001");
    }

    @Test
    void createsWorkOrderAndAssignsItToMaintainer() {
        Long workOrderId = insertWorkOrder("WO-ASSIGN-001", "ASSIGNED");
        Long assignmentId = insertAssignment(workOrderId, managerId, maintainerId);

        Map<String, Object> assignedOrder = jdbcTemplate.queryForMap("""
                select wo.work_order_no, wo.status, wo.maintainer_id, wa.assignee_id, wa.assignment_status
                from work_order wo
                join work_order_assignment wa on wa.work_order_id = wo.id
                where wa.id = ?
                """, assignmentId);

        assertThat(assignedOrder.get("WORK_ORDER_NO")).isEqualTo("WO-ASSIGN-001");
        assertThat(assignedOrder.get("STATUS")).isEqualTo("ASSIGNED");
        assertThat(((Number) assignedOrder.get("MAINTAINER_ID")).longValue()).isEqualTo(maintainerId);
        assertThat(((Number) assignedOrder.get("ASSIGNEE_ID")).longValue()).isEqualTo(maintainerId);
        assertThat(assignedOrder.get("ASSIGNMENT_STATUS")).isEqualTo("ASSIGNED");
    }

    @Test
    void recordsWorkOrderStatusFlow() {
        Long workOrderId = insertWorkOrder("WO-FLOW-001", "PENDING_ASSIGN");

        insertStatusLog(workOrderId, null, "PENDING_ASSIGN", "CREATE");
        insertStatusLog(workOrderId, "PENDING_ASSIGN", "ASSIGNED", "ASSIGN");
        insertStatusLog(workOrderId, "ASSIGNED", "IN_PROGRESS", "START");
        insertStatusLog(workOrderId, "IN_PROGRESS", "PENDING_ACCEPTANCE", "SUBMIT_ACCEPTANCE");
        jdbcTemplate.update("update work_order set status = ?, version = version + 1 where id = ?",
                "PENDING_ACCEPTANCE", workOrderId);

        List<String> flow = jdbcTemplate.queryForList("""
                select to_status
                from work_order_status_log
                where work_order_id = ?
                order by id
                """, String.class, workOrderId);
        String finalStatus = jdbcTemplate.queryForObject(
                "select status from work_order where id = ?", String.class, workOrderId);

        assertThat(flow).containsExactly("PENDING_ASSIGN", "ASSIGNED", "IN_PROGRESS", "PENDING_ACCEPTANCE");
        assertThat(finalStatus).isEqualTo("PENDING_ACCEPTANCE");
    }

    @Test
    void bindsConstructionRecordToWorkOrderWithOfflineSyncFields() {
        Long workOrderId = insertWorkOrder("WO-REC-001", "IN_PROGRESS");
        Long recordId = insertRecord(workOrderId, "REC-001");

        Map<String, Object> record = jdbcTemplate.queryForMap("""
                select r.record_no, r.work_order_id, r.project_id, r.local_id, r.version, r.sync_status, r.device_id
                from work_order_record r
                join work_order wo on wo.id = r.work_order_id
                where r.id = ?
                """, recordId);

        assertThat(record.get("RECORD_NO")).isEqualTo("REC-001");
        assertThat(((Number) record.get("WORK_ORDER_ID")).longValue()).isEqualTo(workOrderId);
        assertThat(((Number) record.get("PROJECT_ID")).longValue()).isEqualTo(projectId);
        assertThat(record.get("LOCAL_ID")).isEqualTo("local-REC-001");
        assertThat(record.get("SYNC_STATUS")).isEqualTo("PENDING");
        assertThat(record.get("DEVICE_ID")).isEqualTo("android-001");
    }

    @Test
    void bindsPhotoAttachmentToWorkOrderAndRecord() {
        Long workOrderId = insertWorkOrder("WO-PHOTO-001", "IN_PROGRESS");
        Long recordId = insertRecord(workOrderId, "REC-PHOTO-001");
        insertFile("FILE-PHOTO-001", "PHOTO", "photo.jpg", workOrderId, recordId);
        Long attachmentId = insertAttachment(workOrderId, recordId, "FILE-PHOTO-001", "PHOTO");

        Map<String, Object> attachment = jdbcTemplate.queryForMap("""
                select a.attachment_type, a.watermark_flag, a.watermark_work_order_no,
                       f.file_type, f.preview_enabled, f.cache_enabled
                from work_order_attachment a
                join file_storage f on f.file_id = a.file_id
                where a.id = ?
                """, attachmentId);

        assertThat(attachment.get("ATTACHMENT_TYPE")).isEqualTo("PHOTO");
        assertThat(((Number) attachment.get("WATERMARK_FLAG")).intValue()).isEqualTo(1);
        assertThat(attachment.get("WATERMARK_WORK_ORDER_NO")).isEqualTo("WO-PHOTO-001");
        assertThat(attachment.get("FILE_TYPE")).isEqualTo("PHOTO");
        assertThat(((Number) attachment.get("PREVIEW_ENABLED")).intValue()).isEqualTo(1);
        assertThat(((Number) attachment.get("CACHE_ENABLED")).intValue()).isEqualTo(1);
    }

    @Test
    void bindsSignatureAndLockedPdfToAcceptance() {
        Long workOrderId = insertWorkOrder("WO-PDF-001", "PENDING_ACCEPTANCE");
        Long recordId = insertRecord(workOrderId, "REC-PDF-001");
        Long acceptanceId = insertAcceptance(workOrderId, "ACC-001");
        insertFile("FILE-SIGN-001", "SIGNATURE", "sign.png", workOrderId, recordId);
        insertFile("FILE-PDF-001", "PDF", "acceptance.pdf", workOrderId, recordId);

        Long signatureId = insertSignature(workOrderId, acceptanceId, "FILE-SIGN-001");
        Long pdfId = insertPdf(workOrderId, acceptanceId, "FILE-PDF-001");
        jdbcTemplate.update("""
                update work_order_acceptance
                set signature_count = 1, pdf_generated_flag = 1, locked_flag = 1, acceptance_status = 'LOCKED'
                where id = ?
                """, acceptanceId);

        Map<String, Object> result = jdbcTemplate.queryForMap("""
                select a.locked_flag, a.pdf_generated_flag, s.id as signature_id, p.id as pdf_id,
                       p.locked_flag as pdf_locked, p.download_enabled
                from work_order_acceptance a
                join work_order_signature s on s.acceptance_id = a.id
                join work_order_pdf p on p.acceptance_id = a.id
                where a.id = ?
                """, acceptanceId);

        assertThat(((Number) result.get("SIGNATURE_ID")).longValue()).isEqualTo(signatureId);
        assertThat(((Number) result.get("PDF_ID")).longValue()).isEqualTo(pdfId);
        assertThat(((Number) result.get("LOCKED_FLAG")).intValue()).isEqualTo(1);
        assertThat(((Number) result.get("PDF_GENERATED_FLAG")).intValue()).isEqualTo(1);
        assertThat(((Number) result.get("PDF_LOCKED")).intValue()).isEqualTo(1);
        assertThat(((Number) result.get("DOWNLOAD_ENABLED")).intValue()).isEqualTo(1);
    }

    @Test
    void recordsMaterialOutboundAndBindsUsageToWorkOrder() {
        Long workOrderId = insertWorkOrder("WO-MAT-001", "IN_PROGRESS");
        jdbcTemplate.update("""
                insert into material_inventory(material_id, material_code, current_qty, available_qty)
                values (?, 'MAT-ANTICORROSION', 100.000, 100.000)
                """, materialId);

        Long inoutId = insertMaterialOutRecord(workOrderId, "WO-MAT-001", new BigDecimal("12.500"));
        Long usageId = insertMaterialUsage(workOrderId, "WO-MAT-001", inoutId, new BigDecimal("12.500"));
        jdbcTemplate.update("""
                update material_inventory
                set current_qty = current_qty - 12.500, available_qty = available_qty - 12.500
                where material_id = ?
                """, materialId);

        Map<String, Object> usage = jdbcTemplate.queryForMap("""
                select u.usage_no, u.used_qty, u.cost_amount, r.inout_type, i.current_qty
                from work_order_material_usage u
                join material_inout_record r on r.id = u.inout_record_id
                join material_inventory i on i.material_id = u.material_id
                where u.id = ?
                """, usageId);

        assertThat(usage.get("USAGE_NO")).isEqualTo("USE-WO-MAT-001");
        assertThat((BigDecimal) usage.get("USED_QTY")).isEqualByComparingTo("12.500");
        assertThat((BigDecimal) usage.get("COST_AMOUNT")).isEqualByComparingTo("625.0000");
        assertThat(usage.get("INOUT_TYPE")).isEqualTo("OUT");
        assertThat((BigDecimal) usage.get("CURRENT_QTY")).isEqualByComparingTo("87.500");
    }

    @Test
    void queriesExpiringEmployeeCertificates() {
        Long employeeId = insertEmployee(maintainerId, "EMP-WK001", "维修工");
        Long qualificationTypeId = insertQualificationType("Q-HEIGHT", "高处作业证", 30);
        jdbcTemplate.update("""
                insert into employee_certificate(employee_id, qualification_type_id, certificate_no, certificate_name,
                  valid_from, valid_to, valid_status, warning_level)
                values (?, ?, 'CERT-EXP-001', '高处作业证', ?, ?, 'VALID', 'WARNING')
                """, employeeId, qualificationTypeId, LocalDate.now().minusYears(1), LocalDate.now().plusDays(10));

        List<Map<String, Object>> expiringCertificates = jdbcTemplate.queryForList("""
                select e.real_name, c.certificate_no, c.valid_to, q.warning_days
                from employee_certificate c
                join employee_info e on e.id = c.employee_id
                join qualification_type q on q.id = c.qualification_type_id
                where c.deleted_flag = 0
                  and c.valid_status = 'VALID'
                  and c.valid_to <= ?
                """, LocalDate.now().plusDays(30));

        assertThat(expiringCertificates).hasSize(1);
        assertThat(expiringCertificates.get(0).get("CERTIFICATE_NO")).isEqualTo("CERT-EXP-001");
    }

    @Test
    void writesMobileSyncTaskAndLog() {
        Long workOrderId = insertWorkOrder("WO-SYNC-001", "IN_PROGRESS");
        insertDevice("android-001", maintainerId);
        Long taskId = insertSyncTask("SYNC-001", "BATCH-001", "PENDING");
        Long logId = insertSyncLog(taskId, workOrderId, "WORK_ORDER", "work_order", "UPDATE", "PENDING");

        Map<String, Object> sync = jdbcTemplate.queryForMap("""
                select t.sync_task_no, t.task_status, l.entity_type, l.local_id, l.server_id, l.sync_status
                from sync_task t
                join sync_log l on l.sync_task_id = t.id
                where l.id = ?
                """, logId);

        assertThat(sync.get("SYNC_TASK_NO")).isEqualTo("SYNC-001");
        assertThat(sync.get("TASK_STATUS")).isEqualTo("PENDING");
        assertThat(sync.get("ENTITY_TYPE")).isEqualTo("work_order");
        assertThat(sync.get("LOCAL_ID")).isEqualTo("local-sync-001");
        assertThat(((Number) sync.get("SERVER_ID")).longValue()).isEqualTo(workOrderId);
        assertThat(sync.get("SYNC_STATUS")).isEqualTo("PENDING");
    }

    @Test
    void createsSyncConflictRecordForVersionConflict() {
        Long workOrderId = insertWorkOrder("WO-CONFLICT-001", "IN_PROGRESS");
        insertDevice("android-001", maintainerId);
        Long taskId = insertSyncTask("SYNC-CONFLICT-001", "BATCH-CONFLICT-001", "CONFLICT");
        Long logId = insertSyncLog(taskId, workOrderId, "WORK_ORDER", "work_order", "UPDATE", "CONFLICT");
        Long conflictId = insertSyncConflict(taskId, logId, workOrderId);
        jdbcTemplate.update("update sync_log set conflict_id = ? where id = ?", conflictId, logId);

        Map<String, Object> conflict = jdbcTemplate.queryForMap("""
                select c.conflict_no, c.conflict_type, c.resolve_status, c.default_strategy,
                       c.client_version, c.server_version, l.sync_status
                from sync_conflict c
                join sync_log l on l.conflict_id = c.id
                where c.id = ?
                """, conflictId);

        assertThat(conflict.get("CONFLICT_NO")).isEqualTo("CONF-001");
        assertThat(conflict.get("CONFLICT_TYPE")).isEqualTo("VERSION_CONFLICT");
        assertThat(conflict.get("RESOLVE_STATUS")).isEqualTo("PENDING_REVIEW");
        assertThat(conflict.get("DEFAULT_STRATEGY")).isEqualTo("LAST_WRITE_WINS");
        assertThat(((Number) conflict.get("CLIENT_VERSION")).intValue()).isEqualTo(2);
        assertThat(((Number) conflict.get("SERVER_VERSION")).intValue()).isEqualTo(3);
        assertThat(conflict.get("SYNC_STATUS")).isEqualTo("CONFLICT");
    }

    @Test
    void bindsAiResultAndDefectBoxToConstructionPhoto() {
        Long workOrderId = insertWorkOrder("WO-AI-001", "PENDING_ACCEPTANCE");
        Long recordId = insertRecord(workOrderId, "REC-AI-001");
        insertFile("FILE-AI-PHOTO-001", "PHOTO", "defect-photo.jpg", workOrderId, recordId);
        Long attachmentId = insertAttachment(workOrderId, recordId, "FILE-AI-PHOTO-001", "PHOTO");
        Long modelId = insertAiModel();
        Long aiResultId = insertAiResult(workOrderId, recordId, attachmentId, modelId);
        jdbcTemplate.update("update work_order_attachment set ai_result_id = ?, ai_bind_status = 'BOUND' where id = ?",
                aiResultId, attachmentId);
        jdbcTemplate.update("""
                insert into ai_defect_box(ai_result_id, defect_type, confidence, x, y, width, height)
                values (?, 'RUST', 0.930000, 10.0000, 20.0000, 80.0000, 60.0000)
                """, aiResultId);

        Map<String, Object> ai = jdbcTemplate.queryForMap("""
                select ar.ai_result_no, ar.defect_type, ar.suspected_defect_flag, ar.review_status,
                       a.ai_bind_status, count(b.id) as box_count
                from ai_result ar
                join work_order_attachment a on a.ai_result_id = ar.id
                left join ai_defect_box b on b.ai_result_id = ar.id
                where ar.id = ?
                group by ar.ai_result_no, ar.defect_type, ar.suspected_defect_flag, ar.review_status, a.ai_bind_status
                """, aiResultId);

        assertThat(ai.get("AI_RESULT_NO")).isEqualTo("AI-001");
        assertThat(ai.get("DEFECT_TYPE")).isEqualTo("RUST");
        assertThat(((Number) ai.get("SUSPECTED_DEFECT_FLAG")).intValue()).isEqualTo(1);
        assertThat(ai.get("REVIEW_STATUS")).isEqualTo("PENDING_REVIEW");
        assertThat(ai.get("AI_BIND_STATUS")).isEqualTo("BOUND");
        assertThat(((Number) ai.get("BOX_COUNT")).intValue()).isEqualTo(1);
    }

    @Test
    void dashboardStatisticSqlReturnsExpectedValues() {
        Long inProgressOrder = insertWorkOrder("WO-DASH-001", "IN_PROGRESS");
        Long completedOrder = insertWorkOrder("WO-DASH-002", "COMPLETED");
        Long pendingOrder = insertWorkOrder("WO-DASH-003", "PENDING_ACCEPTANCE");
        jdbcTemplate.update("update work_order set actual_end_time = ? where id = ?",
                LocalDateTime.now().minusDays(1), completedOrder);
        insertRecord(inProgressOrder, "REC-DASH-001");
        insertRecord(completedOrder, "REC-DASH-002");
        Long outId = insertMaterialOutRecord(completedOrder, "WO-DASH-002", new BigDecimal("5.000"));
        insertMaterialUsage(completedOrder, "WO-DASH-002", outId, new BigDecimal("5.000"));

        insertDevice("android-001", maintainerId);
        Long taskId = insertSyncTask("SYNC-DASH-001", "BATCH-DASH-001", "CONFLICT");
        Long logId = insertSyncLog(taskId, pendingOrder, "WORK_ORDER", "work_order", "UPDATE", "CONFLICT");
        insertSyncConflict(taskId, logId, pendingOrder);

        Long recordId = insertRecord(pendingOrder, "REC-DASH-AI");
        insertFile("FILE-DASH-AI", "PHOTO", "dash-ai.jpg", pendingOrder, recordId);
        Long attachmentId = insertAttachment(pendingOrder, recordId, "FILE-DASH-AI", "PHOTO");
        Long modelId = insertAiModel();
        insertAiResult(pendingOrder, recordId, attachmentId, modelId);

        Integer inProgressCount = jdbcTemplate.queryForObject("""
                select count(*)
                from work_order
                where deleted_flag = 0 and status in ('ASSIGNED', 'IN_PROGRESS', 'PENDING_ACCEPTANCE', 'REJECTED')
                """, Integer.class);
        Integer todayAttendance = jdbcTemplate.queryForObject("""
                select count(distinct construction_user_id)
                from work_order_record
                where deleted_flag = 0 and cast(construction_time as date) = current_date
                """, Integer.class);
        BigDecimal completionRate = jdbcTemplate.queryForObject("""
                select round(cast(sum(case when status = 'COMPLETED' then 1 else 0 end) as decimal(10, 4)) / count(*), 4)
                from work_order
                where deleted_flag = 0
                """, BigDecimal.class);
        BigDecimal materialCost = jdbcTemplate.queryForObject("""
                select coalesce(sum(cost_amount), 0)
                from work_order_material_usage
                where deleted_flag = 0 and project_id = ?
                """, BigDecimal.class, projectId);
        Integer suspectedAiCount = jdbcTemplate.queryForObject("""
                select count(*)
                from ai_result
                where deleted_flag = 0 and suspected_defect_flag = 1
                """, Integer.class);
        Integer pendingConflictCount = jdbcTemplate.queryForObject("""
                select count(*)
                from sync_conflict
                where deleted_flag = 0 and resolve_status = 'PENDING_REVIEW'
                """, Integer.class);

        assertThat(inProgressCount).isEqualTo(2);
        assertThat(todayAttendance).isEqualTo(1);
        assertThat(completionRate).isEqualByComparingTo(new BigDecimal("0.3333"));
        assertThat(materialCost).isEqualByComparingTo("250.0000");
        assertThat(suspectedAiCount).isEqualTo(1);
        assertThat(pendingConflictCount).isEqualTo(1);
    }

    private Long insertUser(String username, String realName, String employeeNo) {
        return insertAndReturnId("""
                insert into sys_user(username, password_hash, real_name, phone, employee_no)
                values (?, '{noop}test', ?, ?, ?)
                """, username, realName, "138" + Math.abs(username.hashCode() % 100000000), employeeNo);
    }

    private Long insertProject(String code, String name, Long projectManagerId) {
        return insertAndReturnId("""
                insert into project_info(project_code, project_name, project_manager_id)
                values (?, ?, ?)
                """, code, name, projectManagerId);
    }

    private Long insertWorkOrder(String workOrderNo, String status) {
        return insertAndReturnId("""
                insert into work_order(work_order_no, project_id, work_title, work_location, work_content,
                  required_material_desc, leader_id, maintainer_id, planned_start_time, planned_end_time,
                  status, priority, local_id, server_id, version, sync_status, device_id, operator_id)
                values (?, ?, ?, 'A平台甲板区', '防腐层修复施工', '防腐涂料、刷具', ?, ?,
                  ?, ?, ?, 'HIGH', ?, null, 1, 'SYNCED', 'android-001', ?)
                """, workOrderNo, projectId, workOrderNo + "标题", managerId, maintainerId,
                LocalDateTime.now(), LocalDateTime.now().plusHours(8), status,
                "local-" + workOrderNo, maintainerId);
    }

    private Long insertAssignment(Long workOrderId, Long assignerId, Long assigneeId) {
        return insertAndReturnId("""
                insert into work_order_assignment(work_order_id, assigner_id, assignee_id, assignment_role,
                  assignment_status, local_id, server_id, version, sync_status, device_id, operator_id)
                values (?, ?, ?, 'MAINTAINER', 'ASSIGNED', ?, ?, 1, 'SYNCED', 'android-001', ?)
                """, workOrderId, assignerId, assigneeId, "local-assignment-" + workOrderId,
                workOrderId, assignerId);
    }

    private void insertStatusLog(Long workOrderId, String fromStatus, String toStatus, String operationType) {
        jdbcTemplate.update("""
                insert into work_order_status_log(work_order_id, from_status, to_status, operation_type,
                  operator_id, local_id, server_id, version, sync_status, device_id)
                values (?, ?, ?, ?, ?, ?, ?, 1, 'SYNCED', 'android-001')
                """, workOrderId, fromStatus, toStatus, operationType, managerId,
                "local-status-" + workOrderId + "-" + operationType, workOrderId);
    }

    private Long insertRecord(Long workOrderId, String recordNo) {
        return insertAndReturnId("""
                insert into work_order_record(work_order_id, project_id, record_no, construction_time,
                  construction_user_id, construction_user_name, construction_desc, site_condition,
                  abnormal_flag, record_status, local_id, server_id, version, sync_status, device_id, operator_id)
                values (?, ?, ?, ?, ?, '维修工', '完成表面处理和防腐涂刷', '现场风浪较小', 0,
                  'SUBMITTED', ?, ?, 1, 'PENDING', 'android-001', ?)
                """, workOrderId, projectId, recordNo, LocalDateTime.now(), maintainerId,
                "local-" + recordNo, workOrderId, maintainerId);
    }

    private void insertFile(String fileId, String fileType, String originalName, Long workOrderId, Long recordId) {
        jdbcTemplate.update("""
                insert into file_storage(file_id, original_name, stored_name, file_type, mime_type, file_size,
                  file_path, upload_user_id, upload_status, work_order_id, record_id, preview_enabled,
                  cache_enabled, local_id, server_id, version, sync_status, device_id, operator_id)
                values (?, ?, ?, ?, ?, 2048, ?, ?, 'UPLOADED', ?, ?, 1, 1, ?, ?, 1, 'SYNCED', 'android-001', ?)
                """, fileId, originalName, fileId + "-" + originalName, fileType, mimeType(fileType),
                "/test/" + originalName, maintainerId, workOrderId, recordId,
                "local-" + fileId, workOrderId, maintainerId);
    }

    private Long insertAttachment(Long workOrderId, Long recordId, String fileId, String type) {
        String workOrderNo = jdbcTemplate.queryForObject(
                "select work_order_no from work_order where id = ?", String.class, workOrderId);
        return insertAndReturnId("""
                insert into work_order_attachment(work_order_id, record_id, file_id, attachment_type,
                  business_scene, capture_time, capture_user_id, watermark_flag, watermark_text,
                  watermark_work_order_no, mobile_cache_status, upload_status, local_id, server_id,
                  version, sync_status, device_id, operator_id)
                values (?, ?, ?, ?, 'WORK_RECORD', ?, ?, 1, ?, ?, 'CACHED', 'UPLOADED',
                  ?, ?, 1, 'SYNCED', 'android-001', ?)
                """, workOrderId, recordId, fileId, type, LocalDateTime.now(), maintainerId,
                "时间/工单/人员水印", workOrderNo, "local-att-" + fileId, workOrderId, maintainerId);
    }

    private Long insertAcceptance(Long workOrderId, String acceptanceNo) {
        String workOrderNo = jdbcTemplate.queryForObject(
                "select work_order_no from work_order where id = ?", String.class, workOrderId);
        return insertAndReturnId("""
                insert into work_order_acceptance(acceptance_no, work_order_id, project_id, work_order_no,
                  project_name, construction_user_id, acceptance_user_id, acceptance_time, acceptance_status,
                  acceptance_result, record_summary, local_id, server_id, version, sync_status, device_id, operator_id)
                values (?, ?, ?, ?, '测试海上平台项目', ?, ?, ?, 'PASSED', 'PASS', '施工记录摘要',
                  ?, ?, 1, 'PENDING', 'android-001', ?)
                """, acceptanceNo, workOrderId, projectId, workOrderNo, maintainerId,
                acceptorId, LocalDateTime.now(), "local-" + acceptanceNo, workOrderId, acceptorId);
    }

    private Long insertSignature(Long workOrderId, Long acceptanceId, String fileId) {
        return insertAndReturnId("""
                insert into work_order_signature(signature_no, work_order_id, acceptance_id, file_id,
                  signature_role, signer_user_id, signer_name, signed_at, upload_status, local_id,
                  server_id, version, sync_status, device_id, operator_id)
                values ('SIGN-001', ?, ?, ?, 'ACCEPTANCE_USER', ?, '验收员', ?, 'UPLOADED',
                  'local-SIGN-001', ?, 1, 'SYNCED', 'android-001', ?)
                """, workOrderId, acceptanceId, fileId, acceptorId, LocalDateTime.now(), workOrderId, acceptorId);
    }

    private Long insertPdf(Long workOrderId, Long acceptanceId, String fileId) {
        String workOrderNo = jdbcTemplate.queryForObject(
                "select work_order_no from work_order where id = ?", String.class, workOrderId);
        return insertAndReturnId("""
                insert into work_order_pdf(pdf_no, work_order_id, acceptance_id, file_id, work_order_no,
                  project_name, construction_user_name, acceptance_user_name, acceptance_time,
                  signature_file_ids, record_summary, generated_by, upload_status, local_id, server_id,
                  version, sync_status, device_id, operator_id)
                values ('PDF-001', ?, ?, ?, ?, '测试海上平台项目', '维修工', '验收员', ?,
                  'FILE-SIGN-001', '施工记录摘要', ?, 'UPLOADED', 'local-PDF-001', ?, 1,
                  'SYNCED', 'android-001', ?)
                """, workOrderId, acceptanceId, fileId, workOrderNo, LocalDateTime.now(),
                acceptorId, workOrderId, acceptorId);
    }

    private Long insertMaterial(String code, String name) {
        return insertAndReturnId("""
                insert into material_info(material_code, material_name, material_spec, unit, safety_stock_qty)
                values (?, ?, '20kg/桶', '桶', 10.000)
                """, code, name);
    }

    private Long insertMaterialQrcode(Long materialId, String materialCode, String qrcodeValue) {
        return insertAndReturnId("""
                insert into material_qrcode(material_id, material_code, qrcode_value)
                values (?, ?, ?)
                """, materialId, materialCode, qrcodeValue);
    }

    private Long insertMaterialOutRecord(Long workOrderId, String workOrderNo, BigDecimal quantity) {
        return insertAndReturnId("""
                insert into material_inout_record(record_no, material_id, material_code, material_name, qrcode_id,
                  qrcode_value, project_id, work_order_id, work_order_no, inout_type, quantity, before_qty,
                  after_qty, operator_id)
                values (?, ?, 'MAT-ANTICORROSION', '防腐涂料', ?, 'QR-MAT-001', ?, ?, ?, 'OUT',
                  ?, 100.000, 87.500, ?)
                """, "OUT-" + workOrderNo, materialId, qrcodeId, projectId, workOrderId, workOrderNo,
                quantity, maintainerId);
    }

    private Long insertMaterialUsage(Long workOrderId, String workOrderNo, Long inoutRecordId, BigDecimal usedQty) {
        BigDecimal costAmount = usedQty.multiply(new BigDecimal("50.0000"));
        return insertAndReturnId("""
                insert into work_order_material_usage(usage_no, work_order_id, work_order_no, project_id,
                  material_id, material_code, material_name, qrcode_id, qrcode_value, used_qty, usage_time,
                  usage_user_id, cost_price, cost_amount, inout_record_id, local_id, server_id, version,
                  sync_status, device_id, operator_id)
                values (?, ?, ?, ?, ?, 'MAT-ANTICORROSION', '防腐涂料', ?, 'QR-MAT-001', ?, ?,
                  ?, 50.0000, ?, ?, ?, ?, 1, 'SYNCED', 'android-001', ?)
                """, "USE-" + workOrderNo, workOrderId, workOrderNo, projectId, materialId, qrcodeId,
                usedQty, LocalDateTime.now(), maintainerId, costAmount, inoutRecordId,
                "local-use-" + workOrderNo, workOrderId, maintainerId);
    }

    private Long insertEmployee(Long userId, String employeeNo, String realName) {
        return insertAndReturnId("""
                insert into employee_info(user_id, employee_no, real_name)
                values (?, ?, ?)
                """, userId, employeeNo, realName);
    }

    private Long insertQualificationType(String code, String name, int warningDays) {
        return insertAndReturnId("""
                insert into qualification_type(qualification_code, qualification_name, warning_days, required_flag)
                values (?, ?, ?, 1)
                """, code, name, warningDays);
    }

    private void insertDevice(String deviceId, Long userId) {
        jdbcTemplate.update("""
                insert into device_info(device_id, user_id, device_name, app_version, online_status)
                values (?, ?, '测试Android设备', '1.0.0', 'ONLINE')
                """, deviceId, userId);
    }

    private Long insertSyncTask(String taskNo, String batchId, String status) {
        return insertAndReturnId("""
                insert into sync_task(sync_task_no, batch_id, device_id, operator_id, sync_direction,
                  sync_type, task_status, total_count, success_count, failed_count, conflict_count, idempotency_key)
                values (?, ?, 'android-001', ?, 'PUSH', 'INCREMENTAL', ?, 1, 0, 0,
                  case when ? = 'CONFLICT' then 1 else 0 end, ?)
                """, taskNo, batchId, maintainerId, status, status, "idem-" + taskNo);
    }

    private Long insertSyncLog(Long taskId, Long workOrderId, String moduleType, String entityType,
            String actionType, String status) {
        return insertAndReturnId("""
                insert into sync_log(sync_task_id, batch_id, device_id, operator_id, module_type, entity_type,
                  action_type, local_id, server_id, entity_id, work_order_id, business_no, client_version,
                  server_version, sync_status)
                values (?, 'BATCH-001', 'android-001', ?, ?, ?, ?, 'local-sync-001', ?, ?, ?,
                  ?, 2, 3, ?)
                """, taskId, maintainerId, moduleType, entityType, actionType, workOrderId, workOrderId,
                workOrderId, workOrderBusinessNo(workOrderId), status);
    }

    private Long insertSyncConflict(Long taskId, Long logId, Long workOrderId) {
        return insertAndReturnId("""
                insert into sync_conflict(conflict_no, sync_task_id, sync_log_id, device_id, operator_id,
                  module_type, entity_type, entity_id, local_id, server_id, work_order_id, business_no,
                  base_version, client_version, server_version, conflict_type, conflict_fields, old_payload,
                  client_payload, server_payload, resolve_status)
                values ('CONF-001', ?, ?, 'android-001', ?, 'WORK_ORDER', 'work_order', ?, 'local-sync-001',
                  ?, ?, ?, 1, 2, 3, 'VERSION_CONFLICT', '["status"]', '{}', '{}', '{}', 'PENDING_REVIEW')
                """, taskId, logId, maintainerId, workOrderId, workOrderId, workOrderId,
                workOrderBusinessNo(workOrderId));
    }

    private Long insertAiModel() {
        return insertAndReturnId("""
                insert into ai_model_info(model_code, model_name, model_version, active_flag, model_status)
                values ('ANTICORROSION_DEFECT', '防腐层缺陷识别模型', '1.0.0', 1, 'ACTIVE')
                """);
    }

    private Long insertAiResult(Long workOrderId, Long recordId, Long attachmentId, Long modelId) {
        String workOrderNo = workOrderBusinessNo(workOrderId);
        return insertAndReturnId("""
                insert into ai_result(ai_result_no, work_order_id, work_order_no, project_id, record_id,
                  attachment_id, file_id, model_id, model_code, model_version, infer_side, infer_time,
                  infer_cost_ms, defect_type, confidence, suspected_defect_flag, defect_count,
                  review_status, local_id, server_id, version, sync_status, device_id, operator_id)
                values ('AI-001', ?, ?, ?, ?, ?, ?, ?, 'ANTICORROSION_DEFECT', '1.0.0', 'MOBILE',
                  ?, 180, 'RUST', 0.930000, 1, 1, 'PENDING_REVIEW', 'local-AI-001', ?, 1,
                  'PENDING', 'android-001', ?)
                """, workOrderId, workOrderNo, projectId, recordId, attachmentId,
                fileIdByAttachment(attachmentId), modelId, LocalDateTime.now(), workOrderId, maintainerId);
    }

    private String fileIdByAttachment(Long attachmentId) {
        return jdbcTemplate.queryForObject(
                "select file_id from work_order_attachment where id = ?", String.class, attachmentId);
    }

    private String workOrderBusinessNo(Long workOrderId) {
        return jdbcTemplate.queryForObject(
                "select work_order_no from work_order where id = ?", String.class, workOrderId);
    }

    private String mimeType(String fileType) {
        return switch (fileType) {
            case "PHOTO", "AI_IMAGE" -> "image/jpeg";
            case "PDF" -> "application/pdf";
            case "SIGNATURE" -> "image/png";
            default -> "application/octet-stream";
        };
    }

    private Long insertAndReturnId(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"ID"});
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        assertThat(key).as("generated id for sql: %s", sql).isNotNull();
        return key.longValue();
    }
}
