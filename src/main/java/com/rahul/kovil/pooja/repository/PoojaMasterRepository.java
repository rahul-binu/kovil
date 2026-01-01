package com.rahul.kovil.pooja.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.pooja.entity.PoojaMaster;

@Repository
public interface PoojaMasterRepository extends JpaRepository<PoojaMaster, Long> {

	List<PoojaMaster> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, BaseStatus status);

	Optional<PoojaMaster> findByIdAndTenantId(Long id, String tenantId);

}
