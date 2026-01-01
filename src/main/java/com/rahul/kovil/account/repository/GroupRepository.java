package com.rahul.kovil.account.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rahul.kovil.account.entity.Group;
import com.rahul.kovil.common.enums.BaseStatus;

public interface GroupRepository extends JpaRepository<Group, Long> {

	@Query("SELECT g FROM Group g WHERE g.tenantId IN :tids AND g.status = :status ORDER BY g.orderNo DESC")
	List<Group> findByTenanIdInAndStatus(List<String> tids, BaseStatus status);

	@Query("SELECT g.id, g.groupType FROM Group g WHERE g.id IN :groupIds")
	List<Object[]> findGroupTypeIdById(Set<Long> groupIds);

}
