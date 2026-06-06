package com.offshore.platform.vo.sync;

import java.util.ArrayList;
import java.util.List;

public class SyncPullVO {
    /** 服务端生成的游标，移动端下次 pull 时回传 */
    public String cursor;
    /** 服务端时间 "yyyy-MM-dd HH:mm:ss" */
    public String serverTime;
    /** 下一页游标，移动端下次 pull 时回传此值 */
    public String nextCursor;
    /** 是否还有更多数据 */
    public Boolean hasMore;
    /** 是否需要移动端在成功写入本地后调用 sync/ack 确认 */
    public Boolean ackRequired = true;
    /** 结构化的增量数据项列表 */
    public List<SyncPullItemVO> items = new ArrayList<>();
}
