package com.breakupstories.repository;

import com.breakupstories.model.ShortVideoComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortVideoCommentRepository extends MongoRepository<ShortVideoComment, String> {

    Page<ShortVideoComment> findByVideoIdAndActiveTrue(String videoId, Pageable pageable);

    Page<ShortVideoComment> findByVideoIdAndActiveTrueAndParentIdIsNull(String videoId, Pageable pageable);

    Page<ShortVideoComment> findByVideoIdAndActiveTrueAndParentId(String videoId, String parentId, Pageable pageable);

    java.util.List<ShortVideoComment> findByParentId(String parentId);
}
