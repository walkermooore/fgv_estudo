package com.fgv.studyhub.repository;
import com.fgv.studyhub.entity.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface QuestionRepository extends JpaRepository<Question,Long>{
 long countByTopicId(Long topicId);
 List<Question> findByTopicIdOrderByCreatedAtAsc(Long topicId);
 List<Question> findByTopicIdOrderByCreatedAtAsc(Long topicId, Pageable pageable);
 @Query(value="select * from question order by random() limit :amount", nativeQuery=true) List<Question> findRandom(@Param("amount") int amount);
}
