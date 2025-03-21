package com.amalitech.aws_project_lab_two.repository;

import com.amalitech.aws_project_lab_two.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    void deleteByName(String name);
    Image findByName(String name);
}