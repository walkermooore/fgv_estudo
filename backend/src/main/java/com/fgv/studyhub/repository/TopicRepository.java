package com.fgv.studyhub.repository;
import com.fgv.studyhub.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TopicRepository extends JpaRepository<Topic,Long>{ Optional<Topic> findByNormalizedName(String normalizedName); }
