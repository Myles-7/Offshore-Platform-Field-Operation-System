package com.offshore.platform.common.sync;

import java.time.LocalDateTime;

/**
 * Unified interface for all entities that participate in offline sync.
 *
 * Each syncable entity must implement these methods so that SyncServiceImpl
 * can fill create/update/delete sync fields without instanceof chains.
 *
 * Implementation note: most entities already have these getters/setters.
 * WorkOrder lacks conflictFlag — it defaults to 0 via default method.
 */
public interface SyncableEntity {

    Long getId();
    void setId(Long id);

    String getLocalId();
    void setLocalId(String localId);

    Long getServerId();
    void setServerId(Long serverId);

    Integer getVersion();
    void setVersion(Integer version);

    String getSyncStatus();
    void setSyncStatus(String syncStatus);

    String getDeviceId();
    void setDeviceId(String deviceId);

    Long getOperatorId();
    void setOperatorId(Long operatorId);

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);

    /**
     * Last-updater ID — required for sync audit trail.
     * Entities that already have setUpdatedBy/getUpdatedBy should map to those;
     * others can use a no-op default.
     */
    default Long getUpdatedBy() {
        return null;
    }

    default void setUpdatedBy(Long updatedBy) {
        // no-op default: entities without this column silently ignore
    }

    Integer getDeletedFlag();
    void setDeletedFlag(Integer deletedFlag);

    /**
     * Conflict flag: 0 = no conflict, 1 = has conflict.
     *
     * Entities that do not have a physical conflictFlag column
     * (e.g. WorkOrder) should override this default to return 0
     * and silently ignore the setter to keep backward compatibility.
     */
    default Integer getConflictFlag() {
        return 0;
    }

    default void setConflictFlag(Integer conflictFlag) {
        // no-op default: entities without this column silently ignore
    }
}
