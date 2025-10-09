package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepo extends JpaRepository<Package,Integer> {
}
