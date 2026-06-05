package com.offshore.platform.mapper;

import com.offshore.platform.entity.KnowledgeCase;
import java.util.List;

public interface KnowledgeCaseMapper {
    int insert(KnowledgeCase knowledgeCase);
    int updateById(KnowledgeCase knowledgeCase);
    KnowledgeCase selectById(Long id);
    List<KnowledgeCase> selectAll();
    int softDeleteById(Long id);
}
